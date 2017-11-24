package com.broada.carrier.monitor.impl.mw.tongweb.conn;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.tongweb.AbstractTongWebManager;
import com.broada.carrier.monitor.impl.mw.tongweb.MBeanException;
import com.broada.carrier.monitor.impl.mw.tongweb.PerfItemMap;
import com.broada.carrier.monitor.method.tongweb.TongWebMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.NamingException;
import java.io.Serializable;
import java.util.*;

public class TongWebConnMonitor implements Monitor {
	private static final Log logger = LogFactory.getLog(TongWebConnMonitor.class);
	public final static String MBEAN_NAME = "teas:type=connectionMethod,*";

	public final static String[] CONDITION_FIELDS = new String[] { "liveThreadNumber", "requestWaitingNumber" };

	private static Map<String, String> MAPINFO = new HashMap<String, String>();

	static {
		MAPINFO.put("waitingThreads", "TONGWEB-CONN-1");
		MAPINFO.put("liveThreadNumber", "TONGWEB-CONN-2");
		MAPINFO.put("maxHandlers", "TONGWEB-CONN-3");
		MAPINFO.put("queueSize", "TONGWEB-CONN-4");
		MAPINFO.put("clientTimeout", "TONGWEB-CONN-5");
		MAPINFO.put("threadTimeout", "TONGWEB-CONN-6");
		MAPINFO.put("requestWaitingNumber", "TONGWEB-CONN-7");
		MAPINFO.put("throughPutRatio", "TONGWEB-CONN-8");
		MAPINFO.put("bytesSentRatio", "TONGWEB-CONN-9");
		MAPINFO.put("bytesReceivedRatio", "TONGWEB-CONN-10");
	}

	@Override public MonitorResult monitor(MonitorContext context) {
		String ipAddr = context.getNode().getIp();
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		boolean state = true;

		TongWebMonitorMethodOption option = new TongWebMonitorMethodOption(context.getMethod());

		String jndiName = option.getJndiName();

		int jndiPort = option.getPort();

		TongWebConnManager manager = new TongWebConnManager(ipAddr, jndiName, jndiPort);

		long replyTime = System.currentTimeMillis();
		try {
			manager.init();
		} catch (NamingException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("无法连接到目标TongWeb的JNDI端口,IP:" + ipAddr + ",PORT:" + jndiPort + ",JNDI NAME=" + jndiName, e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("无法连接到目标TongWeb服务器,原因:" + e.getMessage() + ".");
			manager.close();
			return result;
		}

		List beanList = null;
		try {
			beanList = manager.fetchMBeanInfo(MBEAN_NAME, TongWebConnInfo.class);
		} catch (MBeanException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("获取连接信息失败.", e);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("无法取得TongWeb上的连接信息.");
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
			return result;
		} finally {
			manager.close();
		}
		replyTime = System.currentTimeMillis() - replyTime;
		if (replyTime <= 0)
			replyTime = 1L;
		result.setResponseTime(replyTime);

		List perfList = new ArrayList();
		for (Iterator iter = new ArrayList(Arrays.asList(context.getInstances())).iterator(); iter.hasNext(); ) {
			MonitorInstance mi = (MonitorInstance) iter.next();
			String instKey = mi.getCode();
			for (int i = 0, size = beanList.size(); i < size; i++) {
				TongWebConnInfo connInfo = (TongWebConnInfo) beanList.get(i);

				if (connInfo != null & connInfo.getConnectionid().equalsIgnoreCase(instKey)) {

					TongWebConnManager.calculateRatio(connInfo, context.getTask().getId() + instKey);

					List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
					BeanMap connInfoMap = new BeanMap(connInfo);
					for (Iterator it = connInfoMap.keyIterator(); it.hasNext(); ) {
						String key = (String) it.next();
						if (MAPINFO.containsKey(key)) {
							PerfItemMap perfItemMap = new PerfItemMap(MAPINFO.get(key), key, connInfoMap.get(key));
							nameForIndex.add(perfItemMap);
						}
					}

					perfList.addAll(Arrays.asList(TongWebConnManager.assemblePerf(nameForIndex, instKey)));
				}
			}
		}

		result.setPerfResults((PerfResult[]) perfList.toArray(new PerfResult[0]));

		if (!state) {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(manager.getResultDesc().toString());
		} else {
			result.setState(MonitorConstant.MONITORSTATE_NICER);
			result.setResultDesc("监测一切正常");
			// result.setResultDesc("所有会话各监测项都在正常范围.");
			// result.setCurrentVal("会话各监测项都正常.");
		}

		return result;

	}

	@Override public Serializable collect(CollectContext context) {
		TongWebMonitorMethodOption option = new TongWebMonitorMethodOption(context.getMethod());
		String jndiName = option.getJndiName();
		int jndiPort = option.getPort();
		AbstractTongWebManager manager = new TongWebConnManager(context.getNode().getIp(), jndiName, jndiPort);
		List beanList;
		try {
			manager.init();
			beanList = manager.fetchMBeanInfo(TongWebConnMonitor.MBEAN_NAME, TongWebConnInfo.class);
		} catch (NamingException e) {
			throw new CollectException("TongWeb连接失败.", e);
		} catch (MBeanException e) {
			throw new CollectException("无法获取TongWeb连接信息.", e);
		} finally {
			manager.close();
		}
		List monitorInstances = new ArrayList();
		for (Object object : beanList) {
			TongWebConnInfo connInfo = (TongWebConnInfo) object;
			MonitorInstance mi = new MonitorInstance();
			mi.setInstanceKey(connInfo.getConnectionid());
			mi.setInstanceName(connInfo.getConnectionid());
			monitorInstances.add(mi);
		}

		List perfList = new ArrayList();
		for (Iterator iter = monitorInstances.iterator(); iter.hasNext(); ) {
			MonitorInstance mi = (MonitorInstance) iter.next();
			String instKey = mi.getCode();
			for (int i = 0, size = beanList.size(); i < size; i++) {
				TongWebConnInfo connInfo = (TongWebConnInfo) beanList.get(i);

				if (connInfo != null & connInfo.getConnectionid().equalsIgnoreCase(instKey)) {

					TongWebConnManager.calculateRatio(connInfo, -1 + instKey);

					List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
					BeanMap connInfoMap = new BeanMap(connInfo);
					for (Iterator it = connInfoMap.keyIterator(); it.hasNext(); ) {
						String key = (String) it.next();
						if (MAPINFO.containsKey(key)) {
							PerfItemMap perfItemMap = new PerfItemMap(MAPINFO.get(key), key, connInfoMap.get(key));
							nameForIndex.add(perfItemMap);
						}
					}
					perfList.addAll(Arrays.asList(TongWebConnManager.assemblePerf(nameForIndex, instKey)));
				}
			}
		}
		MonitorResult result = new MonitorResult();
		result.setPerfResults((PerfResult[]) perfList.toArray(new PerfResult[perfList.size()]));
		return result;
	}
}
