package com.broada.carrier.monitor.impl.db.mysql.util;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.mysql.MySQLException;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.error.MonitorException;

import java.sql.SQLException;

public class MySQLErrorUtil {

	public static MonitorException createError(SQLException e) {
		throw new MonitorException("" + e.getErrorCode(), e);
	}

	public static MonitorException createError(MySQLException e) {
		throw new MonitorException("" + e.getErrorCode(), e);
	}

	public static MonitorResult process(MySQLException e, long time, MonitorResult result) {
		//如果是用户名和密码错误的话则认为可用(不健康)，其他的异常都认为不可用
		if (e.getErrorCode() == MySQLException.ERRCODE_USERPWD) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(e.getMessage());
			//认为可以连接,所以有连接时间
			long replyTime = System.currentTimeMillis() - time;
			if (replyTime <= 0) {
				replyTime = 1;
			}
			result.setResponseTime((int) replyTime);
		} else {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(e.getMessage());
		}
		return result;
	}
}
