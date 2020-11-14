/*
 *  Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.alibaba.nacos.core.distributed.raft.processor;

import com.alibaba.nacos.consistency.Serializer;
import com.alibaba.nacos.consistency.entity.WriteRequest;
import com.alipay.sofa.jraft.rpc.RpcContext;
import com.alipay.sofa.jraft.rpc.RpcProcessor;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class NacosWriteRequestProcessor extends AbstractProcessor implements RpcProcessor<WriteRequest> {
    
    public NacosWriteRequestProcessor(Serializer serializer) {
        super(serializer);
    }
    
    @Override
    public void handleRequest(RpcContext rpcCtx, WriteRequest request) {
    
    }
    
    @Override
    public String interest() {
        return null;
    }
}
