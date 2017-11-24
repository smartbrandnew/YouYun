import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;


executeMonitor();


def executeMonitor() {

	def ssInstances = $smis.getInstancesByClass('HuaSy_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def scInstances = $smis.getAssociatedInstances(ssOP, "CIM_ComponentCS", "HuaSy_StorageControllerSystem", "GroupComponent", "PartComponent");
		scInstances.each{scInst->
		def svOP = scInst.getObjectPath();

		def svName = $smis.getProperty(scInst,'ElementName');
		//def consumableBlocks = $smis.getProperty(scInst, 'ConsumableBlocks');
		//def blockSize = $smis.getProperty(scInst, 'BlockSize');
		//def capacity = $unit.b2GB(blockSize * $smis.getProperty(scInst, 'NumberOfBlocks'), 3)
		//def usedCapacity = $unit.b2GB(consumableBlocks* blockSize, 3)
		def status = $smis.getProperty(scInst,'OperationalStatus')[0]==2?1:0;
		def storResCode = $smis.getProperty(scInst,'DeviceId');
		
			result.addRow(svName, [
			'class','RAIDController',
			'rs.ComponentOf','node',
			'attr.storResCode',storResCode,
			'state.available_status', status
			//'perf.stor_use.capacity_unused',$unit.b2GB(capacity - usedCapacity, 3),
			//'perf.stor_use.capacity_useage',NumberContext.round(usedCapacity * 100 / capacity, 3),
			//'perf.stor_use.capacity_used',usedCapacity,
			]);
		}
	}

}