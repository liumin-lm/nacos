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

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.core.cluster.MemberManager;
import com.alibaba.nacos.core.executor.ExecutorFactory;
import com.alibaba.nacos.core.executor.NameThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class GlobalExecutor {

    private static final ExecutorService COMMON_EXECUTOR = ExecutorFactory.newFixExecutorService(
            GlobalExecutor.class.getCanonicalName(),
            4,
            new NameThreadFactory("com.alibaba.nacos.core.common")
    );

    private static final ScheduledExecutorService syncMemberExecutor = ExecutorFactory.newSingleScheduledExecutorService(
            MemberManager.class.getCanonicalName(),
            new NameThreadFactory("com.alibaba.nacos.core.sync-member")
            );

    private static final ScheduledExecutorService cleanMemberExecutor = ExecutorFactory.newSingleScheduledExecutorService(
            MemberManager.class.getCanonicalName(),
            new NameThreadFactory("com.alibaba.nacos.core.sync-member")
    );

    private static final ScheduledExecutorService reportStateExecutor = ExecutorFactory.newSingleScheduledExecutorService(
            MemberManager.class.getCanonicalName(),
            new NameThreadFactory("com.alibaba.nacos.core.sync-member")
    );

    private static final ScheduledExecutorService pingMemberExecutor = ExecutorFactory.newSingleScheduledExecutorService(
            MemberManager.class.getCanonicalName(),
            new NameThreadFactory("com.alibaba.nacos.core.ping-member")
    );

    public static void runWithoutThread(Runnable runnable) {
        runnable.run();
    }

    public static void executeByCommon(Runnable runnable) {
        COMMON_EXECUTOR.execute(runnable);
    }

    public static void scheduleSyncJob(Runnable runnable, long delay) {
        syncMemberExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static void scheduleBroadCastJob(Runnable runnable, long delay) {
        cleanMemberExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static void schedulePullJob(Runnable runnable, long delay) {
        reportStateExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static void schedulePingJob(Runnable runnable, long delay) {
        pingMemberExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

}
