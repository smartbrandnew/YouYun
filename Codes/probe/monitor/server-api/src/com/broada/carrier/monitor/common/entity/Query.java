package com.broada.carrier.monitor.common.entity;

import java.util.List;

/**
 * 动态查询条件
 * @author Jiangjw
 */
public class Query {
	private List<QueryCondition> conditions;

	/**
	 * 获取条件列表
	 * @return
	 */
	public List<QueryCondition> getConditions() {
		return conditions;
	}

	/**
	 * 设置条件列表
	 * @param conditions
	 */
	public void setConditions(List<QueryCondition> conditions) {
		this.conditions = conditions;
	}
}
