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

package com.alibaba.nacos.consistency.exception;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class KvException extends Exception {

	private static final long serialVersionUID = 1935132712388069418L;

	public KvException() {
	}

	public KvException(String message) {
		super(message);
	}

	public KvException(String message, Throwable cause) {
		super(message, cause);
	}

	public KvException(Throwable cause) {
		super(cause);
	}

	public KvException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
