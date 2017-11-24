package uyun.bat.monitor.core.entity;

/**
 * 聚合计算符
 */
public enum Aggregator {
	AVG("avg", "平均值"), MAX("max", "最大值"), MIN("min", "最小值"), SUM("sum", "总和");

	private String code;
	private String name;

	private Aggregator(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static Aggregator checkByCode(String code) {
		for (Aggregator a : Aggregator.values()) {
			if (a.getCode().equals(code)) {
				return a;
			}
		}
		throw new IllegalArgumentException("Illegal aggregation operators！");
	}
}
