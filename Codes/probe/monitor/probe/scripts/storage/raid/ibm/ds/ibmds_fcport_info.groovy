/*!Action
 action.name=通过smi-s协议监测FC端口
 action.descr=通过smi-s协议监测FC端口
 action.protocols=smis
 monitor.output=IBMDS-FCPORT-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.IBMDSUtils;

executeMonitor();
return $result;

def executeMonitor() {
	
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInsts = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def fcportInstances = IBMDSUtils.findCIMInstances($smis,'CIM_FCPort');
	arrayInsts.each{arrayInst->
	def sysname =  $smis.getProperty(arrayInst, 'Name');
	fcportInstances.each{fcPortInst->
		def name =  $smis.getProperty(fcPortInst, 'ElementName');
		def DeviceID =  $smis.getProperty(fcPortInst, 'DeviceID');
		def SystemName =  $smis.getProperty(fcPortInst, 'SystemName');
		if(SystemName.startsWith(sysname)){
		def result = $result.create(DeviceID);
		result.clazz = 'FCPort';
		def linkTechnology = $smis.getProperty(fcPortInst, 'LinkTechnology');
		result.attr.permanentAddress = $smis.getProperty(fcPortInst, 'PermanentAddress');
		result.attr.usageRestriction = dsUtil.getUsageRestriction($smis.getProperty(fcPortInst, 'UsageRestriction'));
		result.attr.portIdx = $smis.getProperty(fcPortInst, 'PortNumber');
		result.attr.speed = $smis.getProperty(fcPortInst, 'Speed');
		result.attr.maxSpeed = $smis.getProperty(fcPortInst, 'MaxSpeed');
		result.attr.portAlias = $smis.getProperty(fcPortInst, 'DeviceID');
		result.attr.portType = dsUtil.getFCPortType($smis.getProperty(fcPortInst, 'PortType'));
		def portStatus = $smis.getProperty(fcPortInst, 'OperationalStatus')[0];
		result.state.available_status = (portStatus == '2' ? 1 : 0);
		}
	}
	}
}


