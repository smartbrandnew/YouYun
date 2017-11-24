package com.broada.carrier.monitor.impl.mw.webspheremq.channel;

public class IbmMqChannelState {
	static final int Initializing = 13;

	static final int Starting = 2;

	static final int Binding = 1;

	static final int Requesting = 7;

	static final int Running = 3;

	static final int Paused = 8;

	static final int Stoppin = 4;

	static final int Retrying = 5;

	static final int Stopped = 6;

	static final int Inactive = 0;

	static final String MQCHS_INACTIVE = "不活动";

	static final String MQCHS_BINDING = "绑定";

	static final String MQCHS_STARTING = "正在开始";

	static final String MQCHS_RUNNING = "活动";

	static final String MQCHS_STOPPING = "正在停止";

	static final String MQCHS_RETRYING = "重试";

	static final String MQCHS_STOPPED = "已经停止";

	static final String MQCHS_REQUESTING = "请求中";

	static final String MQCHS_PAUSED = "暂停中";

	static final String MQCHS_INITIALIZING = "正在初始化";

	static final String MQCHS_NOKNOWS = "未知";

	/* Channel Type */
	static final int MQCHT_SENDER = 1;

	static final int MQCHT_SERVER = 2;

	static final int MQCHT_RECEIVER = 3;

	static final int MQCHT_REQUESTER = 4;

	static final int MQCHT_ALL = 5;

	static final int MQCHT_CLNTCONN = 6;

	static final int MQCHT_SVRCONN = 7;

	/**
	 * 获取MQ通道状态（中文字符串表示）
	 * 
	 * @param state
	 * @return
	 */
	public static String getMQChState(int state) {
		String str = null;
		switch (state) {
		case Inactive:
			str = MQCHS_INACTIVE;
			break;
		case Binding:
			str = MQCHS_BINDING;
			break;
		case Starting:
			str = MQCHS_STARTING;
			break;
		case Running:
			str = MQCHS_RUNNING;
			break;
		case Stoppin:
			str = MQCHS_STOPPING;
			break;
		case Retrying:
			str = MQCHS_RETRYING;
			break;
		case Stopped:
			str = MQCHS_STOPPED;
			break;
		case Requesting:
			str = MQCHS_REQUESTING;
			break;
		case Paused:
			str = MQCHS_PAUSED;
			break;
		case Initializing:
			str = MQCHS_INITIALIZING;
			break;
		default:
			str = MQCHS_NOKNOWS;
		}
		return str;
	}

	public static int getMQChState(String statesString) {
		if (statesString.equals(MQCHS_INACTIVE)) {
			return Inactive;
		}
		if (statesString.equals(MQCHS_BINDING)) {
			return Binding;
		}
		if (statesString.equals(MQCHS_STARTING)) {
			return Starting;
		}
		if (statesString.equals(MQCHS_RUNNING)) {
			return Running;
		}
		if (statesString.equals(MQCHS_STOPPING)) {
			return Stoppin;
		}
		if (statesString.equals(MQCHS_RETRYING)) {
			return Retrying;
		}
		if (statesString.equals(MQCHS_STOPPED)) {
			return Stopped;
		}
		if (statesString.equals(MQCHS_REQUESTING)) {
			return Requesting;
		}
		if (statesString.equals(MQCHS_PAUSED)) {
			return Paused;
		}
		if (statesString.equals(MQCHS_INITIALIZING)) {
			return Initializing;
		}
		return -1;
	}

	/**
	 * 获取MQ通道类型（中文字符串表示）
	 * 
	 * @return
	 */
	public static String getMQChType(int type) {
		String str = null;
		switch (type) {
		case MQCHT_SENDER:
			str = "发送方类型";
			break;
		case MQCHT_SERVER:
			str = "服务器类型";
			break;
		case MQCHT_RECEIVER:
			str = "接收方类型";
			break;
		case MQCHT_REQUESTER:
			str = "请求方类型";
			break;
		case MQCHT_ALL:
			str = "除客户连接以外的类型";
			break;
		case MQCHT_CLNTCONN:
			str = "客户连接类型";
			break;
		case MQCHT_SVRCONN:
			str = "服务器连接类型";
			break;
		default:
			str = "未知类型";
		}
		return str;
	}
}
