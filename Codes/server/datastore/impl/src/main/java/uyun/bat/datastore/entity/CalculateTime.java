package uyun.bat.datastore.entity;

import org.kairosdb.client.builder.TimeUnit;

public class CalculateTime {
	private int period;
	private TimeUnit timeUnit;

	public CalculateTime() {

	}

	public CalculateTime(int period, TimeUnit timeUnit) {
		this.period = period;
		this.timeUnit = timeUnit;
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	@Override
	public String toString() {
		return "CalculateTime [period=" + period + ", timeUnit=" + timeUnit + "]";
	}

}
