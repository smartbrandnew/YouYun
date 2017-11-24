package com.broada.carrier.monitor.impl.db.oracle.basic;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.db.oracle.checkpoint.OracleCheckpointMonitor;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleErrorUtil;
import com.broada.carrier.monitor.impl.db.oracle.util.OracleManager;
import com.broada.carrier.monitor.method.oracle.OracleMethod;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * Oracle 基本配置监测类
 * 
 * @author Lixy (lixy@broada.com.cn)
 * Create By 2006-11-8 14:10:48
 */
public class OracleBaseMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(OracleBaseMonitor.class);
	private PerfResult[] asseblePerfResults(Map<String, Object> orclBase) {
		List<PerfResult> perfs = new ArrayList<PerfResult>();
		for (int i = 0; i < OracleBaseConfiger.keys.length; i++) {
			String value = String.valueOf(orclBase.get(OracleBaseConfiger.keys[i]));
			if (!("null".equalsIgnoreCase(value) || value.trim().length() < 1)) {
				perfs.add(new PerfResult("ORACLE-BASE-" + (i + 1), value));
			}
		}
		return perfs.toArray(new PerfResult[0]);
	}

	@Override
	public Serializable collect(CollectContext context) {
		CollectResult result = context.getResult();
		result.setProgress(1);
		result.setMessage("采集指标开始....");
		result.setState(CollectMonitorState.START);
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			result.setMessage("采集指标进行中....");
			result.setState(CollectMonitorState.PROCESSING);
			Map<String, Object> orclBase = om.getOracleBaseInfo();
			result.setPerfResults(asseblePerfResults(orclBase));
			result.setMessage("采集工作完成。");
			result.setProgress(100);
			result.setState(CollectMonitorState.SUCCESSED);
			return result;
		} catch (SQLException e) {
			result.setState(CollectMonitorState.FAILED);
			result.setProgress(100);
			result.setMessage(e.getMessage());
			return result;
		} finally {
			om.close();
		}
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		OracleMethod oracleMethod = new OracleMethod(context.getMethod());
		OracleManager om = new OracleManager(context.getNode().getIp(), oracleMethod);
		try {
			om.initConnection();
			Map<String, Object> orclBase = om.getOracleBaseInfo();
			return new MonitorResult(asseblePerfResults(orclBase));
		} catch (SQLException e) {
			throw OracleErrorUtil.createError(e);
		} finally {
			om.close();
		}
	}

}
