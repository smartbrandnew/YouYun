package uyun.bat.web.api.agent.entity;

import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.Dashwindow;
import uyun.bat.dashboard.api.entity.TenantResTemplate;

public class TemplateParam {
	private TenantResTemplate template;
	private Dashboard dashboard;
	private Dashwindow dashwindow;
	public TenantResTemplate getTemplate() {
		return template;
	}
	public void setTemplate(TenantResTemplate template) {
		this.template = template;
	}
	public Dashboard getDashboard() {
		return dashboard;
	}
	public void setDashboard(Dashboard dashboard) {
		this.dashboard = dashboard;
	}
	public Dashwindow getDashwindow() {
		return dashwindow;
	}
	public void setDashwindow(Dashwindow dashwindow) {
		this.dashwindow = dashwindow;
	}
	
	
}
