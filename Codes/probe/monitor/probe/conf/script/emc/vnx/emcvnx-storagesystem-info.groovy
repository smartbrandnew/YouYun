import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext;
import java.lang.*;

executeMonitor();

def executeMonitor() {
	def ip = param.get('monitorNode').getIpAddress();
	def ssInstances = $smis.getInstancesByClass('Clar_StorageSystem');
	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def spInstances = $smis.getAssociatedInstances(ssOP, "EMC_ComponentCS", "Clar_StorageProcessorSystem", "GroupComponent", "PartComponent");
		def isnow = false;
		def SPAip = null;
		def SPBip = null;
		spInstances.each{spInst->
			def spOP = spInst.getObjectPath();
			def haInstances = $smis.getAssociatedInstances(spOP, "EMC_HostedAccessPoint", "Clar_RemoteServiceAccessPoint", "Antecedent", "Dependent");
			haInstances.each{haInst->
				def SPip = $smis.getProperty(haInst,'AccessInfo');
				if(SPip==ip){
					isnow = true;
					if(SPAip==null)
						SPAip = ip;
					else
						SPBip = ip;
				}
			}
		}
		if(isnow){
			def unused = 0;
			def capacity = 0;
			def pInstances = $smis.getAssociatedInstances(ssOP, "Clar_HostedStoragePool_SS_PSP", "Clar_PrimordialStoragePool", "GroupComponent", "PartComponent");
			for(sp in pInstances){
				def poolname = $smis.getProperty(sp, 'Primordial');
				if(poolname||poolname=='TRUE')
					continue;
				unused += $smis.getProperty(sp, 'RemainingManagedSpace');
				capacity += $smis.getProperty(sp, 'TotalManagedSpace');
			}
			if(capacity!=0){
				def ssname = $smis.getProperty(ssInst,'ElementName');
				def memorysize = NumberContext.round($smis.getProperty(ssInst,'EMCMemorySize')/1024,3);
				def descript = $smis.getProperty(ssInst,'Description');
				def diskNumber = $smis.getProperty(ssInst,'EMCNumberOfDisks');
				def status = ($smis.getProperty(ssInst,'OperationalStatus')[0])==2?1:0;
				result.addRow(ssname, [
					'class','DiskArrayEMC',
					'attr.descript',descript,
					'attr.memorysize',memorysize,
					'attr.ipAddr',SPAip,
					'attr.otherIpAddr',SPBip,
					'attr.driverNum',diskNumber,
					'perf.stor_use.capacity',UnitContext.b2GB(capacity, 3),
					'perf.stor_use.capacity_used',UnitContext.b2GB((capacity-unused), 3),
					'perf.stor_use.capacity_unused',UnitContext.b2GB(unused, 3),
					'perf.stor_use.capacity_usage',NumberContext.round((capacity-unused) * 100 / capacity,2),
					'state.available_status', status]);
			}
		}
	}
}