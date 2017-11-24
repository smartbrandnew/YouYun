package com.broada.carrier.monitor.impl.db.st.workQueue;

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;

/**
 * 作业队列监测
 * 
 * @author Zhouqa
 * Create By 2016年4月13日 下午4:23:49
 */
public class ShentongWorkQueueMonitor extends BaseMonitor {
	private static Log logger = LogFactory.getLog(ShentongWorkQueueMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		ShentongMethod method = new ShentongMethod(context.getMethod());
		ShentongManager sm = new ShentongManager(context.getNode().getIp(), method);
		
		long respTime = System.currentTimeMillis();
		try {
			sm.initConnection();			
		} catch (SQLException e) {
			logger.error("数据库初始化连接失败！");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("数据库初始化连接失败");
			sm.close();
			return result;
		} catch (ClassNotFoundException e) {
			logger.error("数据库驱动类加载失败！");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("数据库驱动类加载失败");
			sm.close();
			return result;
		}
		
		try {
			/**--------------------破损作业---------------------------*/	
			try {
				int broken_num = sm.getWorkQueueBrokenNum();
				result.addPerfResult(new PerfResult("SHENTONG-WORKQUE-1", broken_num));
			} catch (SQLException e) {
				throw ErrorUtil.createRuntimeException("作业队列中破损数目或取失败!", e);
			}
			
			/**--------------------失败作业---------------------------*/		
			try {
				int failure_num = sm.getWorkQueueFailureNum();
				result.addPerfResult(new PerfResult("SHENTONG-WORKQUE-2", failure_num));
			} catch (SQLException e) {
				throw ErrorUtil.createRuntimeException("作业队列中失败数目或取失败!", e);
			}
			/**--------------------过期作业---------------------------*/
				
			try {
				int overDue_num = sm.getWorkQueueOverdueNum();
				result.addPerfResult(new PerfResult("SHENTONG-WORKQUE-3", overDue_num));
			} catch (SQLException e) {
				throw ErrorUtil.createRuntimeException("作业队列中过期数目或取失败!", e);				
			}		
	
			respTime = System.currentTimeMillis() - respTime;
	    if(respTime <= 0) {
	      respTime = 1;
	    }
	    result.setResponseTime(respTime);//响应时间
		} finally {
			sm.close();
		}
					
		return result;
	}
}
