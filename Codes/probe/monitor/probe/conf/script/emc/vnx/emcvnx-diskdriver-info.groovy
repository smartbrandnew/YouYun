import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	def ip = param.get('monitorNode').getIpAddress();
	def ssInstances = $smis.getInstancesByClass('Clar_StorageSystem');
	
	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def spInstances = $smis.getAssociatedInstances(ssOP, "EMC_ComponentCS", "Clar_StorageProcessorSystem", "GroupComponent", "PartComponent");
		def isnow = false;
		spInstances.each{spInst->
			def spOP = spInst.getObjectPath();
			def haInstances = $smis.getAssociatedInstances(spOP, "EMC_HostedAccessPoint", "Clar_RemoteServiceAccessPoint", "Antecedent", "Dependent");
			haInstances.each{haInst->
			 def SPip = $smis.getProperty(haInst,'AccessInfo');
			 if(SPip==ip)
				isnow = true;
			}
		}
		if(isnow){
			def ddInstances = $smis.getAssociatedInstances(ssOP, "EMC_SystemDevice", "Clar_DiskDrive", "GroupComponent", "PartComponent");
			ddInstances.each{ddInst->
				def ddOP = ddInst.getObjectPath();
				def ddName = $smis.getProperty(ddInst,'ElementName');
				def deviceid = $smis.getProperty(ddInst,'DeviceID');
				def total = $smis.getProperty(ddInst,'EMCActualBlocks');
				def free = $smis.getProperty(ddInst,'EMCFreeBlocks');
				def size = $smis.getProperty(ddInst,'DefaultBlockSize');
				def mfg = $smis.getProperty(ddInst,'EMCManufacturer');
				def serialNumber = $smis.getProperty(ddInst,'EMCSerialNumber');
				
				def formFactor = $smis.getProperty(ddInst,'FormFactor');
				if(formFactor=="0"||formFactor=="2"||formFactor==2||formFactor==0)
					formFactor = "未知";
				else if(formFactor=="1"||formFactor==1)
					formFactor = "其他";
				else if(formFactor=="3"||formFactor==3)
					formFactor = "5.25 inch";
				else if(formFactor=="4"||formFactor==4)
					formFactor = "3.5 inch";
				else if(formFactor=="5"||formFactor==5)
					formFactor = "2.5 inch";
				else if(formFactor=="6"||formFactor==6)
					formFactor = "1.8 inch";
				
				def encryption = $smis.getProperty(ddInst,'Encryption');
				if(encryption=="0"||encryption==0)
					encryption="未知";
				else if(encryption=="1"||encryption==1)
					encryption="不支持";
				else if(encryption=="2"||encryption==2)
					encryption="未加密";
				else if(encryption=="3"||encryption==3)
					encryption="加密";
				
				def  diskType = $smis.getProperty(ddInst,'DiskType');
				if(diskType=="1"||diskType==1)
					diskType="其他";
				else if(diskType=="2"||diskType==2)
					diskType = "机械硬盘";
				else if(diskType=="3"||diskType==3)
					diskType = "固态硬盘";
				else if(diskType=="0"||diskType==0)
					diskType = "未知类型";

				def capacity = total*size;
				if(capacity!=0){
					def dmInstances = $smis.getAssociatedInstances(ddOP, "EMC_Realizes", "Clar_DiskModule", "Dependent", "Antecedent");
					def version = null;
					def partNum = null;
					def status = null;
					dmInstances.each{dmInst->
						version = $smis.getProperty(dmInst,'Version');
						partNum = $smis.getProperty(dmInst,'Model');
						status = $smis.getProperty(dmInst,'OperationalStatus')[0]==2?1:0;
					}
					
					def unusecapacity = free*size;
					def usecapacity = capacity-unusecapacity;
					result.addRow(ddName, [
					'class','physicalDisk',
					'rs.ComponentOf','node',
					'state.available_status', status,
					'perf.stor_use.capacity',UnitContext.b2GB(capacity, 3),
					'perf.stor_use.capacity_unused',UnitContext.b2GB(unusecapacity, 3),
					'perf.stor_use.capacity_usage',NumberContext.round(usecapacity * 100 / capacity, 0),
					'perf.stor_use.capacity_used',UnitContext.b2GB(usecapacity, 3),
					'attr.capacity',UnitContext.b2GB(capacity,3),
					'attr.formFactor',formFactor,
					'attr.encryption',encryption,
					'attr.diskType',diskType,
					'attr.serial',serialNumber,
					'attr.partNum',partNum,
					'attr.mfg',mfg]);
				}
			}
		}
	}
}

def getPackageType(def type) {
	switch (type) {
		case 0:
			return 'Unknown';
		case 1:
			return 'Other';
		case 2:
			return 'Rack';
		case 3:
			return 'Chassis/Frame';
		case 4:
			return 'Cross Connect/Backplane';
		case 5:
			return 'Container/Frame Slot';
		case 6:
			return 'Power Supply';
		case 7:
			return 'Fan';
		case 8:
			return 'Sensor';
		case 9:
			return 'Module/Card';
		case 10:
			return 'Port/Connector';
		case 11:
			return 'Battery';
		case 12:
			return 'Processor';
		case 13:
			return 'Memory';
		case 14:
			return 'Power Source/Generator';
		case 15:
			return 'Storage Media Package (e.g., Disk or Tape Drive)';
		case 16:
			return 'Blade';
		case 17:
			return 'Blade Expansion';
	}
}