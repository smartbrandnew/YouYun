/*!Action
 action.name=通过smi-s协议监测THREEPAR存储控制器信息
 action.descr=通过smi-s协议THREEPAR存储控制器信息
 action.protocols=smis
 monitor.output=THREEPAR-STORAGECONTROLLER-INFO
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

	         String nodeDescription = ctrlInst.getProperty("Description");
             String nodeElementName =ctrlInst.getProperty("ElementName");
             String nodeName = ctrlInst.getProperty("Name");
             String nodePosition = ctrlInst.getProperty("Position");
             String isMasterNode = ctrlInst.getProperty("IsMaster");
             String isNodeOnline = ctrlInst.getProperty("IsOnline");
             String isNodeInCluster = ctrlInst.getProperty("IsInCluster");
             String kernelVersion = ctrlInst.getProperty("KernelVersion");
             String biosVersion = ctrlInst.getProperty("BiosVersion");
             String systemLED = "Not Available";
             try
             {
               systemLED = ThreePARUtils.getLEDStatus(ctrlInst.getProperty("SystemLED"));
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }
             String cacheEnabled = ctrlInst.getProperty("CacheEnabled");
             String nodeStatus = "Not Available";
             try
             {
               nodeStatus = ThreePARUtils.getNodeOperationalStatus(ctrlInst.getProperty("OtherOperationalStatus"));
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }
             
             String nodePackageTag = "Not Available";
             String nodeManufacturer = "Not Available";
             String nodeModel = "Not Available";
             String nodeSerialNumber = "Not Available";
             def nodeCOP = ctrlInst.getObjectPath();
             def nodePackages = ThreePARUtils.findNodePackageInstances($smis, nodeCOP);
             nodePackages.each{node->
             	nodePackageTag = nodePackage.getProperty("Tag");
             	nodeManufacturer = nodePackage.getProperty("Manufacturer");
             	nodeModel = nodePackage.getProperty("Model");
             	nodeSerialNumber = nodePackage.getProperty("SerialNumber");
             }
             
             def result = $result.create(nodeElementName);
             result.clazz="RAIDController";
 			//result.attr.position = nodePosition;
			result.attr.mfg = nodeManufacturer;
			result.attr.serial = nodeSerialNumber;
			result.attr.partNum = nodeModel;
			result.state.available_status = (nodeStatus == 'OK' ? 1 : 0);
             

		}
	}
}