import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.UnitContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	def ssInstances = $smis.getInstancesByClass('TPD_StorageSystem');

	ssInstances.each{ssInst->
		def ssOP = ssInst.getObjectPath();
		def storageip = $smis.getProperty(ssInst,'OtherIdentifyingInfo')[1];
		def ssname = $smis.getProperty(ssInst,'ElementName');
		def status = ($smis.getProperty(ssInst,'OperationalStatus')[0])==2?1:0;
		def description = $smis.getProperty(ssInst, 'Description', 'N/A');
        def info = $smis.getProperty(ssInst, 'OtherIdentifyingInfo');
        def infoDescriptions = $smis.getProperty(ssInst, 'IdentifyingDescriptions');
		def ipAddr = null;
        for(int i = 0;i<infoDescriptions.length;i++){
            if(infoDescriptions[i]=="Ipv4 Address"){
                ipAddr = info[i];
                break;
            }

        }
		def systemPackages = $smis.getAssociatedInstances(ssOP, 'TPD_ComputerSystemPackage', 'TPD_SystemPackage', 'Dependent', 'Antecedent');
		def serialNumber = null;
		def model = null;
		def manufacturer = null;
		def version = null;
        for (sp in systemPackages) {
            serialNumber = $smis.getProperty(sp, 'SerialNumber', 'N/A');
            model = $smis.getProperty(sp, 'Model', 'N/A');
            manufacturer = $smis.getProperty(sp, 'Manufacturer', 'HP');
            version = $smis.getProperty(sp, 'Version');
            break;
        }
		
		def unused = 0;
		def capacity = 0;
		def pools = $smis.getAssociatedInstances(ssOP, 'TPD_HostedStoragePool', 'TPD_StoragePool', 'GroupComponent', 'PartComponent');
		for(sp in pools){
			def poolname = $smis.getProperty(sp, 'Primordial');
			if(poolname||poolname=='TRUE'){
			continue;
			}
			unused += $smis.getProperty(sp, 'RemainingManagedSpace');
			capacity += $smis.getProperty(sp, 'TotalManagedSpace');
		}
		
			result.addRow(ssname, [
			'class','DiskArrayHP',
			'attr.ipAddr',ipAddr,
			'attr.serial',serialNumber,
			'attr.partNum',model,
			'attr.mfg',manufacturer,
			'attr.fwOSName',version,
			'perf.stor_use.capacity_used',UnitContext.b2GB((capacity-unused), 3),
			'perf.stor_use.capacity_unused',UnitContext.b2GB(unused, 3),
			'perf.stor_use.capacity_useage',NumberContext.round((capacity-unused) * 100 / capacity,2),
			'state.available_status', status]);
		
	}
}