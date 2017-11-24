/*!Action
 action.name=通过smis协议监测FC端口
 action.descr=通过smis协议监测FC端口
 action.protocols=smis
 monitor.output=HW-FILESYSTEM-INFO
 monitor.priority=100
*/
import com.broada.carrier.monitor.impl.storage.emc.EMCCelerraUtils;
import java.lang.*;

executeMonitor();
return $result;

def executeMonitor() {
	def arrayInstances = EMCCelerraUtils.findArrayInstances($smis);
	arrayInstances.each {arrayInst->
	def arrayCOP = arrayInst.getObjectPath();
	def computerInstances = EMCCelerraUtils.findComputerSystem($smis,arrayCOP);
	computerInstances.each{computerInst->
	def compCOP = computerInst.getObjectPath();
	def fileInstances = EMCCelerraUtils.findFileSystem($smis,compCOP);
	fileInstances.each{fileInst->
	def fileName = $smis.getProperty(fileInst,'Name');
	def result = $result.create(fileName);
	result.clazz = 'FileSystem';
	result.rs.ComponentOf = 'node';
	result.state.available_status = $smis.getProperty(fileInst,'OperationalStatus');
	result.perf.stor_use.capacity_unused = $smis.getProperty(fileInst,'AvailableSpace');
	
	result.attr.casePreserved = $smis.getProperty(fileInst,'CasePreserved');
	result.attr.caseSensitive = $smis.getProperty(fileInst,'CaseSensitive');
	result.attr.capacity = $unit.b2GB($smis.getProperty(fileInst,'FileSystemSize'));
	result.attr.maxFileNameLength = $smis.getProperty(fileInst,'MaxFileNameLength');
	result.attr.nameSeparator = $smis.getProperty(fileInst,'PathNameSeparatorString');
	}
	}
	}
	

}




