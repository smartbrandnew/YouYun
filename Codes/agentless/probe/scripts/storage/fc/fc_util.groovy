/*!Action
 action.name=光纤交换机工具类
 action.descr=光纤交换机工具类
 action.protocols=snmp
*/

def getSwitchOperStatus(def status) {
	switch (status) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "UNUSED";
		case 3:
			return "OK";
		case 4:
			return "WARNING";
		case 5:
			return "FAILED";
	}
	return "Not Available";
}

def getSwitchAdminStatus(def status) {
	switch (status) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "ONLINE";
		case 3:
			return "OFFLINE";
	}
	return "Not Available";
}

def getSwitchType(def type) {
	switch (type) {
		case 1:
			return 'unknown';
		case 2:
			return 'other';
		case 3:
			return 'hub';
		case 4:
			return 'switch';
		case 5:
			return 'gateway';
		case 6:
			return 'converter';
		case 7:
			return 'hba';
		case 8:
			return 'proxy-agent';
		case 9:
			return 'storage-device';
		case 10:
			return 'host';
		case 11:
			return 'storage-subsystem';
		case 12:
			return 'module';
		case 13:
			return 'swdriver';
		case 14:
			return 'storage-access-device';
		case 15:
			return 'wdm';
		case 16:
			return 'ups';
	}
	
	return "Not Available";
}

def getPortOperStatus(def status) {
	switch (status) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "UNUSED";
		case 3:
			return "READY";
		case 4:
			return "WARNING";
		case 5:
			return "FAILURE";
		case 6:
			return "NOT PARTICIPATING";
		case 7:
			return "INITIALIZING";
		case 8:
			return "BYPASS";
		case 9:
			return "OLS";
	}
	return "Not Available";
}

def getPortAdminStatus(def status) {
	switch (status) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "ONLINE";
		case 3:
			return "OFFLINE";
		case 4:
			return "BYPASSED";
		case 5:
			return "DIAGNOSTICS";
	}
	return "Not Available";
}

def getPortHardwareState(def status) {
	switch (status) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "FAILED";
		case 3:
			return "BYPASSED";
		case 4:
			return "ACTIVE";
		case 5:
			return "LOOPBACK";
		case 6:
			return "TXFAULT";
		case 7:
			return "NO MEDIA";
		case 8:
			return "LINK DOWN";
	}
	return "Not Available";
}

def getSeverityFromStatus(String status) {
	switch (status) {
		case 1:
			return 4;
		case 2:
			return 4;
		case 3:
			return 5;
		case 4:
			return 4;
		case 5:
			return 1;
	}
	return 6;
}

def getPortSeverityFromStatus(def status) {
	switch (status) {
		case 1:
			return 4;
		case 2:
			return 4;
		case 3:
			return 5;
		case 4:
			return 4;
		case 5:
			return 1;
		case 6:
			return 4;
		case 7:
			return 4;
		case 8:
			return 4;
		case 9:
			return 4;
	}
	return 6;
}

def getPortSeverityFromAdminState(def status) {
	switch (status) {
		case 1:
			return 4;
		case 2:
			return 5;
		case 3:
			return 1;
		case 4:
			return 4;
		case 5:
			return 4;
	}
	return 6;
}

def isPrincipalSwitch(def principal) {
	switch (principal) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "NO";
		case 3:
			return "YES";
	}
	return "Not Available";
}

def getPortType(def type) {
	switch (type) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "OTHER";
		case 3:
			return "NOT PRESENT";
		case 7:
			return "FL_PORT";
		case 8:
			return "F_PORT";
		case 9:
			return "E_PORT";
		case 10:
			return "G_PORT";
	}
	return "Not Available";
}

def getPortTransmitterType(def type) {
	switch (type) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "OTHER";
		case 3:
			return "UNUSED";
		case 4:
			return "SHORTWAVE";
		case 5:
			return "LONGWAVE";
		case 6:
			return "COPPER";
		case 7:
			return "SCSI";
		case 8:
			return "LONGWAVE NOOFC";
		case 9:
			return "SHORTWAVE NOOFC";
		case 10:
			return "LONGWAVE LED";
		case 11:
			return "SSA";
	}
	return "Not Available";
}

def getPortModuleType(def moduleType) {
	switch (moduleType) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "OTHER";
		case 3:
			return "GBIC";
		case 4:
			return "EMBEDDED";
		case 5:
			return "GLM";
		case 6:
			return "GBIC SERIAL ID";
		case 7:
			return "GBIC NO SERIAL ID";
		case 8:
			return "GBIC NOT INSTALLED";
		case 9:
			return "SMALL FORM FACTOR";
	}
	return "Not Available";
}

def getSensorCharacteristic(int sensorCharacteristic) {
	switch (sensorCharacteristic) {
		case 1:
			return "Unknown";
		case 2:
			return "Other";
		case 3:
			return "Temperature";
		case 4:
			return "Pressure";
		case 5:
			return "EMF";
		case 6:
			return "Current";
		case 7:
			return "Airflow";
		case 8:
			return "Frequency";
		case 9:
			return "Power";
		case 10:
			return "Door";
	}
	return "Not Available";
}

def getSensorValue(String vendor, String sensorType,
		String sensorMessage) throws Exception {


	String value = "Not Available";
	if ((vendor != null) && (sensorMessage != null) && (sensorType != null)) {
		if ((vendor.equalsIgnoreCase("brocade"))
		|| (sensorMessage.indexOf(", value is") != -1)) {
			value = sensorMessage
					.substring(sensorMessage.lastIndexOf(" ") + 1);
		} else if ((vendor.equalsIgnoreCase("qlogic"))
		|| (sensorMessage.indexOf("degrees") != -1)) {
			if (sensorType.equalsIgnoreCase("board")) {
				try {
					if (!sensorMessage.equalsIgnoreCase("normal")) {
						value = sensorMessage.substring(0,
								sensorMessage.indexOf(" "));
					} else {
						value = sensorMessage;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	return value;
}

def getSensorName(String moName, String sensorType,
		String sensorIdx) throws Exception {
	StringBuffer sensorName = new StringBuffer(moName);
	if (sensorType.equalsIgnoreCase("board")) {
		sensorName.append("_TEMP_");
	} else if (sensorType.equalsIgnoreCase("fan")) {
		sensorName.append("_FAN_");
	} else if (sensorType.equalsIgnoreCase("power-supply")) {
		sensorName.append("_PS_");
	} else {
		sensorName.append("_SENSOR_");
	}
	sensorName.append(sensorIdx);
	return sensorName.toString();
}

def getSensorType(int sensorType) {
	switch (sensorType) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "OTHER";
		case 3:
			return "BATTERY";
		case 4:
			return "FAN";
		case 5:
			return "POWER-SUPPLY";
		case 6:
			return "TRANSMITTER";
		case 7:
			return "ENCLOSURE";
		case 8:
			return "BOARD";
		case 9:
			return "RECIEVER";
	}
	return "Not Available";
}

def getVendor(String model) {
	String vendor = "Brocade";
	if ((model.indexOf("connectrix") != -1) || (model.indexOf("cx") != -1)
	|| (model.indexOf("8b") != -1) || (model.indexOf("16b") != -1)
	|| (model.indexOf("32b") != -1) || (model.indexOf("16m") != -1)
	|| (model.indexOf("24m") != -1) || (model.indexOf("32m") != -1)
	|| (model.indexOf("ds") != -1)) {
		vendor = "EMC";
	} else if ((model.indexOf("gadzoox") != -1)
	|| (model.indexOf("slingshot") != -1)
	|| (model.indexOf("capellix") != -1)
	|| (model.indexOf("4210") != -1)
	|| (model.indexOf("4218") != -1)
	|| (model.indexOf("2000") != -1)
	|| (model.indexOf("3000") != -1)) {
		vendor = "Gadzoox";
	} else if (model.toLowerCase().indexOf("lightsand") != -1) {
		vendor = "LightSand";
	} else {
		vendor = model;
	}

	return vendor;
}

def getVender() {
	def vender = 'Brocade';
	String fcEosSysFirmwareVersion = $snmp.get('.1.3.6.1.4.1.289.2.1.1.2.1.3.0');
	String swSoftwareVersion = $snmp.get('.1.3.6.1.4.1.3412.2.3.1.1.1.1.1.1.3');
	if ($snmp.get('.1.3.6.1.4.1.289.2.1.1.2.1.3.0') != null && (fcEosSysFirmwareVersion != null) && (!fcEosSysFirmwareVersion.trim().equals("")) && (!fcEosSysFirmwareVersion.trim().equals("Not Available"))) {
		vender = 'McData';
	} else if ($snmp.get('.1.3.6.1.4.1.1663') != null) {
		vender = 'QLogic';
	} else if ($snmp.get('.1.3.6.1.4.1.3412.2.3.1.1.1.1.1.1.3') != null && (swSoftwareVersion != null) && (!swSoftwareVersion.trim().equals(""))) {
		vender = 'LightSand';
	} else {
		vender = $snmp.get('.1.3.6.1.3.94.1.6.1.21.0')
	}
}

def getEventType(String eventType) {
	switch (eventType) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "OTHER";
		case 3:
			return "STATUS";
		case 4:
			return "CONFIGURATION";
		case 5:
			return "TOPOLOGY";
	}
	return "Not Available";
}

def getSensorStatus(def status) {
	switch (status) {
		case 1:
			return "UNKNOWN";
		case 2:
			return "OTHER";
		case 3:
			return "OK";
		case 4:
			return "WARNING";
		case 5:
			return "FAILED";
	}
	return "Not Available";
}


def getNsPortType(int typeArg) {
	try {
		switch (typeArg) {
			case 0:
				return "Unknown";
			case 1:
				return "N_Port";
			case 2:
				return "NL_port";
		}
	} catch (Exception e) {
	}

	return "Not Available";
}


def getFirmwareVersion() {
	def firmwareVersion = null;
	def revs = $snmp.walk(REVS_ATTRS);
	revs.each {
		if (it[3].value.text == 'software') {
			firmwareVersion = it[2].value.text;
			break;
		}
	}
	
	String fcEosSysFirmwareVersion = $snmp.get('.1.3.6.1.4.1.289.2.1.1.2.1.3.0');
	String swSoftwareVersion = $snmp.get('.1.3.6.1.4.1.3412.2.3.1.1.1.1.1.1.3');
	if (fcEosSysFirmwareVersion != null && !fcEosSysFirmwareVersion.trim().empty) {
		firmwareVersion = fcEosSysFirmwareVersion;
	} else if (swSoftwareVersion != null && !swSoftwareVersion.trim().empty) {
		firmwareVersion = swSoftwareVersion;
	}
	return firmwareVersion;
}

static final PORT_ATTRS = [
	'.1.3.6.1.3.94.1.10.1.1',
	'.1.3.6.1.3.94.1.10.1.2',
	'.1.3.6.1.3.94.1.10.1.3',
	'.1.3.6.1.3.94.1.10.1.4',
	'.1.3.6.1.3.94.1.10.1.5',
	'.1.3.6.1.3.94.1.10.1.6',
	'.1.3.6.1.3.94.1.10.1.7',
	'.1.3.6.1.3.94.1.10.1.8',
	'.1.3.6.1.3.94.1.10.1.9',
	'.1.3.6.1.3.94.1.10.1.10',
	'.1.3.6.1.3.94.1.10.1.11',
	'.1.3.6.1.3.94.1.10.1.12',
	'.1.3.6.1.3.94.1.10.1.13',
	'.1.3.6.1.3.94.1.10.1.14',
	'.1.3.6.1.3.94.1.10.1.15',
	'.1.3.6.1.3.94.1.10.1.16',
	'.1.3.6.1.3.94.1.10.1.17'
	];

static final PORT_IO_STATS = [
	'.1.3.6.1.3.94.4.5.1.3', 
	'.1.3.6.1.3.94.4.5.1.4',
	'.1.3.6.1.3.94.4.5.1.5',
	'.1.3.6.1.3.94.4.5.1.6',  
	'.1.3.6.1.3.94.4.5.1.7',  
	'.1.3.6.1.3.94.4.5.1.29', 
	'.1.3.6.1.3.94.4.5.1.30', 
	'.1.3.6.1.3.94.4.5.1.31', 
	'.1.3.6.1.3.94.4.5.1.32', 
	];


static final SENSOR_ATTRS = [
	'1.3.6.1.3.94.1.8.1.1',
	'1.3.6.1.3.94.1.8.1.2',
	'1.3.6.1.3.94.1.8.1.3',
	'1.3.6.1.3.94.1.8.1.4',
	'1.3.6.1.3.94.1.8.1.5',
	'1.3.6.1.3.94.1.8.1.6',
	'1.3.6.1.3.94.1.8.1.7',
	'1.3.6.1.3.94.1.8.1.8'
	];


static final FC_ATTRS = [
	'.1.3.6.1.3.94.1.6.1.1',
	'.1.3.6.1.3.94.1.6.1.2',
	'.1.3.6.1.3.94.1.6.1.3',
	'.1.3.6.1.3.94.1.6.1.4',
	'.1.3.6.1.3.94.1.6.1.5',
	'.1.3.6.1.3.94.1.6.1.6',
	'.1.3.6.1.3.94.1.6.1.7',
	'.1.3.6.1.3.94.1.6.1.8',
	'.1.3.6.1.3.94.1.6.1.9',
	'.1.3.6.1.3.94.1.6.1.10',
	'.1.3.6.1.3.94.1.6.1.11',
	'.1.3.6.1.3.94.1.6.1.12',
	'.1.3.6.1.3.94.1.6.1.13',
	'.1.3.6.1.3.94.1.6.1.14',
	'.1.3.6.1.3.94.1.6.1.15',
	'.1.3.6.1.3.94.1.6.1.16',
	'.1.3.6.1.3.94.1.6.1.17',
	'.1.3.6.1.3.94.1.6.1.18',
	'.1.3.6.1.3.94.1.6.1.19',
	'.1.3.6.1.3.94.1.6.1.20',
	'.1.3.6.1.3.94.1.6.1.21',
	'.1.3.6.1.3.94.1.6.1.22',
	'.1.3.6.1.3.94.1.6.1.23'
];

static final REVS_ATTRS = [
	'.1.3.6.1.3.94.1.7.1.3',
	'.1.3.6.1.3.94.1.7.1.4'
];


static final CISCO_PORT_ATTRS = [
	'.1.3.6.1.2.1.2.2.1.1', 
	'.1.3.6.1.2.1.2.2.1.2', 
	'.1.3.6.1.2.1.2.2.1.3', 
	'.1.3.6.1.2.1.2.2.1.4', 
	'.1.3.6.1.2.1.2.2.1.5', 
	'.1.3.6.1.2.1.2.2.1.6', 
	'.1.3.6.1.2.1.2.2.1.7', 
	'.1.3.6.1.2.1.2.2.1.8', 
	'.1.3.6.1.2.1.2.2.1.9',
	'.1.3.6.1.2.1.31.1.1.1.1', 
	'.1.3.6.1.2.1.31.1.1.1.15', 
	'.1.3.6.1.2.1.31.1.1.1.18', 
	'.1.3.6.1.4.1.9.9.282.1.2.5.1.1', 
	'.1.3.6.1.4.1.9.9.289.1.1.2.1.6', 
	'.1.3.6.1.4.1.9.9.289.1.1.2.1.17',
	'.1.3.6.1.4.1.9.9.289.1.1.2.1.40', 
	];


static final PORT_STATS = [
	'.1.3.6.1.2.1.31.1.1.1.1', 
	'.1.3.6.1.2.1.2.2.1.10', 
	'.1.3.6.1.2.1.2.2.1.11', 
	'.1.3.6.1.2.1.2.2.1.12', 
	'.1.3.6.1.2.1.2.2.1.13', 
	'.1.3.6.1.2.1.2.2.1.14', 
	'.1.3.6.1.2.1.2.2.1.15', 
	'.1.3.6.1.2.1.2.2.1.16', 
	'.1.3.6.1.2.1.2.2.1.17', 
	'.1.3.6.1.2.1.2.2.1.18', 
	'.1.3.6.1.2.1.2.2.1.19', 
	'.1.3.6.1.2.1.2.2.1.20', 
	'.1.3.6.1.2.1.2.2.1.21', 
	'.1.3.6.1.2.1.2.2.1.22', 
	'.1.3.6.1.2.1.31.1.1.1.15', 
	'.1.3.6.1.2.1.31.1.1.1.3', 
	'.1.3.6.1.2.1.31.1.1.1.4', 
	'.1.3.6.1.2.1.31.1.1.1.5', 
	];