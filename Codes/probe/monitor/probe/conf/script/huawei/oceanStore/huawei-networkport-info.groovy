import com.broada.cid.action.impl.action.context.NumberContext;
import java.lang.*;


executeMonitor();


def executeMonitor() {
	def epInstances = $smis.getInstancesByClass('Nex_EthernetPort');

	epInstances.each{epInst->
		def epName = $smis.getProperty(epInst,'ElementName');
		def permanentAddr = $smis.getProperty(epInst,'PermanentAddress');
		def status = $smis.getProperty(epInst,'OperationalStatus')[0];
		def netWorkAddr = $smis.getProperty(epInst,'NetworkAddresses')[0];
		def fullDuplex = $smis.getProperty(epInst,'FullDuplex')==true?'全双工':'半双工';
		def speed = $smis.getProperty(epInst,'Speed');

		
		result.addRow(epName+'_'+permanentAddr, [
			'class','NetDevPort',
			'rs.ComponentOf','node',
			'state.available_status', status==2?1:0,
			'attr.permanentAddr',permanentAddr,
			'attr.macAddr',netWorkAddr,
			'attr.communicationMode',fullDuplex,
			'attr.speed',speed
			]);
		
	}
}