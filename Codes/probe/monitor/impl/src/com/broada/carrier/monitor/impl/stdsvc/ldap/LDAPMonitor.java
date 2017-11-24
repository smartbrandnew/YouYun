package com.broada.carrier.monitor.impl.stdsvc.ldap;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.ldap.LdapMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.AuthenticationNotSupportedException;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import java.io.Serializable;
import java.util.Hashtable;

/**
 *  LDAP 监听器实现类
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author lucumu
 * @version 1.0
 */
public class LDAPMonitor extends BaseMonitor {
  
  private static final Log logger = LogFactory.getLog(LDAPMonitor.class);
  /**
   * 默认构造函数
   */
  public LDAPMonitor() {
  }

  /**
   * 实现Monitor.doMonitor()方法,当前会检查连接是否有效,指定的BaseDN是否有效,用户登陆等情况
   * @return MonitorResult
   * @throws Exception
   */
  @Override public Serializable collect(CollectContext context) {
    MonitorResult mr = new MonitorResult();
    mr.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    //取得将会用到的参数
    String ip = context.getNode().getIp();
    //配置参数
    LdapMethod method = new LdapMethod(context.getMethod());
    int port = method.getPort();
    int version = method.getVersion();
    String baseDN = method.getBaseDN();
    boolean anonymous = method.isAnonymous();
    boolean appendBaseDN = method.isAppendBaseDN();
    String userDN = method.getUsername();
    String password = method.getPassword();
    //LDAPContext
    DirContext cxt = null;
    //构造URL
    String url = "ldap://" + ip + ":" + port;
    //监测开始
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, url);
    env.put("java.naming.ldap.version", String.valueOf(version));

    if (anonymous) {
      //匿名方式登陆
      env.put(Context.SECURITY_AUTHENTICATION, "none");
    } else {
      //简单密码校验,以后要加入SSL,MD5加密校验等
      env.put(Context.SECURITY_AUTHENTICATION, "Simple");
      env.put(Context.SECURITY_PRINCIPAL,
              userDN + (appendBaseDN ? "," + baseDN : ""));
      env.put(Context.SECURITY_CREDENTIALS, password);
    }
    //设置监测参数(用于发送告警信息）
    try {
      long replyTime = System.currentTimeMillis();
      cxt = new InitialDirContext(env);
      SearchControls sc = new SearchControls();
      sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
      //不配任何过滤器，只是检查基本分辨名BASEDN是否有效
      cxt.search(baseDN, "key=*", sc);
      replyTime=System.currentTimeMillis() - replyTime;
      if (replyTime <= 0) {
        replyTime = 1;
      }
      mr.setResponseTime((int) replyTime);
    } catch (AuthenticationNotSupportedException e) {
      logger.debug("用户登录验证失败.",e);
      mr.setResultDesc("用户登录验证错误.");
      mr.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      return mr;
    } catch (NameNotFoundException e) {
      logger.debug("基本分辨名" + baseDN + "无效!URL=" + url ,e);
      mr.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      mr.setResultDesc("基本分辨名" + baseDN + "无效." );
      return mr;
    } catch (NamingException e) {
      mr.setState(MonitorConstant.MONITORSTATE_FAILING);
      mr.setResultDesc("无法连接服务器" + url +".");
      return mr;
    } finally {
      try {
        if (cxt != null) {
          cxt.close();
        }
      } catch (NamingException ex) {
      }
    }
    mr.setResultDesc("监测一切正常");
    mr.setState(MonitorConstant.MONITORSTATE_NICER);
    return mr;
  }
  
}
