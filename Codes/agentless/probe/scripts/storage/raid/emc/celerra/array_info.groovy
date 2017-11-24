/*!Action
 action.name=通过smi-s协议监测华为设备物理基本信息
 action.descr=通过smi-s协议监测华为设备物理基本信息
 action.protocols=smis
 monitor.output=EMCCELERRA-ARRAY-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = EMCCelerraUtils.findArrayInstances($smis);
	arrayInstances {arrayInst->
			def diskName = $smis.getProperty(arrayInst, 'Name');
			def result = $result.create(diskName);
			result.clazz = 'EMC';
			result.rs.ComponentOf = 'node';
			
			//result.attr.primarOwnerContact = $smis.getProperty(arrayInst, 'PrimaryOwnerContact');
			
			//result.attr.primarOwnerName = $smis.getProperty(arrayInst, 'PrimaryOwnerName');
			result.state.available_status = $smis.getProperty(arrayInst, 'OperationalStatus');
			
	}
	
}


