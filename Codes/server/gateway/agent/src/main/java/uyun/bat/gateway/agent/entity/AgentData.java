package uyun.bat.gateway.agent.entity;

import java.util.List;

import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.gateway.api.common.GatewayConstants;

/**
 * 为指标和事件增加资源的入参类
 * 
 */
public class AgentData {
	private String hostId;
	private String hostName = GatewayConstants.UNKNOWN;
	private String hostIp = GatewayConstants.UNKNOWN;
	private List<String> tags;
	private List<String> apps;
	private String type;
	private String os;
	private OnlineStatus onlineStatus;   // 在线状态
	/**
	 * 数据是否来源于创建host接口
	 */
	private boolean isHost;

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public AgentData() {
		super();
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getApps() {
		return apps;
	}

	public void setApps(List<String> apps) {
		this.apps = apps;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

	public boolean isHost() {
		return isHost;
	}

	public void setHost(boolean host) {
		isHost = host;
	}

	public AgentData(String hostId) {
		super();
		this.hostId = hostId;
	}

	public AgentData(String hostId, String hostName) {
		super();
		this.hostId = hostId;
		this.hostName = hostName;
	}

	public AgentData(String hostId, String hostName, String hostIp, List<String> tags, List<String> apps, String type, String os, boolean status, boolean isHost) {
		super();
		this.hostId = hostId;
		this.hostName = hostName;
		this.hostIp = hostIp;
		this.tags = tags;
		this.apps = apps;
		this.type = type;
        this.os = os;
        if(status)
        	this.onlineStatus = OnlineStatus.ONLINE;
        else
        	this.onlineStatus = OnlineStatus.OFFLINE;
		this.isHost = isHost;
	}

	public OnlineStatus getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(OnlineStatus onlineStatus) {
		this.onlineStatus = onlineStatus;
	}

}
