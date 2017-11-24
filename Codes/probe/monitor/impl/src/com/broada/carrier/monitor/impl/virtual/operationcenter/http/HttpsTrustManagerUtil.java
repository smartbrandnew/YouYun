package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsTrustManagerUtil implements X509TrustManager {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsTrustManagerUtil.class);
    
    private X509TrustManager sunJSSEX509TrustManager = null;
    
    /**
     * Constructor
     * @param keyStore keyStore路径
     * @throws KeyStoreException KeyStoreException
     */
    public HttpsTrustManagerUtil(KeyStore keyStore)
        throws KeyStoreException {
        if (null == keyStore){
            LOGGER.info("keyStore is null");
            sunJSSEX509TrustManager = null;
            return;
        }
        
        FileInputStream fileInputStream = null;
        try{
            //创建默认的x509TrustManager       
            TrustManagerFactory trustManagerFactory;
            trustManagerFactory = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
            trustManagerFactory.init(keyStore);
            
            //获得所有的
            TrustManager tms[] = trustManagerFactory.getTrustManagers();
            for (int i = 0; i < tms.length; i++){
                if (tms[i] instanceof X509TrustManager){
                    sunJSSEX509TrustManager = (X509TrustManager)tms[i];
                    return;
                }
            }
        } catch (KeyStoreException e){
            LOGGER.error("get KeyStoreException!", e);
        } catch (NoSuchAlgorithmException e){
            LOGGER.error("get NoSuchAlgorithmException!", e);
        } catch (NoSuchProviderException e){
            LOGGER.error("get NoSuchProviderException!", e);
        } finally {
            if (null != fileInputStream){
                try{
                    fileInputStream.close();
                } catch (IOException e){
                    LOGGER.error("get IOException!", e);
                }
            }
        }
        
    }
    
    /**
     * check client
     * @param chain chain
     * @param authType auth type
     * @throws CertificateException  CertificateException
     * @date 2013-3-21
     * @version [BMS V100R002C02]
     */
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
        if (null == sunJSSEX509TrustManager) {
            return;
        } else {
            sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
        }
    }
    
    /**
     * check server
     * @param chain chain
     * @param authType auth type
     * @throws CertificateException  CertificateException
     * @date 2013-3-21
     * @version [BMS V100R002C02]
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
        if (null == sunJSSEX509TrustManager){
            return;
        } else {
            sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
        }
    }
    
    /**
     * X509Certificate
     * @return  X509Certificate
     * @date 2013-3-21
     * @version [BMS V100R002C02]
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        if (null == sunJSSEX509TrustManager){
            X509Certificate[] array = new X509Certificate[1];
            return array;
        } else {
            return sunJSSEX509TrustManager.getAcceptedIssuers();
        }
    }
    
}
