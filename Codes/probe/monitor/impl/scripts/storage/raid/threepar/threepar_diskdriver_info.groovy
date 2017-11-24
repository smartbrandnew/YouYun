/*!Action
 action.name=通过smi-s协议监测THREEPAR磁盘信息
 action.descr=通过smi-s协议THREEPAR磁盘信息
 action.protocols=smis
 monitor.output=THREEPAR-DISKDRIVER-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.ThreePARUtils;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/threepar/threepar_util');
	def arrayInst = svcUtil.getCIMInstances();
	arrayInst.each{cimInst->
		def cop = cimInst.getObjectPath();
		def driverInsts = ThreePARUtils.findDiskDriver($smis, cop);
		driverInsts.each{driverInst->
			String diskDriveSize = driverInst.getProperty("MaxMediaSize");
	        String diskDriveTemp = driverInst.getProperty("Temperature");
	        String diskDriveInitiatorA = driverInst.getProperty("InitiatorPortA");
	        String diskDriveInitiatorB = driverInst.getProperty("InitiatorPortB");
	        String diskPrimaryPortPath = driverInst.getProperty("PrimaryPortPath");
	        String diskSpeed = driverInst.getProperty("DiskSpeed");
	        String diskElmName = driverInst.getProperty("ElementName");
	        String diskSystemName = driverInst.getProperty("SystemName");
	        String diskDeviceId = driverInst.getProperty("DeviceID");
	        String diskPosition = driverInst.getProperty("Position");
	        String diskCageType = "Not Available";
	        try
	        {
	          diskCageType = ThreePARUtils.getCageType(driverInst.getProperty("CageType"));
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	        String diskID = driverInst.getProperty("ID");
	        String deviceType = driverInst.getProperty("DeviceType");
	        String isLDAllocateable = driverInst.getProperty("IsLDAllocateable");
	        String stateOnLoopA = "Not Available";
	        try
	        {
	          stateOnLoopA = ThreePARUtils.getDiskStateOnLoop(driverInst.getProperty("StateOnLoopA"));
	        }
	        catch (Exception e)
	        {
	          out.warning("[ ThreePARArrayModeller - modelData ] Exception while fetching the Disk State On Loop. Message = " + e.getMessage());
	          e.printStackTrace();
	        }
	
	        String stateOnLoopB = "Not Available";
	        try
	        {
	          stateOnLoopB = ThreePARUtils.getDiskStateOnLoop(driverInst.getProperty("StateOnLoopB"));
	        }
	        catch (Exception e)
	        {
	          out.warning("[ ThreePARArrayModeller - modelData ] Exception while fetching the State On Loop B. Message = " + e.getMessage());
	          e.printStackTrace();
	        }
	
	        String alpaOnLoopA = driverInst.getProperty("AlpaOnLoopA");
	        String alpaOnLoopB = driverInst.getProperty("AlpaOnLoopB");
	        String chunkSize = driverInst.getProperty("ChunkSize");
	        String chunksNormalUsedOK = driverInst.getProperty("ChunksNormalUsedOK");
	        String chunksNormalUsedFail = driverInst.getProperty("ChunksNormalUsedFail");
	        String chunksNormalUnusedFree = driverInst.getProperty("ChunksNormalUnusedFree");
	        String chunksNormalUnusedUninit = driverInst.getProperty("ChunksNormalUnusedUninit");
	        String chunksNormalUnusedFail = driverInst.getProperty("ChunksNormalUnusedFail");
	        String chunksSpareUsedOK = driverInst.getProperty("ChunksSpareUsedOK");
	        String chunksSpareUsedFail = driverInst.getProperty("ChunksSpareUsedFail");
	        String chunksSpareUnusedFree = driverInst.getProperty("ChunksSpareUnusedFree");
	        String chunksSpareUnusedUninit = driverInst.getProperty("ChunksSpareUnusedUninit");
	        String chunksSpareUnusedFail = driverInst.getProperty("ChunksSpareUnusedFail");
	        String uncorrectableReadErrors = driverInst.getProperty("UncorrectableReadErrors");
	        String uncorrectableWriteErrors = driverInst.getProperty("UncorrectableWriteErrors");
	        String isESI = driverInst.getProperty("IsESI");
	        String esiState = "Not Available";
	        try
	        {
	          esiState = ThreePARUtils.getDiskESIState(driverInst.getProperty("ESIState"));
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskUsedCapacity = 0.0D;
	        try
	        {
	          diskUsedCapacity = svcUtil.convertBytesIntoGB(chunksNormalUsedOK, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskUsedFailedCapacity = 0.0D;
	        try
	        {
	          diskUsedFailedCapacity = svcUtil.convertBytesIntoGB(chunksNormalUsedFail, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskFreeCapacity = 0.0D;
	        try
	        {
	          diskFreeCapacity = svcUtil.convertBytesIntoGB(chunksNormalUnusedFree, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskUnusedCapacity = 0.0D;
	        try
	        {
	          diskUnusedCapacity = svcUtil.convertBytesIntoGB(chunksNormalUnusedUninit, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskUnusedFailedCapacity = 0.0D;
	        try
	        {
	          diskUnusedFailedCapacity = svcUtil.convertBytesIntoGB(chunksNormalUnusedFail, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskSpareUsedCapacity = 0.0D;
	        try
	        {
	          diskSpareUsedCapacity = svcUtil.convertBytesIntoGB(chunksSpareUsedOK, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskSpareUsedFailedCapacity = 0.0D;
	        try
	        {
	          diskSpareUsedFailedCapacity = svcUtil.convertBytesIntoGB(chunksSpareUsedFail, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskSpareFreeCapacity = 0.0D;
	        try
	        {
	          diskSpareFreeCapacity = svcUtil.convertBytesIntoGB(chunksSpareUnusedFree, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskSpareUnusedCapacity = 0.0D;
	        try
	        {
	          diskSpareUnusedCapacity = svcUtil.convertBytesIntoGB(chunksSpareUnusedUninit, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskSpareUnusedFailedCapacity = 0.0D;
	        try
	        {
	          diskSpareUnusedFailedCapacity = svcUtil.convertBytesIntoGB(chunksSpareUnusedFail, chunkSize);
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskTotalCapacity = 0.0D;
	        try
	        {
	          diskTotalCapacity = diskUsedCapacity + diskUsedFailedCapacity + diskFreeCapacity + diskUnusedCapacity + diskUnusedFailedCapacity + diskSpareUsedCapacity + diskSpareUsedFailedCapacity + diskSpareFreeCapacity + diskSpareUnusedCapacity + diskSpareUnusedFailedCapacity;
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskAllocatedCapacity = 0.0D;
	        try
	        {
	          diskAllocatedCapacity = diskUsedCapacity + diskUsedFailedCapacity + diskFreeCapacity;
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	
	        double diskAllocatedPercentage = 0.0D;
	        try
	        {
	          diskAllocatedPercentage = diskAllocatedCapacity / diskTotalCapacity * 100.0D;
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	        
	        String diskDriveManufacturer = "Not Available";
            String diskDriveSerialNum = "Not Available";
            String diskDriveModel = "Not Available";
            String diskFirmwareVersion = "Not Available";
	        
	        def diskDriveCOP = driverInst.getObjectPath();
	        def diskProps = ThreePARUtils.findDriverPhysicalPackage($smis, diskDriveCOP);
	        diskProps.each{diskProp->
	        	diskDriveManufacturer = diskProp.getProperty("Manufacturer");
	        	diskDriveSerialNum = diskProp.getProperty("SerialNumber");
	        	diskDriveModel = diskProp.getProperty("Model");
	        	 
	        }
	        def softProps = ThreePARUtils.findDiskSoftwareIdentity($smis, diskDriveCOP);
	        softProps.each{softProp->
	        	diskFirmwareVersion = softProp.getProperty("FIRMWARE_VERSION");
	        }
	        
	        def result = $result.create(diskElmName);
	        result.clazz="physicalDisk";
	        result.attr.mDiskID=diskID;
	        result.attr.mfg =diskDriveManufacturer ;
	        result.attr.serial = diskDriveSerialNum;
	        result.attr.productName = diskDriveTemp;
	        
	        result.attr.diskDriveSize = diskDriveSize;
	        
	        result.attr.diskSpeed = diskSpeed;
	        result.attr.diskSystemName = diskSystemName;
	        result.attr.diskPosition = diskPosition;
	        result.attr.diskCageType = diskCageType;
	        result.attr.deviceType=deviceType;

	        result.attr.partNum = diskDriveModel;
	        result.attr.diskFirmwareVersion=diskFirmwareVersion;
	        

	        
		}
	}
}