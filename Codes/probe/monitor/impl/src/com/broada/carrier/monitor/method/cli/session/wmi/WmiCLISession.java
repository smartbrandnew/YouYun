package com.broada.carrier.monitor.method.cli.session.wmi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.session.AbstractCLISession;
import com.broada.core.queue.ProcessRentManager;

/**
 * vbs脚本文件以"cscript ${base}/conf/cli-config/script/files.vbs addr[ name passwd]"的形式被执行， 如果目标地址为本地，name,passwd参数不设置
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2007-12-25 下午05:25:33
 */
public class WmiCLISession extends AbstractCLISession {
	private static final Log logger = LogFactory.getLog(WmiCLISession.class);

  private static String[] localHostAddr = null;

  private static String localHostName = null;

  private String addr = "";

  private String name = "";

  private String passd = "";
  
  private Boolean isLocalHost = null;

  /*
   * @see com.broada.carrier.monitor.method.cli.session.CLISession#close()
   */
  public void close() {
  }

  /*
   * 
   * @see com.broada.carrier.monitor.method.cli.session.CLISession#execCmd(java.lang.String, java.lang.String[], java.lang.String, java.lang.StringBuffer, boolean)
   */
  public String execCmd(String cmd, String[] args,String prompt,StringBuffer localBuf,boolean isLogErr) throws CLIException {
    init();
    // 命令字符串构建
    String result = executeCmd(cmd, args);
    localBuf.append(result);
    return result; 
  }

  /*
   * @see com.broada.carrier.monitor.method.cli.session.CLISession#execScript(java.lang.String, java.lang.String[])
   */
  public String execScript(String scriptFile, String[] args) throws CLIException {
    return null;
  }

  /*
   * 
   * @see com.broada.carrier.monitor.method.cli.session.CLISession#open(java.util.Properties, boolean)
   */
  public void open(Properties options,boolean isLogErr) throws CLILoginFailException, CLIConnectException {
    isLocalHost = null;
    addr = options.getProperty(CLIConstant.OPTIONS_REMOTEHOST);
    name = options.getProperty(CLIConstant.OPTIONS_LOGINNAME);
    passd = options.getProperty(CLIConstant.OPTIONS_PASSD);
  }

  /**
   * 只返回存在文件的文件信息
   * 
   * @param cmd
   * @param params
   * @return
   * @throws CLIException
   */
  private String executeCmd(String cmd, String[] params) throws CLIException {
    String fileName = cmd;
    int fnIdx = cmd.lastIndexOf("/");
    if (fnIdx >= 0) {
      fileName = cmd.substring(fnIdx + 1);
    }
    StringBuffer cmdBuf = new StringBuffer();
    cmdBuf.append(cmd).append(" ").append(addr);
    if (isLocalHost(addr)) {
      cmdBuf.append(" true");
    } else {
      if (name == null || name.trim().length() < 1)
        throw new CLIException("登录用户名不可以为空");
      
      String osUsername = System.getProperty("user.name");
      if (osUsername != null && osUsername.toLowerCase().equals("system") && name.indexOf('\\') < 0) {      	
      	name = addr + "\\" + name;       	
      	if (logger.isDebugEnabled())
      		logger.debug("由于在windows system用户下执行，因此WMI目标脚本修改为：" + name);
      }
      	
      cmdBuf.append(" false").append(" ").append(name).append(" ").append(passd);
    }
    if (params != null) {
      for (int i = 0; i < params.length; i++) {
        String param = params[i];
        cmdBuf.append(" \"").append(param).append("\"");
      }
    }
    Process process = null;

    synchronized (Runtime.getRuntime()) {
      try {
      	if (logger.isDebugEnabled())
      		logger.debug("准备执行WMI CLI命令：" + cmdBuf.toString());      	
        process = ProcessRentManager.rent(cmdBuf.toString(),600000);
//        process.waitFor();
      } catch (Exception e) {
        throw new CLIException("执行命令[" + cmd + "]失败.", e);
      }
    }
      BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      StringBuffer resultBuf = new StringBuffer();
      try {
      	String errorText = null;
        String tmp = null;
        int idx = 0;
        while ((tmp = bufferedreader.readLine()) != null) {
        	if (logger.isDebugEnabled()) 
        		logger.debug("命令输出：" + tmp);  
          if (errorText == null && tmp.indexOf("Error #") != -1)
          	errorText = tmp.substring(tmp.indexOf("Error #") + 7);            
          idx++;
          if (idx <= 3 || tmp.trim().length() < 1) {
            continue;
          }
          if (resultBuf.length() > 0)
            resultBuf.append("\n");
          resultBuf.append(tmp);
        }
        if (errorText != null)
        	throw new CLIException(errorText);
        if (resultBuf.length() < 1)
          return null;
        if (resultBuf.toString().indexOf("Script execution time was exceeded") == -1) {
          return resultBuf.toString();
        } else {// 超时处理
          throw new CLIException("在主机" + addr + "上执行脚本文件" + fileName + "超时.");
        }
      } catch (IOException e) {
        throw new CLIException("读取脚本" + fileName + "执行结果失败.", e);
      } finally {
        try {
          bufferedreader.close();
        } catch (IOException e) {
          logger.debug("输入流关闭异常", e); 
        }
        ProcessRentManager.revert(process);
      }
  }

  private void init() {
    try {
      if (localHostName == null) {
        localHostName = InetAddress.getLocalHost().getHostName();
      }
      if (localHostAddr == null) {
        InetAddress[] addrs = (InetAddress[]) InetAddress.getAllByName(localHostName);
        String[] ips = new String[addrs.length];
        for (int i = 0; i < addrs.length; i++)
          ips[i] = addrs[i].getHostAddress();
        localHostAddr = ips;
      }
    } catch (UnknownHostException ee) {
      logger.debug("获取localHostName与localHostAddr异常，localHostName=" + localHostName + ",localHostAddr=" + localHostAddr, ee); 
      localHostAddr = null;
      localHostName = null;
    }
  }

  /**
   * 目标地址为本机确认
   * 
   * @param addr
   * @return
   */
  private boolean isLocalHost(String addr) {
    if (isLocalHost == null) {
      if (localHostAddr != null) {
        for (String a : localHostAddr) {
          if (a.equals(addr)) {
            isLocalHost = Boolean.TRUE;
            break;
          }
        }
      }
      if (isLocalHost == null && localHostName != null && localHostName.equals(addr)) {
        isLocalHost = Boolean.TRUE;
      }
      if (isLocalHost == null && HostIpUtil.getLocalHost().equals(addr)) {
        isLocalHost = Boolean.TRUE;
      }
      if (isLocalHost == null) {
        isLocalHost = Boolean.FALSE;
      }
    }
    return isLocalHost.booleanValue();
  }

	@Override
	public boolean isStanding() {
		return true;
	}
}
