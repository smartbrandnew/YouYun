import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {

	
	def ssInstances = $smis.getInstancesByClass('HuaSy_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def svInstances = $smis.getAssociatedInstances(ssOP, "HuaSy_SystemDevice", "HuaSy_StorageVolume", "GroupComponent", "PartComponent");
		svInstances.each{svInst->
		def svOP = svInst.getObjectPath();

		def svName = $smis.getProperty(svInst,'ElementName');
		def DeviceID = $smis.getProperty(svInst,'DeviceID');
		def consumableBlocks = $smis.getProperty(svInst, 'ConsumableBlocks');
		def blockSize = $smis.getProperty(svInst, 'BlockSize');
		def capacity = $unit.b2GB(blockSize * $smis.getProperty(svInst, 'NumberOfBlocks'), 3)
		def usedCapacity = $unit.b2GB(consumableBlocks* blockSize, 3)
		def status = $smis.getProperty(svInst,'OperationalStatus')[0]==2?1:0;
		def storResCode = $smis.getProperty(svInst,'DeviceId');
		
			result.addRow(svName, [
			'class','StorageVolume',
			'rs.ComponentOf','node',
			'attr.storResCode',DeviceID,
			'state.available_status', status,
			'perf.stor_use.capacity_unused',$unit.b2GB(capacity - usedCapacity, 3),
			'perf.stor_use.capacity_useage',NumberContext.round(usedCapacity * 100 / capacity, 3),
			'perf.stor_use.capacity_used',usedCapacity]);
		}
	}

}