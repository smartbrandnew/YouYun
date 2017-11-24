package com.broada.carrier.monitor.method.cli.session.telnet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.telnet.TelnetClient;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIWaitResponseTimeoutException;
import com.broada.carrier.monitor.method.cli.parser.DefaultCLIParser;
import com.broada.carrier.monitor.method.cli.session.AbstractCLISession;

public class TelnetSession extends AbstractCLISession {
	private static final Log logger = LogFactory.getLog(TelnetSession.class);
	private char promptChar = '#'; //默认结束标识字符
	private TelnetClient telnet = null;
	private InputStream in = null; // 输入流,接收返回信息  
	private PrintStream out = null; // 向服务器写入 命令
	private int waitTimeOut = 60000;
	
	@Override
	public void open(Properties options, boolean isLogErr) throws CLILoginFailException, CLIConnectException {
		if(telnet != null)
			close();
		//TelnetClient的构造方法中有个termtype参数，此处没有用到，表示协议类型：VT100、VT52、VT220(Windows,用VT220,否则会乱码)、VTNT、ANSI
		telnet = new TelnetClient();
		String loginPrompt = (String) options.get(CLIConstant.OPTIONS_LOGINPROMPT);
		String passwdPrompt = (String) options.get(CLIConstant.OPTIONS_PASSWORDPROMPT);
		String prompt = (String) options.get(CLIConstant.OPTIONS_PROMPT);
		waitTimeOut = (Integer)options.get(CLIConstant.OPTIONS_LOGINTIMEOUT);
		if(!StringUtils.isEmpty(prompt) && !prompt.equals("default"))
			this.promptChar = prompt.charAt(0);
		//连接超时
		telnet.setConnectTimeout((Integer)options.get(CLIConstant.OPTIONS_LOGINTIMEOUT));
		try {
			telnet.connect((String) options.get(CLIConstant.OPTIONS_REMOTEHOST), (Integer)options.get(CLIConstant.OPTIONS_REMOTEPORT));
		} catch (Exception e) {
			logger.error("连接错误,请检查主机地址和端口.", e);
			throw new CLIConnectException("连接错误,请检查主机地址和端口.", e);
		}
		try{
			in = telnet.getInputStream();
			out = new PrintStream(telnet.getOutputStream());
		} catch (Exception e){
			close();
			logger.error("连接错误,无法获取输入输出流.", e);
			throw new CLIConnectException("连接错误,无法获取输入输出流.", e);
		}
		
		try{
			String result = null;
			try{
				//匹配登录提示，然后发送用户名
				result = read(loginPrompt);
			} catch (CLIWaitResponseTimeoutException e){//对超时异常做特殊处理，提示给用户
				logger.error("读取超时,请检查登录提示." + "返回结果为：" + e.getMessage(), e);
				throw new CLIConnectException("读取超时,请检查登录提示.", e);
			}
			handlerName(result);
			out.println((String) options.get(CLIConstant.OPTIONS_LOGINNAME));
			out.flush();
			
			if(!StringUtils.isEmpty(passwdPrompt)){//判断登录是否需要密码
				try{
					//匹配密码提示，然后发送密码
					result = read(passwdPrompt);
				} catch (CLIWaitResponseTimeoutException e){//对超时异常做特殊处理，提示给用户
					logger.error("读取超时,请检查密码提示." + "返回结果为：" + e.getMessage(), e);
					throw new CLIConnectException("读取超时,请检查密码提示.", e);
				}
				handlerPassword(result);
				out.println((String) options.get(CLIConstant.OPTIONS_PASSWORD));
				out.flush();
			}
			
			try{
				//判断是否登录成功
				result = read(String.valueOf(promptChar), loginPrompt, passwdPrompt);
			} catch (CLIWaitResponseTimeoutException e){
				logger.error("读取超时,请检查命令提示符." + "返回结果为：" + e.getMessage(), e);
				throw new CLIConnectException("读取超时,请检查命令提示符.", e);
			}
			handlerLogin(result);
		} catch (CLIException e){
			close();
			throw e;
		} catch (Exception e) {
			close();
			throw new CLIException("发生未知未知异常", e);
		}
	}
	
	@Override
	public String execCmd(String cmd, String[] args, String prompt, StringBuffer localBuf, boolean isLogErr) throws CLIException {
		try {
			telnet.setKeepAlive(true);
			out.println(cmd);
			out.flush();
		} catch (Exception e) {
			logger.error("发送命令失败,可能连接已经失效.", e);
			throw new CLIConnectException("发送命令失败,可能连接已经失效.", e);
		}
		String result = null;
		try {
			result = read((prompt == null || prompt.equals("default")) ? String.valueOf(promptChar) : prompt);
		} catch (CLIWaitResponseTimeoutException e) {
			logger.error("读取超时,已读取结果为：" + e.getMessage(), e);
			throw new CLIConnectException("读取超时,已读取结果为：" + e.getMessage(), e);
		}
		String [] arr = splitResult(result);
		DefaultCLIParser.getInstance().messageLocalized(arr[0], localBuf);
		return arr[0];
	}
	
	@Override
	public void close() {
		try {
			if(in != null)
				in.close();
			if(out != null)
				out.close();
		} catch (IOException e) {
			logger.error("Telnet流关闭异常", e);
		}finally{
			if (telnet != null && !telnet.isConnected()){
				try {
					telnet.disconnect();
				} catch (IOException e) {
					logger.error("Telnet session关闭异常", e);
				}
			}
		}
	}

	@Override
	public String execScript(String scriptFile, String[] args) throws CLIException {
		throw new UnsupportedOperationException(this.getClass().getName() + "不支持execScript方法。");
	}
	
	@Override
	public boolean isStanding() {
		return true;
	}

	/** 
	 * 读取结果
	 *  
	 * @param pattern 匹配到该字符串时返回结果 
	 * @return 结果数据(包含命令提示符)
	 */
	private String read(String... pattern){
		if(pattern == null || pattern.length == 0)
			throw new NullPointerException("命令结束匹配符不能为空");
		List<String> list = Arrays.asList(pattern);
		for(String pat : list){
			if (pat == null || pat.trim().equals(""))
				list.remove(pat);
		}
		if(list.size() == 0)
			throw new NullPointerException("命令结束匹配符不能为空");
		
		StringBuffer sb = null;
		BufferedInputStream bis = null;
		int waitTime = 0;//已等待时间
		int i = 0;
		int num = 0;
		byte [] tmp = null;
		try {
			sb = new StringBuffer();
			bis = new BufferedInputStream(in);
			while (true) {
				if((num = bis.available()) != 0){
					tmp = new byte[num];
				  i = bis.read(tmp);
				  sb.append(new String(tmp, 0 , i, "UTF-8"));
					//匹配到结束标识时返回结果
					for(String pat : list){
						if (sb.toString().toLowerCase().trim().endsWith(pat.toLowerCase())) {
							return sb.toString();
						}
					}
					waitTime = 0;//读取到数据时重置已等待时间
				} else {
					if(waitTime >= waitTimeOut)
						throw new CLIWaitResponseTimeoutException(sb.toString());
					waitTime += 100;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
		} catch (SocketTimeoutException e) {
			throw new CLIConnectException(e);
		} catch (IOException e) {
			throw new CLIConnectException("从服务器读取结果发生异常.", e);
		}
	}
	
	/**
	 * 对命令结果进行分解，找到结果和命令提示符
	 * @param result
	 * @return
	 */
	private String [] splitResult(String result){
		String [] arr = new String[2];
		if(result.lastIndexOf("\r\n") != -1){
			int index = result.lastIndexOf("\r\n");
			arr[0] = result.substring(0, index);
			arr[1] = result.substring(index + 2);
		} else {
			int index = result.lastIndexOf("\n") > result.lastIndexOf("\r") ? result.lastIndexOf("\n") : result.lastIndexOf("\r");
			if(index == -1)
				arr[0] = "";
			else
				arr[0] = result.substring(0, index);
			arr[1] = result.substring(index + 1);
		}
		return arr;
	}
	
	private void handlerName(String result) throws CLILoginFailException, CLIConnectException{
		if(result.contains("timed out")){
			throw new CLILoginFailException("读取超时,请检查登录提示.");
		} else if(result.contains("time out")){
			throw new CLILoginFailException("读取超时,请检查登录提示.");
		} else if(result.contains("invalid login")){
			throw new CLILoginFailException("登录失败,请检查登录提示.");
		}
	}
	
	private void handlerPassword(String result) throws CLILoginFailException, CLIConnectException{
		if(result.contains("timed out")){
			throw new CLILoginFailException("读取超时,请检查密码提示.");
		} else if(result.contains("time out")){
			throw new CLILoginFailException("读取超时,请检查密码提示.");
		} else if(result.contains("invalid login")){
			throw new CLILoginFailException("登录失败,请检查密码提示.");
		}
	}
	
	private void handlerLogin(String result) throws CLILoginFailException, CLIConnectException{
		if(result.contains("timed out")){
			throw new CLILoginFailException("读取超时,请检查命令提示符.");
		} else if(result.contains("time out")){
			throw new CLILoginFailException("读取超时,请检查命令提示符.");
		} else if(result.contains("invalid login")){
			throw new CLILoginFailException("登录失败,请检查用户名、密码.");
		} else if(result.toLowerCase().contains("login failed")){
			throw new CLILoginFailException("登录失败,请检查用户名、密码.");
		} else if(!result.trim().endsWith(String.valueOf(promptChar))){
			throw new CLILoginFailException("登录失败,请检查用户名、密码.");
		}
	}
}
