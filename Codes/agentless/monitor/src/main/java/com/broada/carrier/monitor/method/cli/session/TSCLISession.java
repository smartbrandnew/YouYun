package com.broada.carrier.monitor.method.cli.session;

import java.util.Properties;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.adventnet.cli.CLIMessage;
import com.adventnet.cli.CLIResourceManager;
import com.adventnet.cli.FixedCLISession;
import com.adventnet.cli.MaxConnectionException;
import com.adventnet.cli.transport.CLIProtocolOptions;
import com.adventnet.cli.transport.ConnectException;
import com.adventnet.cli.transport.LoginException;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;
import com.broada.carrier.monitor.method.cli.error.CLIWaitResponseTimeoutException;
import com.broada.carrier.monitor.method.cli.parser.DefaultCLIParser;
import com.broada.utils.TextUtil;

/**
 * Telnet和SSH的实现
 * 
 * @author zhoucy (zhoucy@broada.com.cn)
 * @Email : zhoucy@broada.com
 * @Create By 2006-8-17 上午09:47:32
 * huangjb 2007/11/29  添加同步发送执行命令返回结果为null的日志记录
 * huangjb 2007/12/13  添加CLIMessage发送执行超时时间设置为1分钟以及命令不充分执行记录日志动作
 * panlx 2008/12/3 修改执行命令的方法，之前只能是将参数依次添加到命令行末尾，现允许参数放置在命令行中间。格式如： xxxx ${0} xxxx ${1} xxxxx。
 * maico 2011/10/13 修改execCmd和open加入isLogErr参数，可设置是否log错误信息，用于log的控制
 */
@Deprecated
public abstract class TSCLISession extends AbstractCLISession {
  private static final Log logger = LogFactory.getLog(TSCLISession.class);

  protected CLIProtocolOptions cpo = null;

  protected FixedCLISession clisession = null;
  
  protected String fullPrompt = null;
  /*
   * 
   * @see com.broada.carrier.monitor.method.cli.session.CLISession#open(java.util.Properties, boolean)
   */
  public void open(Properties options,boolean isLogErr) throws CLILoginFailException, CLIConnectException {
    fullPrompt = null;
    this.options = options;
    Properties noPasswdOpts = null;
    try {
      //modify by huangjb 2008/04/14 添加保存一个没有密码信息的属性,用于异常时输出
      noPasswdOpts = (Properties) options.clone();
      if (noPasswdOpts.containsKey("password"))
        noPasswdOpts.remove("password");
      PropertyUtils.copyProperties(cpo, options);
    } catch (Exception e) {
      logger.error("设置参数发生错误:" + noPasswdOpts, e);
      throw new CLIRuntimeException("设置参数发生错误", e);
    }
    CLIResourceManager rm = null;
    try {
      if (clisession != null) {
        close();
      }
      clisession = new FixedCLISession(cpo, false);
      clisession.setTransportProviderClassName(getTransportClassName());
      clisession.setIgnoreSpecialCharacters(true);
      //clisession.setPooling(true);
      clisession.setMaxConnections(100);
      rm = clisession.getResourceManager();
      if (rm != null) {
        rm.setMaxConnections(100);
        rm.setSystemWideMaxConnections(100);
      }
      //logger.info("~~~~~~>clisession.open(): " + clisession.hashCode());
      clisession.open();
      // 从登陆信息中获取完整的命令行提示符
      if (isLogErr && options.get("doTest") != null) {
        fullPrompt = ProtocolUtils.getPromptFromLoginMessage(clisession.getCLIProtocolOptions().getInitialMessage());
        if (fullPrompt != null)
          options.put("fullPrompt", fullPrompt);
      }
    } catch (LoginException le) {
      close();
      if(isLogErr){
      	logger.error("登录失败." + noPasswdOpts, le);
      }
      throw new CLILoginFailException("登录目标系统失败,请检查命令提示符和用户名密码等信息.", le);
    } catch (ConnectException ce) {
      close();
      if(isLogErr){
      	logger.error("连接错误,请检查主机地址和端口." + noPasswdOpts, ce);
      }
      throw new CLIConnectException("连接错误,请检查主机地址和端口.", ce);
		} catch (MaxConnectionException me) {
			close();

			String msg = "连接失败,已经达到目标设备的最大连接数";
			if (rm != null) {
				int count = rm.getAliveConnectionsCount();
				msg += ",当前系统连接数为:" + count;
			}
			if (isLogErr) {
				logger.error(msg + "." + noPasswdOpts, me);
			}
			throw new CLIConnectException(msg, me);
		} catch (Throwable e) {
      close();
      if(isLogErr){
      	logger.error("登录时发生错误,请检查填写参数是否正确" + noPasswdOpts, e);
      }
      throw new CLILoginFailException("登录时发生错误,请检查填写参数是否正确.", e);
    }
  }
  
  /*
   * 
   * @see com.broada.carrier.monitor.method.cli.session.CLISession#execCmd(java.lang.String, java.lang.String[], java.lang.String, java.lang.StringBuffer, boolean)
   */
  public String execCmd(String cmd, String[] args,String prompt,StringBuffer localBuf,boolean isLogErr) throws CLIException {
    CLIMessage msg = new CLIMessage("");
    if (prompt == null || prompt.equalsIgnoreCase("default")) {
			msg.setCLIPrompt(getPrompt());
		} else {//在推送脚本的时候，交互的命令提示符(可修改配置文件来匹配)是 >
			msg.setCLIPrompt(prompt);
		}
    if (args != null) {
      String _cmd = TextUtil.matchReplace(cmd, TextUtil.PROP_NUM_PATTERN, args);
      if (!cmd.equals(_cmd)) { // 意味cmd中存在属性参数，并且替换成功
        cmd = _cmd;
      }else {
        for (int index = 0; index < args.length; index++) {
          cmd = cmd + " " + args[index];//把命令的参数加到命令末尾
        }
      }
    }
    /*
     * CLI API系统默认的执行时间为5秒钟,这时间可能对于有些命令的执行太短
     * 因此把执行超时时间设置为1分钟
     * modify by huangjb 2007/12/13 
     */
    msg.setRequestTimeout(60000);
    msg.setCommandEcho(false);
    /*
     * 由于有些机器配置的原因,通过AM的API获取的返回结果不包含命令提示符的情况下，
     * 最后一个的换行符已经被trim掉了，这时通过限定命令提示符回显来保证获取正确的数据
     * modify by huangjb 2008/03/11
     */
    msg.setPromptEcho(true);
    msg.setCLIProtocolOptions(cpo);
    long startTime = 0;
    if (logger.isDebugEnabled()) {
    	startTime = System.currentTimeMillis();
      logger.debug("执行命令开始[" + cmd.hashCode() + "]:" + cmd);
    }

    String[] cmdArray = cmd.split("\n");
    String message = "";
    /*
     * 测试发现telnet方式发送带有下划线(_)的命令到服务端执行的时候,am的api在处理返回数据时会有乱码产生
     * 即使是在设置CLIMessage.setCommandEcho(false)的情况下,也无法过滤掉发送的命令
     * 这样我们通过将显示的将命令回显回来，通过换行符 来取真正的数据
     * 注意：这样也只能处理 发送的命令只有一行的情况
     */
		if (cmdArray.length == 1) {
			msg.setCommandEcho(true);
		}
    try {
      for (int index = 0; index < cmdArray.length; index++) {
        msg.setData(cmdArray[index]);
        //logger.info("~~~~~~>clisession.syncSend(msg): " + clisession.hashCode());
        CLIMessage result = clisession.syncSend(msg);
        if (result != null) {
        	String reponseData = result.getData();
        	DefaultCLIParser.getInstance().messageLocalized(reponseData,localBuf);
          message = message + reponseData;
          /*
           * 基于对CLI的api不太信任,在这里添加对取回的结果是否完整进行判断,
           * 并记录到log日志,以便改进分析
           * modify by huangjb 2007/12/13
           */
          if (result.getPartialResponse())
            logger.warn(cmd + ":命令未执行完全,只取得部分反馈结果!");
        } else {
          // add by huangjb 2007/11/29 为了记录有可能出现返回结果为空指针的情况
          logger.error("执行命令:" + cmd + "的返回结果result为null");
        }
      }
    } catch (MaxConnectionException e) {
    	if(isLogErr){
    		logger.error("执行命名" + cmd + "时到达最大连接数:" + options, e);
    	}
      throw new CLIConnectException("已经到达系统允许的最大连接数.", e);
    } catch (LoginException le) {
    	if(isLogErr){
    		logger.error("执行命令" + cmd + "时登录失败" + options, le);
    	}
      throw new CLILoginFailException("登录目标节点失败.", le);
    } catch (ConnectException ce) {
    	if(isLogErr){
    		logger.error("执行命令" + cmd + "时连接错误,请检查主机地址和端口" + options, ce);
    	}
      throw new CLIConnectException("连接错误,请检查主机地址和端口.", ce);
    } catch (Exception e) {
    	if(isLogErr){
    		logger.error("执行命令" + cmd + "时发生异常:" + options, e);
    	}
    	if (e instanceof NullPointerException) {
    		StackTraceElement[] stack = e.getStackTrace();
    		if (stack != null && e.getStackTrace().length > 0) {
    			if (stack[0].getClassName().equals("com.adventnet.cli.CLISession") && stack[0].getMethodName().equals("truncate"))
    				throw new CLIWaitResponseTimeoutException(String.format("执行命令[%s]时等待结果超时", cmd), e);
    		}
    	}
      throw parserException(e);
    } finally {
      if (logger.isDebugEnabled()) {
        logger.debug(String.format("执行命令完成[%d]，耗时[%dms]", cmd.hashCode(), System.currentTimeMillis() - startTime));
      }
    }

    if (message == null || message.length() == 0) {
      logger.error("暂时取不到数据:" + options);
      throw new CLIException("暂时取不到数据");
    }
    message = message.trim();
    if (cmdArray.length == 1) {
    	int firstRet = message.indexOf("\n");
    	if(firstRet == -1){
    		logger.warn(cmd + ":命令执行没有回显回来,返回结果如下：" + message);
    	} else {
    	  // 如果返回的结果中不包含命令行，则不去掉第一行。
    	  if (!ProtocolUtils.containsIgnoreWhiteSpaces(message.substring(0, firstRet), cmdArray[0]))
    	    firstRet = -1;
    	}
    	message = message.substring(firstRet + 1);
		}
    int lastRet = message.lastIndexOf("\n", message.length());
    String retStr = message.substring(0, lastRet == -1 ? 0 : lastRet);
    if (logger.isDebugEnabled()){
      logger.debug("执行Shell返回结果:" + retStr);
    }
    return retStr;
  }

  /**
   * @param scriptFile
   * @param args
   * @pdOid c03847d7-88f3-4170-a17a-9d4351a355ac
   */
  public String execScript(String scriptFile, String[] args) throws CLIException {
    throw new UnsupportedOperationException(this.getClass().getName() + "不支持execScript方法。");
  }

  /**
   * 关闭连接
   */
  public void close() {
    if (logger.isDebugEnabled()) {
      logger.debug("开始关闭Session.");
    }
    try {
      if (clisession != null) {
        if (logger.isDebugEnabled()) {
          logger.debug("关闭Session:" + clisession.getName());
        }
        clisession.close();
      }
    } catch (Exception e) {
      logger.error("关闭连接发生错误", e);
    }
    clisession = null;
  }
  
  /**
   * 获取完整的命令行提示符
   * 
   * @return
   */
  public String getFullPrompt() {
    return fullPrompt;
  }

  /**
   * 对一些异常进行转换处理，主要因为有些异常只能通过异常的消息进行分辨
   * @param e
   * @return CLIException 转换后的CLI异常
   * 
   * MDF BY panghf 2011-10-12 修改为通过返回获得转换后的异常，而不是直接跑出异常
   */
  protected abstract CLIException parserException(Throwable e);

  protected abstract String getTransportClassName();

  private String getPrompt() {
    try {
      return (String) PropertyUtils.getProperty(cpo, "prompt");
    } catch (Exception e) {
      logger.error("获取Prompt发生错误", e);
      throw new CLIRuntimeException("获取Prompt发生错误", e);
    }
  }
  
  @Override
  public boolean isStanding() {
  	return true;
  }
  
}
