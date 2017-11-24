import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('ONTAP_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = $smis.getProperty(ssInst,'OtherIdentifyingInfo')[0];
		
		def svInstances = $smis.getAssociatedInstances(ssOP, "ONTAP_HostedConcretePool", "ONTAP_ConcretePool", "GroupComponent", "PartComponent");
		svInstances.each{svInst->
		
		
		def ssname = $smis.getProperty(svInst,'ElementName');
		def status = $smis.getProperty(svInst,'OperationalStatus')[0]==2?1:0;
		def capacity = $smis.getProperty(svInst,'TotalManagedSpace');
		def unused = $smis.getProperty(svInst,'RemainingManagedSpace');
		def instanceID = $smis.getProperty(svInst,'InstanceID');

		
			result.addRow(ssname, [
			'class','StorageVolume',
			'rs.ComponentOf' , 'node',
			'attr.storResCode',instanceID,
			'perf.stor_use.capacity_unused' , UnitContext.b2GB(unused, 3),
			'perf.stor_use.capacity_useage' , NumberContext.round((capacity-unused) * 100 / capacity, 3),
			'perf.stor_use.capacity_used' , UnitContext.b2GB((capacity-unused), 3),
			'attr.capacity',UnitContext.b2GB(capacity,2),
			'state.available_status', status]);
		}
	}

}