package com.broada.carrier.monitor.impl.stdsvc.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.SocketException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * FTP 监听器实现类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class FTPMonitor implements Monitor {

  private static final Log logger = LogFactory.getLog(FTPMonitor.class);

  public static final String ANONYMOUS_PASS = "anonymous";

  private static final String ITEMKDX_REPLYTIME = "FTP-1";// 响应时间

  public FTPMonitor() {
  }

  /**
   * 实现监测
   *
   * 使用sun未公开的sun.net.ftp.FtpClient类的相关方法测试
   * 该类一定包含于JDK中了
   *
   * @param srv
   * @return
   */
  public MonitorResult monitor(MonitorContext context) {
    MonitorResult result = new MonitorResult();
    result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);
    String param = context.getTask().getParameter();
    String ip = context.getNode().getIp();
    FTPParameter p = new FTPParameter(param);

    int port = p.getPort();

    result.setState(MonitorConstant.MONITORSTATE_FAILING);

    /*
		 * socket的连接超时有两个,一个是读写超时SO_TIMEOUT,另一个是连接超时,SocketTimeoutException,
		 * 前者指的是连接后等待了一段时间,还没有可供读取的内容,就会放弃,不再等待,同时抛出ReadTimeOutExcetpion
		 * 后者是指在在指定时间内找不到主机或是无法与制定的端口建立Socket连接,就会抛出SocketTimeoutException,放弃连接
		 * 覆写这个方法,是为了配置defaultSoTimeout这个变量, 该变量的作用就是设置io读写超时
		 */
		FTPClient ftp = new FTPClient() {
			@Override
			public void connect(String hostname, int port) throws SocketException, IOException {
				super.connect(hostname, port);
				// 只有连接后才能设置so_timeout
				setSoTimeout(6000);
			}
		};
		ftp.setAutodetectUTF8(true);
		ftp.setControlEncoding("GBK");
		
    long replyTime = 0;//响应时间
    try {
      long time = System.currentTimeMillis();
      ftp.connect(ip, port);
      replyTime = System.currentTimeMillis() - time;
      if(replyTime<=0){
        replyTime=1;
      }
      result.setResponseTime((int)replyTime);
      //    modify by yangf 修改响应时间为毫秒而不是秒 
      //      replyTime = replyTime / 1000;
    } catch (IOException ex) {
      //异常表示服务没法连接    	
      result.setResultDesc("无法连接到FTP端口:" + port);
      close(ftp);
      return result;
    }

    StringBuffer msg = new StringBuffer(); //监测结果信息描述    
    boolean wonted = true;
    boolean anonymousLogin = false; //标识匿名登录是否成功

    //判断是否检查匿名登录,和监测匿名
    if (p.isAnonymous() || p.isNotAnonymous()) {
      try {
        if (!ftp.login("anonymous", ANONYMOUS_PASS))
        	throw new IOException("用户名或密码错误");
        wonted = p.isAnonymous();
        anonymousLogin = true;
        close(ftp);
      } catch (IOException ex) {
        wonted = p.isNotAnonymous();
        msg.append("匿名登录失败；");        
      }
    }

    result.addPerfResult(new PerfResult(ITEMKDX_REPLYTIME, replyTime));
    if (p.isChkReplyTime()) {
      if (replyTime <= p.getReplyTime()) {
        wonted = wonted && true;
      } else {
        wonted = wonted && false;
        msg.append("服务响应时间超阈值，当前" + replyTime + "毫秒>" + p.getReplyTime() + "毫秒；");
      }
    }

    //校验登录
    if (p.isChkLogin()) {
      if (anonymousLogin) { //与服务器的连接已经断开,重新连接
        try {
          ftp.connect(ip, port);
        } catch (IOException ex) {
          //异常表示服务没法连接
          result.setState(MonitorConstant.MONITORSTATE_FAILING);
          result.setResultDesc("无法连接到FTP端口:" + port);
          close(ftp);
          return result;
        }
      }
      try {
        if (!ftp.login(p.getUser(), p.getPassword()))
        	throw new IOException("用户名或密码错误");
        wonted = wonted && true;
      } catch (IOException err) {
				wonted = wonted && false;
				String errMsg = "用户登录失败.";
				logger.warn(String.format("%s错误：%s", errMsg, err));
				logger.debug("堆栈：", err);

				msg.append("用户\"" + p.getUser() + "\"登录失败；");
      }
    }

    //校验文件是否存在
    if (p.isChkFile()) {
      try {      	
        InputStream is = ftp.retrieveFileStream(p.getFilename());
        if (is == null) {
          throw new IOException("File Not Found!");
        }
        is.close();
        wonted = wonted && true;
      } catch (IOException ex) {
        wonted = wonted && false;
        msg.append("文件\"" + p.getFilename() + "\"不存在；");
      }
    }

    if (wonted) {
      result.setState(MonitorConstant.MONITORSTATE_NICER);
    } else {
      result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
      result.setResultDesc(msg.toString());
    }
    close(ftp);
    return result;
  }
  
  /**
   * 关闭连接
   * @param ftp
   */
	private void close(FTPClient ftp) {
		if (ftp != null) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException err) {
					String msg = "关闭FTP连接失败";
					logger.warn(String.format("%s。错误：%s", msg, err));
					logger.debug("堆栈：", err);
				}
			}
		}
  }

	@Override
	public Serializable collect(CollectContext context) {
		return null;
	}
}