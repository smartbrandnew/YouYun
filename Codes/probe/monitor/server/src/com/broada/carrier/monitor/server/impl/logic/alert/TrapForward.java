package com.broada.carrier.monitor.server.impl.logic.alert;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;

import com.broada.carrier.monitor.method.snmp.SnmpVersion;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.snmp.SnmpSender;

public class TrapForward {
	private static final Logger logger = LoggerFactory.getLogger(TrapForward.class);
	private static TrapForward instance;
	private List<SnmpSender> senders = new ArrayList<SnmpSender>();

	/**
	 * 获取默认实例
	 * @return
	 */
	public static TrapForward getDefault() {
		if (instance == null) {
			synchronized (TrapForward.class) {
				if (instance == null)
					instance = new TrapForward();
			}
		}
		return instance;
	}
	
	public TrapForward() {
		String targets = Config.getDefault().getTrapTargets();
		String[] items = targets.split(";");
		for (String item : items) {
			String[] fields = item.split(":");
			String ip = fields[0];
			int port = 162;
			if (fields.length > 1)
				port = Integer.parseInt(fields[1]);
			senders.add(new SnmpSender(SnmpVersion.V2C.getId(), ip, port, "public"));
		}
	}

	public void send(PDU pdu) {
		for (SnmpSender sender : senders) {
			try {				
				sender.send(pdu);
				logger.debug("发送snmptrap至：{}", sender);
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "发送snmptrap失败", e);
			}
		}
	}
	
	
}
