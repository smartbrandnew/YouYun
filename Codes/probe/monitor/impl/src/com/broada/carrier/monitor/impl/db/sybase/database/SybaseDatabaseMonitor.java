package com.broada.carrier.monitor.impl.db.sybase.database;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.db.sybase.SybaseManager;
import com.broada.carrier.monitor.method.sybase.SybaseMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.*;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.utils.ListUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;

/**
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-7 下午06:00:55
 */
public class SybaseDatabaseMonitor implements Monitor {
	private static final Log logger = LogFactory.getLog(SybaseDatabaseMonitor.class);
	private static final int ITEMIDX_LENGTH = 5;
	private static final String ITEM_CODE = "SYBASE-DATABASE-";

	@Override public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorState.UNMONITOR);

		List<MonitorInstance> insts = new ArrayList(Arrays.asList(context.getInstances()));
		if (ListUtil.isNullOrEmpty(insts))
			return null;
		boolean dbsWonteds[] = new boolean[insts.size()];

		PerfResult[] perfs = null;
		if (perfs == null || insts.size() * ITEMIDX_LENGTH != perfs.length) {
			perfs = new PerfResult[insts.size() * ITEMIDX_LENGTH];
			for (int i = 0; i < perfs.length; i++) {
				perfs[i] = new PerfResult("-1", "" + 0, false);
			}
		}
		result.setPerfResults(perfs);

		String ip = context.getNode().getIp();
		SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());
		int port = option.getPort();
		// 下面2句到底什么意思？
		String sid = "";

		StringBuffer msgSB = new StringBuffer(); // 监测结果信息描述
		StringBuffer valSB = new StringBuffer(); // 当前情况，用于发送Trap

		// 先测试连接
		boolean ok = testConnect(ip, port);
		if (ok) {
			msgSB.append("服务端口可连接.\n");
			valSB.append("服务端口可连接.");
		} else {
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

		SybaseManager manager = new SybaseManager(ip, sid, port, user, passd);
		long replyTime = System.currentTimeMillis();
		try {
			manager.initConnection();
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
			manager.close();
			return result;
		}

		for (int i = 0; i < insts.size(); i++) {
			dbsWonteds[i] = true;
		}
		Map dbs = new HashMap();
		try {
			for (int i = 0; i < insts.size(); i++) {
				MonitorInstance inst = insts.get(i);
				SybaseDatabase db = manager.getSybaseDatabase(inst.getCode());
				dbs.put(inst.getCode(), db);
			}
		} catch (Exception ee) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取Sybase数据库实例信息失败.");
			return result;
		} finally {
			manager.close();
		}

		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0) {
			replyTime = 1;
		}
		result.setResponseTime(replyTime);

		for (int i = 0, wontedCount = 0; i < perfs.length; i = i + ITEMIDX_LENGTH, wontedCount++) {
			MonitorInstance instance = insts.get(wontedCount);
			String instKey = instance.getCode();
			SybaseDatabase db = (SybaseDatabase) dbs.get(instKey);
			if (db == null)
				continue;
			for (int j = 0; j < ITEMIDX_LENGTH; j++) {
				perfs[i + j].setInstanceKey(instKey);
				perfs[i + j].setItemCode("SYBASE-DATABASE-" + (j + 1));
				double pValue = getPerfValue(db, j + 1);
				if (pValue >= 0) {
					perfs[i + j].setValue(pValue);
				}
			}

			boolean isWonted = true;

			dbsWonteds[wontedCount] = isWonted;
		}
		MonitorState state = MonitorConstant.MONITORSTATE_NICER;
		for (int i = 0; i < dbsWonteds.length; i++) {
			if (!dbsWonteds[i]) {
				state = MonitorConstant.MONITORSTATE_OVERSTEP;
				break;
			}
		}

		if (state == MonitorConstant.MONITORSTATE_NICER) {
			result.setResultDesc("监测一切正常");
		} else {
			result.setResultDesc(msgSB.toString());
		}

		result.setPerfResults(perfs);
		result.setState(state);
		return result;
	}

	private double getPerfValue(SybaseDatabase db, int itemIdx) {
		if (db == null)
			return 0;
		switch (itemIdx) {
		case 1:
			return db.getDbSize();
		case 2:
			return db.getDataSize();
		case 3:
			return db.getIdxSize();
		case 4:
			return db.getUsedSize();
		case 5:
			return db.getUsedRate();
		default:
			return 0;
		}
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

	@Override public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();

		SybaseMonitorMethodOption option = new SybaseMonitorMethodOption(context.getMethod());

		String ip = context.getNode().getIp();
		String sid = "";
		int port = option.getPort();
		String user = option.getUsername();
		String passwd = option.getPassword();
		SybaseManager sm = new SybaseManager(ip, sid, port, user, passwd);
		List<String> dbNames = null;
		try {
			sm.initConnection();
			dbNames = sm.getDbNames();
		} catch (ClassNotFoundException e) {
			throw new CollectException("无效的Sybase数据库jdbc连接驱动", e);
		} catch (Exception e) {
			// 关闭连接
			sm.close();
			throw new CollectException("无法连接到Sybase数据库或连接超时", e);
		}

		List<SybaseDatabase> dbs = new ArrayList<SybaseDatabase>();
		try {
			for (String dbName : dbNames) {
				SybaseDatabase db = sm.getSybaseDatabase(dbName);
				if (db != null)
					dbs.add(db);
			}
		} catch (Exception e) {
			throw new CollectException("获取Sybase数据库实例信息失败", e);
		} finally {
			// 关闭连接
			sm.close();
		}

		for (SybaseDatabase db : dbs) {
			MonitorResultRow row = new MonitorResultRow(db.getDbName());
			for (int i = 0; i < ITEMIDX_LENGTH; i++) {
				row.setIndicator(ITEM_CODE + (i + 1), getPerfValue(db, i + 1));
			}
			result.addRow(row);
		}

		return result;
	}

}
