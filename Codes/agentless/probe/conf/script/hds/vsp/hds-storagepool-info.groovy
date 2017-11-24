import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;


executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	
	
	ssInstances.each{ssInst->

	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'ElementName');
	
	def spInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_HostedStoragePool", "HITACHI_StoragePool", "GroupComponent", "PartComponent");
	spInstances.each{spInst->
	def spname = $smis.getProperty(spInst,'ElementName');
	def status = $smis.getProperty(spInst,'OperationalStatus')[0];
	def poolID = $smis.getProperty(spInst,'PoolID');
	def unused = $smis.getProperty(spInst,'RemainingManagedSpace');
	def capacity = $smis.getProperty(spInst,'TotalManagedSpace');
	def primordial = $smis.getProperty(spInst,'Primordial');
	def instanceID = $smis.getProperty(spInst,'InstanceID');
	
	result.addRow(spname, [
	'class','StoragePool',
	'rs.ComponentOf','node',
	'attr.storResCode',poolID+"/"+instanceID,
	'state.available_status', status,
	'perf.stor_manage.space_unallocated',UnitContext.b2GB(unused, 3),
	'perf.stor_manage.space_useage',NumberContext.round(unused * 100 / capacity, 0),
	'perf.stor_manage.space_allocated',UnitContext.b2GB(capacity-unused, 3),
	'attr.primordial', primordial]);
	}
	}
	}