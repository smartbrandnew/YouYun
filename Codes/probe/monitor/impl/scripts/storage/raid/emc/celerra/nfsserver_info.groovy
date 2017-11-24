/*!Action
 action.name=通过smis协议监测FC端口
 action.descr=通过smis协议监测FC端口
 action.protocols=smis
 monitor.output=EMCCELERRA-NFSSERVER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {

	def nfsServerInstances = EMCCelerraUtils.findCelerraNFSServer($smis);
	nfsServerInstances.each{nfsServerInst->
	def nfsServerName = $smis.getProperty(nfsServerInst,'Name');
	def result = $result.create(nfsServerName);
	result.clazz = 'NFSServer';
	result.rs.ComponentOf = 'node';
	
	result.state.available_status = fitchDedicated($smis.getProperty(nfsServerInst,'OperationalStatus'));
	result.attr.serverDedicated = $smis.getProperty(nfsServerInst,'Dedicated');
	
	}
	

}

def fitchDedicated(def dedicated){
	switch(dedicated){
		case 0:
      		return "Not Dedicated";
    	case 1:
     		 return "Unknown";
    	case 2:
     		return "Other";
    	case 3:
     		return "Storage";
    	case 4:
      		return "Router";
    	case 5:
      		return "Switch";
    	case 6:
      		return "Layer 3 Switch";
    	case 7:
      		return "Central Office Switch";
   		case 8:
      		return "Hub";
    	case 9:
      		return "Access Server";
    	case 10:
      		return "Firewall";
    	case 11:
      		return "Print";
    	case 12:
      		return "I/O";
    	case 13:
      		return "Web Caching";
    	case 14:
      		return "Management";
    	case 15:
      		return "Block Server";
    	case 16:
      		return "File Server";
    	case 17:
      		return "Mobile User Device";
    	case 18:
      		return "Repeater";
    	case 19:
      		return "Bridge/Extender";
    	case 20:
      		return "Gateway";
    	case 21:
      		return "Storage Virtualizer";
    	case 22:
      		return "Media Library";
    	case 23:
      		return "ExtenderNode";
    	case 24:
      		return "NAS Head";
    	case 25:
      		return "Self-contained NAS";
    	case 26:
      		return "UPS";
    	case 27:
      		return "IP Phone";
    	case 28:
      		return "Management Controller";
    	case 29:
      		return "Chassis Manager";
    	case 30:
      		return "Host-based RAID controller";
    	case 31:
      		return "Storage Device Enclosure";
    	case 32:
      		return "Desktop";
    	case 33:
      		return "Laptop";
    	case 34:
     		return "Virtual Tape Library";
    	case 35:
      		return "Virtual Library System";
	}
}




