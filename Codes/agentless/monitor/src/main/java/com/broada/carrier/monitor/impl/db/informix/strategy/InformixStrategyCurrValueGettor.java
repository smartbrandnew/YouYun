package com.broada.carrier.monitor.impl.db.informix.strategy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategy;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategyGroup;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.InformixStrategyResult;
import com.broada.carrier.monitor.impl.db.informix.strategy.entity.StrategyLastResult;
import com.broada.carrier.monitor.impl.db.informix.strategy.interceptor.StrategyResultIntercetor;
import com.broada.carrier.monitor.method.cli.parser.ScriptManager;
import com.broada.utils.JDBCUtil;
import com.broada.utils.StringUtil;

/**
 * 通过策略组获取监测结果集
 * @author lixy Sep 2, 2008 3:59:16 PM
 */
public class InformixStrategyCurrValueGettor {
	private static final DecimalFormat formator = new DecimalFormat("00.00");
	private static final Log logger = LogFactory.getLog(InformixStrategyCurrValueGettor.class);

	private Connection conn = null;

	private boolean repeatMonitor = false;

	public InformixStrategyCurrValueGettor(Connection conn) {
		if (conn == null) {
			throw new RuntimeException("数据连接为空。");
		}
		this.conn = conn;
	}

	/**
	 * 根据策略组ID获取策略结果集
	 * 
	 * @param strategyGroupId
	 * @return
	 * @throws SQLException 
	 * @throws SQLException 
	 */
	public InformixStrategyResult getStrategyResult(String strategyGroupId, String srvId) throws SQLException {
		InformixStrategyResult result = new InformixStrategyResult(strategyGroupId);
		InformixStrategyGroup group = InformixStrategyFacade.getStrategyGroup(strategyGroupId);

		Map<String, Double> groupParams = new HashMap<String, Double>();
		initParams(group.getSql(), groupParams);

		Map<String, InformixStrategy> strategies = group.getStrategies();
		if (!strategies.isEmpty()) {
			for (Iterator<InformixStrategy> iter = strategies.values().iterator(); iter.hasNext();) {
				InformixStrategy strategy = iter.next();
				Double d = new Double(0);
				try {
					d = getStrategyResult(groupParams, strategy, strategyGroupId, srvId);
				} catch (SQLException e) {
					logger.error("获取" + group.getName() + "-->" + strategy.getName() + "的值失败", e);
					continue;
				}
				result.putResult(strategy.getItemCode(), new Double(formator.format(d.doubleValue())));
			}
		}
		if(repeatMonitor){
			repeatMonitor = false;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			return this.getStrategyResult(strategyGroupId, srvId);
		}
		return result;
	}

	private Double getStrategyResult(Map<String, Double> groupParams, InformixStrategy strategy,String groupId, String srvId)
	throws SQLException {
		Map<String, Double> strategyParams = new HashMap<String, Double>();
		initParams(strategy.getSql(), strategyParams);
		Double currValue;
		try {
			if (StringUtil.isNullOrBlank(strategy.getBsh())) {
				currValue = getFiledValueFromParams(strategy.getField(), groupParams, strategyParams);
			} else {
				currValue = this.executeStrategy(strategy.getBsh(), groupParams, strategyParams);
			}
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		return intercetorResult(strategy, groupId, srvId, currValue);
	}

	private Double intercetorResult(InformixStrategy strategy, String groupId, String srvId, Double currValue) {
		List<StrategyResultIntercetor> intercetors = strategy.getIntercetors();
		if (intercetors.isEmpty()) {
			return currValue;
		}
		String itemCode = strategy.getItemCode();
		Double tmp = new Double(currValue.doubleValue());
		for (int i = 0; i < intercetors.size(); i++) {
			StrategyResultIntercetor itercetor = intercetors.get(i);
			tmp = itercetor.intercetor(srvId, itemCode, tmp);
			if (tmp.doubleValue() == StrategyResultIntercetor.REPEAT_MONIOTR_VALUE.doubleValue()) {
				repeatMonitor = true;
				break;
			}
		}

		StrategyLastResult.putReustInfo(srvId, itemCode, new Date(), currValue);
		return tmp;
	}

	private Double executeStrategy(String bsh, Map<String, Double> groupParams, Map<String, Double> strategyParams) throws Exception {
		ScriptManager sm = ScriptManager.getInstance("beanshell");
		putParams(sm, groupParams);
		putParams(sm, strategyParams);
		try {
			return (Double) sm.eval(bsh);
		} catch (Exception e) {
			throw new Exception("执行脚本" + bsh + "失败。", e);
		}
	}

	private void putParams(ScriptManager sm, Map<String, Double> params) {
		for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			sm.put(key.trim(), params.get(key));
		}
	}

	@SuppressWarnings("rawtypes")
	private Double getFiledValueFromParams(String filed, Map groupParams, Map strategyParams) throws Exception {
		if (strategyParams.get(filed) != null) {
			return new Double(strategyParams.get(filed).toString());
		} else if (groupParams.get(filed) != null) {
			return new Double(groupParams.get(filed).toString());
		} else {
			throw new Exception("获取" + filed + "信息失败.");
		}

	}

	private void initParams(String sql, Map<String, Double> groupParams) throws SQLException {
		if (!StringUtil.isNullOrBlank(sql)) {
			executeSQL(sql, groupParams);
		}
	}

	private void executeSQL(String sql, Map<String, Double> results) throws SQLException {
		String[] sqlArr = sql.split(";");
		for (int i = 0; i < sqlArr.length; i++) {
			executeQuery(sqlArr[i], results);
		}
	}

	private void executeQuery(String sql, Map<String, Double> results) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				results.put(rs.getString("name").trim(), rs.getDouble("value"));
			}
		} catch (Exception ee) {
			throw new SQLException("执行" + sql + "查询失败,原因：" + ee.getMessage());
		} finally {
			JDBCUtil.close(rs, ps);
		}
	}

}
