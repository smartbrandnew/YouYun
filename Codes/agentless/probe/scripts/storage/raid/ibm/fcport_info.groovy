/*!Action
 action.name=通过smi-s协议监测FC端口
 action.descr=通过smi-s协议监测FC端口
 action.protocols=smis
 monitor.output=IBMSVC-DISKARRAY-FCPORT-INFO
 monitor.priority=100
*/
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
		def name = 'fc_port_' + $smis.getProperty(fcPortInst, 'FCIOPortID');
		def result = $result.create(name);
		result.clazz = 'FCPort';
		def linkTechnology = $smis.getProperty(fcPortInst, 'LinkTechnology');
		def otherNetworkPortType = $smis.getProperty(fcPortInst, 'OtherNetworkPortType');
		result.attr.portIdx = $smis.getProperty(fcPortInst, 'PortNumber');
		result.attr.speed = $smis.getProperty(fcPortInst, 'Speed');
		def supportedMaximumTransmissionUnit = $smis.getProperty(fcPortInst, 'SupportedMaximumTransmissionUnit');
		def maxSpeed = $smis.getProperty(fcPortInst, 'MaxSpeed');
		result.attr.portAlias = $smis.getProperty(fcPortInst, 'DeviceID');
		def portStatus = svcUtil.getOperationalStatus($smis.getProperty(fcPortInst, 'OperationalStatus')[0]);
		result.state.available_status = (portStatus == 'OK' ? 1 : 0);
	}
}


