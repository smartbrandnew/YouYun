/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HDS-STORAGEVOLUME-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HDSUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def osUtil = $script.use('storage/raid/huawei/oceanstor_util');
	def storageVolumeInstances = HDSUtils.findStorageVolumeInstances($smis);
	storageVolumeInstances.each{storageVolumeInst->
	def diskName = $smis.getProperty(storageVolumeInst,'ElementName');
	def result = $result.create(diskName);
	result.clazz = 'StorageVolume';
	result.rs.ComponentOf = 'node';
	result.state.running_status = $smis.getProperty(storageVolumeInst,'OperationalStatus')[0];
	result.attr.storResCode =  $smis.getProperty(storageVolumeInst,'DeviceId');
	def consumableBlocks = $smis.getProperty(storageVolumeInst, 'ConsumableBlocks');
	def blockSize = $smis.getProperty(storageVolumeInst, 'BlockSize');
	def capacity = $unit.b2GB(blockSize * $smis.getProperty(storageVolumeInst, 'NumberOfBlocks'), 3)
	def usedCapacity = $unit.b2GB(consumableBlocks* blockSize, 3)
	result.attr.capacity = capacity;
	result.perf.stor_use.capacity_unused = $unit.b2GB(capacity - usedCapacity, 3);
	result.perf.stor_use.capacity_useage = NumberContext.round(usedCapacity * 100 / capacity, 3);
	result.perf.stor_use.capacity_used = usedCapacity;
	}
	
	
}


