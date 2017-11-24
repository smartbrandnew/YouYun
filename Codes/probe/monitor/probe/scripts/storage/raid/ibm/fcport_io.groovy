/*!Action
 action.name=通过smi-s协议监测FC端口
 action.descr=通过smi-s协议监测FC端口
 action.protocols=smis
 monitor.output=IBMSVC-DISKARRAY-FCPORT-IO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each{clusterInst->
		def clusterCOP = clusterInst.getObjectPath();

		def clusterName = $smis.getProperty(clusterInst, 'ElementName');
		if (clusterName == null) {
			return;
		}
		fcPortMonitor(clusterCOP);
	}
}

def fcPortMonitor(def clusterCOP) {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def fcPortInstances = IBMSVCUtils.findFCPortByCluster($smis, clusterCOP);
	fcPortInstances.each{fcPortInst->
		def fcPortCOP = fcPortInst.getObjectPath();
		def name = 'fc_port_' + $smis.getProperty(fcPortInst, 'FCIOPortID');
		def result = $result.create(name);
		result.clazz = 'FCPort';
		result.attr.portAlias = $smis.getProperty(fcPortInst, 'DeviceID');
		def statsInstance = IBMSVCUtils.findFCPortStatByCluster($smis, fcPortCOP);
		
		def lossOfSignalCounter = $smis.getProperty(statsInstance, 'LossOfSignalCounter');
		def lossOfSyncCounter = $smis.getProperty(statsInstance, 'LossOfSyncCounter');
		result.perf."port-speed".mcast_in_pkts_rate = $smis.getProperty(statsInstance, 'MulticastFramesReceived');
		result.perf."port-speed".mcast_out_pkts_rate = $smis.getProperty(statsInstance, 'MulticastFramesTransmitted');
		def NOSCount = $smis.getProperty(statsInstance, 'NOSCount');
		def LinkFailures = $smis.getProperty(statsInstance, 'LinkFailures');
		
		
		//instanceId|date|bytesReceived|bytesTransmitted
		String instanceId = $smis.getProperty(statsInstance, 'InstanceID');
		long currentDate = new Date().getTime();
		double bytesReceived = $smis.getProperty(statsInstance, 'BytesReceived');
		double bytesTransmitted = $smis.getProperty(statsInstance, 'BytesTransmitted');
		StringBuilder sb = new StringBuilder();
		sb.append(instanceId).append("|");
		sb.append(currentDate).append("|");
		sb.append(bytesReceived).append("|");
		sb.append(bytesTransmitted).append("\n");
		
		StringBuilder speedList = new StringBuilder();
		File file = new File("portSpeed.dat");
		if (file.exists()) {
			file.eachLine {
				if (it.contains(instanceId)) {
					String[] info = it.split("\\|");
					long updated = (currentDate - Long.valueOf(info[1])) / 1000;
					System.out.println("详情：" + info + ", updated:" + updated + ", " + (bytesReceived - Double.valueOf(info[2])) / updated + ", " + (bytesTransmitted - Double.valueOf(info[3])) / updated);
					System.out.println(bytesReceived + "-" + Double.valueOf(info[2]) + "         " + bytesTransmitted +"-" + Double.valueOf(info[3]));
					result.perf."port-speed".input_rate = (bytesReceived - Double.valueOf(info[2])) / updated;
					result.perf."port-speed".output_rate = (bytesTransmitted - Double.valueOf(info[3])) / updated;
				} else {
					speedList.append(it).append('\n');
				}
			}
			file.delete()
		}
		
		speedList.append(sb.toString());
		def printWriter = file.newPrintWriter() //
		printWriter.write(speedList.toString())
	
		printWriter.flush()
		printWriter.close()

		def portStatus = svcUtil.getOperationalStatus($smis.getProperty(fcPortInst, 'OperationalStatus')[0]);
		result.state.available_status = (portStatus == 'OK' ? 1 : 0);
	}
}
