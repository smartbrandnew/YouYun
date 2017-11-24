package com.broada.carrier.monitor.impl.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.impl.smis.SmisSession;

public class IBMSVCUtils {
	private static final Logger logger = LoggerFactory.getLogger(IBMSVCUtils.class);
	
	private IBMSVCUtils() {
		throw new RuntimeException("此工具类不允许构建实例");
	}
	
	/**
	 * TODO 此处计算方式
	 * 根据集群节点获得所有的磁盘和所有MDisk,根据RaidGroup获得所有的节点，
	 * 一个磁盘对应一个节点和一个MDisk
	 * 根据上面的关系，遍历所有的节点，在节点里遍历所有磁盘，根据磁盘里关联的节点名称与节点匹配，若匹配到节点则匹配磁盘关联的mDisk,最后取出mDiks的RaidLevel
	 * @param session
	 * @param ioGroupCOB
	 * @param clusterCOP
	 * @return
	 */
	public static String findRaidLevel(SmisSession session, CIMObjectPath ioGroupCOB, CIMObjectPath clusterCOP) {
		List<CIMInstance> diskInstances = findDiskDriverByCluster(session, clusterCOP);
		List<CIMInstance> nodeInstances = session.getAssociatedInstances(ioGroupCOB, "IBMTSSVC_NodeComponentOfIOGroup", "IBMTSSVC_Node", "GroupComponent", "PartComponent");
		List<CIMInstance> arrayInstances = findMDiskInstancesByCluster(session, clusterCOP);
		logger.debug("raid组过滤前，节点数和磁盘数及MDisk分别为：{}, {}, {}", new Object[] {nodeInstances.size(), diskInstances.size(), arrayInstances.size()});
		for (CIMInstance nodeInst : nodeInstances) {
			String nodeName = (String) session.getProperty(nodeInst, "ElementName");
			for (CIMInstance diskInst : diskInstances) {
				String diskOfNode = (String)session.getProperty(diskInst, "NodeName");
				String diskOfMDisk = (String)session.getProperty(diskInst, "MdiskName");
				if (StringUtils.equals(nodeName, diskOfNode)) {
					for (CIMInstance arrayInst : arrayInstances) {
						String mDiskName = (String) session.getProperty(arrayInst, "ElementName");
						String mDiskName2 = (String) session.getProperty(arrayInst, "Name");
						if (StringUtils.equals(mDiskName, diskOfMDisk) || StringUtils.equals(mDiskName2, diskOfMDisk)) {
							return (String) session.getProperty(arrayInst, "RaidLevel");
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 查询集群下的所有NODE实例
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findNodeInstanceByCluster(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_NodeComponentOfCluster", "IBMTSSVC_Node", "GroupComponent", "PartComponent");
	}
	
	public static Map<String, Object> findCapacityOfRaidGroup(SmisSession session, CIMObjectPath ioGroupCOP, CIMObjectPath clusterCOP) {
		List<CIMInstance> diskInstances = findDiskDriverByCluster(session, clusterCOP);
		List<CIMInstance> nodeInstances = session.getAssociatedInstances(ioGroupCOP, "IBMTSSVC_NodeComponentOfIOGroup", "IBMTSSVC_Node","GroupComponent", "PartComponent");
		logger.debug("raid组过滤前，节点数和磁盘数分别为：{}, {}", nodeInstances.size(), diskInstances.size());
		int diskNum = 0;
		long capacity = 0;
		for (CIMInstance nodeInst : nodeInstances) {
			String nodeName = (String) session.getProperty(nodeInst, "ElementName");
			for (CIMInstance diskInst : diskInstances) {
				String diskOfNode = (String)session.getProperty(diskInst, "NodeName");
				long diskCapacity = (Long) session.getProperty(diskInst, "Capacity");
				if (StringUtils.equals(nodeName, diskOfNode)) {
					diskNum++;
					capacity += diskCapacity;
				}
			}
		}
		HashMap<String, Object> diskMap = new HashMap<String, Object>();
		diskMap.put("diskNum", diskNum);
		diskMap.put("capacity", capacity);
		logger.debug("raid组过滤后，节点数和磁盘数分别为：{}, {}", capacity, diskNum);
		return diskMap;
	}
	
	/**
	 * 查询集群下的所有磁盘
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskDriverByCluster(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_StorageSystemToDiskDrive", "IBMTSSVC_DiskDrive", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询集群的所有mdisk
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findMDiskInstancesByCluster(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_ArrayOnCluster", "IBMTSSVC_Array","GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询集群列表
	 * @param session
	 * @return
	 */
	public static List<CIMInstance> findClusterInstances(SmisSession session) {
		logger.debug("开始存储监控采集！");
		try {
			List<CIMInstance> clusterInstances = session.getInstancesByClass("IBMTSSVC_Cluster");
			if (clusterInstances.size() == 0 ) {
				throw new UnsupportedOperationException("不支持通过smi-s协议来监测IBM存储。");
			}
			return clusterInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("不支持通过smi-s协议来监测IBM存储。");
		}
	}
	
	/**
	 * 查询集群下的所有存储卷
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findVdiskInstancesByCluster(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_StorageVolumeOnCluster", "IBMTSSVC_StorageVolume","GroupComponent", "PartComponent");
	}

	/**
	 * 查询硬盘性能实例
	 * @param session
	 * @param diskCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskDriverStatsInstances(SmisSession session, CIMObjectPath diskCOP) {
		return session.getAssociatedInstances(diskCOP, "IBMTSSVC_DiskDriveStatisticalData", "IBMTSSVC_DiskDriveStatistics","ManagedElement", "Stats");
	}

	
	/**
	 * 查询集群下的所有以太网端口实例
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findEthernetPortByCluster(SmisSession session, CIMObjectPath clusterCOP) {
		List<CIMInstance> ethernetPorts = new ArrayList<CIMInstance>();
		List<CIMInstance> nodeInstances = findNodeInstanceByCluster(session, clusterCOP);
		for (CIMInstance nodeInst : nodeInstances) {
			CIMObjectPath nodeCOP = nodeInst.getObjectPath();
			List<CIMInstance> ethernetPortInstances = findEthernetPortByNode(session, nodeCOP);
			ethernetPorts.addAll(ethernetPortInstances);
		}
		return ethernetPorts;
	}
	
	/**
	 * 查询集群下的所有FC端口实例
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findFCPortByCluster(SmisSession session, CIMObjectPath clusterCOP) {		
		return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_PortsOnCluster", "IBMTSSVC_FCPort", "Antecedent", "Dependent");
	}
	
	/**
	 * 查询节点上的所有以太网端口实例
	 * @param session
	 * @param nodeCOP
	 * @return
	 */
	public static List<CIMInstance> findEthernetPortByNode(SmisSession session, CIMObjectPath nodeCOP) {
		return session.getAssociatedInstances(nodeCOP, "IBMTSSVC_SystemEthernetPort", "IBMTSSVC_EthernetPort", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询节点上的所有FC端口实例
	 * @param session
	 * @param nodeCOP
	 * @return
	 */
	public static List<CIMInstance> findFCPortByNode(SmisSession session, CIMObjectPath nodeCOP) {
		return session.getAssociatedInstances(nodeCOP, "IBMTSSVC_SystemFCPort", "IBMTSSVC_FCPort", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询集群下的所有IOGroup实例
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findIOGroupByCluster(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_IOGroupComponentOfCluster", "IBMTSSVC_IOGroup", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询IOGroup下的所有存储卷
	 * @param session
	 * @param ioGroupCOP
	 * @return
	 */
	public static List<CIMInstance> findVolumeByIOGroup(SmisSession session, CIMObjectPath ioGroupCOP) {
		return session.getAssociatedInstances(ioGroupCOP, "IBMTSSVC_StorageVolumeOnIOGroup", "IBMTSSVC_StorageVolume", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 查询集群下的所有MDisk实例
	 * @param session
	 * @param clusterCOP
	 * @return
	 */
	public static List<CIMInstance> findArrayByCluster(SmisSession session, CIMObjectPath clusterCOP) {
		return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_ArrayOnCluster", "IBMTSSVC_Array", "GroupComponent", "PartComponent");
	}
	
	/**
	 * 统计FC端口信息
	 * @param session
	 * @param fcPortCOP
	 * @return
	 */
	public static CIMInstance findFCPortStatByCluster(SmisSession session, CIMObjectPath fcPortCOP) {
		List<CIMInstance> fcPortStatInstances = session.getAssociatedInstances(fcPortCOP, "IBMTSSVC_FCPortStatisticalData", "IBMTSSVC_FCPortStatistics", "ManagedElement", "Stats");
		return fcPortStatInstances.isEmpty() ? null : fcPortStatInstances.get(0);
	}
}
