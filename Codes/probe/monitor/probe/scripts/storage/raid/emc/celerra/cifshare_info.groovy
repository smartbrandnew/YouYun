/*!Action
 action.name=通过smis协议监测FC端口
 action.descr=通过smis协议监测FC端口
 action.protocols=smis
 monitor.output=EMCCELERRA-CIFSSHARE-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {

	def cifsShareInstances = EMCCelerraUtils.findCelerraCIFSShare($smis);
	cifsShareInstances.each{cifsShareInst->
	def cifsShareName = $smis.getProperty(cifsShareInst,'Name');
	def result = $result.create(cifsShareName);
	result.clazz = 'CIFSShare';
	result.rs.ComponentOf = 'node';
	
	result.state.available_status = $smis.getProperty(cifsShareInst,'OperationalStatus');
	result.attr.shareNameSeparator = $smis.getProperty(cifsShareInst,'PathNameSeparatorString');
	result.attr.shareDir = $smis.getProperty(cifsShareInst,'SharingDirectory');
	
	}
	

}




