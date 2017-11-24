/*!Action
 action.name=通过snmp协议监测光纤交换机基本信息
 action.descr=通过snmp协议监测光纤交换机基本信息
 action.protocols=snmp
 monitor.output=fc-info
 monitor.priority=100
*/

executeMonitor();
return $result;

def executeMonitor() {
	def fcUtil = $script.use('fc_util');
	def table = $snmp.walk(fcUtil.FC_ATTRS);
	for (fcRow in table) {
		
		def type = fcUtil.getSwitchType(fcRow[0].value.int);
		if (!(type == 'switch' || type == 'gateway' || type == 'converter' || type == 'module')) {
			println '跳过不需要采集的类型';
			continue;
		}
		
		def result = $result.create(fcRow[19].value.text);
		
		result.attr.tunnelProtocol = '';
		result.attr.stackPortNum = '';
		
		def fiberName = '';
		if ($snmp.get('.1.3.6.1.2.1.75.1.1.1.0') != null) {
			fiberName = $snmp.get('.1.3.6.1.2.1.75.1.1.1.0');
		} else {
			fiberName = $snmp.get('.1.3.6.1.3.42.2.1.1.1.0');
		}
		result.attr.fiberName = fiberName;
		result.attr.areaCode = '';
		result.attr.domainID = fcRow[10].value.text;
		result.attr.portNum= fcRow[3].value.text;
		result.attr.slotNum = '';
		result.attr.usedSlotNum = '';
		result.attr.readonlyCommunity = $snmp.params['community'];
		result.attr.snmpSupport = 'yes';
		//result.attr.readwriteCommunity = $snmp.params['community'];
		result.attr.fwOSName = fcUtil.getFirmwareVersion();
		result.attr.runningConfig = '';
		result.attr.devNetLev = 3;
		result.attr.ipAddr = $snmp.params['ip'];
		result.attr.sequenceNum = fcRow[7].value.text;
		result.attr.vender = fcUtil.getVender();
		result.attr.maintenContact = fcRow[22].value.text;
		result.attr.brand = fcRow[6].value.text;
		def state = fcUtil.getSwitchAdminStatus(fcRow[4].value.int);
		result.state.available_status = (state == 'ONLINE' ? 1 : 0);
	}
}
