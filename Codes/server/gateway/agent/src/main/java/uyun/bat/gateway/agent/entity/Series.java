package uyun.bat.gateway.agent.entity;

public class Series {
	private String scope;
	private double[][] points;
	private String unit;

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public double[][] getPoints() {
		return points;
	}

	public void setPoints(double[][] points) {
		this.points = points;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Series(String scope, double[][] points, String unit) {
		this.scope = scope;
		this.points = points;
		this.unit = unit;
	}

	public Series() {
		super();
	}

}
