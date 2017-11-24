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
			def psInstances = $smis.getAssociatedInstances(ssOP, "EMC_SystemDevice", "Clar_PowerDevice", "GroupComponent", "PartComponent");
			psInstances.each{psInst->
				def psOP = psInst.getObjectPath();
				def serial = null;
				def productName = null;
				def partNum = null;
				def mfg = null;
				def pmInstances = $smis.getAssociatedInstances(psOP, "EMC_Realizes", "Clar_PowerModule", "Dependent", "Antecedent");
				pmInstances.each{pmInst->
					 serial = $smis.getProperty(pmInst,'SerialNumber');
					 productName = $smis.getProperty(pmInst,'Model');
					 partNum = $smis.getProperty(pmInst,'PartNumber');
					 mfg = $smis.getProperty(pmInst,'Manufacturer');
				}	
				def name = $smis.getProperty(psInst,'ElementName');
				def storResCode = $smis.getProperty(psInst,'DeviceID');
				def status = ($smis.getProperty(psInst,'OperationalStatus')[0])==2?1:0;
				result.addRow(name, [
					'class','PowerSupply',
					'rs.ComponentOf','node',
					'attr.storResCode',storResCode,
					'state.available_status', status,
					'attr.serial',serial,
					'attr.productName',productName,
					'attr.partNum',partNum,
					'attr.mfg',mfg]);
			}
		}
	}
}