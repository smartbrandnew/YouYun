/*!Action
 action.name=通过smi-s协议监测LUN
 action.descr=通过smi-s协议监测LUN
 action.protocols=smis
 monitor.output=IBMSVC-DISKARRAY-FAN-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

executeMonitor();
return $result;


def executeMonitor() {
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	clusterInstances.each{clusterInst->
		def clusterCOP = clusterInst.getObjectPath();

		def clusterName = $smis.getProperty(clusterInst, 'ElementName');
		if (clusterName == null) {
			return;
		}
		
		def enclosureInstances = svcUtil.findEnclosureByCluster($smis, clusterCOP);
		enclosureInstances.each{enclosureInst->
			def packageType = svcUtil.getPackageType($smis.getProperty(enclosureInst, 'PackageType'));
			if (packageType != 'Fan') {
				return;
			}
			
			def enclosureCOP = enclosureInst.getObjectPath();
			def fanName = $smis.getProperty(enclosureInst, 'ElementName');
			
			def result = $result.create(fanName);
			result.clazz = 'FAN';
			
			result.attr.capacity = null;
			result.attr.flags = $smis.getProperty(enclosureInst, 'Tag');
			def driveSlots = $smis.getProperty(enclosureInst, 'DriveSlots');
			result.attr.productName = $smis.getProperty(enclosureInst, 'MachinePartNumber');
			result.attr.partNum = $smis.getProperty(enclosureInst, 'Model');
			result.attr.serial = $smis.getProperty(enclosureInst, 'SerialNumber');
			def hotSwappable = $smis.getProperty(enclosureInst, 'HotSwappable');
			result.attr.mfg = $smis.getProperty(enclosureInst, 'Manufacturer');

			def enclosureStatus = svcUtil.getEnclosureStatus($smis.getProperty(enclosureInst, 'EnclosureStatus'));
			result.state.available_status = (enclosureStatus == 'online' ? 1 : 0);

		}
	}
}
