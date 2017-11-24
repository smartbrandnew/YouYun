import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;


executeMonitor();


def executeMonitor() {

	
	
	def ssInstances = $smis.getInstancesByClass('HITACHI_StorageSystem');
	ssInstances.each{ssInst->
	println("测试开始1！");
	println(ssInst);
	def ssOP = ssInst.getObjectPath();
	def ssname = $smis.getProperty(ssInst,'ElementName');
	
	def fanInstances = $smis.getAssociatedInstances(ssOP, "HITACHI_StorageSystemDeviceFan", "HITACHI_Fan", "GroupComponent", "PartComponent");
	fanInstances.each{fanInst->
	println("测试开始2！");
	println(fanInst);
	def fanname = $smis.getProperty(fanInst,'ElementName');
	def snname = $smis.getProperty(fanInst,'SystemName');
	def status = $smis.getProperty(fanInst,'OperationalStatus')[0]==2?1:0;
	
	result.addRow(fanname+"-"+snname, [
	'class','FAN',
	'rs.ComponentOf','node',
	'attr.storResCode',fanname+"/"+snname,
	'state.available_status', status]);
	}
	}
	}