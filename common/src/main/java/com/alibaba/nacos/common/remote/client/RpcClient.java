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

package com.alibaba.nacos.common.remote.client;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.remote.PayloadRegistry;
import com.alibaba.nacos.api.remote.RequestCallBack;
import com.alibaba.nacos.api.remote.RequestFuture;
import com.alibaba.nacos.api.remote.request.ConnectResetRequest;
import com.alibaba.nacos.api.remote.request.Request;
import com.alibaba.nacos.api.remote.request.RequestMeta;
import com.alibaba.nacos.api.remote.response.ConnectResetResponse;
import com.alibaba.nacos.api.remote.response.ConnectionUnregisterResponse;
import com.alibaba.nacos.api.remote.response.Response;
import com.alibaba.nacos.api.utils.NetUtils;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.remote.ConnectionType;
import com.alibaba.nacos.common.utils.LoggerUtils;
import com.alibaba.nacos.common.utils.NumberUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.common.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static com.alibaba.nacos.api.exception.NacosException.SERVER_ERROR;

/**
 * abstract remote client to connect to server.
 *
 * @author liuzunfei
 * @version $Id: RpcClient.java, v 0.1 2020年07月13日 9:15 PM liuzunfei Exp $
 */
@SuppressWarnings("PMD.AbstractClassShouldStartWithAbstractNamingRule")
public abstract class RpcClient implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
    
    private ServerListFactory serverListFactory;
    
    protected LinkedBlockingQueue<ConnectionEvent> eventLinkedBlockingQueue = new LinkedBlockingQueue<ConnectionEvent>();
    
    protected volatile AtomicReference<RpcClientStatus> rpcClientStatus = new AtomicReference<RpcClientStatus>(
            RpcClientStatus.WAIT_INIT);
    
    protected ScheduledExecutorService executorService;
    
    protected volatile Connection currentConnection;
    
    protected Map<String, String> labels = new HashMap<String, String>();
    
    private String name;
    
    /**
     * listener called where connection's status changed.
     */
    protected List<ConnectionEventListener> connectionEventListeners = new ArrayList<ConnectionEventListener>();
    
    /**
     * handlers to process server push request.
     */
    protected List<ServerRequestHandler> serverRequestHandlers = new ArrayList<ServerRequestHandler>();
    
    static {
        PayloadRegistry.init();
    }
    
    public RpcClient(String name) {
        this.name = name;
    }
    
    protected RequestMeta buildMeta() {
        RequestMeta meta = new RequestMeta();
        meta.setClientVersion(VersionUtils.getFullClientVersion());
        meta.setClientIp(NetUtils.localIP());
        meta.setLabels(labels);
        return meta;
    }
    
    public RpcClient(ServerListFactory serverListFactory) {
        this.serverListFactory = serverListFactory;
        rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
        LoggerUtils.printIfInfoEnabled(LOGGER, "RpcClient init in constructor, ServerListFactory ={}",
                serverListFactory.getClass().getName());
    }
    
    public RpcClient(String name, ServerListFactory serverListFactory) {
        this(name);
        this.serverListFactory = serverListFactory;
        rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
        LoggerUtils.printIfInfoEnabled(LOGGER, "RpcClient init in constructor, ServerListFactory ={}",
                serverListFactory.getClass().getName());
    }
    
    /**
     * Notify when client disconnected.
     */
    protected void notifyDisConnected() {
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        LoggerUtils.printIfInfoEnabled(LOGGER, "Notify disconnected event to listeners");
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            connectionEventListener.onDisConnect();
        }
    }
    
    /**
     * Notify when client new connected.
     */
    protected void notifyConnected() {
        if (connectionEventListeners.isEmpty()) {
            return;
        }
        LoggerUtils.printIfInfoEnabled(LOGGER, "Notify connected event to listeners.");
        for (ConnectionEventListener connectionEventListener : connectionEventListeners) {
            connectionEventListener.onConnected();
        }
    }
    
    /**
     * check is this client is initiated.
     *
     * @return is wait initiated or not.
     */
    public boolean isWaitInitiated() {
        return this.rpcClientStatus.get() == RpcClientStatus.WAIT_INIT;
    }
    
    /**
     * check is this client is running.
     *
     * @return is running or not.
     */
    public boolean isRunning() {
        return this.rpcClientStatus.get() == RpcClientStatus.RUNNING;
    }
    
    /**
     * check is this client is shutdown.
     *
     * @return is shutdown or not.
     */
    public boolean isShutdown() {
        return this.rpcClientStatus.get() == RpcClientStatus.SHUTDOWN;
    }
    
    /**
     * init server list factory.only can init once.
     *
     * @param serverListFactory serverListFactory
     */
    public void init(ServerListFactory serverListFactory) {
        if (!isWaitInitiated()) {
            return;
        }
        this.serverListFactory = serverListFactory;
        rpcClientStatus.compareAndSet(RpcClientStatus.WAIT_INIT, RpcClientStatus.INITIALIZED);
        
        LoggerUtils.printIfInfoEnabled(LOGGER, "RpcClient init, ServerListFactory ={}",
                serverListFactory.getClass().getName());
    }
    
    /**
     * init labels.
     *
     * @param labels labels
     */
    public void initLabels(Map<String, String> labels) {
        this.labels.putAll(labels);
        LoggerUtils.printIfInfoEnabled(LOGGER, "RpcClient init label, labels={}", this.labels);
    }
    
    /**
     * Start this client.
     */
    public final void start() throws NacosException {
        
        boolean success = rpcClientStatus.compareAndSet(RpcClientStatus.INITIALIZED, RpcClientStatus.STARTING);
        if (!success) {
            return;
        }
        
        executorService = new ScheduledThreadPoolExecutor(5, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("com.alibaba.nacos.client.remote.worker");
                t.setDaemon(true);
                return t;
            }
        });
        
        // connection event consumer.
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ConnectionEvent take = null;
                    try {
                        take = eventLinkedBlockingQueue.take();
                        if (take.isConnected()) {
                            notifyConnected();
                        } else if (take.isDisConnected()) {
                            notifyDisConnected();
                        }
                    } catch (Exception e) {
                        //Do nothing
                    }
                }
            }
        });
        
        //connect to server ,try to connect to server sync once, async starting if fail.
        Connection connectToServer = null;
        rpcClientStatus.set(RpcClientStatus.STARTING);
        
        int startUpRetryTimes = 3;
        while (startUpRetryTimes > 0 && connectToServer == null) {
            try {
                startUpRetryTimes--;
                ServerInfo serverInfo = nextRpcServer();
                
                LoggerUtils.printIfInfoEnabled(LOGGER,
                        String.format("[%s] try to connect to server on start up, server: %s", name, serverInfo));
                
                connectToServer = connectToServer(serverInfo);
            } catch (Exception e) {
                LoggerUtils.printIfWarnEnabled(LOGGER, String.format(
                        "Fail to connect to server on start up, error message=%s, start up retry times left: %s",
                        e.getMessage(), startUpRetryTimes));
            }
        }
        
        if (connectToServer != null) {
            LoggerUtils
                    .printIfInfoEnabled(LOGGER, String.format("[%s] success to connect to server on start up", name));
            this.currentConnection = connectToServer;
            rpcClientStatus.set(RpcClientStatus.RUNNING);
            eventLinkedBlockingQueue.offer(new ConnectionEvent(ConnectionEvent.CONNECTED));
        } else {
            switchServerAsync();
        }
        
        registerServerRequestHandler(new ServerRequestHandler() {
            @Override
            public Response requestReply(Request request, RequestMeta requestMeta) {
                if (request instanceof ConnectResetRequest) {
                    
                    try {
                        synchronized (this) {
                            if (isRunning()) {
                                ConnectResetRequest connectResetRequest = (ConnectResetRequest) request;
                                if (StringUtils.isNotBlank(connectResetRequest.getServerIp()) && NumberUtil
                                        .isDigits(connectResetRequest.getServerPort())) {
                                    
                                    ServerInfo serverInfo = new ServerInfo();
                                    
                                    serverInfo.setServerIp(connectResetRequest.getServerIp());
                                    serverInfo.setServerPort(
                                            Integer.valueOf(connectResetRequest.getServerPort()) + rpcPortOffset());
                                    switchServerAsync(serverInfo);
                                } else {
                                    switchServerAsync();
                                }
                            }
                        }
                    } catch (Exception e) {
                        LoggerUtils.printIfErrorEnabled(LOGGER, "Switch server error ", e);
                    }
                    return new ConnectResetResponse();
                }
                return null;
            }
            
        });
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    RpcClient.this.shutdown();
                } catch (NacosException e) {
                    e.printStackTrace();
                }
                
            }
        });
        
    }
    
    @Override
    public void shutdown() throws NacosException {
        executorService.shutdown();
        rpcClientStatus.set(RpcClientStatus.SHUTDOWN);
        closeConnection(currentConnection);
    }
    
    private final ReentrantLock switchingLock = new ReentrantLock();
    
    private volatile AtomicBoolean switchingFlag = new AtomicBoolean(false);
    
    public void switchServerAsync() {
        switchServerAsync(null);
    }
    
    /**
     * switch server .
     */
    protected void switchServerAsync(final ServerInfo recommendServerInfo) {
        
        //return if is in switching of other thread.
        if (switchingFlag.get()) {
            return;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                
                try {
                    
                    AtomicReference<ServerInfo> recommendServer = new AtomicReference<ServerInfo>(recommendServerInfo);
                    //only one thread can execute switching meantime.
                    boolean innerLock = switchingLock.tryLock();
                    if (!innerLock) {
                        return;
                    }
                    switchingFlag.compareAndSet(false, true);
                    // loop until start client success.
                    boolean switchSuccess = false;
                    
                    int reConnectTimes = 0;
                    int retryTurns = 0;
                    Exception lastException = null;
                    while (!switchSuccess && !isShutdown()) {
                        
                        //1.get a new server
                        ServerInfo serverInfo = null;
                        //2.create a new channel to new server
                        try {
                            serverInfo = recommendServer.get() == null ? nextRpcServer() : recommendServer.get();
                            
                            Connection connectNew = connectToServer(serverInfo);
                            if (connectNew != null) {
                                LoggerUtils.printIfInfoEnabled(LOGGER,
                                        String.format("[%s] success to connect server : %s", name, serverInfo));
                                //successfully create a new connect.
                                if (currentConnection != null) {
                                    currentConnection.setAbandon(true);
                                    closeConnection(currentConnection);
                                }
                                currentConnection = connectNew;
                                rpcClientStatus.set(RpcClientStatus.RUNNING);
                                switchSuccess = true;
                                boolean s = eventLinkedBlockingQueue
                                        .add(new ConnectionEvent(ConnectionEvent.CONNECTED));
                                return;
                            }
                            
                            //close connetion if client is already shutdown.
                            if (isShutdown()) {
                                closeConnection(connectNew);
                            }
                            
                            lastException = null;
                            
                        } catch (Exception e) {
                            lastException = e;
                        } finally {
                            recommendServer.set(null);
                        }
                        
                        if (reConnectTimes > 0
                                && reConnectTimes % RpcClient.this.serverListFactory.getServerList().size() == 0) {
                            LoggerUtils.printIfInfoEnabled(LOGGER, String.format(
                                    "[%s] fail to connect server,after trying %s times, last try server is %s", name,
                                    reConnectTimes, serverInfo));
                            if (Integer.MAX_VALUE == retryTurns) {
                                retryTurns = 10;
                            } else {
                                retryTurns++;
                            }
                        }
                        
                        reConnectTimes++;
                        
                        try {
                            //sleep 100 millsecond to switch next server.
                            if (!isRunning()) {
                                // first round ,try servers at a delay 100ms;second round ,200ms; max delays 1s. to be reconsidered.基本上会快速收敛到几个可用的IP
                                Thread.sleep(Math.min(retryTurns + 1, 10) * 100L);
                            }
                        } catch (InterruptedException e) {
                            // Do  nothing.
                        }
                    }
                    
                    if (isShutdown()) {
                        LoggerUtils.printIfInfoEnabled(LOGGER,
                                String.format("[%s] client is shutdown ,stop reconnect to server", name));
                    }
                    
                } catch (Exception e) {
                    LoggerUtils.printIfWarnEnabled(LOGGER, String.format("[%s] fail to  connect to server", name));
                } finally {
                    switchingFlag.set(false);
                    switchingLock.unlock();
                }
            }
        });
    }
    
    private void closeConnection(Connection connection) {
        if (connection != null) {
            connection.close();
            eventLinkedBlockingQueue.add(new ConnectionEvent(ConnectionEvent.DISCONNECTED));
        }
    }
    
    /**
     * get connection type of this client.
     *
     * @return ConnectionType.
     */
    public abstract ConnectionType getConnectionType();
    
    /**
     * increase offset of the nacos server port for the rpc server port.
     *
     * @return rpc port offset
     */
    public abstract int rpcPortOffset();
    
    /**
     * get current server.
     *
     * @return server info.
     */
    public ServerInfo getCurrentServer() {
        if (this.currentConnection != null) {
            return currentConnection.serverInfo;
        }
        return null;
    }
    
    /**
     * send request.
     *
     * @param request request.
     * @return response from server.
     */
    public Response request(Request request) throws NacosException {
        return request(request, 3000L);
    }
    
    /**
     * send request.
     *
     * @param request request.
     * @return response from server.
     */
    public Response request(Request request, long timeoutMills) throws NacosException {
        int retryTimes = 3;
        Response response = null;
        Exception exceptionToThrow = null;
        while (retryTimes > 0) {
            try {
                if (this.currentConnection == null || !isRunning()) {
                    throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "Client not connected.");
                }
                response = this.currentConnection.request(request, buildMeta());
                
                if (response != null) {
                    if (response instanceof ConnectionUnregisterResponse) {
                        synchronized (this) {
                            if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
                                switchServerAsync();
                            }
                            throw new IllegalStateException("Invalid client status.");
                        }
                    } else {
                        return response;
                    }
                }
                
            } catch (Exception e) {
                LoggerUtils.printIfErrorEnabled(LOGGER, "Fail to send request, request={}, errorMessage={}", request,
                        e.getMessage());
                exceptionToThrow = e;
            }
            retryTimes--;
            
        }
        
        if (rpcClientStatus.compareAndSet(RpcClientStatus.RUNNING, RpcClientStatus.UNHEALTHY)) {
            switchServerAsync();
        }
        
        if (exceptionToThrow != null) {
            throw new NacosException(SERVER_ERROR, exceptionToThrow);
        }
        return null;
    }
    
    /**
     * send aync request.
     *
     * @param request request.
     */
    public void asyncRequest(Request request, RequestCallBack callback) throws NacosException {
        int retryTimes = 3;
        
        Exception exceptionToThrow = null;
        while (retryTimes > 0) {
            try {
                if (this.currentConnection == null) {
                    throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "Client not connected.");
                }
                this.currentConnection.asyncRequest(request, buildMeta(), callback);
                return;
            } catch (Exception e) {
                LoggerUtils.printIfErrorEnabled(LOGGER, "Fail to send request, request={}, error Message={}", request,
                        e.getMessage());
                exceptionToThrow = e;
            }
            retryTimes--;
            
        }
        if (exceptionToThrow != null) {
            throw new NacosException(SERVER_ERROR, exceptionToThrow);
        }
    }
    
    /**
     * send async request.
     *
     * @param request request.
     * @return request future.
     */
    public RequestFuture requestFuture(Request request) throws NacosException {
        if (this.currentConnection == null) {
            throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "Client not connected.");
        }
        RequestFuture requestFuture = this.currentConnection.requestFuture(request, buildMeta());
        return requestFuture;
        
    }
    
    /**
     * connect to server.
     *
     * @param serverInfo server address to connect.
     * @return return connection when sucussfully connect to server, or null if failed.
     * @throws Exception exception when fail to connect to server.
     */
    public abstract Connection connectToServer(ServerInfo serverInfo) throws Exception;
    
    /**
     * handle server request.
     *
     * @param request request.
     * @return response.
     */
    protected Response handleServerRequest(final Request request, final RequestMeta meta) {
        
        for (ServerRequestHandler serverRequestHandler : serverRequestHandlers) {
            Response response = serverRequestHandler.requestReply(request, meta);
            if (response != null) {
                return response;
            }
        }
        return null;
    }
    
    /**
     * Register connection handler. Will be notified when inner connection's state changed.
     *
     * @param connectionEventListener connectionEventListener
     */
    public synchronized void registerConnectionListener(ConnectionEventListener connectionEventListener) {
        
        LoggerUtils.printIfInfoEnabled(LOGGER, "Registry connection listener to current client:{}",
                connectionEventListener.getClass().getName());
        this.connectionEventListeners.add(connectionEventListener);
    }
    
    /**
     * Register serverRequestHandler, the handler will handle the request from server side.
     *
     * @param serverRequestHandler serverRequestHandler
     */
    public synchronized void registerServerRequestHandler(ServerRequestHandler serverRequestHandler) {
        LoggerUtils.printIfInfoEnabled(LOGGER, "Register server push request handler:{}",
                serverRequestHandler.getClass().getName());
        
        this.serverRequestHandlers.add(serverRequestHandler);
    }
    
    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Getter method for property <tt>serverListFactory</tt>.
     *
     * @return property value of serverListFactory
     */
    public ServerListFactory getServerListFactory() {
        return serverListFactory;
    }
    
    protected ServerInfo nextRpcServer() {
        String serverAddress = getServerListFactory().genNextServer();
        return resolveServerInfo(serverAddress);
    }
    
    protected ServerInfo currentRpcServer() {
        String serverAddress = getServerListFactory().getCurrentServer();
        return resolveServerInfo(serverAddress);
    }
    
    private ServerInfo resolveServerInfo(String serverAddress) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.serverPort = rpcPortOffset();
        if (serverAddress.contains(Constants.HTTP_PREFIX)) {
            serverInfo.serverIp = serverAddress.split(Constants.COLON)[1].replaceAll("//", "");
            serverInfo.serverPort += Integer.valueOf(serverAddress.split(Constants.COLON)[2].replaceAll("//", ""));
        } else {
            serverInfo.serverIp = serverAddress.split(Constants.COLON)[0];
            serverInfo.serverPort += Integer.valueOf(serverAddress.split(Constants.COLON)[1]);
        }
        return serverInfo;
    }
    
    public static class ServerInfo {
        
        protected String serverIp;
        
        protected int serverPort;
        
        public ServerInfo() {
        
        }
        
        /**
         * Setter method for property <tt>serverIp</tt>.
         *
         * @param serverIp value to be assigned to property serverIp
         */
        public void setServerIp(String serverIp) {
            this.serverIp = serverIp;
        }
        
        /**
         * Setter method for property <tt>serverPort</tt>.
         *
         * @param serverPort value to be assigned to property serverPort
         */
        public void setServerPort(int serverPort) {
            this.serverPort = serverPort;
        }
        
        /**
         * Getter method for property <tt>serverIp</tt>.
         *
         * @return property value of serverIp
         */
        public String getServerIp() {
            return serverIp;
        }
        
        /**
         * Getter method for property <tt>serverPort</tt>.
         *
         * @return property value of serverPort
         */
        public int getServerPort() {
            return serverPort;
        }
        
        @Override
        public String toString() {
            return "ServerInfo{" + "serverIp='" + serverIp + '\'' + ", serverPort=" + serverPort + '}';
        }
    }
    
    public class ConnectionEvent {
        
        public static final int CONNECTED = 1;
        
        public static final int DISCONNECTED = 0;
        
        int eventType;
        
        public ConnectionEvent(int eventType) {
            this.eventType = eventType;
        }
        
        public boolean isConnected() {
            return eventType == CONNECTED;
        }
        
        public boolean isDisConnected() {
            return eventType == DISCONNECTED;
        }
    }
    
    /**
     * Getter method for property <tt>labels</tt>.
     *
     * @return property value of labels
     */
    public Map<String, String> getLabels() {
        return labels;
    }
}
