/*!Action
 action.name=通过smi-s协议监测磁盘阵列
 action.descr=通过smi-s协议监测磁盘阵列
 action.protocols=smis
 monitor.output=raid-info
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils;

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusters = $smis.getInstancesByClass('IBMTSSVC_Cluster');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);

	clusterInstances.each{clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def arrayProps = new Properties();
		
		def clusterName = $smis.getProperty(clusterInst, 'ElementName');
		if (clusterName == null) {
			return;
		}
		SMIArrayUtils.fetchPhysicalPackage($smis, clusterCOP, arrayProps);
		SMIArrayUtils.fetchSoftwareIdentity($smis, clusterCOP, arrayProps);
		
		def result = $result.create(clusterName);
		result.attr.transferSpeed = $smis.getProperty(clusterInst, 'FcPortSpeed');
		if (clusterName.contains("v7000")) {
			result.attr.cacheSize = 16;
		} else if (clusterName.contains("v3700")) {
			result.attr.cacheSize = 4;
		}
		result.attr.ipAddr = $smis.getProperty(clusterInst, 'ConsoleIP');
		def codeLevel = $smis.getProperty(clusterInst, 'CodeLevel');
		
		def ddInstances = SMIArrayUtils.fetchDiskDriverInstances($smis, clusterCOP);
		result.attr.driverNum = ddInstances.size();
		result.attr.name = clusterName;
		def firmwareVersion = arrayProps.VersionString;
		if ((firmwareVersion == null) || (firmwareVersion.equalsIgnoreCase('Not Available'))){
			firmwareVersion = codeLevel;
		}
		result.attr.brand = 'IBM';
		result.attr.fwOSName = firmwareVersion;
		
		result.attr.sequenceNum = arrayProps.SerialNumber;
		def totalCapacity = $unit.b2GB($smis.getProperty(clusterInst, 'BackendStorageCapacity'), 3);
		def usedCapacity = $unit.b2GB($smis.getProperty(clusterInst, "TotalUsedCapacity"), 3);
		result.perf.stor_use.capacity_used = usedCapacity;
		result.perf.stor_use.capacity_unused = totalCapacity - usedCapacity;
		result.perf.stor_use.capacity_useage = NumberContext.round((usedCapacity * 100) / totalCapacity, 2);
		result.attr.capacity = totalCapacity;
		def arrayInstances = IBMSVCUtils.findArrayByCluster($smis, clusterCOP);
		for (arrayInst in arrayInstances) {
			def cimProperty = arrayInst.getProperty('RaidLevel');
			def cimValue = cimProperty.getValue();
			result.attr.raidLev = cimValue.getValue()[0]
			break;
		}
		result.attr.interfaceType = 'Fibre Channel, Ethernet';
		def operationalStatus = $smis.getProperty(clusterInst, 'OperationalStatus');
		def status = svcUtil.getOperationalStatus(operationalStatus[0]);
		result.state.available_status = (status == 'OK' ? 1 : 0);
	};

}
