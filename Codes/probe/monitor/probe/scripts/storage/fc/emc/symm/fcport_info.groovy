/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=EMCSYMM-FCPORT-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCSymmUtils; 
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
	if(ctrlName.indexOf("Symm_StorageProcessorSystem")>-1){
	def fcportInstances = EMCSymmUtils.findFcOrIscsiPort($smis,ctrlCOP);
	fcportInstances.each{fcportInst->
	def fcName = $smis.getProperty(computerInst,'DeviceID');
	def fcCOP = fcportInst.getObjectpath();
	def result = $result.create(fcName);
	result.clazz = 'ComputerSystem';
	result.rs.ComponentOf = 'node';
	def fcinfos = $smis.getAssociatedInstances(fcCOP,"CIM_ElementStatisticalData", "CIM_BlockStorageStatisticalData", "ManagedElement", "Stats")
	fcinfos.each{fcinfo->
	result.attr.transferred = $smis.getProperty(computerInst,'KBytesTransferred');
	result.perf.emcsymm-perf.total_ios = $smis.getProperty(computerInst,'TotalIOs');
	//result.attr.statisticTime = $smis.getProperty(computerInst,'StatisticTime');
	}
	}
	}
	}
	}

}




