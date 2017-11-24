/*!Action
 action.name=通过smi-s协议监测华为设备物理基本信息
 action.descr=通过smi-s协议监测华为设备物理基本信息
 action.protocols=smis
 monitor.output=EMCSYMM-DISKDRIVER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils; 
import java.lang.*; 

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = EMCSymmUtils.findArrayInstances($smis);
	arrayInstances.each {arrayInst->
	def arrayCOP = arrayInst.getObjectPath();
	def computerInstances = EMCSymmUtils.findComputerSystem($smis,arrayCOP);
	computerInstances{computerInst->
	def ctrlCOP = computerInst.getObjectPath();
	def ctrlName = $smis.getProperty(computerInst,'CreationClassName');
	if(ctrlName.indexOf("Symm_StorageProcessorSystem")==-1){
	def ddInstances = EMCSymmUtils.findDiskDriver($smis,ctrlCOP);
	def statsDatas =  $smis.getInstancesByClass("Symm_BlockStorageStatisticalData");
	ddInstances.each{ddInst->
	def ddName = $smis.getProperty(ddInst,'SystemName');
	def ddCOP = ddInst.getObjectpath();
	def result = $result.create(ddName);
	result.clazz = 'physicalDisk';
	result.rs.ComponentOf = 'node';
	
	}
	}
	}
	}
	

}


