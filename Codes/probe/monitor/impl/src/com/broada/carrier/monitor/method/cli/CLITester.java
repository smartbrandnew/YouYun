package com.broada.carrier.monitor.method.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.method.cli.config.CLIConfigurationHandler;
import com.broada.carrier.monitor.method.cli.config.Category;
import com.broada.carrier.monitor.method.cli.config.Command;
import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.pool.CollectorPool;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class CLITester {
	private static final Log logger = LogFactory.getLog(CLITester.class);

	public List<String> getSysNames() {
		return CLIConfigurationHandler.getInstance().getSysNames();
	}

	public Integer getSelSysNameIndex() {
		return CLIConfigurationHandler.getInstance().getSelSysNameIndex();
	}

	/**
	 * CLI调试用,command为调试命令,返回格式化结果
	 * 
	 * @param srvId
	 * @param nodeId
	 * @param paramId
	 * @param command
	 * @return
	 */
	public String debug(MonitorNode node, CLIMonitorMethodOption options, String[] commands) {
		CLITestContent content = new CLITestContent();
		try {
			ClIExecuteListener listener = new ClIExecuteListener(content);
			CLIExecutor cliExecutor = new CLIExecutor("-1");
			cliExecutor.execute(node, options, "sysversion", 0, listener);
			for (String command : commands) {
				cliExecutor.execute(node, options, command, 0, listener);
			}
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			content.addError("错误", t);
		}
		return content.getContent();
	}

	/**
	 * CLI调试用,command为调试命令,返回格式化结果
	 * 
	 * @param srvId
	 * @param nodeId
	 * @param paramId
	 * @param command
	 * @return
	 */
	public CLITestResult doTest(MonitorNode node, CLIMonitorMethodOption options) {
		CLIResult result;
		CLITestResult tr = new CLITestResult();
		// 加入这个参数来获取完整命令行提示符
		Properties props = options.toOptions(node.getIp());
		props.put("doTest", "");
		CLICollector collector = null;
		try {
			collector = CollectorPool.getCLICollector(props, "-1", true);
			result = collector.execute("sysversion", null, true);
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			tr.setError(ex.toString());
			return tr;
		} finally {
			if (collector != null)
				collector.close();
		}
		if (result == null) {
			tr.setError("取不到系统信息");
			return tr;
		}
		String os = (String) result.getPropResult().get(CLIConstant.RESULT_OS);
		if (os == null) {
			tr.setError("取不到OS信息");
			return tr;
		}
		tr.setOs(os);

		String version = (String) result.getPropResult().get(CLIConstant.RESULT_VERSION);
		if (version == null) {
			tr.setError("取不到版本信息");
			return tr;
		}
		tr.setVersion(version);
		// 返回完整命令行
		String fullPrompt = (String) options.getProperties().get("fullPrompt");
		if (fullPrompt != null) {
			tr.setFullPrompt(fullPrompt);
		}
		return tr;
	}

}

class CLITestContent {
	private static final String HOST_INFO = "<table border='0' width='100%'><caption><font color='blue'>主机信息</font></caption><tr><td>Host</td><td>{0}</td></tr><tr><td>OS</td><td>{1}</td></tr><tr><td>version</td><td>{2}</td></tr><tr><td>port</td> <td>{3}</td></tr><tr><td>session</td><td>{4}</td></tr><tr><td>user</td><td>{5}</td></tr><tr><td>login prompt</td><td>{6}</td></tr><tr><td>password prompt</td><td>{7}</td></tr><tr><td>command prompt</td><td>{8}</td></tr><tr><td>timeout</td><td>{9}</td></tr></table>";

	private static final String COMMAND = "<hr><br><font color='blue'>执行命令</font><br>命令:{0}<br>参数:{1}";

	private static final String RESULT = "<br><font color='red'>执行结果</font><br>{0}";

	private static final String ERROR = "<br><font color='red'><b>{0}</b><br>{1}</font>";

	private static final String ERRORTITLE = "<br><font color='red'>{0}解析结果描述</font><br><table border='0' width='100%' color='red'>{1}</table>";
	private static final String GOODTILE = "<br><font color='red'>{0}解析结果描述</font><br><table border='0' width='100%' color='green'>{1}</table>";
	private static final String ERRORDESC = "<tr><td>第{0}行</td><td>内容:{1}</td></tr>";

	private String oldContent = "";

	public void addCommand(Command command, String[] args) {
		StringBuffer argsBuffer = new StringBuffer();
		if (args == null) {
			argsBuffer.append("无参数");
		} else {
			for (int index = 0; index < args.length; index++) {
				argsBuffer.append("[").append(args[index]).append("]");
			}
		}
		addContent(MessageFormat.format(COMMAND, new Object[] { XmlEscapeUtil.escapeXml(command.getCmd()),
				XmlEscapeUtil.escapeXml(argsBuffer.toString()) }));
	}

	public void addResult(String result) {
		result = XmlEscapeUtil.escapeXml(result);
		addContent(MessageFormat.format(RESULT, new Object[] { result }));
	}

	public void addHostInfo(Properties options) {
		Object timeout = options.get(CLIConstant.OPTIONS_LOGINTIMEOUT);
		Object port = options.get(CLIConstant.OPTIONS_REMOTEPORT);

		addContent(MessageFormat.format(
				HOST_INFO,
				new Object[] { options.get(CLIConstant.OPTIONS_REMOTEHOST), options.get(CLIConstant.OPTIONS_OS),
						options.get(CLIConstant.OPTIONS_OSVERSION), port == null ? "" : port.toString(),
						options.get(CLIConstant.OPTIONS_SESSIONNAME), options.get(CLIConstant.OPTIONS_LOGINNAME),
						options.get(CLIConstant.OPTIONS_LOGINPROMPT), options.get(CLIConstant.OPTIONS_PASSDPROMPT),
						options.get(CLIConstant.OPTIONS_PROMPT), timeout == null ? "" : timeout.toString() }));
	}

	public void addContent(String text) {
		oldContent += text == null ? "" : text;
	}

	public String getContent() {
		return oldContent;
	}

	public void addError(String message, Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		t.printStackTrace(writer);
		addContent(MessageFormat.format(
				ERROR,
				new Object[] { XmlEscapeUtil.escapeXml(message),
						XmlEscapeUtil.escapeXml(stringWriter.getBuffer().toString()) }));
	}

	public void addError(String message, CLIErrorLine[] lines) {
		StringBuffer errsBuffer = new StringBuffer();
		if (lines != null) {
			for (CLIErrorLine item : lines) {
				String index = Integer.toString(item.getId());
				String content = item.getContent();
				String line = MessageFormat.format(ERRORDESC, new Object[] { XmlEscapeUtil.escapeXml(index),
						XmlEscapeUtil.escapeXml(content) });
				errsBuffer.append(line);
			}
		}
		String desc = "";
		if (lines == null || lines.length == 0) {
			errsBuffer.append("<font color='blue'>解析正常</font>");
			desc = MessageFormat.format(GOODTILE, new Object[] { message, errsBuffer.toString() });
		} else {
			desc = MessageFormat.format(ERRORTITLE, new Object[] { message, errsBuffer.toString() });
		}
		addContent(desc);
	}

	public void addEnd() {
		addContent("<br>======================END=========================<br><br>");

	}
}

class ClIExecuteListener implements AfterExecuteListener {

	private CLITestContent hostTestDlg;

	private boolean addHostInfo = false;

	public ClIExecuteListener(CLITestContent cliTestDlg) {
		this.hostTestDlg = cliTestDlg;
	}

	public void handle(Properties options, Category category, Command command, String[] args, String result) {
		if (!addHostInfo) {
			hostTestDlg.addHostInfo(options);
			addHostInfo = true;
		}
		hostTestDlg.addCommand(command, args);
		hostTestDlg.addResult(result);
	}

	public void handle(Category category, CLIErrorLine[] errlines) {
		hostTestDlg.addError(category.getDescription(), errlines);
	}

}
