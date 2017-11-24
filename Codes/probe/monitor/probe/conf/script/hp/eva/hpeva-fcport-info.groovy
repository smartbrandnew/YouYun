import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {
		
	def ssInstances = $smis.getInstancesByClass('HPEVA_FCPort');
	ssInstances.each{ssInst->
	def name = $smis.getProperty(ssInst,'ElementName');
	def status = $smis.getProperty(ssInst,'OperationalStatus')[0]==2?1:0;
	result.addRow(name, [
	'class','FCPort',
	'state.available_status', status]);
	}
}