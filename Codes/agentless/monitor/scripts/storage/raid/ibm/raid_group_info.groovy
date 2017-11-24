/*!Action
 action.name=通过smi-s协议监测raid组
 action.descr=通过smi-s协议监测raid组
 action.protocols=smis
 monitor.output=raid-group-info
 monitor.priority=100
*/

import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocolType;

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		
		def diskDriverInsts = $smis.getAssociatedInstances(clusterCOP, 'IBMTSSVC_StorageSystemToDiskDrive', 'IBMTSSVC_DiskDrive', 'GroupComponent', 'PartComponent');
		def lunInstances = $smis.getAssociatedInstances(clusterCOP, 'IBMTSSVC_StorageExtentOnCluster', 'IBMTSSVC_BackendVolume', 'GroupComponent', 'PartComponent');
		lunInstances.each{lunInst->
			def lunCOP = lunInst.getObjectPath();
			def lunName = $smis.getProperty(lunInst, 'ElementName');
			
			def result = $result.create(lunName);
			result.clazz = 'RAIDGroup';
			
			def capacity = $unit.b2GB($smis.getProperty(lunInst, 'Capacity'), 3);
			result.attr.capacity = capacity;
			def mode = $smis.getProperty(lunInst, 'Mode');
			def poolId = $smis.getProperty(lunInst, 'PoolID');
			def poolName = $smis.getProperty(lunInst, 'Poolname');
			def nodeName = $smis.getProperty(lunInst, 'NodeName');
			def deviceId = $smis.getProperty(lunInst, 'DeviceID');
			result.attr.storResCode = poolId + "/" + deviceId;
			def consumableBlocks = $smis.getProperty(lunInst, 'ConsumableBlocks');
			def blockSize = $smis.getProperty(lunInst, 'BlockSize');
			def usedCapacity = $unit.b2GB(consumableBlocks * blockSize, 3)
			
			def diskNum = 0;
			for (diskDriverInst in diskDriverInsts) {
				def property = diskDriverInst.getProperty("MdiskID");
				def value = property.getValue();
				if (value == null) {
					continue;
				}

				def mdiskID = $smis.getProperty(diskDriverInst, 'MdiskID', "");
				if (deviceId.equals(mdiskID+"")) {
					diskNum ++;
				}
			}
			
			//def arrayInstances = $smis.getAssociatedInstances(lunCOP, 'IBMTSSVC_ArrayIsABackendVolume', 'IBMTSSVC_Array', 'SameElement', 'SystemElement');
			def arrayInstances = $smis.getAssociatedInstances(lunCOP, 'IBMTSSVC_ArrayIsABackendVolume', 'IBMTSSVC_Array',  'SystemElement', 'SameElement');
			
			for (arrayInst in arrayInstances) {
				def cimProperty = arrayInst.getProperty('RaidLevel');
				def cimValue = cimProperty.getValue();
				result.attr.raidLev = cimValue.getValue()[0]
				break;
			}
			
			result.attr.driverNum = diskNum;
			result.perf.stor_use.capacity_unused = capacity - usedCapacity;
			result.perf.stor_use.capacity_useage = NumberContext.round((usedCapacity/capacity)*100, 2);
			result.perf.stor_use.capacity_used = usedCapacity;
			def status = svcUtil.getOperationalStatus($smis.getProperty(lunInst, 'OperationalStatus')[0]);
			result.state.available_status = (status == 'OK' ? 1 : 0);
		}
	}
}