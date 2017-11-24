/*!Action
 action.name=通过smi-s协议监测IBMDS存储系统基本信息
 action.descr=通过smi-s协议IBMDS存储系统基本信息
 action.protocols=smis
 monitor.output=IBMDS-STORAGESYSTEM-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils;
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
		def spInsts = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageProcessorSystem');
		
		def ssdInsts = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystemStatisticalData');
	
	arrayInst.each{cimInst->
		def arrayCOP = cimInst.getObjectPath();
		def arrayProps = new Properties();
		def ssName = $smis.getProperty(cimInst,'Name');
		def cimInstances = IBMDSUtils.findDiskDriver($smis, arrayCOP);
		def status = $smis.getProperty(cimInst, 'OperationalStatus')[0];
		def descript = $smis.getProperty(cimInst, 'Description');
		def maxSSDSupport = $smis.getProperty(cimInst, 'Description');
		def mappableLUNCount = $smis.getProperty(cimInst, 'MappableLUNCount');
		def cacheBlockSize = $smis.getProperty(cimInst, 'CacheBlockSize');
		def NVSRAMVersion = $smis.getProperty(cimInst, 'NVSRAMVersion');
		def maxHotSpares = $smis.getProperty(cimInst, 'MaxHotSpares');
		def maxPartitionCount = $smis.getProperty(cimInst, 'MaxPartitionCount');
		def maxStorageVolumes = $smis.getProperty(cimInst, 'MaxStorageVolumes');
		
		
		
		//SMIArrayUtils.fetchPhysicalPackage($smis, arrayCOP, arrayProps);
		//SMIArrayUtils.fetchSoftwareIdentity($smis, arrayCOP, arrayProps);
		
		
		def ip = null;
		def identifying = null;
		def cacheMemorySize = null;
		def processorMemorySize = null;
		spInsts.each{spInst->
		def SystemName = $smis.getProperty(spInst,'SystemName');
		 identifying = $smis.getProperty(spInst,'OtherIdentifyingInfo');
		 if(SystemName==ssName){
			if(identifying.length>2){
			ip = identifying[1];
			}else if(identifying.length==2){
			ip = identifying[0];
			}
			cacheMemorySize = $smis.getProperty(spInst,'CacheMemorySize');
			processorMemorySize = $smis.getProperty(spInst,'ProcessorMemorySize');
		}
		}
		
		def TotalIOs = null;
		def KBytesTransferred = null;
		def KBytesWritten = null;
		def IOTimeCounter = null;
		def ReadIOs = null;
		def ReadHitIOs = null;
		def ReadIOTimeCounter = null;
		def ReadHitIOTimeCounter = null;
		def KBytesRead = null;
		def WriteIOs = null;
		def WriteHitIOs = null;
		def WriteIOTimeCounter = null;
		def WriteHitIOTimeCounter = null;
		def IdleTimeCounter = null;
		def MaintOp = null;
		def MaintTimeCounter = null;
		
		
		ssdInsts.each{ssdInst->
		def InstanceID = $smis.getProperty(ssdInst,'InstanceID');
		if(InstanceID==ssName){
		TotalIOs = $smis.getProperty(ssdInst,'TotalIOs');
		KBytesTransferred = $smis.getProperty(ssdInst,'KBytesTransferred');
		KBytesWritten = $smis.getProperty(ssdInst,'KBytesWritten');
		IOTimeCounter = $smis.getProperty(ssdInst,'IOTimeCounter');
		ReadIOs = $smis.getProperty(ssdInst,'ReadIOs');
		ReadHitIOs = $smis.getProperty(ssdInst,'ReadHitIOs');
		ReadIOTimeCounter = $smis.getProperty(ssdInst,'ReadIOTimeCounter');
		ReadHitIOTimeCounter = $smis.getProperty(ssdInst,'ReadHitIOTimeCounter');
		KBytesRead = $smis.getProperty(ssdInst,'KBytesRead');
		WriteIOs = $smis.getProperty(ssdInst,'WriteIOs');
		WriteHitIOs = $smis.getProperty(ssdInst,'WriteHitIOs');
		WriteIOTimeCounter = $smis.getProperty(ssdInst,'WriteIOTimeCounter');
		WriteHitIOTimeCounter = $smis.getProperty(ssdInst,'WriteHitIOTimeCounter');
		IdleTimeCounter = $smis.getProperty(ssdInst,'IdleTimeCounter');
		MaintOp = $smis.getProperty(ssdInst,'MaintOp');
		MaintTimeCounter = $smis.getProperty(ssdInst,'MaintTimeCounter');
		}
		}
		
		
		def result = $result.create(ssName);
			result.clazz = 'DiskArrayIBM';
			result.attr.ibmxl = 'DS';
			result.state.available_status = (status == '2' ? 1 : 0);;
			result.attr.descript = descript;
			result.attr.mappableLUNCount = mappableLUNCount;
			result.attr.ipAddr = ip;
			result.attr.maxHotSpares = maxHotSpares;
			result.attr.maxPartitionCount = maxPartitionCount;
			result.attr.maxStorageVolumes = maxStorageVolumes;
			
			result.attr.brand = 'IBM';
			result.attr.fwOSName = arrayProps.VersionString;
			result.attr.sequenceNum = arrayProps.SerialNumber;
			
			
			result.perf.storage_perf.total_ios = TotalIOs;
			result.perf.storage_perf.write_ios = WriteIOs;
			result.perf.storage_perf.read_ios = ReadIOs;
			//result.perf.storage_perf.io_timeCounter = IOTimeCounter;
			result.perf.storage_perf.read_write_rate = KBytesTransferred;
			result.perf.storage_perf.write_rate = KBytesWritten;
			result.perf.storage_perf.read_rate = KBytesRead;
			//result.perf.storage_perf.write_hitio = WriteHitIOs;
			//result.perf.storage_perf.write_hitio_timeCounter = WriteHitIOTimeCounter;
			//result.perf.storage_perf.idle_timeCounter = IdleTimeCounter;
			

		
	}
	
	
}