package com.broada.carrier.monitor.impl.db.informix.dbspace;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.informix.InformixManager;
import com.broada.carrier.monitor.method.informix.InformixMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Informax数据空间使用率监测
 *
 * @author Maico Pang (panghf@broada.com.cn)
 *         Create By 2006-6-8 21:39:23
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DbSpaceMonitor implements Monitor {

	private static final String ITEMIDX_USEDPER = "INFORMIX-DBSPACE-1";

	/**
	 * 根据dsName获取空间使用率
	 *
	 * @param dSpaceList
	 * @param dsName
	 * @return
	 */
	private double getUsedPer(List dSpaceList, String dsName) {
		double usedPer = -1;
		if (dSpaceList == null || dSpaceList.size() <= 0 || dsName == null || dsName.equals("")) {
			return usedPer;
		}
		for (Iterator itr = dSpaceList.iterator(); itr.hasNext(); ) {
			InformixDataBaseSpace ds = (InformixDataBaseSpace) itr.next();
			if (dsName.equalsIgnoreCase(ds.getName())) {
				usedPer = ds.getCurPerDBS().doubleValue();
				break;
			}
		}
		usedPer = new BigDecimal(usedPer).setScale(2, 4).doubleValue();
		return usedPer;
	}

	@Override public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		InformixMonitorMethodOption option = new InformixMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();

		int port = option.getPort();
		String srvName = option.getServername();
		String userName = option.getUsername();
		String passwd = option.getPassword();

		List<MonitorInstance> insts = new ArrayList(Arrays.asList(context.getInstances()));
		int count = insts.size();
		StringBuffer msgSB = new StringBuffer();
		StringBuffer valSB = new StringBuffer();
		boolean state = true;
		boolean getFail = false;
		InformixManager im = new InformixManager(ip, port, srvName, userName, passwd);
		long replyTime = System.currentTimeMillis();
		try {
			im.initConnection();
		} catch (SQLException ex) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(ex.getMessage());
			im.close();
			return result;
		}

		List dbSpaceList = null;
		try {
			dbSpaceList = im.getDataBaseSpaces();
		} catch (SQLException e) {
			String errMsg = e.getMessage();
			if (e.getMessage() == null || e.getMessage().equals("")) {
				errMsg = "无法获取数据空间!";
			}
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(errMsg);
			return result;
		} finally {
			im.close();
		}
		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0) {
			replyTime = 1;
		}
		result.setResponseTime((int) replyTime);

		for (int i = 0; i < count; i++) {
			MonitorInstance instance = insts.get(i);
			double usedPer = getUsedPer(dbSpaceList, instance.getCode());
			MonitorResultRow row = new MonitorResultRow(instance.getCode());
			if (usedPer >= 0) {
				row.setIndicator(ITEMIDX_USEDPER, usedPer);
			} else {
				row.setIndicator(ITEMIDX_USEDPER, 0d);
			}
			result.addRow(row);
		}

		if (getFail) {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(msgSB.toString());
		} else {
			if (!state) {
				result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
				msgSB.deleteCharAt(msgSB.length() - 1);
				valSB.deleteCharAt(valSB.length() - 1);
				result.setResultDesc(msgSB.toString());
			} else {
				result.setState(MonitorConstant.MONITORSTATE_NICER);
			}
		}
		return result;
	}

	@Override public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		InformixMonitorMethodOption option = new InformixMonitorMethodOption(context.getMethod());
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String dbServerName = option.getServername();
		String user = option.getUsername();
		String pass = option.getPassword();
		InformixManager im = new InformixManager(ip, port, dbServerName, user, pass);
		List<InformixDataBaseSpace> tSpaces = new ArrayList(); // 表格中的列表,其中的元素为InformixDataBaseSpace对象
		try {
			im.initConnection();
			tSpaces = im.getDataBaseSpaces();
		} catch (SQLException ex) {
			throw new CollectException("无法获取数据空间!", ex);
		} finally {
			im.close();
		}
		for (int i = 0; i < tSpaces.size(); i++) {
			InformixDataBaseSpace space = tSpaces.get(i);
			MonitorResultRow row = new MonitorResultRow(space.getName());
			double usedPer = space.getCurPerDBS().doubleValue();
			usedPer = new BigDecimal(usedPer).setScale(2, 4).doubleValue();
			if (usedPer >= 0) {
				row.setIndicator(ITEMIDX_USEDPER, usedPer);
			} else {
				row.setIndicator(ITEMIDX_USEDPER, 0);
			}
			result.addRow(row);
		}
		return result;
	}

}
