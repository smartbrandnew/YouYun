package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.virtual.operationcenter.config.NodeConfig;

public class HttpsClient extends AbstractHttpClient {
	/**
	 * HTTP_CODE_SSL_FAILED, 通信ssl失败码
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsClient.class);

	private static final int HTTP_CODE_SSL_FAILED = -2;

	private NodeConfig config;

	public void SetNodeConf(NodeConfig config){
		this.config = config;
	}

	/**
	 * 发送请求
	 * @param sendUrl 发送地址
	 * @param entity 请求实体
	 * @return 请求响应
	 */
	private ResponseEntity send(String sendUrl, String content, RequestEntity entity){
		ResponseEntity resEntity = null;
		resEntity = sendHttpsRequest(sendUrl, content, entity);
		if (HTTP_CODE_SSL_FAILED == resEntity.getHttpCode()){
			HttpsNode node = entity.getNode();
			removeKeyStore(node);
			resEntity = sendHttpsRequest(sendUrl, content, entity);
		}
		return resEntity;
	}

	private void removeKeyStore(HttpsNode node){
		if (null == node){
			return;
		}
	}

	private ResponseEntity sendHttpsRequest(String sendUrl, String content, RequestEntity entity){
		ResponseEntity resEntity = new ResponseEntity();
		String method = entity.getReqType().toString();
		Map<String, String> header = entity.getHeader();
		HttpsNode node = entity.getNode();

		String response = null;
		String retCode = null;
		int httpCode = -1;
		Map<String, String> resHeader = null;

		HttpsURLConnection conn = null;
		try {
			SSLContext sslContext = setHttpsURLConnSSL(node);
			URL url = new URL(sendUrl);

			conn = (HttpsURLConnection)url.openConnection();
			conn.disconnect();
			conn.setRequestMethod(method);
			conn.setHostnameVerifier(new HostnameVerifierUtil());
			if (null != sslContext){
				conn.setSSLSocketFactory(sslContext.getSocketFactory());
			}
			setHeader(header, conn);
			setConnection(conn);
			setContent(RequestType.valueOf(method), content, conn);

			httpCode = conn.getResponseCode();

			if (String.valueOf(httpCode).startsWith("2")){
				retCode = RetCode.SUCCESS.toString();
				String charset = this.getCharset(header);
				response = this.getResponseContent(conn, charset);
				resHeader = this.getResponseHeader(conn);
			} else{
				retCode = RetCode.ERROR.toString();
			}
			LOGGER.info("retCode is {} ", retCode);
			LOGGER.info("httpCode is {} ", httpCode);
		} catch (SSLHandshakeException ssle){
			LOGGER.error("url is {} ", sendUrl);
			LOGGER.error(
					"sendMassage() occur Exception during https connect or https's send, please check your certificate is {} ",
					ssle);
			retCode = RetCode.ERROR.toString();
			httpCode = HTTP_CODE_SSL_FAILED;
		} catch (MalformedURLException urle){
			LOGGER.error("sendMassage() occur Exception url is not a illegal url, please check your template");
			entity.setUrl(" url is illegal, please check your template!");
			retCode = RetCode.ERROR.toString();
		} catch (IllegalArgumentException illegale){
			LOGGER.error("IllegalArgumentException, maybe illegal character(s), please check your request parms!");
			retCode = RetCode.ERROR.toString();
		} catch (Exception e){
			LOGGER.error(
					"sendMassage() occur Exception during https connect or https's send, please check your ip port username and so on!");
			LOGGER.error("Exception");
			retCode = RetCode.ERROR.toString();
		} finally{
			if (null != conn){
				try{
					InputStream connError = conn.getErrorStream();
					LOGGER.info("conn error stream is {} ", input2String(connError));
					if (connError != null){
						connError.close();
					}
					conn.disconnect();
				} catch (IOException e2){
					LOGGER.error("io Exception");
				}
			}
		}

		resEntity.setRetCode(retCode);
		resEntity.setHeader(resHeader);
		resEntity.setHttpCode(httpCode);
		resEntity.setContent(response);
		return resEntity;
	}

	private SSLContext setHttpsURLConnSSL(HttpsNode node)
	throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException{
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		KeyStore ks = KeyStore.getInstance("JKS");
		KeyStore tks = KeyStore.getInstance("JKS");
		try{
			if (config.getCertificate().containsKey("keyStoreName")
					&& config.getCertificate().containsKey("keyStorePwd")){
				ks.load(new FileInputStream(config.getCertificate().get("keyStoreName")),
						config.getCertificate().get("keyStorePwd").toCharArray());
				kmf.init(ks, config.getCertificate().get("keyStorePwd").toCharArray());
				tks.load(new FileInputStream(config.getCertificate().get("trustStoreName")),
						config.getCertificate().get("trustStorePwd").toCharArray());
				tmf.init(tks);
				sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			} else {
				tks.load(new FileInputStream(config.getCertificate().get("trustStoreName")),
						config.getCertificate().get("trustStorePwd").toCharArray());
				tmf.init(tks);
				sslContext.init(null, tmf.getTrustManagers(), null);
			}

		}catch (Exception e){
			LOGGER.error("exception");
		}
		return sslContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity put(RequestEntity entity){
		LOGGER.error("https put is enter.");
		String content = null;
		if (null != entity.getNameValuePairs()){
			content = this.convertNameValuePair(entity.getNameValuePairs());
		} else{
			content = entity.getContent();
		}
		return send(entity.getUrl(), content, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity delete(RequestEntity entity){
		LOGGER.error("https delete is enter.");
		String url = entity.getUrl();
		if (null != entity.getNameValuePairs()){
			String param = this.convertNameValuePair(entity.getNameValuePairs());
			if (null != param){
				if (-1 == url.indexOf("?")){
					url = url + "?" + param;
				} else{
					url = url + "&" + param;
				}
			}
		}
		return send(url, entity.getContent(), entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity post(RequestEntity entity){
		LOGGER.error("https post is enter.");
		String content = null;
		if (null != entity.getNameValuePairs()){
			content = this.convertNameValuePair(entity.getNameValuePairs());
		} else{
			content = entity.getContent();
		}
		return send(entity.getUrl(), content, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ResponseEntity get(RequestEntity entity){
		LOGGER.error("https get is enter.");
		String url = entity.getUrl();
		if (null != entity.getNameValuePairs()){
			String param = this.convertNameValuePair(entity.getNameValuePairs());
			if (null != param){
				url += param;
			}
		}
		return send(url, null, entity);
	}

	private String input2String(InputStream input){
		if (null == input){
			return null;
		}
		String str = null;
		BufferedReader reader;
		try{
			reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
			StringBuffer sb = new StringBuffer();

			while ((str = reader.readLine()) != null){
				sb.append(str).append("\n");
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e){
			LOGGER.error("UnsupportedEncodingException " + e.getLocalizedMessage());
		} catch (IOException e){
			LOGGER.error("IOException " + e.getLocalizedMessage());
		}
		return null;
	}
	
}
