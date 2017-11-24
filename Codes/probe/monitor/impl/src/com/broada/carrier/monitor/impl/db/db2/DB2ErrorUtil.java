package com.broada.carrier.monitor.impl.db.db2;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;

public class DB2ErrorUtil {
	private static final Logger logger = LoggerFactory.getLogger(DB2ErrorUtil.class);
	
	public static MonitorResult process(Throwable e) {
		if (e instanceof DB2LoginException) {
      logger.error(e.getMessage(), e);
      return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, e.getMessage());
    } else if (e instanceof SQLException) {
      logger.error("查询数据库失败:" + e.getMessage(), e);
      return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, "查询数据库失败:" + e.getMessage());
    } else if (e instanceof InstantiationException) {
      logger.error("获取结果集失败：系统内部错误（类实例化错误）", e);
      return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, "获取结果集失败：系统内部错误（类实例化错误）");
    } else if (e instanceof IllegalAccessException) {
      logger.error("获取结果集失败：系统内部错误（类实例化错误或bean映射错误）", e);
      return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, "获取结果集失败：系统内部错误（类实例化错误或bean映射错误）");
    } else if (e instanceof InvocationTargetException) {
      logger.error("获取结果集失败：系统内部错误（bean映射错误）", e);
      return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, "获取结果集失败：系统内部错误（bean映射错误）");
    } else if (e instanceof CLIException) {
      logger.error("通过agent方式获取数据出错:", e);
      return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, "通过agent方式获取数据出错:" + e.getMessage());
    } else {
    	logger.error("DB2监测未知错误", e);    	
    	return new MonitorResult(MonitorConstant.MONITORSTATE_FAILING, "DB2监测未知错误:" + e.getMessage());
    }
	}

}
