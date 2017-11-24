/*!Action
 action.name=通过smi-s协议监测华为设备物理基本信息
 action.descr=通过smi-s协议监测华为设备物理基本信息
 action.protocols=smis
 monitor.output=HW-PHYSICALDISK-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HuaWeiOSUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def osUtil = $script.use('storage/raid/huawei/oceanstor_util');
	def clusterInstances = HuaWeiOSUtils.findClusterInstances($smis);
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def diskDriverInsts = HuaWeiOSUtils.findDiskDriverByCluster($smis, clusterCOP);
		diskDriverInsts.each {diskInst->
			def diskCOP = diskInst.getObjectPath();
			out.println("集群ObjectPath："+diskCOP);
			def diskName = $smis.getProperty(diskInst, 'Name');
			def result = $result.create(diskName);
			result.clazz = 'physicalDisk';
			result.rs.ComponentOf = 'node';
			
			result.attr.diskType = osUtil.getDiskType($smis.getProperty(diskInst, 'DiskType'));
			result.state.available_status = $smis.getProperty(diskInst, 'EnabledState');
			result.state.available_status = $smis.getProperty(diskInst, 'OperationalStatus');
			result.attr.formFactor = osUtil.getDiskType($smis.getProperty(diskInst, 'FormFactor'));
			result.attr.encryption = osUtil.getDiskType($smis.getProperty(diskInst, 'Encryption'));
			def blockSize = $smis.getProperty(diskInst, 'DefaultBlockSize');
			result.attr.capacity = $unit.b2GB(blockSize);
			
			def diskExtProps = new Properties();
			osUtil.fetchDiskExtent($smis, diskCOP, diskExtProps);
			result.attr.access = diskExtProps.getProperty("Access");
			def numberOfBlock = diskExtProps.getProperty("NumberOfBlock");
			def consumableBlock = diskExtProps.getProperty("ConsumableBlocks");
			def totalBlock = numberOfBlock * consumableBlock;
			result.attr.totalcapacity = $unit.b2GB(totalBlock);
			
			
			
			def diskDriverPackageProps = new Properties();
			osUtil.fetchDiskDrivePackage($smis, diskCOP, diskDriverPackageProps);
			
			
			def diskProps = new Properties();
			osUtil.fetchDiskPhysicalPackage($smis, diskCOP, diskProps);
			
			 result.attr.serialNumber = diskProps.getProperty("SerialNumber");
			 
	  		  result.attr..manuFacturer = diskProps.getProperty("Manufacturer");
	  		  result.attr.model = diskProps.getProperty("Model");
	  		  result.attr.description = diskProps.getProperty("Description");
	  		  result.attr..elementName = diskProps.getProperty("ElementName");
	  		  result.attr..packageType = diskProps.getProperty("PackageType");
	  		  result.attr..version = diskProps.getProperty("Version");
			
		}
	}
	
}


