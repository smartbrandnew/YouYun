/*!Action
 action.name=通过smi-s协议监测磁盘阵列
 action.descr=通过smi-s协议监测磁盘阵列
 action.protocols=smis
 monitor.output=HPMSA-DISKARRAY-INFO
 monitor.priority=100
*/

import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HPMSAUtils;

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = HPMSAUtils.findArrayInstances($smis);
	def scInstances = HPMSAUtils.findStorageCapabilityInstances($smis);
	def productorInstances = HPMSAUtils.findProductInstances($smis);
	def ppInstances = HPMSAUtils.findPhysicalPackageInstances($smis);
	arrayInstances.each{arrayInst->
		def arrayName = $smis.getProperty(arrayInst,'ElementName');
		def result = $result.create(arrayName);
		result.clazz = 'HPMSA';
		result.rs.ComponentOf = 'node';
		result.state.available_status = $smis.getProperty(arrayInst,'OperationalStatus');
		result.attr.name =  $smis.getProperty(arrayInst,'Name');
		scInstances.each{scInst->
		def identifyingNumber = $smis.getProperty(scInst,'ElementName');
		if(identifyingNumber.indexOf(arrayName)>-1){
		result.attr.raidLev =  $smis.getProperty(scInst,'SupportedRaidValues');
		}
		}
		productorInstances.each{productorInst->
		def identifyingNumber = $smis.getProperty(productorInst,'IdentifyingNumber');
		if(identifyingNumber.indexOf(arrayName)>-1){
		result.attr.mfg =  $smis.getProperty(productorInst,'Vendor');
		result.attr.descript =  $smis.getProperty(productorInst,'Description');
		result.attr.otherInfo =  $smis.getProperty(productorInst,'OtherIdentifyingInfo');
		}
		}
		ppInstances.each{ppInst->
		def name_ = $smis.getProperty(ppInst,'Name');
		if(name_.indexOf(arrayName)>-1){
		result.attr.deviceVersion =  $smis.getProperty(ppInst,'Version');
		}
		}
	};

}
