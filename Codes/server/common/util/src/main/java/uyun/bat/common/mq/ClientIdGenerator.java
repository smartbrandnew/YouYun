package uyun.bat.common.mq;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class ClientIdGenerator {
	private static final Logger logger = LoggerFactory.getLogger(ClientIdGenerator.class);

	protected static String generateClientId(String cid) {
		String clientId = null;
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			clientId = cid + "-" + ip + "-" + System.currentTimeMillis();
		} catch (Exception e) {
			clientId = cid + "-127.0.0.1-" + System.currentTimeMillis();
			logger.warn("gain host message error: " + e);
		}
		return clientId;
	}
}
