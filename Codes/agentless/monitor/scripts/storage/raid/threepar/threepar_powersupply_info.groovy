/*!Action
 action.name=通过smi-s协议监测THREEPAR磁盘电源信息
 action.descr=通过smi-s协议THREEPAR磁盘电源信息
 action.protocols=smis
 monitor.output=THREEPAR-POWERSUPPLY-INFO
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
				def cagePowerSupplies = ThreePARUtils.findPowerSupply($smis, decop);
				cagePowerSupplies.each{cagePowerInst->
					def psDisplayName = cagePowerInst.getProperty("ElementName");
	                def psDeviceId = cagePowerInst.getProperty("DeviceID");
	                def psManufacturer = cagePowerInst.getProperty("Manufacturer");
	                def psModel = ScagePowerInst.getProperty("Model");
	                def psSerialNumber = cagePowerInst.getProperty("SerialNumber");
	                def psPosition = cagePowerInst.getProperty("Position");
	                def psACStatus = "Not Available";
	                try
	                {
	                  psACStatus = ThreePARUtils.getPowerSupplyACStatus(cagePowerInst.getProperty("ACStatus"));
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	                def psACSeverity = -1;
	                try
	                {
	                  psACSeverity = ThreePARUtils.getSeverityForPowerSupplyACStatus(psACStatus);
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
					
	                String psCageType = "Not Available";
	                try
	                {
	                  psCageType = ThreePARUtils.getCageType(cagePowerInst.getProperty("CageType"));
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	                String psState = "Not Available";
	                try
	                {
	                  psState = ThreePARUtils.getOperationalStatus(cagePowerInst.getProperty("OperationalStatus"));
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	    			def result = $result.create(psDisplayName);
	    			result.clazz = 'powerSupply';
	    			result.attr.capacity = null;
	    			
	    			result.attr.productName = $smis.getProperty(enclosureInst, 'MachinePartNumber');
	    			result.attr.partNum = psModel;
	    			result.attr.serial = psSerialNumber;
	    			result.attr.mfg = psManufacturer;
	    			//result.attr.position = psPosition;
	    			
	    			result.state.available_status = psState == 'online' ? 1 : 0;
	    			
	    			result.attr.psACSeverity = psACSeverity;	
	    			result.attr.psCageType = psCageType;		
	    			
				}
			}
			
		}
		
	}
	
}