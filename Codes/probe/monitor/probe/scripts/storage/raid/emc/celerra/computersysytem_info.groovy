/*!Action
 action.name=通过smis协议监测FC端口
 action.descr=通过smis协议监测FC端口
 action.protocols=smis
 monitor.output=EMCCELERRA-COMPUTERSYSTEM-INFO
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
	def computerInstances = EMCCelerraUtils.findComputerSystem($smis,arrayCOP);
	computerInstances{computerInst->
	def compName = $smis.getProperty(computerInst,'Name');
	def result = $result.create(compName);
	result.clazz = 'ComputerSystem';
	result.rs.ComponentOf = 'node';
	result.state.available_status = $smis.getProperty(nfsServerInst,'OperationalStatus');
	
	}
	

}




