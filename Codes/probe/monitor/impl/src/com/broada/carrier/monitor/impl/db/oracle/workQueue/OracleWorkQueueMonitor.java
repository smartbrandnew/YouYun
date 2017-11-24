package com.broada.carrier.monitor.impl.db.oracle.workQueue;

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;
/**
 * 作业队列监测
 * @author zhouqr
 *
 */
public class OracleWorkQueueMonitor extends BaseMonitor {
	private static Log logger = LogFactory.getLog(OracleWorkQueueMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		OracleMethod method = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), method);
		
		long respTime = System.currentTimeMillis();
		try {
			om.initConnection();			
		} catch (SQLException e) {
			logger.error("数据库初始化连接失败！");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("数据库初始化连接失败");
			om.close();
			return result;
		}
		
		try {
			/**--------------------破损作业---------------------------*/	
			try {
				int broken_num = om.getWorkQueueBrokenNum();
				result.addPerfResult(new PerfResult("ORACLE-WORKQUE-1", broken_num));
			} catch (SQLException e) {
				throw ErrorUtil.createRuntimeException("作业队列中破损数目或取失败!", e);
			}
			
			/**--------------------失败作业---------------------------*/		
			try {
				int failure_num = om.getWorkQueueFailureNum();
				result.addPerfResult(new PerfResult("ORACLE-WORKQUE-2", failure_num));
			} catch (SQLException e) {
				throw ErrorUtil.createRuntimeException("作业队列中失败数目或取失败!", e);
			}
			/**--------------------过期作业---------------------------*/
				
			try {
				int overDue_num = om.getWorkQueueOverdueNum();
				result.addPerfResult(new PerfResult("ORACLE-WORKQUE-3", overDue_num));
			} catch (SQLException e) {
				throw ErrorUtil.createRuntimeException("作业队列中过期数目或取失败!", e);				
			}		
	
			respTime = System.currentTimeMillis() - respTime;
	    if(respTime <= 0) {
	      respTime = 1;
	    }
	    result.setResponseTime(respTime);//响应时间
		} finally {
			om.close();
		}
					
		return result;
	}
}
