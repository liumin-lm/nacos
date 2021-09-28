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

package com.alibaba.nacos.naming.monitor;

import com.alibaba.nacos.metrics.manager.MetricsManager;
import com.alibaba.nacos.metrics.manager.NamingMetricsConstant;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetricsMonitorTest {
    
    @Test
    public void testGetTotalPush() {
        assertEquals(0, MetricsManager.gauge(NamingMetricsConstant.NACOS_MONITOR,
                NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                NamingMetricsConstant.NAME, NamingMetricsConstant.TOTAL_PUSH).get());
        assertEquals(1, MetricsManager.gauge(NamingMetricsConstant.NACOS_MONITOR,
                NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                NamingMetricsConstant.NAME, NamingMetricsConstant.TOTAL_PUSH).incrementAndGet());
    }
    
    @Test
    public void testGetFailedPush() {
        assertEquals(0, MetricsManager.gauge(NamingMetricsConstant.NACOS_MONITOR,
                NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                NamingMetricsConstant.NAME, NamingMetricsConstant.FAILED_PUSH).get());
        assertEquals(1, MetricsManager.gauge(NamingMetricsConstant.NACOS_MONITOR,
                NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                NamingMetricsConstant.NAME, NamingMetricsConstant.FAILED_PUSH).incrementAndGet());
    }
}
