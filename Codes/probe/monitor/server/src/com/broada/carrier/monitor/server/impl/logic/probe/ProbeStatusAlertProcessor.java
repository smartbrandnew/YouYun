package com.broada.carrier.monitor.server.impl.logic.probe;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.impl.logic.alert.TrapForward;
import com.broada.snmp.BCSnmpAPI;
import com.broada.snmp.BCTrapV2PDU;

/**
 * 负责Probe状态改变时告警的处理
 * 
 * @author chenliang1
 * 
 */
public class ProbeStatusAlertProcessor {
	private static final Log logger = LogFactory.getLog(ProbeStatusAlertProcessor.class);
	public static final int NORMAL = 0;

	public static final int CRITICAL = 1;

	public static final int MAJOR = 2;

	public static final int MINOR = 3;

	public static final int WARNING = 4;

	public static void probeOffline(MonitorProbe probe) {
		sendAlarm(probe, false);
	}

	public static void probeOnline(MonitorProbe probe) {
		sendAlarm(probe, true);
	}

	/**
	 * 发送告警
	 * 
	 * @param probe
	 */
	protected static void sendAlarm(MonitorProbe probe, boolean online) {
		BCTrapV2PDU pdu = ProbeTrapUtil.probe2PDU(probe, online);
		if (logger.isInfoEnabled()) {
			logger.info("Probe[name=" + probe.getName() + ",code=" + probe.getCode() + "]因状态改变发送Trap告警。");
		}
		TrapForward.getDefault().send(pdu);
	}
}

class ProbeTrapUtil {
	public static String PROBEOID = ".1.3.6.1.4.1.22014.1.5.4.1.1";
	public static String PROBEOID_CODE = ".1.3.6.1.4.1.22014.1.3.4.1.2.1";
	public static String PROBEOID_NAME = ".1.3.6.1.4.1.22014.1.3.4.1.2.2";
	public static String PROBEOID_SRV_ADDR = ".1.3.6.1.4.1.22014.1.3.4.1.2.3";
	public static String PROBEOID_SRV_PORT = ".1.3.6.1.4.1.22014.1.3.4.1.2.4";
	public static String PROBEOID_PROBE_SYSTEM_TIME = ".1.3.6.1.4.1.22014.1.3.4.1.2.5";
	public static String PROBEOID_IP_STATUS = ".1.3.6.1.4.1.22014.1.3.4.1.2.6";
	public static String PROBEOID_ALARM_SEVERITY = ".1.3.6.1.4.1.22014.1.3.4.1.2.8";
	/**
	 * 日期格式
	 */
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Probe到PDU的转换
	 * 
	 * @param probe
	 * @return
	 */
	public static BCTrapV2PDU probe2PDU(MonitorProbe probe, boolean online) {
		String trapOid = PROBEOID;
		BCTrapV2PDU pdu = new BCTrapV2PDU(trapOid);
		pdu.addStringVarBind(PROBEOID_CODE, convertString(probe.getCode()));
		pdu.addStringVarBind(PROBEOID_NAME, convertString(probe.getName()));
		pdu.addStringVarBind(PROBEOID_SRV_ADDR, convertString(probe.getHost()));
		pdu.addVarBind(PROBEOID_SRV_PORT, Integer.toString(probe.getPort()), BCSnmpAPI.INTEGER);
		pdu.addStringVarBind(PROBEOID_PROBE_SYSTEM_TIME, format.format(new Date()));
		pdu.addVarBind(PROBEOID_IP_STATUS, online ? "1" : "0", BCSnmpAPI.INTEGER);
		int alarmSeverity;
		if (!online) {
			alarmSeverity = ProbeStatusAlertProcessor.MAJOR;
		} else {
			alarmSeverity = ProbeStatusAlertProcessor.NORMAL;
		}
		pdu.addVarBind(PROBEOID_ALARM_SEVERITY, Integer.toString(alarmSeverity), BCSnmpAPI.INTEGER);
		return pdu;
	}

	private static String convertString(String s) {
		return s == null ? "" : s;
	}
}
