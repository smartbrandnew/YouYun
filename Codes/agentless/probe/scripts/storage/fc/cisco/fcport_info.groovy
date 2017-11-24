/*!Action
 action.name=通过snmp协议监测FC端口
 action.descr=通过snmp协议监测FC端口
 action.protocols=snmp
 monitor.output=FCPort-info
 monitor.priority=90
*/

executeMonitor();
return $result;

def executeMonitor() {
	def fcUtil = $script.use('fc_util');
	def table = $snmp.walk(fcUtil.CISCO_PORT_ATTRS);
	
	for (row in table) {
		def name = row[9].value.text;
		
		def result = $result.create(name);
		result.clazz = 'FCPort';
		result.rs.componentOf = 'node';
		result.attr.portIdx = row[0].value.int;
		result.attr.portType = fcUtil.getPortType(row[2].value.int);
		result.attr.speed = row[4].value.int;
		result.attr.portAlias = row[11].value.text;
		result.attr.transmitterType = getPortTransmitterType(row[14].value.int);
		result.attr.moduleType = null;
		result.attr.fiberChannelID = row[13].value.int;
		result.attr.vsanID = row[12].value.int;
		def protState = getPortAdminStatus(row[5].value.int);
		result.state.available_status = (protState == 'up' ? 1 : 0);

	}
}


def getPortTransmitterType(def index) {
	switch(index) {
		case 1:
			return 'unknown';
		case 2:
			return 'longWaveLaser';
		case 3:
			return 'shortWaveLaser';
		case 4:
			return 'longWaveLaserCostReduced';
		case 5:
			return 'electrical';
		case 6:
			return 'tenGigBaseSr';
		case 7:
			return 'tenGigBaseLr';
		case 8:
			return 'tenGigBaseEr';
		case 9:
			return 'tenGigBaseLx4';
		case 10:
			return 'tenGigBaseSw';
		case 11:
			return 'tenGigBaseLw';
		case 12:
			return 'tenGigBaseEw';
	}
}

def getPortAdminStatus(def index) {
	switch(index) {
		case 1:
			return 'up';
		case 2:
			return 'down';
		case 3:
			return 'testing';

	}
}