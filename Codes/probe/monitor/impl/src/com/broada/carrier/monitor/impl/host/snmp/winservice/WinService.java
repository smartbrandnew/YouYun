package com.broada.carrier.monitor.impl.host.snmp.winservice;

import java.io.Serializable;

/**
 * <p>
 * Title: WinService
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx
 * @version 2.3
 */

public class WinService implements Serializable {
	private static final long serialVersionUID = 3218687211266474498L;

	private String winServiceKey;
	private String winServiceName;
	private WinServiceState winServiceState;

	public String getWinServiceKey() {
		return winServiceKey;
	}

	public String getWinServiceName() {
		return winServiceName;
	}

	public WinServiceState getWinServiceState() {
		return winServiceState;
	}

	public void setWinServiceKey(String winServiceKey) {
		this.winServiceKey = winServiceKey;
	}

	public void setWinServiceName(String winServiceName) {
		this.winServiceName = winServiceName;
	}

	public void setWinServiceState(WinServiceState winServiceState) {
		this.winServiceState = winServiceState;
	}
	
	public void setWinServiceState(Integer winServiceState) {
		if (winServiceState == null)
			this.winServiceState = WinServiceState.STOP;
		else 
			this.winServiceState = WinServiceState.getById(winServiceState, WinServiceState.STOP);
	}

}
