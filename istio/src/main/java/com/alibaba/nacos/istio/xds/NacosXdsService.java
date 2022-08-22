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

package com.alibaba.nacos.istio.xds;

import com.alibaba.nacos.istio.api.ApiGenerator;
import com.alibaba.nacos.istio.api.ApiGeneratorFactory;
import com.alibaba.nacos.istio.common.*;
import com.alibaba.nacos.istio.misc.Loggers;
import com.alibaba.nacos.istio.model.PushContext;
import com.alibaba.nacos.istio.util.NonceGenerator;
import com.google.protobuf.Any;
import io.envoyproxy.envoy.service.discovery.v3.AggregatedDiscoveryServiceGrpc;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryRequest;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.nacos.istio.api.ApiConstants.CLUSTER_V2_TYPE;
import static com.alibaba.nacos.istio.api.ApiConstants.CLUSTER_V3_TYPE;
import static com.alibaba.nacos.istio.api.ApiConstants.ENDPOINT_TYPE;
import static com.alibaba.nacos.istio.api.ApiConstants.MESH_CONFIG_PROTO_PACKAGE;
import static com.alibaba.nacos.istio.api.ApiConstants.SERVICE_ENTRY_PROTO_PACKAGE;

/**
 * @author special.fy
 */
@Service
public class NacosXdsService extends AggregatedDiscoveryServiceGrpc.AggregatedDiscoveryServiceImplBase {

    private final Map<String, AbstractConnection<DiscoveryResponse>> connections = new ConcurrentHashMap<>(16);

    public boolean hasClientConnection() {
        return connections.size() != 0;
    }

    @Autowired
    ApiGeneratorFactory apiGeneratorFactory;

    @Autowired
    NacosResourceManager resourceManager;

    @Override
    public StreamObserver<DiscoveryRequest> streamAggregatedResources(StreamObserver<DiscoveryResponse> responseObserver) {
        // TODO add authN

        // Init snapshot of nacos service info.
        resourceManager.initResourceSnapshot();
        AbstractConnection<DiscoveryResponse> newConnection = new XdsConnection(responseObserver);

        return new StreamObserver<DiscoveryRequest>() {
            private boolean initRequest = true;

            @Override
            public void onNext(DiscoveryRequest discoveryRequest) {
                // init connection
                if (initRequest) {
                    newConnection.setConnectionId(discoveryRequest.getNode().getId());
                    connections.put(newConnection.getConnectionId(), newConnection);
                    initRequest = false;
                }

                process(discoveryRequest, newConnection);
            }

            @Override
            public void onError(Throwable throwable) {
                Loggers.MAIN.error("xds: {} stream error.", newConnection.getConnectionId(), throwable);
                clear();
            }

            @Override
            public void onCompleted() {
                Loggers.MAIN.info("xds: {} stream close.", newConnection.getConnectionId());
                responseObserver.onCompleted();
                clear();
            }

            private void clear() {
                connections.remove(newConnection.getConnectionId());
            }
        };
    }

    public void process(DiscoveryRequest discoveryRequest, AbstractConnection<DiscoveryResponse> connection) {
        if (!shouldPush(discoveryRequest, connection)) {
            return;
        }
    
        PushContext pushContext = new PushContext(resourceManager.getResourceSnapshot(), true, null, null);
        
        DiscoveryResponse response = buildDiscoveryResponse(discoveryRequest.getTypeUrl(), pushContext);
        connection.push(response, connection.getWatchedStatusByType(discoveryRequest.getTypeUrl()));
    }

    private boolean shouldPush(DiscoveryRequest discoveryRequest, AbstractConnection<DiscoveryResponse> connection) {
        String type = discoveryRequest.getTypeUrl();
        String connectionId = connection.getConnectionId();

        // Suitable for bug of istio
        // See https://github.com/istio/istio/pull/34633
        if (type.equals(MESH_CONFIG_PROTO_PACKAGE)) {
            Loggers.MAIN.info("xds: type {} should be ignored.", type);
            return false;
        }

        if (discoveryRequest.getErrorDetail().getCode() != 0) {
            Loggers.MAIN.error("xds: ACK error, connection-id: {}, code: {}, message: {}",
                    connectionId,
                    discoveryRequest.getErrorDetail().getCode(),
                    discoveryRequest.getErrorDetail().getMessage());
            return false;
        }

        WatchedStatus watchedStatus;
        if (discoveryRequest.getResponseNonce().isEmpty()) {
            Loggers.MAIN.info("xds: init request, type {}, connection-id {}, version {}",
                    type, connectionId, discoveryRequest.getVersionInfo());
            watchedStatus = new WatchedStatus();
            watchedStatus.setType(discoveryRequest.getTypeUrl());
            connection.addWatchedResource(discoveryRequest.getTypeUrl(), watchedStatus);

            return true;
        }

        watchedStatus = connection.getWatchedStatusByType(discoveryRequest.getTypeUrl());
        if (watchedStatus == null) {
            Loggers.MAIN.info("xds: reconnect, type {}, connection-id {}, version {}, nonce {}.",
                    type, connectionId, discoveryRequest.getVersionInfo(), discoveryRequest.getResponseNonce());
            watchedStatus = new WatchedStatus();
            watchedStatus.setType(discoveryRequest.getTypeUrl());
            connection.addWatchedResource(discoveryRequest.getTypeUrl(), watchedStatus);

            return true;
        }

        if (!watchedStatus.getLatestNonce().equals(discoveryRequest.getResponseNonce())) {
            Loggers.MAIN.warn("xds: request dis match, type {}, connection-id {}",
                    discoveryRequest.getTypeUrl(),
                    connection.getConnectionId());
            return false;
        }

        // This request is ack, we should record version and nonce.
        watchedStatus.setAckedVersion(discoveryRequest.getVersionInfo());
        watchedStatus.setAckedNonce(discoveryRequest.getResponseNonce());
        Loggers.MAIN.info("xds: ack, type {}, connection-id {}, version {}, nonce {}", type, connectionId,
                discoveryRequest.getVersionInfo(), discoveryRequest.getResponseNonce());
        return false;
    }

    public void handleEvent(ResourceSnapshot resourceSnapshot, Event event) {
        if (connections.size() == 0) {
            return;
        }
        boolean full = resourceSnapshot.getIstioConfig().isFullEnabled();
        PushContext pushContext = new PushContext(resourceSnapshot, true, null, null);
        
        for (AbstractConnection<DiscoveryResponse> connection : connections.values()) {
            //mcp
            WatchedStatus watchedStatus = connection.getWatchedStatusByType(SERVICE_ENTRY_PROTO_PACKAGE);
            if (watchedStatus != null) {
                Loggers.MAIN.info("mcp Pushing");
                DiscoveryResponse serviceEntryResponse = buildDiscoveryResponse(SERVICE_ENTRY_PROTO_PACKAGE, pushContext);
                connection.push(serviceEntryResponse, watchedStatus);
            }
        }
        
        switch (event.getType()) {
            case Service:
                Loggers.MAIN.info("xds: event {} trigger push.", event.getType());
                
                for (AbstractConnection<DiscoveryResponse> connection : connections.values()) {
                    //CDS
                    WatchedStatus cdsWatchedStatus = connection.getWatchedStatusByType(CLUSTER_V3_TYPE);
                    if (cdsWatchedStatus == null) {
                        Loggers.MAIN.info("V2 Cluster Pushing");
                        cdsWatchedStatus = connection.getWatchedStatusByType(CLUSTER_V2_TYPE);
                        if (cdsWatchedStatus != null) {
                            DiscoveryResponse cdsResponse = buildDiscoveryResponse(CLUSTER_V2_TYPE, pushContext);
                            connection.push(cdsResponse, cdsWatchedStatus);
                        }
                    } else {
                        Loggers.MAIN.info("V3 Cluster Pushing");
                        DiscoveryResponse cdsResponse = buildDiscoveryResponse(CLUSTER_V3_TYPE, pushContext);
                        connection.push(cdsResponse, cdsWatchedStatus);
                    }
                }
                break;
            case Endpoint:
                Loggers.MAIN.info("xds: event {} trigger push.", event.getType());
    
                for (AbstractConnection<DiscoveryResponse> connection : connections.values()) {
                    //EDS
                    WatchedStatus edsWatchedStatus = connection.getWatchedStatusByType("Delta_ENDPOINT_TYPE");
                    if (edsWatchedStatus == null) {
                        edsWatchedStatus = connection.getWatchedStatusByType(ENDPOINT_TYPE);
                        if (edsWatchedStatus != null) {
                            DiscoveryResponse edsResponse = buildDiscoveryResponse(ENDPOINT_TYPE, pushContext);
                            connection.push(edsResponse, edsWatchedStatus);
                        }
                    } else {
                        pushContext.setFull(full);
                        DiscoveryResponse edsResponse = buildDiscoveryResponse("Delta_ENDPOINT_TYPE", pushContext);
                        connection.push(edsResponse, edsWatchedStatus);
                    }
                }
                break;
            default:
                Loggers.MAIN.warn("Invalid event {}, ignore it.", event.getType());
        }
    }

    private DiscoveryResponse buildDiscoveryResponse(String type, PushContext pushContext) {
        @SuppressWarnings("unchecked")
        ApiGenerator<Any> generator = (ApiGenerator<Any>) apiGeneratorFactory.getApiGenerator(type);
        List<Any> rawResources = generator.generate(pushContext);

        String nonce = NonceGenerator.generateNonce();
        return DiscoveryResponse.newBuilder()
                .setTypeUrl(type)
                .addAllResources(rawResources)
                .setVersionInfo(pushContext.getVersion())
                .setNonce(nonce).build();
    }
}
