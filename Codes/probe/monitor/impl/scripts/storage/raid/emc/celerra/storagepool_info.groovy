/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=EMCCELERRA-STORAGEPOOL-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils; 
import java.lang.*; 

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = EMCCelerraUtils.findArrayInstances($smis);
	arrayInstances.each {arrayInst->
	def arrayCOP = arrayInst.getObjectPath();
	def storagePoolInstances = EMCCelerraUtils.findStoragePool($smis,arrayCOP);
	storagePoolInstances.each {storagePoolInst->
	def spName = $smis.getProperty(storagePoolInst,'PoolID');
	def result = $result.create(spName);
	result.clazz = 'StoragePool';
	result.rs.ComponentOf = 'node';
	
	def capacity = $unit.b2GB($smis.getProperty(storagePoolInst, 'TotalManagedSpace'), 3);
	def unusedCapacity = $unit.b2GB($smis.getProperty(storagePoolInst, 'RemainingManagedSpace'), 3);
	def usedCapacity = capacity-unusedCapacity;
	result.attr.capacity = capacity;
	result.perf.stor_use.capacity_unused = unusedCapacity;
	result.perf.stor_use.capacity_useage = NumberContext.round(usedCapacity * 100 / capacity, 3);
	result.perf.stor_use.capacity_used = usedCapacity;
	result.attr.perimordial = $smis.getProperty(storagePoolInst,'Perimordial');
	result.attr.storResCode = $smis.getProperty(storagePoolInst,'PoolID');
	}
	}
	
	
}


