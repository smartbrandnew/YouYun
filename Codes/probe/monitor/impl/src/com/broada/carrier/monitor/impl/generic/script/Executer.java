package com.broada.carrier.monitor.impl.generic.script;

import java.io.File;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.impl.generic.script.session.JdbcSession;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.jdbc.JdbcMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.impl.action.context.DateContext;
import com.broada.cid.action.impl.action.context.NumberContext;
import com.broada.cid.action.impl.action.context.TextContext;
import com.broada.cid.action.impl.action.context.UnitContext;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;
import com.broada.cid.action.protocol.impl.smis.SmisProtocol;
import com.broada.cid.action.protocol.impl.smis.SmisSession;
import com.broada.cid.action.protocol.impl.snmp.SnmpProtocol;
import com.broada.cid.action.protocol.impl.snmp.SnmpSession;
import com.broada.numen.agent.script.context.Context;
import com.broada.numen.agent.script.context.cli.CLIContext;
import com.broada.numen.agent.script.entity.Parameter;
import com.broada.numen.agent.script.entity.Result;
import com.broada.numen.agent.script.impl.ScriptBuffer;
import com.broada.numen.agent.script.service.ExecuteException;
import com.broada.numen.agent.script.service.ExecuteResultReceiver;
import com.broada.numen.agent.util.FileInfo;

/**
 * 接口，定义一个执行器所应该执行的方法
 */
public class Executer {
	private static final Log logger = LogFactory.getLog(Executer.class);
	private static GroovyScriptEngineFactory factory = new GroovyScriptEngineFactory();
	private static ScriptBuffer scriptBuffer = new ScriptBuffer(factory);


	/**
	 * 执行一个指定的脚本
	 * 
	 * @param scriptFilePath
	 *            脚本文件路径
	 * @param param
	 *            传入脚本的参数
	 * @param contexts
	 *            上下文环境
	 * @return
	 * @throws ExecuteException
	 */
	public Result execute(String scriptFilePath, Parameter param,
			Context[] contexts, MonitorMethod method, String ip)
			throws ExecuteException {
		File scriptFile = new File(scriptFilePath);
		if (!scriptFile.exists()) {
			File userFile = new File(System.getProperty("user.dir"),
					scriptFilePath);
			if (!userFile.exists())
				throw new ExecuteException(
						ExecuteException.TYPE_SCRIPT_NOT_EXIST, "脚本["
								+ scriptFilePath + "]不存在");
		}

		long startTime = 0;
		if (logger.isDebugEnabled()) {
			startTime = System.currentTimeMillis();
			logger.debug("执行脚本" + scriptFilePath);
		}

		CompiledScript compiledScript = scriptBuffer.getScript(FileInfo
				.getFilename(scriptFilePath));
		if (compiledScript == null) {
			throw new ExecuteException(ExecuteException.TYPE_SCRIPT_ERROR,
					"无法获取编译后的脚本，原因可能是上一次执行脚本时发生编译错误且上一次和这一次脚本的版本信息没有发生变化。");
		}

		Result result = new Result();

		ScriptEngine engine = compiledScript.getEngine();

		Bindings bindings = engine.createBindings();
		bindings.put("result", result);
		bindings.put("param", param);
		addContextWithMethod(bindings, method, ip);

		StringBuffer console = new StringBuffer();
		for (int i = 0; i < contexts.length; i++) {
			contexts[i].setConsole(console);
			bindings.put(contexts[i].getName(), contexts[i]);
		}

		try {
			compiledScript.eval(bindings);

			if (logger.isDebugEnabled()) {
				logger.debug("操作脚本成功：" + scriptFilePath + " 所需时间"
						+ ((System.currentTimeMillis() - startTime) / 1000)
						+ "s");
				logger.debug("执行信息：\n" + result);
			}

			if (result.getExecuteText() == null
					|| result.getExecuteText().length() == 0)
				result.setExecuteText(console.toString());
			return result;
		} catch (Exception err) {
			try {
				if (result.getExecuteText() == null
						|| result.getExecuteText().length() == 0)
					result.setExecuteText(console.toString());
			} catch (Exception e) {
			}
			if (logger.isDebugEnabled()) {
				logger.debug("操作执行失败，没有任何脚本正确处理：" + scriptFilePath + " 所需时间："
						+ ((System.currentTimeMillis() - startTime) / 1000)
						+ "s");
				logger.debug("执行信息：\n" + result);
				logger.debug("堆栈", err);
			}

			Throwable nextErr = err;
			while (nextErr.getCause() != null) {
				if (!(nextErr.getCause() instanceof ScriptException)) {
					if (nextErr.getCause() instanceof InterruptedException)
						throw new ExecuteException(
								ExecuteException.TYPE_SCRIPT_ERROR,
								"脚本可能由于执行时间超长被中止："
										+ nextErr.getCause().getMessage());
					else if (nextErr.getCause() instanceof ArrayIndexOutOfBoundsException)
						throw new ExecuteException(
								ExecuteException.TYPE_SCRIPT_ERROR,
								"脚本逻辑错误，访问数组越界："
										+ nextErr.getCause().getMessage());
					else
						throw new ExecuteException(
								ExecuteException.TYPE_SCRIPT_ERROR,
								"脚本语法或逻辑错误：" + nextErr.getCause());
				}
				nextErr = nextErr.getCause();
			}
			throw new ExecuteException(ExecuteException.TYPE_SCRIPT_ERROR,
					"脚本执行由于未知错误被中止：" + nextErr.getMessage(), nextErr);
		} finally {
			bindings.clear();
			for (int i = 0; i < contexts.length; i++) {
				Context context = contexts[i];
				try {
					context.close();
				} catch (Exception err) {
					logger.warn("关闭上下文环境[" + context.getName() + "]时碰到异常", err);
				}
			}
		}
	}

	/**
	 * 根据监测方式添加相应的接口参数到contexts中,脚本中可以直接调用接口的方法
	 * 
	 * @param bindings
	 * @param method
	 * @param ip
	 */
	private void addContextWithMethod(Bindings bindings, MonitorMethod method,
			String ip) {
		// TODO Auto-generated method stub
		DateContext date = new DateContext();
		TextContext text = new TextContext();
		NumberContext number = new NumberContext();
		UnitContext unit = new UnitContext();
		bindings.put("$date", date);
		bindings.put("$text", text);
		bindings.put("$number", number);
		bindings.put("$unit", unit);
		if (method.getTypeId().equals(SnmpMethod.TYPE_ID)) {
			DefaultDynamicObject properties = method.getProperties();
			SnmpProtocol protocol = new SnmpProtocol(new Protocol("snmp",
					properties));
			protocol.setField("ip", ip);
			SnmpSession snmp = new SnmpSession(protocol);
			bindings.put("$snmp", snmp);
		} else if (method.getTypeId().equals(SmisMethod.TYPE_ID)) {
			logger.warn("开始初始化smis协议参数！");
			DefaultDynamicObject properties = method.getProperties();
			SmisProtocol protocol = new SmisProtocol(new Protocol("smis",
					properties));
			protocol.setField("ip", properties.get("privIp", ip).toString());
			logger.warn("privIP is "+properties.get("privIp", ip).toString());
			SmisSession smis = new SmisSession(protocol);
			logger.warn("$smis is "+smis);
			smis.connect();
			bindings.put("$smis", smis);
		} else if (method.getTypeId().equals(CLIMonitorMethodOption.TYPE_ID)) {
			DefaultDynamicObject properties = method.getProperties();
			CcliProtocol protocol = new CcliProtocol(new Protocol("ccli",
					properties));
			protocol.setField("ip", ip);
			CcliSession cli = new CcliSession(protocol);
			bindings.put("$cli", cli);
			// cli 扩展字段
			setExtParameters(bindings, properties);
		} else if (method.getTypeId().equals(JdbcMonitorMethodOption.TYPE_ID)) {
			DefaultDynamicObject properties = method.getProperties();
			JdbcMonitorMethodOption option = new JdbcMonitorMethodOption();
			DefaultDynamicObject extra = SerializeUtil.decodeJson((String) properties.get("extra"), DefaultDynamicObject.class);
			option.setDbType(extra.get("dbType", ""));
			option.setEncoding(extra.get("encoding", ""));
			option.setUsername(properties.get("username", ""));
			option.setPassword(properties.get(CLIConstant.OPTIONS_PASSD, ""));
			option.setSid(properties.get("sid", ""));
			option.setPort(properties.get("port", 1521));
			JdbcSession jdbc = new JdbcSession(option, ip);
			bindings.put("$jdbc", jdbc);
		}
	}

	/**
	 * 执行一个指定的脚本
	 * 
	 * @param scriptFilename
	 *            脚本文件路径
	 * @return 如果执行正确结束，则返回执行结果
	 * @throws ExecuteException
	 *             如果执行发现了任何错误，则以相应异常返回
	 */
	public Result execute(String scriptFilename, Parameter param,
			MonitorMethod method, String ip) throws ExecuteException {
		Context[] contexts = new Context[] { new CLIContext() };

		try {
			return execute(scriptFilename, param, contexts, method, ip);
		} finally {
			for (int i = 0; i < contexts.length; i++) {
				Context context = contexts[i];
				try {
					context.close();
				} catch (Exception err) {
					logger.warn("关闭上下文环境[" + context.getName() + "]时碰到异常", err);
				}
			}
		}
	}

	private static ExecuteResultReceiver receiver;

	private static ExecuteResultReceiver getReceiver(String serverIP,
			int serverPort) throws ExecuteException {
		String serviceUrl = null;
		try {
			if (receiver == null) {
				serviceUrl = "rmi://" + serverIP + ":" + serverPort
						+ "/executeResultReceiver";

				RmiProxyFactoryBean rmiProxyFactoryBean = new RmiProxyFactoryBean();
				rmiProxyFactoryBean
						.setServiceInterface(ExecuteResultReceiver.class);
				rmiProxyFactoryBean.setServiceUrl(serviceUrl);
				rmiProxyFactoryBean.setLookupStubOnStartup(false);
				rmiProxyFactoryBean.setRefreshStubOnConnectFailure(true);
				rmiProxyFactoryBean.afterPropertiesSet();

				receiver = (ExecuteResultReceiver) rmiProxyFactoryBean
						.getObject();
			}
		} catch (Exception err) {
			throw new ExecuteException(ExecuteException.TYPE_ENVIRONMENT,
					"脚本执行进程与脚本执行服务[" + serviceUrl + "]无法建立连接，将导致执行结果无法返回", err);
		}
		return receiver;
	}
	
	/**
	 * 将扩展字段加入cli脚本处理中
	 * @param bindings
	 * @param properties
	 * @return
	 */
	private void setExtParameters(Bindings bindings, DefaultDynamicObject properties){
		if(properties == null || properties.isEmpty()) return;
		for(String key:properties.keySet())
			if(key.endsWith("_ext"))
				bindings.put("$" + key, properties.get(key));
	}
	
	/**
	 * 输出使用说明
	 */
	private static void usage() {
		logger.info("Usage: ");
		logger.info("\tscriptexec [options] scriptfile");
		logger.info("Options: ");
		logger.info("\t-s x.x.x.x\t指定结果回转服务IP");
		logger.info("\t-p n\t\t指定结果回转服务端口");
		logger.info("\t-i xxxx\t\t指定执行id");
		System.exit(1);
	}
}
