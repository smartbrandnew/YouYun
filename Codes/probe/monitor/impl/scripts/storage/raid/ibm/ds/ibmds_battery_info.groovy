/*!Action
 action.name=通过smi-s协议监测POWERSUPPLY
 action.descr=通过smi-s协议监测POWERSUPPLY
 action.protocols=smis
 monitor.output=IBMDS-BATTERY-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.IBMDSUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def dsUtil = $script.use('storage/raid/ibm/ds/ibmds_util');
	def arrayInst = IBMDSUtils.findCIMInstances($smis,'LSISSI_StorageSystem');
	def batteryInstances = IBMDSUtils.findCIMInstances($smis,'LSISSI_Battery');
	def batteryLJs = IBMDSUtils.findCIMInstances($smis,'LSISSI_BatteryLearnJob');
	def batteryPackages = IBMDSUtils.findCIMInstances($smis,'LSISSI_BatteryPackage');
	def batterySensors = IBMDSUtils.findCIMInstances($smis,'LSISSI_BatteryThermalSensor');

	arrayInst.each{systemInst->
		def arrayCOP = systemInst.getObjectPath();
		def name = $smis.getProperty(systemInst, 'Name');
		batteryInstances.each{batteryInst->
			def batteryName = $smis.getProperty(batteryInst, 'ElementName');
			def SystemName = $smis.getProperty(batteryInst, 'SystemName');
			def deviceID = $smis.getProperty(batteryInst, 'System_DeviceID');
			def state = $smis.getProperty(batteryInst, 'OperationalStatus')[0];
			if(SystemName==name){
			def RunMonth = null;
			def RunDay = null;
			def PercentComplete = null;
			def ljstates = null;
				batteryLJs.each{batteryLJ->
					def ljid = $smis.getProperty(batteryLJ, 'DeviceID');
					if(ljid==deviceID){
					 RunMonth = $smis.getProperty(batteryLJ, 'RunMonth');
					 RunDay = $smis.getProperty(batteryLJ, 'RunDay');
					 PercentComplete = $smis.getProperty(batteryLJ, 'PercentComplete');
					 ljstates = $smis.getProperty(batteryLJ, 'OperationalStatus')[0];
					}
				}
				
			def Manufacturer = null;
			def Model = null;
			def SerialNumber = null;
			def PartNumber = null;
			def ManufactureDate = null;
			def tag = null;
				batteryPackages.each{batteryPackage->
				 tag = $smis.getProperty(batteryPackage, 'Tag');
				if(tag==deviceID){
				 Manufacturer = $smis.getProperty(batteryPackage, 'Manufacturer');
				 Model = $smis.getProperty(batteryPackage, 'Model');
				 SerialNumber = $smis.getProperty(batteryPackage, 'SerialNumber');
				 PartNumber = $smis.getProperty(batteryPackage, 'PartNumber');
				 ManufactureDate = $smis.getProperty(batteryPackage, 'ManufactureDate');
				 }
				}
				
			def sensorState = null;
				batterySensors.each{batterySensor->
					def bsid = $smis.getProperty(batterySensor, 'System_DeviceID');
					if(bsid==deviceID){
					sensorState = $smis.getProperty(batterySensor, 'OperationalStatus')[0];
					}
				}
				
			def result = $result.create(batteryName);
			result.clazz = 'Bettery';
			//result.attr.flags = tag;
			result.attr.productName = PartNumber;
			result.attr.partNum = Model;
			result.attr.serial = SerialNumber;
			result.attr.mfg = Manufacturer;
			result.attr.manufacturingDate = ManufactureDate;
			result.attr.useTime = (RunMonth==null?"0":RunMonth)+"月"+(RunDay==null?"0":RunDay)+"天";
			result.attr.storResCode = deviceID;
			
			result.perf.battrty_info.percent_complete = PercentComplete;
			result.state.available_status =(state=="2"?1:0);
			result.state.sensor_status =sensorState;
			result.state.work_status =ljstates;
			}
		};
	};
}
