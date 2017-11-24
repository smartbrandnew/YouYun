package com.broada.carrier.monitor.method.cli.session.ssh;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.parser.DefaultCLIParser;
import com.broada.carrier.monitor.method.cli.session.AbstractCLISession;
import com.broada.utils.TextUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHSession extends AbstractCLISession {
	private static final Log logger = LogFactory.getLog(SSHSession.class);
	private Session session = null;
	private Channel channel = null;

	@Override
	public void open(Properties options, boolean isLogErr) throws CLILoginFailException, CLIConnectException {
		try {
			if (session != null) {
        close();
      }
			JSch jsch = new JSch();

			session = jsch.getSession(options.getProperty(CLIConstant.OPTIONS_LOGINNAME), options.getProperty(CLIConstant.OPTIONS_REMOTEHOST), (Integer)options.get(CLIConstant.OPTIONS_REMOTEPORT));
			session.setPasswdPrompt((String) options.get(CLIConstant.OPTIONS_PASSWORDPROMPT));
			session.setPassword(options.getProperty(CLIConstant.OPTIONS_PASSWORD));
			session.setTimeout((Integer)options.get(CLIConstant.OPTIONS_LOGINTIMEOUT));
			
			session.setConfig("kex", "diffie-hellman-group1-sha1"); 

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			
			session.connect();
		} catch (JSchException e) {
			close();
			throw handler(e);
		}
	}

	@Override
	public String execCmd(String cmd, String[] args, String prompt, StringBuffer localBuf, boolean isLogErr) throws CLIException {
		//传入参数判断
		if(StringUtils.isEmpty(cmd))
			throw new NullPointerException("命令不能为空");
		
		//命令参数
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
		
		InputStream in = null;
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		StringBuffer strb = null;
		String strbErr = null;
		try {
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(cmd);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(err);
			in = new BufferedInputStream(channel.getInputStream());
			channel.connect();
		} catch (JSchException e) {
			logger.error("SSH通道连接失败", e);
			throw new CLIConnectException("SSH通道连接失败", e);
		} catch (IOException e) {
			logger.error("SSH通道连接失败", e);
			throw new CLIConnectException("SSH通道连接失败", e);
		}
		
		try{
			//命令执行的正常结果
			int i = -1;
			strb = new StringBuffer();
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					strb.append(new String(tmp, 0 , i, "UTF-8"));
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					if(logger.isDebugEnabled())
						logger.debug("exit-status: " + channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			in.close();
			
			//命令执行的错误信息
			strbErr = err.toString("UTF-8");
		}catch(IOException ioe){
			logger.error("采集失败", ioe);
			throw new CLIConnectException("采集失败", ioe);
		}finally{
			channel.disconnect();
		}
		//如果命令执行结果为空，则返回错误信息
		String result = strb.toString().equals("") ? strbErr : strb.toString();
		DefaultCLIParser.getInstance().messageLocalized(result,localBuf);
		return result;
	}
	
	@Override
	public void close() {
		try{
			if(session != null && session.isConnected())
				session.disconnect();
		}catch(Exception e){
			logger.error("SSH session关闭异常", e);
		}
	}

	@Override
	public String execScript(String scriptFile, String[] args) throws CLIException {
		throw new UnsupportedOperationException(this.getClass().getName() + "不支持execScript方法。");
	}
	
	private CLIException handler(JSchException e){
		logger.error(e.getMessage(), e);
		String msg = e.getMessage();
		if(msg.contains("socket is not established"))
			return new CLIConnectException("连接错误,请检查主机地址和状态", e);
		
		else if(msg.contains("Connection refused"))
			return new CLIConnectException("连接错误,请检查协议和端口.", e);
		
		else if(msg.contains("Auth fail"))
			return new CLILoginFailException("登录失败,请检用户名、密码及密码提示.", e);
		
		else if(msg.contains("Algorithm negotiation fail"))
			return new CLIConnectException("连接错误,不支持的加密算法,请尝试为服务器添加配置.", e);
		
		else
			return new CLILoginFailException("登录时发生未知错误：" + msg, e);
	}
	
	@Override
	public boolean isStanding() {
		return true;
	}
}
