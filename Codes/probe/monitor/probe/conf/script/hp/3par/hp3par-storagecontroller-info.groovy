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

		def nsName = $smis.getProperty(nsInst,'ElementName');
		def status = ($smis.getProperty(nsInst,'OperationalStatus')[0])==2?1:0;
		def name = $smis.getProperty(nsInst,'Name');
		def master = $smis.getProperty(nsInst,'IsMaster');
		def online = $smis.getProperty(nsInst,'IsOnline');
		def cluster = $smis.getProperty(nsInst,'IsInCluster');
		def identifier = $smis.getProperty(nsInst,'KernelVersion');
		def biosversion = $smis.getProperty(nsInst,'BiosVersion');
		
		
		def npInstances = $smis.getAssociatedInstances(nsOP, "TPD_NodeSystemPackage", "TPD_NodePackage", "Dependent", "Antecedent");
		def serial = null;
		def productName = null;
		def partNum = null;
		def mfg = null;
		def physicMem = null;
		def capacity = null;
		npInstances.each{npInst->
			def npOP = npInst.getObjectPath();
			 serial = $smis.getProperty(npInst,'SerialNumber');
			 productName = $smis.getProperty(npInst,'ModelName');
			 partNum = $smis.getProperty(npInst,'SparePartNumber');
			 mfg = $smis.getProperty(npInst,'Manufacturer');
		def pmInstances = $smis.getAssociatedInstances(npOP, "TPD_NodePackagedMemory", "TPD_PhysicalMemory", "GroupComponent", "PartComponent");
		pmInstances.each{pmInst->
			def MemoryType = $smis.getProperty(pmInst,'MemoryType');
			capacity = $smis.getProperty(pmInst,'Capacity');
			if(MemoryType==11||MemoryType=='11'){
			physicMem = UnitContext.b2GB(capacity, 3);
			}
		}
		}
		
			result.addRow(nsName, [
			'class','RAIDController',
			'rs.ComponentOf','node',
			'attr.storResCode',name+"/"+ip,
			'state.available_status', status,
			'attr.serial',serial,
			'attr.physicMem',physicMem,
			'attr.productName',productName,
			'attr.partNum',partNum,
			'attr.mfg',mfg]);
		}
		}

}