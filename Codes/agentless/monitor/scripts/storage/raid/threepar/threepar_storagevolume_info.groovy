/*!Action
 action.name=通过smi-s协议监测THREEPAR存储卷信息
 action.descr=通过smi-s协议THREEPAR存储卷信息
 action.protocols=smis
 monitor.output=THREEPAR-STORAGEVOLUME-INFO
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
		def spInstances = ThreePARUtils.findSPInstances($smis, cop);
		spInstances.each{spInst->
	         CIMObjectPath spCOP = storagePoolInst.getObjectPath();
	         def svInstances = ThreePARUtils.findSVInstances($smis, spCOP);
	         svInstances.each{svInst->
		         String caption = svInst.getProperty("Caption");
	             String svElementName = svInst.getProperty("ElementName");
	             svDeviceId = svInst.getProperty("DeviceID");
	
	             String svBlockSize = svInst.getProperty("BlockSize");
	
	             String svNumberOfBlocks = svInst.getProperty("NumberOfBlocks");
	             double svTotalCapacity = 0.0D;
	             try
	             {
	               svTotalCapacity = svcUtil.convertBytesIntoGB(svNumberOfBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String svConsumableBlocks = svInst.getProperty("ConsumableBlocks");
	             double svConsumableCapacity = 0.0D;
	             try
	             {
	               svConsumableCapacity = svcUtil.convertBytesIntoGB(svConsumableBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               out.warning("[ ThreePARArrayModeller - modelData ] Exception while calculating the Storage Volume Consumable Capacity. Message = " + e.getMessage());
	               e.printStackTrace();
	             }
	             String svName = svInst.getProperty("Name");
	
	             String thinlyProvisioned = svInst.getProperty("ThinlyProvisioned");
	             String numberOfRawBlocks = svInst.getProperty("NumberOfRawBlocks");
	             String raidType = "Not Available";
	             try
	             {
	               raidType = ThreePARUtils.getRaidType(svInst.getProperty("RaidType"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String volumeType = "Not Available";
	             try
	             {
	               volumeType = ThreePARUtils.getVolumeType(svInst.getProperty("VolumeType"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	             String rawAdminBlocks = svInst.getProperty("RawAdminBlocks");
	
	             double svRawCapacity = 0.0D;
	             try
	             {
	               svRawCapacity = svcUtil.convertBytesIntoGB(numberOfRawBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             double svRawAdminCapacity = 0.0D;
	             try
	             {
	               svRawAdminCapacity = svcUtil.convertBytesIntoGB(rawAdminBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               out.warning("[ ThreePARArrayModeller - modelData ] Exception while calculating the Storage Volume Raw Admin Capacity. Message = " + e.getMessage());
	               e.printStackTrace();
	             }
	             String consumableAdminBlocks = svInst.getProperty("ConsumableAdminBlocks");
	             double consumableAdminCapacity = 0.0D;
	             try
	             {
	               consumableAdminCapacity = svcUtil.convertBytesIntoGB(consumableAdminBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	             String rawCopyBlocks = svInst.getProperty("RawCopyBlocks");
	             double svRawCopyCapacity = 0.0D;
	             try
	             {
	               svRawCopyCapacity = svcUtil.convertBytesIntoGB(rawCopyBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String consumableCopyBlocks = svInst.getProperty("ConsumableCopyBlocks");
	             double consumableCopyCapacity = 0.0D;
	             try
	             {
	               consumableCopyCapacity = svcUtil.convertBytesIntoGB(consumableCopyBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String usedSnapshotAdminSpaceBlocks = svInst.getProperty("UsedSnapshotAdminSpaceBlocks");
	             double snapShotUsedAdminCapacity = 0.0D;
	             try
	             {
	               snapShotUsedAdminCapacity = svcUtil.convertBytesIntoGB(usedSnapshotAdminSpaceBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String usedSnapshotDataSpaceBlocks = svInst.getProperty("UsedSnapshotDataSpaceBlocks");
	             double snapShotUsedDataCapacity = 0.0D;
	             try
	             {
	               snapShotUsedDataCapacity = svcUtil.convertBytesIntoGB(usedSnapshotDataSpaceBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String createdDate = null;
	             try
	             {
	               createdDate = ThreePARUtils.convertDateTimeToSANDate(svInst.getProperty("DateCreated").getValue().toString());
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String masterNode = svInst.getProperty("MasterNode");
	             String backupNode1 = svInst.getProperty("BackupNode1");
	             String backupNode2 = svInst.getProperty("BackupNode2");
	             String policy = "Not Available";
	             try
	             {
	               policy = ThreePARUtils.getVolumePolicy(svInst.getProperty("Policy"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	             String geometrySectorsPerTrack = svInst.getProperty("GeometrySectorsPerTrack");
	             String geometryHeadsPerCylinder = svInst.getProperty("GeometryHeadsPerCylinder");
	             String geometrySectorSize = svInst.getProperty("GeometrySectorSize");
	             String preferredAvailability = "Not Available";
	             try
	             {
	               preferredAvailability = ThreePARUtils.getVolumeAvailability(svInst.getProperty("PreferredAvailability"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String currentAvailability = "Not Available";
	             try
	             {
	               currentAvailability = ThreePARUtils.getVolumeAvailability(svInst.getProperty("CurrentAvailability")));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String exportState = "Not Available";
	             try
	             {
	               exportState = ThreePARUtils.getExportState(svInst.getProperty("ExportState"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String diskDeviceType = "Not Available";
	             try
	             {
	               diskDeviceType = ThreePARUtils.getDiskDeviceType(svInst.getProperty("DiskDeviceType"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String snapSpaceAllocationWarning = svInst.getProperty("SnapSpaceAllocationWarning");
	             String snapSpaceAllocationLimit = svInst.getProperty("SnapSpaceAllocationLimit");
	             String userSpaceAllocationWarning = svInst.getProperty("UserSpaceAllocationWarning");
	             String userSpaceAllocationLimit = svInst.getProperty("UserSpaceAllocationLimit");
	             String snapDSPName = svInst.getProperty("SnapDSPName");
	             String userDSPName = svInst.getProperty("UserDSPName");
	             String provisionedConsumableBlocks = svInst.getProperty("ProvisionedConsumableBlocks");
	             double provisionedConsumableCapacity = 0.0D;
	             try
	             {
	               provisionedConsumableCapacity = svcUtil.convertBytesIntoGB(provisionedConsumableBlocks, svBlockSize);
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	             String domain = svInst.getProperty("Domain");
	             String setSize = svInst.getProperty("SetSize");;
	             String setData = svInst.getProperty("SetData");
	             String provisioningType = "Not Available";
	             try
	             {
	               provisioningType = ThreePARUtils.getProvisioningType(svInst.getProperty("ProvisioningType"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String activity = "Not Available";
	             try
	             {
	               activity = ThreePARUtils.getVolumeActivity(svInst.getProperty("Activity"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	             String volumeOperStatus = "Not Available";
	             try
	             {
	               volumeOperStatus = ThreePARUtils.getOperationalStatus(svInst.getProperty("OperationalStatus"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	             
	             def result = $result.create(storageVolName);
				 result.clazz = 'StorageVolume';
			     result.attr.capacity = svTotalCapacity;
				 result.perf.stor_use.capacity_unused = svConsumableCapacity;
				 result.perf.stor_use.capacity_useage = NumberContext.round((svTotalCapacity - svConsumableCapacity) * 100 / svTotalCapacity, 3);
				 result.perf.stor_use.capacity_used = svTotalCapacity - svConsumableCapacity;
				 result.state.available_status = (volumeOperStatus == 'OK' ? 1 : 0);
	
		     }
		}
	}
}