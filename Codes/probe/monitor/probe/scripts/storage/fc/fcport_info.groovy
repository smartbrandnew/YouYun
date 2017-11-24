/*!Action
 action.name=通过snmp协议监测FC端口
 action.descr=通过snmp协议监测FC端口
 action.protocols=snmp
 monitor.output=FCPort-info
 monitor.priority=100
*/

executeMonitor();
return $result;


def executeMonitor() {
	def fcUtil = $script.use('fc_util');
	def table = $snmp.walk(fcUtil.PORT_ATTRS);
	
	for (row in table) {
		def name = row[16].value.text;
		
		def result = $result.create(name);
		result.clazz = 'FCPort';
		result.rs.componentOf = 'node';
		result.attr.portIdx = row[1].value.int;
		result.attr.portType = fcUtil.getPortType(row[2].value.int);
		result.attr.speed = row[14].value.int;
		result.attr.portAlias = 'FC_PORT_' + name;
		result.attr.transmitterType = fcUtil.getPortTransmitterType(row[7].value.int);
		result.attr.moduleType = fcUtil.getPortModuleType(row[8].value.int);
		result.attr.fiberChannelID = row[10].value.text;
		result.attr.vsanID = '';
		def protState = fcUtil.getPortAdminStatus(row[5].value.int);
		result.state.available_status = (protState == 'ONLINE' ? 1 : 0);

	}
}




