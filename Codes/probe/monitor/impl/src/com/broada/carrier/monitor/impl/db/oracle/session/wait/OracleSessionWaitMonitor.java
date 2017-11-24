package com.broada.carrier.monitor.impl.db.oracle.session.wait;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleErrorUtil;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class OracleSessionWaitMonitor  extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(OracleSessionWaitMonitor.class);
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			List<LockWait> waits = om.getLockWait();
			if(waits == null || waits.isEmpty()){
				result.setResultDesc("未采集到锁等待信息");
				return result;
			} else{
				for(LockWait wait:waits){
					MonitorResultRow row = new MonitorResultRow(wait.getSid(), wait.getSid());
					row.setIndicator("LOCK-WAIT-SECOND", wait.getSeconds_in_wait());
					row.addTag("sid:" + wait.getSid());
					result.addRow(row);
				}
				result.setState(MonitorState.SUCCESSED);
				return result;
			}
		} catch (SQLException e) {
			LOG.error("采集锁等待信息异常,{}", e);
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}
	
}
