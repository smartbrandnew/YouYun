/*!Action
 action.name=通过smi-s协议监测LUN
 action.descr=通过smi-s协议监测LUN
 action.protocols=smis
 monitor.output=IBMSVC-LUN-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each{clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def clusterName = $smis.getProperty(clusterInst, 'ElementName');
		if (clusterName == null) {
			return;
		}

		def lunInstances = $smis.getAssociatedInstances(clusterCOP, 'IBMTSSVC_StorageExtentOnCluster', 'IBMTSSVC_BackendVolume', 'GroupComponent', 'PartComponent');
		lunInstances.each{lunInst->
			def lunCOP = lunInst.getObjectPath();
			def lunName = $smis.getProperty(lunInst, 'ElementName');
			
			def result = $result.create(lunName);
			result.clazz = 'LUN';
			
			def capacity = $unit.b2GB($smis.getProperty(lunInst, 'Capacity'), 3);
			result.attr.capacity = capacity;
			def mode = $smis.getProperty(lunInst, 'Mode');
			def poolId = $smis.getProperty(lunInst, 'PoolID');
			def poolName = $smis.getProperty(lunInst, 'Poolname');
			def slotLocation = $smis.getProperty(lunInst, 'SlotLocation');
			def nodeName = $smis.getProperty(lunInst, 'NodeName');
			def preferredWWPN = $smis.getProperty(lunInst, 'PreferredWWPN');
			def vdisks = $smis.getProperty(lunInst, 'VdiskIds');
			result.attr.storResCode = poolId + "/" + $smis.getProperty(lunInst, 'DeviceID');
			def consumableBlocks = $smis.getProperty(lunInst, 'ConsumableBlocks');
			def blockSize = $smis.getProperty(lunInst, 'BlockSize');
			def usedCapacity = $unit.b2GB(consumableBlocks * blockSize, 3)
			
			result.perf.stor_use.capacity_unused = capacity - usedCapacity;
			result.perf.stor_use.capacity_useage = NumberContext.round((usedCapacity/capacity)*100, 2);
			result.perf.stor_use.capacity_used = usedCapacity;
			def status = svcUtil.getOperationalStatus($smis.getProperty(lunInst, 'OperationalStatus')[0]);
			result.state.available_status = (status == 'OK' ? 1 : 0);
		};
	};
}
