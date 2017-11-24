package com.broada.carrier.monitor.impl.db.oracle.tablestate;

import java.io.Serializable;

/**
 * <p>
 * Title: oracle表状态监测
 * </p>
 * <p>
 * Description: 产品部
 * </p>
 * <p>
 * Copyright: Copyright (c) 2010
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author caikang
 * @version 3.3
 */

public class OracleTableState implements Serializable {
	private static final long serialVersionUID = 1L;
	private boolean isWacthed = false;
	private String tablename = null;
	private String username = null;
	private float tablesize = 0;
	private float indexsize = 0;
	private float growthrate = 0;
	private double lastTime = -1;// 小于0表示未赋值时间
	private Float maxUsedTableSize = new Float(100);//表格最大值
	private Float maxUsedGrowthrate = new Float(10);//最大增长速率

	public Float getMaxUsedTableSize() {
		return maxUsedTableSize;
	}

	public void setMaxUsedTableSize(Float maxUsedTableSize) {
		this.maxUsedTableSize = maxUsedTableSize;
	}

	public Float getMaxUsedGrowthrate() {
		return maxUsedGrowthrate;
	}

	public void setMaxUsedGrowthrate(Float maxUsedGrowthrate) {
		this.maxUsedGrowthrate = maxUsedGrowthrate;
	}

	public boolean getIsWacthed() {
		return isWacthed;
	}

	public void setIsWacthed(boolean isWacthed) {
		this.isWacthed = isWacthed;
	}

	public String getTablename() {
		return tablename;
	}

	public void setTablename(String tablename) {
		this.tablename = tablename;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public float getTablesize() {
		return tablesize;
	}

	public void setTablesize(float tablesize) {
		this.tablesize = tablesize;
	}

	public float getIndexsize() {
		return indexsize;
	}

	public void setIndexsize(float indexsize) {
		this.indexsize = indexsize;
	}

	public float getGrowthrate() {
		return growthrate;
	}

	public void setGrowthrate(float growthrate) {
		this.growthrate = growthrate;
	}

	public double getLastTime() {
		return lastTime;
	}

	public void setLastTime(double lastTime) {
		this.lastTime = lastTime;
	}

	public String getName() {
		return username+ "." +tablename;
	}	
}