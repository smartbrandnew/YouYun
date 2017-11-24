package com.broada.carrier.monitor.impl.db.oracle.tablestate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.component.utils.error.ErrorUtil;

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

public class OracleTableStateMonitor implements Monitor {
	private static Log log = LogFactory.getLog(OracleTableStateMonitor.class);	
	
	@Override
	public Serializable collect(CollectContext context) {
		OracleMethod method = new OracleMethod(context.getMethod());
	  TableStateManager tsManager = new TableStateManager(context.getNode().getIp(), method);
	  try {
	  	tsManager.initConnection();	  
		  CollectRequest request = context.getParameterObject(CollectRequest.class);
		  switch (request.getAction()) {
		  case CollectRequest.ACTION_GET_TABLE_DETAIL:
		  	return tsManager.getTableState(request.getTableName());
		  case CollectRequest.ACTION_GET_TABLES:
		  	try {
					return tsManager.getAllTableStates(request.getTableFilter(), request.getTableNum(), request.getPageIndex());
				} catch (SQLException e) {
					throw ErrorUtil.createRuntimeException("获取表数据失败", e);
				}//取得符合过滤器的数据
		  default:
		  	throw new IllegalArgumentException("未知的Action：" + request.getAction());
		  }
	  } catch (SQLException e) {
	  	throw ErrorUtil.createRuntimeException("采集表信息失败", e);
	  } finally {
	  	tsManager.close();
	  }
	}
	
	@Override
	public MonitorResult monitor(MonitorContext context) {	
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);// 没响应——0		
		OracleMethod option = new OracleMethod(context.getMethod());		
		TableStateManager tsm = new TableStateManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try
		{
			tsm.initConnection();
		}
		catch (LogonDeniedException lde)
		{
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0)
			{
				respTime = 1;
			}
			result.setResponseTime(respTime);
			tsm.close();
			return result;
		}
		catch (SQLException e)
		{
			String errMsg = "无法连接目标数据库.";
			if (log.isDebugEnabled())
			{
				log.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			tsm.close();
			return result;
		}

		List<OracleTableState> tableStateList = null;
		try
		{
			tableStateList = tsm.getListForDoMonitor(context.getInstances());
		}
		catch (SQLException e)
		{
			if (log.isDebugEnabled())
			{
				log.debug("无法获取数据库表信息.", e);
			}
			result.setResultDesc("无法获取数据库表信息.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		}
		finally 
		{
			tsm.close();
		}
    respTime = System.currentTimeMillis() - respTime;
    if (respTime <= 0)
    {
      respTime = 1;
    }
    result.setResponseTime(respTime);

		// 逐个将结果tableStateList转译到监测结果数组中
		for (OracleTableState ots : tableStateList) {
			MonitorResultRow row = new MonitorResultRow(ots.getName());
			row.setIndicator("ORACLE-TABLESTATE-2", ots.getTablesize());
			row.setIndicator("ORACLE-TABLESTATE-3", ots.getIndexsize());
			result.addRow(row);
		}
		return result;
	}
}
