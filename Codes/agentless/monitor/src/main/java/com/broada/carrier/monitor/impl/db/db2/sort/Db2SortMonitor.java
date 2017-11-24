package com.broada.carrier.monitor.impl.db.db2.sort;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.db2.DB2ExtendManager;
import com.broada.carrier.monitor.impl.db.db2.DB2ExtendManagerImpl;
import com.broada.carrier.monitor.impl.db.db2.DB2LoginException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.db2.DB2MonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * DB2排序监测器
 * 
 * @author 杨帆
 * 
 */
public class Db2SortMonitor extends BaseMonitor {

	public static final Log logger = LogFactory.getLog(Db2SortMonitor.class);

	private static final String ITEM_TOTAL_SORTS = "DB2-SORT-JDBC-1";
	private static final String ITEM_SORT_OVER_RATIO = "DB2-SORT-JDBC-2";
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		String ipAddr = context.getNode().getIp();
		DB2MonitorMethodOption option = new DB2MonitorMethodOption(context.getMethod());
		DB2ExtendManager manager = new DB2ExtendManagerImpl(ipAddr, option);
		DBSort sort = null;
		try {
      long replyTime = System.currentTimeMillis();
			sort = manager.getDBSortData();
			replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
		} catch (DB2LoginException e) {
			logger.error(e.getMessage(), e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(e.getMessage());
			return result;
		} catch (SQLException e) {
			logger.error("查询数据库失败:" + e.getMessage(), e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("查询数据库失败:" + e.getMessage());
			return result;
		} catch (InstantiationException e) {
			logger.error("获取结果集失败：系统内部错误（类实例化错误）", e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取结果集失败：系统内部错误（类实例化错误）");
			return result;
		} catch (IllegalAccessException e) {
			logger.error("获取结果集失败：系统内部错误（类实例化错误或bean映射错误）", e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取结果集失败：系统内部错误（类实例化错误或bean映射错误）");
			return result;
		} catch (InvocationTargetException e) {
			logger.error("获取结果集失败：系统内部错误（bean映射错误）", e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取结果集失败：系统内部错误（bean映射错误）");
			return result;
		} catch (CLIException e) {
			logger.error("通过agent方式获取数据出错:", e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("通过agent方式获取数据出错:" + e.getMessage());
			return result;
		}
		if(sort!=null){
		PerfResult perf_total_sorts = new PerfResult(ITEM_TOTAL_SORTS, sort.getTotalSorts());
		result.addPerfResult(perf_total_sorts);		
		PerfResult perf_sort_ratio = new PerfResult(ITEM_SORT_OVER_RATIO, sort.getSortOverRatio());
		result.addPerfResult(perf_sort_ratio);
		return result;
		}
		return null;
	}
}
