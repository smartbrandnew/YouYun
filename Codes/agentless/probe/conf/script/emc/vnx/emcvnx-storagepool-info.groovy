import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;

executeMonitor();

def executeMonitor() {
	def ip = param.get('monitorNode').getIpAddress();
	def ssInstances = $smis.getInstancesByClass('Clar_StorageSystem');
	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def ssname =  $smis.getProperty(ssInst,'ElementName');
		
		def spInstances = $smis.getAssociatedInstances(ssOP, "EMC_ComponentCS", "Clar_StorageProcessorSystem", "GroupComponent", "PartComponent");
		def isnow = false;
		spInstances.each{spInst->
			def spOP = spInst.getObjectPath();
			def haInstances = $smis.getAssociatedInstances(spOP, "EMC_HostedAccessPoint", "Clar_RemoteServiceAccessPoint", "Antecedent", "Dependent");
			haInstances.each{haInst->
			 def SPip = $smis.getProperty(haInst,'AccessInfo');
			 if(SPip==ip)
				isnow = true;
			}
		}
		if(isnow){	
			def pInstances = $smis.getAssociatedInstances(ssOP, "Clar_HostedStoragePool_SS_PSP", "Clar_PrimordialStoragePool", "GroupComponent", "PartComponent");
			pInstances.each{spInst->
				def spOP = spInst.getObjectPath();
				def spName = $smis.getProperty(spInst,'ElementName');
				def status = ($smis.getProperty(spInst,'OperationalStatus')[0])==2?1:0;
				def name = $smis.getProperty(spInst,'ElementName');
				def capacity = $smis.getProperty(spInst,'TotalManagedSpace');
				def unused = $smis.getProperty(spInst,'RemainingManagedSpace');
				def storResCode = $smis.getProperty(spInst,'InstanceID');
				def primordial = $smis.getProperty(spInst,'Primordial');
				def usage = 0;
				if(unused!=0&&capacity!=0)
					usage = NumberContext.round(unused * 100 / capacity, 0);
				result.addRow(spName, [
					'class','StoragePool',
					'rs.ComponentOf','node',
					'state.available_status', status,
					'attr.storResCode',storResCode,
					'perf.stor_manage.space_unallocated',unused==0?0:UnitContext.b2GB(unused, 3),
					'perf.stor_manage.space_usage',usage,
					'perf.stor_manage.space_allocated',unused==0?100:UnitContext.b2GB(capacity-unused, 3),
					'attr.primordial', primordial]);
					
				def unifiedStoragePools = $smis.getAssociatedInstances(spOP, "EMC_AllocatedFromStoragePool", "Clar_UnifiedStoragePool", "Antecedent", "Dependent");
				unifiedStoragePools.each{unifiedStoragePool->
					def elementName = $smis.getProperty(unifiedStoragePool, 'ElementName');
					def percentageUsed = $smis.getProperty(unifiedStoragePool, 'EMCPercentageUsed');
					def percentageSubscribed = $smis.getProperty(unifiedStoragePool, 'EMCPercentSubscribed');
					result.addRow(elementName, [
						'perf.percentage.used', percentageUsed,
						'perf.percentahe.subscribed', percentageSubscribed]);
				}
			}
		}
	}
}