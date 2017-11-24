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
		def btInstances = $smis.getAssociatedInstances(nsOP, "TPD_NodeBattery", "TPD_Battery", "GroupComponent", "PartComponent");
		btInstances.each{btInst->
			 def serial = $smis.getProperty(btInst,'SerialNumber');
			 def productName = $smis.getProperty(btInst,'ModelName');
			 def partNum = $smis.getProperty(btInst,'SparePartNumber');
			 def mfg = $smis.getProperty(btInst,'Manufacturer');
			 def storResCode = $smis.getProperty(btInst,'DeviceID');
			 def sysname = $smis.getProperty(btInst,'SystemName');
			 def status = $smis.getProperty(btInst,'OperationalStatus')[0]==2?1:0;
			 def name = $smis.getProperty(btInst,'ElementName');
			result.addRow(name, [
			'class','Battery',
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