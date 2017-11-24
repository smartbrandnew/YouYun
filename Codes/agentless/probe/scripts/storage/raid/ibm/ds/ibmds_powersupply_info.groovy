/*!Action
 action.name=通过smi-s协议监测POWERSUPPLY
 action.descr=通过smi-s协议监测POWERSUPPLY
 action.protocols=smis
 monitor.output=IBMDS-POWERSUPPLY-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def psInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_PowerSupply');
	def pspInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_PowerSupplyPackage');
	arrayInst.each{systemInst->
		def arrayCOP = systemInst.getObjectPath();
		def name = $smis.getProperty(systemInst, 'Name');
		psInstances.each{psInst->
			def powerSupplyName = $smis.getProperty(psInst, 'ElementName');
			def SystemName = $smis.getProperty(psInst, 'SystemName');
			def deviceID = $smis.getProperty(psInst, 'DeviceID');
			def state = dsUtil.getOperationalStatus($smis.getProperty(psInst, 'OperationalStatus')[0]);
			if(SystemName==name){
			pspInstances.each{pspInst->
				def tag = $smis.getProperty(pspInst, 'Tag');
				def type = $smis.getProperty(pspInst, 'PackageType');
				if(tag==SystemName+"_"+deviceID){

					def result = $result.create(powerSupplyName+"_"+deviceID);
					result.clazz = 'PowerSupply';

					result.attr.flags = tag;
					result.attr.productName = $smis.getProperty(pspInst, 'PartNumber');
					result.attr.partNum = $smis.getProperty(pspInst, 'Model');
					result.attr.serial = $smis.getProperty(pspInst, 'SerialNumber');
					result.attr.mfg = $smis.getProperty(pspInst, 'Manufacturer');
					result.attr.canFRU = ($smis.getProperty(pspInst, 'CanBeFRUed')=='TRUE'?'是':'否');
					
					result.state.available_status = (state == 'OK' ? 1 : 0);
					
				}
			}
			}
		};
	};
}
