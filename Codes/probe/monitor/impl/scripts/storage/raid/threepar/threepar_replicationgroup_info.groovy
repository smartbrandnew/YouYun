/*!Action
 action.name=通过smi-s协议监测THREEPAR折叠组信息
 action.descr=通过smi-s协议THREEPAR折叠组信息
 action.protocols=smis
 monitor.output=THREEPAR-REPLICATIONGROUPS-INFO
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
		def replicationGroups = ThreePARUtils.findArrayReplicationGroups($smis, cop);
		replicationGroups.each{replicationGroup->
		
			String rgCaption = replicationGroup.getProperty("Caption");
	        String rgElmName = replicationGroup.getProperty("ElementName");
	        String rgPersistent = replicationGroup.getProperty("Persistent");
	        String rgDeleteOnEmptyElement = replicationGroup.getProperty("DeleteOnEmptyElement");
	        String rgDeleteOnUnassociated = replicationGroup.getProperty("DeleteOnUnassociated");
	        String rgDomain = replicationGroup.getProperty("Domain");
		
	        def result = $result.create(rgElmName);
	        result.clazz="RepliGroup";
	        resulot.attr.caption=rgCaption;	
	        resulot.attr.persistent=rgPersistent;	
	        resulot.attr.deleteOnEmptyElement=rgDeleteOnEmptyElement;	
	        resulot.attr.deleteOnUnassociated=rgDeleteOnUnassociated;	
	        resulot.attr.domain=rgDomain;	
		}
	}
}
