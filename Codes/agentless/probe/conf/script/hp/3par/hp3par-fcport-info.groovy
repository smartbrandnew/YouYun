import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	
	def ssInstances = $smis.getInstancesByClass('TPD_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
			def nsInstances = $smis.getAssociatedInstances(ssOP, "TPD_NodeComponentCS", "TPD_NodeSystem", "GroupComponent", "PartComponent");
			nsInstances.each{nsInst->
				def nsOP = nsInst.getObjectPath();
				def npInstances = $smis.getAssociatedInstances(nsOP, "TPD_SystemFCPort", "TPD_FCPort", "GroupComponent", "PartComponent");

				npInstances.each{npInst->
					def npOP = npInst.getObjectPath();
					nsName = $smis.getProperty(npInst,'ElementName');
					def status = ($smis.getProperty(npInst,'OperationalStatus')[0])==2?1:0;
					def name = $smis.getProperty(npInst,'Name');
					def deviceID = $smis.getProperty(npInst,'DeviceID');
					def portIndex = $smis.getProperty(npInst,'PortNumber');
					def fullDuplex = $smis.getProperty(npInst,'FullDuplex')==TRUE?'全双工':'半双工';
					def speed = UnitContext.b2MB($smis.getProperty(npInst,'Speed'),2);

						result.addRow(nsName, [
						'class','FCPort',
						'rs.ComponentOf','node',
						'attr.portAlias',name+"/"+deviceID,
						'attr.portIdx',portIndex,
						'attr.communicationMode',fullDuplex,
						'state.available_status', status,
						'attr.speed',speed]);
				}
			
		}
	}
}