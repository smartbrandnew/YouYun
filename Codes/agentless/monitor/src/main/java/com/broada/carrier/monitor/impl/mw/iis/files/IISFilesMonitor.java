package com.broada.carrier.monitor.impl.mw.iis.files;

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
 * IIS传输文件监测
 * 
 * @author 杨帆
 * 
 */
public class IISFilesMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(IISFilesMonitor.class);
  public static final String INDEX_SEND = "IIS-TRANSFERFILES-1";

  public static final String INDEX_RECEIVED = "IIS-TRANSFERFILES-2";

  public static final String INDEX_TOTAL = "IIS-TRANSFERFILES-3";

  public static final String INDEX_RUNTIME = "IIS-TRANSFERFILES-4";

  public static final String INDEX_NOT_FOUND_ERR = "IIS-TRANSFERFILES-5";

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
          CLIConstant.COMMAND_IISTRANSFERFILES);
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
      result.setResultDesc("解析IIS传输文件采集结果失败：" + e.getMessage());
      logger.error("解析IIS传输文件采集结果失败.@" + context.getNode().getIp(), e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (Throwable e) {
      String msg = e.getMessage();
      if (StringUtils.contains(msg, "RPC 服务器不可用") || StringUtils.contains(msg, "RPC server is unavailable")) {
        result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        return result;
      }
      result.setResultDesc("获取IIS传输文件失败:" + e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    List<Properties> files = cliResult.getListTableResult();

    List<Properties> realFiles = getRealData(files, context, tempData);

    tempData.getDatas().clear();
    tempData.getDatas().addAll(files);

    for (Properties properties : realFiles) {
    	String webName = (String) properties.get("Name");
    	MonitorResultRow row = new MonitorResultRow(webName);
    	
      String filesReceivedPersec = (String) properties.get("FilesReceivedPersec");
      row.setIndicator(INDEX_RECEIVED, Integer.parseInt(filesReceivedPersec));

      String filesSentPersec = (String) properties.get("FilesSentPersec");
      row.setIndicator(INDEX_SEND, Integer.parseInt(filesSentPersec));

      String filesTotalPersec = (String) properties.get("FilesPersec");
      row.setIndicator(INDEX_TOTAL, Integer.parseInt(filesTotalPersec));

      String serviceUptime = (String) properties.get("ServiceUptime");
      row.setIndicator(INDEX_RUNTIME, Double.parseDouble(serviceUptime));

      String totalNotFoundErrors = (String) properties.get("TotalNotFoundErrors");
      row.setIndicator(INDEX_NOT_FOUND_ERR, Double.parseDouble(totalNotFoundErrors));

      result.addRow(row);
    }

    return result;
  }

  /**
   * 根据上次保存的性能值计算真实的流量
   * 
   * @param files
   *          当前实时获取到的数据
   * @param srv
   * @return
   */
  private List<Properties> getRealData(List<Properties> files, CollectContext context, IISTempData lastData) {
    List<Properties> realData = new ArrayList<Properties>();
    for (int index = 0; index < files.size(); index++) {
      Properties properties = (Properties) files.get(index);
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
      newProp.setProperty("TotalNotFoundErrors", (String) properties.get("TotalNotFoundErrors"));
      if (Last_data == null) {
        // 没有历史数据则直接获取这次的数据,并设置相关值为0
        newProp.setProperty("FilesReceivedPersec", "0");
        newProp.setProperty("FilesSentPersec", "0");
        newProp.setProperty("FilesPersec", "0");
      } else {
        // 有历史数据的情况下计算真实数据
        double last_uptime = Double.parseDouble((String) Last_data.get("ServiceUptime"));
        double realFilesSentPersec = 0;
        double realFilesReceivedPersec = 0;
        double realFilesPersec = 0;

        double per_received = Double.parseDouble((String) Last_data.get("FilesSentPersec"));
        double per_send = Double.parseDouble((String) Last_data.get("FilesReceivedPersec"));
        double per_total = Double.parseDouble((String) Last_data.get("FilesPersec"));
       
        realFilesSentPersec = (Double.parseDouble((String) properties.get("FilesSentPersec")) - (Double
            .parseDouble((String) Last_data.get("FilesSentPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        // 如果数据没有发生变化或则不合理则取上一次保存的性能数据
        if (realFilesSentPersec == 0 || Double.isNaN(realFilesSentPersec) || realFilesSentPersec < 0) {
          realFilesSentPersec = per_send;
        }

        realFilesReceivedPersec = (Double.parseDouble((String) properties.get("FilesReceivedPersec")) - (Double
            .parseDouble((String) Last_data.get("FilesReceivedPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        if (realFilesReceivedPersec == 0 || Double.isNaN(realFilesReceivedPersec) || realFilesReceivedPersec < 0) {
          realFilesReceivedPersec = per_received;
        }

        realFilesPersec = (Double.parseDouble((String) properties.get("FilesPersec")) - (Double
            .parseDouble((String) Last_data.get("FilesPersec"))))
            / (Double.parseDouble((String) properties.get("ServiceUptime")) - last_uptime);

        if (realFilesPersec == 0 || Double.isNaN(realFilesPersec) || realFilesPersec < 0) {
          realFilesPersec = per_total;
        }
        newProp.setProperty("FilesReceivedPersec", String.valueOf(new Double(realFilesReceivedPersec).intValue()));
        newProp.setProperty("FilesSentPersec", String.valueOf(new Double(realFilesSentPersec).intValue()));
        newProp.setProperty("FilesPersec", String.valueOf(new Double(realFilesPersec).intValue()));
      }
      realData.add(newProp);
    }
    return realData;
  }
}
