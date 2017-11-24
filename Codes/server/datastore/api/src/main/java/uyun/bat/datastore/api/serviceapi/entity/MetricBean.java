package uyun.bat.datastore.api.serviceapi.entity;

public class MetricBean {
	private String metric;
	private String desc;

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String toString() {
		return "MetricBean [metric=" + metric + ", desc=" + desc + "]";
	}
	
	
}
