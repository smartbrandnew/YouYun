package com.broada.carrier.monitor.impl.db.oracle.sga;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.oracle.exception.LogonDeniedException;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;

/**
 * Oracle SGA 监测实现类
 * 
 * @author Lixy (lixy@broada.com.cn)
 * Create By 2006-10-31 16:34:15
 */
public class OracleSGAMonitor extends BaseMonitor {
	private static final Log log = LogFactory.getLog(OracleSGAMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		OracleMethod option = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), option);
		long respTime = System.currentTimeMillis();
		try {
			om.initConnection();
		} catch (LogonDeniedException lde) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			om.close();
			return result;
		} catch (SQLException e) {
			String errMsg = e.getMessage();
			if (log.isDebugEnabled()) {
				log.debug(errMsg, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(errMsg);
			om.close();
			return result;
		}

		try {
			result.addPerfResult(doCondition(om, "ORACLE-SGA-1", 0));
			result.addPerfResult(doCondition(om, "ORACLE-SGA-2", 1));
			result.addPerfResult(doCondition(om, "ORACLE-SGA-3", 2));
			result.addPerfResult(doCondition(om, "ORACLE-SGA-4", 3));
			result.addPerfResult(doCondition(om, "ORACLE-SGA-5", 4));
			result.addPerfResult(doCondition(om, "ORACLE-SGA-6", 5));
			result.addPerfResult(doCondition(om, "ORACLE-SGA-7", 6));
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
		} finally {
			om.close();
		}
		return result;
	}

	/**
	 * OracleSGA 性能比较
	 * @param om
	 * @param perfResult
	 * @param condition
	 * @param msg
	 * @param desc
	 * @param msgSB
	 * @param valSB
	 * @return
	 */
	private PerfResult doCondition(OracleManager om, String itemCode, int index) {
		double size = 0;
		try {
			switch (index) {
			case 0:
				size = om.getSGALibraryCacheSize();
				break;
			case 1:
				size = om.getSGARedoLogCacheSize();
				break;
			case 2:
				size = om.getSGASharedPoolSize();
				break;
			case 3:
				size = om.getSGADictionCacheSize();
				break;
			case 4:
				size = om.getSGASharedCacheSize();
				break;
			case 5:
				size = om.getSGASqlCacheSize();
				break;
			case 6:
				size = om.getSGAHitRate();
				break;
			}

			size = new BigDecimal(size).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
			return new PerfResult(itemCode, size);
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException("获取SGA信息失败", e);
		}
	}
}
