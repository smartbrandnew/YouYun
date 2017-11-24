package com.broada.carrier.monitor.impl.db.oracle.wait;

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

public class OracleWaitEventMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(OracleWaitEventMonitor.class);
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			List<WaitEvent> events = om.getWaitEvent();
			if(events == null || events.isEmpty()){
				result.setResultDesc("未采集到等待事件信息");
				return result;
			} else{
				for(WaitEvent event:events){
					MonitorResultRow row = new MonitorResultRow(event.getEvent(), event.getEvent());
					row.setIndicator("WAIT-EVENT-ACT-SESSION", event.getAct_session());
					row.setIndicator("WAIT-EVENT-RATE", event.getRate());
					row.addTag("event:" + event.getEvent());
					result.addRow(row);
				}
				result.setState(MonitorState.SUCCESSED);
				return result;
			}
		} catch (SQLException e) {
			LOG.error("采集等待事件信息异常,{}", e);
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}
	
}