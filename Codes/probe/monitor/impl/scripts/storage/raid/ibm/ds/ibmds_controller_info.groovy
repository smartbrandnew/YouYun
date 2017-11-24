/*!Action
 action.name=通过smi-s协议监测POWERSUPPLY
 action.descr=通过smi-s协议监测POWERSUPPLY
 action.protocols=smis
 monitor.output=IBMDS-CONTROLLER-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def ctrInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_ControllerCanister');
	def ctrPs = IBMDSUtils.findCIMInstances($smis,'LSISSI_ControllerProduct');

	arrayInst.each{systemInst->
		def arrayCOP = systemInst.getObjectPath();
		def name = $smis.getProperty(systemInst, 'Name');
		ctrInstances.each{ctrInst->
			def controllerName = $smis.getProperty(ctrInst, 'ElementName');
			def Manufacturer = $smis.getProperty(ctrInst, 'Manufacturer');
			def Model = $smis.getProperty(ctrInst, 'Model');
			def SerialNumber = $smis.getProperty(ctrInst, 'SerialNumber');
			def PartNumber = $smis.getProperty(ctrInst, 'PartNumber');
			def SystemName = $smis.getProperty(ctrInst, 'StorageSystem_Name');
			def deviceID = $smis.getProperty(ctrInst, 'Tag');
			def state = $smis.getProperty(ctrInst, 'OperationalStatus')[0];
			if(SystemName==name){
			def Version = null;
			def idf = null; 
			def sn = null;
				ctrPs.each{ctrP->
					 idf = $smis.getProperty(ctrP, 'IdentifyingNumber');
					 sn = $smis.getProperty(ctrP, 'StorageSystem_Name');
					if(sn+"_"+idf==deviceID){
					 Version = $smis.getProperty(ctrP, 'Version');
					}
				}
	
			def result = $result.create(controllerName+"_"+SerialNumber);
			result.clazz = 'RAIDController';
			result.attr.productName = PartNumber;
			result.attr.partNum = Model;
			result.attr.serial = SerialNumber;
			result.attr.mfg = Manufacturer;
			}
		};
	};
}
