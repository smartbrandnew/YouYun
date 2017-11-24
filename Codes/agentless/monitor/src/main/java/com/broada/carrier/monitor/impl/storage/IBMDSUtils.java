package com.broada.carrier.monitor.impl.storage;

import java.util.List;

import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.impl.smis.SmisSession;

public class IBMDSUtils {
private static final Logger logger = LoggerFactory.getLogger(IBMDSUtils.class);
	
	private IBMDSUtils() {
		throw new RuntimeException("此工具类不允许构建实例");
	}
	
	/**
	 * 存储系统
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findStorageSystem(SmisSession session) {
		List<CIMInstance> storageSystems =  session.getInstancesByClass("LSISSI_StorageSystem");
		return storageSystems;
	}
	
	/**
	 * 存储系统
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> find(SmisSession session) {
		List<CIMInstance> storageSystems =  session.getInstancesByClass("LSISSI_StorageSystem");
		if(storageSystems.size()>0)
			logger.warn("获取到存储系统个数："+storageSystems.size()+"!");
		for (int i = 0; i < storageSystems.size(); i++) {
			CIMInstance cim = storageSystems.get(i);
		}
		return storageSystems;
	}
	
	
	
	/**
	 * 存储系统
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findCIMInstances(SmisSession session,String path) {
		List<CIMInstance> storageSystems =  session.getInstancesByClass(path);
		if(storageSystems.size()>0)
			logger.warn("获取到"+path+"实例个数："+storageSystems.size()+"!");
		return storageSystems;
	}
	
	/**
	 * 卷组
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findVolumeGroup(SmisSession session) {
		List<CIMInstance> volumeGroups =  session.getInstancesByClass("CIM_StorageVolume");
		return volumeGroups;
	}
	
	/**
	 * 
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findExtentPool(SmisSession session) {
		List<CIMInstance> extentPools =  session.getInstancesByClass("IBMTSDS_ExtentPool");
		return extentPools;
	}
	
	/**
	 * 小型计算机通讯协议控制器
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findSCSIProtocolController(SmisSession session) {
		List<CIMInstance> scsipcs =  session.getInstancesByClass("IBMTSDS_SCSIProtocolController");
		return scsipcs;
	}
	
	/**
	 * 阵列框
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findChassis(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> chassisInstances = null;
		try {
			chassisInstances = session.getAssociatedInstances(arrayCOP, "CIM_SystemPackaging", "CIM_PhysicalPackage", "Dependent", "Antecedent");
			if (chassisInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取物理磁盘数为0，请确认支持smi-s协议或者具有物理磁盘！");
			}
			logger.warn("获取到物理磁盘个数："+chassisInstances.size());
			return chassisInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 磁盘
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
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 磁盘信息
	 * @param session
	 * @param ddCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskEctent(SmisSession session,CIMObjectPath ddCOP){
		List<CIMInstance> deInstances = null;
		try {
			deInstances = session.getAssociatedInstances(ddCOP, "CIM_MediaPresent", "CIM_StorageExtent", "Antecedent", "Dependent");
			if (deInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取磁盘信息数为0，请确认支持smi-s协议或者具有磁盘信息！");
			}
			return deInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 磁盘模块
	 * @param session
	 * @param ddCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskPackage(SmisSession session,CIMObjectPath ddCOP){
		List<CIMInstance> dpInstances = null;
		try {
			dpInstances = session.getAssociatedInstances(ddCOP, "CIM_Realizes", "CIM_PhysicalPackage", "Dependent", "Antecedent");
			if (dpInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取磁盘模块数为0，请确认支持smi-s协议或者具有磁盘模块！");
			}
			return dpInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询硬盘性能实例
	 * @param session
	 * @param diskCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskDriverStatsInstances(SmisSession session, CIMObjectPath diskCOP) {
		return session.getAssociatedInstances(diskCOP, "LSISSI_DiskDriveStatisticalData", "LSISSI_DiskDriveStatisticalData","ManagedElement", "Stats");
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
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 存储信息
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findStorageExtent(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> storagePoolInstances = null;
		try {
			storagePoolInstances = session.getAssociatedInstances(arrayCOP, "CIM_SystemDevice", "CIM_StorageExtent", "GroupComponent", "PartComponent");
			if (storagePoolInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储池数为0，请确认支持smi-s协议或者具有存储池信息！");
			}
			logger.warn("获取到存储池个数："+storagePoolInstances.size());
			return storagePoolInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 存储卷
	 * @param session
	 * @param poolCOP
	 * @return
	 */
	public static List<CIMInstance> findStorageVolume(SmisSession session,CIMObjectPath poolCOP){
		List<CIMInstance> storageVolumeInstances = null;
		try {
			storageVolumeInstances = session.getAssociatedInstances(poolCOP, "CIM_AllocatedFromStoragePool", "CIM_StorageVolume", "Antecedent", "Dependent");
			if (storageVolumeInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储卷数为0，请确认支持smi-s协议或者具有存储卷信息！");
			}
			logger.warn("获取到存储卷个数："+storageVolumeInstances.size());
			return storageVolumeInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	
	/**
	 * 控制器信息
	 * @param session
	 * @param scsipcCOP
	 * @return
	 */
	public static List<CIMInstance> findCtrlProduct(SmisSession session,CIMObjectPath scsipcCOP){
		List<CIMInstance> ctrlProductInstances = null;
		try {
			ctrlProductInstances = session.getAssociatedInstances(scsipcCOP, "CIM_SystemPackaging", "CIM_PhysicalPackage", "Dependent", "Antecedent");
			if (ctrlProductInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储卷数为0，请确认支持smi-s协议或者具有存储卷信息！");
			}
			logger.warn("获取到存储卷个数："+ctrlProductInstances.size());
			return ctrlProductInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 协议终结点
	 * @param session
	 * @param scsipcCOP
	 * @return
	 */
	public static List<CIMInstance> findProtocolEndPoint(SmisSession session,CIMObjectPath scsipcCOP){
		List<CIMInstance> protocolEndPointInstances = null;
		try {
			protocolEndPointInstances = session.getAssociatedInstances(scsipcCOP, "CIM_SAPAvailableForElement", "CIM_SCSIProtocolEndpoint", "AvailableSAP", "ManagedElement");
			if (protocolEndPointInstances.size() == 0 ) {
				protocolEndPointInstances = session.getAssociatedInstances(scsipcCOP, "CIM_SAPAvailableForElement", "CIM_SCSIProtocolEndpoint", "ManagedElement", "AvailableSAP");
				if (protocolEndPointInstances.size() == 0 )
				throw new UnsupportedOperationException("通过smi-s协议获取协议终结点数为0，请确认支持smi-s协议或者具有协议终结点信息！");
			}
			logger.warn("获取到协议终结点个数："+protocolEndPointInstances.size());
			return protocolEndPointInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 协议终结点端口
	 * @param session
	 * @param pepCOP
	 * @return
	 */
	public static List<CIMInstance> findControllerPort(SmisSession session,CIMObjectPath pepCOP){
		List<CIMInstance> protocolEndPointInstances = null;
		try {
			protocolEndPointInstances = session.getAssociatedInstances(pepCOP, "CIM_DeviceSAPImplementation", "CIM_FCPort", "Antecedent", "Dependent");
			if (protocolEndPointInstances.size() == 0 ) {
				protocolEndPointInstances = session.getAssociatedInstances(pepCOP, "CIM_DeviceSAPImplementation", "CIM_FCPort", "Dependent", "Antecedent");
				if (protocolEndPointInstances.size() == 0 )
				throw new UnsupportedOperationException("通过smi-s协议获取协议终结点端口数为0，请确认支持smi-s协议或者具有协议终结点端口信息！");
			}
			logger.warn("获取到协议终结点个数："+protocolEndPointInstances.size());
			return protocolEndPointInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
}
