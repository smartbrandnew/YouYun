package com.broada.carrier.monitor.impl.mw.websphere;

import java.awt.Frame;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import com.broada.carrier.monitor.common.util.WorkPathUtil;
import com.broada.carrier.monitor.impl.common.convertor.ConvertUtil;
import com.broada.carrier.monitor.impl.mw.websphere.entity.WASMonitorResult;
import com.broada.carrier.monitor.impl.mw.websphere.entity.XMLLock;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Item;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Type;
import com.broada.carrier.monitor.impl.mw.websphere.ssl.BroadaSecureProtocolSocketFactory;
import com.broada.carrier.monitor.method.cli.parser.ScriptManager;
import com.broada.carrier.monitor.method.websphere.WASParamPanel;
import com.broada.common.util.ConcurrentLocker;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.utils.ListUtil;
import com.broada.utils.StringUtil;

/**
 * 
 * @author lixy Sep 17, 2008 11:43:44 AM
 */
public class WASUtil {
	private static String DEBUG_FILE;
	private static final Log logger = LogFactory.getLog(WASUtil.class);
	public static final String inst_hyphen = "->";// 实例连接符

	static {
		DEBUG_FILE = System.getProperty("monitor.websphere.debug.file");
	}

	/**
	 * 格式化url key = xml文件的${...} 替换成value
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String applyParams(String str, Map<String, String> params) {
		if (StringUtil.isNullOrBlank(str) || params == null || params.isEmpty()) {
			return StringUtil.convertNull2Blank(str);
		}
		for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
			String param = iter.next();
			String paramValue = params.get(param);
			str = str.replaceAll("\\$\\{" + param + "\\}", paramValue);
		}
		String useSSL = params.get("useSSL").toLowerCase();
		if (useSSL == "true" || useSSL.equals("true")) {
			str = str.replaceAll("http://", "https://");
		}
		return str;
	}

	@SuppressWarnings("deprecation")
	public static GetMethod getHttpGet(String url, String host,
			String username, String password) throws HttpException,
			IOException, InterruptedException {
		String locker = "wasutil." + host;
		int waitTime = ConcurrentLocker.getInstance().begin(locker, 1);
		if (logger.isDebugEnabled())
			logger.debug(String.format("操作[%s]等待了%dms", locker, waitTime));

		try {
			// 创建一个client实例
			HttpClient client = new HttpClient();
			// 设置socket超时30000ms！这是为了防止一些错误的配置
			// 引起的阻塞,譬如这样的方式连接到一个ftp服务器,就会导致
			// 正确连接但是无返回数据的情况,导致线程阻塞
			client.getParams().setSoTimeout(30000);
			// 设置一个GET请求
			GetMethod get = new GetMethod(url);
			// 配置一个请求用户,对于部分服务器是必要的,譬如GOOGLE若是没有请求身份,将什么也不能得到
			// UsernamePasswordCredentials upc =
			// new UsernamePasswordCredentials("htmltest", "test");
			NTCredentials upc = new NTCredentials();
			upc.setUserName(username == null ? "foo" : username);
			upc.setPassword(password == null ? "bar" : password);
			upc.setDomain(host);
			upc.setHost(host);

			// 暂时没有域,p.getRealm一定为null
			client.getState().setCredentials(null, null, upc);
			get.setDoAuthentication(true);
			// 设置连接超时
			client.setConnectionTimeout(30000);
			// 通过客户端执行GET方法
			client.executeMethod(get);
			return get;
		} finally {
			ConcurrentLocker.getInstance().end(locker);
		}
	}

	public static Document build(InputStream is) throws IOException,
			JDOMException {
		try {
			SAXBuilder sb = new SAXBuilder(false);
			sb.setValidation(false);

			sb.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId,
						String systemId) {
					InputSource is = new InputSource(new StringReader(""));
					is.setPublicId(publicId);
					is.setSystemId(systemId);
					return is;
				}
			});
			return sb.build(is, "");
		} catch (IOException ee) {
			ErrorUtil.warn(logger, "输入流XML解析失败", ee);
			throw ee;
		} catch (JDOMException ee) {
			ErrorUtil.warn(logger, "输入流XML解析失败", ee);
			throw ee;
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static Document build(GetMethod get, String host, String port)
			throws IOException, JDOMException {
		return build(get.getResponseBodyAsStream());
	}

	public static String getCurrWASVersion(Document doc, String eleId,
			String value) {
		Element root = doc.getRootElement();
		if (StringUtil.isBlank(eleId)) {
			return root.getAttributeValue(value);
		} else {
			String[] eleIds = eleId.split(">");
			Element currEle = root;
			for (int i = 0; i < eleIds.length; i++) {
				currEle = currEle.getChild(eleIds[i]);
			}
			return currEle.getAttributeValue(value);
		}
	}

	public static void link(String url, Map<String, String> attrs)
			throws IOException, InterruptedException {
		String host = attrs.get("host");
		String username = attrs.get("username");
		String password = attrs.get("password");
		url = applyParams(url, attrs);
		GetMethod get = null;
		try {
			String useSSL = attrs.get("useSSL").toLowerCase();
			if (useSSL == "true" || useSSL.equals("true")) {
				registerHttps(attrs);
			}
			get = getHttpGet(url, host, username, password);
			get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler());// http.method.retry-handler
			if (get.getStatusCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException("无法连接到Websphere服务器或连接超时，HTTP连接信息："
						+ get.getStatusCode());
			}
		} catch (IOException e) {

			throw new IOException("无法连接到Websphere服务器或连接超时："
					+ exception2String(e));
		} finally {
			if (get != null) {
				get.releaseConnection();
			}
		}
	}

	public static Map<String, Element> getMonitorElements(Type type,
			String node, String server, Map<String, String> attrs, File file)
			throws JDOMException, IOException, InterruptedException {
		FileInputStream is = new FileInputStream(file);
		Document doc = build(is);
		return getMonitorElements(type, node, server, attrs, doc);
	}

	public static Map<String, Element> getMonitorElements(Type type,
			String node, String server, Map<String, String> attrs, Document doc)
			throws JDOMException, IOException, InterruptedException {
		Element root = doc.getRootElement();
		String attr = type.getAttr();
		Map<String, Element> eles = new HashMap<String, Element>();
		List<String> tmp = null;
		if (node == null || server == null) {
			try {
				tmp = getNodeServers(root, attr);
				for (Iterator<String> iter = tmp.iterator(); iter.hasNext();) {
					String[] nodeSrv = iter.next().split(">");
					eles.putAll(getElements(root, type.getParsePath(),
							nodeSrv[0], nodeSrv[1], attr, type.getInstAttr(),
							type.getChildName(), attrs));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			eles.putAll(getElements(root, type.getParsePath(), node, server,
					attr, type.getInstAttr(), type.getChildName(), attrs));
		}
		return eles;
	}

	public static Map<String, Element> getMonitorElements(Type type,
			String host, String port, String node, String server,
			String username, String password, Map<String, String> attrs)
			throws JDOMException, IOException, InterruptedException {
		if (DEBUG_FILE != null) {
			File file = new File(DEBUG_FILE);
			return getMonitorElements(type, node, server, attrs, file);
		}

		String url = type.getUrl();
		Map<String, String> params = new HashMap<String, String>();
		params.put("host", host);
		params.put("port", port);
		params.putAll(attrs);
		url = applyParams(url, params);
		synchronized (XMLLock.getInstance()) {
			GetMethod get = null;
			try {
				String useSSL = attrs.get("useSSL").toLowerCase();
				if (useSSL == "true" || useSSL.equals("true")) {
					registerHttps(params);
				}
				get = getHttpGet(url, host, username, password);
				Document doc = build(get, host, port);
				return getMonitorElements(type, node, server, params, doc);
			} catch (IOException e) {
				throw new IOException("无法连接到Websphere服务器或连接超时："
						+ e.getMessage());
			} catch (JDOMException e) {
				throw new JDOMException("获取Websphere性能数据出错：" + e.getMessage());
			} finally {
				if (get != null) {
					get.releaseConnection();
				}
			}
		}
	}

	private static Map<String, Element> getElements(Element root,
			String parsePath, String node, String server, String attr,
			String instAttr, String childName, Map<String, String> attrs) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("node", (node == null) ? "" : node);
		params.put("server", (server == null) ? "" : server);
		params.putAll(attrs);
		Map<String, Element> eleMap = new HashMap<String, Element>();
		Map<String, List<Element>> eles = get(root,
				applyParams(parsePath, params), attr, childName);
		for (Iterator<String> iter = eles.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			List<Element> eleList = eles.get(key);
			for (Iterator<Element> eleIter = eleList.iterator(); eleIter
					.hasNext();) {
				Element ele = eleIter.next();
				String instName = StringUtil.isNullOrBlank(instAttr) ? ele
						.getName() : ele.getAttributeValue(instAttr);
				eleMap.put(key + instName, ele);
			}
		}
		return eleMap;
	}

	@SuppressWarnings("unchecked")
	private static List<String> getNodeServers(Element rootEle, String attr) {
		List<String> list = new ArrayList<String>();
		List<Element> nodes = rootEle.getChildren();
		if (!ListUtil.isNullOrEmpty(nodes)) {
			for (Iterator<Element> nodeIter = nodes.iterator(); nodeIter
					.hasNext();) {
				Element node = nodeIter.next();
				List<Element> servers = node.getChildren();
				if (!ListUtil.isNullOrEmpty(servers)) {
					for (Iterator<Element> srvIter = servers.iterator(); srvIter
							.hasNext();) {
						Element server = srvIter.next();
						list.add(node.getAttributeValue(attr) + ">"
								+ server.getAttributeValue(attr));
					}
				}
			}
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, List<Element>> get(Element rootEle,
			String parsePath, String attr, String childName) {
		String[] paths = parsePath.split(">");
		Element currEle = rootEle;
		Map<String, List<Element>> tmp = new HashMap<String, List<Element>>();
		String instPath = "";
		for (int i = 0; i < paths.length; i++) {
			// 预防没有下个节点时,出现空指针错误
			if (currEle == null) {
				return new HashMap<String, List<Element>>();
			}

			String[] nodes = paths[i].split(":");
			if (nodes.length == 1) {
				// 支持模糊匹配
				if (nodes[0].indexOf("*") != -1) {
					String nodeName = nodes[0].substring(1);
					List<Element> children = currEle.getChildren();
					for (Iterator<Element> iter = children.iterator(); iter
							.hasNext();) {
						Element ele = iter.next();
						if (ele.getName().indexOf(nodeName) != -1
								|| nodeName.length() == 0) {
							tmp.put(instPath + ele.getName() + inst_hyphen,
									ele.getChildren());
						}
					}
				} else {
					instPath = instPath + nodes[0] + inst_hyphen;
					currEle = currEle.getChild(nodes[0]);
				}
			} else {
				// 模糊匹配
				if (nodes[1].indexOf("*") != -1) {
					String nodeName = nodes[1].substring(1);
					List<Element> children = currEle.getChildren(nodes[0]);
					for (Iterator<Element> iter = children.iterator(); iter
							.hasNext();) {
						Element ele = iter.next();
						String attrValue = ele.getAttributeValue(attr);
						if (attrValue.indexOf(nodeName) != -1
								|| nodeName.length() == 0) {
							tmp.put(instPath + attrValue + inst_hyphen,
									ele.getChildren());
						}
					}
				} else {
					// 精确匹配
					instPath = instPath + nodes[1] + inst_hyphen;
					List<Element> children = currEle.getChildren(nodes[0]);
					for (Iterator<Element> iter = children.iterator(); iter
							.hasNext();) {
						Element child = iter.next();
						String attrValue = child.getAttributeValue(attr, "");
						if (attrValue.indexOf(nodes[1]) != -1) {
							currEle = child;
							break;
						}
					}
				}
			}
		}

		Map<String, List<Element>> elesMap = new HashMap<String, List<Element>>();
		if (!tmp.isEmpty()) {
			return tmp;
		} else if (StringUtil.isNullOrBlank(childName)) {
			try {
				elesMap.put(instPath, currEle.getChildren());
			} catch (NullPointerException ee) {
			}
			return elesMap;
		} else {
			elesMap.put(instPath, currEle.getChildren(childName));
			return elesMap;
		}
	}

	public static WASMonitorResult getMonitorResult(Type type,
			Map<String, Element> insts, String instKey) {
		Element ele = insts.get(instKey);
		if (ele == null) {
			return null;
		}

		WASMonitorResult result = new WASMonitorResult();
		result.setInstKey(instKey);
		Map<String, Item> items = type.getItems();
		for (Iterator<String> iterCode = items.keySet().iterator(); iterCode
				.hasNext();) {
			String itemCode = iterCode.next();
			result.addPerfItem(
					itemCode,
					getPerfItemValue(ele, items.get(itemCode),
							type.getItemAttr()));
		}
		return result;
	}

	public static Map<String, WASMonitorResult> getAllMonitorResults(Type type,
			Map<String, Element> insts) throws Exception {
		Map<String, WASMonitorResult> results = new HashMap<String, WASMonitorResult>();
		Map<String, Item> items = type.getItems();
		for (Iterator<String> iter = insts.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			WASMonitorResult result = new WASMonitorResult();
			result.setInstKey(key);
			Element inst = insts.get(key);
			for (Iterator<String> iterCode = items.keySet().iterator(); iterCode
					.hasNext();) {
				try {
					String itemCode = iterCode.next();
					Item item = items.get(itemCode);
					result.addPerfItem(itemCode,
							getPerfItemValue(inst, item, type.getItemAttr()));
				} catch (Throwable e) {
					continue;
				}
			}
			results.put(result.getInstKey(), result);
		}
		execBsfScript(results, type);
		doConvert(results, type);
		return results;
	}

	@SuppressWarnings("unchecked")
	private static double getPerfItemValue(Element ele, Item item,
			String itemArr) {
		String[] eleIds = item.getEleId().split(">");
		Element currEle = ele;
		for (int i = 0; i < eleIds.length - 1; i++) {
			String[] tmp = eleIds[i].split(":");
			if (tmp.length == 1) {
				currEle = currEle.getChild(eleIds[i]);
			} else {
				List<Element> eles = currEle.getChildren(tmp[0]);
				for (Iterator<Element> iter = eles.iterator(); iter.hasNext();) {
					Element tmpEle = iter.next();
					if (tmpEle.getAttributeValue(itemArr, "").endsWith(tmp[1])) {
						currEle = tmpEle;
						break;
					}
				}
			}
			if (currEle == null) {
				// 为空，表明不是我们要的节点
				throw new RuntimeException("");
			}
		}
		List<Element> children = currEle.getChildren(eleIds[eleIds.length - 1]);
		for (Iterator<Element> iter = children.iterator(); iter.hasNext();) {
			Element itemEle = iter.next();
			String tmp = itemEle.getAttributeValue(itemArr, "");
			if (item.getAttr().equalsIgnoreCase(tmp)) {
				return new Double(itemEle.getAttributeValue(item.getValue(),
						"-1")).doubleValue();
			}
		}
		// 为空，表明不是我们要的节点
		throw new RuntimeException("");
	}

	// ============================================下面两个方法对返回结果预处理======================================================//

	private static void execBsfScript(Map<String, WASMonitorResult> results,
			Type type) throws Exception {
		for (Iterator<WASMonitorResult> iter = results.values().iterator(); iter
				.hasNext();) {
			WASMonitorResult result = iter.next();
			Map<String, Double> params = null;
			Map<String, Double> perfsMap = result.getPerfs();
			for (Iterator<String> valIter = perfsMap.keySet().iterator(); valIter
					.hasNext();) {
				String key = valIter.next();
				Item item = type.getItem(key);
				if (StringUtil.isNullOrBlank(item.getBsh())) {
					continue;
				}
				if (params == null) {
					params = getParams(perfsMap, type);
				}
				double d = execBsh(item.getBsh(), params);
				perfsMap.put(key, new Double(d));
			}
		}
	}

	private static Map<String, Double> getParams(Map<String, Double> perfsMap,
			Type type) {
		Map<String, Double> params = new HashMap<String, Double>();
		Map<String, Item> items = type.getItems();
		for (Iterator<String> iter = perfsMap.keySet().iterator(); iter
				.hasNext();) {
			String key = iter.next();
			Item item = items.get(key);
			params.put(item.getId(), perfsMap.get(key));
		}
		return params;
	}

	private static double execBsh(String bsh, Map<String, Double> params)
			throws Exception {
		ScriptManager sm = ScriptManager.getInstance("beanshell");
		putParams(sm, params);
		try {
			return (Double) sm.eval(bsh);
		} catch (Exception e) {
			throw new Exception("执行脚本" + bsh + "失败。", e);
		}
	}

	private static void putParams(ScriptManager sm, Map<String, Double> params) {
		for (Iterator<String> iter = params.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			sm.put(key.trim(), params.get(key));
		}
	}

	public static void doConvert(Map<String, WASMonitorResult> results,
			Type type) {
		for (Iterator<WASMonitorResult> iter = results.values().iterator(); iter
				.hasNext();) {
			WASMonitorResult result = iter.next();
			Map<String, Double> perfsMap = result.getPerfs();
			for (Iterator<String> valIter = perfsMap.keySet().iterator(); valIter
					.hasNext();) {
				String key = valIter.next();
				Item item = type.getItem(key);
				double d = ConvertUtil.doConvert(item.getConvertors(),
						perfsMap.get(key));
				perfsMap.put(key, new Double(d));
			}
		}
	}

	public static boolean verifyServerCerPath(String serverCerPath,
			boolean isSave) {
		// 检查路径是否为空
		/*
		 * if (serverCerPath.trim().length() == 0) { return false; }
		 */
		// 检查路径是否正确
		File sourseFile = new File(serverCerPath);
		if (!sourseFile.exists()) {
			if (!Boolean.getBoolean("monitor.runinprobe")) {
				JOptionPane.showMessageDialog(new Frame(), "签署者证书文件路径错误.",
						"错误", JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			String sourseFilePath = sourseFile.getPath();
			if(!sourseFilePath.startsWith(WorkPathUtil.getRootPath())){
				sourseFilePath = WorkPathUtil.getRootPath() + sourseFilePath;
			}
			throw new RuntimeException("签署者证书文件路径错误" + sourseFilePath);
		}
		return true;
	}

	public static boolean verifyClientKeyPath(String clientKeyPath,
			boolean isSave) {
		// 检查路径是否为空
		/*
		 * if (clientKeyPath.trim().length() == 0) { return false; }
		 */
		// 检查路径是否正确
		File sourseFile = new File(clientKeyPath);
		if (!sourseFile.exists()) {
			if (!Boolean.getBoolean("monitor.runinprobe")) {
				JOptionPane.showMessageDialog(new Frame(), "个人证书文件路径错误.", "错误",
						JOptionPane.INFORMATION_MESSAGE);
				return false;
			}
			throw new RuntimeException("个人证书文件路径错误.");

		}
		// 检查是否有同名证书存在
		if (!Boolean.getBoolean("monitor.runinprobe")) {
			// 20110817修改在probe端运行时
			String targetPath = WASParamPanel.CERPATH + sourseFile.getName();
			File targetFile = new File(targetPath);
			if (targetFile.exists()
					&& isSave == true
					&& !(clientKeyPath == targetPath || clientKeyPath
							.equals(targetPath))
					&& !(clientKeyPath == WASParamPanel.CERPATH || clientKeyPath
							.equals(WASParamPanel.CERPATH))) {
				int opt = JOptionPane.showConfirmDialog(new Frame(),
						"已经存在同名的个人证书，是否覆盖？", "提问？", JOptionPane.YES_NO_OPTION);
				if (opt != JOptionPane.YES_OPTION) {
					return false;
				}
			}
		}
		return true;
	}

	private static void importCer(String cerFilePath, String trustStoreName,
			String trustStorePwd, boolean isSave) throws IOException,
			InterruptedException {
		String fullCerFilePath = cerFilePath;
		String alias = createAlias4Cer(new File(fullCerFilePath));
		boolean containsAlias = false;
		try {
			KeyStore ks;
			ks = KeyStore.getInstance("jks");
			URL url = new File(trustStoreName).toURI().toURL();
			ks.load(url.openStream(), trustStorePwd.toCharArray());
			containsAlias = ks.containsAlias(alias);
			if (containsAlias) {
				logger.debug("" + fullCerFilePath + "在" + url + "已经存在.");
				return;
			}
		} catch (Exception e) {
			logger.error("", e);
			return;
		}

		String keytoolDir = System.getProperty("java.home").replaceAll("\\\\",
				"/")
				+ "/bin/keytool.exe";
		String[] importServerCer = new String[] { keytoolDir, "-import",
				"-noprompt", "-alias", alias, "-storetype", "jks", "-keystore",
				trustStoreName, "-file", cerFilePath, "-storepass",
				trustStorePwd };

		logger.info("导入Websphere的证书:"
				+ java.util.Arrays.deepToString(importServerCer));
		Process process = Runtime.getRuntime().exec(importServerCer);
		process.waitFor();
	}

	public static String copyFile(String scourePath) {
		try {
			File fileScoure = new File(scourePath);
			String targetPath = WASParamPanel.CERPATH + fileScoure.getName();
			if (!(scourePath == targetPath || scourePath.equals(targetPath))) {
				File fileTarget = new File(targetPath);
				FileInputStream fis = new FileInputStream(fileScoure);
				FileOutputStream fos = new FileOutputStream(fileTarget);
				byte[] bytes = new byte[1024];
				int i;
				while ((i = fis.read(bytes)) != -1) {
					fos.write(bytes, 0, i);
				}
				fis.close();
				fos.close();
			}
			return fileScoure.getName();
		} catch (Exception e) {
			return null;
		}
	}

	public static void registerHttps(Map<String, String> extras) {
		Map<String, String> tmp = processCert(extras);
		// 注册https协议
		try {
			BroadaSecureProtocolSocketFactory bspsf = new BroadaSecureProtocolSocketFactory(
					new URL("file:///" + tmp.get("clientKeyPath")),
					tmp.get("clientKeyPwd"), new URL("file:///"
							+ WASParamPanel.CERPATH
							+ WASParamPanel.TRUSTSTORE_NAME),
					WASParamPanel.TRUSTSTORE_PWD, Integer.parseInt(tmp
							.get("stroeState")), tmp.get("clientKeyType"));
			@SuppressWarnings("deprecation")
			Protocol httpsProtocol = new Protocol("https", bspsf, 443);
			Protocol.registerProtocol("https", httpsProtocol);
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	private static Map<String, String> processCert(Map<String, String> extras) {
		Map<String, String> ret = new HashMap<String, String>();
		try {
			String serverCerFilePath = null;
			String clientKeyPath = extras.get("client_key") == null ? ""
					: extras.get("client_key").trim().replaceAll("\\\\", "/");
			String clientKeyPwd = extras.get("client_key_pwd");

			if (null != extras.get("server_cer_file")) {
				serverCerFilePath = extras.get("server_cer_file").trim()
						.replaceAll("\\\\", "/");
			}
			int stroeState = 0;// null
			if (null != serverCerFilePath
					&& new File(serverCerFilePath).isFile()) {
				stroeState = 1;
			}
			if (null != clientKeyPath && new File(clientKeyPath).isFile()) {
				stroeState = 2;
			}
			if ((null != serverCerFilePath && new File(serverCerFilePath)
					.isFile())
					&& (null != clientKeyPath && new File(clientKeyPath)
							.isFile())) {
				stroeState = 3;
			}
			String[] clientKeyPaths = clientKeyPath.split("\\.");
			String clientKeyType = clientKeyPaths[clientKeyPaths.length - 1];
			
			String rootPath = new File(System.getProperty("user.dir", ".")).getPath();
			String serverCerPath = new File(serverCerFilePath).getPath();
			if(!serverCerPath.startsWith(WorkPathUtil.getRootPath())){
				serverCerPath = WorkPathUtil.getRootPath() + serverCerPath;
			}
			clientKeyPath = rootPath.replaceAll("\\\\", "/") + "/" + clientKeyPath;
			if (!new File(WASParamPanel.CERPATH + WASParamPanel.TRUSTSTORE_NAME)
					.getPath().equals(serverCerPath)) {
				importServerCer2Trusted(serverCerFilePath);
			}

			ret.put("serverCerFilePath", serverCerFilePath);
			ret.put("serverCerPath", serverCerFilePath);
			ret.put("clientKeyPath", clientKeyPath);
			ret.put("clientKeyPwd", clientKeyPwd);
			ret.put("clientKeyType", clientKeyType);
			ret.put("stroeState", String.valueOf(stroeState));
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
		return ret;
	}

	private static String createAlias4Cer(File cer) {
		String ret = null;
		try {
			FileInputStream fis = new FileInputStream(cer);
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			fis.close();
			java.security.MessageDigest md5 = java.security.MessageDigest
					.getInstance("MD5");
			md5.update(buffer);
			byte[] digest = md5.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < digest.length; i++) {
				sb.append(Integer.toString(digest[i], 16));
			}
			ret = sb.toString();
		} catch (Exception e) {
			logger.error("", e);
			ret = cer.getPath();
			if(!ret.startsWith(WorkPathUtil.getRootPath())){
				ret = WorkPathUtil.getRootPath() + ret;
			}
		}

		return ret;
	}

	/**
	 * 该方法会被服务器远程调用
	 * 
	 * @param cerFilePath
	 *            证书相对user.dir的路径
	 * @return
	 */
	public static String importServerCer2Trusted(String cerFilePath) {
		String ret = null;
		try {

			importCer(cerFilePath, WASParamPanel.CERPATH
					+ WASParamPanel.TRUSTSTORE_NAME,
					WASParamPanel.TRUSTSTORE_PWD, true);
		} catch (Exception e) {
			logger.error("", e);
			ret = "false\u007f" + exception2String(e);
		}
		return ret;
	}

	public static String exception2String(Throwable ex) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bos);
		ex.printStackTrace(ps);
		ps.close();
		return new String(bos.toByteArray());
	}

}