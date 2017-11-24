package com.broada.carrier.monitor.common.entity;

/**
 * 查询条件
 * @author Jiangjw
 */
public class QueryCondition {
	private String field;
	private QueryOperator operator;
	private Object value;

	/**
	 * 构造一个查询条件
	 * @param field 字段编码
	 * @param operator 运算符
	 * @param value 条件值
	 */
	public QueryCondition(String field, QueryOperator operator, Object value) {
		super();
		this.field = field;
		this.operator = operator;
		this.value = value;
	}

	/**
	 * 查询字段编码
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * 运算符
	 * @return
	 */
	public QueryOperator getOperator() {
		return operator;
	}

	/**
	 * 查询值
	 * @return
	 */
	public Object getValue() {
		return value;
	}

}
