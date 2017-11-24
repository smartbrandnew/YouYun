/*!Action
 action.name=通过smi-s协议监测IBMDS数据卷基本信息
 action.descr=通过smi-s协议监测IBMDS数据卷基本信息
 action.protocols=smis
 monitor.output=IBMDS-STORAGEVOLUME-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils;
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def svInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageVolume');
	def svsInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageVolumeSetting');
	def svsdInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageVolumeStatisticalData');
	arrayInst.each{systemInst->
	def arrayCOP = systemInst.getObjectPath();
	def name = $smis.getProperty(systemInst, 'Name');
	svInstances.each{svInst->
		def sysname =  $smis.getProperty(svInst, 'SystemName');
		if(sysname==name){
		def Purpose = $smis.getProperty(svInst,"Purpose");
		def poolId = $smis.getProperty(svInst,"StoragePool_InstanceID");
		def instaceId = $smis.getProperty(svInst,"DeviceID");
		def storageVolName = $smis.getProperty(svInst,"ElementName");
		def BlockSize = $smis.getProperty(svInst,"BlockSize");
		def NumberOfBlocks = $smis.getProperty(svInst,"NumberOfBlocks");
		def ConsumableBlocks = $smis.getProperty(svInst,"ConsumableBlocks");
		def OwningController = $smis.getProperty(svInst,"OwningController");
		def PreferredController = $smis.getProperty(svInst,"PreferredController");
		def SequentialAccess = $smis.getProperty(svInst,"SequentialAccess");
		def svStatus = $smis.getProperty(svInst,"OperationalStatus")[0];
        def RaidLevel = $smis.getProperty(svInst,"RaidLevel");
        def MirrorEnable = $smis.getProperty(svInst,"MirrorEnable");
        def ReadCacheEnable = $smis.getProperty(svInst,"ReadCacheEnable");
        def WriteCacheEnable = $smis.getProperty(svInst,"WriteCacheEnable");
		
		def totalCapacity =  $unit.b2GB(BlockSize*ConsumableBlocks);
		

		
		def TotalIOs = null;
		def KBytesTransferred = null;
		def KBytesWritten = null;
		def ReadIOs = null;
		def KBytesRead = null;
		def WriteIOs = null;

		for(svsdInst in svsdInstances){
			def deviceid =  $smis.getProperty(svsdInst,"InstanceID");
			if(deviceid==sysname+"_"+instaceId){
				TotalIOs = $smis.getProperty(svsdInst,"TotalIOs");
				KBytesTransferred = $smis.getProperty(svsdInst,"KBytesTransferred");
				KBytesWritten = $smis.getProperty(svsdInst,"KBytesWritten");
				ReadIOs = $smis.getProperty(svsdInst,"ReadIOs");
				KBytesRead = $smis.getProperty(svsdInst,"KBytesRead");
				WriteIOs = $smis.getProperty(svsdInst,"WriteIOs");
				break;
			}
		}	
  
				def result = $result.create(storageVolName);
				result.clazz = 'StorageVolume';
		        result.attr.capacity = totalCapacity;
				result.attr.storResCode = poolId+"-"+storageVolName;
				result.attr.raidLev = RaidLevel;
				result.attr.ownController = OwningController;
				result.attr.readCacheEnable = (ReadCacheEnable=='TRUE'?'是':'否');
				result.attr.writeCacheEnable = (WriteCacheEnable=='TRUE'?'是':'否');
				result.attr.mirrorEnable =  (MirrorEnable=='TRUE'?'是':'否');
				result.attr.sequentialAccess =  (SequentialAccess=='TRUE'?'是':'否');
				result.state.available_status = (svStatus == '2' ? 1 : 0);
				
				result.perf."storvolume-io".total_ios = NumberContext.round(TotalIOs/1024,2);
				result.perf."storvolume-io".read_ios = NumberContext.round(ReadIOs/1024,2);
				result.perf."storvolume-io".write_ios = NumberContext.round(WriteIOs/1024,2);
				result.perf."storvolume-io".write_rate = NumberContext.round(KBytesWritten/1024,2);
				result.perf."storvolume-io".read_rate = NumberContext.round(KBytesRead/1024,2);
				result.perf."storvolume-io".read_write_rate = NumberContext.round(KBytesTransferred/1024,2);
				
			}
			}
			}
}