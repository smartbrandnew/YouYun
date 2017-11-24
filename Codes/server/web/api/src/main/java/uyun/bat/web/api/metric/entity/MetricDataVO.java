package uyun.bat.web.api.metric.entity;

public class MetricDataVO {
	private String name;
	private String scope;
	private String desc;
	private String cn;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public MetricDataVO() {
		super();
	}

	public MetricDataVO(String name, String scope, String desc, String cn) {
		super();
		this.name = name;
		this.scope = scope;
		this.desc = desc;
		this.cn = cn;
	}

}
