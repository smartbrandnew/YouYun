package uyun.bat.monitor.core.entity;

/**
 * 匹配计算符
 */
public enum Comparison {
	GT(">", "above", "大于"), GE(">=", "above or equal to", "大于等于"), LT("<", "below", "小于"), LE("<=", "below or equal to",
			"小于等于");

	private String code;
	private String name;
	private String cname;

	private Comparison(String code, String name, String cname) {
		this.code = code;
		this.name = name;
		this.cname = cname;
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

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
	}

	/**
	 * 比较当前值与阈值
	 * 
	 * @param target
	 * @param threshold
	 * @return
	 */
	public boolean match(Double target, Double threshold) {
		if (GT.code.equals(code))
			return target > threshold;
		else if (GE.code.equals(code))
			return target >= threshold;
		else if (LT.code.equals(code))
			return target < threshold;
		else if (LE.code.equals(code))
			return target <= threshold;
		else
			return false;
	}

	public static Comparison checkByCode(String code) {
		for (Comparison c : Comparison.values()) {
			if (c.getCode().equals(code)) {
				return c;
			}
		}
		throw new IllegalArgumentException("Illegal aggregation operators！");
	}
}
