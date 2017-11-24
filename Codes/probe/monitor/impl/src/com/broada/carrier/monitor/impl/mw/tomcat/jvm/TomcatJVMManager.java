package com.broada.carrier.monitor.impl.mw.tomcat.jvm;

import com.broada.carrier.monitor.impl.mw.tomcat.AbstractTomcatManager;
import com.broada.carrier.monitor.impl.mw.tomcat.Tomcat;
import com.broada.carrier.monitor.impl.mw.tomcat.TomcatStatus;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("deprecation") public class TomcatJVMManager extends AbstractTomcatManager {
	private static Logger logger = LoggerFactory.getLogger(TomcatJVMManager.class);
	private static final String TOMCAT_DIGESTER_RULE = "conf/tomcat/tomcat-digester-rule.xml";

	private static Digester digester = null;

	static {
		URL url = null;
		try {
			url = new File(TOMCAT_DIGESTER_RULE).toURL();
			digester = DigesterLoader.createDigester(url);
		} catch (MalformedURLException e) {
			logger.error("地址{}错误", url, e);
		}
	}

	public Tomcat fetchInfo(InputStream is) throws IOException, SAXException {
		digester.setValidating(false);
		TomcatStatus ts = new TomcatStatus();
		digester.push(ts);
		digester.parse(is);

		return ts;
	}
}
