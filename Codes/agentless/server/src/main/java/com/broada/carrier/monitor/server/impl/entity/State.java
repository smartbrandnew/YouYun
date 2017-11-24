package com.broada.carrier.monitor.server.impl.entity;

import java.util.Date;

import com.broada.carrier.monitor.common.util.TextUtil;

public abstract class State {
	private Date lastTime;
	private String value;
	private String lastValue;
	private String message;
	private int count;

	public State() {
	}

	public State(StateType type, String objectId, Date firstTime, Date lastTime, String value, String lastValue,
			String message, int count) {
		setType(type);
		setObjectId(objectId);
		setFirstTime(firstTime);
		this.lastTime = lastTime;
		this.value = value;
		this.lastValue = lastValue;
		this.message = message;
		this.count = count;
	}

	public String getLastValue() {
		return lastValue;
	}

	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}

	public abstract StateType getType();

	public abstract void setType(StateType type);

	public abstract Date getFirstTime();

	public abstract void setFirstTime(Date firstTime);

	public Date getLastTime() {
		return lastTime;
	}

	public void setLastTime(Date lastTime) {
		this.lastTime = lastTime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = TextUtil.truncate(message, 500);
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public abstract String getObjectId();

	public abstract void setObjectId(String objectId);

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
