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
			def nsInstances = $smis.getAssociatedInstances(ssOP, "Clar_SystemDevice_SS_SV", "Clar_StorageVolume", "GroupComponent", "PartComponent");
			def lun_count = 0;
			def nsName = "";
			nsInstances.each{nsInst->
				nsName = $smis.getProperty(nsInst,'ElementName');
				if(nsName.contains("LUN")){
					lun_count ++;
				}
				def status = ($smis.getProperty(nsInst,'OperationalStatus')[0])==2?1:0;
				def name = $smis.getProperty(nsInst,'Name');
				def blockSize = $smis.getProperty(nsInst,'BlockSize');
				def numberOfBlocks = $smis.getProperty(nsInst,'NumberOfBlocks');
				def raidlevel = $smis.getProperty(nsInst,'EMCRaidLevel');
				def totalSize = UnitContext.b2GB(blockSize*numberOfBlocks, 3);
				result.addRow(nsName, [
					'class','StorageVolume',
					'rs.ComponentOf','node',
					'attr.storResCode',nsName+"/"+name,
					'attr.capacity', totalSize,
					'attr.raidLev', raidlevel,
					'state.available_status', status]);
			}
			result.addRow(nsName,['perf.lun.count', lun_count]);
			
			nsInstances.each{nsInst->
				def csssOP = nsInst.getObjectPath();
				def blockStorageStatisticalDatas = $smis.getAssociatedInstances(csssOP, "Clar_ElementStatisticalData_SV_BSSD", "Clar_BlockStorageStatisticalData", "ManagedElement", "Stats");
				blockStorageStatisticalDatas.each{blockStorageStatisticalData ->
					def elementType =  $smis.getProperty(blockStorageStatisticalData,'ElementType');
					if(elementType==8){
						long kByteTransferred = $smis.getProperty(blockStorageStatisticalData,'KBytesTransferred');
						long staticTime = $smis.getProperty(blockStorageStatisticalData,'EMCLoggingTime');
						long totalIOs = $smis.getProperty(blockStorageStatisticalData,'TotalIOs');
						long requestArrivals = $smis.getProperty(blockStorageStatisticalData,'EMCNonZeroRequestArrivals');
						//def outstandingRequest = $smis.getProperty(blockStorageStatisticalData,'EMCOutstandingRequests');
						//result.addRow('outstandingRequest',['outstandingRequest', outstandingRequest]);
						//def throughput = requestArrivals-outstandingRequest;
						def responseTime = 0;
						if(requestArrivals!=0){
							responseTime = staticTime/requestArrivals;
						}
						double MBTransferred = kByteTransferred/1024;
						double totalTime = staticTime/1000;
						double bandwidth = 0;
						bandwidth = MBTransferred/totalTime;
						double iops = totalIOs/totalTime;
						def elementName = $smis.getProperty(blockStorageStatisticalData,'ElementName');
						result.addRow(elementName, [
							'perf.bandwidth', bandwidth,
							'perf.iops', iops,
							'perf.responsetime', responseTime,
							'perf.throughput', requestArrivals]);
					}
				}
			}
		}
	}
}