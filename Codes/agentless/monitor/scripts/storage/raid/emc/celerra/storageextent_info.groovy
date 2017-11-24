/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=EMCCELERRA-STORAGEEXTENT-INFO
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
	def storageExtentInstances = EMCCelerraUtils.findStorageExtent($smis,arrayCOP);
	storageExtentInstances.each {storageExtentInst->
	def seName = $smis.getProperty(storageExtentInst,'Name');
	def result = $result.create(seName);
	result.clazz = 'StorageExtent';
	result.rs.ComponentOf = 'node';
	def noSinglePointOfFailure = $smis.getProperty(storageExtentInst,'NoSinglePointOfFailure');
	result.attr.perimordial = $smis.getProperty(storageExtentInst,'Primordial');
	def blockSize = $unit.b2GB($smis.getProperty(storageExtentInst,"BlockSize"));
	def numberOfBlock = $smis.getProperty(storageExtentInst,"NumberOfBlocks");
	def consumableBlock = $smis.getProperty(storageExtentInst,"ConsumableBlocks");
	def capacity = numberOfBlock*consumableBlock;
	result.attr.capacity = $unit.b2GB(capacity);
	result.state.available_status = fitchDedicated($smis.getProperty(cifsServerInst,'OperationalStatus'));
	}
	}
	
	
}


