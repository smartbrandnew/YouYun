package uyun.bat.web.api.dashboard.entity;

import uyun.bat.dashboard.api.entity.Dashwindow;

import java.util.List;

/**
 * 用户打开单个dashboard
 */
public class MineDashboard {
	private String id;
	private String name;
	private boolean favourite;
	private boolean isApplied;

	public MineDashboard(){
		this.isApplied=true;
	}
	public boolean isApplied() {
		return isApplied;
	}

	public void setApplied(boolean isApplied) {
		this.isApplied = isApplied;
	}

	private List<Dashwindow> dashwindows;

	public boolean isFavourite() {
		return favourite;
	}

	public void setFavourite(boolean favourite) {
		this.favourite = favourite;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Dashwindow> getDashwindows() {
		return dashwindows;
	}

	public void setDashwindows(List<Dashwindow> dashwindows) {
		this.dashwindows = dashwindows;
	}
}
