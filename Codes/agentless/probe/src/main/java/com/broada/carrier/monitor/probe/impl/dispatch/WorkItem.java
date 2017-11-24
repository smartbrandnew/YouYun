package com.broada.carrier.monitor.probe.impl.dispatch;

public class WorkItem extends BaseItem {
	private WorkType type;
	private int timeout;
	private Object input;
	private Object output;
	private RuntimeException error;

	public WorkItem(WorkType type, int timeout, Object input) {
		super();
		this.type = type;
		this.timeout = timeout;
		this.input = input;
	}

	public RuntimeException getError() {
		return error;
	}

	public void setError(RuntimeException error) {
		this.error = error;
	}

	public WorkType getType() {
		return type;
	}

	public boolean isFinish() {
		return getEndTime() > 0;
	}

	public int getTimeout() {
		return timeout;
	}

	public Object getOutput() {
		return output;
	}

	public void setOutput(Object output) {
		this.output = output;
	}

	public Object getInput() {
		return input;
	}

}
