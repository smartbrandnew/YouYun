import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	def ssInstances = $smis.getInstancesByClass('HPEVA_StorageSystem');
	ssInstances.each{ssInst->
	println(ssInst);
	def name = $smis.getProperty(ssInst,'ElementName');
	def status = $smis.getProperty(ssInst,'OperationalStatus')[0]==2?1:0;
	def capacity = $smis.getProperty(ssInst,'TotalStorageSpace');
	def used = $smis.getProperty(ssInst,'UsedStorageSpace');
	def unused = $smis.getProperty(ssInst,'AvailableStorageSpace');
	def version = $smis.getProperty(ssInst,'FirmwareVersion');
	result.addRow(name, [
	'class','DiskArrayHP',
	'attr.capacity',capacity,
	'perf.stor_use.capacity_used',used,
	'perf.stor_use.capacity_unused',unused,
	'perf.stor_use.capacity_useage',NumberContext.round(used*100/capacity,2),
	'state.available_status', status]);
	}

}