import com.broada.cid.action.impl.action.context.UnitContext;
import java.lang.*;


executeMonitor();


def executeMonitor() {

	def ssInstances = smis.getInstancesByClass('ONTAP_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = smis.getProperty(ssInst,'OtherIdentifyingInfo')[0];
		def ssname = smis.getProperty(ssInst,'ElementName');
		
		def lunInstances = smis.getAssociatedInstances(ssOP, "ONTAP_StorageSystemVolume", "ONTAP_StorageVolume", "GroupComponent", "PartComponent");
		lunInstances.each{lunInst->
	
		def name = smis.getProperty(lunInst,'ElementName');
		def status = smis.getProperty(lunInst,'OperationalStatus')[0]==2?1:0;
		def consumableBlocks = smis.getProperty(lunInst,'ConsumableBlocks');
		def blocksize = smis.getProperty(lunInst,'BlockSize');
		def deviceID = smis.getProperty(lunInst,'DeviceID');
		def capacity = consumableBlocks*blocksize;
		
			result.addRow(name, [
			'class','LUN',
			'rs.ComponentOf' , 'node',
			'attr.storResCode',name+"/"+deviceID,
			'attr.capacity',UnitContext.b2GB(capacity,2),
			'state.available_status', status]);
		}
	}

}