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
			def btInstances = $smis.getAssociatedInstances(ssOP, "EMC_SystemDevice", "Clar_BatteryDevice", "GroupComponent", "PartComponent");
			btInstances.each{btInst->
				def btOP = btInst.getObjectPath();	
				def mfg = null;
				def productName
				def bmInstances = $smis.getAssociatedInstances(btOP, "EMC_Realizes", "Clar_BatteryModule", "Dependent", "Antecedent");
				bmInstances.each{bmInst->
					productName = $smis.getProperty(bmInst,'Model');
					mfg = $smis.getProperty(bmInst,'Manufacturer');
				}
				def status = $smis.getProperty(btInst,'OperationalStatus')[0]==2?1:0;
				def name = $smis.getProperty(btInst,'DeviceID');
				def storResCode = $smis.getProperty(btInst,'DeviceID');
				result.addRow(name, [
				'class','Battery',
				'rs.ComponentOf','node',
				'attr.storResCode',storResCode,
				'attr.mfg',mfg,
				'attr.productName',productName,
				'state.available_status', status]);
			}
	    }
	}
}