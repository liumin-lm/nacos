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

package com.alibaba.nacos.api.config.remote.response;

import com.alibaba.nacos.api.remote.response.ResponseTypeConstants;

/**
 * response type defined in config module.
 * @author liuzunfei
 * @version $Id: ConfigResponseTypeConstants.java, v 0.1 2020年07月14日 3:10 PM liuzunfei Exp $
 */
public class ConfigResponseTypeConstants extends ResponseTypeConstants {
    
    public static final String CONFIG_CHANGE_BATCH = "CONFIG_CHANGE_BATCH";
    
    public static final String CONFIG_QUERY = "CONFIG_QUERY";
    
    public static final String CONFIG_PUBLISH = "CONFIG_PUBLISH";
    
    public static final String CONFIG_REMOVE = "CONFIG_REMOVE";
    
    public static final String CONFIG_NOTIFY = "CONFIG_NOTIFY";
    
}
