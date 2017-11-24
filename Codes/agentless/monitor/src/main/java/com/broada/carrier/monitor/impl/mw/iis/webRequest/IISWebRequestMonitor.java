package com.broada.carrier.monitor.impl.mw.iis.webRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.iis.common.IISTempData;
import com.broada.carrier.monitor.impl.mw.iis.files.IISFilesMonitor;
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
 * IISWeb请求监测
 * 
 * @author 杨帆
 * 
 */
public class IISWebRequestMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(IISFilesMonitor.class);

  public static final String INDEX_GET_REQUEST = "IIS-WEBREQUEST-1";

  public static final String INDEX_POST_REQUEST = "IIS-WEBREQUEST-2";

  public static final String INDEX_HEAD_REQUEST = "IIS-WEBREQUEST-3";

  public static final String INDEX_OTHER_REQUEST = "IIS-WEBREQUEST-4";

  public static final String INDEX_RUNTIME = "IIS-WEBREQUEST-5";

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
          CLIConstant.COMMAND_IISWEBREQUEST);
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
      result.setResultDesc("解析IISWeb请求信息采集结果失败：" + e.getMessage());
      logger.error("解析IISWeb请求信息采集结果失败.@" + context.getNode().getIp(), e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (Throwable e) {
      String msg = e.getMessage();
      if (StringUtils.contains(msg, "RPC 服务器不可用") || StringUtils.contains(msg, "RPC server is unavailable")) {
        result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        return result;
      }
      result.setResultDesc("获取IISWeb请求信息失败:" + e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    List<Properties> webRes = cliResult.getListTableResult();

    List<Properties> realWebRes = getRealData(webRes, context, tempData);

    tempData.getDatas().clear();
    tempData.getDatas().addAll(webRes);

    for (Properties properties : realWebRes) {
    	String webName = (String) properties.get("Name");
    	MonitorResultRow row = new MonitorResultRow(webName);
    	
      String getRequestsPersec = (String) properties.get("GetRequestsPersec");
      row.setIndicator(INDEX_GET_REQUEST, Integer.parseInt(getRequestsPersec));

      String headRequestsPersec = (String) properties.get("HeadRequestsPersec");
      row.setIndicator(INDEX_HEAD_REQUEST, Integer.parseInt(headRequestsPersec));

      String postRequestsPersec = (String) properties.get("PostRequestsPersec");
      row.setIndicator(INDEX_POST_REQUEST, Integer.parseInt(postRequestsPersec));

      String otherRequestMethodsPersec = (String) properties.get("OtherRequestMethodsPersec");
      row.setIndicator(INDEX_OTHER_REQUEST, Integer.parseInt(otherRequestMethodsPersec));

      String serviceUptime = (String) properties.get("ServiceUptime");
      row.setIndicator(INDEX_RUNTIME, Double.parseDouble(serviceUptime));
      
      result.addRow(row);
    }

    return result;
  }

  /**
   * 根据上次保存的性能值计算真实的流量
   * 
   * @param webRes
   *          当前实时获取到的数据
   * @param srv
   * @return
   */
  private List<Properties> getRealData(List<Properties> webRes, CollectContext context, IISTempData lastData) {
    List<Properties> realData = new ArrayList<Properties>();
    for (int index = 0; index < webRes.size(); index++) {
      Properties properties = (Properties) webRes.get(index);
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
      newProp.setProperty("ServiceUptime", (String) properties.get("ServiceUptime"));
      if (Last_data == null) {
        // 没有历史数据则直接获取这次的数据,并设置相关值为0
        newProp.setProperty("GetRequestsPersec", "0");
        newProp.setProperty("HeadRequestsPersec", "0");
        newProp.setProperty("OtherRequestMethodsPersec", "0");
        newProp.setProperty("PostRequestsPersec", "0");
      } else {
        // 有历史数据的情况下计算真实数据
        double last_uptime = Double.parseDouble((String) Last_data.get("ServiceUptime"));
        double realGet = 0;
        double realPost = 0;
        double realHead = 0;
        double realOther = 0;

        double per_get_request = Double.parseDouble((String) Last_data.get("GetRequestsPersec"));
        double per_post_request = Double.parseDouble((String) Last_data.get("PostRequestsPersec"));
        double per_head_request = Double.parseDouble((String) Last_data.get("HeadRequestsPersec"));
        double per_other_request = Double.parseDouble((String) Last_data.get("OtherRequestMethodsPersec"));        

        realGet = (Double.parseDouble((String) properties.get("GetRequestsPersec")) - (Double
            .parseDouble((String) Last_data.get("GetRequestsPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        // 如果数据没有发生变化或则不合理则取上一次保存的性能数据
        if (realGet == 0 || Double.isNaN(realGet) || realGet < 0) {
          realGet = per_get_request;
        }

        realPost = (Double.parseDouble((String) properties.get("PostRequestsPersec")) - (Double
            .parseDouble((String) Last_data.get("PostRequestsPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        if (realPost == 0 || Double.isNaN(realPost) || realPost < 0) {
          realPost = per_post_request;
        }

        realHead = (Double.parseDouble((String) properties.get("HeadRequestsPersec")) - (Double
            .parseDouble((String) Last_data.get("HeadRequestsPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        if (realHead == 0 || Double.isNaN(realHead) || realPost < 0) {
          realHead = per_head_request;
        }

        realOther = (Double.parseDouble((String) properties.get("OtherRequestMethodsPersec")) - (Double
            .parseDouble((String) Last_data.get("OtherRequestMethodsPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        if (realOther == 0 || Double.isNaN(realOther) || realPost < 0) {
          realOther = per_other_request;
        }

        newProp.setProperty("PostRequestsPersec", String.valueOf(new Double(realPost).intValue()));
        newProp.setProperty("GetRequestsPersec", String.valueOf(new Double(realGet).intValue()));
        newProp.setProperty("HeadRequestsPersec", String.valueOf(new Double(realHead).intValue()));
        newProp.setProperty("OtherRequestMethodsPersec", String.valueOf(new Double(realOther).intValue()));
      }
      realData.add(newProp);
    }
    return realData;
  }
}
