/*!Action
 action.name=通过smi-s协议监测磁盘阵列
 action.descr=通过smi-s协议监测磁盘阵列
 action.protocols=smis
 monitor.output=raid-perf
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocolType;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

//executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		
		def clusterName = $smis.getProperty(clusterInst, 'ElementName');
		if (clusterName == null) {
			return;
		}
		
		def cpuSum = 0;
		def cpuInfos = monitorCpu();
			if (cpuInfos == null || cpuInfos.size() == 0) {
				cpuInfos.each {cpuInfo->
				cpuSum += cpuInfo.get(3);
			}
		}
		
		def freeMemory = 0;
		def totalMemory = 0;
		
		def ioGroupInsts = IBMSVCUtils.findIOGroupByCluster($smis, clusterCOP)
		ioGroupInsts.each {ioGroupInst->
			freeMemory += $smis.getProperty(ioGroupInst, 'RAIDFreeMemory');
			totalMemory += $smis.getProperty(ioGroupInst, 'RAIDTotalMemory');
		}
		
		def result = $result.create(clusterName);
		result.perf."cpu-use".cpu_usage = cpuInfos == null ? 0 : cpuSum/cpuInfos.size()
		result.perf."ram-use".mem_usage = ((totalMemory - freeMemory) * 100)/totalMemory;
		result.perf."ram-use".mem_used = $unit.b2GB(totalMemory - freeMemory);
		def status = svcUtil.getOperationalStatus($smis.getProperty(clusterInst, 'OperationalStatus')[0]);
		result.state.available_status = (status == 'OK' ? 1 : 0);			
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
		System.out.println( e.getMessage()); 
	} finally {
		if (cli.isConnected()) {
			cli.disconnect();
		}
	}
}