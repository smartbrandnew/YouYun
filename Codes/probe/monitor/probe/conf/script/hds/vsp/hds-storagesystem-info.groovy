import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;


executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	

	ssInstances.each{ssInst->

	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'Name');
	
	def status = ($smis.getProperty(ssInst,'OperationalStatus')[0])==2?1:0;
	
	def capacity = 0;
	def unused = 0;
	def spInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_HostedStoragePool", "HITACHI_StoragePool", "GroupComponent", "PartComponent");
	spInstances.each{spInst->
	 unused += $smis.getProperty(spInst,'RemainingManagedSpace');
	 capacity += $smis.getProperty(spInst,'TotalManagedSpace');
	}
	result.addRow(ssname, [
	'class','DiskArrayHDS',
	'attr.capacity' , capacity,
	'perf.stor_use.capacity_used' , UnitContext.b2GB(capacity - unused),
	'perf.stor_use.capacity_unused' , UnitContext.b2GB(unused),
	'perf.stor_use.capacity_useage' , NumberContext.round(((capacity - unused) * 100) / capacity, 2),
	'state.available_status', status]);
	}
	}