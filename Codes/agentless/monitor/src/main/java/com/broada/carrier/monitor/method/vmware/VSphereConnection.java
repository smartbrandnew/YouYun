package com.broada.carrier.monitor.method.vmware;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vmware.vim25.InvalidLogin;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;

/**
 * 处理vsphere连接的相关操作
 * @author Panhk
 * @version 1.0
 * @created 28-九月-2012 9:45:30
 */
public class VSphereConnection {

	private static final Log logger = LogFactory.getLog(VSphereConnection.class);
	
	/** vsphere 连接 */
	private static final Map<String, SoftReference<VSphereConnection>> connections = new ConcurrentHashMap<String, SoftReference<VSphereConnection>>();
	
	/**
	 * 连接状态，true表示已连接
	 */
	private volatile boolean isConnected;
	/**
	 * 连接对应的url
	 */
	private String urlStr;
	/**
	 * 服务服务内容
	 */
	private ServiceContent serviceContent;
	/**
	 * vsphere服务
	 */
	private VimPortType service;

	/**
	 * 构造ServiceConnection实例
	 * @param urlStr
	 */
	private VSphereConnection(String urlStr) {
		this.urlStr = urlStr;
		isConnected = false;
	}

	/**
	 * 根据实例名创建ServiceConnection实例
	 * 
	 * 为了提高效率，这里不做同步
	 * @param urlStr
	 * @return
	 */
	public static VSphereConnection getVSphereConnection(String urlStr) {
		SoftReference<VSphereConnection> ref = connections.get(urlStr);
		if (ref == null || ref.get() == null) {
			ref = new SoftReference<VSphereConnection>(new VSphereConnection(urlStr));
			connections.put(urlStr, ref);
		}
		return ref.get();
	}
	
	/**
	 * 服务端通过probe测试vsphere sdk连接
	 * 返回null表示连接正常，否则返回的字符串为连接失败的描述信息
	 * @param ipAddr
	 * @param username
	 * @param password
	 * @return
	 */
	public static String testConnect(String ipAddr, String username, String password) {
		VSphereConnection connection = VSphereConnection.getVSphereConnection(getVSphereSDKUrl(ipAddr));
		String message = null;
		try {
			connection.connect(username, password);
		} catch (Exception e) {
			logger.error("连接vSphere sdk失败。", e);
			message = "连接vSphere sdk失败。";
		} finally {
			try {
				if (connection != null) {
					connection.disconnect();
				}
			} catch (VSphereException e) {
				logger.error("断开vSphere sdk连接失败。", e);
				message = "断开vSphere sdk连接失败。";
			}
		}
		return message;
	}
	
	/**
	 * 根据ip地址得到sdk url
	 * 
	 * @param ipAddr ip地址
	 */
	public static String getVSphereSDKUrl(String ipAddr) {
		return "https://" + ipAddr + "/sdk";
	}
	
	/**
	 * 根据参数进行连接
	 * @param userName
	 * @param password
	 * @throws VSphereException
	 */
	public synchronized void connect(String userName, String password) throws VSphereException {
		while (isConnected) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				// 不做处理
			}
		}
		try {
			service = VSphereServiceFactory.getFactory().getVSphereService(urlStr);
			serviceContent = VSphereServiceFactory.getFactory().getServiceContent(urlStr);

			if (serviceContent.getSessionManager() != null) {
				service.login(serviceContent.getSessionManager(), userName, password, null);
			} else {
				VSphereServiceFactory.getFactory().resetVSphereService(urlStr);
			}
			isConnected = true;
		} catch (InvalidLogin e) {
			throw new VSphereException("无法连接vsphere sdk，错误：" + e.toString(), e);
		} catch (Exception e) {
			VSphereServiceFactory.getFactory().resetVSphereService(urlStr);
			throw new VSphereException("无法连接vsphere sdk，错误：" + e.toString(), e);
		}
	}

	/**
	 * 断开连接
	 * 调用此方法前必须确定已调用了connect方法
	 * @throws VSphereException 
	 */
	public synchronized void disconnect() throws VSphereException {
		if (service != null && isConnected) {
			try {
				service.logout(serviceContent.getSessionManager());
			} catch (Exception e) {
				throw new VSphereException("无法断开vSphere sdk连接" + e.toString(), e);
			} finally {
				isConnected = false;
			}
		}
		isConnected = false;
	}

	/**
	 * 状态是否为已连接
	 */
	public boolean isConnected(){
		return isConnected;
	}

	/**
	 * 获取vsphere根服务
	 */
	public VimPortType getService(){
		return service;
	}

	/**
	 * 获取服务内容
	 */
	public ServiceContent getServiceContent(){
		return serviceContent;
	}

	/*
	 * 返回服务内容的根目录管理对象
	 */
	public ManagedObjectReference getRootFolder() {
		return serviceContent.getRootFolder();
	}

	/**
	 * 获取属性收集器管理对象
	 */
	public ManagedObjectReference getPropCol(){
		return serviceContent.getPropertyCollector();
	}

}