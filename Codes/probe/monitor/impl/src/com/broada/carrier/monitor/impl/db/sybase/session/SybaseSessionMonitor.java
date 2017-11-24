package com.broada.carrier.monitor.impl.db.sybase.session;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.sybase.SybaseEnhanceManager;
import com.broada.carrier.monitor.method.sybase.SybaseMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.utils.StringUtil;

public class SybaseSessionMonitor implements Monitor {
	public static final String[] COLUMNS = { "memUsage", "cpuTime" };

	static final int ITEMIDX_LENGTH = 9;

	@Override public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());
		//连接参数的获取
		String ip = context.getNode().getIp();
		int port = option.getPort();
		String user = option.getUsername();
		String pass = option.getPassword();

		boolean state = true;
		//标识为有动态实例
		StringBuffer msgSB = new StringBuffer();

		SybaseEnhanceManager om = null;
		long respTime = System.currentTimeMillis();
		try {
			om = new SybaseEnhanceManager(ip, null, port, user, pass);
		} catch (Exception lde) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc(lde.getMessage());
			respTime = System.currentTimeMillis() - respTime;
			if (respTime <= 0) {
				respTime = 1;
			}
			result.setResponseTime(respTime);
			if (om != null) {
				om.close();
			}
			return result;
		}

		List sessList = null;
		try {
			sessList = om.getSessions();
		} catch (SQLException e) {
			result.setResultDesc("无法获取当前连接会话列表.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			return result;
		} finally {
			if (om != null) {
				om.close();
			}
		}
		respTime = System.currentTimeMillis() - respTime;
		if (respTime <= 0) {
			respTime = 1;
		}
		result.setResponseTime(respTime);

		PerfResult[] perfs = null;
		if (perfs == null || sessList.size() * ITEMIDX_LENGTH != perfs.length) {
			perfs = new PerfResult[sessList.size() * ITEMIDX_LENGTH];
			for (int i = 0; i < perfs.length; i++) {
				perfs[i] = new PerfResult("-1", "" + 0, false);
			}
		}

		boolean[] sessWonteds = new boolean[sessList.size()];

		// 设置返回性能数据
		result.setPerfResults(perfs);

		MonitorInstance[] instances = new MonitorInstance[sessList.size()];
		for (int i = 0, perfIndex = 0; i < sessList.size(); perfIndex = perfIndex + ITEMIDX_LENGTH, i++) {
			SybaseSession sess = (SybaseSession) sessList.get(i);

			//创建实例
			instances[i] = new MonitorInstance(sess.getPid(), sess.getPid());

			boolean isWonted = true;
			//告警项监测
			boolean tmpWonted = true;
			//性能项监测
			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				//性能值
				perfs[perfIndex + j].setInstanceKey(sess.getPid());
				perfs[perfIndex + j].setItemCode("SYBASE-SESSION-" + (j + 1));
				if (j < 6) {
					String value = (String) getSessValue(sess, j);
					if (!StringUtil.isNullOrBlank(value)) {
						perfs[perfIndex + j].setStrValue(value);
					} 
				} else {
					Double value = ((Double) getSessValue(sess, j)).doubleValue();
					if (value >= 0) {
						perfs[perfIndex + j].setValue(value.doubleValue());
					} 
				}
			}

			//只比较running状态的会话
			if ("running".equalsIgnoreCase(StringUtils.trim(sess.getStatus()))) {
				for (int j = 0; j < COLUMNS.length; j++) {
					isWonted = isWonted && tmpWonted;
				}
			}

			sessWonteds[i] = isWonted;
		}
		//按sessionId排序
		Arrays.sort(instances, new OrderComparator(OrderComparator.FIELD_NAMELEN));

		for (int i = 0; i < sessWonteds.length; i++) {
			if (sessWonteds[i] == false) {
				state = false;
				break;
			}
		}

		if (!state) {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(msgSB.toString());
		} else {
			result.setState(MonitorConstant.MONITORSTATE_NICER);
			result.setResultDesc("监测一切正常");
		}
		return result;
	}

	/**
	 * 根据索引取得属性值
	 *
	 * @param sess
	 * @param colIndex
	 * @return
	 */
	private Object getSessValue(SybaseSession sess, int colIndex) {
		switch (colIndex) {
		case 0:
			return sess.getStatus();
		case 1:
			return sess.getUser();
		case 2:
			return sess.getHost();
		case 3:
			return sess.getProgram();
		case 4:
			return sess.getDatabase();
		case 5:
			return sess.getCommand();
		case 6:
			return sess.getMemUsage();
		case 7:
			return sess.getCpuTime();
		case 8:
			return sess.getIoNumber();
		default:
			return null;
		}
	}

	@Override public Serializable collect(CollectContext context) {

		MonitorResult result = new MonitorResult();

		SybaseEnhanceManager sm = null;
		try {
			SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());
			String ip = context.getNode().getIp();
			String sid = "";
			int port = option.getPort();
			String user = option.getUsername();
			String passwd = option.getPassword();

			sm = new SybaseEnhanceManager(ip,sid,port,user,passwd);
		} catch (ClassNotFoundException e) {
			throw new CollectException("无效的Sybase数据库jdbc连接驱动", e);
		} catch (Exception e) {
			sm.close();
			throw new CollectException("无法获取数据段列表", e);
		}

		List<SybaseSession> sessList = null;
		try {
			sessList = sm.getSessions();
		} catch (SQLException e) {
			throw new CollectException("无法获取当前连接会话列表", e);
		} finally {
			sm.close();
		}

		PerfResult[] perfs = null;
		if (perfs == null || sessList.size() * ITEMIDX_LENGTH != perfs.length) {
			perfs = new PerfResult[sessList.size() * ITEMIDX_LENGTH];
			for (int i = 0; i < perfs.length; i++) {
				perfs[i] = new PerfResult("-1", ""+0, false);
			}
		}
		for (int i = 0; i < sessList.size(); i++) {
			SybaseSession sess = (SybaseSession) sessList.get(i);
			// 性能项监测
			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				// 性能值
				perfs[i * ITEMIDX_LENGTH + j].setInstanceKey(sess.getPid());
				perfs[i * ITEMIDX_LENGTH + j].setItemCode("SYBASE-SESSION-" + (j + 1));
				if (j < 6) {
					String value = (String) getSessValue(sess, j);
					if (!StringUtil.isNullOrBlank(value)) {
						perfs[i * ITEMIDX_LENGTH + j].setStrValue(value);
					} else {
						perfs[i * ITEMIDX_LENGTH + j].setStrValue("");
					}
				} else {
					Double value = ((Double) getSessValue(sess, j)).doubleValue();
					if (value >= 0) {
						perfs[i * ITEMIDX_LENGTH + j].setValue(value.doubleValue());
					} else {
						perfs[i * ITEMIDX_LENGTH + j].setValue(0);
					}
				}
			}
		}
		result.setPerfResults(perfs);
		// 设置动态实例
		return result;
	}

}
