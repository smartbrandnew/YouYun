import java.lang.*;

executeMonitor();

def executeMonitor() {
	def ip = param.get('monitorNode').getIpAddress();
	def ssInstances = $smis.getInstancesByClass('Clar_BackEndFCPort');
	ssInstances.each{fcInst->
		def ssOP = fcInst.getObjectPath();
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
			def status = $smis.getProperty(fcInst,'OperationalStatus')[0]==2?1:0;
			def name = $smis.getProperty(fcInst,'ElementName');
			def storResCode = $smis.getProperty(fcInst,'DeviceID');
			def portNum = $smis.getProperty(fcInst,'PortNumber');
			def portType = getFCPortType($smis.getProperty(fcInst,'PortType'));
			result.addRow(name, [
			'class','FCPort',
			'rs.ComponentOf','node',
			'attr.storResCode',storResCode,
			'attr.portIdx',portNum,
			'attr.portAlias',name,
			'attr.portType',portType,
			'state.available_status', status]);
		}
	}
}

def getFCPortType(def portTypeIndex) {
	def portType = null;
	switch(portTypeIndex) {
		case 0:
			portType = 'Unknown';
		case 1:
			portType = 'Other';
			break;
		case 10:
			portType = 'N';
			break;
		case 11:
			portType = 'NL';
			break;
		case 12:
			portType = 'F/NL';
			break;
		case 13:
			portType = 'Nx';
			break;
		case 14:
			portType = 'E';
			break;
		case 15:
			portType = 'F';
			break;
		case 16:
			portType = 'FL';
			break;
		case 17:
			portType = 'B';
			break;
		case 18:
			portType = 'G';
			break;
		case 16000..65535:
			portType = 'Vendor Reserved';
			break;
		
	}
}