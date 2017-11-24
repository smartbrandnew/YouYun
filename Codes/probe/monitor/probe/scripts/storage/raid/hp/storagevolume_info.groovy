/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HPMSA-STORAGEVOLUME-INFO
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
	def svInstances = HPMSAUtils.findStorageVolumeInstances($smis);
	arrayInstances.each{arrayInst->
		svInstances.each{svInst->
		def svName = $smis.getProperty(svInst,'ElementName');
		def result = $result.create(svName);
		result.clazz = 'StorageVolume';
		result.rs.ComponentOf = 'node';
		result.state.available_status = $smis.getProperty(svInst,'OperationalStatus');
		result.attr.spaceConsumed =  $smis.getProperty(svInst,'SpaceConsumed');
		result.attr.name =  $smis.getProperty(svInst,'Name');
		result.attr.accessList =  $smis.getProperty(svInst,'AccessList');
		result.attr.storResCode =  $smis.getProperty(svInst,'StoragePoolID');
		}
	};
	
}


