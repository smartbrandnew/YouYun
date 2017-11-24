package com.broada.carrier.monitor.method.vmware;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidArgument;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;

/**
 * 为vsphere sdk的访问提供一些工具性质的操作，如获取vsphere性能数据，内部提供静态方法
 *
 * @author Panhk
 * @version 1.0
 * @created 28-九月-2012 9:22:23
 */
public class VSphereUtil {
	private static final Log logger = LogFactory.getLog(VSphereUtil.class);
	private static final Map<String, String> powerStateMap = new HashMap<String, String>();
	private static final Map<String, String> overallStatusMap = new HashMap<String, String>();
	private static final Map<String, String> computeResourceTypeMap = new HashMap<String, String>();

	static {
		powerStateMap.put("poweredOff", "停机");
		powerStateMap.put("poweredOn", "运行");
		powerStateMap.put("suspended", "暂停");

		overallStatusMap.put("gray", "未知");
		overallStatusMap.put("green", "正常");
		overallStatusMap.put("red", "存在问题");
		overallStatusMap.put("yellow", "可能存在问题");

		computeResourceTypeMap.put("ComputeResource", "主机");
		computeResourceTypeMap.put("ClusterComputeResource", "集群");
	}

	/**
	 * 获取动态属性，方法逻辑：
	 * 1.创建属性条件数组：PropertySpec[]
	 * 2.创建属性过滤条件数组：PropertyFilterSpec[]
	 * 3.调用sdk的retrieveProperties方法获取数据
	 * 4.以<管理对象引用, <属性名, 属性对象>>组织Map数据，如果属性对象是基本数据类型，那么将其转化为相应包装类。
	 * <p/>
	 * 注意：
	 * 正常情况下返回非空Map，如果参数不合法，则会返回null
	 *
	 * @param connection vsphere连接，该连接的状态必须是已连接
	 * @param infoType   属性路径，最佳形式为{{管理对象名称,属性名称1,属性名称2,...}...}，如new String[][] { new String[] { "HostSystem",
	 *                   "summary" }}，最终返回的是HostSystem管理对象的summary属性
	 * @throws VSphereException
	 */
	public static Map<ManagedObjectReference, Map<String, Object>> retrieveProperties(VSphereConnection connection,
			String[][] infoType) throws VSphereException {
		// 参数验证
		if (connection == null || !connection.isConnected()) {
			return null;
		}
		if (infoType == null || infoType.length <= 0) {
			return null;
		}

		ManagedObjectReference usecoll = connection.getPropCol();
		ManagedObjectReference useroot = connection.getRootFolder();
		SelectionSpec[] selectionSpecs = buildFullTraversal();
		PropertySpec[] propspecary = buildPropertySpecArray(infoType);
		PropertyFilterSpec spec = new PropertyFilterSpec(null, null, propspecary,
				new ObjectSpec[] { new ObjectSpec(null, null, useroot, Boolean.FALSE, selectionSpecs) }, null);
		ObjectContent[] retoc = null;
		try {
			retoc = connection.getService().retrieveProperties(usecoll, new PropertyFilterSpec[] { spec });
		} catch (Exception e) {
			if (e instanceof VSphereException) {
				throw new VSphereException("获取vsphere动态属性失败", e);
			} else {
				throw new VSphereException("获取vsphere动态属性失败，错误：" + e.toString(), e);
			}
		}
		Map<ManagedObjectReference, Map<String, Object>> returnMap = new HashMap<ManagedObjectReference, Map<String, Object>>();
		if (retoc != null && retoc.length >= 0) {
			for (ObjectContent content : retoc) {
				Map<String, Object> propMap = new HashMap<String, Object>();
				returnMap.put(content.getObj(), propMap);
				for (DynamicProperty prop : content.getPropSet()) {
					propMap.put(prop.getName(), prop.getVal());
				}
			}
		}
		return returnMap;
	}

	/**
	 * 获取指定指定指标在指定时间内的管理对象性能数据，方法逻辑：
	 * 1.创建性能查询条件数组：PerfQuerySpec[]
	 * 2.手动创建性能标识数组：PerfMetricId[]
	 * 3.使用ServiceInstance的queryPerf方法查询性能指标数据
	 * 4.以<管理对象引用, <指标定义标识, 指标性能值数组>>方式组织Map数据并返回
	 * <p/>
	 * 注意：
	 * 正常情况下返回非空Map，如果参数不合法，则会返回null
	 *
	 * @param connection vsphere连接，该连接的状态必须是已连接
	 * @param mors       管理对象引用列表
	 * @param counterIds 性能指标标识
	 * @param startTime  开始时间
	 * @param endTime    结束时间
	 *
	 * @return 返回一个<管理对象引用,<指标定义标识, 指标性能值数组>>的map,
	 * 其中性能指标数组为在间隔时间内每隔20秒的所有检测数据
	 */
	public static Map<ManagedObjectReference, Map<Integer, long[]>> getIntPerfValues(VSphereConnection connection,
			ManagedObjectReference[] mors, int[] counterIds, Calendar startTime, Calendar endTime) throws VSphereException {
		// 参数验证
		if (connection == null || !connection.isConnected()) {
			return null;
		}
		if (mors == null || mors.length <= 0) {
			return null;
		}
		if (counterIds == null || counterIds.length <= 0) {
			return null;
		}

		ManagedObjectReference perfMgr = connection.getServiceContent().getPerfManager();
		VimPortType service = connection.getService();

		// 查询的性能指标标识
		PerfMetricId[] metricIds = new PerfMetricId[counterIds.length];
		for (int i = 0; i < counterIds.length; i++) {
			metricIds[i] = new PerfMetricId(null, null, counterIds[i], "");
		}

		// 查询条件
		PerfQuerySpec[] qSpecs = new PerfQuerySpec[mors.length];
		for (int i = 0; i < qSpecs.length; i++) {
			qSpecs[i] = new PerfQuerySpec();
			qSpecs[i].setMetricId(metricIds);
			qSpecs[i].setEntity(mors[i]);
			qSpecs[i].setStartTime(startTime);
			qSpecs[i].setEndTime(endTime);

			// 查询vsphere性能更新周期为20s的性能数据
			qSpecs[i].setIntervalId(20);
		}

		PerfEntityMetricBase[] samples = null;
		try {
			samples = service.queryPerf(perfMgr, qSpecs);
		} 
		catch (Exception e) {
			logger.debug("堆栈:", e);
			if (e instanceof InvalidArgument||e instanceof RuntimeFault) {
				logger.debug("采集某个指标出现null值异常");
			}else if (e instanceof VSphereException) {
				throw new VSphereException("根据获取管理对象的性能数据失败", e);
			} else {
				throw new VSphereException("根据获取管理对象的性能数据失败，错误：" + e.toString(), e);
			}
		}
		Map<ManagedObjectReference, Map<Integer, long[]>> resultMap = new HashMap<ManagedObjectReference, Map<Integer, long[]>>();
		if (samples != null) {
			for (PerfEntityMetricBase obj : samples) {
				PerfEntityMetric sample = (PerfEntityMetric) obj;
				Map<Integer, long[]> valueMap = new HashMap<Integer, long[]>();
				resultMap.put(sample.getEntity(), valueMap);

				for (PerfMetricSeries series : sample.getValue()) {
					valueMap.put(series.getId().getCounterId(), ((PerfMetricIntSeries) series).getValue());
				}
			}
		}
		return resultMap;
	}

	/**
	 * 获取指定指定指标的管理对象性能数据，方法逻辑：
	 * 1.创建性能查询条件数组：PerfQuerySpec[]
	 * 2.手动创建性能标识数组：PerfMetricId[]
	 * 3.使用ServiceInstance的queryPerf方法查询性能指标数据
	 * 4.以<管理对象引用, <指标定义标识, 指标性能值>>方式组织Map数据并返回
	 * <p/>
	 * 注意：
	 * 正常情况下返回非空Map，如果参数不合法，则会返回null
	 *
	 * @param connection vsphere连接，该连接的状态必须是已连接
	 * @param mors       管理对象引用列表
	 * @param counterIds 性能指标标识
	 */
	public static Map<ManagedObjectReference, Map<Integer, Long>> getIntPerfValues(VSphereConnection connection,
			ManagedObjectReference[] mors, int[] counterIds) throws VSphereException {
		// 参数验证
		if (connection == null || !connection.isConnected()) {
			return null;
		}
		if (mors == null || mors.length <= 0) {
			return null;
		}
		if (counterIds == null || counterIds.length <= 0) {
			return null;
		}

		ManagedObjectReference perfMgr = connection.getServiceContent().getPerfManager();
		VimPortType service = connection.getService();

		// 性能数据的查询开始时间
		//		Calendar endTime = Calendar.getInstance();
		//		Calendar startTime = (Calendar) endTime.clone();
		//		startTime.add(Calendar.SECOND, -60);
		//		startTime.setTimeInMillis(0);
		// start.add(Calendar.SECOND, 0); //目前只查询实时数据，所以不需要这一项

		// 时间应该服务器时间
		Calendar endTime = getServerTime(connection);
		Calendar startTime = (Calendar) endTime.clone();
		startTime.add(Calendar.MINUTE, -5);

		// 查询的性能指标标识
		PerfMetricId[] metricIds = new PerfMetricId[counterIds.length];
		for (int i = 0; i < counterIds.length; i++) {
			metricIds[i] = new PerfMetricId(null, null, counterIds[i], "");
		}

		// 查询条件
		PerfQuerySpec[] qSpecs = new PerfQuerySpec[mors.length];
		for (int i = 0; i < qSpecs.length; i++) {
			qSpecs[i] = new PerfQuerySpec();
			qSpecs[i].setMetricId(metricIds);
			qSpecs[i].setEntity(mors[i]);
			qSpecs[i].setStartTime(startTime);
			qSpecs[i].setEndTime(endTime);
			// 查询vsphere性能更新周期为5分钟的性能数据
			qSpecs[i].setIntervalId(Integer.valueOf(300));
		}

		PerfEntityMetricBase[] samples = null;
		try {
			samples = service.queryPerf(perfMgr, qSpecs);
		} catch (Exception e) {
			logger.debug("堆栈:", e);
			if (e instanceof InvalidArgument||e instanceof RuntimeFault) {
				logger.debug("采集某个指标出现null值异常");
			}else if (e instanceof VSphereException) {
				throw new VSphereException("根据获取管理对象的性能数据失败", e);
			} else {
				throw new VSphereException("根据获取管理对象的性能数据失败，错误：" + e.toString(), e);
			}
		}
		Map<ManagedObjectReference, Map<Integer, Long>> resultMap = new HashMap<ManagedObjectReference, Map<Integer, Long>>();
		if (samples != null) {
			for (PerfEntityMetricBase obj : samples) {
				PerfEntityMetric sample = (PerfEntityMetric) obj;
				Map<Integer, Long> valueMap = new HashMap<Integer, Long>();
				resultMap.put(sample.getEntity(), valueMap);
				if(sample.getSampleInfo() == null || sample.getSampleInfo().length < 1) continue;
				// 每个指标都会对应一组性能数据，我们只获取最近一次的性能数据
				int latestIndex = sample.getSampleInfo().length - 1;
				if (latestIndex >= 0) {
					for (PerfMetricSeries series : sample.getValue()) {
						valueMap.put(Integer.valueOf(series.getId().getCounterId()),
								Long.valueOf(((PerfMetricIntSeries) series).getValue(latestIndex)));
					}
				}
			}
		}
		return resultMap;
	}

	/**
	 * 获取指定管理对象的属性，方法逻辑：
	 * 获取动态属性，方法逻辑：
	 * 1.创建属性过滤条件数组：PropertyFilterSpec[]
	 * 2.调用sdk的retrieveProperties方法获取数据
	 * 3.以<属性名, 属性对象>组织Map数据，如果属性对象是基本数据类型，那么将其转化为相应包装类。
	 * <p/>
	 * 注意：
	 * 正常情况下返回非空Map，如果参数不合法，则会返回null
	 *
	 * @param connection vsphere连接，该连接的状态必须是已连接
	 * @param mor        管理对象引用
	 * @param properties 属性名，如new String[] {"perfCounter"}，最终查询管理对象的perfCounter属性
	 */
	public static Map<String, Object> retrieveProperties(VSphereConnection connection, ManagedObjectReference mor,
			String[] properties) throws VSphereException {
		// 参数验证
		if (connection == null || !connection.isConnected()) {
			return null;
		}
		if (mor == null) {
			return null;
		}
		if (properties == null || properties.length <= 0) {
			return null;
		}

		ManagedObjectReference usecoll = connection.getPropCol();
		PropertyFilterSpec spec = new PropertyFilterSpec();
		spec.setPropSet(new PropertySpec[] { new PropertySpec() });
		spec.getPropSet(0).setAll(new Boolean(properties == null || properties.length == 0));
		spec.getPropSet(0).setType(mor.getType());
		spec.getPropSet(0).setPathSet(properties);
		spec.setObjectSet(new ObjectSpec[] { new ObjectSpec() });
		spec.getObjectSet(0).setObj(mor);
		spec.getObjectSet(0).setSkip(Boolean.FALSE);

		ObjectContent[] retoc = null;
		try {
			retoc = connection.getService().retrieveProperties(usecoll, new PropertyFilterSpec[] { spec });
		} catch (Exception e) {
			if (e instanceof VSphereException) {
				throw new VSphereException("获取vsphere动态属性失败", e);
			} else {
				throw new VSphereException("获取vsphere动态属性失败，错误：" + e.toString(), e);
			}
		}
		Map<String, Object> returnMap = new HashMap<String, Object>();
		if (retoc != null && retoc.length >= 0)
			for (DynamicProperty prop : retoc[0].getPropSet()) {
				returnMap.put(prop.getName(), prop.getVal());
			}
		return returnMap;
	}

	private static SelectionSpec[] buildFullTraversal() {
		// Recurse through all ResourcePools
		TraversalSpec rpToRp = new TraversalSpec(null, null, null, "ResourcePool", "resourcePool", Boolean.FALSE,
				new SelectionSpec[] { new SelectionSpec(null, null, "rpToRp"), new SelectionSpec(null, null, "rpToVm") });
		rpToRp.setName("rpToRp");
		// Recurse through all ResourcePools
		TraversalSpec rpToVm = new TraversalSpec(null, null, null, "ResourcePool", "vm", Boolean.FALSE,
				new SelectionSpec[] {});
		rpToVm.setName("rpToVm");
		// Traversal through ResourcePool branch
		TraversalSpec crToRp = new TraversalSpec(null, null, null, "ComputeResource", "resourcePool", Boolean.FALSE,
				new SelectionSpec[] { new SelectionSpec(null, null, "rpToRp"), new SelectionSpec(null, null, "rpToVm") });
		crToRp.setName("crToRp");
		// Traversal through host branch
		TraversalSpec crToH = new TraversalSpec(null, null, null, "ComputeResource", "host", Boolean.FALSE,
				new SelectionSpec[] {});
		crToH.setName("crToH");
		// Traversal through hostFolder branch
		TraversalSpec dcToHf = new TraversalSpec(null, null, null, "Datacenter", "hostFolder", Boolean.FALSE,
				new SelectionSpec[] { new SelectionSpec(null, null, "visitFolders") });
		dcToHf.setName("dcToHf");
		// Traversal through vmFolder branch
		TraversalSpec dcToVmf = new TraversalSpec(null, null, null, "Datacenter", "vmFolder", Boolean.FALSE,
				new SelectionSpec[] { new SelectionSpec(null, null, "visitFolders") });
		dcToVmf.setName("dcToVmf");
		// Recurse through all Hosts
		TraversalSpec HToVm = new TraversalSpec(null, null, null, "HostSystem", "vm", Boolean.FALSE,
				new SelectionSpec[] { new SelectionSpec(null, null, "visitFolders") });
		HToVm.setName("HToVm");
		// Recurse thriugh the folders
		TraversalSpec visitFolders = new TraversalSpec(null, null, null, "Folder", "childEntity", Boolean.FALSE,
				new SelectionSpec[] { new SelectionSpec(null, null, "visitFolders"), new SelectionSpec(null, null, "dcToHf"),
				new SelectionSpec(null, null, "dcToVmf"), new SelectionSpec(null, null, "crToH"),
				new SelectionSpec(null, null, "crToRp"), new SelectionSpec(null, null, "HToVm"),
				new SelectionSpec(null, null, "rpToVm"), });
		visitFolders.setName("visitFolders");
		return new SelectionSpec[] { visitFolders, dcToVmf, dcToHf, crToH, crToRp, rpToRp, HToVm, rpToVm };
	}

	/**
	 * 参加属性条件数组
	 * 1.整理参数，最终形式为Map<管理对象名称,Set<属性名称1.属性名称2,...>...>
	 * 2.创建属性条件数组
	 */
	private static PropertySpec[] buildPropertySpecArray(String[][] typeinfo) {
		// Eliminate duplicates
		HashMap<String, Set<String>> tInfo = new HashMap<String, Set<String>>();
		for (int ti = 0; ti < typeinfo.length; ++ti) {
			Set<String> props = (Set<String>) tInfo.get(typeinfo[ti][0]);
			if (props == null) {
				props = new HashSet<String>();
				tInfo.put(typeinfo[ti][0], props);
			}
			for (int pi = 1; pi < typeinfo[ti].length; ++pi) {
				props.add(typeinfo[ti][pi]);
			}
		}

		// Create PropertySpecs
		PropertySpec[] pSpecs = new PropertySpec[tInfo.keySet().size()];
		int index = 0;
		for (String type : tInfo.keySet()) {
			PropertySpec pSpec = new PropertySpec();
			Set<String> props = tInfo.get(type);
			pSpec.setType(type);
			pSpec.setAll(props.isEmpty() ? Boolean.TRUE : Boolean.FALSE);
			pSpec.setPathSet(props.toArray(new String[0]));
			pSpecs[index++] = pSpec;
		}
		return pSpecs;
	}

	/**
	 * 获取所有性能指标定义信息
	 * <p/>
	 * vsphere尚未提供获取指标定义的遍历操作（如直接根据指标组或名称查询指标）
	 *
	 * @param connection
	 * @return 以<指标标识、指标对象>的方式来组织数据并返回
	 * @throws VSphereException
	 */
	public static Map<Integer, PerfCounterInfo> queryAllPerfCounterInfo(VSphereConnection connection)
	throws VSphereException {
		// 获取性能指标定义
		String[] properties = new String[] { "perfCounter" };
		Map<String, Object> perfInfos = null;
		try {
			perfInfos = VSphereUtil
			.retrieveProperties(connection, connection.getServiceContent().getPerfManager(), properties);
		} catch (VSphereException e) {
			throw new VSphereException("获取性能指标定义失败", e);
		}
		Map<Integer, PerfCounterInfo> returnMap = new HashMap<Integer, PerfCounterInfo>();
		if (perfInfos != null && perfInfos.values() != null) {
			for (Object obj : perfInfos.values()) {
				for (PerfCounterInfo info : ((ArrayOfPerfCounterInfo) obj).getPerfCounterInfo())
					returnMap.put(Integer.valueOf(info.getKey()), info);
			}
		}
		return returnMap;
	}

	/**
	 * 根据性能类型（如cpu.usage 1对应平均值，等等）获取性能指标定义信息
	 *
	 * @param connection
	 * @param level
	 * @return
	 * @throws VSphereException
	 */
	public static Map<Integer, PerfCounterInfo> queryPerfCounterInfosByLevel(VSphereConnection connection, int level)
	throws VSphereException {
		// 获取性能指标定义
		PerfCounterInfo[] perfInfos = null;
		try {
			perfInfos = connection.getService()
			.queryPerfCounterByLevel(connection.getServiceContent().getPerfManager(), level);
		} catch (Exception e) {
			if (e instanceof VSphereException) {
				throw new VSphereException("按级别[" + level + "]获取性能指标定义失败", e);
			} else {
				throw new VSphereException("按级别[" + level + "]获取性能指标定义失败，错误：" + e.toString(), e);
			}
		}
		Map<Integer, PerfCounterInfo> returnMap = new HashMap<Integer, PerfCounterInfo>();
		if (perfInfos != null) {
			for (PerfCounterInfo info : perfInfos) {
				returnMap.put(Integer.valueOf(info.getKey()), info);
			}
		}
		return returnMap;
	}

	/**
	 * 获取电源状态的中文描述
	 *
	 * @return
	 */
	public static String getPowerStateDescr(String powerState) {
		String descr = powerStateMap.get(powerState);
		return descr == null ? powerState : descr;
	}

	/**
	 * 获取整体状态的中文描述
	 *
	 * @return
	 */
	public static String getOverallStatusDescr(String overallStatus) {
		String descr = overallStatusMap.get(overallStatus);
		return descr == null ? overallStatus : descr;
	}

	/**
	 * 获取计算资源类型的中文描述
	 *
	 * @return
	 */
	public static String getComputeResourceTypeDescr(String type) {
		String descr = computeResourceTypeMap.get(type);
		return descr == null ? type : descr;
	}

	/**
	 * 获取服务器时间，如果未能获取到，则取当前时间
	 * @param connection
	 * @return
	 */
	public static Calendar getServerTime(VSphereConnection connection){
		// 构造ServiceInstance数据对象
		ManagedObjectReference serviceInstance = new ManagedObjectReference();
		serviceInstance.setType("ServiceInstance");
		serviceInstance.set_value("ServiceInstance");
		Calendar calendar = null;
		try {
			calendar = connection.getService().currentTime(serviceInstance);
		} catch (Exception e) {
			logger.error(e.getMessage());
			calendar = Calendar.getInstance();
		} 
		return calendar;
	}
}