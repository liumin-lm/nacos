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

package com.alibaba.nacos.plugin.datasource.mapper.impl;

import com.alibaba.nacos.plugin.datasource.mapper.base.ConfigTagsRelationMapper;

/**
 * The abstract ConfigTagsRelationMapper.
 * To implement the default method of the ConfigTagsRelationMapper interface.
 * @author hyx
 **/

public class AbstractConfigTagsRelationMapper implements ConfigTagsRelationMapper {
    
    @Override
    public boolean addConfigTagRelationAtomic(long configId, String tagName, String dataId, String group,
            String tenant) {
        return false;
    }
    
    @Override
    public boolean addConfigTagsRelation(long configId, String configTags, String dataId, String group, String tenant) {
        return false;
    }
    
    @Override
    public Integer removeTagByIdAtomic(long id) {
        return null;
    }
}
