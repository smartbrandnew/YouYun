package uyun.bat.gateway.agent.entity;

public class MetricSnapshoot {
	private String tag;
	private double[] point;
	private String unit;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public double[] getPoint() {
		return point;
	}

	public void setPoint(double[] point) {
		this.point = point;
	}

	public MetricSnapshoot() {
		super();
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public MetricSnapshoot(String tag, double[] point, String unit) {
		this.tag = tag;
		this.point = point;
		this.unit = unit;
	}
}
