/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.auth;

import com.alibaba.nacos.auth.context.IdentityContext;
import com.alibaba.nacos.auth.exception.AccessException;
import com.alibaba.nacos.auth.model.Permission;

/**
 * Auth service.
 *
 * @author Wuyfee
 */
public interface AuthService {
    
    /**
     * Authentication of request, identify the user who request the resource.
     *
     * @param requestContext where we can find the user information
     * @return boolean if the user identify success
     * @throws AccessException if authentication is failed
     */
    Boolean login(IdentityContext requestContext) throws AccessException;
    
    
    /**
     * identity whether the user has the resource authority.
     * @param requestContext  where we can find the user information.
     * @param permission permission to auth.
     * @return Boolean if the user has the resource authority.
     */
    Boolean authorityAccess(IdentityContext requestContext, Permission permission);
    
    /**
     * idetity type.
     * @return GrantTypes auth grant type.
     */
    String getType();
    
}
