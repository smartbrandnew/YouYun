package com.broada.carrier.monitor.impl.mw.jboss;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

public class JbossHttpGetUtil {
	public static GetMethod getMethod(String url, String username, String password) throws HttpException, IOException {

		HttpClient client = new HttpClient();
		client.getParams().setSoTimeout(30000);
		GetMethod method = new GetMethod(url);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		client.getState().setCredentials(AuthScope.ANY, credentials);
		client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
		method.setDoAuthentication(true);
		client.executeMethod(method);

		return method;

	}
}
