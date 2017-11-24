/*!Action
 action.name=通过smi-s协议监测磁盘阵列控制器
 action.descr=通过smi-s协议监测磁盘阵列控制器
 action.protocols=smis
 monitor.output=raid-controller-info
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.IBMSVCUtils; 

executeMonitor();
return $result;

def executeMonitor() {
	def svcUtil = $script.use('storage/raid/ibm/svc_util');
	def clusterInstances = IBMSVCUtils.findClusterInstances($smis);
	
	clusterInstances.each {clusterInst->
		def clusterCOP = clusterInst.getObjectPath();
		def controllerInstances = $smis.getAssociatedInstances(clusterCOP, 'IBMTSSVC_ConnectedBackendController', 'IBMTSSVC_BackendController', 'Antecedent', 'Dependent');
		controllerInstances.each{
			def COP = it.getObjectPath();
			def name = $smis.getProperty(it, 'ElementName');
			def result = $result.create(name);
			result.clazz = 'RAIDController';
			result.rs.componentOf = 'node';
			result.attr.mfg = 'IBM';
			result.attr.productName = '控制器';
			def status = svcUtil.getOperationalStatus($smis.getProperty(it, 'OperationalStatus')[0]);
			result.state.available_status = (status == 'OK' ? 1 : 0);
		};
		/*
		def nodeInsts = IBMSVCUtils.findNodeInstanceByCluster($smis, clusterCOP);
		nodeInsts.each {nodeInst->
			def nodeCOP = nodeInst.getObjectPath();
			def nodeName = $smis.getProperty(nodeInst, 'Name') + '_' + $smis.getProperty(nodeInst, 'ElementName');
			
			def result = $result.create(nodeName);
			result.clazz = 'RAIDController';
			result.rs.componentOf = 'node';
			//TODO
			result.attr.cacheSize = null;
			result.attr.physicMem = null;
			result.attr.virtualMem = null;
			def ethernetPortInstances = IBMSVCUtils.findEthernetPortByNode($smis, nodeCOP);
			def fcPortInstances = IBMSVCUtils.findFCPortByNode($smis, nodeCOP);
			
			result.attr.portNum = ethernetPortInstances.size() + fcPortInstances.size();
			def physcProps = new Properties();
			SMIArrayUtils.fetchPhysicalPackage($smis, nodeCOP, physcProps);
			result.attr.mfg = 'IBM';
			result.attr.productName = 'IBM';
			result.attr.serial = physcProps.SerialNumber;
			result.attr.partNum = physcProps.Model;
			def status = svcUtil.getOperationalStatus($smis.getProperty(nodeInst, 'OperationalStatus')[0]);
			result.state.available_status = (status == 'OK' ? 1 : 0);
		}
		*/
	}
	
}