package com.broada.carrier.monitor.impl.stdsvc.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * SMTP 服务监听器实现类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class SMTPMonitor implements Monitor {

  private static final Log logger = LogFactory.getLog(SMTPMonitor.class);

  private static final String ITEMKDX_REPLYTIME = "SMTP-1";// 响应时间

  public SMTPMonitor() {
  }

  /**
   * 实现SMTP监测
   *
   * 暂时使用Socket进行端口的连接测试
   * 然后根据SMTP协议，读取返回的第一行，看是否状态是220
   *
   * 返回结果包括监测参数，端口和结果信息等
   *
   * @param srv
   * @return
   */
  public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    
    String param = context.getTask().getParameter();
    String ip = context.getNode().getIp();
    SMTPParameter p = new SMTPParameter(param);
    int port = p.getPort();
    int timeout = p.getTimeout();

    long replyTime = getReplyTime(ip, port, timeout, p);
    
    PerfResult perfRespTime = new PerfResult(ITEMKDX_REPLYTIME, false);
    PerfResult[] perfs = { perfRespTime };
    result.setPerfResults(perfs);
    if (replyTime <=0) {//若不大于0说明不能连接到服务器或者服务停止
      result.setState(MonitorConstant.MONITORSTATE_FAILING);
      result.setResultDesc("不能连接到目标SMTP服务.");
      perfRespTime.setValue(0);
      return result;
    }else{
      result.setResponseTime(replyTime);
      //转换为秒
      double respTime=((double)replyTime)/1000;
      perfRespTime.setValue(respTime);
      result.setState(MonitorConstant.MONITORSTATE_NICER);
      if (p.isChkReplyTime() && respTime>p.getReplyTime()) {
        result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
        result.setResultDesc("成功连接服务器,响应时间" + respTime + "秒 > " + p.getReplyTime() + "秒.\n");
      }else{
        result.setResultDesc("成功连接服务器,响应时间" + respTime + "秒.\n");
      }
      return result;
    }
  }

  /**
   * 测试服务是否运行,返回超时时间(单位毫秒),如果连接不成功则返回-1
   * @param ip
   * @param port
   * @param timeout
   * @param p
   * @return 超时时间(单位毫秒)
   */
  private long getReplyTime(String ip, int port, int timeout, SMTPParameter p) {
    Socket c = new Socket();
    BufferedReader reader = null;
    PrintWriter writer = null;
    boolean isConn = false;
    long replyTime = 0;// 响应时间

    try {
      long time = System.currentTimeMillis();
      c.setSoTimeout(timeout);
      c.connect(new InetSocketAddress(ip, port), timeout);
      reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
      writer = new PrintWriter(new OutputStreamWriter(c.getOutputStream()));
      String str = reader.readLine();
      replyTime = System.currentTimeMillis() - time;
      if(replyTime<=0){
        replyTime=1;
      }
      if (str != null && str.startsWith("220")) {
        isConn = true;
        writer.println("QUIT");
        writer.flush();
      }
    } catch (IOException ex) {
      logger.debug("输入输出流操作出错,连接失败", ex);
      isConn = false;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex1) {
          logger.debug("关闭输入流出错", ex1);
        }
      }
      if (writer != null) {
        writer.close();
      }
      if (c != null) {
        try {
          c.close();
        } catch (IOException ex2) {
          logger.debug("关闭套接字出错", ex2);
        }
      }
    }
    if (!isConn) {
      replyTime = -1;
    }
    return replyTime;
  }

	@Override
	public Serializable collect(CollectContext context) {
		return null;
	}
}