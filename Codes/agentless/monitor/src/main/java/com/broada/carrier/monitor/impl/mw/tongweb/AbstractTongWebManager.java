package com.broada.carrier.monitor.impl.mw.tongweb;

import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.tongweb.teas.jmx.RMIConnector;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang.StringUtils;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractTongWebManager {
	/**
	 * 监测的结果信息(用于监测结果详细描述).
	 */
	private StringBuffer resultDesc = new StringBuffer();

	/**
	 * 当前情况(用于发送告警信息).
	 */
	private StringBuffer currentVal = new StringBuffer();

	private RMIConnector rmic;

	private String ipAddr;

	private int port;

	private String name;

	private Context context;

	/**
	 * 进行初始化动作
	 * 
	 * @throws NamingException
	 */
	public void init() throws NamingException {

		Properties properties = new Properties();
		properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.tongweb.naming.interfaces.NamingContextFactory");
		// JNDI端口号
		properties.setProperty(Context.PROVIDER_URL, ipAddr + ":" + port);
		context = new InitialContext(properties);

		try {
			rmic = (RMIConnector) context.lookup(name);
		} catch (NamingException e) {
			try {
				context.close();
			} catch (Throwable t) {
			}
			throw e;
		} catch (Throwable e) {
			NamingException ne = new NamingException("获取RMI连接失败.");
			ne.setRootCause(e);
			try {
				context.close();
			} catch (Throwable t) {
			}
			throw ne;
		}
		// 因为要计算连接响应时间，所以不进行缓存
		// rmic = RMIConnectorFactory.getRMIConnector(ipAddr, name, port);
	}

	/**
	 * 关闭并释放资源
	 */
	public void close() {
		if (context != null) {
			try {
				context.close();
			} catch (NamingException e) {
			}
		}
		context = null;
		rmic = null;
	}

	private RMIConnector getRMIConnector() {
		if (rmic == null) {
			throw new NullPointerException("RMIConnector还没有初始化,请先初始化.");
		}
		return rmic;
	}

	protected static List fetchBeanName(Class clazz) {
		Field[] fileds = clazz.getDeclaredFields();
		List filedNameList = new ArrayList();
		for (int i = 0, len = fileds.length; i < len; i++) {
			String name = fileds[i].getName();
			if (StringUtils.isNotEmpty(name)) {
				filedNameList.add(name);
			}
		}

		return filedNameList;
	}

	public List fetchMBeanInfo(String objNameQry, Class clazz) throws MBeanException {

		List filedNameList = fetchBeanName(clazz);

		List beanList = new ArrayList();
		try {
			ObjectName objQry = new ObjectName(objNameQry);

			Set names = getRMIConnector().queryNames(objQry, null);

			for (Iterator it = names.iterator(); it.hasNext();) {
				Object bean = clazz.newInstance();
				BeanMap map = new BeanMap(bean);

				ObjectName name = (ObjectName) it.next();
				MBeanAttributeInfo[] attrs = getRMIConnector().getMBeanInfo(name).getAttributes();
				;

				for (int i = 0, len = attrs.length; i < len; i++) {
					String attrName = attrs[i].getName();
					String beanName = attrName.substring(0, 1).toLowerCase() + attrName.substring(1);
					if (filedNameList.contains(beanName)) {
						map.put(beanName, getRMIConnector().getAttribute(name, attrName));
					}
				}

				setMBeanNameInfo(map, name);

				map.put("domain", name.getDomain());

				beanList.add(map.getBean());
			}

		} catch (Throwable t) {
			RMIConnectorFactory.remove(ipAddr + name + port);
			throw new MBeanException("MBean提取失败,Name=" + objNameQry, t);
		}

		return beanList;
	}

	/**
	 * 装配性能数据.
	 * 
	 * @param nameForIndex
	 * @param instanceKey
	 * @return
	 */
	public static PerfResult[] assemblePerf(List nameForIndex, String instanceKey) {
		List perfResultList = new ArrayList();
		for (Iterator it = nameForIndex.iterator(); it.hasNext();) {
			PerfItemMap perfItemMap = (PerfItemMap) it.next();
			PerfResult pr = new PerfResult(perfItemMap.getCode(), true);
			Object value = perfItemMap.getValue();

			if (value instanceof String) {
				pr.setStrValue((String) value);
			} else if (value instanceof Double) {
				pr.setValue(((Double) value).doubleValue());
			} else if (value instanceof Integer) {
				pr.setValue(Integer.parseInt(value.toString()));
			}

			pr.setInstanceKey(instanceKey);

			perfResultList.add(pr);
		}

		return (PerfResult[]) perfResultList.toArray(new PerfResult[0]);
	}

	public abstract void setMBeanNameInfo(Map map, ObjectName name);

	public String getIpAddr() {
		return ipAddr;
	}

	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}

	public StringBuffer getCurrentVal() {
		return currentVal;
	}

	public StringBuffer getResultDesc() {
		return resultDesc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
