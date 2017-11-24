package com.broada.carrier.monitor.impl.mw.iis.bytes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.PerfResultUtil;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.utils.SimpleParams;

/**
 * IIS 传输字节监测
 * 
 * @author 杨帆
 * 
 */
public class IISBytesMonitor implements Monitor {
	private static final Logger logger = LoggerFactory.getLogger(IISBytesMonitor.class);
  private static Map<String, Map<String, AppTranSummaryRecord>> lastMonitorRecords = new HashMap<String, Map<String, AppTranSummaryRecord>>();

  @Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(new CollectContext(context), context.getTask().getId());
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect(context, "-1");
	}

	private MonitorResult collect(CollectContext context, String taskId) {
    MonitorResult result = new MonitorResult();
    result.setState(MonitorConstant.MONITORSTATE_NICER);
    Map<String, AppTranSummaryRecord> nowRecords = null;
    try {
      long replyTime = System.currentTimeMillis();
    	nowRecords= getRecords(context.getNode(), taskId, context.getMethod());
      replyTime = System.currentTimeMillis() - replyTime;
      if (replyTime <= 0)
        replyTime = 1L;
      result.setResponseTime(replyTime);
    } catch (CollectException e) {
      result.setResultDesc("解析IIS传输字节采集结果失败：" + e.getMessage());
      logger.error("解析IIS传输字节采集结果失败.@" + context.getNode().getIp(), e);
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    } catch (Throwable e) {
      String msg = e.getMessage();
      if (StringUtils.contains(msg, "RPC 服务器不可用") || StringUtils.contains(msg, "RPC server is unavailable")) {
        result.setResultDesc("无法连接目标服务器，可能目标服务器没开启或者网络断开或者未启用RPC服务.");
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
        return result;
      }
      result.setResultDesc("获取IIS传输字节失败:" + e.getMessage());
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      return result;
    }
    
    Map<String, AppTranSummaryRecord> lastRecords = lastMonitorRecords.get(taskId);
    if (taskId!=null&&taskId.trim().length()>0)
    	lastMonitorRecords.put(taskId, nowRecords);
    
    if (nowRecords != null) {
	    for(AppTranSummaryRecord record : nowRecords.values()) {
	    	MonitorResultRow row = new MonitorResultRow(record.getName());
	    	AppTranSpeedRecord speed = new AppTranSpeedRecord(lastRecords == null ? null : lastRecords.get(row.getInstCode()), record);
	    	PerfResultUtil.fill(row, speed);
	    	result.addRow(row);
	    }
    }

    return result;
  }
  
	private Map<String, AppTranSummaryRecord> getRecords(MonitorNode node, String srvId, MonitorMethod method) throws CollectException {
    CLIResult cliResult = null;
    try {
      cliResult = new CLIExecutor(srvId).execute(node, method,
          CLIConstant.COMMAND_IISTRANSFERBYTES);
    } catch (CLILoginFailException fe) {
      throw new CollectException("登录目标服务器失败.", fe);
    } catch (CLIConnectException ce) {
      throw new CollectException("获取IIS传输字节信息失败,无法连接目标服务器.", ce);
    } catch (CLIResultParseException e) {
      if (logger.isErrorEnabled()) {
        logger.error("解析IIS传输字节信息采集结果失败.@" + node.getIp(), e);
      }
      throw new CollectException("获取IIS传输字节信息失败,解析采集结果失败.", e);
    } catch (Throwable e) {
      String message = e.getMessage();
      if(message==null)
        message = "获取IIS传输字节信息失败.";
      else
        message = "获取IIS传输字节信息失败：" + message;
      throw new CollectException(message, e);
    }
    
    Map<String, AppTranSummaryRecord> result = new HashMap<String, AppTranSummaryRecord>();
    List<?> rows = cliResult.getListTableResult();
    if (rows != null) {
	    for (Object obj : rows) {
	    	Properties props = (Properties) obj;
	    	SimpleParams params = new SimpleParams(props);
	    	AppTranSummaryRecord record = new AppTranSummaryRecord(
	    			params.checkString("Name"), 
	    			params.checkLong("ServiceUptime"),
	    			params.checkLong("BytesSentPersec"),
	    			params.checkLong("BytesReceivedPersec"));
	    	result.put(record.getName(), record);
	    }
    }
    return result;
	}
}
