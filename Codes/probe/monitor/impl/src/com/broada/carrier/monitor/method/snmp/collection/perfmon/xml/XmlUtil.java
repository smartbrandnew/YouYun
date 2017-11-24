package com.broada.carrier.monitor.method.snmp.collection.perfmon.xml;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XmlUtil {
	public static String getAttr(Element element, String name) throws Exception {
		if (element.getAttributeNode(name) == null)
			throw new Exception("元素[" + element.getNodeName() + "]属性[" + name + "]是必须的。");
		return element.getAttribute(name);
	}
	
	public static Document parseXmlFile(String filename) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		return factory.newDocumentBuilder().parse(filename);
	}
}
