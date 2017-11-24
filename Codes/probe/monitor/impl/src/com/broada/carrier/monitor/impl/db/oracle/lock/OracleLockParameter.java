package com.broada.carrier.monitor.impl.db.oracle.lock;

import java.io.Serializable;

/**
 * <p>
 * Title: OracleDeadLockParameter
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author plx
 * @version 2.3
 */

public class OracleLockParameter implements Serializable {

	private static final long serialVersionUID = -5582705833099412896L;

	// 匹配模式
	public static final int MATCH_NORMAL = 0;// 常规匹配（匹配*和？）
	public static final int MATCH_REGEX = 1;// 正则表达式

	public static final String[] MATCH_NMAE = { "常规匹配", "正则表达式" };

	private int cTime;
	private int matchType;
	private String resourceName;

	public int getCTime() {
		return cTime;
	}

	public void setCTime(int cTime) {
		this.cTime = cTime;
	}

	public int getMatchType() {
		return matchType;
	}

	public void setMatchType(int matchType) {
		this.matchType = matchType;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public void removeResourceName() {
		setResourceName(null);
	}

	public void removeMatchType() {
		setMatchType(-1);
	}

}
