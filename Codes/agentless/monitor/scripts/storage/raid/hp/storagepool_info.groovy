/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HPMSA-STORAGEPOOL-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HPMSAUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = HPMSAUtils.findArrayInstances($smis);
	def spInstances = HPMSAUtils.findStoragePoolInstances($smis);
	arrayInstances.each{arrayInst->
		spInstances.each{spInst->
		def spName = $smis.getProperty(spInst,'ElementName');
		def result = $result.create(spName);
		result.clazz = 'StoragePool';
		result.rs.ComponentOf = 'node';
		def total=  $smis.getProperty(spInst,'TotalManagedSpace');
		def unused=  $smis.getProperty(spInst,'RemainingManagedSpace');
		result.attr.capacity = total;
		result.perf.stor_use.capacity_used = total-unused;
		result.perf.stor_use.capacity_unused = unused;
		result.perf.stor_use.capacity_useage = NumberContext.round((total-unused) * 100 / total, 3);
		result.attr.primordial =  $smis.getProperty(spInst,'Primordial');
		result.attr.storResCode =  $smis.getProperty(spInst,'InstanceID');
		}
	};
	
}


