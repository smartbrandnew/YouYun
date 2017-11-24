/*!Action
 action.name=通过snmp协议监测电源基本信息
 action.descr=通过snmp协议监测电源基本信息
 action.protocols=snmp
 monitor.output=powersupply-info
 monitor.priority=100
*/

executeMonitor();
return $result;

def executeMonitor() {
	def fcUtil = $script.use('fc_util');
	def table = $snmp.walk(fcUtil.SENSOR_ATTRS);
	def vender = fcUtil.getVender();
	for (row in table) {
		def sensorType = fcUtil.getSensorType(row[6].value.int);
		if (sensorType != 'POWER-SUPPLY') {
			continue;
		}
		
		def name = fcUtil.getSensorName(row[2].value.text);
		def state = fcUtil.getSensorStatus(row[3].value.int);
		
		def result = $result.create(name);
		result.clazz = 'PowerSupply';
		result.rs.componentOf = 'node';
		
		result.attr.productName = null;
		result.attr.partNum = null;
		result.attr.serial = row[4].value.text;
		result.attr.mfg = null;
		result.perf.IPMI_env.fan_speed = fcUtil.getSensorValue(vender, sensorType, row[5].value.text);
		result.state.available_status = ((state == 'OK' || state == 'WARNING') ? 1 : 0);
	}

}
