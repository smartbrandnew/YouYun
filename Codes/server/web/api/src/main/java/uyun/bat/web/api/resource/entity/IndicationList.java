package uyun.bat.web.api.resource.entity;

import java.util.List;

public class IndicationList {

	private List<Indication> hosts;
	private Double min;
	private Double max;
	private String unit;

	public List<Indication> getHosts() {
		return hosts;
	}

	public void setHosts(List<Indication> hosts) {
		this.hosts = hosts;
	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
