/*!Action
 action.name=通过smi-s协议监测THREEPAR光纤端口信息
 action.descr=通过smi-s协议THREEPAR光纤端口信息
 action.protocols=smis
 monitor.output=THREEPAR-FCPORT-INFO
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
             def nodeFCPorts = ThreePARUtils.findCtlrFCPorts($smis, nodeCOP);
             nodeFCPorts.each{nodeFCPort->
             String nodeFCPortName = nodeFCPort.getproperty("Name");
             String nodeFCPortSystemName = nodeFCPort.getproperty("SystemName");
             String nodeFCPortDeviceId = nodeFCPort.getproperty("DeviceID");
             String wwn = ThreePARUtils.getFormattedWwnString(nodeFCPortDeviceId);
             String nodeFCPortMOName = "FC_PORT_" + nodeFCPortDeviceId;
             String nodeFCPortMaxSpeed = svcUtil.getGbFromBits(nodeFCPort.getproperty("MaxSpeed")) + " Gb/s";
             String nodeFCPortSpeed = svcUtil.getGbFromBits(nodeFCPort.getproperty("Speed")) + "";
             String nodeFCPortLinkTechnology = svcUtil.getLinkTechnology(Integer.parseInt(nodeFCPort.getproperty("LinkTechnology")));
             String nodeFCPortFullDuplex = nodeFCPort.getproperty("FullDuplex");
             String nodeFCPortAutoSense = nodeFCPort.getproperty("AutoSense");
             String nodeSupportedMTU = nodeFCPort.getproperty("SupportedMaximumTransmissionUnit") + " bytes";
             String nodePortType = svcUtil.getPortType(Integer.parseInt(nodeFCPort.getproperty("PortType")));
             String nodeFCPortNumber = nodeFCPortName.substring(nodeFCPortName.lastIndexOf(":") + 1);

             String nodeFCPortOperationalStatus = "Not Available";
             try
             {
               nodeFCPortOperationalStatus = ThreePARUtils.getOperationalStatus(SMICommonUtil.getVectorValue(nodeFCPort, "OperationalStatus"));
             }
             catch (Exception e)
             {
               e.printStackTrace();
             }
             
             def result = $result.create(nodeFCPortName);
     		result.clazz = 'FCPort';
     		def linkTechnology = nodeFCPortLinkTechnology;
     		result.attr.portIdx = nodeFCPortNumber;
     		result.attr.speed = nodeFCPortSpeed;
     		result.attr.portAlias = nodeFCPortDeviceId;
     		result.state.available_status = (nodeFCPortOperationalStatus == 'OK' ? 1 : 0);
     		
     		result.attr.sysName = nodeFCPortSystemName;
     		result.attr.maxSpeed = nodeFCPortMaxSpeed;	
     		result.attr.communicationMode = nodeFCPortFullDuplex;	
     		result.attr.autoSence = nodeFCPortAutoSense;	
     		//result.attr.supportedMTU = nodeSupportedMTU;	
     		result.attr.portType = nodePortType;
             
             }
		}
	}
}
             