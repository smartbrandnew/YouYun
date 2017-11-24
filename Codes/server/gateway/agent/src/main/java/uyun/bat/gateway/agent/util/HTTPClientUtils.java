package uyun.bat.gateway.agent.util;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

public class HTTPClientUtils {

	private static HttpClient client = null;
	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(128);
		cm.setDefaultMaxPerRoute(128);
		client = HttpClients.custom().setConnectionManager(cm)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
	}

	/**
	 * 发送一个 Post 请求, 使用指定的字符集编码.
	 *
	 * @param url
	 * @param body RequestBody
	 * @return ResponseBody, 使用指定的字符集编码.
	 *
	 * @throws ConnectTimeoutException 建立链接超时异常
	 * @throws SocketTimeoutException 响应超时
	 * @throws Exception
	 */
	public static String post(String url, String body) throws Exception {
		return post(url, null, body, null, null, 6 * 1000, 10 * 1000);
	}

	public static String post(String url, Map<String, String> headers, String body) throws Exception {
		return post(url, headers, body, null, null, 6 * 1000, 10 * 1000);
	}

	/**
	 * 发送一个 Post 请求, 使用指定的字符集编码.
	 *
	 * @param url
	 * @param headers
	 * @param body RequestBody
	 * @param mimeType 例如 application/xml, 默认: text/plain
	 * @param charset 编码
	 * @param connTimeout 建立链接超时时间,毫秒.
	 * @param readTimeout 响应超时时间,毫秒.
	 * @return ResponseBody, 使用指定的字符集编码.
	 *
	 * @throws ConnectTimeoutException 建立链接超时异常
	 * @throws SocketTimeoutException 响应超时
	 * @throws Exception
	 */
	public static String post(String url, Map<String, String> headers, String body, String mimeType, String charset,
			Integer connTimeout, Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {
		HttpClient client = null;
		HttpPost post = new HttpPost(url);
		String result = "";

		if (StringUtils.isBlank(mimeType)) {
			mimeType = "application/json";
		}
		if (StringUtils.isBlank(charset)) {
			charset = "utf-8";
		}

		try {
			if (StringUtils.isNotBlank(body)) {

				HttpEntity entity = new StringEntity(body, ContentType.create(mimeType, charset));
				post.setEntity(entity);
			}
			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, String> entry : headers.entrySet()) {
					post.addHeader(entry.getKey(), entry.getValue());
				}
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());

			HttpResponse res;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HTTPClientUtils.client;
				res = client.execute(post);
			}
			if (null != res.getEntity()) {
				result = IOUtils.toString(res.getEntity().getContent(), charset);
			}
			int code = res.getStatusLine().getStatusCode();
			if (code == 204 || code == 200) {
				// return result;
				return code + "";
			}
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}

	/**
	 * 提交form表单
	 *
	 * @param url
	 * @param params
	 * @param headers
	 * @param connTimeout
	 * @param readTimeout
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public static String postForm(String url, Map<String, String> params, Map<String, String> headers,
			Integer connTimeout, Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {

		return postForm(url, params, headers, null, connTimeout, readTimeout);
	}

	/**
	 * 提交form表单，可以设置charset，如果charset为null，默认UTF-8
	 *
	 * @param url
	 * @param params
	 * @param headers
	 * @param charset
	 * @param connTimeout
	 * @param readTimeout
	 * @return
	 * @throws ConnectTimeoutException
	 * @throws SocketTimeoutException
	 * @throws Exception
	 */
	public static String postForm(String url, Map<String, String> params, Map<String, String> headers, String charset,
			Integer connTimeout, Integer readTimeout) throws ConnectTimeoutException, SocketTimeoutException, Exception {

		HttpClient client = null;

		HttpPost post = new HttpPost(url);
		try {
			if (params != null && !params.isEmpty()) {
				List<NameValuePair> formParams = new ArrayList<org.apache.http.NameValuePair>();
				Set<Entry<String, String>> entrySet = params.entrySet();
				for (Entry<String, String> entry : entrySet) {
					formParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				UrlEncodedFormEntity entity = null;
				if (StringUtils.isNotBlank(charset)) {
					entity = new UrlEncodedFormEntity(formParams, Charset.forName(charset.toUpperCase()));
				} else {
					entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
				}
				post.setEntity(entity);
			}
			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, String> entry : headers.entrySet()) {
					post.addHeader(entry.getKey(), entry.getValue());
				}
			}
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			post.setConfig(customReqConf.build());
			HttpResponse res = null;
			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(post);
			} else {
				// 执行 Http 请求.
				client = HTTPClientUtils.client;
				res = client.execute(post);
			}
			HttpEntity resEntity = res.getEntity();
			if (resEntity != null) {
				if (StringUtils.isNotBlank(charset)) {
					return IOUtils.toString(res.getEntity().getContent(), charset.toUpperCase());
				} else {
					return IOUtils.toString(res.getEntity().getContent(), "UTF-8");
				}
			} else {
				return null;
			}
		} finally {
			post.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
	}

	/**
	 * 发送一个 GET 请求
	 *
	 * @param url
	 * @param charset
	 * @return
	 * @throws Exception
	 */
	public static String get(String url, String charset) throws Exception {
		return get(url, charset, null, null);
	}

	/**
	 * 发送一个 GET 请求
	 *
	 * @param url
	 * @param charset
	 * @param connTimeout 建立链接超时时间,毫秒.
	 * @param readTimeout 响应超时时间,毫秒.
	 * @return
	 * @throws ConnectTimeoutException 建立链接超时
	 * @throws SocketTimeoutException 响应超时
	 * @throws Exception
	 */
	public static String get(String url, String charset, Integer connTimeout, Integer readTimeout)
			throws ConnectTimeoutException, SocketTimeoutException, Exception {
		HttpClient client = null;

		HttpGet get = new HttpGet(url);
		String result = "";
		try {
			// 设置参数
			Builder customReqConf = RequestConfig.custom();
			if (connTimeout != null) {
				customReqConf.setConnectTimeout(connTimeout);
			}
			if (readTimeout != null) {
				customReqConf.setSocketTimeout(readTimeout);
			}
			get.setConfig(customReqConf.build());

			HttpResponse res = null;

			if (url.startsWith("https")) {
				// 执行 Https 请求.
				client = createSSLInsecureClient();
				res = client.execute(get);
			} else {
				// 执行 Http 请求.
				client = HTTPClientUtils.client;
				res = client.execute(get);
			}

			result = IOUtils.toString(res.getEntity().getContent(), charset);
		} finally {
			get.releaseConnection();
			if (url.startsWith("https") && client != null && client instanceof CloseableHttpClient) {
				((CloseableHttpClient) client).close();
			}
		}
		return result;
	}

	/**
	 * 从 response 里获取 charset
	 *
	 * @param ressponse
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String getCharsetFromResponse(HttpResponse ressponse) {
		// Content-Type:text/html; charset=GBK
		if (ressponse.getEntity() != null && ressponse.getEntity().getContentType() != null
				&& ressponse.getEntity().getContentType().getValue() != null) {
			String contentType = ressponse.getEntity().getContentType().getValue();
			if (contentType.contains("charset=")) {
				return contentType.substring(contentType.indexOf("charset=") + 8);
			}
		}
		return null;
	}

	public static CloseableHttpClient createSSLInsecureClient() throws GeneralSecurityException {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {

				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}

				@Override
				public void verify(String host, SSLSocket ssl) throws IOException {
				}

				@Override
				public void verify(String host, X509Certificate cert) throws SSLException {
				}

				@Override
				public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
				}

			});
			return HttpClients.custom().setSSLSocketFactory(sslsf)
					.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
		} catch (GeneralSecurityException e) {
			throw e;
		}
	}

	public static void main(String[] args) {
		try {
			Map<String, String> header = new HashMap<String, String>();
			//header.put("Content-Type", "application/json");
			//header.put("Cache-Control", "no-cache");
			// header.put("MOBILE_DEVICE", "ANDROID_PHONE");
			header.put("sysCode", "Monitor");
			header.put("apikey", "43e638f0f96643bc83c9620d12a07037");
			// trade.tongbanjie.com
			// 192.168.1.109:8101
			String json = "{\"classCode\":\"Y-SERVER\",\"Y_ip\":\"unknown\",\"source\":{\"uid\":\"30b70f62ac8a182264b138ce565e82c2\",\"code\":\"monitor\",\"state\":\"未监控\"},\"Y_name\":\"VOOHMVDB-unknown\"}";
			// json
			// ="[{\"id\":\"0034bd3fec294feea5d7ce89430864f4\",\"timestamp\":1467945684000,\"state\": \"OnlineState\", \"value\":\"offline\",\"tags\":[\"location:a\",\"level:high\"]}]";
			json = "{\"classCode\":\"Y_Router\", \"Y_name\":\"git.server_238\", \"Y_ip\":\"10.1.2.238\", \"source\":{ \"code\":\"monitor\", \"uid\":\"e0a67e986a594a61b3d1e523a0a39c77\", \"state\":\"已监控\" } }";
			String string = post("https://cmdb.uyuntest.cn/openapi/v2/repo/cis", header, json, null, "utf-8", 10000, 10000);
			System.out.println(string);
			System.out.println(string.length()); // 2178129

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
