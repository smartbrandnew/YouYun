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

		
		
		def psInstances = $smis.getAssociatedInstances(nsOP, "TPD_SystemPowerSupply", "TPD_NodePowerSupply", "GroupComponent", "PartComponent");
		psInstances.each{psInst->
			def serial = $smis.getProperty(psInst,'SerialNumber');
			def productName = $smis.getProperty(psInst,'ModelName');
			def partNum = $smis.getProperty(psInst,'SparePartNumber');
			def mfg = $smis.getProperty(psInst,'Manufacturer');
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