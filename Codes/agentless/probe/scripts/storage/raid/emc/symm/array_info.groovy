/*!Action
 action.name=通过smi-s协议监测华为设备物理基本信息
 action.descr=通过smi-s协议监测华为设备物理基本信息
 action.protocols=smis
 monitor.output=EMCSYMM-ARRAY-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCSymmUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = EMCSymmUtils.findArrayInstances($smis);
	arrayInstances {arrayInst->
			def diskName = $smis.getProperty(arrayInst, 'ElementName');
			def result = $result.create(diskName);
			result.clazz = 'EMC';
			result.rs.ComponentOf = 'node';
			def arrayCOP = arrayInst.getObjectPath();
			def arrayinfos = $smis.getAssociatedInstances(arrayCOP,"CIM_ElementStatisticalData", "CIM_BlockStorageStatisticalData", "ManagedElement", "Stats")
			arrayinfos.each{arrayinfo->
			
			result.attr.transferSpeed = $smis.getProperty(arrayinfo,'KBytesTransferred');
			
			result.perf.emcsymm-perf.write_ios = $smis.getProperty(arrayinfo,'WriteIOs');
			result.perf.emcsymm-perf.read_ios = $smis.getProperty(arrayinfo,'ReadIOs');
			result.perf.emcsymm-perf.total_ios = $smis.getProperty(arrayinfo,'TotalIOs');
			result.perf.emcsymm-perf.emc_deferred_write_ios = $smis.getProperty(arrayinfo,'EMCDeferredWriteIOs');
			result.perf.emcsymm-perf.emc_delayeddfw_ios = $smis.getProperty(arrayinfo,'EMCDelayedDFWIOs');
			result.perf.emcsymm-perf.emc_kbpending_flush = $smis.getProperty(arrayinfo,'EMCKBPendingFlush');
			result.perf.emcsymm-perf.emc_kbpending_format = $smis.getProperty(arrayinfo,'EMCKBPendingFormat');
			result.perf.emcsymm-perf.emc_kbperfetched = $smis.getProperty(arrayinfo,'EMCKBPrefetched');
			result.perf.emcsymm-perf.emc_maxkbpending_flush = $smis.getProperty(arrayinfo,'EMCMaxKBPendingFlush');
			result.attr.numFreeSlots = $smis.getProperty(arrayinfo,'EMCNumFreePermacacheSlots');
			result.attr.numUsedSlots = $smis.getProperty(arrayinfo,'EMCNumUsedPermacacheSlots');
			result.perf.emcsymm-perf.emc_sequential_readios = $smis.getProperty(arrayinfo,'EMCSequentialReadIOs');
			result.perf.emcsymm-perf.emc_totalhit_ios = $smis.getProperty(arrayinfo,'EMCTotalHitIOs');
			result.perf.emcsymm-perf.emc_writekb_flush = $smis.getProperty(arrayinfo,'EMCWriteKBytesFlushed');
			result.perf.emcsymm-perf.writehit_ios = $smis.getProperty(arrayinfo,'WriteHitIOs');
			result.perf.emcsymm-perf.kbread = $smis.getProperty(arrayinfo,'KBytesRead');
			result.perf.emcsymm-perf.kbwrite = $smis.getProperty(arrayinfo,'KBytesWritten');
			result.perf.emcsymm-perf.readhit_ios = $smis.getProperty(arrayinfo,'ReadHitIOs');
			//result.attr.statisticTime = $smis.getProperty(arrayinfo,'StatisticTime');
			}
	}
	
}


