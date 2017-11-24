package uyun.bat.gateway.agent.entity.newentity;

import java.util.List;

public class Device1 {
	private String desc;
	private boolean online_state;
	private List<String> tags;
	private String agent_descr;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public boolean isOnline_state() {
		return online_state;
	}

	public void setOnline_state(boolean online_state) {
		this.online_state = online_state;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getAgent_descr() {
		return agent_descr;
	}

	public void setAgent_descr(String agent_descr) {
		this.agent_descr = agent_descr;
	}

	public Device1() {
		super();
	}

	public Device1(String desc, boolean online_state, List<String> tags, String agent_descr) {
		super();
		this.desc = desc;
		this.online_state = online_state;
		this.tags = tags;
		this.agent_descr = agent_descr;
	}

	public Device1(boolean online_state, List<String> tags) {
		super();
		this.online_state = online_state;
		this.tags = tags;
	}

}
