package com.broada.carrier.monitor.impl.db.oracle.asm.diskgroup;

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

public class OracleASMDiskGroupMonitor extends BaseMonitor {

private static final Log LOG = LogFactory.getLog(OracleASMDiskGroupMonitor.class);
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult(MonitorState.FAILED);
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			List<ASMDiskGroup> groups = om.getASMDiskGroup();
			if(groups == null || groups.isEmpty()){
				result.setResultDesc("未采集到ASM-Disk group 使用率信息");
				return result;
			} else{
				for(ASMDiskGroup group:groups){
					MonitorResultRow row = new MonitorResultRow(group.getName(), group.getName());
					row.setIndicator("ASM-DISK-GROUP-USAGE_PCT", group.getUsage_pct());
					row.addTag("name:" + group.getName());
					result.addRow(row);
				}
				result.setState(MonitorState.SUCCESSED);
				return result;
			}
		} catch (SQLException e) {
			LOG.error("采集ASM-Disk group 使用率信息异常,{}", e);
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}
	
}
