/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HDS-STORAGEPOOL-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HDSUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def storagePoolInstances = HDSUtils.findStoragePool($smis);
	storagePoolInstances.each{storagePoolInst->
	def diskName = $smis.getProperty(storagePoolInst,'ElementName');
	def result = $result.create(diskName);
	result.clazz = 'StoragePool';
	result.rs.ComponentOf = 'node';
	result.state.running_status = $smis.getProperty(storagePoolInst,'OperationalStatus')[0];
	result.attr.storResCode =  $smis.getProperty(storagePoolInst,'InstanceID');
	
	def capacity = $unit.b2GB($smis.getProperty(storagePoolInst, 'TotalManagedSpace'), 3);
	def unusedCapacity = $unit.b2GB($smis.getProperty(storagePoolInst, 'RemainingManagedSpace'), 3);
	def usedCapacity = capacity-unusedCapacity;
	def capacutyLimit = $unit.b2GB($smis.getProperty(storagePoolInst, 'SpaceLimit'), 3);
	result.attr.capacity = capacity;
	result.perf.stor_use.capacity_unused = unusedCapacity;
	result.perf.stor_use.capacity_useage = NumberContext.round(usedCapacity * 100 / capacity, 3);
	result.perf.stor_use.capacity_used = usedCapacity;
	result.attr.usage = $smis.getProperty(storagePoolInst,'Usage');
	result.attr.perimordial = $smis.getProperty(storagePoolInst,'Perimordial');
	result.attr.poolID = $smis.getProperty(storagePoolInst,'PoolID');
	result.state.health_status = $smis.getProperty(storagePoolInst,'HealthState');
	result.perf.stor_use.capacity_limit = capacutyLimit;
	}
	
	
}


