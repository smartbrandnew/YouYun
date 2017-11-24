package com.broada.carrier.monitor.method.tibco;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

import java.util.List;

/**
 * Created by hu on 2015/5/18.
 */
public class TibcoMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	private int port;
	private String community;
	private String version;
	private String appName;
	private int timeout;
	private List<Object> procNames;
	private boolean wacthMem;

	public TibcoMethod(){
		super();
	}

	public TibcoMethod(MonitorMethod method) {
		super();
	}

	public int getPort() {
		return port;
	}

	public String getCommunity() {
		return community;
	}

	public String getVersion() {
		return version;
	}

	public long getSubjLimit(String key) {
		if (key == null)
			return 0;
		long lg = getLongOption(key);
		if (lg <= 0) {
			return 0;
		}
		return lg;
	}

	public long getLongOption(String key) {
		String val = getProperties().getByMethod(key);
		return val == null?-9223372036854775808L:Long.parseLong(val);
	}

	public String getAppName() {
		return appName;
	}

	public int getTimeout() {
		return timeout;
	}

	public List<Object> getProcNames() {
		return procNames;
	}

	public boolean isWacthMem() {
		return wacthMem;
	}
}
