import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {
	
	
	def ssInstances = $smis.getInstancesByClass('HPEVA_StorageVolume');
	ssInstances.each{ssInst->
	def name = $smis.getProperty(ssInst,'ElementName');
	def status = $smis.getProperty(ssInst,'OperationalStatus')[0]==2?1:0;
	def block = $smis.getProperty(ssInst,'BlockSize');
	def number = $smis.getProperty(ssInst,'NumberOfBlocks');
	
	result.addRow(name, [
	'class','StorageVolume',
	'rs.ComponentOf','node',
	//'attr.storResCode',storResCode,
	'state.available_status', status]);
	}
}