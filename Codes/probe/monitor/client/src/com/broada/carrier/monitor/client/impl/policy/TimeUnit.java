package com.broada.carrier.monitor.client.impl.policy;

public enum TimeUnit {
	SECOND(1, "秒"), MINUTE(60, "分"), HOUR(60 * 60, "小时"), DAY(24 * 60 * 60, "天");

	private String descr;
	private int seconds;

	private TimeUnit(int seconds, String descr) {
		this.seconds = seconds;
		this.descr = descr;
	}

	public String getDescr() {
		return descr;
	}

	public int getSeconds() {
		return seconds;
	}

	@Override
	public String toString() {
		return getDescr();
	}

	public static TimeUnit getPerfectUnit(int seconds) {
		if (seconds == 0) 
			return MINUTE;
		
		for (int i = values().length - 1; i >= 0; i--) {
			TimeUnit unit = values()[i];
			if (seconds % unit.getSeconds() == 0)
				return unit;
		}
		
		return SECOND;
	}
}
