/*!Action
 action.name=通过smi-s协议监测物理基本信息
 action.descr=通过smi-s协议监测物理基本信息
 action.protocols=smis
 monitor.output=HPMSA-CONTROLLER-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.SMIArrayUtils;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.carrier.monitor.impl.storage.HuaWeiOSUtils; 
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = HPMSAUtils.findArrayInstances($smis);
	def ctrlInstances = HPMSAUtils.findArrayControllerInstances($smis);
	def productorInstances = HPMSAUtils.findProductInstances($smis);
	def ppInstances = HPMSAUtils.findPhysicalPackageInstances($smis);
	arrayInstances.each{arrayInst->
		ctrlInstances.each{ctrlInst->
		def name = $smis.getProperty(ctrlInst,'Name');
		def ctrlName = $smis.getProperty(ctrlInst,'Name');
		def result = $result.create(ctrlName);
		result.clazz = 'RAIDContrller';
		result.rs.ComponentOf = 'node';
		result.state.available_status = $smis.getProperty(arrayInst,'OperationalStatus');
		result.attr.name =  $smis.getProperty(arrayInst,'Name');
		}
		productorInstances.each{productorInst->
		def identifyingNumber = $smis.getProperty(productorInst,'IdentifyingNumber');
		if(identifyingNumber.indexOf(name)>-1){
		result.attr.mfg =  $smis.getProperty(productorInst,'Vendor');
		result.attr.descr =  $smis.getProperty(productorInst,'Description');
		result.attr.otherInfo =  $smis.getProperty(productorInst,'OtherIdentifyingInfo');
		}
		}
		ppInstances.each{ppInst->
		def name_ = $smis.getProperty(ppInst,'Name');
		if(name_.indexOf(name)>-1){
		//result.attr.version =  $smis.getProperty(ppInst,'Version');
		}
		}
	};
	
	
}


