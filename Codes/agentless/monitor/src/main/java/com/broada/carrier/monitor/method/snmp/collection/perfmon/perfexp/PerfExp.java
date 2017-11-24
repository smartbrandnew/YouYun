package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.broada.carrier.monitor.method.snmp.collection.perfmon.xml.XmlUtil;

public class PerfExp {
	private static final Log logger = LogFactory.getLog(PerfExp.class);
	
	private ProducerCollection producers = new ProducerCollection();
	
	private static PerfExp instance = null;

	public ProducerCollection getProducers() {
		return producers;
	}
	
	private void readConfig(Element root) throws Exception {
		getProducers().clear();
		NodeList products = root.getElementsByTagName("producer");
		for (int i = 0; i < products.getLength(); i++){			
			getProducers().add(Producer.parse((Element)products.item(i)));
    }
	}

	public static PerfExp getInstance() {
		synchronized (PerfExp.class) {
			if (instance == null) {
				instance = new PerfExp();
				Document doc = null;
				try {
					doc = XmlUtil.parseXmlFile("conf/perfmon.xml");
					NodeList perfExps = doc.getElementsByTagName("perfexp");
					if (perfExps.getLength() <= 0)
						throw new Exception("配置文件未配置元素[perfExp]");
					instance.readConfig((Element) perfExps.item(0));
				} catch (Exception err) {
					logger.error("读取性能采集表达式文件conf/perfmon.xml错误，系统将不提供任何设备的性能表达式。",err);
				}
			}
			return instance;
		}
	}
}
