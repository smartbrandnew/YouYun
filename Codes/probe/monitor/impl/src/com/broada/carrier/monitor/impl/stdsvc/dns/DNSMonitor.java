package com.broada.carrier.monitor.impl.stdsvc.dns;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.net.dns.DNSClient;

/**
 * DNS 服务监听器实现类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class DNSMonitor implements Monitor {

  private static final Log logger = LogFactory.getLog(DNSMonitor.class);

  private static final String ITEMCODE_REPLYTIME = "DNS-1";// 响应时间

  public DNSMonitor() {
  }

  /**
   * 实现DNS监测
   *
   *
   * 返回结果包括监测参数，端口和结果信息等
   *
   * @param srv
   * @return
   */
  public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    String param = context.getTask().getParameter();
    String ip = context.getNode().getIp();
    DNSParameter p = new DNSParameter(param);
    List<DNSMonitorCondition> conditions = p.getConditions();

    StringBuffer msg = new StringBuffer();
    int portCount = conditions.size();    
    long totalRespTime = 0;//总响应时间
    int canConnCount = 0;//可连接的端口数
    boolean isGood = true;
    boolean isHealth = true;
    MonitorInstance[] instances = new MonitorInstance[portCount];
    for (int index = 0; index < portCount; index++) {
      DNSMonitorCondition condition = (DNSMonitorCondition) conditions.get(index);
      int port = condition.getPort();
      int timeout = condition.getTimeout();
      DNSClient dns = new DNSClient(ip, port, timeout);
      String name = (condition.getParsename() == null || condition.getParsename().trim().equals("")) ? "www.test.com"
          : condition.getParsename();
      msg.append("监测DNS服务,端口:" + port + ".");
      int code = 0;
      long replyTime = 0;
      try {
        replyTime = System.currentTimeMillis();
        code = dns.query(name);
        replyTime = System.currentTimeMillis() - replyTime;
        if (replyTime <= 0) {
          replyTime = 1;
        }
        if (code == 0) {
          totalRespTime += replyTime;
          canConnCount++;
          // 检查是否能正确解析
          if (condition.isChkParse()) {
            msg.append("正确解析域名:" + condition.getParsename() + ".\n");
          } else {
            msg.append("正常运行.\n");
          }

          // 检查是否超时
          if (condition.isChkReplyTime()) {
            if (replyTime <= condition.getTimeout()) {
              msg.append("服务响应时间:" + replyTime + "毫秒.\n");
            } else {
              isHealth = false;
              msg.append("服务响应时间:" + replyTime + "毫秒>" + condition.getTimeout() + "毫秒.\n");
            }
          }
        } else {
          isGood = false;
          // 检查是否能正确解析
          if (condition.isChkParse()) {
            msg.append("解析域名:" + condition.getParsename() + "失败,原因:" + dns.getErrorMsg(code) + ".\n");
          } else {
            msg.append("运行失败,原因:" + dns.getErrorMsg(code) + ".\n");
          }

        }

        instances[index] = new MonitorInstance(condition.getField(), condition.getField());
        result.addPerfResult(new PerfResult(condition.getField(), ITEMCODE_REPLYTIME, replyTime));
      } catch (NamingException ex) {
        isGood = false;
        if (logger.isDebugEnabled()) {
          logger.debug("回应格式错误,域名解析异常.", ex);
        }
        if (condition.isChkParse()) {
          msg.append("解析域名:" + condition.getParsename() + "失败,原因:");
        }
        msg.append("回应格式错误,域名解析异常.\n");
      } catch (IOException ex) {
        isGood = false;
        if (condition.isChkParse()) {
          msg.append("解析域名:" + condition.getParsename() + "失败,原因:");
        }
        msg.append("获取回应信息超时,原因可能是服务没有运行.\n");
      }

    }

    if (!isGood) {
      result.setResultDesc(msg.toString());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
    } else {
      if (!isHealth) {
        result.setResultDesc(msg.toString());
        result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      } else {
        result.setState(MonitorConstant.MONITORSTATE_NICER);
      }
    }
    if (portCount == canConnCount) {
      //设定总的平均响应时间
      if (canConnCount > 0) {
        result.setResponseTime((long) ((double) totalRespTime / canConnCount));
      }
    } else {
      result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    }
    return result;
  }

	@Override
	public Serializable collect(CollectContext context) {		
		return null;
	}
}