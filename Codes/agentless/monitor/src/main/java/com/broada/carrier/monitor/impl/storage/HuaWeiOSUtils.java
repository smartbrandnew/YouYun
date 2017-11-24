package com.broada.carrier.monitor.impl.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.impl.smis.SmisSession;

/**
 * 华为统一存储工具类
 * 用于获取设备集群列表等方法
 * 
 * @author shoulw 
 * Create By 2016-4-13 下午02:29:04
 */
public class HuaWeiOSUtils {
private static final Logger logger = LoggerFactory.getLogger(HuaWeiOSUtils.class);
	
	private HuaWeiOSUtils() {
		throw new RuntimeException("此工具类不允许构建实例");
	}
	
	public static List<CIMInstance> findDiskArrayInstances(SmisSession session) {
		List<CIMInstance> diskArrayInstances =  session.getInstancesByClass("HuaSy_ArrayChassis");
		return diskArrayInstances;
	}
	
	public static List<CIMInstance> findStorageSystemInstances(SmisSession session) {
		List<CIMInstance> diskArrayInstances =  session.getInstancesByClass("HuaSy_StorageSystem");
		return diskArrayInstances;
	}
	
	
	/**
	 * 查询阵列框下所有磁盘模块
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findDiskPackageInstances(SmisSession session) {
		List<CIMInstance> diskPackageInstances = new ArrayList<CIMInstance>();
		try {
			List<CIMInstance> diskArrayInstances = findDiskArrayInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findEnclosureChassis(session,diskArrayInstance.getObjectPath());
				
				for (int j = 0; j < enclosureInstances.size(); j++) {
					List<CIMInstance> dpInstances = session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "CIM_PackageInChassis", "CIM_PhysicalPackage", "GroupComponent", "PartComponent");
					for (int j2 = 0; j2 < dpInstances.size(); j2++) {
						diskPackageInstances.add(dpInstances.get(j2));
					}
				}
			}
			logger.warn("获取到磁盘模块个数："+diskPackageInstances.size());
			return diskPackageInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询阵列框下所有磁盘模块
	 * @param session
	 * @return
	 */
	public static List<Properties> findDiskPropertiesInstances(SmisSession session) {
		List<Properties> diskPackageInstances = new ArrayList<Properties>();
		try {
			List<CIMInstance> diskArrayInstances = findDiskArrayInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findEnclosureChassis(session,diskArrayInstance.getObjectPath());
				
				for (int j = 0; j < enclosureInstances.size(); j++) {
					List<CIMInstance> dpInstances = session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "HuaSy_PackageInChassis", "HuaSy_PhysicalPackage", "GroupComponent", "PartComponent");
					for (int j2 = 0; j2 < dpInstances.size(); j2++) {
						Properties pro = new Properties();
						CIMInstance diskPackage = dpInstances.get(j2);
						pro = diskPackageValue(diskPackage,pro);

						List<CIMInstance> diskProduct = session.getAssociatedInstances(diskPackage.getObjectPath(), "CIM_ProductPhysicalComponent", "CIM_Product", "PartComponent", "GroupComponent");
						if(diskProduct.size()==1)
						pro = diskProductValue(diskProduct.get(0),pro);
						List<CIMInstance> diskDriver = session.getAssociatedInstances(diskPackage.getObjectPath(), "CIM_Realizes", "CIM_DiskDrive", "Antecedent", "Dependent");
						if(diskDriver.size()==1){
						pro = diskDriverValue(diskDriver.get(0),pro);
						List<CIMInstance> diskExtent = session.getAssociatedInstances(diskDriver.get(0).getObjectPath(), "CIM_MediaPresent", "CIM_StorageExtent", "Antecedent", "Dependent");
						if(diskExtent.size()==1)
						pro = diskExtentValue(diskExtent.get(0),pro);
						}
						diskPackageInstances.add(pro);
					}
				}
			}
			logger.warn("获取到磁盘模块个数："+diskPackageInstances.size());
			return diskPackageInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 
	 * 将diskpackage数据置入properties中
	 */
	public static Properties diskPackageValue(CIMInstance diskPackage,Properties pro){
		pro.setProperty("Model", diskPackage.getProperty("Model").getValue().getValue().toString());
		pro.setProperty("SerialNumber", diskPackage.getProperty("SerialNumber").getValue().getValue().toString());
		pro.setProperty("ElementName", diskPackage.getProperty("ElementName").getValue().getValue().toString());
		return pro;
	}
	
	/**
	 * 
	 * 将diskProduct数据置入properties中
	 */
	public static Properties diskProductValue(CIMInstance diskProduct,Properties pro){
		pro.setProperty("IdentifyingNumber", diskProduct.getProperty("IdentifyingNumber").getValue().getValue().toString());
		pro.setProperty("Vendor", diskProduct.getProperty("Vendor").getValue().getValue().toString());
		pro.setProperty("Version", diskProduct.getProperty("Version").getValue().getValue().toString());
		return pro;
	}
	
	/**
	 * 
	 * 将diskDriver数据置入properties中
	 */
	public static Properties diskDriverValue(CIMInstance diskDriver,Properties pro){
		pro.setProperty("OperationalStatus", diskDriver.getProperty("OperationalStatus").getValue().getValue().toString());
		pro.setProperty("EnabledState", diskDriver.getProperty("EnabledState").getValue().getValue().toString());
		pro.setProperty("DiskType", diskDriver.getProperty("DiskType").getValue().getValue().toString());
		pro.setProperty("FormFactor", diskDriver.getProperty("FormFactor").getValue().getValue().toString());
		pro.setProperty("Encryption", diskDriver.getProperty("Encryption").getValue().getValue().toString());
		pro.setProperty("Caption", diskDriver.getProperty("Caption").getValue().getValue().toString());
		return pro;
	}
	
	/**
	 * 
	 * 将diskExtent数据置入properties中
	 */
	public static Properties diskExtentValue(CIMInstance diskExtent,Properties pro){
		long blockSize = Long.parseLong(diskExtent.getProperty("BlockSize").getValue().getValue().toString());
		long numberBlock = Long.parseLong(diskExtent.getProperty("NumberOfBlocks").getValue().getValue().toString());
		long capacity = blockSize*numberBlock;
		pro.setProperty("Capacity", Long.toString(capacity));
		pro.setProperty("ConsumableBlocks", diskExtent.getProperty("ConsumableBlocks").getValue().getValue().toString());
		pro.setProperty("Primordial", diskExtent.getProperty("Primordial").getValue().getValue().toString());
		return pro;
	}
	
	/**
	 * 查询特定磁盘模块下磁盘产品信息
	 * @param session
	 * @return
	 */
	public static CIMInstance findDiskProduct(SmisSession session,CIMObjectPath diskPackage) {
		List<CIMInstance> diskProductInstances = null;
		try {
					diskProductInstances = session.getAssociatedInstances(diskPackage, "CIM_ProductPhysicalComponent", "CIM_Product", "PartComponent", "GroupComponent");
			if (diskProductInstances.size() == 0 ) {
				throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。获取到阵列产品信息为空！");
			}
			logger.warn("获取到磁盘产品信息个数："+diskProductInstances.size());
			if(diskProductInstances.size()==1)
			return diskProductInstances.get(0);
			return null;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询特点磁盘模块下所有磁盘
	 * @param session
	 * @return
	 */
	public static CIMInstance findDiskDriver(SmisSession session,CIMObjectPath diskPackage) {
		List<CIMInstance> diskDriverInstances = null;
		try {
				diskDriverInstances = session.getAssociatedInstances(diskPackage, "CIM_Realizes", "CIM_DiskDrive", "Antecedent", "Dependent");
			if (diskDriverInstances.size() == 0 ) {
				throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。");
			}
			logger.warn("获取到磁盘个数："+diskDriverInstances.size());
			if(diskDriverInstances.size()==1)
			return diskDriverInstances.get(0);
			return null;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询特点磁盘下所有磁盘原始存储信息
	 * @param session
	 * @return
	 */
	public static CIMInstance findDiskExtent(SmisSession session,CIMObjectPath diskDriver) {
		List<CIMInstance> diskExtentInstances = null;
		try {
			 diskExtentInstances = session.getAssociatedInstances(diskDriver, "CIM_MediaPresent", "CIM_StorageExtent", "Antecedent", "Dependent");
			if (diskExtentInstances.size() == 0 ) {
				throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。");
			}
			if(diskExtentInstances.size()==1)
			return diskExtentInstances.get(0);
			return null;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询阵列框下磁盘框/控制框
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findEnclosureChassis(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "CIM_Container", "CIM_Chassis", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询阵列框下存储卷
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findStorageVolume(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "HuaSy_SystemDevice", "HuaSy_StorageVolume", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询阵列框下存储池
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findStoragePool(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "CIM_HostedStoragePool", "SNIA_StoragePool", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询阵列框下存储池
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findFileSystem(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "Huasy_HostedFileSystem", "HuaSy_LocalFileSystem", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询阵列框下逻辑磁盘
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findLogicalDisk(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "HuaSy_SystemDevice", "HuaSy_LogicalDisk", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询阵列框下控制器
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findController(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "CIM_ComponentCS", "HuaSy_StorageControllerSystem", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询阵列框下控制器
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findBlockStatisticsService(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "HuaSy_HostedService", "HuaSy_BlockStatisticsService", "GroupComponent", "PartComponent");
	}

	
	/**
	 * 查询阵列框下逻辑磁盘
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findLogicDisk(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "HuaSy_SystemDevice", "HuaSy_LogicDisk", "GroupComponent", "PartComponent");
	}
	
	

	
	
	/**
	 * 查询阵列框下存储卷
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findStorageVolumeInstances(SmisSession session) {
		List<CIMInstance> storageVolumeInstances = new ArrayList<CIMInstance>();;
		try {
			List<CIMInstance> diskArrayInstances = findStorageSystemInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findStorageVolume(session,diskArrayInstance.getObjectPath());
				for (int j = 0; j < enclosureInstances.size(); j++) {
					storageVolumeInstances.add(enclosureInstances.get(j));
				}
			}
			logger.warn("获取到逻辑磁盘模块个数："+storageVolumeInstances.size());
			return storageVolumeInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询阵列框下存储池
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findStoragePoolInstances(SmisSession session) {
		List<CIMInstance> storagePoolInstances = new ArrayList<CIMInstance>();;
		try {
			List<CIMInstance> diskArrayInstances = findStorageSystemInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findStoragePool(session,diskArrayInstance.getObjectPath());
				for (int j = 0; j < enclosureInstances.size(); j++) {
					storagePoolInstances.add(enclosureInstances.get(j));
				}
			}
			return storagePoolInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询阵列框下文件系统
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findFileSystemInstances(SmisSession session) {
		List<CIMInstance> fileSystemInstances = new ArrayList<CIMInstance>();;
		try {
			List<CIMInstance> diskArrayInstances = findStorageSystemInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findFileSystem(session,diskArrayInstance.getObjectPath());
				for (int j = 0; j < enclosureInstances.size(); j++) {
					fileSystemInstances.add(enclosureInstances.get(j));
				}
			}
			return fileSystemInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询阵列框下逻辑磁盘
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findLogicalDiskInstances(SmisSession session) {
		List<CIMInstance> logicalDiskInstances = new ArrayList<CIMInstance>();;
		try {
			List<CIMInstance> diskArrayInstances = findStorageSystemInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findLogicalDisk(session,diskArrayInstance.getObjectPath());
				for (int j = 0; j < enclosureInstances.size(); j++) {
					logicalDiskInstances.add(enclosureInstances.get(j));
					session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "HuaSy_SystemDevice", "HuaSy_FrontEndFCPort", "GroupComponent", "PartComponent");
				}
			}
			return logicalDiskInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询阵列框下控制器
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findControllerInstances(SmisSession session) {
		List<CIMInstance> controllerInstances = new ArrayList<CIMInstance>();;
		try {
			List<CIMInstance> diskArrayInstances = findStorageSystemInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findController(session,diskArrayInstance.getObjectPath());
				for (int j = 0; j < enclosureInstances.size(); j++) {
					controllerInstances.add(enclosureInstances.get(j));
				}
			}
			return controllerInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 查询控制器下前端FC端口
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findFCPortInstances(SmisSession session) {
		List<CIMInstance> fcPortInstances = new ArrayList<CIMInstance>();
		try {
			List<CIMInstance> diskArrayInstances = findStorageSystemInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findController(session,diskArrayInstance.getObjectPath());
				for (int j = 0; j < enclosureInstances.size(); j++) {
					List<CIMInstance>	fpInstances = session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "HuaSy_SystemDevice", "HuaSy_FrontEndFCPort", "GroupComponent", "PartComponent");
					for (int k = 0; k < fpInstances.size(); k++) {
						fcPortInstances.add(fpInstances.get(k));
					}
				}
				
			}
			return fcPortInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
		
	}
	
	

	/**
	 * 查询控制器下网络端口
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findNetWorkPortInstances(SmisSession session) {
		List<CIMInstance> fcPortInstances = new ArrayList<CIMInstance>();;
		try {
			List<CIMInstance> diskArrayInstances = findStorageSystemInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findController(session,diskArrayInstance.getObjectPath());
				for (int j = 0; j < enclosureInstances.size(); j++) {
					List<CIMInstance>	fpInstances = session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "HuaSy_SystemDevice", "HuaSy_EthernetPort", "GroupComponent", "PartComponent");
					for (int k = 0; k < fpInstances.size(); k++) {
						logger.warn("通过"+enclosureInstances.get(j).getProperty("ElementName")+"获取到网络端口"+fpInstances.size()+"个。");
						fcPortInstances.add(fpInstances.get(k));
					}
				}
				
			}
			logger.warn("通过控制器获取到网络端口"+fcPortInstances.size()+"个。");
			return fcPortInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
		
	}

	
	/**
	 * 查询阵列框下风扇模块
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findFanInstances(SmisSession session) {
		List<CIMInstance> fanPackageInstances = new ArrayList<CIMInstance>();
		try {
			List<CIMInstance> diskArrayInstances = findDiskArrayInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findEnclosureChassis(session,diskArrayInstance.getObjectPath());
				
				for (int j = 0; j < enclosureInstances.size(); j++) {
					List<CIMInstance> dpInstances = session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "HuaSy_PackageInChassis", "HuaSy_Battery", "GroupComponent", "PartComponent");
					for (int j2 = 0; j2 < dpInstances.size(); j2++) {
						fanPackageInstances.add(dpInstances.get(j2));
					}
				}
			}
			logger.warn("获取到风扇模块个数："+fanPackageInstances.size());
			return fanPackageInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 
	 * 
	 */
	public static List<CIMInstance> findFan(SmisSession session,CIMObjectPath objectpath){
		List<CIMInstance> fan =  session.getAssociatedInstances(objectpath, "HuaSy_Realizes", "HuaSy_Fan", "Antecedent", "Dependent");
		return fan;
	}
	
	
	/**
	 * 查询阵列框下电池模块
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findBatteryInstances(SmisSession session) {
		List<CIMInstance> diskPackageInstances = new ArrayList<CIMInstance>();
		try {
			List<CIMInstance> diskArrayInstances = findDiskArrayInstances(session);
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = findEnclosureChassis(session,diskArrayInstance.getObjectPath());
				
				for (int j = 0; j < enclosureInstances.size(); j++) {
					List<CIMInstance> dpInstances = session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "CIM_PackageInChassis", "CIM_BatteryPackage", "GroupComponent", "PartComponent");
					for (int j2 = 0; j2 < dpInstances.size(); j2++) {
						diskPackageInstances.add(dpInstances.get(j2));
					}
				}
			}
			logger.warn("获取到电池模块个数："+diskPackageInstances.size());
			return diskPackageInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 
	 * 
	 */
	public static List<CIMInstance> findBattery(SmisSession session,CIMObjectPath objectpath){
		List<CIMInstance> battery =  session.getAssociatedInstances(objectpath, "HuaSy_Realizes", "HuaSy_Battery", "Antecedent", "Dependent");
		return battery;
	}
	
	/**
	 * 查询阵列框下电源模块
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findPowerSupplyInstances(SmisSession session) {
		List<CIMInstance> powerPackageInstances = new ArrayList<CIMInstance>();
		try {
			List<CIMInstance> diskArrayInstances = session.getInstancesByClass("HuaSy_ArrayProduct");
			for (int i = 0; i < diskArrayInstances.size(); i++) {
				CIMInstance diskArrayInstance = diskArrayInstances.get(i);
				List<CIMInstance> enclosureInstances = session.getAssociatedInstances(diskArrayInstance.getObjectPath(), "HuaSy_ProductPhysicalComponent", "HuaSy_EnclosureChassis", "GroupComponent", "PartComponent");
				//List<CIMInstance> enclosureInstances = session.getAssociatedInstances(enclosureInstances1.get(0).getObjectPath(), "HuaSy_PackageInChassis", "HuaSy_PowerPackage", "GroupComponent", "PartComponent");
				
				for (int j = 0; j < enclosureInstances.size(); j++) {
					List<CIMInstance> dpInstances = session.getAssociatedInstances(enclosureInstances.get(j).getObjectPath(), "HuaSy_PackageInChassis", "HuaSy_PowerPackage", "GroupComponent", "PartComponent");
					for (int j2 = 0; j2 < dpInstances.size(); j2++) {
						powerPackageInstances.add(dpInstances.get(j2));
					}
				}
			}
			logger.warn("获取到电源模块个数："+powerPackageInstances.size());
			return powerPackageInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测HuaWei存储。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 
	 * 
	 */
	public static List<CIMInstance> findPower(SmisSession session,CIMObjectPath objectpath){
		
		List<CIMInstance> power =  session.getAssociatedInstances(objectpath, "HuaSy_Realizes", "HuaSy_PowerSupply", "Antecedent", "Dependent");
		return power;
	}
	
}
