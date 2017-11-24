package com.broada.carrier.monitor.impl.mw.jboss.jdbc;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Iterator;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.jboss.JbossHttpGetUtil;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class Jboss7JdbcMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(Jboss7JdbcMonitor.class);
	private static final String ITEMIDX_JBOSS7_JDBC_BLOCKINGTIMEOUTMILLIS = "JBOSS7-JDBC-1";

	private static final String ITEMIDX_JBOSS7_JDBC_MAXSIZE = "JBOSS7-JDBC-2";

	private static final String ITEMIDX_JBOSS7_JDBC_MINSIZE = "JBOSS7-JDBC-3";

	private static final String ITEMIDX_JBOSS7_JDBC_IDLETIMEOUTMINUTES = "JBOSS7-JDBC-4";

	private static final String ITEMIDX_JBOSS7_JDBC_NEWCONNECTIONSQL = "JBOSS7-JDBC-5";

	private static final String ITEMIDX_JBOSS7_JDBC_POOLSIZE = "JBOSS7-JDBC-6";
	
	private static final String ITEMIDX_JBOSS7_JDBC_ACTIVECOUNT = "JBOSS7-JDBC-7";
	
	private static final String ITEMIDX_JBOSS7_JDBC_AVAILABLECOUNT = "JBOSS7-JDBC-8";
	
	private static final String ITEMIDX_JBOSS7_JDBC_INUSECOUNT = "JBOSS7-JDBC-9";
	
	private static final String ITEMIDX_JBOSS7_JDBC_AVERAGEBLOCKTIME= "JBOSS7-JDBC-10";
	
	private static final String ITEMIDX_JBOSS7_JDBC_CREATECOUNT= "JBOSS7-JDBC-11";
	
	private static final String ITEMIDX_JBOSS7_JDBC_DESTROYCOUNT= "JBOSS7-JDBC-12";

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
		result.setState(MonitorConstant.MONITORSTATE_NICER);
		JbossJMXOption option = new JbossJMXOption(context.getMethod());
		if (!"7.x".equalsIgnoreCase(option.getVersion()) && !"6.x-eap".equalsIgnoreCase(option.getVersion())) {
			throw new RuntimeException("该项监测不支持" + option.getVersion() + "版本");
		}
		String host = option.getIpAddr();
		String username = option.getUsername();
		String password = option.getPassword();
		int port = option.getPort();
		String url = "http://" + host + ":" + port + "/management/subsystem/datasources/data-source";

		int blockingTimeoutMillis = 0;
		int maxSize = 0;
		int minSize = 0;
		long idleTimeoutMinutes = 0;
		long newConnectionSql = 0;
		int active = 0;//活动总数
		int available = 0;//可用连接数
		int inUse = 0;//当前连接数
		int averageTime = 0;
		int create = 0;
		int destroy = 0;
		try {
			GetMethod method = JbossHttpGetUtil.getMethod(url, username, password);
			String str = method.getResponseBodyAsString();
			method.releaseConnection();
			JSONObject json = new JSONObject(str);
			String obj = json.get("data-source").toString();
			JSONObject dataJson = new JSONObject(obj);
			@SuppressWarnings("rawtypes")
			Iterator tor = dataJson.keys();
			while (tor.hasNext()) {
				String key = tor.next().toString();
				url = "http://" + host + ":" + port + "/management/subsystem/datasources/data-source/" + key;
				method = JbossHttpGetUtil.getMethod(url, username, password);
				str = method.getResponseBodyAsString();
				method.releaseConnection();
				json = new JSONObject(str);
				String max = json.get("max-pool-size").toString();
				String min = json.get("min-pool-size").toString();
				String idleTimeOut = json.get("idle-timeout-minutes").toString();
				String blockTimeOut = json.get("blocking-timeout-wait-millis").toString();
				String newConnSql = json.get("new-connection-sql").toString();
				if (max != null && !"null".equalsIgnoreCase(max))
					maxSize = Integer.parseInt(max);
				if (min != null && !"null".equalsIgnoreCase(min))
					minSize = Integer.parseInt(min);
				if (idleTimeOut != null && !"null".equalsIgnoreCase(idleTimeOut))
					idleTimeoutMinutes = Long.parseLong(idleTimeOut);
				if (blockTimeOut != null && !"null".equalsIgnoreCase(blockTimeOut))
					blockingTimeoutMillis = Integer.parseInt(blockTimeOut);
				if (newConnSql != null && !"null".equalsIgnoreCase(newConnSql))
					newConnectionSql = 1;
				
				url = "http://" + host + ":" + port + "/management/subsystem/datasources/data-source/" + key + "/statistics/pool?include-runtime=true";
				method = JbossHttpGetUtil.getMethod(url, username, password);
				str = method.getResponseBodyAsString();
				method.releaseConnection();
				json = new JSONObject(str);
				String activeCount = json.get("ActiveCount").toString();//活动总数
				String availableCount = json.get("AvailableCount").toString();//可用连接数
				String inUseCount = json.get("InUseCount").toString();//当前连接数
				String averageBlockingTime = json.get("AverageBlockingTime").toString();//平均阻塞时间
				String createCount = json.get("CreatedCount").toString();//当前连接数
				String destroyCount = json.get("DestroyedCount").toString();//平均阻塞时间
				if (activeCount != null && !"null".equalsIgnoreCase(activeCount))
					active =  Integer.parseInt(activeCount);
				if (availableCount != null && !"null".equalsIgnoreCase(availableCount))
					available =  Integer.parseInt(availableCount);
				if (inUseCount != null && !"null".equalsIgnoreCase(inUseCount))
					inUse = Integer.parseInt(inUseCount);
				if (averageBlockingTime != null && !"null".equalsIgnoreCase(averageBlockingTime))
					averageTime = Integer.parseInt(averageBlockingTime);
				if (createCount != null && !"null".equalsIgnoreCase(createCount))
					create = Integer.parseInt(createCount);
				if (destroyCount != null && !"null".equalsIgnoreCase(destroyCount))
					destroy = Integer.parseInt(destroyCount);
				MonitorResultRow row = new MonitorResultRow(key);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_MAXSIZE, maxSize);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_MINSIZE, minSize);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_IDLETIMEOUTMINUTES, idleTimeoutMinutes);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_BLOCKINGTIMEOUTMILLIS, blockingTimeoutMillis);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_NEWCONNECTIONSQL, newConnectionSql);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_POOLSIZE, maxSize);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_ACTIVECOUNT, active);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_AVAILABLECOUNT, available);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_INUSECOUNT, inUse);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_AVERAGEBLOCKTIME, averageTime);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_CREATECOUNT, create);
				row.setIndicator(ITEMIDX_JBOSS7_JDBC_DESTROYCOUNT, destroy);
				result.addRow(row);
			}
			return result;
		} catch (ConnectTimeoutException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("连接目标地址" + option.getIpAddr() + "超时.");
			if (logger.isDebugEnabled()) {
				logger.debug("连接目标地址" + option.getIpAddr() + "超时.", e);
			}
			return result;
		} catch (ConnectException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("无法连接到" + option.getIpAddr() + "的" + option.getPort() + "端口.");
			if (logger.isDebugEnabled()) {
				logger.debug("无法连接到" + option.getIpAddr() + "的" + option.getPort() + "端口.", e);
			}
			return result;
		} catch (SocketTimeoutException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("成功连接端口:" + option.getPort() + ",但读取数据超时或HTTP协议错误.");
			if (logger.isDebugEnabled()) {
				logger.debug("成功连接端口:" + option.getPort() + ",但读取数据超时或HTTP协议错误.", e);
			}
			return result;
		} catch (IOException e) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("IO错误:" + e.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug("IO错误:", e);
			}
			return result;
		} catch (Throwable t) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("未知错误:" + t.getMessage());
			if (logger.isDebugEnabled()) {
				logger.debug("未知错误:", t);
			}
			return result;
		}

	}
}
