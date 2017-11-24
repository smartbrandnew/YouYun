package com.broada.carrier.monitor.server.impl.logic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.snmp4j.PDU;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import com.broada.carrier.monitor.server.impl.logic.alert.TrapForward;
import com.broada.component.utils.text.DateUtil;

public class AlertSender {
	private static final String OID_TRAP = "1.3.6.1.6.3.1.1.4.1.0";

	public static void sendMonitorState(String taskId, String taskName, String entityId, String entityName,
			String entityAddr,
			String value, String lastValue, Date time, String methodType, String methodCode, String message)
			throws IOException {
		PDU trap = new PDU();
		trap.setType(PDU.TRAP);

		addTrapVB(trap, OID_TRAP, new OID("1.3.6.1.4.1.22014.1.3.3.1.1.1.90"));

		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.7.0", DateUtil.format(time, DateUtil.PATTERN_YYYYMMDD_HHMMSS));

		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.63.0", entityId);
		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.3.0", entityAddr);
		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.10.0", entityName);

		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.1.0", taskName);
		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.6.0", value);
		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.6.1", lastValue);
		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.11.0", methodType);
		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.12.0", methodCode);
		addTrapVB(trap, "1.3.6.1.4.1.22014.1.3.3.1.2.13.0", message);

		TrapForward.getDefault().send(trap);
	}

	private static void addTrapVB(PDU pdu, String oid, Object value) {
		if (value == null)
			throw new IllegalArgumentException("值不能为空：" + oid);
		pdu.add(new VariableBinding(new OID(oid), getSnmpValue(value)));
	}

	private static Variable getSnmpValue(Object value) {
		if (value instanceof OID)
			return (OID) value;
		else if (value instanceof String)
			try {
				return new OctetString(value.toString().getBytes("GBK"));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalArgumentException("编码转化出错：" + value);
			}
		else if (value instanceof Integer)
			return new Integer32((Integer) value);
		else
			throw new IllegalArgumentException("不支持的值类型：" + value);
	}
}
