/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HPMSA-FCPORT-INFO
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
	def fcInstances = HPMSAUtils.findFCPortInstances($smis);
	arrayInstances.each{arrayInst->
	fcInstances.each{fcInst->
	def fcName = $smis.getProperty(fcInst,'DeviceID');
	def result = $result.create(fcName);
	result.clazz = 'FCPort';
	result.rs.ComponentOf = 'node';
	result.attr.speed = $smis.getProperty(fcInst,'Speed');
	//result.attr.maxspeed =  $smis.getProperty(fcInst,'UsageRestriction');
	result.attr.portType = $smis.getProperty(fcInst,'PortType');
	//result.attr.macAddr = $smis.getProperty(fcInst,'PermanentAddress');
	result.attr.portIdx= $smis.getProperty(fcInst,'PortNumber');
	result.state.available_status = $smis.getProperty(fcInst,'OperationalStatus');
	//result.attr.ipAddr = $smis.getProperty(fcInst,'NetworkAddress');
	}
	}
}




