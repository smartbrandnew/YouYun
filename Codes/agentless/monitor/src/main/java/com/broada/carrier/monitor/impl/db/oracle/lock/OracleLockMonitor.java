package com.broada.carrier.monitor.impl.db.oracle.lock;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * <p>
 * Title: OracleLockMonitor
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

public class OracleLockMonitor extends BaseMonitor {
	private static final Log logger = LogFactory
			.getLog(OracleLockMonitor.class);

	private List lockList;

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		// OracleLockParameter p =
		// context.getParameterObject(OracleLockParameter.class);
		
		// 修改监测器监测所有,减少配置
		OracleLockParameter p = new OracleLockParameter();
		p.setCTime(10);
		p.setMatchType(-1);
		p.setResourceName(null);
		OracleMethod option = new OracleMethod(context.getMethod());
		int ctime = p.getCTime();
		String condResource = p.getResourceName();
		int matchType = p.getMatchType();

		// Oracle 管理器取得
		OracleManager om = new OracleManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			om.initConnection();
		} catch (LogonDeniedException lde) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			om.close();
			return result;
		} catch (SQLException e) {
			String errMsg = e.getMessage();
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			om.close();
			return result;
		}

		// 锁定资源获取
		try {
			lockList = om.getAllLocksByTime(ctime);
		} catch (SQLException e) {
			String errMsg = "无法获取被锁定的对象列表信息.";
			if (logger.isDebugEnabled()) {
				logger.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			om.close();
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		OracleLockRules rule;
		if (condResource != null && !condResource.equals("")) {
			rule = new OracleLockRules(lockList, condResource, matchType);
		} else {
			rule = new OracleLockRules(lockList);
		}

		// 加入实例列表
		List lockedRes = rule.getLockedRes();
		if (lockedRes == null || lockedRes.size() < 1)
			return result;

		PerfResult[] perfs = new PerfResult[lockedRes.size()];
		for (int i = 0; i < lockedRes.size(); i++) {
			OracleLock ol = (OracleLock) lockedRes.get(i);

			MonitorResultRow row = new MonitorResultRow(ol.getObjName(),
					ol.getObjName());
			row.setIndicator("ORACLE-LOCK-1", ol.getCtime());
			result.addRow(row);
		}
		result.setPerfResults(perfs);
		return result;
	}
}
