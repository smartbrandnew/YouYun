/*!Action
 action.name=通过smis协议监测FC端口
 action.descr=通过smis协议监测FC端口
 action.protocols=smis
 monitor.output=EMCCELERRA-NFSSHARE-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {


	def nfsShareInstances = EMCCelerraUtils.findCelerraNFSShare($smis);
	nfsShareInstances.each{nfsShareInst->
	def nfsShareName = $smis.getProperty(nfsShareInst,'Name');
	def result = $result.create(nfsShareName);
	result.clazz = 'NFSShare';
	result.rs.ComponentOf = 'node';
	
	result.state.available_status = $smis.getProperty(nfsShareInst,'OperationalStatus');
	result.attr.shareNameSeparator = $smis.getProperty(nfsShareInst,'PathNameSeparatorString');
	result.attr.shareDir = $smis.getProperty(nfsShareInst,'SharingDirectory');
	
	}
	

}




