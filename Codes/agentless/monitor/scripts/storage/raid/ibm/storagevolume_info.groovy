/*!Action
 action.name=通过smi-s协议监测存储卷基本信息
 action.descr=通过smi-s协议监测存储卷基本信息
 action.protocols=smis
 monitor.output=storevolume-info
 monitor.priority=100
*/
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils;

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def poolAttrMap = svcUtil.findStoragePoolAttr($smis, clusterCOP);
		def vdiskInsts = IBMSVCUtils.findVdiskInstancesByCluster($smis, clusterCOP);;
		vdiskInsts.each {vdiskInst->
			def vdiskCOP = vdiskInst.getObjectPath();
			def vdiskName = $smis.getProperty(vdiskInst, 'ElementName');
			def	capacityMap = null		
			def poolName = $smis.getProperty(vdiskInst, 'PoolName');
			def name = $smis.getProperty(vdiskInst, 'Caption');
			def result = $result.create(name);
			result.clazz = 'StorageVolume';

			if (poolAttrMap.get(poolName) != null) {
				def poolAttr = poolAttrMap.get(poolName);
				result.attr.raidLev = poolAttr.raidLevel;
			}
			
			result.attr.storResCode = $smis.getProperty(vdiskInst, 'PoolID') + "/" + $smis.getProperty(vdiskInst, 'UniqueID');
			def consumableBlocks = $smis.getProperty(vdiskInst, 'ConsumableBlocks');
			def blockSize = $smis.getProperty(vdiskInst, 'BlockSize');
			def capacity = $unit.b2GB(blockSize * $smis.getProperty(vdiskInst, 'NumberOfBlocks'), 3)
			def usedCapacity = $unit.b2GB(consumableBlocks* blockSize, 3)
			result.attr.capacity = capacity;
			result.perf.stor_use.capacity_unused = $unit.b2GB(capacity - usedCapacity, 3);
			result.perf.stor_use.capacity_useage = NumberContext.round(usedCapacity * 100 / capacity, 3);
			result.perf.stor_use.capacity_used = usedCapacity;
			def status = svcUtil.getOperationalStatus($smis.getProperty(vdiskInst, 'OperationalStatus')[0]);
			result.state.available_status = (status == 'OK' ? 1 : 0);
		}
	}
	
}