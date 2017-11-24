package com.broada.carrier.monitor.probe.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.common.net.icmp.IcmpPing;

/**
 * probe状态监测器
 * @author panhk
 * @version 1.0
 * @updated 25-二月-2014 10:30:27
 */
public class ProbeStateMonitor {
	private static final Logger logger = LoggerFactory.getLogger(ProbeStateMonitor.class);

	/**
	 * 是否正在动监测
	 */
	private static boolean isMonitor = false;
	/**
	 * 是否可用，是一个volatile变量
	 * 
	 * 默认为false
	 */
	private volatile static boolean isAvailable = true;
	/**
	 * probe状态变化的时间点，是一个volatile变量
	 * 
	 * 默认为0，表示未进行过状态检查
	 */
	private volatile static long stateChangeTime = 0L;

	private ProbeStateMonitor() {
	}

	/**
	 * 开始监测
	 * 
	 * 方法逻辑： 
	 * 1. 初始化检查是使用的ip列表 
	 * 2. 创建一个电镀任务，定期进行probe与ip地址日报中的ip的连通性判断，并实时更新probe的在线状态
	 */
	public synchronized static void startup() {
		if (isMonitor) {
			return;
		}
		isMonitor = true;

		String ips = Config.getDefault().getAvailableTestIps();
		int interval = Config.getDefault().getAvailableTestInterval();
		if (ips == null || interval < 10) {
			logger.warn(String.format("probe可用性监测服务启动失败，参数：ips=%s interval=%d", "" + ips, interval));
			return;
		}

		final String[] ipArr = ips.split(",");
		Timer timer = new Timer("");
		logger.info(String.format("启动探针可用性监测服务，参数：ips=%s interval=%d", "" + ips, interval));
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				doMonitor(ipArr);
			}
		}, Integer.parseInt(System.getProperty("available.test.delay", "10")) * 1000, interval * 1000);
	}

	/**
	 * 实际监测操作:对于配置的地址列表，逐一进行连通性判断，直到存在可连通的地址。
	 * 
	 * 最终，如果连通性状态发送变化，则更新probe可用状态及最后更新时间
	 * 
	 * 1.如果监测到probe由下线变为上线，则重新开始监测框架的监测工作
	 * 2.如果监测到probe由上线变为下线，则停止监测框架的监测工作
	 * 
	 * @param ips
	 */
	static void doMonitor(String[] ips) {
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder("探针可用性监测服务 —— 待检查的ip列表");
			for (String ip : ips) {
				sb.append("\n\t").append(ip);
			}
			logger.debug(sb.toString());
		}

		for (String ip : ips) {
			try {
				if (IcmpPing.ping(ip, 1000, 2, 1000) >= 0) {
					if (!isAvailable) {
						isAvailable = true; // 这个操作应该放到monitorDispatcher.resume()之后，目前只是为了方便测试
						if (stateChangeTime == 0) {
							stateChangeTime = System.currentTimeMillis();
						}
						logger.info("探针所在服务器的网络恢复正常，探针将继续进行监测");
					}
					if (logger.isDebugEnabled()) {
						logger.debug(String.format("探针可用性监测服务 —— 检查[ip=%s]成功", ip));
					}
					return;
				}
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("探针可用性监测服务 —— 检查[ip=%s]失败", ip));
				}
			} catch (Exception err) {
				logger.warn(String.format("探针可用性监测服务 —— 检查[ip=%s]时出错，错误：%s", ip, err));
				logger.debug("堆栈：", err);
			}
		}
		if (isAvailable) {
			isAvailable = false; //这个操作应该放到monitorDispatcher.suspend()之后，目前只是为了方便测试
			stateChangeTime = System.currentTimeMillis();
			logger.info("探针所在服务器的网络不可用，探针将停止监测");			
		}
	}

	/**
	 * 获取probe的可用状态
	 * @return 
	 */
	public static boolean isAvailable() {
		return isAvailable;
	}

	/**
	 * 获取状态变化的时间点，是一个volatile变量
	 * 
	 * 默认为0，表示未进行过状态检查
	 */
	public static long getStateChangeTime() {
		return stateChangeTime;
	}
}