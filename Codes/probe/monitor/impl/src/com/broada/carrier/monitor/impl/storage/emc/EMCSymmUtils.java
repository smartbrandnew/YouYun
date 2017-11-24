package com.broada.carrier.monitor.impl.storage.emc;

import java.util.List;

import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.impl.smis.SmisSession;

public class EMCSymmUtils {
private static final Logger logger = LoggerFactory.getLogger(EMCSymmUtils.class);
	
	private EMCSymmUtils() {
		throw new RuntimeException("此工具类不允许构建实例");
	}
	
	/**
	 * 阵列
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findArrayInstances(SmisSession session) {
		List<CIMInstance> arrayInstances =  session.getInstancesByClass("Symm_StorageSystem");
		return arrayInstances;
	}
	
	/**
	 * computerSystem
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findComputerSystem(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> computerSysInstances = null;
		try {
			computerSysInstances = session.getAssociatedInstances(arrayCOP, "CIM_ComponentCS", "CIM_ComputerSystem", "GroupComponent", "PartComponent");
			if (computerSysInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储信息数为0，请确认支持smi-s协议或者具有存储信息！");
			}
			logger.warn("获取到存储信息个数："+computerSysInstances.size());
			return computerSysInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * FC端口
	 * @param session
	 * @param computerSystemCOP
	 * @return
	 */
	public static List<CIMInstance> findFcOrIscsiPort(SmisSession session,CIMObjectPath ctlrCOP){
		List<CIMInstance> fcPortInstances = null;
		try {
			fcPortInstances = session.getAssociatedInstances(ctlrCOP, "CIM_SystemDevice", "CIM_FCPort", "GroupComponent", "PartComponent");
			if (fcPortInstances.size() == 0 ) {
				fcPortInstances =  session.getInstancesByClass("DHS_ISCSIEthernetPort");
			}
			return fcPortInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 物理磁盘
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskDriver(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> ddInstances = null;
		try {
			ddInstances = session.getAssociatedInstances(arrayCOP, "CIM_SystemDevice", "CIM_DiskDrive", "GroupComponent", "PartComponent");
			if (ddInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取磁盘模块数为0，请确认支持smi-s协议或者具有磁盘模块信息！");
			}
			return ddInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 存储池
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findStoragePool(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> storagePoolInstances = null;
		try {
			storagePoolInstances = session.getAssociatedInstances(arrayCOP, "CIM_HostedStoragePool", "CIM_StoragePool", "GroupComponent", "PartComponent");
			if (storagePoolInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储池数为0，请确认支持smi-s协议或者具有存储池信息！");
			}
			logger.warn("获取到存储池个数："+storagePoolInstances.size());
			return storagePoolInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 存储卷
	 * @param session
	 * @param storagePoolCOP
	 * @return
	 */
	public static List<CIMInstance> findStorageVolume(SmisSession session,CIMObjectPath spCOP){
		List<CIMInstance> storageVolumeInstances = null;
		try {
			storageVolumeInstances = session.getAssociatedInstances(spCOP, "CIM_AllocatedFromStoragePool", "CIM_StorageVolume", "Antecedent", "Dependent");
			if (storageVolumeInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储卷数为0，请确认支持smi-s协议或者具有存储卷信息！");
			}
			logger.warn("获取到存储卷个数："+storageVolumeInstances.size());
			return storageVolumeInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 存储卷
	 * @param session
	 * @param storagePoolCOP
	 * @return
	 */
	public static List<CIMInstance> findBlockStorage(SmisSession session,CIMObjectPath spCOP){
		List<CIMInstance> storageVolumeInstances = null;
		try {
			storageVolumeInstances = session.getAssociatedInstances(spCOP, "CIM_AllocatedFromStoragePool", "CIM_StorageVolume", "Antecedent", "Dependent");
			if (storageVolumeInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储卷数为0，请确认支持smi-s协议或者具有存储卷信息！");
			}
			logger.warn("获取到存储卷个数："+storageVolumeInstances.size());
			return storageVolumeInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
}
