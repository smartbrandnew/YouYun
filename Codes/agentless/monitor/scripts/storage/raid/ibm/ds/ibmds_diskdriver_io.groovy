/*!Action
 action.name=通过smi-s协议监测硬盘性能信息
 action.descr=通过smi-s协议监测硬盘性能信息
 action.protocols=smis
 monitor.output=IBMDS-DISKDRIVER-IO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMDSUtils;
import java.lang.*;


executeMonitor();
return $result;

def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def sdInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_DiskDriveStatisticalData');
	arrayInst.each{systemInst->
		def arrayCOP = systemInst.getObjectPath();
		def cimInstances = IBMDSUtils.findDiskDriver($smis, arrayCOP);
		cimInstances.each{cimInst->
			def driverCOP = cimInst.getObjectPath();
			def diskName = $smis.getProperty(cimInst,'Name');
			def diskSystemName = $smis.getProperty(cimInst,'SystemName');
			def diskDriveID = $smis.getProperty(cimInst, 'DeviceID');
			
			sdInstances.each{sdInst->
			def id = $smis.getProperty(sdInst,"InstanceID");
			if(id==diskSystemName+"_"+diskDriveID){
			
				 def TotalIOs = $smis.getProperty(sdInst,"TotalIOs");
				 def KBytesTransferred = $smis.getProperty(sdInst,"KBytesTransferred");
				 def KBytesWritten = $smis.getProperty(sdInst,"KBytesWritten");
				 def IOTimeCounter = $smis.getProperty(sdInst,"IOTimeCounter");
				 def ReadIOs = $smis.getProperty(sdInst,"ReadIOs");
				 def ReadIOTimeCounter = $smis.getProperty(sdInst,"ReadIOTimeCounter");
				 def KBytesRead = $smis.getProperty(sdInst,"KBytesRead");
				 def WriteIOs = $smis.getProperty(sdInst,"WriteIOs");
				 def IdleTimeCounter = $smis.getProperty(sdInst,"IdleTimeCounter");
				 def ReadTimeMax = $smis.getProperty(sdInst,"ReadTimeMax");
				 def WriteTimeMax = $smis.getProperty(sdInst,"WriteTimeMax");
				 
				 def result = $result.create(diskName);
				 result.clazz = 'physicalDisk';
				 
				 result.perf."diskdrive-io".total_ios = NumberContext.round(TotalIOs/1024,2);
				 result.perf."diskdrive-io".read_ios =  NumberContext.round(ReadIOs/1024,2);
				 result.perf."diskdrive-io".write_ios =  NumberContext.round(WriteIOs/1024,2);
				 result.perf."diskdrive-io".write_rate =  NumberContext.round(KBytesWritten/1024,2);
				 result.perf."diskdrive-io".read_rate =  NumberContext.round(KBytesRead/1024,2);
				 result.perf."diskdrive-io".read_write_rate =  NumberContext.round(KBytesTransferred/1024,2);
				 result.perf."diskdrive-io".read_time_max =  NumberContext.round(ReadTimeMax/1000,2);
				 result.perf."diskdrive-io".write_time_max =  NumberContext.round(WriteTimeMax/1000,2);
				 result.perf."diskdrive-io".total_timeCounter =  NumberContext.round(IOTimeCounter/1000,2);
				 
				}
			}
		}
		}
		}
	
	