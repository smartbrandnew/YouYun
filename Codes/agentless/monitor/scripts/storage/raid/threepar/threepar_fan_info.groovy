/*!Action
 action.name=通过smi-s协议监测THREEPAR 风扇信息
 action.descr=通过smi-s协议THREEPAR 风扇信息
 action.protocols=smis
 monitor.output=THREEPAR-FAN-INFO
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
		def fanInstances = ThreePARUtils.findArrayFans($smis, cop);
		fanInstances.each{fanInst->
			String fanElementName = fanInst.getProperty("ElementName");
	        String fanSystemName = fanInst.getProperty("SystemName");
	        String fanDeviceId = fanInst.getProperty("DeviceID");
	        String fanPosition = fanInst.getProperty("Position");
	        String fanSpeed = fanInst.getProperty("Speed");
	        String fanStatus = "Not Available";
	        try
	        {
	          fanStatus = ThreePARUtils.getFanStatus(fanInst.getProperty("OtherOperationalStatus"));
	        }
	        catch (Exception e)
	        {
	          e.printStackTrace();
	        }
	        
	        def result = $result.create(fanElementName);
			result.clazz = 'FAN';
			
			//new add
			result.attr.speed = fanSpeed;
			//result.attr.fanDeviceId = fanDeviceId;
			result.attr.fanName = fanSystemName;
			
			
			//result.attr.position = fanPosition;
			result.state.available_status = (fanStatus == 'OK' ? 1 : 0);
		}
	}
}