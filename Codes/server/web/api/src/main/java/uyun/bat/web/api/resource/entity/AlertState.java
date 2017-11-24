package uyun.bat.web.api.resource.entity;

public class AlertState {
	private String id;
	private Integer severity;
	private boolean state;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getSeverity() {
		return severity;
	}

	public void setSeverity(Integer severity) {
		this.severity = severity;
	}

	public boolean getState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public AlertState() {
	}

	public AlertState(String id, boolean state) {
		this.id = id;
		this.state = state;
	}
}
