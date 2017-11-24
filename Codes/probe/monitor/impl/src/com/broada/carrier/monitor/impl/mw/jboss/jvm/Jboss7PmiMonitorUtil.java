package com.broada.carrier.monitor.impl.mw.jboss.jvm;

import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.mw.jboss.JbossHttpGetUtil;
import com.broada.carrier.monitor.method.jboss.JbossJMXOption;

public class Jboss7PmiMonitorUtil {
	private static final Logger logger = LoggerFactory.getLogger(Jboss7PmiMonitorUtil.class);
	private static int maxHeapMemory = 0;
	private static int usedHeapMemory = 0;
	private static int committedHeapMemory = 0;
	private static int initHeapMemory = 0;
	private static int maxNonHeapMeomory = 0;
	private static int usedNonHeapMemory = 0;
	private static int committedNonHeapMemory = 0;
	private static int initNonHeapMemory = 0;

	public static PmiJboss7Information getJboss7PmiInfo(JbossJMXOption jbossjmxoption) throws HttpException, IOException,
			JSONException {
		String host = jbossjmxoption.getIpAddr();
		String username = jbossjmxoption.getUsername();
		String password = jbossjmxoption.getPassword();
		int port = jbossjmxoption.getPort();
		int edenMax = 0;
		int edenCommitted = 0;
		int edenInit = 0;
		int edenUsed = 0;
		int survivorMax = 0;
		int survivorCommitted = 0;
		int survivorInit = 0;
		int survivorUsed = 0;
		int tenuredMax = 0;
		int tenuredCommitted = 0;
		int tenuredInit = 0;
		int tenuredUsed = 0;
		int permMax = 0;
		int permCommitted = 0;
		int permInit = 0;
		int permUsed = 0;
		int codeMax = 0;
		int codeCommitted = 0;
		int codeInit = 0;
		int codeUsed = 0;
		int threadCount = 0;
		int daemonThreadCount = 0;

		try {
			String url1 = "http://" + host + ":" + port
					+ "/management/core-service/platform-mbean/type/memory-pool/name/PS_Eden_Space";
			GetMethod method1 = JbossHttpGetUtil.getMethod(url1, username, password);
			String str1 = method1.getResponseBodyAsString();
			method1.releaseConnection();
			JSONObject edenJson = new JSONObject(str1);
			JSONObject edenSon = edenJson.getJSONObject("usage");

			edenMax = edenSon.getInt("max");
			edenCommitted = edenSon.getInt("committed");
			edenInit = edenSon.getInt("init");
			edenUsed = edenSon.getInt("used");

			String url2 = "http://" + host + ":" + port
					+ "/management/core-service/platform-mbean/type/memory-pool/name/PS_Survivor_Space";
			GetMethod method2 = JbossHttpGetUtil.getMethod(url2, username, password);
			String str2 = method2.getResponseBodyAsString();
			method2.releaseConnection();
			JSONObject survivorJson = new JSONObject(str2);
			JSONObject survivorSon = survivorJson.getJSONObject("usage");

			survivorMax = survivorSon.getInt("max");
			survivorCommitted = survivorSon.getInt("committed");
			survivorInit = survivorSon.getInt("init");
			survivorUsed = survivorSon.getInt("used");

			String url3 = "http://" + host + ":" + port
					+ "/management/core-service/platform-mbean/type/memory-pool/name/PS_Old_Gen";
			GetMethod method3 = JbossHttpGetUtil.getMethod(url3, username, password);
			String str3 = method3.getResponseBodyAsString();
			method3.releaseConnection();
			JSONObject tenuredJson = new JSONObject(str3);
			JSONObject tenuredSon = tenuredJson.getJSONObject("usage");

			tenuredMax = tenuredSon.getInt("max");
			tenuredCommitted = tenuredSon.getInt("committed");
			tenuredInit = tenuredSon.getInt("init");
			tenuredUsed = tenuredSon.getInt("used");

			String url4 = "http://" + host + ":" + port
					+ "/management/core-service/platform-mbean/type/memory-pool/name/PS_Perm_Gen";
			GetMethod method4 = JbossHttpGetUtil.getMethod(url4, username, password);
			String str4 = method4.getResponseBodyAsString();
			method4.releaseConnection();
			JSONObject permJson = new JSONObject(str4);
			JSONObject permdSon = permJson.getJSONObject("usage");

			permMax = permdSon.getInt("max");
			permCommitted = permdSon.getInt("committed");
			permInit = permdSon.getInt("init");
			permUsed = permdSon.getInt("used");

			String url5 = "http://" + host + ":" + port
					+ "/management/core-service/platform-mbean/type/memory-pool/name/Code_Cache";
			GetMethod method5 = JbossHttpGetUtil.getMethod(url5, username, password);
			String str5 = method5.getResponseBodyAsString();
			method5.releaseConnection();
			JSONObject codeJson = new JSONObject(str5);
			JSONObject codedSon = codeJson.getJSONObject("usage");

			codeMax = codedSon.getInt("max");
			codeCommitted = codedSon.getInt("committed");
			codeInit = codedSon.getInt("init");
			codeUsed = codedSon.getInt("used");

			String url6 = "http://" + host + ":" + port
					+ "/management/core-service/platform-mbean/type/threading";
			GetMethod method6 = JbossHttpGetUtil.getMethod(url6, username, password);
			String str6 = method6.getResponseBodyAsString();
			method6.releaseConnection();
			JSONObject threadJson = new JSONObject(str6);
			threadCount = threadJson.getInt("thread-count");
			daemonThreadCount = threadJson.getInt("daemon-thread-count");
		} catch (JSONException e) {
			logger.debug("json异常", e);
		} catch (Exception e) {
			logger.debug("未知异常: ", e);
		}
		maxHeapMemory = (edenMax + survivorMax + tenuredMax) / 1024 / 1024;//MB
		committedHeapMemory = (edenCommitted + survivorCommitted + tenuredCommitted) / 1024 / 1024;//MB
		initHeapMemory = (edenInit + survivorInit + tenuredInit) / 1024 / 1024;//MB
		usedHeapMemory = (edenUsed + survivorUsed + tenuredUsed) / 1024 / 1024;//MB
		maxNonHeapMeomory = (permMax + codeMax) / 1024 / 1024;
		committedNonHeapMemory = (permCommitted + codeCommitted) / 1024 / 1024;
		initNonHeapMemory = (permInit + codeInit) / 1024 / 1024;
		usedNonHeapMemory = (permUsed + codeUsed) / 1024 / 1024;

		PmiJboss7Information info = new PmiJboss7Information();
		info.setCommittedHeapMemory(committedHeapMemory);
		info.setCommittedNonHeapMemory(committedNonHeapMemory);
		info.setInitHeapMemory(initHeapMemory);
		info.setInitNonHeapMemory(initNonHeapMemory);
		info.setMaxHeapMemory(maxHeapMemory);
		info.setMaxNonHeapMeomory(maxNonHeapMeomory);
		info.setUsedHeapMemory(usedHeapMemory);
		info.setUsedNonHeapMemory(usedNonHeapMemory);
		info.setThreadCount(threadCount);
		info.setDaemonThreadCount(daemonThreadCount);
		return info;
	}
}
