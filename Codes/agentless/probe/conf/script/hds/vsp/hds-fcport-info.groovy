import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;


executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	ssInstances.each{ssInst->
	println("���Կ�ʼ1��");
	println(ssInst);
	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'ElementName');
	
	def spInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_ComponentCS", "HITACHI_StorageProcessorSystem", "GroupComponent", "PartComponent");
	spInstances.each{spInst->
	def spOP = spInst.getObjectPath();
	
	def fcInstances = $smis.getAssociatedInstances(spOP, "HITACHI_StorageProcessorSystemDeviceFCPort", "HITACHI_FCPort", "GroupComponent", "PartComponent");
	fcInstances.each{fcInst->
	def svname = $smis.getProperty(fcInst,'ElementName');
	def DeviceID = $smis.getProperty(fcInst,'DeviceID');
	def snname = $smis.getProperty(fcInst,'SystemName');
	def speed = $smis.getProperty(fcInst,'Speed');
	def portNum = $smis.getProperty(fcInst,'PortNumber');
	def portType = getFCPortType($smis.getProperty(fcInst,'PortType'));
	def status = $smis.getProperty(fcInst,'OperationalStatus')[0]==2?1:0;
	
	result.addRow(svname+"-"+snname, [
	'class','FCPort',
	'rs.ComponentOf','node',
	'attr.storResCode',svname+"/"+DeviceID,
	'attr.portIdx',portNum,
	'attr.portAlias',svname+"-"+snname,
	'attr.portType',portType,
	'attr.speed',speed,
	'state.available_status', status]);
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