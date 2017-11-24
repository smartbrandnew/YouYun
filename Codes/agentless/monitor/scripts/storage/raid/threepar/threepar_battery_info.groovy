/*!Action
 action.name=通过smi-s协议监测THREEPAR电池信息
 action.descr=通过smi-s协议THREEPAR电池信息
 action.protocols=smis
 monitor.output=THREEPAR-BATTERY-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.ThreePARUtils;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/threepar/threepar_util');
	def arrayInst = svcUtil.getCIMInstances();
	arrayInst.each{cimInst->
		def cop = cimInst.getObjectPath();
		def ctrlInstances = ThreePARUtils.findCtrlInstances($smis, cop);
		ctrlInstances.each{ctrlInst->
             def nodeCOP = ctrlInst.getObjectPath();
             def nodeBatterys = ThreePARUtils.findControllerBatteries($smis, nodeCOP);
             nodeBatterys.each{nodeBattery->
	             String batteryElementName = nodeBattery.getProperty("ElementName");
	             String batteryDeviceId = nodeBattery.getProperty("DeviceID");
	             String batteryStatus = "Not Available";
	             try
	             {
	               batteryStatus = svcUtil.getBatteryStatus(nodeBattery.getProperty("BatteryStatus"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String estimatedRunTime = nodeBattery.getProperty("EstimatedRunTime") + " minutes";
	
	             String maxRechargeTime = nodeBattery.getProperty("MaxRechargeTime") + " minutes";
	             String position = nodeBattery.getProperty("Position");
	
	             String maxLifeLow = nodeBattery.getProperty("MaxLifeLow");
	             String batteryManufacturer = nodeBattery.getProperty("Manufacturer");
	             String batteryModel = nodeBattery.getProperty("Model");
	             String batterySerialNumber = nodeBattery.getProperty("SerialNumber");
	
	             String otherOperationalStatus = "Not Available";
	             try
	             {
	               otherOperationalStatus = svcUtil.getBatteryStatus(nodeBattery.getProperty("OtherOperationalStatus"));
	             }
	             catch (Exception e)
	             {
	               e.printStackTrace();
	             }
	
	             String installDate = "Not Available";
	             try
	             {
	               installDate = ThreePARUtils.convertDateTimeToSANDate(nodeBattery.getProperty("InstallDate").getValue().toString());
	             }
	             catch (Exception e)
	             {
	             }
	
	             String manufacturingDate = "Not Available";
	             try
	             {
	               manufacturingDate = ThreePARUtils.convertDateTimeToSANDate(nodeBattery.getProperty("ManufacturingDate").getValue().toString());
	             }
	             catch (Exception e)
	             {
	             }
	
	             String expirationDate = "Not Available";
	             try
	             {
	               expirationDate = ThreePARUtils.convertDateTimeToSANDate(nodeBattery.getProperty("ExpirationDate").getValue().toString());
	             }
	             catch (Exception e)
	             {
	             }
	             
	             def result = $result.create(batteryElementName);
	 			 result.clazz = 'Battery';
	 			 result.attr.batteryDeviceId = batteryDeviceId;
	 			//result.attr.position = position;
	 			result.attr.mfg = batteryManufacturer;
	 			result.attr.partNum = batteryModel;
	 			result.attr.serial = batterySerialNumber;
	 			//result.attr.installDate=installDate;	
	 			result.attr.maxLifeLow=maxLifeLow;	
	 			//result.attr.manufacturingDate=manufacturingDate;	
	 			//result.attr.expirationDate=expirationDate;	
	 			
	 			//result.perf.runTime.estimatedRunTime=estimatedRunTime;	
	 			//result.perf.runTime.maxRechargeTime=maxRechargeTime;	
             }
		}
	}
}
	         