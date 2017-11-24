package com.broada.carrier.monitor.impl.db.mssql;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.component.utils.error.ErrorUtil;

/**
 * SQLServer的一些异常统一处理类
 * @author Jiangjw
 */
public class MSSQLErrorUtil {
	private static final Logger logger = LoggerFactory.getLogger(MSSQLErrorUtil.class);
	
	/**
	 * 将一个操作消息与异常转为MonitorResult返回
	 * @param message
	 * @param e
	 * @return
	 */
	public static MonitorResult process(String message, Throwable e) {						
		if (e instanceof SQLException)
			message = createMessage(message, (SQLException) e);
		else if (e.getCause() instanceof SQLException)
			message = createMessage(message, (SQLException) e.getCause());
		else
			message = ErrorUtil.createMessage(message, e);
		
		if (logger.isDebugEnabled())
			logger.warn(message, e);
		else
			logger.warn(message);
		
		return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, message);
	}

	private static String createMessage(String message, SQLException e) {
		int errorCode = e.getErrorCode();
		if (errorCode != 0)
			message += "。\n编码：" + e.getErrorCode();
		return ErrorUtil.createMessage(message, e); 
	}

}
