/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=EMCSYMM-STORAGEVOLUME-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCSymmUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	
	def arrayInstances = EMCSymmUtils.findArrayInstances($smis);
	arrayInstances.each {arrayInst->
	def arrayCOP = arrayInst.getObjectPath();
	def storagePoolInstances = EMCSymmUtils.findStoragePool($smis,arrayCOP);
	storagePoolInstances.each {storagePoolInst->
	def spCOP = storagePoolInst.getObjectPath();
	def storageVolumeInstances = EMCSymmUtils.findStorageVolume($smis,spCOP);
	storageVolumeInstances.each {storageVolumeInst->
	def svCOP = storageVolumeInst.getObjectPath();
	def fcinfos = $smis.getAssociatedInstances(svCOP,"CIM_ElementStatisticalData", "CIM_BlockStorageStatisticalData", "ManagedElement", "Stats")
	fcinfos.each{fcinfo->
	//result.attr.transferred = $smis.getProperty(fcinfo,'KBytesTransferred');
	result.emcsymm-perf.total_ios = $smis.getProperty(fcinfo,'TotalIOs');
	result.attr.statisticTime = $smis.getProperty(fcinfo,'StatisticTime');
	
	result.perf.emcsymm-perf.emc_daread_ios = $smis.getProperty(fcinfo,'EMCDAReadIOs');
	result.perf.emcsymm-perf.emc_dawrite_ios = $smis.getProperty(fcinfo,'EMCDAWriteIOs');
	result.perf.emcsymm-perf.emc_kbda_perfetched = $smis.getProperty(fcinfo,'EMCKBDAPrefetched');
	result.perf.emcsymm-perf.emc_kbda_perfetched_used = $smis.getProperty(fcinfo,'EMCKBDAPrefetchedUsed');
	result.perf.emcsymm-perf.emc_kbda_read = $smis.getProperty(fcinfo,'EMCKBDARead');
	result.perf.emcsymm-perf.emc_kbda_write = $smis.getProperty(fcinfo,'EMCKBDAWrite');
	result.perf.emcsymm-perf.emc_kbpending_flush = $smis.getProperty(fcinfo,'EMCKBPendingFlush');
	result.perf.emcsymm-perf.emc_maxkbpending_flush = $smis.getProperty(fcinfo,'EMCMaxKBPendingFlush');
	result.perf.emcsymm-perf.emc_sampled_reads = $smis.getProperty(fcinfo,'EMCSampledReads');
	//result.perf.emcsymm-perf.emc_sampled_readstime = $smis.getProperty(fcinfo,'EMCSampledReadsTime');
	result.perf.emcsymm-perf.emc_sampled_writes = $smis.getProperty(fcinfo,'EMCSampledWrites');
	//result.perf.emcsymm-perf.emc_sampled_writestime = $smis.getProperty(fcinfo,'EMCSampledWritesTime');
	result.perf.emcsymm-perf.emc_sequential_readhitios = $smis.getProperty(fcinfo,'EMCSequentialReadHitIOs');
	result.perf.emcsymm-perf.emc_sequential_readios = $smis.getProperty(fcinfo,'EMCSequentialReadIOs');
	result.perf.emcsymm-perf.emc_sequential_writeios = $smis.getProperty(fcinfo,'EMCSequentialWriteIOs');
	result.perf.emcsymm-perf.emc_totalhit_ios = $smis.getProperty(fcinfo,'EMCTotalHitIOs');
	result.perf.emcsymm-perf.writehit_ios = $smis.getProperty(fcinfo,'WriteHitIOs');
	result.perf.emcsymm-perf.write_ios = $smis.getProperty(fcinfo,'WriteIOs');
	result.perf.emcsymm-perf.kbread = $smis.getProperty(fcinfo,'KBytesRead');
	result.perf.emcsymm-perf.readhit_ios = $smis.getProperty(fcinfo,'ReadHitIOs');
	result.perf.emcsymm-perf.read_ios = $smis.getProperty(fcinfo,'ReadIOs');
	result.perf.emcsymm-perf.kbwrite = $smis.getProperty(fcinfo,'KBytesWritten');
	}
	}
	}
	}
	
	
}


