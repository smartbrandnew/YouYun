/*!Action
 action.name=通过snmp协议监测FC端口
 action.descr=通过snmp协议监测FC端口
 action.protocols=snmp
 monitor.output=FCPort-io
 monitor.priority=100
*/

executeMonitor();
return $result;

def executeMonitor() {
	def fcUtil = $script.use('fc_util');
	def portStatsTable = $snmp.walk(fcUtil.PORT_STATS);
	for (row in protStatsTable) {
		def result = $result.create(name);
		result.clazz = 'FCPort';
		result.rs.componentOf = 'node';
		result.perf.port-speed.in_discard_pkts_rate = row[4].value.int;
		result.perf.port-speed.in_err_pkts_rate = row[5].value.int;
		result.perf.port-speed.nonucast_in_frames_rate = row[3].value.int;
		result.perf.port-speed.nonucast_out_frames_rate = row[9].value.int;
		result.perf.port-speed.out_discard_pkts_rate = row[10].value.int;
		result.perf.port-speed.out_err_pkts_rate = row[11].value.int;
		result.perf.port-speed.ucast_in_pkts_rate = row[2].value.int;
		result.perf.port-speed.ucast_out_pkts_rate = row[8].value.int;
		result.perf.port-speed.input_rate = row[1].value.int;
		result.perf.port-speed.output_rate = row[7].value.int;
		result.perf.port-speed.mcast_in_pkts_rate = row[14].value.int;
		result.perf.port-speed.mcast_out_pkts_rate = row[16].value.int;
		result.perf.port-speed.bcast_in_pkts_rate = row[15].value.int
		result.perf.port-speed.bcast_out_pkts_rate = row[17].value.int
	}
}
