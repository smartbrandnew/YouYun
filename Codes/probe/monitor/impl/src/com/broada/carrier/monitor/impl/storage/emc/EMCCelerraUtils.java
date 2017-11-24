package com.broada.carrier.monitor.impl.storage.emc;

import java.util.ArrayList;
import java.util.List;

import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.impl.smis.SmisSession;

public class EMCCelerraUtils {
private static final Logger logger = LoggerFactory.getLogger(EMCCelerraUtils.class);
	
	private EMCCelerraUtils() {
		throw new RuntimeException("此工具类不允许构建实例");
	}
	
	/**
	 * 阵列
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findArrayInstances(SmisSession session) {
		List<CIMInstance> arrayInstances =  session.getInstancesByClass("Celerra_CelerraComputerSystem");
		return arrayInstances;
	}
	
	/**
	 * NFS服务
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findCelerraNFSServer(SmisSession session) {
		List<CIMInstance> celerraNFSServers =  session.getInstancesByClass("Celerra_NFSServer");
		return celerraNFSServers;
	}
	
	/**
	 * NFS共享
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findCelerraNFSShare(SmisSession session) {
		List<CIMInstance> celerraNFSShares =  session.getInstancesByClass("Celerra_NFSShare");
		return celerraNFSShares;
	}
	
	/**
	 * 物理位置
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findCelerraLocation(SmisSession session) {
		List<CIMInstance> celerraLocations =  session.getInstancesByClass("Celerra_Location");
		return celerraLocations;
	}
	
	/**
	 * CIFS服务
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findCelerraCIFSServer(SmisSession session) {
		List<CIMInstance> celerraCIFSServers =  session.getInstancesByClass("Celerra_CIFSServer");
		return celerraCIFSServers;
	}
	
	/**
	 * CIFS共享
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findCelerraCIFSShare(SmisSession session) {
		List<CIMInstance> celerraCIFSShares =  session.getInstancesByClass("Celerra_CIFSShare");
		return celerraCIFSShares;
	}
	
	/**
	 * 物理磁盘
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskPhysicalPackage(SmisSession session){
		List<CIMInstance> physicalPackageInstances = new ArrayList<CIMInstance>();
		try {
			List<CIMInstance> arrayInstances = findArrayInstances(session);
			for (int i = 0; i < arrayInstances.size(); i++) {
			CIMInstance	array = arrayInstances.get(i);
			List<CIMInstance> ppInstances = session.getAssociatedInstances(array.getObjectPath(), "CIM_SystemPackaging", "CIM_PhysicalPackage", "Dependent", "Antecedent");
			if (ppInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取磁盘模块数为0，请确认支持smi-s协议或者具有磁盘模块信息！");
			}
			for (int j = 0; j < ppInstances.size(); j++) {
				physicalPackageInstances.add(ppInstances.get(j));
			}
			}
			logger.warn("获取到磁盘模块个数："+physicalPackageInstances.size());
			return physicalPackageInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findSoftwareIdentity(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> softwareIdentityInstances = null;
		try {
			softwareIdentityInstances = session.getAssociatedInstances(arrayCOP, "CIM_InstalledSoftwareIdentity", "CIM_SoftwareIdentity", "System", "InstalledSoftware");
			if (softwareIdentityInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取数据发生异常，请确认支持smi-s协议或者具有磁盘模块信息！");
			}
			return softwareIdentityInstances;
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
	 * 存储信息
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findStorageExtent(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> storageExtentInstances = null;
		try {
			storageExtentInstances = session.getAssociatedInstances(arrayCOP, "CIM_SystemDevice", "CIM_StorageExtent", "GroupComponent", "PartComponent");
			if (storageExtentInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取存储信息数为0，请确认支持smi-s协议或者具有存储信息！");
			}
			logger.warn("获取到存储信息个数："+storageExtentInstances.size());
			return storageExtentInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
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
	 * fileSystem
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findFileSystem(SmisSession session,CIMObjectPath ctlrCOP){
		List<CIMInstance> fileSysInstances = null;
		try {
			fileSysInstances = session.getAssociatedInstances(ctlrCOP, "Celerra_HostedFileSystem_DMCS_UFS", "Celerra_UxfsLocalFileSystem", "GroupComponent", "PartComponent");
			if (fileSysInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取文件系统数为0，请确认支持smi-s协议或者具有文件系统！");
			}
			return fileSysInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 网络端口
	 * @param session
	 * @param computerSystemCOP
	 * @return
	 */
	public static List<CIMInstance> findEthernetPort(SmisSession session,CIMObjectPath ctlrCOP){
		List<CIMInstance> ethernetPortInstances = null;
		try {
			ethernetPortInstances = session.getAssociatedInstances(ctlrCOP, "CIM_SystemDevice", "CIM_EthernetPort", "GroupComponent", "PartComponent");
			if (ethernetPortInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取网络端口数为0，请确认支持smi-s协议或者具有网络端口！");
			}
			return ethernetPortInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 网络端口
	 * @param session
	 * @param computerSystemCOP
	 * @return
	 */
	public static List<CIMInstance> findSASPort(SmisSession session,CIMObjectPath ctlrCOP){
		List<CIMInstance> sasPortInstances = null;
		try {
			sasPortInstances = session.getAssociatedInstances(ctlrCOP, "CIM_SystemDevice", "CIM_SASPort", "GroupComponent", "PartComponent");
			if (sasPortInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取网络端口数为0，请确认支持smi-s协议或者具有网络端口！");
			}
			return sasPortInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测EMCCelerra存储发生异常。异常信息："+e.getMessage());
		}
	}
	
}
