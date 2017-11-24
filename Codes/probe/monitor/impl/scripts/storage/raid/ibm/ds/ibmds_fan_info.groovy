/*!Action
 action.name=通过smi-s协议监测LUN
 action.descr=通过smi-s协议监测LUN
 action.protocols=smis
 monitor.output=IBMDS-FAN-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.IBMDSUtils; 

executeMonitor();
return $result;


def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def fanInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_Fan');
	def fanpInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_FanPackage');
	arrayInst.each{systemInst->
		def arrayCOP = systemInst.getObjectPath();
		def name = $smis.getProperty(systemInst, 'Name');
		fanInstances.each{fanInst->
			def fanName = $smis.getProperty(fanInst, 'ElementName');
			def SystemName = $smis.getProperty(fanInst, 'SystemName');
			def deviceID = $smis.getProperty(fanInst, 'DeviceID');
			def state = dsUtil.getOperationalStatus($smis.getProperty(fanInst, 'OperationalStatus')[0]);
			if(SystemName==name){
			fanpInstances.each{fanpInst->
				def tag = $smis.getProperty(fanpInst, 'Tag');
				def type = $smis.getProperty(fanpInst, 'PackageType');
				if(tag==SystemName+"_"+deviceID){
					def result = $result.create(fanName+"_"+deviceID);
					result.clazz = 'FAN';

					//result.attr.flags = tag;
					result.attr.storResCode = deviceID;
					//result.attr.productName = $smis.getProperty(fanpInst, 'PartNumber');
					result.attr.partNum = $smis.getProperty(fanpInst, 'Model');
					//result.attr.serial = $smis.getProperty(fanpInst, 'SerialNumber');
					result.attr.mfg = $smis.getProperty(fanpInst, 'Manufacturer');
					result.attr.canFRU = ($smis.getProperty(fanpInst, 'CanBeFRUed')=='TRUE'?'是':'否');
					
					result.state.available_status = (state == 'OK' ? 1 : 0);
				}
			}
			}
		};
	};
}
