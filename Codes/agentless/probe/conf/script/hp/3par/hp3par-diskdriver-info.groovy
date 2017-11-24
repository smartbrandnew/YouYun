import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	def ssInstances = $smis.getInstancesByClass('TPD_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = $smis.getProperty(ssInst,'OtherIdentifyingInfo')[1];
		
		def ddInstances = $smis.getAssociatedInstances(ssOP, "TPD_SystemDisk", "TPD_DiskDrive", "GroupComponent", "PartComponent");
		ddInstances.each{ddInst->
		def ddOP = ddInst.getObjectPath();
		def ddName = $smis.getProperty(ddInst,'ElementName');
		def deviceid = $smis.getProperty(ddInst,'DeviceID');
		def checksize = $smis.getProperty(ddInst,'ChunkSize');
		def unused = $smis.getProperty(ddInst,'ChunksNormalUnusedFree');
		//def nused = $smis.getProperty(ddInst,'ChunksNormalUsedOK');
		//def sused = $smis.getProperty(ddInst,'ChunksNormalUnusedFree');
		def formFactor = $smis.getProperty(ddInst,'FormFactor');
			if(formFactor=="0"||formFactor=="2"||formFactor==2||formFactor==0){
				formFactor = "未知";
			}else if(formFactor=="1"||formFactor==1){
				formFactor = "其他";
			}else if(formFactor=="3"||formFactor==3){
				formFactor = "5.25 inch";
			}else if(formFactor=="4"||formFactor==4){
				formFactor = "3.5 inch";
			}else if(formFactor=="5"||formFactor==5){
				formFactor = "2.5 inch";
			}else if(formFactor=="6"||formFactor==6){
				formFactor = "1.8 inch";
			}
		 def encryption = $smis.getProperty(ddInst,'Encryption');
			if(encryption=="0"||encryption==0){
				encryption="未知";
			}else if(encryption=="1"||encryption==1){
				encryption="不支持";
			}else if(encryption=="2"||encryption==2){
				encryption="未加密";
			}else if(encryption=="3"||encryption==3){
				encryption="加密";
			}
		def  diskType = $smis.getProperty(ddInst,'DiskType');
			if(diskType=="1"||diskType==1){
				diskType="其他";
			}else if(diskType=="2"||diskType==2){
				diskType = "机械硬盘";
			}else if(diskType=="3"||diskType==3){
				diskType = "固态硬盘";
			}else if(diskType=="0"||diskType==0){
				diskType = "未知类型";
			}
		def status = null;
		def capacity = null;
		def deInstances = $smis.getAssociatedInstances(ddOP, "TPD_DiskPresent", "TPD_DiskStorageExtent", "Antecedent", "Dependent");
		deInstances.each{deInst->
			def deOP = deInst.getObjectPath();
			status = $smis.getProperty(deInst,'OperationalStatus')[0]==2?1:0;
			def bs = $smis.getProperty(deInst,'BlockSize');
			def cb = $smis.getProperty(deInst,'ConsumableBlocks');
			capacity =bs*cb;
			//def deInstances = $smis.getAssociatedInstances(deOP, "TPD_DiskElementStatisticalData", "TPD_DiskStatisticalData", "ManagedElement", "Stats");
		}
		
		//def dsInstances = $smis.getAssociatedInstances(ddOP, "TPD_DiskSoftware", "TPD_DiskSoftwareIdentity", "Dependent", "Antecedent");
		def serial = null;
		def mfg = null;
		def partNum = null;
		def dpInstances = $smis.getAssociatedInstances(ddOP, "TPD_DiskPackageRealizes", "TPD_DiskDrivePackage", "Dependent", "Antecedent");
		dpInstances.each{dpInst->
			mfg = $smis.getProperty(dpInst,'Manufacturer');
			serial = $smis.getProperty(dpInst,'SerialNumber');
			partNum = $smis.getProperty(dpInst,'Model');
		}
		
		def unusecapacity = unused*checksize;
		def usdcapacity = capacity-unusecapacity;
			result.addRow(ddName, [
			'class','physicalDisk',
			'rs.ComponentOf','node',
			'state.available_status', status,
			'perf.stor_use.capacity_unused',UnitContext.b2GB(unusecapacity, 3),
			'perf.stor_use.capacity_useage',NumberContext.round((capacity-unusecapacity) * 100 / capacity, 0),
			'perf.stor_use.capacity_used',UnitContext.b2GB(capacity-unusecapacity, 3),
			'attr.capacity',UnitContext.b2GB(capacity,3),
			'attr.formFactor',formFactor,
			'attr.encryption',encryption,
			'attr.diskType',diskType,
			'attr.serial',serial,
			'attr.partNum',partNum,
			'attr.mfg',mfg]);
		}
		}

}