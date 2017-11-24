package com.broada.carrier.monitor.impl.virtual.operationcenter.util;

import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.broada.carrier.monitor.impl.virtual.operationcenter.config.NodeConfig;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.HttpsNode;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.RequestType;
import com.broada.carrier.monitor.method.operationcenter.OperationCenterMethodOption;

public class OCManager {
	
	/**
	 * 登录系统，获取token
	 * @param option
	 * @return
	 */
	public static String getToken(OperationCenterMethodOption option, HttpsNode node, NodeConfig conf){
		String userId = option.getUsername();
		String password = option.getPassword();
		String localIp = option.getHostIp();
		Object tokenData = HttpsClientUtil.sendRequest(node, conf, "/oc/v2.3/tokens",RequestType.POST,
				"{\"user_id\":\"" + userId + "\",\"value\":\"" + password + "\",\"host_ip\":\"" + localIp + "\"}");
		if (tokenData instanceof String){
			JSONObject json = JSONObject.fromObject(tokenData);
			String tokenId = json.getString("data");
			return tokenId;
		}
		else
			return null;
	}
	
	/**
	 * 登出系统
	 */
	public static void logout(HttpsNode node, NodeConfig config){
		HttpsClientUtil.sendRequest(node, config, "/oc/v2.3/tokens",RequestType.DELETE, null);
	}
	
	/**
	 * 获取实时性能数据
	 * @param node
	 * @param config
	 * @param type   类型
	 * @param content  编码后的包含了object_id和kpi_groups
	 * @return
	 */
	public static String getPerfmance(HttpsNode node, NodeConfig config, String type, String content, String token){
		Object obj = HttpsClientUtil.sendRequestt(node, config, MessageFormat.format("/oc/v2.3/monitors/{0}/realtime?{1}", type, content), 
				RequestType.GET, null, null, token);
		if(obj != null)
			return obj.toString();
		else
			return null;
	}
	
	/**
	 * 返回一个对象的指标组进行URL编码格式
	 * @param object_id
	 * @param metrics
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String getURLEncodedMetrics(String object_id, Map<String, List<String>> metrics){
		String objId = "[\"".concat(object_id).concat("\"]");
		JSONArray json = JSONArray.fromObject(metrics);
		return "object_ids=" + URLEncoder.encode(objId).concat("&").concat("kpi_groups=".concat(URLEncoder.encode(json.toString().substring(1, json.toString().length()-1))));
	}
	
	/**
	 * 获取资源信息
	 * @param node
	 * @param conf
	 * @param restURI
	 * @param method
	 * @param content
	 * @param userid
	 * @param token
	 * @return
	 */
	public static Object sendRequestt(HttpsNode node, NodeConfig conf,String restURI, 
			RequestType method, String content, String userid, String token){
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/json;charset=utf-8");
		headerMap.put("Accept", "application/json;charset=UTF-8");
		if (null != conf.getHostIP()){
			headerMap.put("Host", conf.getHostIP());
		}
		headerMap.put("X-Auth-Token", token);
		headerMap.put("User-Agent", "HttpClient");
		return HttpsClientUtil.sendRequest(node, conf, headerMap, method, restURI, content);
	}
	
}
