import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();

def executeMonitor() {

	
	def ssInstances = $smis.getInstancesByClass('HuaSy_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def scInstances = $smis.getAssociatedInstances(ssOP, "CIM_ComponentCS", "HuaSy_StorageControllerSystem", "GroupComponent", "PartComponent");
		scInstances.each{scInst->
		def scOP = scInst.getObjectPath();
		def fcInstances = $smis.getAssociatedInstances(scOP, "HuaSy_SystemDevice", "HuaSy_FrontEndFCPort", "GroupComponent", "PartComponent");
		fcInstances.each{fcInst-> 
			 def porttype = getFCPortType($smis.getProperty(fcInst,'PortType'));
			 
			 def speed = $smis.getProperty(fcInst,'Speed');
			 def fcname = $smis.getProperty(fcInst,'ElementName');
			 def status = $smis.getProperty(fcInst,'OperationalStatus')[0]==2?1:0;
			
			 result.addRow(fcname, [
			'class','FCPort',
			'rs.ComponentOf','node',
			'state.available_status', status,
			'attr.speed',speed,
			'attr.storResCode',$smis.getProperty(fcInst,'DeviceId'),
			'attr.portType', porttype]);
			
		}
		
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