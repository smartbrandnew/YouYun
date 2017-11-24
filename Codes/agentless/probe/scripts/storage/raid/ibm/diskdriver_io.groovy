/*!Action
 action.name=通过smi-s协议监测硬盘性能信息
 action.descr=通过smi-s协议监测硬盘性能信息
 action.protocols=smis
 monitor.output=IBMSVC-DISKDRIVER-IO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

System.setProperty("https.protocols", "TLSv1.2");
executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def mdiskInstances = IBMSVCUtils.findMDiskInstancesByCluster($smis, clusterCOP);
		def diskDriverInsts = IBMSVCUtils.findDiskDriverByCluster($smis, clusterCOP);
		diskDriverInsts.each {diskInst->
			def diskCOP = diskInst.getObjectPath();
			def diskName = $smis.getProperty(diskInst, 'Name');
			def result = $result.create(diskName);
			result.clazz = 'physicalDisk';
			def diskPerfInstance = IBMSVCUtils.findDiskDriverStatsInstances($smis, diskCOP)[0];
			
			
			//instanceId|date|bytesReceived|bytesTransmitted
			String instanceId = $smis.getProperty(diskPerfInstance, 'InstanceID');
			long currentDate = new Date().getTime();
			double bytesReceived = $smis.getProperty(diskPerfInstance, 'KBytesRead');
			double bytesTransmitted = $smis.getProperty(diskPerfInstance, 'KBytesWritten');
			def readWrite = $smis.getProperty(diskPerfInstance, 'KBytesTransferred');
			def readIos = $smis.getProperty(diskPerfInstance, 'ReadIOs');
			def writeIos = $smis.getProperty(diskPerfInstance, 'WriteIOs');
			StringBuilder sb = new StringBuilder();
			sb.append(instanceId).append("|");
			sb.append(currentDate).append("|");
			sb.append(bytesReceived).append("|");
			sb.append(bytesTransmitted).append("|");
			sb.append(readWrite).append("|");
			sb.append(writeIos).append("|");
			sb.append(readIos).append("\n");
			
			StringBuilder speedList = new StringBuilder();
			File file = new File("diskSpeed.dat");
			if (file.exists()) {
				file.eachLine {
					if (it.contains(instanceId)) {
						String[] info = it.split("\\|");
						long updated = (currentDate - Long.valueOf(info[1])) / 1000;
						System.out.println("详情：" + info + ", updated:" + updated + ", " + (bytesReceived - Double.valueOf(info[2])) / updated + ", " + (bytesTransmitted - Double.valueOf(info[3])) / updated);
						System.out.println(bytesReceived + "-" + Double.valueOf(info[2]) + "         " + bytesTransmitted +"-" + Double.valueOf(info[3]));
						result.perf."diskdrive-io".read_count_per_sec = (readIos - Double.valueOf(info[6])) / updated;
						result.perf."diskdrive-io".write_count_per_sec = (writeIos - Double.valueOf(info[5])) / updated;
						result.perf."diskdrive-io".read_rate = (bytesReceived - Double.valueOf(info[2])) / updated;
						result.perf."diskdrive-io".read_write_rate = (readWrite - Double.valueOf(info[4])) / updated;
						result.perf."diskdrive-io".write_rate = (bytesTransmitted - Double.valueOf(info[3])) / updated;
					} else {
						speedList.append(it).append('\n');
					}
				}
				file.delete()
			}
		
			speedList.append(sb.toString());
			System.out.println("端口速率信息：" + sb.toString());	  
			def printWriter = file.newPrintWriter() //
			printWriter.write(speedList.toString())
		
			printWriter.flush()
			printWriter.close()

			
			
			
			
			
			/*
			
			result.perf."diskdrive-io".read_count_per_sec = $smis.getProperty(diskPerfInstance, 'ReadIOs');
			result.perf."diskdrive-io".write_count_per_sec = $smis.getProperty(diskPerfInstance, 'WriteIOs');
			result.perf."diskdrive-io".read_rate = $smis.getProperty(diskPerfInstance, 'KBytesRead');
			result.perf."diskdrive-io".read_write_rate = $smis.getProperty(diskPerfInstance, 'KBytesTransferred');
			result.perf."diskdrive-io".write_rate = $smis.getProperty(diskPerfInstance, 'KBytesWritten');
			def TotalIOs = $smis.getProperty(diskPerfInstance, 'TotalIOs');
			def WriteIOTimeCounter = $smis.getProperty(diskPerfInstance, 'WriteIOTimeCounter');
			def ReadIOTimeCounter = $smis.getProperty(diskPerfInstance, 'ReadIOTimeCounter');
			def IOTimeCounter = $smis.getProperty(diskPerfInstance, 'IOTimeCounter');
			*/
			def use = $smis.getProperty(diskInst, 'Use'); 
			result.state.available_status = ((use==2 || use == 1 || use == 4) ? 1 : 0);
		}
	}
	
}
