import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {

	def ssInstances = $smis.getInstancesByClass('TPD_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = $smis.getProperty(ssInst,'OtherIdentifyingInfo')[1];
		

		def nsInstances = $smis.getAssociatedInstances(ssOP, "TPD_NodeComponentCS", "TPD_NodeSystem", "GroupComponent", "PartComponent");
		nsInstances.each{nsInst->
		def nsOP = nsInst.getObjectPath();		
		def fanInstances = $smis.getAssociatedInstances(nsOP, "TPD_SystemFan", "TPD_Fan", "GroupComponent", "PartComponent");
		fanInstances.each{fanInst->
			 def name = $smis.getProperty(fanInst,'ElementName');
			 def storResCode = $smis.getProperty(fanInst,'DeviceID');
			 def status = $smis.getProperty(fanInst,'OperationalStatus')[0]==2?1:0;
			result.addRow(storResCode, [
			'class','FAN',
			'rs.ComponentOf','node',
			'attr.storResCode',storResCode,
			'state.available_status', status]);
		
		}
		}
	}
}