import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;

executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	ssInstances.each{ssInst->
	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'ElementName');
	
	def batteryInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_StorageSystemDeviceBattery", "HITACHI_Battery", "GroupComponent", "PartComponent");
	batteryInstances.each{batteryInst->
	def name = $smis.getProperty(batteryInst,'ElementName');
	def snname = $smis.getProperty(batteryInst,'SystemName');
	def status = $smis.getProperty(batteryInst,'OperationalStatus')[0]==2?1:0;
	
	result.addRow(name+"-"+snname, [
	'class','FAN',
	'rs.ComponentOf','node',
	'attr.storResCode',name+"/"+snname,
	'state.available_status', status]);
	}
	}
	}