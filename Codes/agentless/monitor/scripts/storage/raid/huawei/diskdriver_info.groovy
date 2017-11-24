/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HW-DISKDRIVER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HuaWeiOSUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def osUtil = $script.use('storage/raid/huawei/oceanstor_util');
	def diskPackageInstances = HuaWeiOSUtils.findDiskPropertiesInstances($smis);
	//def diskPackageInstances = HuaWeiOSUtils.findDiskPackageInstances($smis);
	diskPackageInstances.each {diskPackageInst->
	def diskName = diskPackageInst.getProperty('ElementName');
	def result = $result.create(diskName);
	result.clazz = 'physicalDisk';
	result.rs.ComponentOf = 'node';
	result.attr.partNum = diskPackageInst.getProperty('Model');
	result.attr.serial = diskPackageInst.getProperty('SerialNumber');

	//result.attr.serial = diskPackageInst.getProperty('IdentifyingNumber');
	result.attr.mfg = diskPackageInst.getProperty('Vendor');
	result.attr.vs = diskPackageInst.getProperty('Version');


	 result.attr.diskType = diskPackageInst.getProperty('DiskType');
	 result.attr.formFactor = diskPackageInst.getProperty('FormFactor');
	 result.attr.encryption = diskPackageInst.getProperty('Encryption');
	 diskPackageInst.getProperty('Caption'); 
	 
	 def capacity = diskPackageInst.getProperty('Capacity'); 
	 result.attr.capacity = $unit.b2GB(capacity.toDouble());
	 diskPackageInst.getProperty('ConsumableBlocks'); 
	 result.attr.isPrim = diskPackageInst.getProperty('Primordial'); 
	 result.state.available_status = diskPackageInst.getProperty('OperationalStatus');
	 result.state.enabled_status =diskPackageInst.getProperty('EnabledState');
	}
	
}


