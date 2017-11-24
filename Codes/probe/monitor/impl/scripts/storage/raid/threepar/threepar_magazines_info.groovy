/*!Action
 action.name=通过smi-s协议监测THREEPAR磁盘软片盒信息
 action.descr=通过smi-s协议THREEPAR磁盘软片盒信息
 action.protocols=smis
 monitor.output=THREEPAR-MAGEZINES-INFO
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
				def decop = enclosure.getObjectPath();
				def magazines = ThreePARUtils.findPowerSupply($smis, decop);
				magazines.each{magazine->
					String magazineTag = magazine.getProperty("Tag");
	                String magazineElmName = magazine.getProperty("ElementName");
	                String magazineManufacturer = magazine.getProperty("Manufacturer");
	                String magazineModel = magazine.getProperty("Model");
	                String magazineSerialNumber = magazine.getProperty("SerialNumber");
	                String magazineLocationType = magazine.getProperty("LocationType");
	                String magazinePosition = magazine.getProperty("Position");
	                String magazineCageType = magazine.getProperty("CageType");
	                String magazineStateOnLoopA = "Not Available";
	                try
	                {
	                    magazineStateOnLoopA = ThreePARUtils.getMagazineStateOnLoop(magazine.getProperty("StateOnLoopA"));
	                }
	                catch (Exception e)
	                {
	                    e.printStackTrace();
	                }
	
	                String magazineStateOnLoopB = "Not Available";
	                try
	                {
	                    magazineStateOnLoopB = ThreePARUtils.getMagazineStateOnLoop(magazine.getProperty("StateOnLoopB"));
	                }
	                catch (Exception e)
	                {
	                    e.printStackTrace();
	                }
	
	                String systemLED = "Not Available";
	                try
	                {
	                    systemLED = ThreePARUtils.getLEDStatus(magazine.getProperty("SystemLED"));
	                }
	                catch (Exception e)
	                {
	                    e.printStackTrace();
	                }

	                String hotPlugLED = "Not Available";
	                try
	                {
	                   hotPlugLED = ThreePARUtils.getLEDStatus(magazine.getProperty("HotplugLED"));
	                }
	                catch (Exception e)
	                {
	                   e.printStackTrace();
	                }
	
	                String magazineState = "Not Available";
	                try
	                {
	                   magazineState = ThreePARUtils.getOperationalStatus(ThreePARUtils.getVectorValue(magazine, "OperationalStatus"));
	                }
	                catch (Exception e)
	                {
	                   e.printStackTrace();
	                }

	                def result = $result.create(magazineElmName);
	    			result.clazz = 'magazines';
	    			result.attr.mfc =magazineManufacturer;
	    			result.attr.partNum=magazineModel;
	    			result.attr.serial=magazineSerialNumber;
	    			result.attr.position=magazinePosition;
	    			
	    			result.attr.magazineLocationType=magazineLocationType;
	    			result.state.magazineStateOnLoopA=magazineStateOnLoopA;
	    			result.state.magazineStateOnLoopB=magazineStateOnLoopB;
	    			result.attr.systemLED=systemLED;
	    			result.attr.hotPlugLED=hotPlugLED;
	    			
	    			result.state.available_status = (magazineState == 'OK' ? 1 : 0);
				}
			}
			
		}
		
	}
	
}