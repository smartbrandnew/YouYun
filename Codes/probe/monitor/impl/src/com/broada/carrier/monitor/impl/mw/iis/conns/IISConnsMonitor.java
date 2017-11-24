package com.broada.carrier.monitor.impl.mw.iis.conns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.iis.common.IISTempData;
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
 * IIS连接监测
 * 
 * @author 杨帆
 * 
 */
public class IISConnsMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(IISConnsMonitor.class);

  public static final String INDEX_CURR_CONNS = "IIS-CONNS-1";

  public static final String INDEX_MAX_CONNS = "IIS-CONNS-2";

  public static final String INDEX_CONN_ATT_PERS = "IIS-CONNS-3";

  public static final String INDEX_LOGON_ATT_PERS = "IIS-CONNS-4";

  public static final String INDEX_RUNTIME = "IIS-CONNS-5";

  @Override
	public MonitorResult monitor(MonitorContext context) {
  	IISTempData tempData = context.getTempData(IISTempData.class);
  	if (tempData == null)
  		tempData = new IISTempData();
		MonitorResult result = collect(new CollectContext(context), context.getTask().getId(), tempData);
		context.setTempData(tempData);
		return result;
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect(context, "-1", new IISTempData());
	}

	private MonitorResult collect(CollectContext context, String taskId, IISTempData tempData) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    CLIResult cliResult = null;

    try {
      long replyTime = System.currentTimeMillis();
      cliResult = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(),
          CLIConstant.COMMAND_IISCONNS);
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
      result.setResultDesc("解析IIS连接信息采集结果失败：" + e.getMessage());
      logger.error("解析IIS连接信息采集结果失败.@" + context.getNode().getIp(), e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (Throwable e) {
      String msg = e.getMessage();
      if (StringUtils.contains(msg, "RPC 服务器不可用") || StringUtils.contains(msg, "RPC server is unavailable")) {
        result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        return result;
      }
      result.setResultDesc("获取IIS连接信息失败:" + e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    List<Properties> conns = cliResult.getListTableResult();

    List<Properties> realConns = getRealData(conns, context, tempData);

    tempData.getDatas().clear();
    tempData.getDatas().addAll(conns);
    
    for (Properties properties : realConns) {
    	String webName = (String) properties.get("Name");
    	MonitorResultRow row = new MonitorResultRow(webName);
    	
      String connectionAttemptsPersec = (String) properties.get("ConnectionAttemptsPersec");
      row.setIndicator(INDEX_CONN_ATT_PERS, Integer.parseInt(connectionAttemptsPersec));
      
      String currentConnections = (String) properties.get("CurrentConnections");
      row.setIndicator(INDEX_CURR_CONNS, Integer.parseInt(currentConnections));
      
      String logonAttemptsPersec = (String) properties.get("LogonAttemptsPersec");
      row.setIndicator(INDEX_LOGON_ATT_PERS, Integer.parseInt(logonAttemptsPersec));

      String maximumConnections = (String) properties.get("MaximumConnections");
      row.setIndicator(INDEX_MAX_CONNS, Integer.parseInt(maximumConnections));

      String serviceUptime = (String) properties.get("ServiceUptime");
      row.setIndicator(INDEX_RUNTIME, Double.parseDouble(serviceUptime));

      result.addRow(row);
    }

    return result;
  }

  /**
   * 根据上次保存的性能值计算真实的流量
   * 
   * @param conns
   *          当前实时获取到的数据
   * @param srv
   * @return
   */
  private List<Properties> getRealData(List<Properties> conns, CollectContext context, IISTempData lastData) {
    List<Properties> realData = new ArrayList<Properties>();
    for (int index = 0; index < conns.size(); index++) {
      Properties properties = (Properties) conns.get(index);
      Properties Last_data = null;
      for (int i = 0; i < lastData.getDatas().size(); i++) {
        Properties last_propertie = (Properties) lastData.getDatas().get(i);
        if (((String) last_propertie.getProperty("Name")).equals((String) properties.get("Name"))) {
          Last_data = last_propertie;
          break;
        }
      }
      Properties newProp = new Properties();
      newProp.setProperty("Name", (String) properties.get("Name"));
      newProp.setProperty("CurrentConnections", (String) properties.get("CurrentConnections"));
      newProp.setProperty("MaximumConnections", (String) properties.get("MaximumConnections"));
      newProp.setProperty("ServiceUptime", (String) properties.get("ServiceUptime"));
      if (Last_data == null) {
        // 没有历史数据则直接获取这次的数据,并设置相关值为0
        newProp.setProperty("ConnectionAttemptsPersec", "0");
        newProp.setProperty("LogonAttemptsPersec", "0");
      } else {
        // 有历史数据的情况下计算真实数据
        double last_uptime = Double.parseDouble((String) Last_data.get("ServiceUptime"));
        double realConnAttPersec = 0;
        double realLogonAttPersec = 0;

        double per_conn_att_pers = Double.parseDouble((String) Last_data.get("ConnectionAttemptsPersec"));
        double per_logon_att_pers = Double.parseDouble((String) Last_data.get("LogonAttemptsPersec"));

        realConnAttPersec = (Double.parseDouble((String) properties.get("ConnectionAttemptsPersec")) - (Double
            .parseDouble((String) Last_data.get("ConnectionAttemptsPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        // 如果数据没有发生变化或则不合理则取上一次保存的性能数据
        if (realConnAttPersec == 0 || Double.isNaN(realConnAttPersec) || realConnAttPersec < 0) {
          realConnAttPersec = per_conn_att_pers;
        }

        realLogonAttPersec = (Double.parseDouble((String) properties.get("LogonAttemptsPersec")) - (Double
            .parseDouble((String) Last_data.get("LogonAttemptsPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        if (realLogonAttPersec == 0 || Double.isNaN(realLogonAttPersec) || realLogonAttPersec < 0) {
          realLogonAttPersec = per_logon_att_pers;
        }
        newProp.setProperty("LogonAttemptsPersec", String.valueOf(new Double(realLogonAttPersec).intValue()));
        newProp.setProperty("ConnectionAttemptsPersec", String.valueOf(new Double(realConnAttPersec).intValue()));
      }
      realData.add(newProp);
    }
    return realData;
  }
}
