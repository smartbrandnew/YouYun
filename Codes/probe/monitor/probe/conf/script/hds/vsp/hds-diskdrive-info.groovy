import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;


executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	ssInstances.each{ssInst->

	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'ElementName');
	
	def ddInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_StorageSystemDeviceDiskDrive", "HITACHI_DiskDrive", "GroupComponent", "PartComponent");
	ddInstances.each{ddInst->

	def ddOP = ddInst.getObjectPath();
	def name = $smis.getProperty(ddInst,'ElementName');
	def status = ($smis.getProperty(ddInst,'OperationalStatus')[0])==2?1:0;
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
	def capacity = null;
	def deInstances = $smis.getAssociatedInstances(ddOP, "HITACHI_DiskMediaPresent", "HITACHI_DiskExtent", "Antecedent", "Dependent");
	deInstances.each{deInst->
		def ConsumableBlocks = $smis.getProperty(deInst,'ConsumableBlocks');
		def BlockSize = $smis.getProperty(deInst,'BlockSize');
		capacity = ConsumableBlocks*BlockSize;
	}
	
	def serial = null;
	def mfg = null;
	def partNum = null;
	def ddvInstances = $smis.getAssociatedInstances(ddOP, "HITACHI_DiskDriveDiskDriveView", "HITACHI_DiskDriveView", "Antecedent", "Dependent");
	ddvInstances.each{ddvInst->
		 serial = $smis.getProperty(ddvInst,'PPSerialNumber');
		 mfg = $smis.getProperty(ddvInst,'PPManufacturer');
		 partNum = $smis.getProperty(ddvInst,'PPModel');
	}

	
	
	result.addRow(name, [
	'class','physicalDisk',
	'rs.ComponentOf','node',
	'attr.serial',serial,
	'attr.partNum',partNum,
	'attr.mfg',mfg,
	'attr.capacity',UnitContext.b2GB(capacity),
	'attr.formFactor',formFactor,
	'attr.encryption',encryption,
	'attr.diskType',diskType,
	'state.available_status', status]);
	}
	}
	}