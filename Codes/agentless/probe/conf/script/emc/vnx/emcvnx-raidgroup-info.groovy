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
			def rgInstances = $smis.getAssociatedInstances(ssOP, "EMC_SystemDevice", "Clar_RaidGroup", "GroupComponent", "PartComponent");
			rgInstances.each{rgInst->
				def status = $smis.getProperty(rgInst,'OperationalStatus')[0]==2?1:0;
				def name = $smis.getProperty(rgInst,'DeviceID');
				def storResCode = $smis.getProperty(rgInst,'DeviceID');
				def size = $smis.getProperty(rgInst,'BlockSize');
				def free = $smis.getProperty(rgInst,'ConsumableBlocks');
				def total = $smis.getProperty(rgInst,'NumberOfBlocks');
				def capacity = total*size;
				if(capacity!="0"){
					def unused = free*size;
				def used = capacity - unused;
				result.addRow(name, [
					'class','RAIDGroup',
					'rs.ComponentOf','node',
					'attr.storResCode',storResCode,
					'attr.capacity',UnitContext.b2GB(capacity,3),
					'perf.stor_use.capacity_unused',UnitContext.b2GB(unused,3),
					'perf.stor_use.capacity_usage',NumberContext.round((used/capacity)*100, 2),
					'perf.stor_use.capacity_used',UnitContext.b2GB(used,3),
					'state.available_status', status]);
				}
			}
		}
	}
}