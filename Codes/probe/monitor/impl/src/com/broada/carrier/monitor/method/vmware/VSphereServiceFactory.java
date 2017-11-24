package com.broada.carrier.monitor.method.vmware;

import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.VimPortType;
import com.vmware.vim25.VimServiceLocator;

/**
 * ServiceContent工厂，用于 缓存ServiceContent。
 * @author panhk
 *
 */
public class VSphereServiceFactory {
private static final Log logger = LogFactory.getLog(VSphereServiceFactory.class);
	
	private static final Map<String, SoftReference<ServiceContent>> contents = new ConcurrentHashMap<String, SoftReference<ServiceContent>>();
	private static final Map<String, SoftReference<VimPortType>> services = new ConcurrentHashMap<String, SoftReference<VimPortType>>();
	
	private static final VSphereServiceFactory factory = new VSphereServiceFactory();
	
	private static final int VSPHERE_SDK_CONNNECT_CONCURRENT_COUNT = Integer.parseInt(System.getProperty("vsphere.sdk.connect.concurrent.count", "20"));
	private static final int VSPHERE_SDK_CONNNECT_WAIT_TIME = Integer.parseInt(System.getProperty("vsphere.sdk.connect.wait.time", "3"));
	
	private static int count = 0; 
	
	static {
		System.setProperty("org.apache.axis.components.net.SecureSocketFactory",
        "org.apache.axis.components.net.SunFakeTrustSocketFactory");
	}
	
	private VSphereServiceFactory() {
	}

	public static VSphereServiceFactory getFactory() {
		return factory;
	}
	
	void initVSphereService(String urlStr) throws MalformedURLException, ServiceException, RuntimeFault,
			RemoteException {
		VimServiceLocator locator = new VimServiceLocator();
		locator.setMaintainSession(true);

		SoftReference<VimPortType> serviceRef = services.get(urlStr);
		SoftReference<ServiceContent> contentRef = contents.get(urlStr);
		if (serviceRef == null || serviceRef.get() == null || contentRef == null || contentRef.get() == null) {
			// 由于一般正常的连接都有缓存，所以这里不需要担心会出现监测器长时间未监测的情况
			long startTime = System.currentTimeMillis();
			while (true) {
				synchronized (this) {
					if (count < VSPHERE_SDK_CONNNECT_CONCURRENT_COUNT) {
						count++;
						break;
					}
					if (System.currentTimeMillis() - startTime > VSPHERE_SDK_CONNNECT_WAIT_TIME * 60 * 1000) {
						logger.warn("获取vsphere sdk服务前的等待时间超过" + VSPHERE_SDK_CONNNECT_WAIT_TIME + "分钟，不再进行等待。");
						count++;
						break;
					}
				}

				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					// 不做处理
				}
			}
			// 由于bcc中不存在相同节点的多个任务并发的情况，所以这里不会发生多个获取相同节点的操作
			try {
				if (serviceRef == null || serviceRef.get() == null) {
					VimPortType service = locator.getVimPort(new URL(urlStr));
					serviceRef = new SoftReference<VimPortType>(service);
					services.put(urlStr, serviceRef);
				}

				ManagedObjectReference mor = new ManagedObjectReference();
				mor.setType("ServiceInstance");
				mor.set_value("ServiceInstance");
				ServiceContent serviceContent = serviceRef.get().retrieveServiceContent(mor);
				contents.put(urlStr, new SoftReference<ServiceContent>(serviceContent));
			} finally {
				count--;
			}
		}
	}
	
	public void resetVSphereService(String urlStr) {
		// services和contents没有严格的同步要求
		services.remove(urlStr);
		contents.remove(urlStr);
	}
	
	/**
	 * 获取服务
	 * @param urlStr
	 * @return
	 * @throws RuntimeFault
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public VimPortType getVSphereService(String urlStr) throws RuntimeFault, MalformedURLException, RemoteException,
			ServiceException {
		initVSphereService(urlStr);
		// 这里已经不需要考虑空指针的问题了
		SoftReference<VimPortType> ref = services.get(urlStr);
		return ref.get();
	}
	
	/**
	 * 获取服务内容
	 * @param urlStr
	 * @return
	 * @throws RuntimeFault
	 * @throws MalformedURLException
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public ServiceContent getServiceContent(String urlStr) throws RuntimeFault, MalformedURLException,
			RemoteException, ServiceException {
		initVSphereService(urlStr);
		// 这里已经不需要考虑空指针的问题了		
		SoftReference<ServiceContent> ref = contents.get(urlStr);
		return ref.get();
	}
}
