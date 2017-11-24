import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;

executeMonitor();


def executeMonitor() {
	def ip = param.get('monitorNode').getIpAddress();
	def acInstances = $smis.getInstancesByClass('HuaSy_ArrayChassis');

	acInstances.each{acInst->
		def acOP = acInst.getObjectPath();
		def ecInstances = $smis.getAssociatedInstances(acOP, "CIM_Container", "CIM_Chassis", "GroupComponent", "PartComponent");;
		ecInstances.each{ecInst->
		def ecOP = ecInst.getObjectPath();
		def dpInstances = $smis.getAssociatedInstances(ecOP, "HuaSy_PackageInChassis", "HuaSy_PhysicalPackage", "GroupComponent", "PartComponent");
		dpInstances.each{dpInst->
		def dpOP = dpInst.getObjectPath();
		def dpname =  $smis.getProperty(dpInst,'ElementName');
		def dpmanufacturer =  $smis.getProperty(dpInst,'Manufacturer');
		def dpmodel =  $smis.getProperty(dpInst,'Model');
		def dpserial =  $smis.getProperty(dpInst,'SerialNumber');
		def dpversion =  $smis.getProperty(dpInst,'Version');
		
		def ddname = null;
		def status = null;
		def ddcaption = null;
		def capacity = null;
		def primordial = null;
		def formFactor = null;
		def encryption = null;	
		def diskType = null;
		def ddInstances = $smis.getAssociatedInstances(dpOP, "CIM_Realizes", "CIM_DiskDrive", "Antecedent", "Dependent");
		ddInstances.each{ddInst->
			def ddOP = ddInst.getObjectPath();
			ddname =  $smis.getProperty(ddInst,'ElementName');
			status =  $smis.getProperty(ddInst,'OperationalStatus')[0];
			ddcaption =  $smis.getProperty(ddInst,'Caption');
			formFactor = $smis.getProperty(ddInst,'FormFactor');
			if(status==2||status=='2'){
			stauts = 1;
			}else{
			status = 0;
			}
			if(formFactor=="0"||formFactor=="2"||formFactor==2||formFactor==0){
				formFactor = "δ֪";
			}else if(formFactor=="1"||formFactor==1){
				formFactor = "����";
			}else if(formFactor=="3"||formFactor==3){
				formFactor = "5.25 inch";
			}else if(formFactor=="4"||formFactor==4){
				formFactor = "3.5 inch";
			}else if(formFactor=="5"||formFactor==5){
				formFactor = "2.5 inch";
			}else if(formFactor=="6"||formFactor==6){
				formFactor = "1.8 inch";
			}
			encryption = $smis.getProperty(ddInst,'Encryption');
			if(encryption=="0"||encryption==0){
				encryption="δ֪";
			}else if(encryption=="1"||encryption==1){
				encryption="��֧��";
			}else if(encryption=="2"||encryption==2){
				encryption="δ����";
			}else if(encryption=="3"||encryption==3){
				encryption="����";
			}
			diskType = $smis.getProperty(ddInst,'DiskType');
			if(diskType=="1"||diskType==1){
				diskType="����";
			}else if(diskType=="2"||diskType==2){
				diskType = "��еӲ��";
			}else if(diskType=="3"||diskType==3){
				diskType = "��̬Ӳ��";
			}else if(diskType=="0"||diskType==0){
				diskType = "δ֪����";
			}

			
			def deInstances = $smis.getAssociatedInstances(ddOP, "HuaSy_MediaPresent", "Nex_DiskExtent", "Antecedent", "Dependent");
			deInstances.each{deInst->
			def blockSize =  $smis.getProperty(deInst,'BlockSize');
			def consumableBlocks =  $smis.getProperty(deInst,'ConsumableBlocks');
			capacity = blockSize*consumableBlocks;
			}
			

		
		result.addRow(ddname+"-"+ip, [
			'class','physicalDisk',
			'rs.ComponentOf','node',
			'state.available_status', status,
			'attr.mfg',dpmanufacturer,
			'attr.serial',dpserial,
			'attr.partNum',dpmodel,
			'attr.capacity',UnitContext.b2GB(capacity),
			'attr.formFactor',formFactor,
			'attr.encryption',encryption,
			'attr.diskType',diskType]);
		}
		}
		}
	}
}