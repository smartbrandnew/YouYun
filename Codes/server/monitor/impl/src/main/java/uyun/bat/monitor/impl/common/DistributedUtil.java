package uyun.bat.monitor.impl.common;

import uyun.bat.common.leaderselector.BatLeaderSelector;

/**
 * 按理说一个进程一个zk临时节点就好
 */
public abstract class DistributedUtil {
	private static BatLeaderSelector selector;

	static {
		selector = new BatLeaderSelector("/uyun.bat.monitor.impl.common.DistributedUtil");
	}

	public static boolean isLeader() {
		return selector.isLeader();
	}
}
