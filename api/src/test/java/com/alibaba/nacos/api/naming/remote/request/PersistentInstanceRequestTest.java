/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.naming.remote.request;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.remote.NamingRemoteConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistentInstanceRequestTest extends BasedNamingRequestTest {

    @Test
    void testSerialize() throws JsonProcessingException {
        PersistentInstanceRequest request = new PersistentInstanceRequest(NAMESPACE, SERVICE, GROUP,
                NamingRemoteConstants.REGISTER_INSTANCE, new Instance());
        String json = mapper.writeValueAsString(request);
        checkSerializeBasedInfo(json);
        assertTrue(json.contains("\"type\":\"" + NamingRemoteConstants.REGISTER_INSTANCE + "\""));
        assertTrue(json.contains("\"instance\":{"));
    }

    @Test
    void testDeserialize() throws JsonProcessingException {
        String json = "{\"headers\":{},\"namespace\":\"namespace\",\"serviceName\":\"service\",\"groupName\":\"group\","
                + "\"type\":\"deregisterInstance\",\"instance\":{\"port\":0,\"weight\":1.0,\"healthy\":true,"
                + "\"enabled\":true,\"ephemeral\":true,\"metadata\":{},\"instanceIdGenerator\":\"simple\","
                + "\"instanceHeartBeatInterval\":5000,\"instanceHeartBeatTimeOut\":15000,\"ipDeleteTimeout\":30000},"
                + "\"module\":\"naming\"}";
        PersistentInstanceRequest actual = mapper.readValue(json, PersistentInstanceRequest.class);
        checkNamingRequestBasedInfo(actual);
        assertEquals(NamingRemoteConstants.DE_REGISTER_INSTANCE, actual.getType());
        assertEquals(new Instance(), actual.getInstance());
    }
}