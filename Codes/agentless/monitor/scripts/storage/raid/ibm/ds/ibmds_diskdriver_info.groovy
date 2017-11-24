/*!Action
 action.name=通过smi-s协议监测IBMDS磁盘基本信息
 action.descr=通过smi-s协议IBMDS磁盘基本信息
 action.protocols=smis
 monitor.output=IBMDS-DISKDRIVER-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def sdInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_DiskDriveStatisticalData');
	arrayInst.each{systemInst->
		def arrayCOP = systemInst.getObjectPath();
		def cimInstances = IBMDSUtils.findDiskDriver($smis, arrayCOP);
		cimInstances.each{cimInst->
			def driverCOP = cimInst.getObjectPath();
			def diskName = $smis.getProperty(cimInst,'Name');
			def diskDriveState = dsUtil.getOperationalStatus($smis.getProperty(cimInst, 'OperationalStatus')[0]);
			
			def diskDriveID = $smis.getProperty(cimInst, 'DeviceID');
			def diskDriveHealth = $smis.getProperty(cimInst,'HealthState');

			
			def diskExtentEn = IBMDSUtils.findDiskEctent($smis, driverCOP);
			def diskDriveBlockSize = null;
			def diskDriveNoOfBlocks = null;
			def diskDriveConsumableBlocks = null;
			def diskDrivePrimordial = null;
			def diskDriveAccess = null;
			diskExtentEn.each{deInst->
				diskDriveBlockSize = $smis.getProperty(deInst,"BlockSize");
				diskDriveNoOfBlocks = $smis.getProperty(deInst,"NumberOfBlocks");
				diskDriveConsumableBlocks = $smis.getProperty(deInst,"ConsumableBlocks");
				diskDrivePrimordial = $smis.getProperty(deInst,"Primordial");
			 	diskDriveAccess = $smis.getProperty(deInst,"Access");
			}
			
			def diskPackageEn = IBMDSUtils.findDiskPackage($smis, driverCOP);
			def diskDriveModel = null;
			def diskDriveManufacturer = null;
			def diskDriveVersion = null;
			def diskDriveRemovalConditions = null;
			def diskDriveCapacity = null;
			
			
			diskPackageEn.each{dpInst->
				diskDriveModel = $smis.getProperty(dpInst,"Model");
				diskDriveManufacturer = $smis.getProperty(dpInst,"Manufacturer");
				
			}
			diskDriveCapacity = $unit.b2GB(diskDriveNoOfBlocks*diskDriveBlockSize);
			def unused =  $unit.b2GB(diskDriveConsumableBlocks*diskDriveBlockSize);
			
			
			
			def result = $result.create(diskName);
			result.clazz = 'physicalDisk';
			
			
			result.state.available_status = (diskDriveState == 'OK' ? 1 : 0);
			result.attr.storResCode = diskDriveID;
			result.attr.capacity = diskDriveCapacity;
			result.attr.mfg = diskDriveManufacturer;
			result.attr.serial = $smis.getProperty(cimInst,"SerialNumber");
			result.state.health_status = diskDriveHealth;
			result.attr.partNum = diskDriveModel;
			
			result.attr.FDECapable =  $smis.getProperty(cimInst,'FDECapable');
			result.attr.FDEEnabled =  $smis.getProperty(cimInst,'FDEEnabled');
			result.attr.FDELocked =  $smis.getProperty(cimInst,'FDELocked');
			result.attr.sequentialAccess = ( $smis.getProperty(cimInst,"SequentialAccess")=="true")?"是":"否";
			result.attr.primordial = ($smis.getProperty(cimInst,"Primordial")=="true")?"是":"否";
			result.attr.discript=  $smis.getProperty(cimInst,"Description");
			result.perf."diskdrive-io".rotation_rate=  $smis.getProperty(cimInst,"NominalRotationRate");
			
			
		}
	}
	
	
}