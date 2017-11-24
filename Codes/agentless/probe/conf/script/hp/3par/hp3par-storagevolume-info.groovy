import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext; 
import java.lang.*;


executeMonitor();


def executeMonitor() {

	def ssInstances = $smis.getInstancesByClass('TPD_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = $smis.getProperty(ssInst,'OtherIdentifyingInfo')[1];
		
		def nsInstances = $smis.getAssociatedInstances(ssOP, "TPD_NodeComponentCS", "TPD_NodeSystem", "GroupComponent", "PartComponent");
		nsInstances.each{nsInst->
		def nsOP = nsInst.getObjectPath();
		def npInstances = $smis.getAssociatedInstances(nsOP, "TPD_NodeSystemPackage", "TPD_NodePackage", "Dependent", "Antecedent");
		npInstances.each{npInst->
			def npOP = npInst.getObjectPath();

		def pmInstances = $smis.getAssociatedInstances(npOP, "TPD_NodePackagedMemory", "TPD_PhysicalMemory", "GroupComponent", "PartComponent");
		pmInstances.each{pmInst->
			def MemoryType = $smis.getProperty(pmInst,'MemoryType');
			def capacity = $smis.getProperty(pmInst,'Capacity');
			def mfg = $smis.getProperty(pmInst,'Manufacturer');
			def productName = $smis.getProperty(pmInst,'Model');
			def serial = $smis.getProperty(pmInst,'SerialNumber');
			def partNum = $smis.getProperty(pmInst,'PartNumber');
			def ElementName = $smis.getProperty(pmInst,'ElementName');
			def status = $smis.getProperty(pmInst,'OperationalStatus')[0];
			def cacheType = getCacheType($smis.getProperty(pmInst,'CacheType'));
			result.addRow(nsName, [
			'class','Memory',
			'rs.ComponentOf','node',
			'attr.storResCode',name+"/"+ip,
			'state.available_status', status,
			'attr.serial',serial,
			'attr.capacity',UnitContext.b2GB(capacity,2),
			'attr.productName',productName,
			'attr.partNum',partNum,
			'attr.mfg',mfg]);
		}
		}	
		}
		}

}
def getCacheType(def type) {
	def cacheType = null;
	switch(type) {
		case 0:
			cacheType = 'Control (CPU)';
		case 1:
			cacheType = 'Data (Cluster)';
			break;
	}
}