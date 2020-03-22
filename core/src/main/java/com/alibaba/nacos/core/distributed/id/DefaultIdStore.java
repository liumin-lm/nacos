/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.core.distributed.id;

import com.alibaba.nacos.consistency.Config;
import com.alibaba.nacos.consistency.ConsistencyProtocol;
import com.alibaba.nacos.consistency.Log;
import com.alibaba.nacos.consistency.LogFuture;
import com.alibaba.nacos.consistency.SerializeFactory;
import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.consistency.cp.Constants;
import com.alibaba.nacos.consistency.cp.LogProcessor4CP;
import com.alibaba.nacos.consistency.request.GetRequest;
import com.alibaba.nacos.consistency.request.GetResponse;
import com.alibaba.nacos.consistency.snapshot.CallFinally;
import com.alibaba.nacos.consistency.snapshot.Reader;
import com.alibaba.nacos.consistency.snapshot.SnapshotOperation;
import com.alibaba.nacos.consistency.snapshot.Writer;
import com.alibaba.nacos.core.distributed.raft.utils.RaftExecutor;
import com.alibaba.nacos.core.utils.ByteUtils;
import com.alibaba.nacos.core.utils.ConvertUtils;
import com.alibaba.nacos.core.utils.DiskUtils;
import com.alibaba.nacos.core.utils.GlobalExecutor;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.core.utils.ApplicationUtils;
import com.alibaba.nacos.core.utils.ThreadUtils;
import com.alibaba.nacos.core.utils.TimerContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;
import javax.annotation.PostConstruct;

import com.google.protobuf.ByteString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Simple implementation of LeafID based on Raft+File
 *
 * @author <a href="mailto:liaochunyhm@live.com">liaochuntao</a>
 */
@ConditionalOnProperty(value = "nacos.core.idGenerator.type", havingValue = "default")
@Component
@DependsOn("serverMemberManager")
public class DefaultIdStore extends LogProcessor4CP {

    private final File[] EMPTY = new File[0];
    private final String SNAPSHOT_DIR = "id_generator";
    private final String SNAPSHOT_ARCHIVE = "id_generator.zip";
    private final String FILE_PATH = Paths.get(ApplicationUtils.getNacosHome(), "data", "id_generator").toString();
    private long ACQUIRE_STEP;
    private ConsistencyProtocol<? extends Config> protocol;
    private Map<String, IdStoreFile> storeFileMap;
    private Serializer serializer;
    private List<SnapshotOperation> snapshotOperations = Collections.singletonList(new IdSnapshotOperation());

    private volatile boolean hasLeader = false;

    @PostConstruct
    protected void init() throws Exception {
        Loggers.ID_GENERATOR.info("The Leaf-ID start");
        this.storeFileMap = new ConcurrentHashMap<>(4);
        this.serializer = SerializeFactory.getDefault();
        ACQUIRE_STEP =
                ConvertUtils.toLong(ApplicationUtils.getProperty("nacos.core.idGenerator.default.acquire.step"), 1000);

        // Delete existing data, relying on raft's snapshot and log
        // playback to reply to the data is the correct behavior.

        DiskUtils.deleteDirectory(FILE_PATH);
        DiskUtils.forceMkdir(FILE_PATH);
    }

    public boolean isHasLeader() {
        return hasLeader;
    }

    public void firstAcquire(String resource, int maxRetryCnt, DefaultIdGenerator generator, boolean bufferIndex) {
        this.protocol.protocolMetaData()
                .subscribe(group(), Constants.LEADER_META_DATA, new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        GlobalExecutor.executeByCommon(() -> acquireNewIdSequence(resource, maxRetryCnt, generator, bufferIndex));
                    }
                });
    }

    public void acquireNewIdSequence(String resource, int maxRetryCnt, DefaultIdGenerator generator, boolean bufferIndex) {
        storeFileMap.computeIfAbsent(resource, s -> new IdStoreFile(resource));
        for (int i = 0; i < maxRetryCnt; i++) {
            // need read maxId from raft-leader
            long currentMaxId = (long) getData(GetRequest.builder()
                    .group(group())
                    .ctx(ByteUtils.toBytes(resource))
                    .build()).getData();
            final long minId = currentMaxId + 1;
            final long maxId = minId + ACQUIRE_STEP;
            final AcquireId acquireId = AcquireId.builder()
                    .minId(minId)
                    .maxId(maxId)
                    .applicant(resource)
                    .build();
            try {
                Log gLog = Log.newBuilder()
                        .data(acquireId)
                        .build();

                LogFuture future = commitAutoSetGroup(gLog);
                if (future.isOk()) {
                    generator.update(new long[]{minId, maxId});
                    Loggers.ID_GENERATOR.info("[{}] ID application successful, bufferIndex : {}, startId : {}, endId : {}",
                            resource, bufferIndex ? "bufferTwo" : "bufferOne", minId, maxId);
                    return;
                } else {
                    Loggers.ID_GENERATOR.error("[{}] An error occurred while applying for ID, error : {}", resource, future.getError());
                }
            } catch (Exception e) {
                Loggers.ID_GENERATOR.error("[{}] An error occurred while applying for ID, error : {}", resource, e);
            }
            ThreadUtils.sleep(100);
        }
        throw new AcquireIdException("[" + resource + "] The maximum number of retries exceeded");
    }

    @SuppressWarnings("all")
    @Override
    public <D> GetResponse<D> getData(GetRequest request) {
        String resources = request.getCtx();
        IdStoreFile file = storeFileMap.get(resources);
        return GetResponse.<D>builder()
                .data((D) file.getCurrentMaxId())
                .build();
    }

    @SuppressWarnings("all")
    @Override
    public LogFuture onApply(Log log) {
        final AcquireId acquireId = log.getData();
        final String resources = acquireId.getApplicant();
        final long minId = acquireId.getMinId();
        final long maxId = acquireId.getMaxId();
        IdStoreFile storeFile = storeFileMap.get(resources);
        if (storeFile == null) {
            return LogFuture.fail(new NoSuchElementException("The resource does not exist"));
        }
        return LogFuture.success(storeFile.canAccept(maxId));
    }

    @Override
    public void onError(Throwable throwable) {
        Loggers.ID_GENERATOR.error("An error occurred while onApply for ID, error : {}", throwable);
    }

    @Override
    public String group() {
        return "default_id_generator";
    }

    @Override
    public List<SnapshotOperation> loadSnapshotOperate() {
        return snapshotOperations;
    }

    void loadFromFile(File parentFile) throws IOException {
        if (parentFile == null) {
            throw new NullPointerException();
        }

        if (!parentFile.isDirectory()) {
            throw new NotDirectoryException(parentFile.getPath() + " not a file directory");
        }

        for (File file : Optional.ofNullable(parentFile.listFiles()).orElse(EMPTY)) {
            String resourceName = file.getName();
            storeFileMap.computeIfAbsent(resourceName, s -> new IdStoreFile(resourceName));
            IdStoreFile storeFile = storeFileMap.get(resourceName);
            storeFile.forceWrite(file);
        }
    }

    private class IdStoreFile {

        private final File file;

        public IdStoreFile(String resourceName) {
            try {
                file = new File(Paths.get(FILE_PATH, resourceName).toUri());
                DiskUtils.touch(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean canAccept(long maxId) {
            long currentMaxId = getCurrentMaxId();
            if (maxId > currentMaxId) {
                DiskUtils.writeFile(file, ByteUtils.toBytes(maxId), false);
                return true;
            }
            return false;
        }

        void forceWrite(File remoteFile) throws IOException {
            DiskUtils.copyFile(file, remoteFile);
        }

        public Long getCurrentMaxId() {
            String data = DiskUtils.readFile(file);
            if (StringUtils.isBlank(data)) {
                return -1L;
            }
            return Long.parseLong(data);
        }

    }

    class IdSnapshotOperation implements SnapshotOperation {

        @Override
        public void onSnapshotSave(Writer writer, CallFinally callFinally) {
            RaftExecutor.doSnapshot(() -> {

                boolean result = false;
                Throwable throwable = null;
                final String writePath = writer.getPath();
                TimerContext.start("[DefaultIdStore] snapshot save job");
                try {
                    final String parentPath = Paths.get(writePath, SNAPSHOT_DIR).toString();
                    DiskUtils.deleteDirThenMkdir(parentPath);
                    DiskUtils.copyDirectory(new File(FILE_PATH), new File(parentPath));
                    final String outputFile = Paths.get(writePath, SNAPSHOT_ARCHIVE).toString();
                    DiskUtils.compress(writePath, SNAPSHOT_DIR, outputFile, new CRC32());
                    DiskUtils.deleteDirectory(parentPath);
                    writer.addFile(SNAPSHOT_ARCHIVE);
                    result = true;
                } catch (Exception e) {
                    Loggers.ID_GENERATOR.error("path : {}, snapshot execution error : {}", writePath, e);
                    throwable = e;
                } finally {
                    TimerContext.end(Loggers.ID_GENERATOR);
                }

                callFinally.run(result, throwable);

            });
        }

        @Override
        public boolean onSnapshotLoad(Reader reader) {
            final String readerPath = reader.getPath();
            final String sourceFile = Paths.get(readerPath, SNAPSHOT_ARCHIVE).toString();
            TimerContext.start("[DefaultIdStore] snapshot load job");
            try {
                DiskUtils.decompress(sourceFile, readerPath, new CRC32());
                final String loadPath = Paths.get(readerPath, SNAPSHOT_DIR).toString();
                Loggers.ID_GENERATOR.info("snapshot load from : {}", loadPath);
                DefaultIdStore.this.loadFromFile(new File(loadPath));
                DiskUtils.deleteDirectory(loadPath);
                return true;
            } catch (final Throwable t) {
                Loggers.ID_GENERATOR.error("Fail to load snapshot, path={}, file list={}, error : {}.", readerPath,
                        reader.listFiles(), t);
                return false;
            } finally {
                TimerContext.end(Loggers.ID_GENERATOR);
            }
        }
    }
}
