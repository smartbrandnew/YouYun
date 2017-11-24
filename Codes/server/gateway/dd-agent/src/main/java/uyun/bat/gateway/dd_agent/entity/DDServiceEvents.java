package uyun.bat.gateway.dd_agent.entity;

import java.util.List;

public class DDServiceEvents {
	private String check;
	private List<DDEvent> events;

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public List<DDEvent> getEvents() {
		return events;
	}

	public void setEvents(List<DDEvent> events) {
		this.events = events;
	}

}
