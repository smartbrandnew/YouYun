/*!Action
 action.name=通过smi-s协议监测存储卷基本信息
 action.descr=通过smi-s协议监测存储卷基本信息
 action.protocols=smis
 monitor.output=HPMSA-DISKDRIVER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HPMSAUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = HPMSAUtils.findArrayInstances($smis);
	def ddInstances = HPMSAUtils.findDiskDriveInstances($smis);
	def seInstances = HPMSAUtils.findStorageExtentInstances($smis);
	def ppInstances = HPMSAUtils.findDiskPhysicalPackageInstances($smis);
	arrayInstances.each{arrayInst->
		ddInstances.each{ddInst->
		def arrayName = $smis.getProperty(ddInst,'ElementName');
		def result = $result.create(arrayName);
		result.clazz = 'physicalDisk';
		result.rs.ComponentOf = 'node';
		result.state.available_status = $smis.getProperty(ddInst,'OperationalStatus');
		def deviceId =  $smis.getProperty(ddInst,'DeviceID');
		def name =  $smis.getProperty(ddInst,'Name');
		result.attr.name =  name;
		
		seInstances.each{seInst->
		def seDeviceId =  $smis.getProperty(seInst,'DeviceID');
		if(seDeviceId.indexOf(deviceId)>-1){
		def bs =  $smis.getProperty(seInst,'BlockSize');
		def nb =  $smis.getProperty(seInst,'NumberOfBlocks');
		result.attr.capacity = $util.b2GB(bs*nb);
		}
		}
		
		ppInstances{ppInst->
		def ppName =  $smis.getProperty(ppInst,'Name');
		if(ppName.indexOf(name)>-1){
		result.attr.partNum =  $smis.getProperty(ppInst,'Model');
		}
		}
		
		}

		
	};
	
}


