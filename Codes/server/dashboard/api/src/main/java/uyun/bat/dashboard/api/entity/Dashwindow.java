package uyun.bat.dashboard.api.entity;

import java.util.List;

public class Dashwindow {
	private String id;
	private String dashId;
	private List<Request> requests;
	private String name;
	private String viz;
	private List<LineData> lineDatas;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Request> getRequests() {
		return requests;
	}

	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}

	public String getDashId() {
		return dashId;
	}

	public void setDashId(String dashId) {
		this.dashId = dashId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getViz() {
		return viz;
	}

	public void setViz(String viz) {
		this.viz = viz;
	}

	public List<LineData> getLineDatas() {
		return lineDatas;
	}

	public void setLineDatas(List<LineData> lineDatas) {
		this.lineDatas = lineDatas;
	}

}
