/*!Action
 action.name=通过smis协议监测FC端口
 action.descr=通过smis协议监测FC端口
 action.protocols=smis
 monitor.output=EMCSYMM-COMPUTERSYSTEM-INFO
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
	def compName = $smis.getProperty(computerInst,'Name');
	def result = $result.create(compName);
	result.clazz = 'ComputerSystem';
	result.rs.ComponentOf = 'node';
	result.state.available_status = $smis.getProperty(computerInst,'OperationalStatus');
	result.attr.className = $smis.getProperty(computerInst,'CreationClassName');
	def roles = $smis.getProperty(computerInst,'Roles');
	if(roles.indesOf("Fornt End")>-1){
		def feinfos = $smis.getAssociatedInstances(svCOP,"CIM_ElementStatisticalData", "CIM_BlockStorageStatisticalData", "ManagedElement", "Stats");
		feinfos.each{feinfo->
		result.perf.emcsymm-perf.emc_device_flushpending_events = $smis.getProperty(arrayinfo,'EMCDeviceFlushPendingEvents');
			result.perf.emcsymm-perf.emc_permacache_ios = $smis.getProperty(arrayinfo,'EMCPermacacheIOs');
			result.perf.emcsymm-perf.emc_solt_collisions = $smis.getProperty(arrayinfo,'EMCSlotCollisions');
			result.perf.emcsymm-perf.emc_system_flushpending_events = $smis.getProperty(arrayinfo,'EMCSystemFlushPendingEvents');
			result.perf.emcsymm-perf.emc_totalhit_ios = $smis.getProperty(arrayinfo,'EMCTotalHitIOs');
			result.attr.transferred = $smis.getProperty(arrayinfo,'KBytesTransferred');
			result.attr.statisticTime = $smis.getProperty(arrayinfo,'StatisticTime');
			result.perf.emcsymm-perf.write_ios = $smis.getProperty(arrayinfo,'WriteIOs');
			result.perf.emcsymm-perf.read_ios = $smis.getProperty(arrayinfo,'ReadIOs');
			result.perf.emcsymm-perf.total_ios = $smis.getProperty(arrayinfo,'TotalIOs');
		}
	}
	}
	

}

def convertVectorToString(def role){
if(role.indexOf("Front End")>-1){
}
}



