package com.broada.carrier.monitor.impl.mw.websphere.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class BroadaX509TrustManager implements X509TrustManager {

	private BroadaX509TrustManager MyX509TrustManager = null;

	public BroadaX509TrustManager(final BroadaX509TrustManager MyX509TrustManager) {
		this.MyX509TrustManager = MyX509TrustManager;
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

	}

	public X509Certificate[] getAcceptedIssuers() {
		return this.MyX509TrustManager.getAcceptedIssuers();
	}
}