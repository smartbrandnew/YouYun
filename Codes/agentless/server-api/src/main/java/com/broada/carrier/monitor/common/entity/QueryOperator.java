package com.broada.carrier.monitor.common.entity;

/**
 * 查询运行算
 * @author Jiangjw
 */
public enum QueryOperator {
	EQUALS("=", "等于"),
	CONTAINS("contains", "包含"),
	GREATER(">", "大于"),
	LESS("<", "小于");

	private String symbol;
	private String descr;

	private QueryOperator(String symbol, String descr) {
		this.symbol = symbol;
		this.descr = descr;
	}

	/**
	 * 获取符号
	 * @return
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * 获取描述
	 * @return
	 */
	public String getDescr() {
		return descr;
	}

}
