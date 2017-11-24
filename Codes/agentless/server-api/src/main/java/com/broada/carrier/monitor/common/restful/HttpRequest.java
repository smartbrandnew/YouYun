package com.broada.carrier.monitor.common.restful;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Vector;

/**
 * HTTP请求工具类
 */
public class HttpRequest {
	public static final int CONNECT_TIMEOUT_DEFAULT = 5 * 1000;
	public static final int READ_TIMEOUT_DEFAINT = 8 * 60 * 1000;	
	private String defaultContentEncoding;	
	private static HttpRequest instance;
	private int connectTimeout;
	private int readTimeout;

	/**
	 * 获取默认实例
	 * @return
	 */
	public static HttpRequest getDefault() {
		if (instance == null) {
			synchronized (HttpRequest.class) {
				if (instance == null)
					instance = new HttpRequest();
			}
		}
		return instance;
	}

	public HttpRequest() {
		this.defaultContentEncoding = "UTF-8";
		this.connectTimeout = CONNECT_TIMEOUT_DEFAULT;
		this.readTimeout = READ_TIMEOUT_DEFAINT;		 
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpResponse get(String urlString) throws IOException {
		return this.send(urlString, "GET", null, null);
	}
	
	private Charset getCharset() {
		return Charset.forName(getDefaultContentEncoding());
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString URL地址
	 * @param params 参数集合
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpResponse get(String urlString, Map<String, String> propertys) throws IOException {
		return this.send(urlString, "GET", null, propertys);
	}

	/**
	 * 发送GET请求
	 * 
	 * @param urlString URL地址
	 * @param params 参数集合
	 * @param propertys 请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpResponse get(String urlString, Map<String, String> params, Map<String, String> propertys)
			throws IOException {
		return this.send(urlString, "GET", params, propertys);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString URL地址
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpResponse post(String urlString) throws IOException {
		return this.send(urlString, "POST", null, null);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString URL地址
	 * @param propertys 请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpResponse post(String urlString, Map<String, String> propertys) throws IOException {
		return this.send(urlString, "POST", null, propertys);
	}

	/**
	 * 发送POST请求
	 * 
	 * @param urlString URL地址
	 * @param params 参数集合
	 * @param propertys 请求属性
	 * @return 响应对象
	 * @throws IOException
	 */
	public HttpResponse post(String urlString, Map<String, String> params, Map<String, String> propertys)
			throws IOException {
		return this.send(urlString, "POST", params, propertys);
	}
	
	public HttpResponse post(String urlString, String content, Map<String, String> propertys)
			throws IOException {
		HttpURLConnection urlConnection = null;
		
		URL url = new URL(urlString);
		urlConnection = openConnection(url, "POST");
		
		for (String key : propertys.keySet()) 
			urlConnection.addRequestProperty(key, propertys.get(key));				

		urlConnection.getOutputStream().write(content.getBytes(getCharset()));
		urlConnection.getOutputStream().flush();
		urlConnection.getOutputStream().close();

		return this.makeContent(urlString, urlConnection);
	}

	private HttpURLConnection openConnection(URL url, String method) throws IOException {
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setConnectTimeout(connectTimeout);
		urlConnection.setReadTimeout(readTimeout);
		urlConnection.setRequestMethod(method);
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);			
		return urlConnection;
	}

	/**
	 * 发送HTTP请求
	 * 
	 * @param urlString
	 * @return 响映对象
	 * @throws IOException
	 */
	private HttpResponse send(String urlString, String method, Map<String, String> parameters,
			Map<String, String> propertys) throws IOException {
		HttpURLConnection urlConnection = null;

		if (method.equalsIgnoreCase("GET") && parameters != null) {
			StringBuffer param = new StringBuffer();
			int i = 0;
			for (String key : parameters.keySet()) {
				if (i == 0)
					param.append("?");
				else
					param.append("&");
				param.append(key).append("=").append(parameters.get(key));
				i++;
			}
			urlString += param;
		}
		URL url = new URL(urlString);
		urlConnection = openConnection(url, method);

		if (propertys != null)
			for (String key : propertys.keySet()) {
				urlConnection.addRequestProperty(key, propertys.get(key));
			}

		if (method.equalsIgnoreCase("POST") && parameters != null) {
			StringBuffer param = new StringBuffer();
			for (String key : parameters.keySet()) {
				param.append("&");
				param.append(key).append("=").append(parameters.get(key));
			}
			urlConnection.getOutputStream().write(param.toString().getBytes(getCharset()));
			urlConnection.getOutputStream().flush();
			urlConnection.getOutputStream().close();
		}

		return this.makeContent(urlString, urlConnection);
	}

	/**
	 * 得到响应对象
	 * 
	 * @param urlConnection
	 * @return 响应对象
	 * @throws IOException
	 */
	private HttpResponse makeContent(String urlString, HttpURLConnection urlConnection) throws IOException {
		HttpResponse httpResponser = new HttpResponse();
		try {
			String ecod = null;
			InputStream in;
			
			try {
				ecod = urlConnection.getContentEncoding();
				in = urlConnection.getInputStream();
			} catch (IOException e) {				
				in = urlConnection.getErrorStream();
				
				if (in == null)
					throw e;
			}
			if (ecod == null)
				ecod = getDefaultContentEncoding();			
			
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, Charset.forName(ecod)));
			httpResponser.contentCollection = new Vector<String>();
			StringBuilder temp = new StringBuilder();
			String line = bufferedReader.readLine();
			while (line != null) {
				httpResponser.contentCollection.add(line);
				temp.append(line).append("\r\n");
				line = bufferedReader.readLine();
			}
			bufferedReader.close();


			httpResponser.urlString = urlString;

			httpResponser.defaultPort = urlConnection.getURL().getDefaultPort();
			httpResponser.file = urlConnection.getURL().getFile();
			httpResponser.host = urlConnection.getURL().getHost();
			httpResponser.path = urlConnection.getURL().getPath();
			httpResponser.port = urlConnection.getURL().getPort();
			httpResponser.protocol = urlConnection.getURL().getProtocol();
			httpResponser.query = urlConnection.getURL().getQuery();
			httpResponser.ref = urlConnection.getURL().getRef();
			httpResponser.userInfo = urlConnection.getURL().getUserInfo();

			httpResponser.content = temp.toString();
			httpResponser.contentEncoding = ecod;
			httpResponser.code = urlConnection.getResponseCode();
			httpResponser.message = urlConnection.getResponseMessage();
			httpResponser.contentType = urlConnection.getContentType();
			httpResponser.method = urlConnection.getRequestMethod();
			httpResponser.connectTimeout = urlConnection.getConnectTimeout();
			httpResponser.readTimeout = urlConnection.getReadTimeout();

			return httpResponser;
		} catch (IOException e) {
			throw e;
		} finally {
			if (urlConnection != null)
				urlConnection.disconnect();
		}
	}

	/**
	 * 默认的响应字符集
	 */
	public String getDefaultContentEncoding() {
		return this.defaultContentEncoding;
	}

	/**
	 * 设置默认的响应字符集
	 */
	public void setDefaultContentEncoding(String defaultContentEncoding) {
		this.defaultContentEncoding = defaultContentEncoding;
	}
}
