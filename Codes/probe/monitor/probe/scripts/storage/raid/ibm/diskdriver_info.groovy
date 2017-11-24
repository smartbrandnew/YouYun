/*!Action
 action.name=通过smi-s协议监测存储卷基本信息
 action.descr=通过smi-s协议监测存储卷基本信息
 action.protocols=smis
 monitor.output=IBMSVC-DISKDRIVER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def mdiskInstances = IBMSVCUtils.findMDiskInstancesByCluster($smis, clusterCOP);
		def diskDriverInsts = IBMSVCUtils.findDiskDriverByCluster($smis, clusterCOP);
		diskDriverInsts.each {diskInst->
			def diskCOP = diskInst.getObjectPath();
			def diskName = $smis.getProperty(diskInst, 'Name');
			def result = $result.create(diskName);
			result.clazz = 'physicalDisk';
			result.rs.ComponentOf = 'node';
			result.attr.diskType = svcUtil.getDiskType($smis.getProperty(diskInst, 'TechType'));
			result.attr.slotNo = $smis.getProperty(diskInst, 'SlotID');
			def blockSize = $smis.getProperty(diskInst, 'BlockSize');
				
			def mDiskName = $smis.getProperty(diskInst, 'MdiskName');
			for (mdiskInst in mdiskInstances) {
				def mdiskName = $smis.getProperty(mdiskInst, 'ElementName');
				if (mDiskName == mdiskName) {
					def cimProperty = mdiskInst.getProperty('RaidLevel');
					def cimValue = cimProperty.getValue();
					result.attr.raidLev = cimValue.getValue()[0]
					break;
				}
			}
			def mDiskID = $smis.getProperty(diskInst, 'MdiskID');
			
			result.attr.storResCode = mDiskID + "/" + diskName;
			def capacity = $smis.getProperty(diskInst, 'Capacity'); 
			
			result.attr.capacity = $unit.b2GB(capacity);
			def diskProps = new Properties();
			svcUtil.fetchDiskPhysicalPackage($smis, diskCOP, diskProps);
			
			def mfg = $smis.getProperty(diskInst, 'VendorID');
			def serial = $smis.getProperty(diskInst, 'FRUPartNum')
			
			if (diskProps.getProperty("Manufacturer") == "") {
				mfg = diskProps.getProperty("Manufacturer");
				serial = diskProps.getProperty("SerialNumber");
			} 
			
			result.attr.mfg = mfg;
			result.attr.productName = $smis.getProperty(diskInst, 'ProductID');
			result.attr.serial = serial;
			def use = $smis.getProperty(diskInst, 'Use'); 
			result.state.available_status = ((use==2 || use == 1 || use == 4) ? 1 : 0);

		}
	}
	
}


