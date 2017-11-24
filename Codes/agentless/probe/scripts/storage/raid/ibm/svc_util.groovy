/*!Action
 action.name=IBM存储监测工具类
 action.descr=IBM存储监测工具类
 action.protocols=smis
*/

import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;

def getPackageType(def type) {
	switch (type) {
		case 0:
			return 'Unknown';
		case 1:
			return 'Other';
		case 2:
			return 'Rack';
		case 3:
			return 'Chassis/Frame';
		case 4:
			return 'Cross Connect/Backplane';
		case 5:
			return 'Container/Frame Slot';
		case 6:
			return 'Power Supply';
		case 7:
			return 'Fan';
		case 8:
			return 'Sensor';
		case 9:
			return 'Module/Card';
		case 10:
			return 'Port/Connector';
		case 11:
			return 'Battery';
		case 12:
			return 'Processor';
		case 13:
			return 'Memory';
		case 14:
			return 'Power Source/Generator';
		case 15:
			return 'Storage Media Package (e.g., Disk or Tape Drive)';
		case 16:
			return 'Blade';
		case 17:
			return 'Blade Expansion';
	}
}

def getEnclosureStatus(def type) {
	switch (type) {
		case 0:
			return 'online';
		case 1:
			return 'offline';
		case 2:
			return 'degraded';
	}
}

def String getOperationalStatus(int index) {
	String operationalStatus = null;
	switch (index) {
	case 0:
		operationalStatus = "Unknown";
		break;
	case 1:
		operationalStatus = "Other";
		break;
	case 2:
		operationalStatus = "OK";
		break;
	case 3:
		operationalStatus = "Degraded or Predicted Failure";
		break;
	case 4:
		operationalStatus = "Stressed";
		break;
	case 5:
		operationalStatus = "Predictive Failure";
		break;
	case 6:
		operationalStatus = "Error";
		break;
	case 7:
		operationalStatus = "Non-Recoverable Error";
		break;
	case 8:
		operationalStatus = "Starting";
		break;
	case 9:
		operationalStatus = "Stopping";
		break;
	case 10:
		operationalStatus = "Stopped";
		break;
	case 11:
		operationalStatus = "In Service";
		break;
	case 12:
		operationalStatus = "No Contact";
		break;
	case 13:
		operationalStatus = "Lost Communication";
		break;
	case 14:
		operationalStatus = "Aborted";
		break;
	case 15:
		operationalStatus = "Dormant";
		break;
	case 16:
		operationalStatus = "Supporting Entity In Error";
		break;
	case 17:
		operationalStatus = "Completed";
		break;
	case 18:
		operationalStatus = "Power Mode";
		break;
	case 19:
		operationalStatus = "Flushing";
		break;
	case 32768:
		operationalStatus = "Removed";
		break;
	case 32769:
		operationalStatus = "Online";
		break;
	case 32770:
		operationalStatus = "Offline";
		break;
	case 32771:
		operationalStatus = "Rebooting";
		break;
	case 32772:
		operationalStatus = "Success";
		break;
	case 32773:
		operationalStatus = "Failure";
		break;
	case 32774:
		operationalStatus = "Write Disabled";
		break;
	case 32775:
		operationalStatus = "Write Protected";
		break;
	case 32776:
		operationalStatus = "Not Ready";
		break;
	case 32777:
		operationalStatus = "Power Saving Mode";
		break;
	default:
		operationalStatus = "Not Available";
	}
	return operationalStatus;
}


def getDiskType(int type) {
	String diskType = null;
	switch(type){
		case 0:
		diskType = 'sas_ssd';
		break;
		case 1:
		diskType = 'sas_hdd';
		break;
		case 2:
		diskType = 'sas_nearline_hdd';
		break;
	}
}

def getPortType(def portTypeIndex) {
	def portType = null;
	switch(portTypeIndex) {
		case 0:
			portType = 'Unknown';
		case 1:
			portType = 'Other';
			break;
		case 2:
			portType = 'iSCSI Port';
			break;
		case 50:
			portType = '10BaseT';
			break;
		case 51:
			portType = '10-100BaseT';
			break;
		case 52:
			portType = '100BaseT';
			break;
		case 53:
			portType = '1000BaseT';
			break;
		case 54:
			portType = '2500BaseT';
			break;
		case 55:
			portType = '10GBaseT';
			break;
		case 56:
			portType = '10GBase-CX4';
			break;
		case 100:
			portType = '100Base-FX';
			break;
		case 101:
			portType = '100Base-SX';
			break;
		case 102:
			portType = '1000Base-SX';
			break;
		case 103:
			portType = '1000Base-LX';
			break;
		case 104:
			portType = '1000Base-CX';
			break;
		case 105:
			portType = '10GBase-SR';
			break;
		case 106:
			portType = '10GBase-SW';
			break;
		case 107:
			portType = '10GBase-LX4';
			break;
		case 108:
			portType = '10GBase-LR';
			break;
		case 109:
			portType = '10GBase-LW';
			break;
		case 110:
			portType = '10GBase-ER';
			break;
		case 111:
			portType = '10GBase-EW';
			break;
		case 16000..65535:
			portType = 'Vendor Reserved';
			break;
		
	}
}

def getFCPortType(def portTypeIndex) {
	def portType = null;
	switch(portTypeIndex) {
		case 0:
			portType = 'Unknown';
		case 1:
			portType = 'Other';
			break;
		case 10:
			portType = 'N';
			break;
		case 11:
			portType = 'NL';
			break;
		case 12:
			portType = 'F/NL';
			break;
		case 13:
			portType = 'Nx';
			break;
		case 14:
			portType = 'E';
			break;
		case 15:
			portType = 'F';
			break;
		case 16:
			portType = 'FL';
			break;
		case 17:
			portType = 'B';
			break;
		case 18:
			portType = 'G';
			break;
		case 16000..65535:
			portType = 'Vendor Reserved';
			break;
		
	}
}

def fetchDiskPhysicalPackage(def smisession, def cop, Properties prop) throws Exception{
	def instances = smisession.getAssociatedInstances(cop, "IBMTSSVC_DiskDrive", "IBMTSSVC_DiskDrivePackage", "Dependent", "Antecedent");
	for (cimInstance in instances) {
	  prop.setProperty("SerialNumber", smisession.getProperty("SerialNumber"));
	  prop.setProperty("Manufacturer", smisession.getProperty("Manufacturer"));
	  prop.setProperty("Model", smisession.getProperty("Model"));
	  prop.setProperty("Description", smisession.getProperty("Tag"));
	}
	return prop;
}

def findStoragePoolAttr(def session, def storagePoolName, def clusterCOP){
	int diskNum = 0;
	String raidLevel = null;
	long capacity = 0L;
	def arrayInstances = IBMSVCUtils.findMDiskInstancesByCluster(session, clusterCOP);
	for (mdiskInstance in arrayInstances){
	  def mdiskCOP = mdiskInstance.getObjectPath();
	  String poolName = (String)session.getProperty(mdiskInstance, "Poolname");
	  if (storagePoolName.equals(poolName)){
		def cimProperty = mdiskInstance.getProperty('RaidLevel');
		raidLevel = cimProperty.getValue().getValue()[0];
		capacity += ((Long)session.getProperty(mdiskInstance, "Capacity")).longValue();
		def diskDriverExtends = findDiskDriverByMdisk(session, mdiskCOP);
		diskNum += diskDriverExtends.size();
	  }
	}
	Map<String, Object> poolAttrMap = new HashMap();
	poolAttrMap.put("diskNum", Integer.valueOf(diskNum));
	poolAttrMap.put("raidLevel", raidLevel);
	poolAttrMap.put("capacity", Long.valueOf(capacity));
	return poolAttrMap;
}
 
def findDiskDriverByMdisk(def session, def mdiskCOP) {
	return session.getAssociatedInstances(mdiskCOP, "IBMTSSVC_ArrayBasedOnDiskDrive", "IBMTSSVC_DiskDriveExtent", "Dependent", "Antecedent");
}

def findStoragePoolAttr(def session, def clusterCOP) throws Exception {
	Map<String, Object> attrMap = new HashMap();
	def storagePoolInstances = SMIArrayUtils.fetchStoragePoolInstances(session, clusterCOP);
	for (poolInst in storagePoolInstances){
		def poolName = session.getProperty(poolInst, "Caption");
		def storagePoolAttr = findStoragePoolAttr(session, poolName, clusterCOP);
		attrMap.put(poolName, storagePoolAttr);
	}
	return attrMap;
}

def findEnclosureByCluster(def session, def clusterCOP){
	return session.getAssociatedInstances(clusterCOP, "IBMTSSVC_EnclosurePackage", "CIM_ManagedElement", "Antecedent", "Dependent");
}

	def findRaidLevel(def session, def ioGroupCOB, def clusterCOP) {
		def diskInstances = IBMSVCUtils.findDiskDriverByCluster(session, clusterCOP);
		def nodeInstances = session.getAssociatedInstances(ioGroupCOB, "IBMTSSVC_NodeComponentOfIOGroup", "IBMTSSVC_Node", "GroupComponent", "PartComponent");
		def arrayInstances = IBMSVCUtils.findMDiskInstancesByCluster(session, clusterCOP);
		for (def nodeInst : nodeInstances) {
			String nodeName = (String) session.getProperty(nodeInst, "ElementName");
			for (def diskInst : diskInstances) {
				String diskOfMDisk = (String)session.getProperty(diskInst, "MdiskName");
				for (def arrayInst : arrayInstances) {
					String mDiskName = (String) session.getProperty(arrayInst, "ElementName");
					String mDiskName2 = (String) session.getProperty(arrayInst, "Name");
					if (mDiskName == diskOfMDisk || mDiskName2==diskOfMDisk) {
						def cimProperty = arrayInst.getProperty('RaidLevel');
						def cimValue = cimProperty.getValue();
						return cimValue.getValue()[0]
					}
				}
			}
		}
		return null;
	}

	def findCapacityOfRaidGroup(def session, def ioGroupCOP) {
		def volumeInstances = session.getAssociatedInstances(ioGroupCOP, "IBMTSSVC_IOGroupDeviceVolumeView", "IBMTSSVC_VolumeView","GroupComponent", "PartComponent");
		long capacity = 0;
		for (def volumeInst : volumeInstances) {
			def blockSize = session.getProperty(volumeInst, 'SVBlockSize');
			capacity += $unit.b2GB(blockSize * session.getProperty(volumeInst, 'SVNumberOfBlocks'), 3)
		}
		return capacity;
	}

  