import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;


executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	ssInstances.each{ssInst->
	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'ElementName');
	
	def psInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_StorageSystemDevicePowerSupply", "HITACHI_PowerSupply", "GroupComponent", "PartComponent");
	psInstances.each{psInst->
	def psname = $smis.getProperty(psInst,'ElementName');
	def snname = $smis.getProperty(psInst,'SystemName');
	def status = $smis.getProperty(psInst,'OperationalStatus')[0]==2?1:0;
	
	result.addRow(psname+"-"+snname, [
	'class','PowerSupply',
	'rs.ComponentOf','node',
	'attr.storResCode',psname+"/"+snname,
	'state.available_status', status]);
	}
	}
	}