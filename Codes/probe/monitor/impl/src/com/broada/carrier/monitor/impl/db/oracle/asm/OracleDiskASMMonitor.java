package com.broada.carrier.monitor.impl.db.oracle.asm;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.MultiInstanceConfiger;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleErrorUtil;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorItemType;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class OracleDiskASMMonitor extends BaseMonitor{
	//public static final MonitorType TYPE = new MonitorType("ORACLE-ASM", "Oracle-ASM监测", "Oracle-ASM磁盘组使用率监测。",
		//	MultiInstanceConfiger.class.getName(), OracleDiskASMMonitor.class.getName(), 1, new String[]{ "Oracle" }, new String[] { OracleMethod.TYPE_ID});
	//public static final MonitorItem ORACLE_ASM_INSTANCE = new MonitorItem("oracle_asm_name", "实例名", "", "实例名", MonitorItemType.TEXT);
	//public static final MonitorItem ORACLE_ASM_USERATE = new MonitorItem("oracle_asm_useRate", "空间使用率", "%", "空间使用率", MonitorItemType.NUMBER);
	//public static final MonitorItem ORACLE_ASM_AVAILABLE_SIZE = new MonitorItem("oracle_asm_availableSize", "可用空间", "MB", "可用空间", MonitorItemType.NUMBER);
	
	//public static final MonitorItem ORACLE_ASM_TOTAL_SIZE = new MonitorItem("oracle_asm_totalSize", "总空间", "MB", "总空间", MonitorItemType.NUMBER);
	
	private static final Log logger = LogFactory.getLog(OracleDiskASMMonitor.class);
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			List<OracleDiskASM> conditions = om.getOracleDiskASMList();
			//return fillCollectResultFromModels(tSpaces);
			for(OracleDiskASM o:conditions){
				String instance=o.getInstanceName();
				PerfResult perf1=new PerfResult("oracle_asm_useRate", o.getUseRate());
				perf1.setInstanceKey(instance);
				PerfResult perf2=new PerfResult("oracle_asm_availableSize", o.getAvailableSize());
				perf2.setInstanceKey(instance);
				PerfResult perf3=new PerfResult("oracle_asm_totalSize", o.getTotalSize());
				perf3.setInstanceKey(instance);
//				PerfResult perf4=new PerfResult(ORACLE_ASM_INSTANCE.getCode(), o.getInstanceName());
//				perf4.setInstanceKey(instance);
				result.addPerfResult(perf1);
				result.addPerfResult(perf2);
				result.addPerfResult(perf3);
//				result.addPerfResult(perf4);
			}
			return result;
		} catch (SQLException e) {
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}
	}

