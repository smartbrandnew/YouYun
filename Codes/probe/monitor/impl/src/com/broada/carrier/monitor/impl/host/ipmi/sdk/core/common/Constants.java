package com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common;

/**
 * 全局常量
 * 
 * @author pippo 
 * Create By 2014-5-12 下午5:41:06
 */
public class Constants {
	//以下为底盘信息相关
	public static final String SYSTEM_POWER = "System Power" ;

	public static final String POWER_OVERLOAD = "Power Overload" ;

	public static final String POWER_INTERLOCK = "Power Interlock" ;
	
	public static final String MAIN_POWER_FAULT = "Main Power Fault" ;
	
	public static final String POWER_CONTROL_FAULT = "Power Control Fault" ;
	
	public static final String CHASSIS_INTRUSION = "Chassis Intrusion" ;

	public static final String PANEL_LOCKOUT = "Front-Panel Lockout" ;
	
	public static final String DRIVE_FAULT = "Drive Fault" ;
	
	public static final String COOLING_FAULT = "PCooling/Fan Fault" ;
	
	public static final String CONF_PATH = "conf.path" ;
	
	public static final String IPMITOOL_PATH = "ipmi.ipmitool.path" ;
	
	public static final String WORK_DIR = "user.dir" ;
	//以下为扩展配置相关
	public static final String IPMI_EXTEND_PATH = "ipmi.extend.path" ;
	
	public static final String ENTITY_PROCESS_CODE = "ipmi.entity.process.code" ;

	public static final String ENTITY_BOARD_CODE = "ipmi.entity.board.code" ;

	public static final String ENTITY_MEMORY_CODE = "ipmi.entity.memory.code" ;
	
	public static final String ENTITY_POWER_CODE = "ipmi.entity.power.code" ;
	
	public static final String BASIC_SEVER_NAME = "服务器" ;
	//以下为网络信息相关
	public static final String CHANNEL_MEDIUM_TYPE = "Channel Medium Type" ;
	
	public static final String CHANNEL_PROTOCOL_TYPE = "Channel Protocol Type" ;

	public static final String SESSION_SUPPORT = "Session Support" ;

	public static final String ACTIVE_SESSION_COUNT = "Active Session Count" ;
	
	public static final String PROTOCOL_VENDOR_ID = "Protocol Vendor ID" ;
	//电源判断标识
	public static final String SDR_CODE_POWER = "power" ;
	public static final String SDR_CODE_SUPPLY = "supply" ;
	public static final String SDR_CODE_PS = "ps" ;
	
	public static final String IPMI_EXE_TIMEOUT = "ipmi.probe.timeout" ;

}
