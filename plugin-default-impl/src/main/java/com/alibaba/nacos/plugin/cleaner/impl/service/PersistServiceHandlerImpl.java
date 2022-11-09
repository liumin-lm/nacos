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

package com.alibaba.nacos.plugin.cleaner.impl.service;

import com.alibaba.nacos.plugin.cleaner.config.CleanerConfig;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;

/**
 * a handler for persist service.
 * @author vivid
 */
public class PersistServiceHandlerImpl implements PersistService {

    PersistService persistService;

    public PersistServiceHandlerImpl(JdbcTemplate jdbcTemplate, CleanerConfig cleanerConfig) {
        if (isEmbeddedStorage()) {
            persistService = new EmbeddedStoragePersistServiceImpl(jdbcTemplate, cleanerConfig);
        } else {
            persistService = new ExternalStoragePersistServiceImpl(jdbcTemplate, cleanerConfig);
        }
    }

    @Override
    public void removeConfigHistory(Timestamp startTime, int limitSize) {
        persistService.removeConfigHistory(startTime, limitSize);
    }

    @Override
    public int findConfigHistoryCount() {
        return persistService.findConfigHistoryCount();
    }

    private boolean isEmbeddedStorage() {
        return EnvUtil.getStandaloneMode();
    }

}
