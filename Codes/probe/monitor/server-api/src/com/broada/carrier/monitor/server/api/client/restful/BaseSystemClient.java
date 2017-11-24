package com.broada.carrier.monitor.server.api.client.restful;

import java.util.Date;

import com.broada.carrier.monitor.common.util.Base64Util;
import com.broada.carrier.monitor.server.api.client.restful.entity.ExecuteMethodRequest;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;
import com.broada.carrier.monitor.server.api.service.BaseSystemService;

public class BaseSystemClient extends BaseServiceClient implements BaseSystemService {

	public BaseSystemClient(String baseServiceUrl, String apiPath) {
		super(baseServiceUrl, apiPath);
	}

	@Override
	public SystemInfo[] getInfos() {
		return client.get("infos", SystemInfo[].class);
	}

	@Override
	public Date getTime() {
		return client.get("time", Date.class);
	}

	@Override
	public String getProperty(String code) {
		return client.get("properties/" + Base64Util.encodeString(code), String.class);
	}

	@Override
	public Object executeMethod(String className, String method, Object... params) {
		String result = client.post("executeMethod", String.class, new ExecuteMethodRequest(className, method, params));
		return Base64Util.decodeObject(result);
	}
}
