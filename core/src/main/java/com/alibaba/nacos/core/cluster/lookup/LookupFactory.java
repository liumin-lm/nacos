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

package com.alibaba.nacos.core.cluster.lookup;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.ApplicationUtils;
import com.alibaba.nacos.core.utils.Loggers;

import java.io.File;
import java.util.Objects;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class LookupFactory {

	static final String DISCOVERY_SWITCH_NAME = "nacos.member.discovery";

	static MemberLookup LOOK_UP = null;

	static LookupType currentLookupType = null;

	enum LookupType {

		/**
		 * File addressing mode
		 */
		FILE_CONFIG(1, "file"),

		/**
		 * Address server addressing mode
		 */
		ADDRESS_SERVER(2, "address-server"),

		/**
		 * Self discovery addressing pattern
		 */
		DISCOVERY(3, "discovery");

		private final int code;
		private final String name;

		LookupType(int code, String name) {
			this.code = code;
			this.name = name;
		}

		public int getCode() {
			return code;
		}

		public String getName() {
			return name;
		}
	}

	public static MemberLookup createLookUp() throws NacosException {
		if (!ApplicationUtils.getStandaloneMode()) {
			LookupType type = chooseLookup();
			LOOK_UP =  find(type);
			currentLookupType = type;
		}
		else {
			LOOK_UP = new StandaloneMemberLookup();
		}
		Loggers.CLUSTER.info("Current addressing mode selection : {}", LOOK_UP.getClass().getSimpleName());
		return LOOK_UP;
	}

	public static MemberLookup switchLookup(String name) throws NacosException {
		LookupType lookupType = LookupType.valueOf(name);
		if (Objects.equals(currentLookupType, lookupType)) {
			return LOOK_UP;
		}
		MemberLookup newLookup = find(lookupType);
		currentLookupType = lookupType;
		LOOK_UP.destroy();
		LOOK_UP = newLookup;
		return LOOK_UP;
	}

	private static MemberLookup find(LookupType type) {
		 if (LookupType.FILE_CONFIG.equals(type)) {
			 LOOK_UP = new FileConfigMemberLookup();
			 return LOOK_UP;
		 }
		if (LookupType.ADDRESS_SERVER.equals(type)) {
			LOOK_UP = new AddressServerMemberLookup();
			return LOOK_UP;
		}
		if (LookupType.DISCOVERY.equals(type)) {
			LOOK_UP = new DiscoveryMemberLookup();
			return LOOK_UP;
		}
		throw new IllegalArgumentException();
	}

	private static LookupType chooseLookup() {
		File file = new File(ApplicationUtils.getClusterConfFilePath());
		if (Boolean.parseBoolean(ApplicationUtils.getProperty(DISCOVERY_SWITCH_NAME, Boolean.toString(false)))) {
			return LookupType.DISCOVERY;
		}
		if (file.exists() || StringUtils.isNotBlank(ApplicationUtils.getMemberList())) {
			return LookupType.FILE_CONFIG;
		}
		return LookupType.ADDRESS_SERVER;
	}

	public static MemberLookup getLookUp() {
		return LOOK_UP;
	}

	public static void destroy() throws NacosException {
		Objects.requireNonNull(LOOK_UP).destroy();
	}

}
