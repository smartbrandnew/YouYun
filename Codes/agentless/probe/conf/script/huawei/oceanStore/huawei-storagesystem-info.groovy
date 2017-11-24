import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;


executeMonitor();


def executeMonitor() {

	def ssInstances = $smis.getInstancesByClass('HuaSy_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def ssname = $smis.getProperty(ssInst,'ElementName');
		def status = $smis.getProperty(ssInst,'OperationalStatus')[0]==2?1:0;
		
		def capacity = 0;
		def unused = 0;
		def spInstances = $smis.getAssociatedInstances(ssOP, "CIM_HostedStoragePool", "SNIA_StoragePool", "GroupComponent", "PartComponent");
		for(sp in spInstances){
		def spname = $smis.getProperty(sp,'Primordial');
			if(spname==TRUE||spname=='TRUE'){
			continue;
			}
		 unused += $smis.getProperty(spInst,'RemainingManagedSpace');
		 capacity += $smis.getProperty(spInst,'TotalManagedSpace');

		}
		
			result.addRow(ssname, [
			'class','DiskArrayHuawei',
			'perf.stor_use.capacity_used',$unit.b2GB(capacity - unused, 3),
			'perf.stor_use.capacity_unused',$unit.b2GB(unused,3),
			'perf.stor_use.capacity_useage',NumberContext.round(used*100/capacity,2),
			'state.available_status', status]);
		
	}

}