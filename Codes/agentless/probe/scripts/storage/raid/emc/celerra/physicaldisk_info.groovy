/*!Action
 action.name=通过smi-s协议监测华为设备物理基本信息
 action.descr=通过smi-s协议监测华为设备物理基本信息
 action.protocols=smis
 monitor.output=EMCCELERRA-DISKDRIVER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def diskInstances = EMCCelerraUtils.findDiskPhysicalPackage($smis);
	diskInstances.each {diskInst->
			def diskName = $smis.getProperty(diskInst, 'Name');
			def result = $result.create(diskName);
			result.clazz = 'physicalDisk';
			result.rs.ComponentOf = 'node';
			
			result.attr.manuFacturer = osUtil.getDiskType($smis.getProperty(diskInst, 'Manufacturer'));
			result.attr.model = $smis.getProperty(diskInst, 'Model');
			result.attr.serialNumber = $smis.getProperty(diskInst, 'SerialNumber');

	  		 
			
		}
	}
	
}


