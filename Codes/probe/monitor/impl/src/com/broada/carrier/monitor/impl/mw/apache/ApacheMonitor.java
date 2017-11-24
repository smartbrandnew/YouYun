package com.broada.carrier.monitor.impl.mw.apache;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.broada.carrier.monitor.impl.util.StringUtils;
import com.broada.carrier.monitor.method.apache.ApacheMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.component.utils.error.ErrorUtil;

/**
 * APACHE 监听器实现类
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: NMS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author Maico Pang,amyson
 * @version 1.0
 */

public class ApacheMonitor implements Monitor {
	private static final String ITEMCODE_REQCOUNT = "APACHE-1";// 累计全部访问数

	private static final String ITEMCODE_DATAPROC = "APACHE-2";// 累计全部处理千字节数(KB)

	private static final String ITEMCODE_CPULOAD = "APACHE-3";// CPU负载

	private static final String ITEMCODE_RUNTIME = "APACHE-4";// 正常运行时间

	private static final String ITEMCODE_RPS = "APACHE-5";// 每秒请求

	private static final String ITEMCODE_BPS = "APACHE-6";// 每秒处理字节

	private static final String ITEMCODE_BPQ = "APACHE-7";// 每请求处理字节

	private static final String ITEMCODE_BUSYWORKER = "APACHE-8";// 忙作业

	private static final String ITEMCODE_IDLEWORKER = "APACHE-9";// 空闲作业

	private static final String ITEMCODE_REPLYTIME = "APACHE-10";// 响应时间

	// 对应
	private static HashMap<String, String> MAPINFO = new HashMap<String, String>();
	static {
		MAPINFO.put("Total Accesses", ITEMCODE_REQCOUNT);
		MAPINFO.put("Total kBytes", ITEMCODE_DATAPROC);
		MAPINFO.put("CPULoad", ITEMCODE_CPULOAD);
		MAPINFO.put("Uptime", ITEMCODE_RUNTIME);
		MAPINFO.put("ReqPerSec", ITEMCODE_RPS);
		MAPINFO.put("BytesPerSec", ITEMCODE_BPS);
		MAPINFO.put("BytesPerReq", ITEMCODE_BPQ);
		MAPINFO.put("BusyWorkers", ITEMCODE_BUSYWORKER);
		MAPINFO.put("IdleWorkers", ITEMCODE_IDLEWORKER);
	}

	/* 单行去除HTML标志的正则表达式 */
	/* 有个问题,中文不能匹配 */
	public static final String REGEX_LINE = "<[^>]+>|&nbsp;|[ \t\f\\v]";

	/* 全部去除HTML标志的正则表达式 */
	/* 有个问题,中文不能匹配 */
	public static final String REGEX_ALL = "<[^>]+>|&nbsp;|[ \t\f\r\n\\v]";

	public ApacheMonitor() {
	}

	/**
	 * 实现监测,当前使用HttpClient包来完成HTTP请求
	 * 
	 * @param srv
	 * @return
	 */
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		PerfResult reqCount = new PerfResult(ITEMCODE_REQCOUNT, false);
		PerfResult reqDataProc = new PerfResult(ITEMCODE_DATAPROC, false);
		PerfResult reqCpuLoad = new PerfResult(ITEMCODE_CPULOAD, false);
		PerfResult reqRuntime = new PerfResult(ITEMCODE_RUNTIME, false);
		PerfResult reqRps = new PerfResult(ITEMCODE_RPS, false);
		PerfResult reqBps = new PerfResult(ITEMCODE_BPS, false);
		PerfResult reqBpq = new PerfResult(ITEMCODE_BPQ, false);
		PerfResult reqBusyWorker = new PerfResult(ITEMCODE_BUSYWORKER, false);
		PerfResult reqIdleWorker = new PerfResult(ITEMCODE_IDLEWORKER, false);
		PerfResult reqReplyTime = new PerfResult(ITEMCODE_REPLYTIME, false);

		PerfResult[] perfs = { reqCount, reqDataProc, reqCpuLoad, reqRuntime, reqRps, reqBps, reqBpq, reqBusyWorker,
				reqIdleWorker, reqReplyTime };

		String ip = context.getNode().getIp();
		ApacheMethodOption method = new ApacheMethodOption(context.getMethod());

		ApacheParameter p = new ApacheParameter();
		if (StringUtils.isNotNullAndTrimBlank(method.getDomain())) {
			p.setDomain(method.getDomain());
		}
		p.setPort(method.getPort());
		String protocl = method.getProtocol();
		if ("https".equalsIgnoreCase(protocl))
			p.setUseSSL("");
		int port = p.getPort();
		String absUrl = "/server-status?auto";
		String domain = p.getDomain();
		boolean chkDomain = p.isChkDomain();

		result.setState(MonitorState.FAILED);

		// 组合URL
		if (chkDomain) {
			ip = domain;
		}
		String protocol = "http://";
		if (p.isUseSSL())
			protocol = "https://";
		String httpUrl = protocol + ip + ":" + port + absUrl;
		try {
			new URL(httpUrl);
		} catch (MalformedURLException ex) {
			// 表示URL错误
			result.setMessage("无效的URL:" + absUrl);
			return result;
		}

		// 创建一个client实例
		HttpClient client = new HttpClient();
		// 设置socket超时3000ms！这是为了防止一些错误的配置
		// 引起的阻塞,譬如这样的方式连接到一个ftp服务器,就会导致
		// 正确连接但是无返回数据的情况,导致线程阻塞
		client.getParams().setSoTimeout(15000);
		// 设置一个GET请求
		GetMethod get = new GetMethod(httpUrl);

		long replyTime = 0;// 响应时间
		// 设置连接超时
		client.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
		// 通过客户端执行GET方法
		try {
			long time = System.currentTimeMillis();
			client.executeMethod(get);
			replyTime = System.currentTimeMillis() - time;
			if (replyTime <= 0) {
				replyTime = 1;
			}
			result.setResponseTime((int) replyTime);
			replyTime = replyTime / 1000;
		} catch (IllegalArgumentException e) {
			// url地址错误
			result.setMessage("无效的URL:" + absUrl);
			return closeAndReturn(result, get, null);
		} catch (UnknownHostException e) {
			// 无效地址
			result.setMessage("无效地址:" + ip);
			return closeAndReturn(result, get, null);
		} catch (ConnectTimeoutException e) {
			// 连接超时
			result.setMessage("连接目标地址" + ip + "超时");
			return closeAndReturn(result, get, null);
		} catch (ConnectException e) {
			// 无法到达目标地址
			result.setMessage("无法连接到" + ip + "的" + port + "端口");
			return closeAndReturn(result, get, null);
		} catch (SocketTimeoutException e) {
			// 读取数据超时
			result.setMessage("成功连接端口:" + port + "\n但读取数据超时或HTTP协议错误");
			result.setState(MonitorState.FAILED);
			return closeAndReturn(result, get, null);
		} catch (Throwable e) {
			// 未知错误
			result.setMessage(ErrorUtil.createMessage("监测失败", e));
			result.setState(MonitorState.FAILED);
			return closeAndReturn(result, get, null);
		}

		// HttpURLConnection conn = null;
		int statusCode = get.getStatusCode();
		// 需要用流的方式打开
		InputStream is = null;

		StringBuffer msg = new StringBuffer();
		// 判断是否校验返回状态码
		if (p.isChkStatusCode()) {
			if (statusCode == HttpURLConnection.HTTP_OK) {
			} else {
				// 不等于OK则返回
				msg.append("回应码" + statusCode + "!=200.\n");
				result.setMessage(msg.toString());
				result.setState(MonitorState.OVERSTEP);
				return closeAndReturn(result, get, is);
			}
		}
		String rets = null;
		try {
			rets = get.getResponseBodyAsString();
		} catch (IOException ex2) {
			result.setMessage("无法连接到" + port + "端口.");
			return closeAndReturn(result, get, is);
		}
		// 保存数据先
		HashMap<String, String> extractedInfo = null;
		try {
			extractedInfo = extractInfo(rets);
		} catch (Exception ex1) {
			msg.append("该URL路径有返回信息,但是该URL路径不能监测APACHE的性能！\n");
			result.setMessage(msg.toString());
			result.setState(MonitorState.FAILED);
			return closeAndReturn(result, get, is);
		}
		for (int i = 0; i < perfs.length - 1; i++) {
			PerfResult perf = perfs[i];
			String value = (String) extractedInfo.get(perf.getItemCode());
			if (value == null) {
				continue;
			} else {
				try {
					perf.setValue(toDbl(value));
					result.addPerfResult(perf);
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		reqReplyTime.setValue(replyTime);
		result.addPerfResult(reqReplyTime);
		result.setState(MonitorState.SUCCESSED);
		result.setMessage(msg.toString());
		return closeAndReturn(result, get, is);
	}

	/**
	 * 关闭并返回
	 * 
	 * @param mr
	 * @param get
	 * @param is
	 * @return
	 */
	private MonitorResult closeAndReturn(MonitorResult mr, GetMethod get, InputStream is) {
		close(get, is);
		return mr;
	}

	/**
	 * 关闭连接
	 * 
	 * @param conn
	 * @param is
	 */
	private void close(GetMethod get, InputStream is) {
		try {
			if (is != null) {
				is.close();
			}
			if (get != null) {
				get.releaseConnection();
			}
		} catch (Exception ex) {
		}
	}

	/** 转换为double类型 */
	protected static double toDbl(Object val) {
		if (!(val instanceof String))
			return 0;
		return Double.parseDouble((String) val);
	}

	/** 根据返回信息检出各项APACHE服务器性能信息 */
	protected static HashMap<String, String> extractInfo(String info) throws Exception {
		HashMap<String, String> hm = new HashMap<String, String>();
		String[] entries = strToArray(info, "\n");
		for (int i = 0; i < entries.length; i++) {
			String[] tmp = strToArray(entries[i], ":");
			String key = (String) MAPINFO.get(tmp[0]);
			hm.put(key, tmp[1]);
		}
		return hm;
	}

	protected static String[] strToArray(String source, String seprator) {
		Vector<String> stList = new Vector<String>();
		if (source == null || seprator == null)
			return null;
		int start = 0, lastPos = 0;
		while (true) {
			start = source.indexOf(seprator, start);
			if (start < 0) {
				if (lastPos < source.length())
					stList.add(source.substring(lastPos, source.length()));
				break;
			}
			if (start >= lastPos) {
				stList.add(source.substring(lastPos, start));
			}
			start += seprator.length();
			lastPos = start;
		}
		String[] ret = new String[stList.size()];
		stList.toArray(ret);
		return ret;
	}

	@Override
	public Serializable collect(CollectContext context) {
		return null;
	}
}