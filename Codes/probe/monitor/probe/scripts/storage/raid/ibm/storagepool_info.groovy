/*!Action
 action.name=通过smi-s协议监测存储池基本信息
 action.descr=通过smi-s协议监测存储池基本信息
 action.protocols=smis
 monitor.output=IBMSVC-STOREPOOL-INFO
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
	
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def storagePoolInsts = SMIArrayUtils.fetchStoragePoolInstances($smis, clusterCOP);;
		storagePoolInsts.each {storagePoolInst->
			def storagePoolCOP = storagePoolInst.getObjectPath();
			def name = $smis.getProperty(storagePoolInst, 'ElementName');
			if (name == 'PrimordialStoragePool') {
				return;
			}
			def result = $result.create(name);
			result.clazz = 'StoragePool';
			
			def usedCapacity = $smis.getProperty(storagePoolInst, 'UsedCapacity');
			def virtualCapacity = $smis.getProperty(storagePoolInst, 'VirtualCapacity');
			def realCapacity = $smis.getProperty(storagePoolInst, 'RealCapacity');
			def totalCapacity = $smis.getProperty(storagePoolInst, 'TotalManagedSpace');
			def lunNum = $smis.getProperty(storagePoolInst, 'NumberOfBackendVolumes');
			def remainingManagedSpace = $smis.getProperty(storagePoolInst, 'RemainingManagedSpace');
			def overallocation = $smis.getProperty(storagePoolInst, 'Overallocation');
			def storagePoolAttr = svcUtil.findStoragePoolAttr($smis, name, clusterCOP);
			result.attr.driverNum = storagePoolAttr.diskNum;
			result.attr.raidLev = storagePoolAttr.raidLevel;
			result.attr.capacity = $unit.b2GB(totalCapacity, 3);
			result.attr.storResCode = $smis.getProperty(storagePoolInst, 'PoolID');
			result.perf.stor_manage.space_allocated = $unit.b2GB(totalCapacity - remainingManagedSpace, 3);
			result.perf.stor_manage.space_unallocated = $unit.b2GB(remainingManagedSpace, 3);
			result.perf.stor_manage.space_useage = NumberContext.round((totalCapacity - remainingManagedSpace) * 100 / totalCapacity, 2);
			if (storagePoolAttr.capacity != 0) {
				result.perf.stor_use.capacity_unused = $unit.b2GB(storagePoolAttr.capacity - usedCapacity, 3);
				result.perf.stor_use.capacity_useage = NumberContext.round(usedCapacity * 100 / storagePoolAttr.capacity, 2);
			}
			if (usedCapacity != null) {
				result.perf.stor_use.capacity_used = $unit.b2GB(usedCapacity, 3);
			}
			
			def status = svcUtil.getOperationalStatus($smis.getProperty(storagePoolInst, 'OperationalStatus')[0]);
			result.state.available_status = (status == 'OK' ? 1 : 0);
		}
	}
	
}