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

package com.alibaba.nacos.core.cluster;

import com.alibaba.nacos.core.utils.ApplicationUtils;
import com.alibaba.nacos.core.utils.Loggers;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class MemberUtils {

	private static final String SEMICOLON = ":";

	private static ServerMemberManager manager;

	public static void setManager(ServerMemberManager manager) {
		MemberUtils.manager = manager;
	}

	public static void copy(Member newMember, Member oldMember) {
		oldMember.setIp(newMember.getIp());
		oldMember.setPort(newMember.getPort());
		oldMember.setState(newMember.getState());
		oldMember.setExtendInfo(newMember.getExtendInfo());
		oldMember.setAddress(newMember.getAddress());
	}

	@SuppressWarnings("PMD.UndefineMagicConstantRule")
	public static Member singleParse(String member) {
		// Nacos default port is 8848
		int defaultPort = 8848;
		// Set the default Raft port information for securit

		String[] memberDetails = member.split("\\?");
		String address = memberDetails[0];
		int port = defaultPort;
		if (address.contains(SEMICOLON)) {
			String[] info = address.split(SEMICOLON);
			address = info[0];
			port = Integer.parseInt(info[1]);
		}

		int raftPort = port + 1000 >= 65535 ? port + 1 : port + 1000;

		Map<String, String> extendInfo = new HashMap<>(4);
		// The Raft Port information needs to be set by default
		extendInfo.put(MemberMetaDataConstants.RAFT_PORT, String.valueOf(raftPort));
		return Member.builder().ip(address).port(port).extendInfo(extendInfo)
				.state(NodeState.UP).build();
	}

	public static Collection<Member> multiParse(Collection<String> addresses) {
		List<Member> members = new ArrayList<>(addresses.size());
		for (String address : addresses) {
			Member member = singleParse(address);
			members.add(member);
		}
		return members;
	}

	public static void onSuccess(Member member) {
		manager.getMemberAddressInfos().add(member.getAddress());
		member.setState(NodeState.UP);
		member.setFailAccessCnt(0);
		manager.update(member);
	}

	public static void onFail(Member member) {
		manager.getMemberAddressInfos().remove(member.getAddress());
		member.setState(NodeState.SUSPICIOUS);
		member.setFailAccessCnt(member.getFailAccessCnt() + 1);
		int maxFailAccessCnt = ApplicationUtils
				.getProperty("nacos.core.member.fail-access-cnt", Integer.class, 3);
		if (member.getFailAccessCnt() > maxFailAccessCnt) {
			member.setState(NodeState.DOWN);
		}
		manager.update(member);
	}

	public static void syncToFile(Collection<Member> members) {
		try {
			StringBuilder builder = new StringBuilder();
			builder.append("#").append(LocalDateTime.now()).append(StringUtils.LF);
			for (String member : simpleMembers(members)) {
				builder.append(member).append(StringUtils.LF);
			}
			ApplicationUtils.writeClusterConf(builder.toString());
		}
		catch (Throwable ex) {
			Loggers.CLUSTER.error("cluster member node persistence failed : {}", ex);
		}
	}

	@SuppressWarnings("PMD.UndefineMagicConstantRule")
	public static List<Member> kRandom(Collection<Member> members,
			Predicate<Member> filter) {
		int k = ApplicationUtils
				.getProperty("nacos.core.member.report.random-num", Integer.class, 3);

		List<Member> tmp = new ArrayList<>();
		// Here thinking similar consul gossip protocols random k node

		int totalSize = members.size();
		for (int i = 0; i < 3 * totalSize && members.size() <= k; i++) {
			for (Member member : members) {
				if (filter.test(member)) {
					tmp.add(member);
				}
			}
		}

		return tmp;
	}

	// 默认配置格式解析，只有nacos-server的ip or ip:port or hostname:port 信息
	// example 192.168.16.1:8848?raft_port=8849&key=value

	public static Collection<Member> readServerConf(Collection<String> members) {
		Set<Member> nodes = new HashSet<>();

		for (String member : members) {
			Member target = singleParse(member);
			nodes.add(target);
		}

		return nodes;
	}

	public static List<String> simpleMembers(Collection<Member> members) {
		return members.stream().map(Member::getAddress)
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}

}
