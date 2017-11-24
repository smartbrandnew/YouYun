package com.broada.carrier.monitor.impl.virtual.operationcenter.http;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class HostnameVerifierUtil implements HostnameVerifier
{
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verify(final String arg0, final SSLSession arg1) {
        return true;
    }
}
