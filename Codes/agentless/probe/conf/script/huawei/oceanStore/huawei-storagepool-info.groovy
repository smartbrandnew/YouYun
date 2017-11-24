import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	def ssInstances = $smis.getInstancesByClass('HuaSy_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def spInstances = $smis.getAssociatedInstances(ssOP, "CIM_HostedStoragePool", "SNIA_StoragePool", "GroupComponent", "PartComponent");
		spInstances.each{spInst->
		def spOP = spInst.getObjectPath();
		def spName = $smis.getProperty(spInst,'ElementName');
		def spInstanceId = $smis.getProperty(spInst,'InstanceID');
		def poolId = $smis.getProperty(spInst,'PoolID');
		def primordial = $smis.getProperty(spInst, 'Primordial')=='TRUE'?'��':'��';
		def unused = $smis.getProperty(spInst,'RemainingManagedSpace');
		def capacity = $smis.getProperty(spInst,'TotalManagedSpace');
		def status = $smis.getProperty(spInst,'OperationalStatus')[0]==2?1:0;
		

			result.addRow(spName, [
			'class','StoragePool',
			'rs.ComponentOf','node',
			'state.available_status', status,
			'attr.storResCode',spName+'/'+spInstanceId,
			'perf.stor_manage.space_unallocated',$unit.b2GB(unused, 3),
			'perf.stor_manage.space_useage',NumberContext.round(unused * 100 / capacity, 0),
			'perf.stor_manage.space_allocated',$unit.b2GB(capacity-unused, 3),
			'attr.primordial', primordial]);
		}
	}
}