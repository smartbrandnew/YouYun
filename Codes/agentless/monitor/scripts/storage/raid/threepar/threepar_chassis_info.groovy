/*!Action
 action.name=通过smi-s协议监测THREEPAR底盘信息
 action.descr=通过smi-s协议THREEPAR底盘信息
 action.protocols=smis
 monitor.output=THREEPAR-CLASSIS-INFO
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
		def packageInsts = ThreePARUtils.findDiskPhysicalPackage($smis, cop);
		packageInsts.each{packageInst->
			
			def ppcop = packageInst.getObjectPath();
			def enclosures = findArrayDiskEnclosures($smis, ppcop);
			enclosures.each{enclosure ->
				def deName = enclosure.getProperty("Name");
				def deTag = enclosure.getProperty("Tag");
				def deElmName = enclosure.getProperty("ElementName");
				def deManufacturer = enclosure.getProperty("Manufacturer");
				def deModel = enclosure.getProperty("Model");
				def deSerialNumber = enclosure.getProperty("SerialNumber");
				def deChassisPackageType = enclosure.getProperty("ChassisPackageType");
				def deChassisType = enclosure.getProperty("ChassisTypeDescription");
				def deMSSupport = enclosure.getProperty("MultipleSystemSupport");
				def deRackMountable = enclosure.getProperty("RackMountable");
				def deCageType = "Not Available";
				try
                {
					deCageType = ThreePARUtils.getCageType(enclosure.getProperty("CageType"));
                }
                catch (Exception e)
                {
                  e.printStackTrace();
                }
                def deCageSubType = "Not Available";
                try
                {
                	deCageSubType = ThreePARUtils.getCageSubType(enclosure.getProperty("CageSubType"));
                }
                catch (Exception e)
                {
                  e.printStackTrace();
                }
				
				def dePosition = enclosure.getProperty("Position");
				def deLocation = enclosure.getProperty("Location");
				def deLoopSplit = enclosure.getProperty("LoopSplit");
				def deChainPositionLoopA = enclosure.getProperty("ChainPositionLoopA");
				def deChainPositionLoopB = enclosure.getProperty("ChainPositionLoopB");
				def deLocateON = enclosure.getProperty("LocateON");
				def deTempSensorState = "Not Available";
				try{
					deTempSensorState = ThreePARUtils.getDC3DriveCageTempSensorState(enclosure.getProperty("TempSensorState"));
				} catch (Exception e)
				{
					e.printStackTrace();
				}
				def deTempSensorValue = enclosure.getProperty("TempSensorValue");
				def deTempSensorThreshold = enclosure.getProperty("TempSensorThreshold");
				def deOperatorPanelState = "Not Available";
				try{
					deOperatorPanelState = ThreePARUtils.getDC3DriveCageOperatorePanelState(enclosure.getProperty("OperatorPanelState"));
				} catch(Exception e)
				{
					e.printStackTrace();
				}
				def deAudibleAlarmState = "Not Available";
				try{
					deAudibleAlarmState = ThreePARUtils.getDC3DriveCageAudibleAlarmState(enclosure.getProperty("AudibleAlarmState"));
				} catch(Exception e)
				{
					e.printStackTrace();
				}
					
				def deEnclosureId = enclosure.getProperty("EnclosureID");
				
				
				def result = $result.create(deElmName);
				result.clazz = 'ThreePAR';
				result.attr.fwOSName = deName;
				result.attr.partNum = deModel;
				result.attr.mfg = deManufacturer;
				result.attr.descript = deChassisType;
				result.attr.sequenceNum =  deSerialNumber;
				
				result.attr.chassisPackageType = deChassisPackageType; 
				result.state.temp_sensor_status=deTempSensorState;	
				result.state.operator_panel_status=deOperatorPanelState;	
				result.state.audible_alarm_status=deAudibleAlarmState;	
			}
		}
	}
}