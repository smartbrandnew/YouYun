/*!Action
 action.name=通过snmp协议监测光纤交换机性能数据
 action.descr=通过snmp协议监测光纤交换机性能数据
 action.protocols=snmp
 monitor.output=fc-perf
 monitor.priority=100
*/

executeMonitor();
return $result;

def executeMonitor() {
	def fcUtil = $script.use('fc_util');
	def table = $snmp.walk(FC_ATTRS);
	
	for (fcRow in table) {
		println fcRow[0].value
		
		def type = fcUtil.getSwitchType(fcRow[0].value.int);
		if (!(type == 'switch' || type == 'gateway' || type == 'converter' || type == 'module')) {
			println '跳过不需要采集的类型';
			continue;
		}
		
		def result = $result.create(fcRow[1].value.text);
		
		def model = fcUtil.getVender();
		
		result.perf.device-net-state.icmp_response_time = $snmp.get('.1.3.6.1.2.1.5.24').value.int;
		def sensorTable = $snmp.walk(fcUtil.SENSOR_ATTRS);
		
		for (sensorRow in sensorTable) {
			def sensorType = fcUtil.getSensorType(row[6].value.int);
			def sensorMessage = sensorRow[5].value.text;
			
			def state = fcUtil.getSensorStatus(row[3].value.int);
			if (state == 'OK' || state == 'WARNING') {
				def sensorValue = fcUtil.getSensorValue(model, sensorType, sensorMessage);
				if (sensorType == 'FAN') {
					result.perf.device-env.device_fan_speed = sensorValue;
				} else if (sensorType == 'POWER-SUPPLY') {
					result.perf.device-env.device_current = sensorValue;
					//result.perf.device-env.device_voltage = sensorValue;
				} else if (sensorType == 'BOARD') {
					result.perf.device-env.device_temp = sensorValue;
				}
			}
		}
		

		if (model == 'Brocade') {
			result.perf.cpu-use.cpu_usage = $snmp.get('.1.3.6.1.4.1.1588.2.1.1.1.26.1');
			result.perf.ram-use.mem_usage = $snmp.get('.1.3.6.1.4.1.1588.2.1.1.1.26.6');
		}
		
		result.perf.cpu-use.cpu_temp = null;
		//result.perf.cpu-use.cpu_usage = null;
		//result.perf.ram-use.mem_usage = null;
		result.perf.ram-use.mem_used = null;
	}
}
