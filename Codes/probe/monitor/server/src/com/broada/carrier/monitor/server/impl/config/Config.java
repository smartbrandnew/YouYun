package com.broada.carrier.monitor.server.impl.config;

import com.broada.carrier.monitor.common.config.BaseConfig;
import com.broada.carrier.monitor.common.config.SimpleProperties;
import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.cmdb.api.client.EventClient;
import com.broada.cmdb.api.service.CMDBServiceFactory;

/**
 * 配置读取类
 * @author Jiangjw
 */
public class Config extends BaseConfig {
	private static Config instance;
	private long sessionTimeout;

	/**
	 * 获取默认实例
	 * @return
	 */
	public static Config getDefault() {
		if (instance == null) {
			synchronized (Config.class) {
				if (instance == null)
					instance = new Config();
			}
		}
		return instance;
	}
	
	public String getMQUrl() {
		return getProps().get("mq.url", "tcp://localhost:9105?startupMaxReconnectAttempts=1&timeout=3000");
	}
	
	public String getMQUser() {
		return getProps().get("mq.user");
	}
	
	public String getMQPassword() {
		return getProps().get("mq.password");
	}
	
	/**
	 * PMDB事件服务IP
	 * @return
	 */
	public String getPMDBEventIp() {
		return getProps().get("pmdb.event.ip", EventClient.SERVICE_IP_DEFAULT);
	}

	/**
	 * PMDB事件服务端口
	 * @return
	 */
	public int getPMDBEventPort() {
		return getProps().get("pmdb.event.port", EventClient.SERVICE_PORT_DEFAULT);
	}

	/**
	 * 获取PMDB服务IP
	 * @return
	 */
	public String getPMDBIp() {
		return getProps().get("pmdb.ip", CMDBServiceFactory.SERVICE_IP_DEFAULT);
	}

	/**
	 * 获取PMDB服务端口
	 * @return
	 */
	public int getPMDBPort() {
		return getProps().get("pmdb.port", CMDBServiceFactory.SERVICE_PORT_DEFAULT);
	}

	/**
	 * 服务端web服务端口
	 * @return
	 */
	public int getWebServerPort() {
		return getProps().get("webserver.port", 9140);
	}

	public String getTrapTargets() {
		return getProps().get("trap.targets", HostIpUtil.getLocalHost() + ":162");
	}
	
	public SimpleProperties getProps() {
		return super.getProps();
	}

	public long getSessionTimeout() {		
		if (sessionTimeout == 0)
			sessionTimeout = getProps().get("session.timeout", 60 * 60l) * 1000l; 
		return sessionTimeout;		
	}

	public String getPMDBApiUrl() {
		return getProps().get("pmdb.api.url", "http://"+HostIpUtil.getLocalHost()+":9116/cmdb/pmdb/api/v1");
	}
}
