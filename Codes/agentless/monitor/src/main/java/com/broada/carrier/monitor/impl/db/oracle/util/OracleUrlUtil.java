package com.broada.carrier.monitor.impl.db.oracle.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.component.utils.error.ErrorUtil;

/**
 * 通过jdbc连接串去获取connection时,不同机器支持的url连接串方式不同,默认是短连接方式
 * 如果在配置文件有特殊的ip配置,则返回特定长连接串方式
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-2-26 上午10:08:31
 */
public class OracleUrlUtil {

	private static final Log logger = LogFactory.getLog(OracleUrlUtil.class);

	private static final String CONFIG_FILE = "conf/oracleUrlFilter.properties";

	private static Properties urlProp = null;

	private static String hostIps = "";
	static {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(CONFIG_FILE));
			if(fis != null){
				urlProp = new Properties();
				urlProp.load(fis);
				hostIps = urlProp.getProperty("hostIp");
				if (logger.isDebugEnabled()) {
					logger.debug(urlProp);
				}
			}
		} catch (FileNotFoundException e) {
			logger.debug("ORACLE监测配置文件不存在：" + CONFIG_FILE, e);    	
		} catch (IOException e) {
			ErrorUtil.warn(logger, "ORACLE监测配置文件加载失败", e);    	
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					ErrorUtil.warn(logger, "ORACLE监测配置文件关闭失败", e);
				}
			}
		}
	}

	/**
	 * 根据用户输入的ip地址,获取不同url连接串(某些机器不支持简单连接方式)
	 * 
	 * @param ip oracle数据库ip地址
	 * @param port oracle数据库端口
	 * @param sid oracle数据库服务名实例
	 * @param flag 连接方式是service_name(true) 还是  sid(false)
	 * @return
	 */
	public static String getUrl(String ip, int port, String sid, boolean flag) {
		String url = null;
		if(!flag)
			url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + sid;
		else
			url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)" + "(HOST=" + ip + ")(PORT=" + port
			+ ")))" + "(CONNECT_DATA=(SERVICE_NAME=" + sid + ")(SERVER=DEDICATED)))";
		if (hostIps.indexOf(ip + ",") >= 0) {
			url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)" + "(HOST=" + ip + ")(PORT=" + port
			+ ")))" + "(CONNECT_DATA=(SERVICE_NAME=" + sid + ")(SERVER=DEDICATED)))";
		}
		if (logger.isDebugEnabled())
			logger.debug("oracle url:" + url);
		return url;
	}
}
