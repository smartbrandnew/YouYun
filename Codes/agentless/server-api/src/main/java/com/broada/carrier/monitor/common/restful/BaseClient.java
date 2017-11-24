package com.broada.carrier.monitor.common.restful;

import java.net.SocketTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.error.BaseException;
import com.broada.carrier.monitor.common.error.CommTimeoutException;
import com.broada.carrier.monitor.common.error.CommunicationException;
import com.broada.carrier.monitor.common.error.ServiceException;
import com.broada.component.utils.error.ErrorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * RPC调用基础客户端
 */
public class BaseClient {
	private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);
	private static ObjectMapper om = new CustomObjectMapper();
	private String baseServiceUrl;
	private String serviceUrl;
	private String name;
	private Map<String, String> requestHeaders;

	public BaseClient(String serviceUrl) {
		this(serviceUrl, serviceUrl);		
	}
	
	/**
	 * 设置HTTP请求头信息
	 * @param code
	 * @param value
	 */
	public void setRequestHeader(String code, String value) {
		requestHeaders.put(code, value);
	}

	/**
	 * 连接服务地址与服务路径
	 * @param baseServiceUrl
	 * @param path
	 * @return
	 */
	public BaseClient(String baseServiceUrl, String path) {
		this.baseServiceUrl = baseServiceUrl.trim();		
		if (this.baseServiceUrl.isEmpty())
			throw new IllegalArgumentException("必须提供服务地址");
		this.serviceUrl = join(this.baseServiceUrl, path.trim());
		requestHeaders = new LinkedHashMap<String, String>();
		requestHeaders.put("Content-Type", "application/json");
		requestHeaders.put("Accept-Language", "zh-CN");
	}
	
	public BaseClient(String baseServiceUrl, String path, String name) {
		this(baseServiceUrl, path);
		this.name = name;
	}

	private static String join(String baseServiceUrl, String path) {
		if (path == null || path.isEmpty())
			return baseServiceUrl;
		if (baseServiceUrl.length() > 0) {
			if (baseServiceUrl.charAt(baseServiceUrl.length() - 1) != '/')
				baseServiceUrl += "/";
		}
		if (path.charAt(0) == '/')
			path = path.substring(1);
		return baseServiceUrl + path;
	}
	
	/**
	 * {@link #get(String, Class, Object...)}
	 * @param cls
	 * @param params
	 * @return
	 */
	public <T> T get(Class<T> cls, Object... params) {
		return get(null, cls, params);
	}

	/**
	 * <pre>
	 * 执行一个http get请求
	 * 如：
	 * BaseClient client = new BaseClient("http://ip:port/api/v1");
	 * // 无参数，单实体 
	 * People people = client.get("peoples/1", People.class);
	 * // 有参数，数组
	 * People[] peoples =  client.get("peoples", People[].class, "name", "a", "location", "hz");
	 * @param method 路径
	 * @param cls 返回的对象类型
	 * @param params 参数，总是偶数，形式为：参数1名称、参数1值、参数2名称、参数2值...
	 * @return 如果没有任何结果返回null，如果有结果，则返回具体对象
	 * @throws CommunicationException 如果通信失败
	 * @throws ServiceException 如果存在服务端错误
	 */
	public <T> T get(String method, Class<T> cls, Object... params) throws CommunicationException, ServiceException {
		StringBuilder url = new StringBuilder();
		url.append(join(getServiceUrl(), method));
		try {
			if (params != null && params.length > 0) {
				if (params.length % 2 != 0)
					throw new IllegalArgumentException("参数必须成对出现");
				url.append("?");
				for (int i = 0; i < params.length; i += 2) {
					if (i > 0)
						url.append("&");
					url.append(params[i].toString()).append("=").append(params[i + 1] == null ? "" : params[i + 1].toString());
				}
			}
			String urlStr = url.toString();
			if (logger.isDebugEnabled())
				logger.debug(String.format("HTTP GET开始[URL：%s]", url));
			HttpResponse resp = HttpRequest.getDefault().get(urlStr, requestHeaders);
			T result = readResult(urlStr, resp, cls);
			if (logger.isDebugEnabled())
				logger.debug(String.format("HTTP GET成功[URL：%s]", url));
			return result;
		} catch (Throwable e) {
			throw processError(e, "GET", url.toString(), null);
		}
	}
	
	/**
	 * {@link #post(String, Class, Object)}
	 * @param cls
	 * @param param
	 * @return
	 */
	public <T> T post(Class<T> cls, Object param) {
		return post(null, cls, param);
	}

	/**
	 * <pre>
	 * 执行一个http post请求
	 * @param method 路径
	 * @param cls 返回的对象类型
	 * @param param 提交的对象，如果是String类型，则直接将String作为post content
	 * @return 如果没有任何结果返回null，如果有结果，则返回具体对象
	 * @throws CommunicationException 如果通信失败
	 * @throws ServiceException 如果存在服务端错误
	 */	
	public <T> T post(String method, Class<T> cls, Object param) {
		String url = join(getServiceUrl(), method);
		String content = "";
		try {
			HttpResponse resp;
			if (logger.isDebugEnabled())
				logger.debug(String.format("HTTP POST开始[URL：%s]", url));
			if (param == null) {
				resp = HttpRequest.getDefault().post(url, requestHeaders);
			} else {
				if (param instanceof String)
					content = param.toString();
				else
					content = om.writeValueAsString(param);
				resp = HttpRequest.getDefault().post(url, content, requestHeaders);
			}
			T result = readResult(url, resp, cls);
			if (logger.isDebugEnabled())
				logger.debug(String.format("HTTP POST成功[URL：%s]，内容：%s%s", url, content.isEmpty() ? "" : "\n", content));
			return result;
		} catch (Throwable e) {
			throw processError(e, "POST", url, content);			
		}
	}	

	private BaseException processError(Throwable e, String action, String url, String content) {
		String msg = String.format("HTTP %s失败[URL：%s]", action, url);
		if (logger.isDebugEnabled()) {
			if (content == null)
				logger.debug(msg + "。错误：", e);
			else
				logger.debug(msg + "，内容：" + (content.isEmpty() ? "" : "\n") + content + "。错误：", e);
		}
		if (e instanceof BaseException)
			throw (BaseException) e;
		
		
		if (e instanceof SocketTimeoutException) {
			msg = String.format("访问%s[URL：%s]超时", name == null ? "服务" : name, url);
			throw new CommTimeoutException(ErrorUtil.createMessage(msg, e), e);
		} else {
			msg = String.format("无法连接%s[URL：%s]，请确定其是否在线", name == null ? "服务" : name, baseServiceUrl);
			throw new CommunicationException(ErrorUtil.createMessage(msg, e), e);
		}			
	}

	@SuppressWarnings("unchecked")
	private <T> T readResult(String url, HttpResponse resp, Class<T> cls) {
		String content = resp.getContent();
		
		logger.debug("HTTP结果解析[状态：{}]，内容：\n{}", resp.getCode(), content);
		
		if (resp.isOk()) {
			try {
				if (cls == null)
					return null;
				else if (content.isEmpty())
					return null;
				else if (cls.equals(String.class) && !content.startsWith("\"") && !content.startsWith("'")) {
					if (content.endsWith("\r\n"))
						content = content.substring(0, content.length() - 2);
					else if (content.endsWith("\n")) 
						content = content.substring(0, content.length() - 1);					
					return (T) content;
				} 
				return om.readValue(content, cls);
			} catch (Throwable e) {
				if (e instanceof BaseException)
					throw (BaseException) e;
				throw new CommunicationException(
						ErrorUtil.createMessage(String.format("反序列化请求结果失败[URL：%s 要求的类：%s 结果：%s]", url, cls, content), e), e);
			}			
		} else {			
			if (resp.getCode() >= 500 && content.contains("errCode")) {
				ErrorResponse er = null;
				try {
					er = om.readValue(content, ErrorResponse.class);				
				} catch (Throwable e) {
					ErrorUtil.warn(logger, "服务请求失败信息解析失败", e);
				}
				if (er != null)
					throw new ServiceException(er.getErrCode(), er.getMessage(), name, er.getRequest(), er.getStack());
			}
						
			String msg = String.format("服务请求失败[URL：%s 返回码：%s]，返回内容：\n%s", url, resp.getCode(), content);
			throw new CommunicationException(msg);
		}
	}

	/**
	 * 获取服务地址
	 * @return
	 */
	public String getServiceUrl() {
		return serviceUrl;
	}

	/**
	 * {@link #post(String, Class, Object)}
	 * @param method
	 */
	public void post(String method) {
		post(method, null, null);
	}

	/**
	 * {@link #post(String, Class, Object)}
	 * @param method
	 * @param cls
	 * @return
	 */
	public <T> T post(String method, Class<T> cls) {
		return post(method, cls, null);
	}
}
