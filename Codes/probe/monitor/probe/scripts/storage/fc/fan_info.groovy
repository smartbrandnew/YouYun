/*!Action
 action.name=通过snmp协议监测FAN
 action.descr=通过snmp协议监测风扇基本信息
 action.protocols=snmp
 monitor.output=fan-info
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
		if (sensorType != 'FAN') {
			continue;
		}
		
		def name = fcUtil.getSensorName(row[2].value.text);
		def state = fcUtil.getPortAdminStatus(row[3].value.text);
		
		def result = $result.create(name);
		result.clazz = 'FAN';
		result.rs.componentOf = 'node';
		
		result.attr.productName = null;
		result.attr.partNum = null;
		result.attr.serial = row[4].value.text;
		result.attr.mfg = null;
		result.perf.IPMI_env.fan_speed = fcUtil.getSensorValue(vender, sensorType, row[5].value.text);
		result.state.available_status = (state == 'ONLINE' ? 1 : 0);
	}

}
