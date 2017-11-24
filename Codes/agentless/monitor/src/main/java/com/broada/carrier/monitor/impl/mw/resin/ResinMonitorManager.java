package com.broada.carrier.monitor.impl.mw.resin;

import com.broada.carrier.monitor.impl.mw.resin.connPool.ResinConn;
import com.broada.carrier.monitor.impl.mw.resin.ratio.ResinRatio;
import com.broada.carrier.monitor.impl.mw.resin.webApp.WebApp;
import com.broada.monitor.agent.resin.server.ResinAgent;
import com.broada.monitor.agent.resin.server.entity.ResinBasic;
import com.broada.monitor.agent.resin.server.entity.ResinCache;
import com.broada.monitor.agent.resin.server.entity.ResinConnectionPool;
import com.broada.monitor.agent.resin.server.entity.ResinWebApp;
import com.broada.utils.ListUtil;
import com.caucho.hessian.client.HessianProxyFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resin监测管理器
 * @author 杨帆
 * 
 */
public class ResinMonitorManager {

  private static HessianProxyFactory factory;

  public ResinMonitorManager() {
    init();
  }

  private void init() {
    if (factory == null) {
      factory = new HessianProxyFactory();
    }
  }

  public ResinBasic getBaseInfoByUrl(String url) throws MalformedURLException {
    ResinAgent agent = (ResinAgent) factory.create(ResinAgent.class, url);
    ResinBasic _resinBasic = agent.getResinBasic();
    return _resinBasic;
  }

  private ResinCache getRatioCacheByUrl(String url) throws MalformedURLException {
    ResinAgent agent = (ResinAgent) factory.create(ResinAgent.class, url);
    ResinCache _cache = agent.getResinCache();
    return _cache;
  }

  public List getFirstConnsByUrl(String url) throws MalformedURLException {
    ResinAgent agent = (ResinAgent) factory.create(ResinAgent.class, url);
    return agent.getResinConnectionPool();
  }

  public List getConnPoolsByUrl(String url) throws MalformedURLException {
    List conns = new ArrayList();
    List connPools = getFirstConnsByUrl(url);
    for (int i = 0; i < connPools.size(); i++) {
      ResinConnectionPool pool = (ResinConnectionPool) connPools.get(i);
      ResinConn conn = new ResinConn();
      conn.setName(pool.getName());
      conn.setActiveCount(pool.getActiveCount());
      conn.setIdleCount(pool.getIdleCount());
      conn.setCreate_ratio(new Double(pool.getCreate_ratio()));
      conns.add(conn);
    }
    return conns;
  }

  public List getFirstWebAppsByUrl(String url, String contextRoot) throws MalformedURLException {
    ResinAgent agent = (ResinAgent) factory.create(ResinAgent.class, url);
    Map webAppsMap = agent.getResinWebApp();
    return (List) webAppsMap.get("http://" + contextRoot);
  }

  public List getWebAppsByUrl(String url, String contextRoot) throws MalformedURLException {
    List webApps = new ArrayList();
    List firstWebApps = getFirstWebAppsByUrl(url, contextRoot);
    if(ListUtil.isNullOrEmpty(firstWebApps)) {
      return webApps;
    }else {
      for (int i = 0; i < firstWebApps.size(); i++) {
        ResinWebApp webApp = (ResinWebApp) firstWebApps.get(i);
        WebApp app = new WebApp();
        app.setContextPath(webApp.getContextPath());
        app.setRequestCount(Integer.valueOf(webApp.getRequestCount()));
        app.setSessionActiveCount(new Integer(String.valueOf(webApp.getSessionActiveCount())));
        webApps.add(app);
      }
    }
    return webApps;
  }

  public List getRatiosByUrl(String url) throws MalformedURLException {
    List list = new ArrayList();
    ResinCache cache = getRatioCacheByUrl(url);
    ResinRatio proxyRatio = new ResinRatio();
    proxyRatio.setName(ResinMonitorUtil.RESIN_PROXY_RATIO);
    proxyRatio.setRatio(new Double(cache.getProxy_miss_ratio()));
    proxyRatio.setHitCount(new Integer(String.valueOf(cache.getProxyHitCount())));
    proxyRatio.setTotalCount(new Integer(String.valueOf(cache.getProxyMissCount() + cache.getProxyHitCount())));
    list.add(proxyRatio);
    ResinRatio blockRatio = new ResinRatio();
    blockRatio.setName(ResinMonitorUtil.RESIN_BLOCK_RATIO);
    blockRatio.setRatio(new Double(cache.getBlk_miss_ratio()));
    blockRatio.setHitCount(new Integer(String.valueOf(cache.getBlockHitCount())));
    blockRatio.setTotalCount(new Integer(String.valueOf(cache.getBlockHitCount() + cache.getBlockMissCount())));
    list.add(blockRatio);
    ResinRatio invRatio = new ResinRatio();
    invRatio.setName(ResinMonitorUtil.RESIN_INV_RATIO);
    invRatio.setRatio(new Double(cache.getInv_miss_ratio()));
    invRatio.setHitCount(new Integer(String.valueOf(cache.getInvocationHitCount())));
    invRatio.setTotalCount(new Integer(String.valueOf(cache.getInvocationHitCount() + cache.getInvocationMissCount())));
    list.add(invRatio);
    return list;
  }
}
