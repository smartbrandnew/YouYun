/*!Action
 action.name=通过smis协议监测FC端口
 action.descr=通过smis协议监测FC端口
 action.protocols=smis
 monitor.output=EMCCELERRA-NETWORKPORT-INFO
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
	computerInstances.each{computerInst->
	def compCOP = computerInst.getObjectPath();
	def fileInstances = EMCCelerraUtils.findEthernetPort($smis,compCOP);
	fileInstances.each{fileInst->
	def fileName = $smis.getProperty(fileInst,'DeviceID');
	def result = $result.create(fileName);
	result.clazz = 'NetDevPort';
	result.rs.ComponentOf = 'node';
	result.state.available_status = $smis.getProperty(fileInst,'OperationalStatus');
	result.attr.netWorkAddr = $smis.getProperty(fileInst,'NetworkAddresses');
	result.attr.MACAddr = $smis.getProperty(fileInst,'PermanentAddress');
	}
	}
	}
	

}




