/*!Action
 action.name=通过snmp协议监测CISCO光纤交换机基本信息
 action.descr=通过snmp协议监测CISCO光纤交换机基本信息
 action.protocols=snmp
 monitor.output=fc-info
 monitor.priority=90
*/

executeMonitor();
return $result;

def executeMonitor() {
	def fcUtil = $script.use('fc_util');
	def bases = $snmp.get(CISCO_FC_ATTRS);
	
	String productDescr = bases[0].value.text;
	String sysName = bases[5].value.text;
	def productInfo = productDescr.split();
	String switchDisName = null;
	if ((sysName != null) && (!sysName.trim().equals("")) && (!sysName.trim().equals("Not Available"))) {
		switchDisName = sysName;
	}
	
	def result = $result.create(switchDisName);
	result.attr.vender = 'cisco';
	result.attr.readonlyCommunity = $snmp.params['community'];
	result.attr.devNetLev = 3;
	result.attr.ipAddr = $snmp.params['ip'];
	result.attr.snmpSupport = 'yes';
	result.attr.maintenContact =  bases[1].value.text;
	result.attr.brand = productInfo[0];
	result.attr.deviceModel = productInfo[1];
	result.attr.fwOSName = productInfo[2];
	result.attr.areaCode = bases[4].value.text;
	
	if ($snmp.get('.1.3.6.1.3.94.1.1.0') != null) {
		def table = $snmp.walk(fcUtil.FC_ATTRS);
		for (fcRow in table) {
			def type = fcUtil.getSwitchType(fcRow[0].value.int);
			if (!(type == 'switch' || type == 'gateway' || type == 'converter' || type == 'module')) {
				println '跳过不需要采集的类型';
				continue;
			}
			
			result.attr.tunnelProtocol = '';
			result.attr.stackPortNum = '';
			
			def fiberName = $snmp.get('.1.3.6.1.2.1.75.1.1.1.0');
			if (fiberName != null) {
				fiberName = $snmp.get('.1.3.6.1.3.42.2.1.1.1.0');
			} 
			result.attr.fiberName = fiberName;
			result.attr.areaCode = '';
			result.attr.domainID = fcRow[10].value.text;
			result.attr.portNum= fcRow[3].value.text;
			result.attr.slotNum = '';
			result.attr.usedSlotNum = '';
			result.attr.fwOSName = fcUtil.getFirmwareVersion();
			result.attr.runningConfig = '';
			
			result.attr.sequenceNum = fcRow[7].value.text;
			result.attr.maintenContact = fcRow[22].value.text;
			
			def state = fcUtil.getSwitchAdminStatus(fcRow[4].value.int);
			result.state.available_status = (state == 'ONLINE' ? 1 : 0);
		}
	}
}

static final CISCO_FC_ATTRS = [".1.3.6.1.2.1.1.1.0", ".1.3.6.1.2.1.1.4.0", ".1.3.6.1.2.1.1.6.0", ".1.3.6.1.4.1.9.9.289.1.1.1.0", ".1.3.6.1.4.1.9.9.294.1.1.6.0", ".1.3.6.1.2.1.1.5.0"];
