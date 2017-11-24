package com.broada.carrier.monitor.common.restful;

import java.util.Vector;

/**
 * HTTP响应对象
 */
public class HttpResponse {
	String urlString;
	int defaultPort;
	String file;
	String host;
	String path;
	int port;
	String protocol;
	String query;
	String ref;
	String userInfo;
	String contentEncoding;
	String content;
	String contentType;
	int code;
	String message;
	String method;
	int connectTimeout;
	int readTimeout;
	Vector<String> contentCollection;

	/**
	 * 正文内容
	 * @return
	 */
	public String getContent() {
		return content;
	}

	/**
	 * 正文类型，如application/json
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * HTTP响应码
	 * @return
	 */
	public int getCode() {
		return code;
	}

	/**
	 * HTTP响应消息
	 * @return
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * 正文内容集合
	 * @return
	 */
	public Vector<String> getContentCollection() {
		return contentCollection;
	}

	/**
	 * 正文字符集
	 * @return
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}

	/**
	 * 请求所使用的HTTP动作
	 * @return
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * 连接耗时
	 * @return
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * 读取耗时
	 * @return
	 */
	public int getReadTimeout() {
		return readTimeout;
	}

	/**
	 * Url
	 * @return
	 */
	public String getUrlString() {
		return urlString;
	}

	/**
	 * 连接端口
	 * @return
	 */
	public int getDefaultPort() {
		return defaultPort;
	}

	/**
	 * Url文件部份
	 * @return
	 */
	public String getFile() {
		return file;
	}

	/**
	 * Url主机部份
	 * @return
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Url路径部份
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Url端口
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Url协议
	 * @return
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Url查询部份
	 * @return
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Url引用部份
	 * @return
	 */
	public String getRef() {
		return ref;
	}

	/**
	 * Url用户信息
	 * @return
	 */
	public String getUserInfo() {
		return userInfo;
	}

	/**
	 * 判断请求返回是否为200
	 * @return
	 */
	public boolean isOk() {
		return code == 200;
	}

}