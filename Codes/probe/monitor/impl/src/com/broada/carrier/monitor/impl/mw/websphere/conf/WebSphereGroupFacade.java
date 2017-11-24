package com.broada.carrier.monitor.impl.mw.websphere.conf;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.impl.mw.websphere.WASUtil;
import com.broada.carrier.monitor.impl.mw.websphere.entity.WASMonitorResult;
import com.broada.carrier.monitor.impl.mw.websphere.entity.ui.UiGroup;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.Version;
import com.broada.carrier.monitor.impl.mw.websphere.entity.was.WebSphere;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

/**
 * 门面
 * 
 * @author lixy Sep 16, 2008 2:28:15 PM
 */
public class WebSphereGroupFacade {
  public static UiGroup getUiGroupByTypeId(String typeId) {
    return UiGroupLoader.getUiGroupByGroupId(typeId);
  }
  
  public static String getDefaultLinkUrl() {
  	return getDefaultLinkUrl(false);
  }
  
  public static String getDefaultLinkUrl(boolean isGroup) {
    List<Version> versions = getWASVersionsConfList(isGroup);
    for (int i = 0; i < versions.size(); i++) {
      Version ver = (Version) versions.get(i);
      if (ver.getDefUrl().equalsIgnoreCase("true"))
        return ver.getUrl();
    }
    return "";
  }
  /**
   * 获取配置的WebSphere版本获取配置列表
   * 
   * @return
   */
  public static List<Version> getWASVersionsConfList(boolean isGroup) {
    return WebSphereGroupLoader.getVersions(isGroup);
  }
  
  public static WebSphere getWebSphereByVersion(String version) throws Exception {
    return getWebSphereByVersion(version, false);
  }

  /**
   * 根据版本信息获取相应版本的WebSphere配置信息
   * 
   * @param version
   * @return
   * @throws Exception
   */
  public static WebSphere getWebSphereByVersion(String version, boolean isGroup) throws Exception {
    return WebSphereGroupLoader.getWebSphereByVersion(version, isGroup);
  }

  /**
   * 获取当前WebSphere的版本号
   * 
   * @param host
   * @param port
   * @param username
   * @param password
   * @param extras 包含其他的配置参数属性
   * @return
   * @throws Exception
   */
  public static String getWASVersion(String host, String port, String username, String password, Map<String, String> extras)
      throws Exception {
    return getWASVersion(host, port, username, password, extras, false);
  }
  
  public static String getWASVersion(String host, String port, String username, String password, Map<String, String> extras, boolean isGroup)
      throws Exception {
  		return WebSpherePerfGetter.getWASVersion(host, port, username, password, extras, isGroup);
  }

  /**
   * 获取当前WebSphere性能结果集
   * 
   * @param version
   * @param typeId
   * @param host
   * @param port
   * @param node
   * @param server
   * @param username
   * @param password
   * @param instKeys
   * @return
   * @throws Exception
   */
  public static Map<String, WASMonitorResult> getWASMonitorResults(String version, String typeId, String host,
      String port, String node, String server, String username, String password, List<String> instKeys,Map extras, boolean isGroup)
      throws Exception {
    return WebSpherePerfGetter.getWASMonitorResults(version, typeId, host, port, node, server, username, password,
        instKeys,extras, isGroup);
  }
  
  public static Map<String,WASMonitorResult> getAllWASMonitorResults(String version, String typeId, String host, String port,
      String node, String server, String username, String password,Map<String, String> extras) throws Exception {
    return getAllWASMonitorResults(version, typeId, host, port, node, server, username, password, extras, false);
  }

  /**
   * 获取所有监测实例的监测结果集
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
  public static Map<String,WASMonitorResult> getAllWASMonitorResults(String version, String typeId, String host, String port,
      String node, String server, String username, String password,Map<String, String> extras, boolean isGroup) throws Exception {
    return WebSpherePerfGetter.getAllWASMonitorResults(version, typeId, host, port, node, server, username, password, extras, isGroup);
  }
  
  public static void link(String url, Map attrs) throws IOException, InterruptedException {
    WASUtil.link(url, attrs);
  }
  
  public static void doJ2CCalculate(String typeId, MonitorTempData lastData, Map<String, WASMonitorResult> results) {  	
  	DefaultDynamicObject last = null;
  	if (lastData != null) {
  		last = lastData.getData(DefaultDynamicObject.class);
  		if (last == null)
  			last = new DefaultDynamicObject();
  	}
  	
    if (typeId.equalsIgnoreCase("WAS-J2C-PMI")) {
      for (Iterator<String> iter = results.keySet().iterator(); iter.hasNext();) {
        String key = iter.next();
        WASMonitorResult result = results.get(key);
        Double newperf = result.getPerfValue("WAS-J2C-PMI-1");
        Double oldperf = null;
        if (last != null) {
        	String item = key + "-WAS-J2C-PMI-1";
        	oldperf = (Double)last.get(item);
        	last.put(item, newperf);
        }
        if (oldperf == null) {
          result.addPerfItem("WAS-J2C-PMI-2", 0);
        } else if (oldperf < newperf) {
          result.addPerfItem("WAS-J2C-PMI-2", newperf - oldperf);
        }
      }
    }
    
    if (lastData != null)
    	lastData.setData(SerializeUtil.encodeBytes(last));
  }  
}
