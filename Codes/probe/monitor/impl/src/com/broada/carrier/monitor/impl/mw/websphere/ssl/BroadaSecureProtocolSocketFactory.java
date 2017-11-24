package com.broada.carrier.monitor.impl.mw.websphere.ssl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.websphere.WASParamPanel;

public class BroadaSecureProtocolSocketFactory implements SecureProtocolSocketFactory {
  
  private static final Log logger = LogFactory.getLog(SecureProtocolSocketFactory.class);

	private URL ksURL;
	private String ksPd;
	private URL tsURL;
	private String tsPwd;
	private String clientStroeType = "JKS";
	private SSLContext sslContext;

	/**
	 * 构造方法
	 */
	public BroadaSecureProtocolSocketFactory(
      final URL keystoreUrl, final String keystorePassword, 
      final URL truststoreUrl,	final String truststorePassword, 
      int stroeState, String clientStroeType) {
		this.ksURL = keystoreUrl;
		this.ksPd = keystorePassword;
		this.tsURL = truststoreUrl;
		this.tsPwd = truststorePassword;
    if(clientStroeType.toLowerCase() == "p12" || clientStroeType.toLowerCase().equals("p12") ||
    		clientStroeType.toLowerCase() == "pfx" || clientStroeType.toLowerCase().equals("pfx")) {
    	this.clientStroeType = "PKCS12";
    }
    if(ksPd==null) {
    	ksPd = "";
    }
	}

	/**
	 * SSLContext，此类的实例表示安全套接字协议的实现，它充当用于安全套接字工厂或 SSLEngine 的工厂
	 * @return
	 */
	private SSLContext getSSLContext() {
		if (this.sslContext == null) {
			this.sslContext = createSSLContext();
		}
		return this.sslContext;
	}
	
	/**
	 * 创建一个新的SSLContext
	 */
	private SSLContext createSSLContext() {
		KeyManager[] kms = null;
		TrustManager[] tms = null;
		try {
			//创建ks
			if (verifyKS(ksURL, ksPd)) {
				KeyStore keystore = createKeyStore(this.ksURL, this.ksPd, this.clientStroeType);
				kms = createKeyManagers(keystore, this.ksPd);
			}
			//创建ts
		  KeyStore keystore = createKeyStore(this.tsURL, this.tsPwd, "jks");
			tms = createTrustManagers(keystore);
			
			SSLContext sslcontext = SSLContext.getInstance("SSL");
			sslcontext.init(kms, tms, null);//kms - 身份验证密钥源或 null,tms - 同位体身份验证信任决策源或 null,random - 此生成器的随机源或 null 
			return sslcontext;
		} catch (Exception e) {
		  logger.error("",e);
			//e.printStackTrace();
		}
		return null;
	}

	/**
	 * 执行,4个重载方法
	 */
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
	}

	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException, UnknownHostException {
		return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
	}

	public Socket createSocket(final String host, final int port, final InetAddress localAddress, final int localPort, final HttpConnectionParams params) throws IOException, UnknownHostException, ConnectTimeoutException {
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null");
		}
		int timeout = params.getConnectionTimeout();
		if (timeout == 0) {
			return createSocket(host, port, localAddress, localPort);
		} else {
			return ControllerThreadSocketFactory.createSocket(this, host, port, localAddress, localPort, timeout);
		}
	}
	
	/**
	 * 创建一个新的ks
	 * @throws Exception 
	 */
	private static KeyStore createKeyStore(final URL url, final String password, String storeType) throws Exception {
		KeyStore ks;
		try {
			ks = KeyStore.getInstance(storeType);
			ks.load(url.openStream(), password != null ? password.toCharArray() : null);
		} catch (Exception e) {
		  logger.error("导入证书出错.storeType=" + storeType + ",e.getMessage" + e.getMessage());
			throw new Exception("导入证书出错.",e);
		}
		return ks;
	}

	/**
	 * 创建一个新的KeyManager 这是用于 JSSE 密钥管理器的基接口。 
	 * KeyManager 负责管理用于验证到同位体的本地 SSLSocket 的密钥内容。如果没有密钥内容可以使用，则套接字将不能提供身份验证凭据。
	 * 
	 */
	private static KeyManager[] createKeyManagers(final KeyStore keystore, final String password)	throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());//所请求算法的标准名称:SunX509
		kmfactory.init(keystore, password != null ? password.toCharArray() : null);
		KeyManager[] kms = kmfactory.getKeyManagers();
		return kms;
	}

	/**
	 * 创建一个新的TrustManager 这是用于 JSSE 信任管理器的基接口
	 * TrustManager负责管理做出信任决定时使用的的信任材料，也负责决定是否接受同位体提供的凭据。
	 * 
	 */
	private static TrustManager[] createTrustManagers(final KeyStore keystore) throws KeyStoreException, NoSuchAlgorithmException {
		TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());//SunX509
		tmfactory.init(keystore);
		TrustManager[] tms = tmfactory.getTrustManagers();
		for (int i = 0; i < tms.length; i++) {
			if (tms[i] instanceof BroadaX509TrustManager) {
				tms[i] = new BroadaX509TrustManager((BroadaX509TrustManager) tms[i]);
			}
		}
		return tms;
	}
	
	private boolean verifyKS(URL ksURL, String ksPwd) {
		String ksURLStr = ksURL.toString();
		if(ksURLStr == "file:/" || ksURLStr.equals("file:/")) {
			return false;
		}
		if(ksURLStr == "file:/"+WASParamPanel.CERPATH || ksURLStr.equals("file:/"+WASParamPanel.CERPATH)) {
			return false;
		}
		if(ksPwd == "" || ksPwd.equals("")) {
			return false;
		}
		File f = new File(ksURL.getFile());
		if(!f.exists() || !f.isFile() || !f.canRead()){
		  return false;
		}
		return true;
	}
}