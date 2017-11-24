package com.broada.carrier.monitor.impl.mw.websphere.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import com.broada.carrier.monitor.impl.mw.websphere.WASUtil;
import com.broada.carrier.monitor.impl.mw.websphere.entity.WASMonitorResult;
import com.broada.carrier.monitor.impl.mw.websphere.entity.XMLLock;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Type;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Version;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.WebSphere;
import com.broada.utils.ListUtil;
import com.broada.utils.StringUtil;

/**
 * 性能获取
 * 
 * @author lixy Sep 16, 2008 2:29:09 PM
 */
public class WebSpherePerfGetter {
  private static final Log logger = LogFactory.getLog(WebSpherePerfGetter.class);

  /**
   * 获取WebSphere的版本号
   * 
   * @return
   * @throws Exception
   * @throws IOException
   * @throws JDOMException
   */
  public static String getWASVersion(String host, String port, String username, String password,Map<String, String> extras, boolean isGroup) throws Exception {
    List<Version> verConfList = WebSphereGroupFacade.getWASVersionsConfList(isGroup);
    if (ListUtil.isNullOrEmpty(verConfList)) {
      logger.error("没有配置WebSphere版本获取信息。");
      throw new RuntimeException("没有配置WebSphere版本获取信息。");
    }
    Map<String, String> params = new HashMap<String, String>();
    params.put("host", host);
    params.put("port", port);
    params.putAll(extras);

    synchronized (XMLLock.getInstance()) {
      for (Iterator<Version> iter = verConfList.iterator(); iter.hasNext();) {
        GetMethod get = null;
        try {
          String userSSL = extras.get("useSSL").toString().toLowerCase();
            if (userSSL == "true" || userSSL.equals("true")) {
              WASUtil.registerHttps(extras);
            }
          Version ver = iter.next();
          get = WASUtil.getHttpGet(WASUtil.applyParams(ver.getUrl(), params), host, username, password);
          if (get == null) {
            continue;
          }
          Document doc = WASUtil.build(get, host, port);
          String version = WASUtil.getCurrWASVersion(doc, ver.getEleId(), ver.getValue());
          if (!StringUtil.isNullOrBlank(version)) {
            return version;
          }
        } catch (Exception e) {
          //e.printStackTrace();
          logger.error("",e);
          continue;
        } finally {
          if (get != null) {
            get.releaseConnection();
          }
        }
      }
      return "5.1.0";
    }
  }

  /**
   * 根据版本信息、监测类型ID获取监测结果集
   * 
   * @param version
   * @param typeId
   * @param node
   * @param server
   * @return
   * @throws Exception
   */
  public static Map<String, WASMonitorResult> getWASMonitorResults(String version, String typeId, String host,
      String port, String node, String server, String username, String password, List<String> instKeys,Map extras, boolean isGroup)
      throws Exception {
    WebSphere was = WebSphereGroupFacade.getWebSphereByVersion(version, isGroup);
    Type type = was.getType(typeId);
    Map<String, Element> monEles = WASUtil.getMonitorElements(type, host, port, node, server, username, password,extras);

    Map<String, WASMonitorResult> results = new HashMap<String, WASMonitorResult>();
    for (Iterator<String> iter = instKeys.iterator(); iter.hasNext();) {
      String currInstKey = iter.next();
      WASMonitorResult result = WASUtil.getMonitorResult(type, monEles, currInstKey);
      results.put(result.getInstKey(), result);
    }
    return results;
  }

  /**
   * 获取所有监测结果集
   * 
   * @param version
   * @param typeId
   * @param host
   * @param port
   * @param node
   * @param server
   * @param username
   * @param password
   * @return
   * @throws Exception
   */
  public static Map<String, WASMonitorResult> getAllWASMonitorResults(String version, String typeId, String host,
      String port, String node, String server, String username, String password,Map<String, String> extras, boolean isGroup) throws Exception {
    WebSphere was = WebSphereGroupFacade.getWebSphereByVersion(version, isGroup);
    //判断是否支持该性能监测
    Type type = was.getType(typeId);
    if (type == null) {
			Map itemMap = new HashMap<String, String>();
			itemMap.put("WAS-EJB-PMI", "beanModule");//以后可加入更多
			String objTypeId = was.getTypes().keySet().iterator().next();
			type = was.getType(objTypeId);
			String url = type.getUrl().trim();
			int x = url.lastIndexOf("=");
			url = url.substring(0, x + 1) + itemMap.get(typeId);
			Map<String, String> params = new HashMap<String, String>();
			params.put("host", host);
			params.put("port", port);
			params.putAll(extras);
			url = WASUtil.applyParams(url, params);
			synchronized (XMLLock.getInstance()) {
				GetMethod get = null;
				try {
					String useSSL = extras.get("useSSL").toLowerCase();
					if (useSSL == "true" || useSSL.equals("true")) {
						/*String serverCerPath = extras.get("server_cer")==null?"":extras.get("server_cer").trim().replaceAll("\\\\", "/");
						String clientKeyPath = extras.get("client_key")==null?"":extras.get("client_key").trim().replaceAll("\\\\", "/");
						String clientKeyPwd = extras.get("client_key_pwd");

						int stroeState = 0;// null
						if (!(serverCerPath == "" && serverCerPath.equals(""))) {
              stroeState = 1;
            }
            if (!(clientKeyPath == "" && clientKeyPath.equals(""))) {
              stroeState = 2;
            }
            if (!(serverCerPath == "" && serverCerPath.equals(""))
                && !(clientKeyPath == "" && clientKeyPath.equals(""))) {
              stroeState = 3;
            }
						String clientKeyType = "";
						if (!(clientKeyPath == WASParamPanel.CERPATH || clientKeyPath.equals(WASParamPanel.CERPATH))) {
							String[] clientKeyPaths = clientKeyPath.split("\\.");
							clientKeyType = clientKeyPaths[clientKeyPaths.length - 1];
						}
						//转换路径
						clientKeyPath = new File(System.getProperty("user.dir", ".")).getAbsolutePath().replaceAll("\\\\", "/") + "/" + clientKeyPath; 
            serverCerPath = new File(System.getProperty("user.dir", ".")).getAbsolutePath().replaceAll("\\\\", "/") + "/" + serverCerPath; 
            // 注册https协议
						BroadaSecureProtocolSocketFactory bspsf = new BroadaSecureProtocolSocketFactory(new URL("file:///"
								+ clientKeyPath), clientKeyPwd, new URL("file:///" + serverCerPath), WASParamPanel.TRUSTSTORE_PWD,
								stroeState, clientKeyType);
						Protocol httpsProtocol = new Protocol("https", bspsf, 443);
						Protocol.registerProtocol("https", httpsProtocol);*/
					  WASUtil.registerHttps(extras);
					}
					get = WASUtil.getHttpGet(url, host, username, password);
					Document doc = WASUtil.build(get, host, port);
					Element root = doc.getRootElement();
					Element childOne = root.getChild("Comments");
					if(childOne.getText().contains("Unable to find the module")) {
						throw new Exception("监测目标websphere不支持此模块 "+ itemMap.get(typeId) +" 监测,\n请确定websphere是否配置正确或是否包含此模块.");
					} else {
						throw new Exception("该监测器暂时不支持WebSphere " + version);
					}
				} catch (IOException e) {
					throw new IOException("无法连接到Websphere服务器或连接超时：" + e.getMessage());
				} catch (JDOMException e) {
					throw new JDOMException("获取Websphere性能数据出错：" + e.getMessage());
				} finally {
					if (get != null) {
						get.releaseConnection();
					}
				}
			}
		}
    //若支持：
    Map<String, Element> monEles = WASUtil.getMonitorElements(type, host, port, node, server, username, password,extras);
    return WASUtil.getAllMonitorResults(type, monEles);
  }
  
  
}
