package uyun.bat.monitor.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import uyun.bat.common.config.Config;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.core.entity.CheckContext;
import uyun.bat.monitor.core.entity.MonitorParam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public abstract class ArbiterSenderUtil {
	private static final Logger logger = LoggerFactory.getLogger(ArbiterSenderUtil.class);

	private static final int DEFAULT_VERSION = SnmpConstants.version2c;
	private static final long DEFAULT_TIMEOUT = 3 * 1000L;
	private static final int DEFAULT_RETRY = 3;
	private static Snmp snmp = null;
	private static CommunityTarget target = null;

	static {
		String targetAddress = null;
		try {
			targetAddress = (String) Config.getInstance().get("snmp.address");
			if (targetAddress != null && targetAddress.length() != 0) {
				target = createTarget4Trap(targetAddress);
				TransportMapping transport = new DefaultUdpTransportMapping();
				snmp = new Snmp(transport);
				transport.listen();
				logger.info("Enable snmptrap，address：" + targetAddress);
			} else {
				logger.info("Disable snmptrap");
			}
		} catch (Throwable e) {
			logger.warn("Enable snmptrap exception , Address:" + targetAddress, e);
		}
	}

	public static PDU generatePerformenceMetricAlertMsg(CheckContext context, String content)
			throws UnsupportedEncodingException {
		short severity = 0;
		Map<String, String> pm = context.getMonitorParam().getParamMap();
		PDU pdu = new PDU();
		switch (context.getMonitorState().getValue()) {
			case 8:
				severity = ArbiterConstants.EVENT_TYPE_ERROR;
				break;
			case 5:
				severity = ArbiterConstants.EVENT_TYPE_WARINING;
				break;
			case 2:
				severity = ArbiterConstants.EVENT_TYPE_OK;	//发送恢复事件
				break;
		}
		//判断发送
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID("1.3.6.1.4.1.22014.1.3.3.1.1.1.666.1")));
		String metric=pm.get(MonitorParam.METRIC_NAME);
		int hostAppType=ArbiterConstants.HOST_TYPE;
		String app=null;
		if (null!=metric){
			int index =metric.indexOf('.');
			if (index != -1) {
				app = metric.substring(0, index);
				if(!"system".equals(app)){
					hostAppType=ArbiterConstants.APP_TYPE;
				}
			}
		}

		if (context.getHostName() != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.1.0"), new OctetString(context.getHostName()
					.getBytes("GBK"))));

		if (context.getIp() != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.2.0"), new OctetString(context.getIp()
					.getBytes("GBK"))));

		//告警来源
		app=(null==app||"system".equals(app))?"":app+"-";
		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.3.0"),new OctetString((app+ArbiterConstants.CONFIG_NAME+context.getHostName()).getBytes("GBK"))));

		if (context.getValue() != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.4.0"), new OctetString(context.getValue()
					.getBytes("GBK"))));
		if (pm.get(MonitorParam.THRESHOLD) != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.5.0"), new OctetString(pm.get(
					MonitorParam.THRESHOLD).getBytes("GBK"))));

		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.6.0"), new OctetString(content.getBytes("GBK"))));
		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.7.0"), new Integer32(severity)));

		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.8.0"), new Integer32(hostAppType)));

		return pdu;
	}

	public static PDU generateHostOrAppUnableAlertMsg(CheckContext context, String content)
			throws UnsupportedEncodingException {
		short severity = 0;
		Map<String, String> pm = context.getMonitorParam().getParamMap();
		PDU pdu = new PDU();
		switch (context.getMonitorState().getValue()) {
		case 8:
			severity = ArbiterConstants.EVENT_TYPE_ERROR;
			break;
		case 5:
			severity = ArbiterConstants.EVENT_TYPE_WARINING;
			break;
		case 2:
			severity = ArbiterConstants.EVENT_TYPE_OK;	//发送恢复事件
			break;
		}
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID("1.3.6.1.4.1.22014.1.3.3.1.1.1.666.2")));
		if (context.getHostName() != null) {
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.1.0"), new OctetString(context.getHostName()
					.getBytes("GBK")))); // 主机名
		}
		if (context.getIp() != null){
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.2.0"), new OctetString(context.getIp()
					.getBytes("GBK")))); // ip地址
		}

		int hostAppType;
		String app=null;
		if (null!=pm.get(MonitorParam.APP)){
			app=pm.get(MonitorParam.APP);
			hostAppType=ArbiterConstants.APP_TYPE;
		}else{
			hostAppType=ArbiterConstants.HOST_TYPE;
		}

		if (app != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.3.0"), new OctetString(app.getBytes("GBK"))));

		if (pm.get(MonitorParam.DURATION) != null){
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.4.0"), new OctetString(pm.get(
					MonitorParam.DURATION).getBytes("GBK"))));
		}
		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.5.0"), new Integer32(severity)));
		if (content != null) {
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.6.0"), new OctetString(content.getBytes("GBK"))));
		}
		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.7.0"),new Integer32(hostAppType)));

		app=null==app?"":app+"-";
		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.8.0"),new OctetString((app+ArbiterConstants.CONFIG_NAME+context.getHostName()).getBytes("GBK"))));

		return pdu;
	}

	public static PDU generateAbnormalEventAlertMsg(CheckContext context, String content)
			throws UnsupportedEncodingException {
		short severity = 0;
		PDU pdu = new PDU();
		Map<String, String> pm = context.getMonitorParam().getParamMap();
		pdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID("1.3.6.1.4.1.22014.1.3.3.1.1.1.666.3")));
		switch (context.getMonitorState().getValue()) {
			case 8:
				severity = ArbiterConstants.EVENT_TYPE_ERROR;
				break;
			case 5:
				severity = ArbiterConstants.EVENT_TYPE_WARINING;
				break;
			case 2:
				severity = ArbiterConstants.EVENT_TYPE_OK;	//发送恢复事件
				break;
		}
		if (context.getHostName() != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.1.0"), new OctetString(context.getHostName()
					.getBytes("GBK"))));
		if (context.getIp() != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.2.0"), new OctetString(context.getIp()
					.getBytes("GBK"))));
		// 临时取一下eventtitle
		Event event = context.getEvent();
		String eventTitle = event.getMsgTitle() != null && event.getMsgTitle().length() > 0 ? event.getMsgTitle() : (pm
				.get(MonitorParam.KEY_WORDS) + "监测");

		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.3.0"),new OctetString((ArbiterConstants.CONFIG_NAME+context.getHostName()).getBytes("GBK"))));

		String eventContent = event.getMsgContent();
		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.4.0"), new OctetString(eventContent
				.getBytes("GBK"))));
		if (pm.get(MonitorParam.DURATION) != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.5.0"), new OctetString(pm.get(
					MonitorParam.DURATION).getBytes("GBK"))));
		if (pm.get(MonitorParam.THRESHOLD) != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.6.0"), new OctetString(pm.get(
					MonitorParam.THRESHOLD).getBytes("GBK"))));
		if (pm.get(MonitorParam.KEY_WORDS) != null)
			pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.7.0"), new OctetString(pm.get(
					MonitorParam.KEY_WORDS).getBytes("GBK"))));
		pdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.22014.1.3.3.1.2.8.0"), new Integer32(severity)));

		return pdu;
	}

	public static void sendPDU(PDU pdu) throws IOException {
		// 向Agent发送PDU
		pdu.setType(PDU.TRAP);
		snmp.send(pdu, target);
	}

	public static CommunityTarget createTarget4Trap(String address) throws UnsupportedEncodingException {
		CommunityTarget target = new CommunityTarget();
		target.setAddress(GenericAddress.parse(address));
		target.setCommunity(new OctetString("public"));
		target.setVersion(DEFAULT_VERSION);
		target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
		target.setRetries(DEFAULT_RETRY);
		return target;
	}

}
