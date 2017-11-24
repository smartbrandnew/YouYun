package com.broada.carrier.monitor.impl.db.oracle.recursion;

import java.io.Serializable;

public class OracleRecursionTemp implements Serializable {
	private static final long serialVersionUID = 1L;
	private long time;
	private double lastRecursionCallNum;
	private double lastUserCallNum;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getLastRecursionCallNum() {
		return lastRecursionCallNum;
	}

	public void setLastRecursionCallNum(double lastRecursionCallNum) {
		this.lastRecursionCallNum = lastRecursionCallNum;
	}

	public double getLastUserCallNum() {
		return lastUserCallNum;
	}

	public void setLastUserCallNum(double lastUserCallNum) {
		this.lastUserCallNum = lastUserCallNum;
	}

}
