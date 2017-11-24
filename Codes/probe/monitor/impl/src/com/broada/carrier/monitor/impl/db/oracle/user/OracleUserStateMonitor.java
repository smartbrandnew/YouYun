package com.broada.carrier.monitor.impl.db.oracle.user;

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

public class OracleUserStateMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(OracleUserStateMonitor.class);
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			List<UserState> states = om.getUserState();
			if(states == null || states.isEmpty()){
				result.setResultDesc("未采集到用户状态信息");
				return result;
			} else{
				for(UserState state:states){
					MonitorResultRow row = new MonitorResultRow(state.getUsername(), state.getUsername());
					row.setIndicator("USER-WAIT-STATE", state.getStatus());
					row.addTag("username:" + state.getUsername());
					result.addRow(row);
				}
				result.setState(MonitorState.SUCCESSED);
				return result;
			}
		} catch (SQLException e) {
			LOG.error("采集用户状态信息异常,{}", e);
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}
	
}