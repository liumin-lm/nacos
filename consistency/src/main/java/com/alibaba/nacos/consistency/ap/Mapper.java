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

package com.alibaba.nacos.consistency.ap;

import com.alibaba.nacos.consistency.cluster.Node;
import com.alibaba.nacos.consistency.cluster.NodeManager;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface Mapper {

    /**
     * Injecting cluster node management
     *
     * @param nodeManager {@link NodeManager}
     */
    void injectNodeManager(NodeManager nodeManager);

    /**
     * Distro calling function provided to the business party, passing
     * in custom rule Supplier and key information
     *
     * @param key origin key
     * @param suppliers customer distro rules
     * @return can distro
     */
    boolean responsibleByCustomerRule(String key, Supplier<Boolean>... suppliers);

    /**
     * Determine if this key can be responsible
     *
     * @param key origin key
     * @return can responsible
     */
    boolean responsible(String key);

    /**
     * Distributed to specific Nodes based on keys
     *
     * @param key origin key
     * @return target server
     */
    Node mapSrv(String key);
}
