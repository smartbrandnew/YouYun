package com.broada.carrier.monitor.impl.mw.tongweb.dbpool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

public class TongWebDBPoolMonitorVer4 implements Monitor {
	private static final Log logger = LogFactory.getLog(TongWebDBPoolMonitor.class);
	private final static String MBEAN_NAME = "teas:type=datasource,*";

	private static Map<String, String> MAPINFO = new HashMap<String, String>();

	static {

		MAPINFO.put("poolSize", "TONGWEB4-DBPOOL-1");
		MAPINFO.put("currentNumberOfJDBCConnectionOpen", "TONGWEB4-DBPOOL-2");
		MAPINFO.put("jDBCMinConnPool", "TONGWEB4-DBPOOL-3");
		MAPINFO.put("jDBCMaxConnPool", "TONGWEB4-DBPOOL-4");
		MAPINFO.put("getConnectionTimeOut", "TONGWEB4-DBPOOL-5");

	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		String ipAddr = context.getNode().getIp();

		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		boolean state = true;
		TongWebMonitorMethodOption option = new TongWebMonitorMethodOption(context.getMethod());

		String jndiName = option.getJndiName();

		int jndiPort = option.getPort();

		TongWebDBPoolManagerVer4 manager = new TongWebDBPoolManagerVer4(ipAddr, jndiName, jndiPort);
		List beanList = null;

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
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
			return result;
		}

		try {
			beanList = manager.fetchMBeanInfo(MBEAN_NAME, TongWebDBPoolinfoVer4.class);
		} catch (MBeanException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			if (logger.isDebugEnabled()) {
				logger.debug("获取TongWeb数据库连接池信息失败.", e);
			}
			result.setResultDesc("无法取得TongWeb数据库连接池信息.");
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
		for (Iterator iter = new ArrayList(Arrays.asList(context.getInstances())).iterator(); iter.hasNext();) {
			MonitorInstance mi = (MonitorInstance) iter.next();
			String instKey = mi.getCode();
			for (int i = 0, size = beanList.size(); i < size; i++) {
				TongWebDBPoolinfoVer4 poolInfo = (TongWebDBPoolinfoVer4) beanList.get(i);
				if (poolInfo != null & poolInfo.getName().equalsIgnoreCase(instKey)) {
					if (poolInfo.getGetConnectionTimeOut() == null
							|| "null".equalsIgnoreCase(poolInfo.getGetConnectionTimeOut().toString()))
						poolInfo.setGetConnectionTimeOut(0);
					if (poolInfo.getjDBCMaxConnPool() == null
							|| "null".equalsIgnoreCase(poolInfo.getjDBCMaxConnPool().toString()))
						poolInfo.setjDBCMaxConnPool(0);
					if (poolInfo.getjDBCMinConnPool() == null
							|| "null".equalsIgnoreCase(poolInfo.getjDBCMinConnPool().toString()))
						poolInfo.setjDBCMinConnPool(0);
					if (poolInfo.getCurrentNumberOfJDBCConnectionOpen() == null
							|| "null".equalsIgnoreCase(poolInfo.getCurrentNumberOfJDBCConnectionOpen().toString()))
						poolInfo.setCurrentNumberOfJDBCConnectionOpen(0);
					if (poolInfo.getPoolSize() == null || "null".equalsIgnoreCase(poolInfo.getPoolSize().toString()))
						poolInfo.setPoolSize(0);
					List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
					BeanMap poolInfoMap = new BeanMap(poolInfo);
					for (Iterator it = poolInfoMap.keyIterator(); it.hasNext();) {
						String key = (String) it.next();
						if (MAPINFO.containsKey(key)) {
							PerfItemMap perfItemMap = new PerfItemMap(MAPINFO.get(key), key, poolInfoMap.get(key));
							nameForIndex.add(perfItemMap);
						}
					}
					perfList.addAll(Arrays.asList(AbstractTongWebManager.assemblePerf(nameForIndex, instKey)));
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

	@Override
	public Serializable collect(CollectContext context) {
		TongWebMonitorMethodOption option = new TongWebMonitorMethodOption(context.getMethod());

		String jndiName = option.getJndiName();
		int jndiPort = option.getPort();
		List beanList;
		AbstractTongWebManager manager = new TongWebDBPoolManagerVer4(context.getNode().getIp(), jndiName, jndiPort);
		try {
			manager.init();
			beanList = manager.fetchMBeanInfo(MBEAN_NAME, TongWebDBPoolinfoVer4.class);
		} catch (NamingException e) {
			throw new CollectException("TongWeb连接失败.", e);
		} catch (MBeanException e) {
			throw new CollectException("无法获取TongWeb连接信息.", e);
		} finally {
			manager.close();
		}

		List monitorInstances = new ArrayList();
		for (Object object : beanList) {
			TongWebDBPoolinfoVer4 poolInfo = (TongWebDBPoolinfoVer4) object;
			MonitorInstance mi = new MonitorInstance();
			mi.setInstanceKey(poolInfo.getName());
			mi.setInstanceName(poolInfo.getName());
			monitorInstances.add(mi);
		}
		List perfList = new ArrayList();
		for (Iterator iter = monitorInstances.iterator(); iter.hasNext();) {
			MonitorInstance mi = (MonitorInstance) iter.next();
			String instKey = mi.getCode();
			for (int i = 0, size = beanList.size(); i < size; i++) {
				TongWebDBPoolinfoVer4 poolInfo = (TongWebDBPoolinfoVer4) beanList.get(i);
				if (poolInfo != null & poolInfo.getName().equalsIgnoreCase(instKey)) {
					if (poolInfo.getGetConnectionTimeOut() == null
							|| "null".equalsIgnoreCase(poolInfo.getGetConnectionTimeOut().toString()))
						poolInfo.setGetConnectionTimeOut(0);
					if (poolInfo.getjDBCMaxConnPool() == null
							|| "null".equalsIgnoreCase(poolInfo.getjDBCMaxConnPool().toString()))
						poolInfo.setjDBCMaxConnPool(0);
					if (poolInfo.getjDBCMinConnPool() == null
							|| "null".equalsIgnoreCase(poolInfo.getjDBCMinConnPool().toString()))
						poolInfo.setjDBCMinConnPool(0);
					if (poolInfo.getCurrentNumberOfJDBCConnectionOpen() == null
							|| "null".equalsIgnoreCase(poolInfo.getCurrentNumberOfJDBCConnectionOpen().toString()))
						poolInfo.setCurrentNumberOfJDBCConnectionOpen(0);
					if (poolInfo.getPoolSize() == null || "null".equalsIgnoreCase(poolInfo.getPoolSize().toString()))
						poolInfo.setPoolSize(0);
					List<PerfItemMap> nameForIndex = new ArrayList<PerfItemMap>();
					BeanMap poolInfoMap = new BeanMap(poolInfo);
					for (Iterator it = poolInfoMap.keyIterator(); it.hasNext();) {
						String key = (String) it.next();
						if (MAPINFO.containsKey(key)) {
							PerfItemMap perfItemMap = new PerfItemMap(MAPINFO.get(key), key, poolInfoMap.get(key));
							nameForIndex.add(perfItemMap);
						}
					}
					perfList.addAll(Arrays.asList(AbstractTongWebManager.assemblePerf(nameForIndex, instKey)));
				}
			}
		}

		MonitorResult result = new MonitorResult();
		if (perfList.size() == 0) {
			throw new RuntimeException("该版本不支持此项监测");
		}
		result.setPerfResults((PerfResult[]) perfList.toArray(new PerfResult[perfList.size()]));
		return result;
	}
}
