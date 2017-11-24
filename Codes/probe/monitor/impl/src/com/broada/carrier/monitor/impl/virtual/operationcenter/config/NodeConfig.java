package com.broada.carrier.monitor.impl.virtual.operationcenter.config;

import java.util.HashMap;

import com.broada.carrier.monitor.method.operationcenter.OperationCenterMethodOption;

public class NodeConfig{

	//OC服务器IP地址
	private String ocip;

	//OC服务器端口，默认63234
	private String port = "63234";

	//证书信息，用于单/双向验证证书
	private HashMap<String, String> certificate;

	//本机IP地址，通过vpn访问则为虚拟IP
	private String hostIP;

	public String getOCIp(){
		return ocip;
	}

	public void setOCIp(String ocip){
		this.ocip = ocip;
	}

	public String getPort(){
		return port;
	}

	public void setPort(String port){
		this.port = port;
	}

	public HashMap<String, String> getCertificate(){
		return certificate;
	}

	public void setCertificate(HashMap<String, String> certificate){
		this.certificate = certificate;
	}

	public String getHostIP(){
		return hostIP;
	}

	public void setHostIP(String hostIP){
		this.hostIP = hostIP;
	}

	public static NodeConfig initNodeConfig(OperationCenterMethodOption option) {
		NodeConfig nodeConf = new NodeConfig();
		nodeConf.setOCIp(option.getOCIp()); //OC的访问IP
		nodeConf.setPort(String.valueOf(option.getPort()));//OC的访问IP，默认63234
		HashMap<String, String> certificate = new HashMap<String, String>();
		certificate.put("keyStoreName", getConfigDir() + "cChat.keystore");// 本地证书，关闭双向认证时不需要设置   
		certificate.put("keyStorePwd", "cNetty"); //证书库密钥，生成证书时指定
		certificate.put("trustStoreName", getConfigDir() + "cChat.truststore");//客户端信任库
		certificate.put("trustStorePwd", "cNetty");//证书库密钥，生成证书时指定
		nodeConf.setCertificate(certificate);
		nodeConf.setHostIP(option.getHostIp());//本机IP地址
		return nodeConf;
	}

	/**
	 * 获取证书所在目录
	 * @return
	 */
	public static String getConfigDir(){
		return System.getProperty("user.dir") + "/conf/";
	}

}
