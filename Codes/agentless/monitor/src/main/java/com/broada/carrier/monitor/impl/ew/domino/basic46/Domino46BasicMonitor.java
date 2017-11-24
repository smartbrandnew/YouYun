package com.broada.carrier.monitor.impl.ew.domino.basic46;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.ew.domino.basic.DominoParam;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import java.io.Serializable;

public class Domino46BasicMonitor extends BaseMonitor {
  private Logger logger = LoggerFactory.getLogger(Domino46BasicMonitor.class);

  private static final String ITEMIDX_MEMUSED = "DOMINO46_BASIC-1";

  private static final String ITEMIDX_DBUSEDPCT = "DOMINO46_BASIC-2";

  private static final String ITEMIDX_MAILWAITSEND = "DOMINO46_BASIC-3";

  private static final String ITEMIDX_MAILDEAD = "DOMINO46_BASIC-4";

  @Override public Serializable collect(CollectContext context) {

    PerfResult perfMemUsed = new PerfResult(ITEMIDX_MEMUSED, false);
    PerfResult perfDBUsedPct = new PerfResult(ITEMIDX_DBUSEDPCT, false);
    PerfResult perfMailWaitSend = new PerfResult(ITEMIDX_MAILWAITSEND, false);
    PerfResult perfMailDead = new PerfResult(ITEMIDX_MAILDEAD, false);
    StringBuffer msg = new StringBuffer();
    StringBuffer currVal = new StringBuffer();
    MonitorState state = MonitorConstant.MONITORSTATE_NICER;
    DominoParam param = context.getParameterObject(DominoParam.class);
    String monDBName1 = param.getDbName();

    MonitorResult result = new MonitorResult();
    result.setResponseTime(0);
    String ipAddr = context.getNode().getIp();

    BasicService service = null;
    long time = System.currentTimeMillis();
    try {
      service = getBasicService(ipAddr);
    } catch (Exception ex) {
      msg.append("无法连接到Domino服务器,请检查是否正确的启动的Domino46的代理Agent!");
      currVal.append("无法连接到Domino服务器,请检查是否正确的启动的Domino46的代理Agent!");
      state = MonitorConstant.MONITORSTATE_FAILING;
      result.setResultDesc(msg.toString());
      result.setState(state);
      return result;
    }

    //memused
    try {
      int memUsed = service.getFirstDocIntValue("statrep.nsf", "1. Statistics Reports \\ 1. System", "Mem.Allocated");
      if (memUsed > 0) {
        perfMemUsed.setValue(memUsed / 1024 / 1024);
      }
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取服务器内存占用时出现错误", e);
      }
      msg.append("获取服务器内存占用时出现错误!\n");
      currVal.append("获取服务器内存占用时出现错误!");
      state = MonitorConstant.MONITORSTATE_FAILING;
    }

    //mailwaitsend
    try {
      int mailWaitSend = service.getFirstDocIntValue("statrep.nsf", "1. Statistics Reports \\ 2. Mail & Database",
          "MAIL.WaitingRecipients");
      perfMailWaitSend.setValue(mailWaitSend);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取服务器待发邮件数时出现错误", e);
      }
      msg.append("获取服务器待发邮件数时出现错误!\n");
      currVal.append("获取服务器待发邮件数时出现错误!");
      state = MonitorConstant.MONITORSTATE_FAILING;
    }

    //maildead
    try {
      int mailDead = service.getFirstDocIntValue("statrep.nsf", "1. Statistics Reports \\ 2. Mail & Database",
          "MAIL.Dead");
      perfMailDead.setValue(mailDead);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("获取服务器僵死邮件数时出现错误", e);
      }
      msg.append("获取服务器僵死邮件数时出现错误!\n");
      currVal.append("获取服务器僵死邮件数时出现错误!");
      state = MonitorConstant.MONITORSTATE_FAILING;
    }

    //custom nsf
    try {
      double pct = service.getDBPercentUsed(monDBName1);
      perfDBUsedPct.setValue(pct);
      //perfDBUsedPct.setInstanceKey(monDBName1);

      msg.append("Domino数据库" + monDBName1 + "使用率" + Math.round((Double) perfDBUsedPct.getValue()) + "%");
      currVal.append(monDBName1 + "使用率" + Math.round((Double) perfDBUsedPct.getValue()) + "%");

    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("统计数据库" + monDBName1 + "使用率出错!请检查数据库是否存在和访问权限配置!", e);
      }
      msg.append("统计数据库" + monDBName1 + "使用率出错!请检查数据库是否存在和访问权限配置!");
      currVal.append("统计数据库" + monDBName1 + "使用率出错!");
      state = MonitorConstant.MONITORSTATE_FAILING;
    }

    long replyTime = System.currentTimeMillis() - time;
    if(replyTime<=0){
      replyTime=1;
    }
    result.setResponseTime((int)replyTime);
    service = null;
    result.setPerfResults(new PerfResult[] { perfMemUsed, perfDBUsedPct, perfMailWaitSend, perfMailDead });
    if (state == MonitorConstant.MONITORSTATE_NICER) {
      result.setResultDesc("监测一切正常");
    } else {
      result.setResultDesc(msg.toString());
    }

    result.setState(state);
    return result;
  }

  private BasicService getBasicService(String ipAddr) throws Exception {
    try {
      RmiProxyFactoryBean factory = new RmiProxyFactoryBean();
      factory.setServiceInterface(BasicService.class);
      factory.setServiceUrl("rmi://" + ipAddr + ":1199/domino46BasicService");
      factory.afterPropertiesSet();
      return (BasicService) factory.getObject();
    } catch (Exception ex) {
      throw ex;
    }
  }
}
