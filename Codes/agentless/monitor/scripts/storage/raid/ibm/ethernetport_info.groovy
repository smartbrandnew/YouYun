/*!Action
 action.name=通过smi-s协议监测网络设备端口
 action.descr=通过smi-s协议监测网络设备端口
 action.protocols=smis
 monitor.output=NetDevPort-info
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
		ethernetPortMonitor(clusterCOP);
	}
}


def ethernetPortMonitor(def clusterCOP) {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def ethernetPortInstances = IBMSVCUtils.findEthernetPortByCluster($smis, clusterCOP);
	ethernetPortInstances.each{ethernetPortInst->
		
		def name = 'ethernet_port_' + $smis.getProperty(ethernetPortInst, 'PortNumber');
		def result = $result.create(name);
		result.clazz = 'NetDevPort';
		result.rs.ComponentOf = 'node';
		def linkTechnology = $smis.getProperty(ethernetPortInst, 'LinkTechnology');
		def otherNetworkPortType = $smis.getProperty(ethernetPortInst, 'OtherNetworkPortType');
		result.attr.portIdx = $smis.getProperty(ethernetPortInst, 'PortNumber');
		result.attr.portType = svcUtil.getPortType($smis.getProperty(ethernetPortInst, 'PortType'));
		result.attr.speed = $smis.getProperty(ethernetPortInst, 'Speed');
		def supportedMaximumTransmissionUnit = $smis.getProperty(ethernetPortInst, 'SupportedMaximumTransmissionUnit');
		def fullDuplex = $smis.getProperty(ethernetPortInst, 'FullDuplex');
		result.attr.communicationMode = fullDuplex ? '全双工' : '半双工';
		result.attr.portAlias = $smis.getProperty(ethernetPortInst, 'DeviceID');
		def status = svcUtil.getOperationalStatus($smis.getProperty(ethernetPortInst, 'OperationalStatus')[0]);
		result.state.available_status = (status == 'OK' ? 1 : 0);
		
	}
}

