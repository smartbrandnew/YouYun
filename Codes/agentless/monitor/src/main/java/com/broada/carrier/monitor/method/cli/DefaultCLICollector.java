package com.broada.carrier.monitor.method.cli;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.config.CLIConfigurationHandler;
import com.broada.carrier.monitor.method.cli.config.Category;
import com.broada.carrier.monitor.method.cli.config.Command;
import com.broada.carrier.monitor.method.cli.config.ShellInteract;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLINotFoundConfigException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.method.cli.error.CLIRuntimeException;
import com.broada.carrier.monitor.method.cli.parser.CLIParser;
import com.broada.carrier.monitor.method.cli.parser.DefaultCLIParser;
import com.broada.carrier.monitor.method.cli.session.CLISession;
import com.broada.carrier.monitor.method.cli.session.CLISessionFactory;
import com.broada.carrier.monitor.method.cli.session.CLISessionProxy;
import com.broada.utils.StringUtil;

/** 
 * CLI信息采集器实现类
 * @pdOid a07a09b5-13d2-4f38-b062-330b6e97f144
 * 
 * 
 */
public class DefaultCLICollector implements CLICollector {
	private static final Log logger = LogFactory.getLog(DefaultCLICollector.class);
	
	/**
	 * 是否加入会话缓存池
	 */
	private boolean cached = false;
	
	/**
	 * 配置文件处理器
	 */
	private static CLIConfigurationHandler cliConfigurationHandler;

	/**
	 * 采集结果解析
	 */
	private static CLIParser cliParser;

	/**
	 * 会话
	 */
	private CLISession cliSession;

	private Properties options = null;

	/**
	 * 当前session是否正在使用中
	 */
	private volatile int sessionCountInUse = 0;
	
	/**
	 * 当前session是否将被关闭
	 */
	private volatile boolean closing = false;
	
  /**
   * 当前session是否已经被关闭
   */
	private volatile boolean closed = false;
	
	static {
		try {
			cliConfigurationHandler = CLIConfigurationHandler.getInstance();
			cliParser = DefaultCLIParser.getInstance();
		} catch (Throwable e) {
			logger.warn("初始化失败", e);
		}
	}
	
	public DefaultCLICollector(boolean cached) {
		this.cached = cached;
	}
	
	/**
	 * 检查cliSession是否有效，无效则抛出CLIRun
	 * 
	 */
	protected void chkSession() {
		if (cliSession == null) {
			throw new CLIRuntimeException("CLI会话为空,请用init()方法并保证成功初始话.");
		}
	}

	@Override
	public CLIResult execute(String comandName, String[] args, AfterExecuteListener afterExecuteListener,boolean isLogErr)
			throws CLIResultParseException, CLIException {
	  sessionCountInUse++;
	  try {
  		/*
  		 * 获得系统版本
  		 */
  		String version = getSysVersion(options);
  
  		if (logger.isDebugEnabled()) {
  			logger.debug(String.format("%s系统版本号：%s", toStringSimple(), version));
  		}
  		/*
  		 * 获取给定commandName的配置
  		 */
  		Category category = getCategory(comandName, version);
  
  		/*
  		 * 执行并且解析
  		 */
  		return execute(category, args, afterExecuteListener,isLogErr);
	  } finally {
	    // 避免强制关闭
      if(--sessionCountInUse <=0 && closing) {
        closing = false;
        closed = true;
        cliSession.close();
      }
	  }
	}

	@Override
	public CLIResult execute(String comandName, String[] args,boolean isLogErr) throws CLIResultParseException, CLIException {
		return this.execute(comandName, args, null,isLogErr);
	}

	/**
	 * 执行命令并且解析结果
	 * 
	 * @param category  category标签
	 * @param args  命令参数
	 * @param afterExecuteListener  每一步执行后的监听器
	 * @return
	 * @throws CLIResultParseException
	 * @throws CLIException
	 */
	private CLIResult execute(Category category, String[] args, AfterExecuteListener afterExecuteListener,boolean isLogErr)
			throws CLIResultParseException, CLIException {
		if (logger.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			if (args != null && args.length > 0) {
				sb.append("[").append(args[0]);
				for (int i = 1; i < args.length; i++)
					sb.append(", ").append(args[i]);
				sb.append("]");
			}
			if (logger.isDebugEnabled()) 
				logger.debug(String.format("%s开始执行请求[cat: %s args: %s, after: %s]", toStringSimple(), category.getName(), sb.toString(), category.getHost()));		
		}
		
		chkSession();// 检查session

		List<Command> commands = category.getCommands();
		String collectData = "";
		StringBuffer collectDataBuf = new StringBuffer("");
		// modify by huangjb 2007/11/29 添加同步锁,防止多线程的相同监视服务操作同一CLIsession
		synchronized (cliSession) {
			if (cliSession.hasContext()) {
				if (!StringUtil.isNullOrBlank(category.getRemoteFilePath())) {// 如果有推送的脚本，则优先采用
					ShellInteract shellInteract = category.getParentCLIConf().getShellInteract();
					if (shellInteract == null) {
						throw new CLIException("缺少推送脚本的前置命令,请编辑相关脚本文件");
					}
					// 先执行一遍 获取远程脚本的配置序列号
					Command realExec = resolvePlaceHolder(shellInteract.getShellExec(), category);
					Command realCheck = (Command) realExec.clone();
					realCheck.setCmd(realCheck.getCmd() + " check");
					innerExecCmd(category, args, afterExecuteListener, collectDataBuf, realCheck,isLogErr);
					if (needPushScript(collectDataBuf.toString(), category.getSequence())) {
						collectDataBuf = new StringBuffer("");
						Command realStart = resolvePlaceHolder(shellInteract.getShellStart(), category);
						Command realEnd = resolvePlaceHolder(shellInteract.getShellEnd(), category);
						innerExecCmd(category, args, afterExecuteListener, collectDataBuf, realStart,isLogErr);
						innerExecCmd(category, args, afterExecuteListener, collectDataBuf, realEnd,isLogErr);
					} else {
						collectDataBuf = new StringBuffer("");
					}
					innerExecCmd(category, args, afterExecuteListener, collectDataBuf, realExec,isLogErr);
				} else {
					for (int index = 0; index < commands.size(); index++) {
						Command command = commands.get(index);//取出一个xml中<commands>中的一个<command>，执行它
						innerExecCmd(category, args, afterExecuteListener, collectDataBuf, command,isLogErr);
					}
				}
				collectData = collectDataBuf.toString();
			} else {
				String cmds = gatherCmds(commands);
				StringBuffer commentBuf = new StringBuffer("");
				collectData = execCommand(cmds, args, null,commentBuf,isLogErr);
				/*
				 * 设置回调
				 */
				if (afterExecuteListener != null) {
					Command command = new Command();
					command.setCmd(cmds);
					afterExecuteListener.handle(options, category, command, args, commentBuf.toString());
				}
			}
		}
		collectData.trim();//最终获取结果
		CLIResult cliResult = cliParser.parse(collectData, category.getParserRule());
		if (afterExecuteListener != null && cliResult != null) {			
			afterExecuteListener.handle(category, cliResult.getErrLines());
		}
		return cliResult;
	}

	/**
	 * 把命令行中的占位符替换成真实的内容
	 * 
	 * @param cmd
	 * @param filename
	 * @return
	 */
	private Command resolvePlaceHolder(Command cmd, Category category) {
		String cmdStr = cmd.getCmd();
		if (cmdStr.contains("${remoteFilePath}")) {
			cmdStr = cmdStr.replace("${remoteFilePath}", category.getRemoteFilePath());
		}
		if (cmdStr.contains("${sequence}")) {
			cmdStr = cmdStr.replace("${sequence}", category.getSequence());
		}
		if (cmdStr.contains("${commands}")) {
			String cmds = "";
			if (category.getShellCmd() != null) {// 如果配置了相应category的shell脚本，则直接采用，否则汇集组合所有的命令
				cmds = category.getShellCmd().getCmd();
			} else {
				cmds = gatherCmds(category.getCommands());
			}
			cmdStr = cmdStr.replace("${commands}", cmds);
		}
		Command newCmd = (Command) cmd.clone();
		newCmd.setCmd(cmdStr);
		return newCmd;
	}

	private String gatherCmds(List<Command> commands) {
		StringBuffer cmdBuffer = new StringBuffer();
		for (int index = 0; index < commands.size(); index++) {
			Command command = (Command) commands.get(index);
			if (!cmdBuffer.toString().endsWith("\n"))
				cmdBuffer.append("\n");
			cmdBuffer.append(command.getCmd());
		}
		return cmdBuffer.toString();
	}

	/**
	 * 执行命令
	 * 
	 * @param category
	 * @param args
	 * @param afterExecuteListener
	 * @param collectDataBuf
	 * @param command
	 * @throws CLIException
	 */
	private void innerExecCmd(Category category, String[] args, AfterExecuteListener afterExecuteListener,
			StringBuffer collectDataBuf, Command command,boolean isLogErr) throws CLIException {
		StringBuffer commentBuf = new StringBuffer("");
		String temp = execCommand(command.getCmd(), args, command.getCliPrompt(),commentBuf,isLogErr);
		temp = temp == null ? "" : temp.trim();
		if (afterExecuteListener != null)
			afterExecuteListener.handle(options, category, command, args, commentBuf.toString());
		/*
		 * 如果要输出则加到采集结果里面
		 */
		String collectData = collectDataBuf.toString();
		if (command.isOutput()) {
			if (StringUtil.isNullOrBlank(collectData)) {
				collectDataBuf.append(temp);
			} else {
				if (collectData.endsWith("\n"))
					collectDataBuf.append(temp);
				else
					collectDataBuf.append("\n").append(temp);
			}
		}

		try {
			Thread.sleep(command.getDelay());
		} catch (InterruptedException e) {
			// 忽略
		}
	}

	/**
	 * 如果远端脚本不存在，或者不是最新脚本，则返回true
	 * 
	 * @param sb
	 *          执行脚本的返回结果
	 * @param latestSeq
	 *          本地配置文件的序列号
	 * @return
	 */
	private boolean needPushScript(String sb, String latestSeq) {
		if (sb.contains("No such file or directory") || sb.contains("not found")||
		    sb.contains("A file or directory in the path name does not exist")) {
			return true;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("从" + options.getProperty(CLIConstant.OPTIONS_REMOTEHOST) + "获取回来的sequence:  " + sb);
		}
		int remoteSeq = Integer.parseInt(sb);
		int localSeq = Integer.parseInt(latestSeq);
		if (localSeq > remoteSeq) {
			return true;
		}
		return false;
	}

	/**
	 * 执行命令，命令行优先执行
	 * 
	 * @param command
	 * @param args
	 * @param prompt
	 * @param localBuf
	 * @param isLogErr 是否输出错误日志
	 * @return
	 * @throws CLIException
	 */
	private String execCommand(String command, String[] args, String prompt,StringBuffer localBuf,boolean isLogErr) throws CLIException {
		if (logger.isDebugEnabled()) 
			logger.debug(String.format("%s开始执行命令[%s]，参数[%s]", toStringSimple(), command, ArrayUtils.toString(args)));		
		String result = StringUtil.isNullOrBlank(command) ? "" : cliSession.execCmd(command, args, prompt,localBuf,isLogErr);
		if (logger.isDebugEnabled()) 
			logger.debug(String.format("%s命令执行结果：%s", toStringSimple(), result));				
		return result;
	}

	/**
	 * 获取category标签
	 * 
	 * @param comandName  category标签的name属性
	 * @param version  category标签的sysversion属性
	 * @return  category标签
	 * @throws CLIException
	 */
	private Category getCategory(String comandName, String version) throws CLIException {
		Category category = cliConfigurationHandler.getCLIConfiguration(comandName, options
				.getProperty(CLIConstant.OPTIONS_REMOTEHOST), options.getProperty(CLIConstant.OPTIONS_OS), version);
		if (category == null) {
			throw new CLINotFoundConfigException("找不到匹配的配置，命令：" + comandName + " 属性：" + options);
		}
		return category;
	}

	@Override
	public boolean init(Properties options,boolean isLogErr) throws CLILoginFailException, CLIConnectException {
		this.options = options;

		if (logger.isDebugEnabled())
			logger.debug(this + "开始初始化");
		
		// 先进行关闭操作
		if (cliSession != null)
			destroy();
		cliSession = CLISessionFactory.getCLISession(getSessionName(options));
		cliSession.open(options,isLogErr);

		if (logger.isDebugEnabled())
			logger.debug(this + "初始化完成");
		
		return true;
	}
	
	private String getSessionName(Properties options) {
		return options.getProperty(CLIConstant.OPTIONS_SESSIONNAME);
	}

	/**
	 * 获取目标主机版本
	 * 
	 * @param options  访问连接参数
	 * @return  目标主机版本
	 * @throws CLIException
	 * @throws CLIResultParseException
	 */
	private String getSysVersion(Properties options) throws CLIException, CLIResultParseException {
		String version = (String)options.get(CLIConstant.OPTIONS_OSVERSION);

		/*
		 * 如果参数没有提供版本信息
		 */
		if (StringUtil.isNullOrBlank(version)) {
			Category category = cliConfigurationHandler.getSysVersionConfiguration((String)options.get(CLIConstant.OPTIONS_OS));
			if (category == null)
				throw new CLINotFoundConfigException("没有指定系统版本号,也没有配置来获取版本号,OS:" + (String)options.get(CLIConstant.OPTIONS_OS));
			CLIResult result = this.execute(category, null, null,true);
			if (result == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("执行操作系统版本获取不到结果,直接返回null." + options);
				}
				return null;
			}
			Properties relProp = result.getPropResult();
			if (relProp == null || (String)relProp.get(CLIConstant.RESULT_VERSION) == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("获取不到操作系统信息,直接返回null." + options);
				}
				return null;
			}
			version = (String)relProp.get(CLIConstant.RESULT_VERSION);
			// 把获取到的版本信息保存到Options里,避免多次调用获取版本信息
			options.setProperty(CLIConstant.OPTIONS_OSVERSION, version);
		}
		return version;
	}
	
	@Override
	public boolean isWeak() {
		return ((CLISessionProxy) cliSession).isWeak() || closing || closed;
	}
	
	@Override
	public List<String> executeSQL(String comandName, String[] args) throws CLIException {
	  sessionCountInUse++;
	  try {
      String version = getSysVersion(options);
      if (logger.isDebugEnabled()) {
        logger.debug("版本号：" + version);
      }
      Category category = getCategory(comandName, version);
      String cmds = gatherCmds(category.getCommands());
      StringBuffer commentBuf = new StringBuffer("");
      return cliSession.runSQL(cmds, args, commentBuf);
	  } finally {
	    // 避免强制关闭
	    if(--sessionCountInUse <=0 && closing) {
	      closing = false;
	      closed = true;
	      cliSession.close();
	    }
	  }
  }

	@Override
	public String toString() {		
		if (this.options == null)
			return toStringSimple();
		else
			return String.format("DefaultCLICollector[id: %d name: %s ip: %s port: %s]", hashCode(),
					options.getProperty(CLIConstant.OPTIONS_SESSIONNAME), 
					options.getProperty(CLIConstant.OPTIONS_REMOTEHOST), 
					options.getProperty(CLIConstant.OPTIONS_REMOTEPORT));
	}
	
	private String toStringSimple() {
		return String.format("DefaultCLICollector[id: %d]", hashCode());
	}
	
	@Override
	public void close() {
		if (cached && !isWeak() && cliSession.isStanding())
			return;
		destroy();
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public boolean isStanding() {
		return cliSession.isStanding();
	}

	@Override
	public void destroy() {
		if (cliSession != null && !closing && !closed) {
			if (logger.isDebugEnabled())
				if (logger.isDebugEnabled())
					logger.debug(this + "关闭开始");
			
		  closing = true;
		  if (sessionCountInUse <= 0) {
		    closing = false;
		    closed = true;
		    cliSession.close();
		    
				if (logger.isDebugEnabled())
					if (logger.isDebugEnabled())
						logger.debug(this + "关闭完成");
				return;
		  }
		}
		
		logger.warn(String.format("%s还在使用中或已关闭，无法进行再次关闭[cliSession: %s closing: %b closed: %b]", this, cliSession, closing, closed));		
	}
	
}