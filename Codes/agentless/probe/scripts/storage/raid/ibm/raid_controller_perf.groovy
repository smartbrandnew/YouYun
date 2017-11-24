/*!Action
 action.name=通过smi-s协议监测磁盘阵列控制器
 action.descr=通过smi-s协议监测磁盘阵列控制器
 action.protocols=smis
 monitor.output=IBMSVC-RAID-CONTROLLER-PERF
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocolType;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each {clusterInst->
		def cpuInfos = monitorCpu();
		def clusterCOP = clusterInst.getObjectPath();
		def nodeInsts = IBMSVCUtils.findNodeInstanceByCluster($smis, clusterCOP);
		nodeInsts.each {nodeInst->
			def nodeCOP = nodeInst.getObjectPath();
			def nodeName = $smis.getProperty(nodeInst, 'ElementName');
			
			def result = $result.create(nodeName);
			result.clazz = 'RAIDController';
			result.rs.componentOf = 'node';
			
			def cpuUseage = null;
			if (cpuInfos != null) {
				for (cpuinfo in cpuInfos) {
					if (cpuInfo.get(1) == nodeName) {
						result.perf."cpu-use".cpu_usage = cpuInfo.get(3);
						break;
					}
				}		
			}	
			def status = svcUtil.getOperationalStatus($smis.getProperty(nodeInst, 'OperationalStatus')[0]);
			result.state.available_status = (status == 'OK' ? 1 : 0);
		}
	}
	
}

def monitorCpu() {
	def props = [
		'ip' : $smis.params.ip,
		'port' : CcliProtocolType.ssh.port,
		'username': $smis.params.user,
		'password' : $smis.params.password,
		'protocol' : CcliProtocolType.ssh,
		'commandPrompt' : '>'
	];
	CcliSession cli = new CcliSession(new CcliProtocol(new Protocol('ccli', props)));
	try {
		cli.doConnect();
		def response = cli.execute('svcinfo lsnodestats -filtervalue stat_name=cpu_pc');
		
		def result = [];
		$text.splitLine(response).each {row->
			if (row.contains("stat_current")) {
				return;
			}
			result.add(row.split("\\s+"));
		}
		return result;
	} catch (e) {
		println e.getMessage(); 
	} finally {
		if (cli.isConnected()) {
			cli.disconnect();
		}
	}
}