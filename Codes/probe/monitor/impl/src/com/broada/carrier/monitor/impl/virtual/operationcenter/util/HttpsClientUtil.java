package com.broada.carrier.monitor.impl.virtual.operationcenter.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.virtual.operationcenter.config.NodeConfig;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.HttpsClient;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.HttpsNode;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.HttpsTrustManagerUtil;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.RequestEntity;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.RequestType;
import com.broada.carrier.monitor.impl.virtual.operationcenter.http.ResponseEntity;

public class HttpsClientUtil{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsTrustManagerUtil.class);

	private static HttpsClient client = new HttpsClient();

	/**
	 * http请求
	 * @param node 目标连接节点
	 * @param conf 连接配置信息
	 * @param restURI rest接口URI
	 * @param method 请求类型
	 * @param content 请求消息内容
	 * @param token  请求鉴权的TOKEN
	 * @return 响应结果
	 * @throws Exception
	 * @see [类、类#方法、类#成员]
	 */

	//获取TOKEN请求
	public static Object sendRequest(HttpsNode node, NodeConfig conf,String restURI, 
			RequestType method, String content){
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/json;charset=utf-8");
		if (null != conf.getHostIP()){
			headerMap.put("Host", conf.getHostIP());
		}
		headerMap.put("Accept", "application/json;charset=UTF-8");
		headerMap.put("User-Agent", "HttpClient");
		LOGGER.info("*******restURI is {} ", restURI);
		LOGGER.info("*******headerMap is {} ", headerMap);
		return sendRequest(node, conf, headerMap, method, restURI, content);
	}

	//调业务接口请求
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
		return sendRequest(node, conf, headerMap, method, restURI, content);
	}

	/**
	 * HTTPS请求
	 * 向指定的HTTP服务器发送https请求
	 * @param node 目标连接节点
	 * @param conf 连接配置信息
	 * @param node https服务节点
	 * @param restURI REST URI
	 * @param method http请求方法
	 * @param content http请求内容
	 * @param headerMap http请求Header信息  
	 * @return
	 * @throws Exception
	 * @see [类、类#方法、类#成员]
	 */
	public static Object sendRequest(HttpsNode node, NodeConfig conf,
			Map<String, String> headerMap, RequestType method, String restURI, String content){
		RequestEntity requestEntity = new RequestEntity();
		requestEntity.setNode(node);
		requestEntity.setReqType(method);

		String fullURL = String.format("https://%s:%s%s", node.getIp(), node.getPort(), restURI);
		requestEntity.setUrl(fullURL);
		requestEntity.setHeader(headerMap);
		if (content != null){
			requestEntity.setContent(content);
		}
		LOGGER.info("requestEntity is {} ", requestEntity.getContent());
		client.SetNodeConf(conf);
		ResponseEntity responseEntity = client.sendRequest(requestEntity);
		Object responseMsg = responseEntity.getContent();
		LOGGER.info(fullURL);
		return responseMsg;
	}
	
}
