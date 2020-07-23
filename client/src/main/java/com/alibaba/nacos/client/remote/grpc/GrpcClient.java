/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.remote.grpc;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.grpc.GrpcMetadata;
import com.alibaba.nacos.api.grpc.GrpcRequest;
import com.alibaba.nacos.api.grpc.GrpcResponse;
import com.alibaba.nacos.api.grpc.RequestGrpc;
import com.alibaba.nacos.api.grpc.RequestStreamGrpc;
import com.alibaba.nacos.api.remote.ResponseRegistry;
import com.alibaba.nacos.api.remote.request.HeartBeatRequest;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.ServerCheckRequest;
import com.alibaba.nacos.api.remote.response.ConnectResetResponse;
import com.alibaba.nacos.api.remote.response.ConnectionUnregisterResponse;
import com.alibaba.nacos.api.remote.response.PlainBodyResponse;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.remote.response.ResponseTypeConstants;
import com.alibaba.nacos.client.config.impl.ConfigHttpClientManager;
import com.alibaba.nacos.client.config.impl.SpasAdapter;
import com.alibaba.nacos.client.identify.StsConfig;
import com.alibaba.nacos.client.naming.utils.NetUtils;
import com.alibaba.nacos.client.naming.utils.SignUtil;
import com.alibaba.nacos.client.remote.RpcClient;
import com.alibaba.nacos.client.remote.RpcClientStatus;
import com.alibaba.nacos.client.remote.ServerListFactory;
import com.alibaba.nacos.client.remote.ServerPushResponseHandler;
import com.alibaba.nacos.client.security.SecurityProxy;
import com.alibaba.nacos.client.utils.AppNameUtils;
import com.alibaba.nacos.client.utils.ClientCommonUtils;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.client.utils.ParamUtil;
import com.alibaba.nacos.client.utils.TemplateUtils;
import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.utils.ConvertUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * gRPC Client.
 *
 * @author liuzunfei
 * @version $Id: GrpcClient.java, v 0.1 2020年07月13日 9:16 PM liuzunfei Exp $
 */
public class GrpcClient extends RpcClient {
    
    private static final Logger LOGGER = LogUtils.logger(GrpcClient.class);
    
    private SecurityProxy securityProxy;
    
    private StsCredential stsCredential;
    
    protected ManagedChannel channel;
    
    protected RequestStreamGrpc.RequestStreamStub grpcStreamServiceStub;
    
    protected RequestGrpc.RequestBlockingStub grpcServiceStub;
    
    private static final NacosRestTemplate NACOS_RESTTEMPLATE = ConfigHttpClientManager.getInstance()
            .getNacosRestTemplate();
    
    /**
     * Reconnect to current server before switch a new server.
     */
    private static final int MAX_RECONNECT_TIMES = 3;
    
    private AtomicInteger reConnectTimesLeft = new AtomicInteger(MAX_RECONNECT_TIMES);
    
    public GrpcClient() {
        super();
        init(new Properties());
    }
    
    public GrpcClient(ServerListFactory serverListFactory) {
        super(serverListFactory);
        init(new Properties());
    }
    
    public GrpcClient(Properties properties) {
        init(properties);
    }
    
    /**
     * create a new channel .
     *
     * @param serverIp   serverIp.
     * @param serverPort serverPort.
     * @return if server check success,return stub.
     */
    private RequestGrpc.RequestBlockingStub createNewChannelStub(String serverIp, int serverPort) {
    
        ManagedChannel managedChannelTemp = ManagedChannelBuilder.forAddress(serverIp, serverPort).usePlaintext()
                .build();
    
        RequestGrpc.RequestBlockingStub grpcServiceStubTemp = RequestGrpc.newBlockingStub(managedChannelTemp);
    
        boolean checkSucess = serverCheck(grpcServiceStubTemp);
    
        if (checkSucess) {
            return grpcServiceStubTemp;
        } else {
            shuntDownChannel(managedChannelTemp);
            return null;
        }
    
    }
    
    /**
     * shutdown a  channel.
     *
     * @param managedChannel channel to be shutdown.
     */
    private void shuntDownChannel(ManagedChannel managedChannel) {
        if (managedChannel != null && !managedChannel.isShutdown()) {
            managedChannel.shutdownNow();
        }
    }
    
    private void connectToServer() {
        
        rpcClientStatus.compareAndSet(RpcClientStatus.INITED, RpcClientStatus.STARTING);
        GrpcServerInfo serverInfo = currentServer();
        RequestGrpc.RequestBlockingStub newChannelStubTemp = createNewChannelStub(serverInfo.serverIp,
                serverInfo.serverPort);
        if (newChannelStubTemp != null) {
            RequestStreamGrpc.RequestStreamStub requestStreamStubTemp = RequestStreamGrpc
                    .newStub(newChannelStubTemp.getChannel());
            bindRequestStream(requestStreamStubTemp);
            //switch current channel and stub
            channel = (ManagedChannel) newChannelStubTemp.getChannel();
            grpcStreamServiceStub = requestStreamStubTemp;
            grpcServiceStub = newChannelStubTemp;
            rpcClientStatus.set(RpcClientStatus.RUNNING);
            notifyConnected();
        } else {
            switchServer(true);
        }
    }
    
    @Override
    public void start() throws NacosException {
    
        if (rpcClientStatus.get() == RpcClientStatus.WAIT_INIT) {
            LOGGER.error("RpcClient has not init yet, please check init ServerListFactory...");
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "RpcClient not init yet");
        }
        if (rpcClientStatus.get() == RpcClientStatus.RUNNING || rpcClientStatus.get() == RpcClientStatus.STARTING) {
            return;
        }
    
        connectToServer();
    
        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                sendBeat();
            }
        }, 0, 3000, TimeUnit.MILLISECONDS);
    
        super.registerServerPushResponseHandler(new ServerPushResponseHandler() {
            @Override
            public void responseReply(Response response) {
                if (response instanceof ConnectResetResponse) {
                    try {
                    
                        if (!isRunning()) {
                            return;
                        }
                        switchServer(false);
                    } catch (Exception e) {
                        LOGGER.error("rebuildClient error ", e);
                    }
                }
            }
        });
    }
    
    /**
     * switch a new server.
     */
    private void switchServer(final boolean onStarting) {
        
        if (onStarting) {
            // access on startup fail
            rpcClientStatus.set(RpcClientStatus.SWITCHING_SERVER);
            
        } else {
            // access from running status, sendbeat fail or receive reset message from server.
            boolean changeStatusSuccess = rpcClientStatus
                    .compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.SWITCHING_SERVER);
            if (!changeStatusSuccess) {
                return;
            }
        }
        
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                
                // loop until start client success.
                while (!isRunning()) {
                    
                    //1.get a new server
                    GrpcServerInfo serverInfo = nextServer();
                    
                    //2.get a new channel to new server
                    RequestGrpc.RequestBlockingStub newChannelStubTemp = createNewChannelStub(serverInfo.serverIp,
                            serverInfo.serverPort);
                    if (newChannelStubTemp != null) {
                        RequestStreamGrpc.RequestStreamStub requestStreamStubTemp = RequestStreamGrpc
                                .newStub(newChannelStubTemp.getChannel());
                        bindRequestStream(requestStreamStubTemp);
                        final ManagedChannel depratedChannel = channel;
                        //switch current channel and stub
                        channel = (ManagedChannel) newChannelStubTemp.getChannel();
                        grpcStreamServiceStub = requestStreamStubTemp;
                        grpcServiceStub = newChannelStubTemp;
                        rpcClientStatus.getAndSet(RpcClientStatus.RUNNING);
                        if (onStarting) {
                            notifyConnected();
                        } else {
                            notifyReConnected();
                        }
                        shuntDownChannel(depratedChannel);
                        continue;
                    }
                    //
                    try {
                        //sleep 3 second to switch next server.
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        // Do  nothing.
                    }
                }
            }
        }, 0L, TimeUnit.MILLISECONDS);
        
    }
    
    /**
     * Send Heart Beat Request.
     */
    public void sendBeat() {
        try {
        
            if (!isRunning()) {
                return;
            }
            HeartBeatRequest heartBeatRequest = new HeartBeatRequest();
            GrpcRequest streamRequest = GrpcRequest.newBuilder().setMetadata(buildMeta()).setType(heartBeatRequest.getType()).setBody(
                    Any.newBuilder().setValue(ByteString.copyFromUtf8(JacksonUtils.toJson(heartBeatRequest))).build()).build();
            GrpcResponse response = grpcServiceStub.request(streamRequest);
            if (ResponseTypeConstants.CONNECION_UNREGISTER.equals(response.getType())) {
                LOGGER.warn("Send heart beat fail,connection is not registerd,trying to switch server ");
                switchServer(false);
            }
        } catch (StatusRuntimeException e) {
            if (Status.UNAVAILABLE.getCode().equals(e.getStatus().getCode())) {
                LOGGER.warn("Send heart beat fail,server is not avaliable now,trying to switch server ");
                switchServer(false);
                return;
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("Send heart beat error, ", e);
        }
    }
    
    private GrpcMetadata buildMeta() {
        GrpcMetadata meta = GrpcMetadata.newBuilder().setConnectionId(connectionId).setClientIp(NetUtils.localIP())
                .setVersion(ClientCommonUtils.VERSION).build();
    
        return meta;
    }
    
    /**
     * chenck server if ok.
     *
     * @param requestBlockingStub requestBlockingStub used to check server.
     * @return
     */
    private boolean serverCheck(RequestGrpc.RequestBlockingStub requestBlockingStub) {
        try {
    
            ServerCheckRequest serverCheckRequest = new ServerCheckRequest();
            GrpcRequest streamRequest = GrpcRequest.newBuilder().setMetadata(buildMeta())
                    .setType(serverCheckRequest.getType())
                    .setBody(Any.newBuilder().setValue(ByteString.copyFromUtf8(JacksonUtils.toJson(serverCheckRequest)))
                                    .build()).build();
            GrpcResponse response = requestBlockingStub.request(streamRequest);
            return response != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * bind request stream observer (send a connection).
     *
     * @param streamStub streamStub to bind.
     */
    private void bindRequestStream(RequestStreamGrpc.RequestStreamStub streamStub) {
        GrpcRequest streamRequest = GrpcRequest.newBuilder().setMetadata(buildMeta()).build();
        LOGGER.info("GrpcClient send stream request  grpc server,streamRequest:{}", streamRequest);
        streamStub.requestStream(streamRequest, new StreamObserver<GrpcResponse>() {
            @Override
            public void onNext(GrpcResponse grpcResponse) {
    
                LOGGER.debug(" stream response receive  ,original reponse :{}", grpcResponse);
                try {
    
                    String message = grpcResponse.getBody().getValue().toStringUtf8();
                    String type = grpcResponse.getType();
                    String bodyString = grpcResponse.getBody().getValue().toStringUtf8();
                    Class classByType = ResponseRegistry.getClassByType(type);
                    final Response response;
                    if (classByType != null) {
                        response = (Response) JacksonUtils.toObj(bodyString, classByType);
                    } else {
                        PlainBodyResponse myresponse = JacksonUtils.toObj(bodyString, PlainBodyResponse.class);
                        myresponse.setBodyString(bodyString);
                        response = myresponse;
                    }
                    serverPushResponseListeners.forEach(new Consumer<ServerPushResponseHandler>() {
                        @Override
                        public void accept(ServerPushResponseHandler serverPushResponseHandler) {
                            serverPushResponseHandler.responseReply(response);
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("error tp process server push response  :{}", grpcResponse);
                }
            }
            
            @Override
            public void onError(Throwable throwable) {
            }
            
            @Override
            public void onCompleted() {
            }
        });
    }
    
    @Override
    public Response request(Request request) throws NacosException {
    
        if (!this.isRunning()) {
            throw new IllegalStateException("Client is not connected to any server now,please retry later");
        }
        try {
        
            GrpcRequest grpcrequest = GrpcRequest.newBuilder().setMetadata(buildMeta()).setType(request.getType())
                    .setBody(Any.newBuilder().setValue(ByteString.copyFromUtf8(JacksonUtils.toJson(request)))).build();
            GrpcResponse response = grpcServiceStub.request(grpcrequest);
            String type = response.getType();
            String bodyString = response.getBody().getValue().toStringUtf8();
        
            // transfrom grpcResponse to response model
            Class classByType = ResponseRegistry.getClassByType(type);
            if (classByType != null) {
                Object object = JacksonUtils.toObj(bodyString, classByType);
                if (object instanceof ConnectionUnregisterResponse) {
                    switchServer(false);
                    throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "connection is not connected.");
                }
                return (Response) object;
            } else {
                PlainBodyResponse myresponse = JacksonUtils.toObj(bodyString, PlainBodyResponse.class);
                myresponse.setBodyString(bodyString);
                return (PlainBodyResponse) myresponse;
            }
        } catch (StatusRuntimeException e) {
            if (Status.UNAVAILABLE.equals(e.getStatus())) {
                LOGGER.warn("request fail,server is not avaliable now,trying to switch server ");
                switchServer(false);
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("grpc client request error, error message is  ", e.getMessage(), e);
            throw new NacosException(NacosException.SERVER_ERROR, e);
        }
    }
    
    @Override
    public void shutdown() throws NacosException {
        if (this.channel != null && !this.channel.isShutdown()) {
            this.channel.shutdownNow();
        }
    }
    
    private static class StsCredential {
        
        @JsonProperty(value = "AccessKeyId")
        private String accessKeyId;
        
        @JsonProperty(value = "AccessKeySecret")
        private String accessKeySecret;
        
        @JsonProperty(value = "Expiration")
        private Date expiration;
        
        @JsonProperty(value = "SecurityToken")
        private String securityToken;
        
        @JsonProperty(value = "LastUpdated")
        private Date lastUpdated;
        
        @JsonProperty(value = "Code")
        private String code;
        
        public String getAccessKeyId() {
            return accessKeyId;
        }
        
        public Date getExpiration() {
            return expiration;
        }
        
        public Date getLastUpdated() {
            return lastUpdated;
        }
        
        public String getCode() {
            return code;
        }
        
        @Override
        public String toString() {
            return "STSCredential{" + "accessKeyId='" + accessKeyId + '\'' + ", accessKeySecret='" + accessKeySecret
                    + '\'' + ", expiration=" + expiration + ", securityToken='" + securityToken + '\''
                    + ", lastUpdated=" + lastUpdated + ", code='" + code + '\'' + '}';
        }
    }
    
    private void setSecurityParam(Request request) throws Exception {
        
        //Config+Naming
        request.putHeader(Constants.ACCESS_TOKEN, securityProxy.getAccessToken());
        
        //Config //TODO
        //        if (StringUtils.isNotBlank(namespaceId) && !params.containsKey(SpasAdapter.TENANT_KEY)) {
        //            params.put(SpasAdapter.TENANT_KEY, namespaceId);
        //        }
        
        if (StsConfig.getInstance().isStsOn()) {
            StsCredential stsCredential = getStsCredential();
            accessKey = stsCredential.accessKeyId;
            secretKey = stsCredential.accessKeySecret;
            request.putHeader("Spas-SecurityToken", stsCredential.securityToken);
        }
        
        if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)) {
            request.putHeader("Spas-AccessKey", accessKey);
            //针对 group 和 tenant做特殊处理 ，//TODO
            Map<String, String> signHeaders = SpasAdapter.getSignHeaders(request.getHeaders(), secretKey);
            if (signHeaders != null) {
                request.putAllHeader(signHeaders);
            }
        }
        String ts = String.valueOf(System.currentTimeMillis());
        String token = MD5Utils.md5Hex(ts + ParamUtil.getAppKey(), Constants.ENCODE);
        
        request.putHeader(Constants.CLIENT_APPNAME_HEADER, ParamUtil.getAppName());
        request.putHeader(Constants.CLIENT_REQUEST_TS_HEADER, ts);
        request.putHeader(Constants.CLIENT_REQUEST_TOKEN_HEADER, token);
        request.putHeader("exConfigInfo", "true");
        request.putHeader(HttpHeaderConsts.ACCEPT_CHARSET, encode);
        
        //NamingRef
        String ak = getAccessKey();
        String sk = getSecretKey();
        request.putHeader("app", AppNameUtils.getAppName());
        if (StringUtils.isNotBlank(ak) && StringUtils.isNotBlank(sk)) {
            try {
                //TODO
                String serviceName = "";
                String signData = StringUtils.isNotEmpty(serviceName) ? System.currentTimeMillis() + "@@" + serviceName
                        : String.valueOf(System.currentTimeMillis());
                String signature = SignUtil.sign(signData, sk);
                request.putHeader("signature", signature);
                request.putHeader("data", signData);
                request.putHeader("ak", ak);
            } catch (Exception e) {
                NAMING_LOGGER.error("inject ak/sk failed.", e);
            }
        }
    }
    
    private StsCredential getStsCredential() throws Exception {
        boolean cacheSecurityCredentials = StsConfig.getInstance().isCacheSecurityCredentials();
        if (cacheSecurityCredentials && stsCredential != null) {
            long currentTime = System.currentTimeMillis();
            long expirationTime = stsCredential.expiration.getTime();
            int timeToRefreshInMillisecond = StsConfig.getInstance().getTimeToRefreshInMillisecond();
            if (expirationTime - currentTime > timeToRefreshInMillisecond) {
                return stsCredential;
            }
        }
        String stsResponse = getStsResponse();
        StsCredential stsCredentialTmp = JacksonUtils.toObj(stsResponse, new TypeReference<StsCredential>() {
        });
        stsCredential = stsCredentialTmp;
        LOGGER.info("[getSTSCredential] code:{}, accessKeyId:{}, lastUpdated:{}, expiration:{}",
                stsCredential.getCode(), stsCredential.getAccessKeyId(), stsCredential.getLastUpdated(),
                stsCredential.getExpiration());
        return stsCredential;
    }
    
    private static String getStsResponse() throws Exception {
        String securityCredentials = StsConfig.getInstance().getSecurityCredentials();
        if (securityCredentials != null) {
            return securityCredentials;
        }
        String securityCredentialsUrl = StsConfig.getInstance().getSecurityCredentialsUrl();
        try {
            HttpRestResult<String> result = NACOS_RESTTEMPLATE
                    .get(securityCredentialsUrl, Header.EMPTY, Query.EMPTY, String.class);
            
            if (!result.ok()) {
                LOGGER.error(
                        "can not get security credentials, securityCredentialsUrl: {}, responseCode: {}, response: {}",
                        securityCredentialsUrl, result.getCode(), result.getMessage());
                throw new NacosException(NacosException.SERVER_ERROR,
                        "can not get security credentials, responseCode: " + result.getCode() + ", response: " + result
                                .getMessage());
            }
            return result.getData();
        } catch (Exception e) {
            LOGGER.error("can not get security credentials", e);
            throw e;
        }
    }
    
    public String getAccessKey() {
        return SpasAdapter.getAk();
    }
    
    public String getSecretKey() {
        return SpasAdapter.getSk();
    }
    
    private String accessKey;
    
    private String secretKey;
    
    private String encode;
    
    private int maxRetry = 3;
    
    private void init(Properties properties) {
        initEncode(properties);
        initAkSk(properties);
        initMaxRetry(properties);
    }
    
    private void initEncode(Properties properties) {
        encode = TemplateUtils
                .stringEmptyAndThenExecute(properties.getProperty(PropertyKeyConst.ENCODE), new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return Constants.ENCODE;
                    }
                });
    }
    
    private void initMaxRetry(Properties properties) {
        maxRetry = ConvertUtils.toInt(String.valueOf(properties.get(PropertyKeyConst.MAX_RETRY)), Constants.MAX_RETRY);
    }
    
    private void initAkSk(Properties properties) {
        String ramRoleName = properties.getProperty(PropertyKeyConst.RAM_ROLE_NAME);
        if (!StringUtils.isBlank(ramRoleName)) {
            StsConfig.getInstance().setRamRoleName(ramRoleName);
        }
        
        String ak = properties.getProperty(PropertyKeyConst.ACCESS_KEY);
        if (StringUtils.isBlank(ak)) {
            accessKey = SpasAdapter.getAk();
        } else {
            accessKey = ak;
        }
        
        String sk = properties.getProperty(PropertyKeyConst.SECRET_KEY);
        if (StringUtils.isBlank(sk)) {
            secretKey = SpasAdapter.getSk();
        } else {
            secretKey = sk;
        }
    }
    
    private GrpcServerInfo nextServer() {
        getServerListFactory().genNextServer();
        String serverAddress = getServerListFactory().getCurrentServer();
        return resolveServerInfo(serverAddress);
    }
    
    private GrpcServerInfo currentServer() {
        String serverAddress = getServerListFactory().getCurrentServer();
        return resolveServerInfo(serverAddress);
    }
    
    private GrpcServerInfo resolveServerInfo(String serverAddress) {
        GrpcServerInfo serverInfo = new GrpcServerInfo();
        serverInfo.serverPort = 1000;
        if (serverAddress.contains("http")) {
            serverInfo.serverIp = serverAddress.split(":")[1].replaceAll("//", "");
            serverInfo.serverPort += Integer.valueOf(serverAddress.split(":")[2].replaceAll("//", ""));
        } else {
            serverInfo.serverIp = serverAddress.split(":")[0];
            serverInfo.serverPort += Integer.valueOf(serverAddress.split(":")[1]);
        }
        return serverInfo;
    }
    
    class GrpcServerInfo {
        
        String serverIp;
        
        int serverPort;
        
    }
}



