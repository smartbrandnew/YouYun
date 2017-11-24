package uyun.bat.datastore.util;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.config.Config;

public class HttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	private static CloseableHttpClient client;

	public static CloseableHttpClient getClient() {
		if (client == null) {
			synchronized (HttpUtil.class) {
				if (client == null) {
					Builder configBuilder = RequestConfig.custom();
					configBuilder.setConnectTimeout(20 * 1000);
					configBuilder.setSocketTimeout(30 * 1000);
					HttpClientBuilder clientBuilder = HttpClients.custom();
					clientBuilder.setDefaultRequestConfig(configBuilder.build());
					clientBuilder.setMaxConnPerRoute(300);
					clientBuilder.setMaxConnTotal(500);
					org.apache.http.config.SocketConfig.Builder configBuiler = SocketConfig.custom();
					configBuiler.setSoTimeout(15 * 1000);
					clientBuilder.setDefaultSocketConfig(configBuiler.build());
					client = clientBuilder.build();
				}
			}
		}
		return client;
	}

	public static String postJson(String url, String json) {
		HttpPost post = new HttpPost(url);
		HttpEntity httpEntity = new StringEntity(json, ContentType.create("application/json", "utf-8"));
		post.setEntity(httpEntity);
		try {
			CloseableHttpResponse response = getClient().execute(post);
			String str = EntityUtils.toString(response.getEntity());
			int code = response.getStatusLine().getStatusCode();
			response.close();
			if (code == 204 || code == 200) {
				return str;
			}
		} catch (ClientProtocolException e) {
			logger.warn("http handle exception：", e);
		} catch (IOException e) {
			logger.warn("IO Exception：", e);
		}
		return null;
	}

	public static String get(String url) {
		HttpGet get = new HttpGet(url);
		try {
			CloseableHttpResponse response = getClient().execute(get);
			String str = EntityUtils.toString(response.getEntity());
			int code = response.getStatusLine().getStatusCode();
			response.close();
			if (code == 204 || code == 200) {
				return str;
			}
		} catch (ClientProtocolException e) {
			logger.warn("http handle exception：", e);
		} catch (IOException e) {
			logger.warn("IO exception：", e);
		}
		return null;
	}
	
	public static void main(String[] args){
		String str=Config.getInstance().get("test.chinese", "");
		System.out.println(str);
	}
}
