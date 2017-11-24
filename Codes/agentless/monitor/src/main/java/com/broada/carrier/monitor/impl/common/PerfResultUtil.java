package com.broada.carrier.monitor.impl.common;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.PerfResult;

/**
 * 提供PerfResult操作的一些工具方法
 */
public class PerfResultUtil {
	private static Map<Class<?>, List<Method>> perfEntityGetMethods = new HashMap<Class<?>, List<Method>>();
  
	/**
	 * 将entity中通过PerfItem声明的属性，生成为perfs中的指标
	 * @param perfs
	 * @param instanceKey
	 * @param entity
	 */
  public static void fill(MonitorResultRow row, Object entity) {
  	List<Method> methods = perfEntityGetMethods.get(entity.getClass());
  	if (methods == null) {
  		synchronized (perfEntityGetMethods) {
  			methods = perfEntityGetMethods.get(entity.getClass());
  			if (methods == null) {
  				methods = new ArrayList<Method>();
  	  		for (Method m : entity.getClass().getDeclaredMethods()) {
  	  			if (m.isAnnotationPresent(PerfItem.class) && (m.getName().startsWith("get") || m.getName().startsWith("is")))
  	  				methods.add(m);
  	  		}
  	  		perfEntityGetMethods.put(entity.getClass(), methods);
  			}
			}
  	}
  	
  	for (Method method : methods) {
  		Object value;
			try {
				value = method.invoke(entity);
			} catch (Throwable e) {
				throw new RuntimeException(String.format("无法调用类[%s]的方法[%s]", entity.getClass().getName(), method.getName()), e);
			}
  		
  		PerfItem item = method.getAnnotation(PerfItem.class);
  		if (value != null) 
  			row.setIndicator(item.code(), value);  			  		
  	}
	}
  
  /**
   * 从perfs中寻找instanceKey与itemCode符合的项目
   * @param perfs
   * @param instanceKey
   * @param itemCode
   * @return
   */
  public static PerfResult getPerf(List<PerfResult> perfs, String instanceKey, String itemCode) {
		for (PerfResult perf : perfs) {
			if (perf.getInstKey().equals(instanceKey) && perf.getItemCode().equals(itemCode))
				return perf;
		}
		return null;
	}  
}
