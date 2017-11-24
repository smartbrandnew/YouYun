package com.broada.carrier.monitor.client.impl.config;

import javax.swing.Icon;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.config.BaseConfig;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.OnlineState;
import com.broada.carrier.monitor.server.api.service.ServerSystemService;

/**
 * 配置读取类
 * @author Jiangjw
 */
public class Config extends BaseConfig {
	private static final String CONFIG_MULTI_TYPES = "monitor.multi.types";
	private static Icon ICON_SUCCESSED = IconLibrary.getDefault().getIcon("resources/images/dot_green.gif");
	private static Icon ICON_FAILED = IconLibrary.getDefault().getIcon("resources/images/dot_red.gif");
	private static Icon ICON_UNMONITOR = IconLibrary.getDefault().getIcon("resources/images/dot_gray.gif");
	
	private static Config instance;

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
		return String.format(getProps().get("mq.url", "tcp://%s:9105?startupMaxReconnectAttempts=1&timeout=3000"), ServerContext.getIp());
	}
	
	public String getMQUser() {
		return getProps().get("mq.user");
	}
	
	public String getMQPassword() {
		return getProps().get("mq.password");
	}
	
	public Icon getIcon(MonitorState state) {
		switch (state) {
		case SUCCESSED:
			return ICON_SUCCESSED;
		case FAILED:
			return ICON_FAILED;
		case UNMONITOR:
			return ICON_UNMONITOR;
		case OVERSTEP:
			return ICON_FAILED;
		default:
			throw new IllegalArgumentException(state.toString());
		}
	}
	
	public Icon getIcon(OnlineState state) {
		switch (state) {
		case ONLINE:
			return ICON_SUCCESSED;
		case OFFLINE:
			return ICON_FAILED;
		default:
			return ICON_UNMONITOR;
		}
	}

	public String getServerWebProtocol() {
		return getProps().get("server.web.protocol", "http");
	}

	public int getServerWebPort() {
		return getProps().get("server.web.port", 8890);
	}

	public int getAutosyncCheckInterval() {
		return getProps().get("autosync.interval", 120);
	}
	
	public static String getResourceDir() {
		return getWorkDir() + "/resources";
	}
	
	public String[] getMonitorMultiTypes() {
		String value = getProps().get(CONFIG_MULTI_TYPES);
		if (value == null)
			return new String[0];
		else
			return value.split(",");
	}

	public void initServerConfig(ServerSystemService systemService) {
		String[] props = new String[]{CONFIG_MULTI_TYPES};
		for (String prop : props) {
			String value = systemService.getProperty(prop);
			if (value != null)
				getProps().set(prop, value);
		}
	}	
	
	public String getTargetTypeImageUrl(MonitorTargetType targetType) {
		return getProps().get("target.type.image.url", "/cmdb/images/template/") + targetType.getId() + "/" + targetType.getImage16();		
	}
}
