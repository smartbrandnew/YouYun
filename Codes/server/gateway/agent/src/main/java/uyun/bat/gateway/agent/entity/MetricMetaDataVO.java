package uyun.bat.gateway.agent.entity;

public class MetricMetaDataVO {
	private String name;
	private String unit = "";
	private Double value_min;
	private Double value_max;
	private int accuracy = 0;
	private String data_type = "gauge";
	private String cn = "";
	private String cdescr;
	private String integration = "other";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Double getValue_min() {
		return value_min;
	}

	public void setValue_min(Double value_min) {
		this.value_min = value_min;
	}

	public Double getValue_max() {
		return value_max;
	}

	public void setValue_max(Double value_max) {
		this.value_max = value_max;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(int accuracy) {
		this.accuracy = accuracy;
	}

	public String getData_type() {
		return data_type;
	}

	public void setData_type(String data_type) {
		this.data_type = data_type;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getCdescr() {
		return cdescr;
	}

	public void setCdescr(String cdescr) {
		this.cdescr = cdescr;
	}

	public String getIntegration() {
		return integration;
	}

	public void setIntegration(String integration) {
		this.integration = integration;
	}

	public MetricMetaDataVO() {
	}

	public MetricMetaDataVO(String name, String unit, Double value_min, Double value_max, int accuracy, String data_type,
							String cn, String cdescr, String integration) {
		this.name = name;
		this.unit = unit;
		this.value_min = value_min;
		this.value_max = value_max;
		this.accuracy = accuracy;
		this.data_type = data_type;
		this.cn = cn;
		this.cdescr = cdescr;
		this.integration = integration;
	}
}
