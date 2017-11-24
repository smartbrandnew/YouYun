package com.broada.carrier.monitor.impl.storage;

import java.util.ArrayList;
import java.util.List;

import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.impl.smis.SmisSession;

public class HDSUtils {
	private static final Logger logger = LoggerFactory.getLogger(HDSUtils.class);
	
	private HDSUtils() {
		throw new RuntimeException("此工具类不允许构建实例");
	}
	
	/**
	 * 存储系统
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findStorageSystem(SmisSession session) {
		List<CIMInstance> storageSystems =  session.getInstancesByClass("HITACHI_StorageSystem");
		return storageSystems;
	}
	
	/**
	 * 存储池
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findStoragePool(SmisSession session){
		List<CIMInstance> storagePoolInstances = new ArrayList<CIMInstance>();
		try {
			List<CIMInstance> storageSystems =  findStorageSystem(session);
			for (int i = 0; i < storageSystems.size(); i++) {
			CIMInstance cim = storageSystems.get(i);
			List<CIMInstance> spInstances = session.getAssociatedInstances(cim.getObjectPath(), "CIM_HostedStoragePool", "CIM_StoragePool", "GroupComponent", "PartComponent");
			for (int j = 0; j < spInstances.size(); j++) {
				storagePoolInstances.add(spInstances.get(j));	
			}
			if (storagePoolInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储池数为0，请确认支持smi-s协议或者具有存储池信息！");
			}
			}
			logger.warn("获取到存储池个数："+storagePoolInstances.size());
			return storagePoolInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测HDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 存储卷
	 * @param session
	 * @param poolCOP
	 * @return
	 */
	public static List<CIMInstance> findStorageVolume(SmisSession session){
		List<CIMInstance> storageVolumeInstances = null;
		try {
			List<CIMInstance> storagePoolInstances =  findStoragePool(session);
			for (int i = 0; i < storagePoolInstances.size(); i++) {
			CIMInstance cim = storagePoolInstances.get(i);
			storageVolumeInstances = session.getAssociatedInstances(cim.getObjectPath(), "CIM_AllocatedFromStoragePool", "CIM_StorageVolume", "Antecedent", "Dependent");
			if (storageVolumeInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储卷数为0，请确认支持smi-s协议或者具有存储卷信息！");
			}
			}
			logger.warn("获取到存储卷个数："+storageVolumeInstances.size());
			return storageVolumeInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测HDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 存储统计信息
	 * @param session
	 * @param volumeCOP
	 * @return
	 */
	public static List<CIMInstance> findStatistics(SmisSession session,CIMObjectPath volumeCOP){
		List<CIMInstance> statisticsInstances = null;
		try {
			statisticsInstances = session.getAssociatedInstances(volumeCOP, "CIM_ElementStatisticalData", "CIM_BlockStorageStatisticalData", "ManagedElement", "Stats");
			if (statisticsInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储卷数为0，请确认支持smi-s协议或者具有存储卷信息！");
			}
			logger.warn("获取到存储卷个数："+statisticsInstances.size());
			return statisticsInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测HDS存储发生异常。异常信息："+e.getMessage());
		}
	}
}
