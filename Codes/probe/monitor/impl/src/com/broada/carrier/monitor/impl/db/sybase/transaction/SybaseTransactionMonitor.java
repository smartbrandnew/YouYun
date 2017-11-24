package com.broada.carrier.monitor.impl.db.sybase.transaction;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.sybase.SybaseEnhanceManager;
import com.broada.carrier.monitor.method.sybase.SybaseMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author chenmw
 */
public class SybaseTransactionMonitor extends BaseMonitor {
	static final String TOTAL_TRANC_INDEX = "SYBASE-TRANSACTION-1";
	static final String SUMPEC_TRANC_INDEX = "SYBASE-TRANSACTION-2";
	static final String ABORT_TRANC_INDEX = "SYBASE-TRANSACTION-3";
	private static final Log logger = LogFactory.getLog(SybaseTransactionMonitor.class);

	@Override public Serializable collect(CollectContext context) {

		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorState.UNMONITOR);

		PerfResult totalTranRst = new PerfResult(TOTAL_TRANC_INDEX, false);
		PerfResult sumPecTranRst = new PerfResult(SUMPEC_TRANC_INDEX, false);
		PerfResult abortTranRst = new PerfResult(ABORT_TRANC_INDEX, false);
		PerfResult[] perfs = new PerfResult[] { totalTranRst, sumPecTranRst, abortTranRst };
		result.setPerfResults(perfs);

		String ip = context.getNode().getIp();
		SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());
		int port = option.getPort();
		String sid = "";

		StringBuffer msgSB = new StringBuffer(); // 监测结果信息描述

		//  先测试连接
		boolean ok = testConnect(ip, port);
		if (!ok) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("服务端口无法连接");
			return result;
		}

		String user = option.getUsername();
		if (user == null) {
			user = "sa";
		}
		String passd = option.getPassword();
		if (passd == null) {
			passd = "";
		}
		SybaseEnhanceManager sem = null;
		long replyTime = System.currentTimeMillis();
		try {
			sem = new SybaseEnhanceManager(ip, sid, port, user, passd);
			sem.initConnection();
		} catch (ClassNotFoundException e) {
			if (logger.isDebugEnabled())
				logger.debug("无效的Sybase数据库jdbc连接驱动");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("无效的Sybase数据库jdbc连接驱动");
			return result;
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				logger.debug("无法连接到Sybase数据库或连接超时.");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("无法连接到Sybase数据库或连接超时.");
			if (sem != null) {
				sem.close();
			}
			return result;
		}

		List transactions = new ArrayList();

		try {
			transactions = sem.getTransaction();
		} catch (Exception ee) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取Sybase数据库事务信息失败.");
			return result;
		} finally {
			sem.close();
		}

		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0) {
			replyTime = 1;
		}
		result.setResponseTime(replyTime);

		for (Iterator iter = transactions.iterator(); iter.hasNext(); ) {
			SybaseTransaction st = (SybaseTransaction) iter.next();
			if (st != null) {
				if (SybaseTransaction.FIELDS[0].equalsIgnoreCase(st.getField())) {
					totalTranRst.setValue(st.getTransactionNumPerSec());					
				} else if (SybaseTransaction.FIELDS[1].equalsIgnoreCase(st.getField())) {
					sumPecTranRst.setValue(st.getTransactionNumPerSec());
				} else if (SybaseTransaction.FIELDS[2].equalsIgnoreCase(st.getField())) {
					abortTranRst.setValue(st.getTransactionNumPerSec());
				}
			}
		}

		if (msgSB.length() > 0) {
			result.setResultDesc(msgSB.toString());
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
		} else {
			result.setState(MonitorConstant.MONITORSTATE_NICER);
			result.setResultDesc("监测一切正常");
		}

		result.setPerfResults(perfs);

		return result;
	}

	/**
	 * 使用Socket测试连接指定端口
	 *
	 * @param ip
	 * @param port
	 * @return
	 */
	private boolean testConnect(String ip, int port) {
		Socket socket = null;
		try {
			socket = new Socket();
			SocketAddress sa = new InetSocketAddress(ip, port);
			socket.connect(sa, 5000);
			return true;
		} catch (IOException ex) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ex1) {
				}
			}
		}
		return false;
	}
}
