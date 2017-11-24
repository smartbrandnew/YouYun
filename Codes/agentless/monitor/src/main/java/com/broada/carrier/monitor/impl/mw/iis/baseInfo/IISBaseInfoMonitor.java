package com.broada.carrier.monitor.impl.mw.iis.baseInfo;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * IIS基本信息监测
 * @author 杨帆
 * 
 */
public class IISBaseInfoMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(IISBaseInfoMonitor.class);
  private static final String INDEX_SERVICEUPTIME = "IIS-BASEINFO-1";
  
  @Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(new CollectContext(context), context.getTask().getId());
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect(context, "-1");
	}
		
	private MonitorResult collect(CollectContext context, String id) {
		MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    CLIResult cliResult = null;

    try {
      long replyTime = System.currentTimeMillis();
      cliResult = new CLIExecutor("-1").execute(context.getNode(), context.getMethod(),
          CLIConstant.COMMAND_IISBASEINFO);
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (CLILoginFailException fe) {
      result.setResultDesc("登录目标服务器失败，请检查监测配置的用户/密码等是否正确.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (CLIConnectException ce) {
      result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开.");
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (CLIResultParseException e) {
      result.setResultDesc("解析IIS基本信息采集结果失败：" + e.getMessage());
      logger.error("解析IIS基本信息采集结果失败.@" + context.getNode().getIp(), e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (Throwable e) {
      String msg = e.getMessage();
      if (StringUtils.contains(msg, "RPC 服务器不可用") || StringUtils.contains(msg, "RPC server is unavailable")) {
        result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        return result;
      }
      result.setResultDesc("获取IIS基本信息失败:" + e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    List<Properties> baseInfos = cliResult.getListTableResult();    
    for (Properties properties : baseInfos) {
      String webName = (String) properties.get("Name");
      String serviceUptime = (String) properties.get("ServiceUptime");
      MonitorResultRow row = new MonitorResultRow(webName);
      row.setIndicator(INDEX_SERVICEUPTIME, Float.parseFloat(serviceUptime));
      result.addRow(row);
    }

    return result;
  }
}
