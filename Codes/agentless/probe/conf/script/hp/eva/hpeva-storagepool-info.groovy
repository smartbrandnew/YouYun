import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;
executeMonitor();


def executeMonitor() {
	
	def ssInstances = $smis.getInstancesByClass('HPEVA_StoragePool');
	ssInstances.each{ssInst->
	def name = $smis.getProperty(ssInst,'ElementName');
	def status = $smis.getProperty(ssInst,'OperationalStatus')[0]==2?1:0;
	def storResCode = $smis.getProperty(ssInst,'PoolID');
	def perimordial = $smis.getProperty(ssInst,'Primordial');
	def descript = $smis.getProperty(ssInst,'Description');
	def capacity = $smis.getProperty(ssInst,'TotalManagedSpace');
	def unused = $smis.getProperty(ssInst,'RemainingManagedSpace');
	
	result.addRow(name, [
	'class','StoragePool',
	'rs.ComponentOf','node',
	'attr.descript',descript,
	'attr.storResCode',storResCode,
	'attr.primordial',perimordial,
	'attr.capacity',capacity,
	'perf.stor_manage.space_allocated',capacity-unused,
	'perf.stor_manage.space_unallocated',unused,
	'perf.stor_manage.space_useage',NumberContext.round(unused*100/capacity,0),
	'state.available_status', status]);
	}
}