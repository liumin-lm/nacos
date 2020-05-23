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
package com.alibaba.nacos.naming.exception;

import com.alibaba.nacos.common.exception.api.NacosException;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.naming.misc.Loggers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @author nkorange
 */
@ControllerAdvice
public class ResponseExceptionHandler {

    @ExceptionHandler(NacosException.class)
    public ResponseEntity<String> handleNacosException(NacosException e) {
        Loggers.SRV_LOG.error("got exception. {}", e.getErrMsg(), ExceptionUtil.getAllExceptionMsg(e));
        return ResponseEntity.status(e.getErrCode()).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleParameterError(IllegalArgumentException ex) {
        Loggers.SRV_LOG.error("got exception. {}", ex.getMessage(), ExceptionUtil.getAllExceptionMsg(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        Loggers.SRV_LOG.error("got exception.", ExceptionUtil.getAllExceptionMsg(ex));
        String name = ex.getParameterName();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parameter '" + name + "' is missing");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        Loggers.SRV_LOG.error("got exception.", ExceptionUtil.getAllExceptionMsg(e));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionUtil.getAllExceptionMsg(e));
    }
}
