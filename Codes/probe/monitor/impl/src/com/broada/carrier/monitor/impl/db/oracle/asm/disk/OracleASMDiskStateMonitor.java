package com.broada.carrier.monitor.impl.db.oracle.asm.disk;

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

public class OracleASMDiskStateMonitor extends BaseMonitor {
	
	private static final Log LOG = LogFactory.getLog(OracleASMDiskStateMonitor.class);
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			List<ASMDiskState> states = om.getASMDiskState();
			if(states == null || states.isEmpty()){
				result.setResultDesc("未采集到ASM-Disk状态信息");
				return result;
			} else{
				for(ASMDiskState state:states){
					MonitorResultRow row = new MonitorResultRow(state.getPath(), state.getPath());
					row.setIndicator("ASM-DISK-STATE", state.getMode_status().equalsIgnoreCase("online")?1:0);
					row.addTag("path:" + state.getPath());
					result.addRow(row);
				}
				result.setState(MonitorState.SUCCESSED);
				return result;
			}
		} catch (SQLException e) {
			LOG.error("采集ASM-Disk 状态异常,{}", e);
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}
	
}
