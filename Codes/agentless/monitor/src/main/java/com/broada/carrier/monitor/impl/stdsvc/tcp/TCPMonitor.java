package com.broada.carrier.monitor.impl.stdsvc.tcp;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.common.entity.RunState;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * TCP 端口监听器实现类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */
public class TCPMonitor implements Monitor {
  private static final String ITEM_PORTSTATE = "TCP-1";

  private static final String ITEM_RESPONSETIME = "TCP-2";

  public TCPMonitor() {
  }

  /**
   * 实现监测
   *
   * 使用Socket进行Tcp端口的连接测试
   *
   * 以此判断指定IP的指定端口是否打开
   *
   * 返回结果包括监测参数，端口和结果信息等
   *
   *
   * @param srv
   * @return
   */
  public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    String param = context.getTask().getParameter();
    String ip = context.getNode().getIp();

    TCPParameter p = new TCPParameter(param);
    List<?> conditions = p.getConditions();

    StringBuffer msg = new StringBuffer();
    StringBuffer curr = new StringBuffer();
    int portCount = conditions.size();
    long totalRespTime = 0;//总响应时间
    int canConnCount = 0;//可连接的端口数

    boolean portState = true;
    for (int index = 0; index < portCount; index++) {
      TCPMonitorCondition condition = (TCPMonitorCondition) conditions.get(index);
      long respTime = System.currentTimeMillis();
      boolean isCanConnect = tryConnect(ip, condition);
      respTime = System.currentTimeMillis() - respTime;
      if (isCanConnect) {
        if (respTime <= 0) {
          respTime = 1;
        }
        totalRespTime += respTime;
        canConnCount++;
      } else {
        //如果不能连通则respTime=0
        respTime = 0;
      }
      int port = setResult(msg, curr, condition, isCanConnect);
      if (port > 0) {
        portState = false;
      }
      //端口状态      
      PerfResult perf = new PerfResult(ITEM_PORTSTATE, true);
      perf.setValue(isCanConnect ? RunState.RUNNING : RunState.STOP);
      perf.setInstanceKey(condition.getField());
      result.addPerfResult(perf);
      
      //响应时间
      perf = new PerfResult(ITEM_RESPONSETIME, true);
      perf.setValue(respTime);
      perf.setInstanceKey(condition.getField());
      
      result.addPerfResult(perf);
    }

    if (!portState) {
      result.setResultDesc(msg.toString());
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      if (portCount != canConnCount) {
        result.setState(MonitorConstant.MONITORSTATE_FAILING);
      }
    } else {
      result.setState(MonitorConstant.MONITORSTATE_NICER);
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

  /**
   * 根据每个监测条件得到的结果设置返回值
   * @param msg
   * @param curr
   * @param condition
   * @param isCanConnect
   * @return 如果端口不符合设定条件则返回端口号，否则返回-1
   */
  private int setResult(StringBuffer msg, StringBuffer curr, TCPMonitorCondition condition, boolean isCanConnect) {
    int port = condition.getPort();
    if ((condition.isUp() && !isCanConnect) || (!condition.isUp() && isCanConnect)) {
    	if (isCanConnect)
    		msg.append("端口" + port + "处于开启状态；");
    	else
    		msg.append("端口" + port + "处于关闭状态；");      
      return port;
    }
    return -1;
  }

  /**
   * 尝试连接到指定端口
   * @param ip
   * @param condition
   * @return
   */
  private boolean tryConnect(String ip, TCPMonitorCondition condition) {
    int port = condition.getPort();
    int timeout = condition.getTimeout();
    int times = condition.getTimes();
    Socket socket = null;
    while (times >= 0) {
      try {
        socket = new Socket();
        socket.connect(new InetSocketAddress(ip, port), timeout * 1000);
        return true;
      } catch (IOException ex) {
      } finally {
        if (socket != null) {
          try {
            socket.close();
          } catch (IOException ex1) {
          }
        }
      }
      times--;
    }
    return false;
  }

	@Override
	public Serializable collect(CollectContext context) {
		return null;
	}
}