/*!Action
 action.name=通过smi-s协议监测THREEPAR磁盘接口卡信息
 action.descr=通过smi-s协议THREEPAR磁盘接口卡信息
 action.protocols=smis
 monitor.output=THREEPAR-INTERFACECARDS-INFO
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
				def interfaceCards = ThreePARUtils.findInterfaceCards($smis, decop);
				interfaceCards.each{interfaceCard->
					def icTag = interfaceCard.getProperty("Tag");
					def icElmName = interfaceCard.getProperty("ElementName");
					def icManufacturer = interfaceCard.getProperty("Manufacturer");
					def icSerialNumber = interfaceCard.getProperty("SerialNumber");
					def icPosition = interfaceCard.getProperty("Position");
					def icCageType = interfaceCard.getProperty("CageType");
					def icIsMaster = interfaceCard.getProperty("IsMaster");
					def icFWStatus = interfaceCard.getProperty("FirmwareStatus");
					def icFWVersion = interfaceCard.getProperty("FirmwareVersion");
					def dc3ICState = "Not Available";
	                try
	                {
	                  dc3ICState = ThreePARUtils.getInterfaceCardStatus(interfaceCard.getProperty("IFCState"));
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                def ifcStateSeverity = -1;
	                try
	                {
	                  ifcStateSeverity = ThreePARUtils.getSeverityForInterfaceCardStatus(dc3ICState);
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	                def eshState = "Not Available";
	                try
	                {
	                  eshState = ThreePARUtils.getDC3ICESHState(interfaceCard.getProperty("ESHState"));
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                def eshStateSeverity = -1;
	                try
	                {
	                  eshStateSeverity = ThreePARUtils.getSeverityForESHState(eshState);
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	                def exESHState = "Not Available";
	                try
	                {
	                  exESHState = ThreePARUtils.getDC3EXTESHState(interfaceCard.getProperty("ExtendedESHState"));
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                def extESHStateSeverity = -1;
	                try
	                {
	                  extESHStateSeverity = ThreePARUtils.getSeverityForEXTESHState(exESHState);
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	                def icState = "Not Available";
	                try
	                {
	                  icState = ThreePARUtils.getOperationalStatus(interfaceCard.getProperty("OperationalStatus")));
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	                def loopAPortWWNs = "Not Available";
	                try
	                {
	                  Vector portWWNs = ThreePARUtils.getVectorValue(interfaceCard, "LoopAPortWWNs");
	                  if (portWWNs != null)
	                  {
	                    loopAPortWWNs = ThreePARUtils.convertVectorToString(portWWNs, ",");
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                def loopBPortWWNs = "Not Available";
	                try
	                {
	                  Vector portWWNs = ThreePARUtils.getVectorValue(interfaceCard, "LoopBPortWWNs");
	                  if (portWWNs != null)
	                  {
	                    loopBPortWWNs = ThreePARUtils.convertVectorToString(portWWNs, ",");
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                def loopAPortNodeWWNs = "Not Available";
	                try
	                {
	                  Vector portWWNs = ThreePARUtils.getVectorValue(interfaceCard, "NodePortsLoopA");
	                  if (portWWNs != null)
	                  {
	                    loopAPortNodeWWNs = ThreePARUtils.convertVectorToString(portWWNs, ",");
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                String loopBPortNodeWWNs = "Not Available";
	                try
	                {
	                  Vector portWWNs = ThreePARUtils.getVectorValue(interfaceCard, "NodePortsLoopB");
	                  if (portWWNs != null)
	                  {
	                    loopBPortNodeWWNs = ThreePARUtils.convertVectorToString(portWWNs, ",");
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                String loopALinkSpeed = "Not Available";
	                try
	                {
	                  Vector linkSpeeds = ThreePARUtils.getVectorValue(interfaceCard, "LoopALinkSpeed");
	                  if (linkSpeeds != null)
	                  {
	                    loopALinkSpeed = ThreePARUtils.convertVectorToString(linkSpeeds, ",") + " (in Gbs)";
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                String loopBLinkSpeed = "Not Available";
	                try
	                {
	                  Vector linkSpeeds = ThreePARUtils.getVectorValue(interfaceCard, "LoopBLinkSpeed");
	                  if (linkSpeeds != null)
	                  {
	                    loopBLinkSpeed = ThreePARUtils.convertVectorToString(linkSpeeds, ",") + " (in Gbs)";
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                String loopAPorts = "Not Available";
	                try
	                {
	                  Vector ports = ThreePARUtils.getVectorValue(interfaceCard, "PortsLoopA");
	                  if (ports != null)
	                  {
	                    loopAPorts = ThreePARUtils.convertVectorToString(ports, ",");
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                String loopBPorts = "Not Available";
	                try
	                {
	                  Vector ports = ThreePARUtils.getVectorValue(interfaceCard, "PortsLoopB");
	                  if (ports != null)
	                  {
	                    loopBPorts = ThreePARUtils.convertVectorToString(ports, ",");
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	
	                String loopState = "Not Available";
	                try
	                {
	                  Vector states = ThreePARUtils.getVectorValue(interfaceCard, "LoopState");
	                  if (states != null)
	                  {
	                    Vector stateStringVec = new Vector();
	                    for (int j = 0; j < states.size(); j++)
	                    {
	                      String stateInString = ThreePARArrayUtil.getLoopState(states.get(j).toString());
	                      stateStringVec.add(stateInString);
	                    }
	                    loopState = ThreePARUtils.convertVectorToString(stateStringVec, ",");
	                  }
	                }
	                catch (Exception e)
	                {
	                  e.printStackTrace();
	                }
	                
	                
	                def result = $result.create(icElmName);
	                result.clazz="ifCards";
					result.attr.mfg = icManufacturer;
					result.attr.serial = icSerialNumber;
					result.attr.position = icPosition;
					
	                result.attr.icTag = icTag;
					result.attr.cageType = cageType;
					result.attr.icIsMaster =icIsMaster ;
					
					result.attr.icFWVersion =icFWVersion ;
					
					result.attr.ifcStateSeverity=ifcStateSeverity;
					
					result.attr.eshStateSeverity=eshStateSeverity;
					
					result.attr.extESHStateSeverity=extESHStateSeverity;
					
					result.attr.loopAPortWWNs=loopAPortWWNs;
					result.attr.loopBPortWWNs=loopBPortWWNs;
					result.attr.loopAPortNodeWWNs=loopAPortNodeWWNs;
					result.attr.loopBPortNodeWWNs=loopBPortNodeWWNs;
					result.attr.loopALinkSpeed=loopALinkSpeed;
					result.attr.loopBLinkSpeed=loopBLinkSpeed;
					result.attr.loopAPorts=loopAPorts;
					result.attr.loopBPorts=loopBPorts;
					
					result.state.loopState=loopState;
					result.state.icFWStatus =icFWStatus;
					result.state.icState=icState;
					result.state.eshState=eshState;
					result.state.dc3ICState=dc3ICState;
					result.state.exESHState=exESHState;
				}
			}
			
		}
		
	}
	
}