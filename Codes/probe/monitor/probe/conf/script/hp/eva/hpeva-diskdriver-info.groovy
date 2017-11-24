import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {

	def ssInstances = $smis.getInstancesByClass('HPEVA_DiskDrive');
	ssInstances.each{ssInst->
	def name = $smis.getProperty(ssInst,'ElementName');
	def status = $smis.getProperty(ssInst,'OperationalStatus')[0]==2?1:0;
	def caption = $smis.getProperty(ssInst,'Caption');
	def descript = $smis.getProperty(ssInst,'Description');
	def diskType = $smis.getProperty(ssInst,'Disktype');
	def used = $smis.getProperty(ssInst,'Occupancy').split(' ')[0];
	def capacity = $smis.getProperty(ssInst,'FormattedCapacity').split(' ')[0];
	def mfg = $smis.getProperty(ssInst,'Manufacturer');
	def partNum = $smis.getProperty(ssInst,'Model');
	result.addRow(name, [
	'class','physicalDisk',
	'attr.caption',caption,
	'attr.descript',descript,
	'attr.diskType',diskType,
	'attr.mfg',mfg,
	'attr.partNum',partNum,
	'attr.capacity',capacity,
	'perf.stor_use.capacity_used',used,
	'perf.stor_use.capacity_unused',capacity-used,
	'state.available_status', status]);
	}
}