package com.broada.carrier.monitor.probe.impl.openapi.entity;

import java.util.Date;

public class Gohai {
	
	private Date build_date;
	private String git_hash;
	private String git_branch;
	private String go_version;
	
	public Gohai() {
		// TODO Auto-generated constructor stub
	}
	public Gohai(Date build_date, String git_hash, String git_branch, String go_version) {
		this.build_date = build_date;
		this.git_hash = git_hash;
		this.git_branch = git_branch;
		this.go_version = go_version;
	}
	
	public Date getBuild_date() {
		return build_date;
	}
	public void setBuild_date(Date build_date) {
		this.build_date = build_date;
	}
	public String getGit_hash() {
		return git_hash;
	}
	public void setGit_hash(String git_hash) {
		this.git_hash = git_hash;
	}
	public String getGit_branch() {
		return git_branch;
	}
	public void setGit_branch(String git_branch) {
		this.git_branch = git_branch;
	}
	public String getGo_version() {
		return go_version;
	}
	public void setGo_version(String go_version) {
		this.go_version = go_version;
	}
	
}
