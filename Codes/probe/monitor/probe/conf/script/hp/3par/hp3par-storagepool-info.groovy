import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	def ssInstances = $smis.getInstancesByClass('TPD_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = $smis.getProperty(ssInst,'OtherIdentifyingInfo')[1];
		def ssname =  $smis.getProperty(ssInst,'ElementName');
		
		def spInstances = $smis.getAssociatedInstances(ssOP, "TPD_HostedStoragePool", "TPD_StoragePool", "GroupComponent", "PartComponent");
		spInstances.each{spInst->
		def spOP = spInst.getObjectPath();

		def spName = $smis.getProperty(spInst,'ElementName');
		def status = ($smis.getProperty(spInst,'OperationalStatus')[0])==2?1:0;
		def name = $smis.getProperty(spInst,'Name');
		def descript = $smis.getProperty(spInst,'Description');
		def capacity = $smis.getProperty(spInst,'TotalManagedSpace');
		def unused = $smis.getProperty(spInst,'RemainingManagedSpace');
		def storResCode = $smis.getProperty(spInst,'InstanceID');
		def primordial = $smis.getProperty(spInst,'Primordial');
		def usage = 0;
		if(unused!=0&&capacity!=0){
		usage = NumberContext.round(unused * 100 / capacity, 0);
		}
		
		
		
			result.addRow(ssname+"-"+spName, [
			'class','StoragePool',
			'rs.ComponentOf','node',
			'state.available_status', status,
			'attr.storResCode',storResCode,
			'attr.descript',descript,
			'perf.stor_manage.space_unallocated',unused==0?0:UnitContext.b2GB(unused, 3),
			'perf.stor_manage.space_useage',usage,
			'perf.stor_manage.space_allocated',unused==0?100:UnitContext.b2GB(capacity-unused, 3),
			'attr.primordial', primordial]);
		
		}
		}
	
}