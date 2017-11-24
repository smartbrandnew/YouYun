package com.broada.carrier.monitor.impl.db.oracle.rac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common.Constants;

/**
 * 通过jdbc连接串去获取connection时,不同机器支持的url连接串方式不同,默认是短连接方式
 * 如果在配置文件有特殊的ip配置,则返回特定长连接串方式
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-2-26 上午10:08:31
 */
public class OracleUrlUtils {

  private static final Log logger = LogFactory.getLog(OracleUrlUtils.class);

  private static final String CONFIG_FILE =System.getProperty("user.dir") +  "/conf/oracleUrlFilter.properties";

  private static Properties urlProp = null;

  private static String hostIps = "";
  
  private static String racInstances = "";
  
  private static Map<String,String[]> racMap = new HashMap();
  static {
    FileInputStream fis = null;
    try {
    	logger.warn("配置文件为："+CONFIG_FILE);
      fis = new FileInputStream(new File(CONFIG_FILE));
      if(fis != null){
        urlProp = new Properties();
        urlProp.load(fis);
        hostIps = urlProp.getProperty("hostIp");
        racInstances = urlProp.getProperty("racInstances");
        if(racInstances != null && !"".equals(racInstances)){
        	String rac[] = racInstances.split(",");
        	if(rac != null && rac.length>0){
        		for (int i = 0; i < rac.length; i++) {
    					String rac_inst[] = rac[i].split("!");
    					if(rac_inst != null && rac_inst.length==2){
    						String inst_arr[] = rac_inst[1].split("@");//划分出 rac下的多实例到数组inst_arr
    						racMap.put(rac_inst[0], inst_arr);
    					}
        		}
        	}
        }
        if (logger.isDebugEnabled()) {
          logger.debug(urlProp);
        }
      }
    } catch (FileNotFoundException e) {
      logger.error("conf/oracleUrlFilter.properties配置文件未找到，请确认配置是否正确", e);
    } catch (IOException e) {
      logger.error("load配置文件出错", e);
    } finally {
      try {
    	if(fis!=null)
        fis.close();
      } catch (IOException e) {
      }
    }
  }

  /**
   * 根据用户输入的ip地址,获取不同url连接串(某些机器不支持简单连接方式)
   * 
   * @param ip oracle数据库ip地址
   * @param port oracle数据库端口
   * @param sid oracle数据库服务名实例
   * @return
   */
  public static String getUrl(String ip, int port, String sid) {
    String url = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + sid;
    if (hostIps.indexOf(ip + ",") >= 0) {
      url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)" + "(HOST=" + ip + ")(PORT=" + port
          + ")))" + "(CONNECT_DATA=(SERVICE_NAME=" + sid + ")(SERVER=DEDICATED)))";
    }
    if(racMap != null){
    	try {
    	Iterator i = racMap.keySet().iterator();
    	while (i.hasNext()) {
				Object o = i.next();
				String key = o.toString();
				if (key.equals(ip+"|"+sid)) {// 判断某台机器（ip）配置的sid是oracle rac的sid，下面的url需要特殊处理
					String value[] = (String[]) racMap.get(key);
					StringBuffer _url = new StringBuffer("jdbc:oracle:thin:@(description=(address_list=");
					for (int j = 0; j < value.length; j++) {
						String name_prot[] = value[j].split(":");
						_url.append("(address=(host=").append(name_prot[0]).append(")(protocol=tcp)(port=").append(name_prot[1])
								.append("))");
					}
					_url.append("(load_balance=yes)(failover=yes))(connect_data=(service_name=").append(sid).append(")))");
					url = _url.toString();
				}
			}
    	}catch (Exception e) {
    		logger.error("在构建oracle rac 的jdbc连接 url时，解析conf/oracleUrlFilter.properties的racInstances参数配置出错", e);
    	}
    }
    if (logger.isDebugEnabled())
      logger.debug("oracle url:" + url);
    return url;
  }
  
}
