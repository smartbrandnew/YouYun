package com.broada.carrier.monitor.impl.host.snmp.winservice;

import com.broada.carrier.monitor.impl.common.entity.RunState;

/**
 * Windows服务，目前的状态值来源于1.3.6.1.4.1.77.1.2.3.1.3 svSvcOperatingState的定义
 * @author Jiangjw
 */
public enum WinServiceState {
	STOP(0, RunState.STOP),
	ACTIVE(1, RunState.RUNNING),
	CONTINUE_PENDING(2, RunState.STOP),
	PAUSE_PENDING(3, RunState.STOP),
	PAUSED(4, RunState.STOP);

	private int id;
	private RunState runState;

	private WinServiceState(int id, RunState runState) {
		this.id = id;
		this.runState = runState;
	}

	public static WinServiceState getById(int id, WinServiceState defaultValue) {
		for (WinServiceState item : values()) {
			if (item.id == id)
				return item;
		}
		return defaultValue;
	}

	/**
	 * id，对应snmpoid的值 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * 运行状态，此值对应的运行状态
	 * @return
	 */
	public RunState getRunState() {
		return runState;
	}

}
