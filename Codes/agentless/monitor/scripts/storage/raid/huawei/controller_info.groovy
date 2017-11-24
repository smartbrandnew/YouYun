/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HW-STORAGECONTROLLER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HuaWeiOSUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def controllerInstances = HuaWeiOSUtils.findControllerInstances($smis);
	controllerInstances.each{controllerInst->
	def diskName = $smis.getProperty(controllerInst,'ElementName');
	def result = $result.create(diskName);
	result.clazz = 'RAIDContrller';
	result.rs.ComponentOf = 'node';
	result.state.running_status = $smis.getProperty(controllerInst,'OperationalStatus')[0];
	result.attr.controllerID =  $smis.getProperty(controllerInst,'HuaSyControllerID');
	result.state.health_status = $smis.getProperty(controllerInst,'HealthState');

	}
	
	
}


