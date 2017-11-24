import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;


executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	ssInstances.each{ssInst->
	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'ElementName');
	
	def svInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_StorageSystemDeviceStorageVolume", "HITACHI_StorageVolume", "GroupComponent", "PartComponent");
	svInstances.each{svInst->
	def svname = $smis.getProperty(svInst,'ElementName');
	def status = $smis.getProperty(svInst,'OperationalStatus')[0]==2?1:0;
	def total = $smis.getProperty(svInst,'Consumed');
	def ConsumableBlocks = $smis.getProperty(svInst,'ConsumableBlocks');
	def BlockSize = $smis.getProperty(svInst,'BlockSize');
	def deviceID = $smis.getProperty(svInst,'DeviceID');
	def capacity = ConsumableBlocks*BlockSize;
	
	result.addRow(svname, [
	'class','StorageVolume',
	'rs.ComponentOf','node',
	'attr.storResCode',svname+"/"+deviceID,
	'attr.capacity',UnitContext.b2GB(capacity,2),
	'state.available_status', status]);
	}
	}
	}