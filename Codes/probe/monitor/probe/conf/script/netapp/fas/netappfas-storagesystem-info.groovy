import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	
	def ssInstances = $smis.getInstancesByClass('ONTAP_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = $smis.getProperty(ssInst,'OtherIdentifyingInfo')[0];
		
		def ssname = $smis.getProperty(ssInst,'ElementName');
		def status = ($smis.getProperty(ssInst,'OperationalStatus')[0])==2?1:0;
		
		def capacity = 0;
		def unused = 0;
		def svInstances = $smis.getAssociatedInstances(ssOP, "ONTAP_HostedConcretePool", "ONTAP_ConcretePool", "GroupComponent", "PartComponent");
		svInstances.each{svInst->
		 capacity += $smis.getProperty(svInst,'TotalManagedSpace');
		 unused += $smis.getProperty(svInst,'RemainingManagedSpace');
		}
			result.addRow(ssname, [
			'class','NETAPP',
			'attr.capacity',capacity,
			'perf.stor_use.capacity_used',UnitContext.b2GB((capacity-unused), 3),
			'perf.stor_use.capacity_unused', UnitContext.b2GB(unused, 3),
			'perf.stor_use.capacity_useage',NumberContext.round((capacity-unused) * 100 / capacity,2),
			'attr.ipAddr',storageip,
			'state.available_status', status]);
		
	}
}