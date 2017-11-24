/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HW-FCPORT-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HuaWeiOSUtils; 
import java.lang.*;


executeMonitor();
return $result;

def executeMonitor() {
def fcInstances = HuaWeiOSUtils.findFCPortInstances($smis);
	fcInstances.each{fcInst->
	def fcName = $smis.getProperty(fcInst,'ElementName');
	def result = $result.create(fcName);
	result.clazz = 'FCPort';
	result.rs.ComponentOf = 'node';
	result.attr.speed = $smis.getProperty(fcInst,'Speed');
	result.attr.maxspeed =  $smis.getProperty(fcInst,'MaxSpeed');
	//result.attr.portAlias = $smis.getProperty(fcInst,'ElementName')
	}
}




