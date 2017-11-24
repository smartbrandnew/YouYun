package com.broada.carrier.monitor.method.cli;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import com.broada.carrier.monitor.common.util.WorkPathUtil;
import com.broada.carrier.monitor.method.cli.config.Command;
import com.broada.carrier.monitor.method.cli.entity.CLIErrorLine;
import com.broada.swing.util.ErrorDlg;
import com.broada.swing.util.WinUtil;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class CLIDebugDlg extends JDialog {
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 2143629686313634917L;

	private static final String HTML = "<html><head><title>{0}</title></head><body>{1}</body></html>";

	private static final String HOST_INFO = "<table border='0' width='100%'><caption><font color='blue'>主机信息</font></caption><tr><td>Host</td><td>{0}</td></tr><tr><td>OS</td><td>{1}</td></tr><tr><td>version</td><td>{2}</td></tr><tr><td>port</td> <td>{3}</td></tr><tr><td>session</td><td>{4}</td></tr><tr><td>user</td><td>{5}</td></tr><tr><td>login prompt</td><td>{6}</td></tr><tr><td>password prompt</td><td>{7}</td></tr><tr><td>command prompt</td><td>{8}</td></tr><tr><td>timeout</td><td>{9}</td></tr></table>";

	private static final String COMMAND = "<hr><br><font color='blue'>执行命令</font><br>命令:{0}<br>参数:{1}";

	private static final String RESULT = "<br><font color='red'>执行结果</font><br>{0}";

	private static final String ERROR = "<br><font color='red'><b>{0}</b><br>{1}</font>";

	private static final String ERRORTITLE = "<br><font color='red'>{0}解析结果描述</font><br><table border='0' width='100%' color='red'>{1}</table>";
	private static final String GOODTILE = "<br><font color='red'>{0}解析结果描述</font><br><table border='0' width='100%' color='green'>{1}</table>";
	private static final String ERRORDESC = "<tr><td>第{0}行</td><td>内容:{1}</td></tr>";

	private String oldContent = "";

	private void init() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private CLIDebugDlg(Frame parent, String title, boolean model) {
		super(parent, title, model);
		init();
	}

	private CLIDebugDlg(Dialog parent, String title, boolean model) {
		super(parent, title, model);
		init();
	}

	public static CLIDebugDlg createDialog(Component parent, String title) {
		Window window = WinUtil.getWindowForComponent(parent);
		CLIDebugDlg dlg = null;
		if (window instanceof Frame) {
			dlg = new CLIDebugDlg((Frame) window, title, true);
		} else {
			dlg = new CLIDebugDlg((Dialog) window, title, true);
		}
		dlg.setSize(500, 500);
		WinUtil.toCenter(dlg);
		return dlg;
	}

	private void jbInit() throws Exception {
		this.getContentPane().setLayout(borderLayout1);
		jScrollPane.getViewport().add(jEditorPane);
		jPanel.add(jButton);
		getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);
		getContentPane().add(jPanel, java.awt.BorderLayout.SOUTH);
		jButton.setEnabled(false);
		jEditorPane.setEditable(false);
		jButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setSelectedFile(new File(getTitle() + ".html"));
				chooser.setFileFilter(new FileFilter() {

					public boolean accept(File f) {
						if (f.isDirectory())
							return true;
						else {
							String fileName = f.getName().toUpperCase();
							return fileName.endsWith("HTML")
									|| fileName.endsWith("HTM");
						}
					}

					public String getDescription() {
						return "超文本文件(*html,*htm)";
					}
				});
				if (JFileChooser.APPROVE_OPTION == chooser
						.showSaveDialog(CLIDebugDlg.this)) {
					File file = chooser.getSelectedFile();
					String fileName = file.getName().toUpperCase();
					if (!(fileName.endsWith("HTML") || fileName.endsWith("HTM"))) {
						String filePath = file.getPath();
						if(!filePath.startsWith(WorkPathUtil.getRootPath())){
							filePath = WorkPathUtil.getRootPath() + filePath;
						}
						file = new File( filePath + ".html");
					}
					try {
						PrintWriter writer = new PrintWriter(
								new FileOutputStream(file));
						writer.print(jEditorPane.getText());
						writer.flush();
						writer.close();
					} catch (FileNotFoundException ex) {
						ErrorDlg.createErrorDlg(CLIDebugDlg.this, "错误",
								ex.getMessage(), ex).setVisible(true);
					}
				}
			}
		});
	}

	public void addCommand(Command command, String[] args) {
		StringBuffer argsBuffer = new StringBuffer();
		if (args == null) {
			argsBuffer.append("无参数");
		} else {
			for (int index = 0; index < args.length; index++) {
				argsBuffer.append("[").append(args[index]).append("]");
			}
		}
		addContent(MessageFormat.format(COMMAND,
				new Object[] { XmlEscapeUtil.escapeXml(command.getCmd()),
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
				new Object[] {
						options.getProperty(CLIConstant.OPTIONS_REMOTEHOST),
						options.getProperty(CLIConstant.OPTIONS_OS),
						options.getProperty(CLIConstant.OPTIONS_OSVERSION),
						port == null ? "" : port.toString(),
						options.getProperty(CLIConstant.OPTIONS_SESSIONNAME),
						options.getProperty(CLIConstant.OPTIONS_LOGINNAME),
						options.getProperty(CLIConstant.OPTIONS_LOGINPROMPT),
						options.getProperty(CLIConstant.OPTIONS_PASSDPROMPT),
						options.getProperty(CLIConstant.OPTIONS_PROMPT),
						timeout == null ? "" : timeout.toString() }));
	}

	public void addContent(String text) {

		oldContent += text == null ? "" : text;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jEditorPane.setText(MessageFormat.format(HTML, new Object[] {
						getTitle(), oldContent }));
			}
		});
	}

	public void addError(String message, Throwable t) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		t.printStackTrace(writer);
		addContent(MessageFormat.format(
				ERROR,
				new Object[] {
						XmlEscapeUtil.escapeXml(message),
						XmlEscapeUtil.escapeXml(stringWriter.getBuffer()
								.toString()) }));
	}

	public void addError(String message, CLIErrorLine[] lines) {
		StringBuffer errsBuffer = new StringBuffer();
		if (lines != null) {
			for (CLIErrorLine item : lines) {
				String index = Integer.toString(item.getId());
				String content = item.getContent();
				String line = MessageFormat.format(ERRORDESC,
						new Object[] { XmlEscapeUtil.escapeXml(index),
								XmlEscapeUtil.escapeXml(content) });
				errsBuffer.append(line);
			}
		}
		String desc = "";
		if (lines == null || lines.length == 0) {
			errsBuffer.append("<font color='blue'>解析正常</font>");
			desc = MessageFormat.format(GOODTILE, new Object[] { message,
					errsBuffer.toString() });
		} else {
			desc = MessageFormat.format(ERRORTITLE, new Object[] { message,
					errsBuffer.toString() });
		}
		addContent(desc);
	}

	private BorderLayout borderLayout1 = new BorderLayout();

	private JScrollPane jScrollPane = new JScrollPane();

	private JPanel jPanel = new JPanel();

	private JButton jButton = new JButton("正在执行...");

	private JEditorPane jEditorPane = new JEditorPane("text/html",
			"正在等待执行命令返回,请等待...");

	public void addEnd() {
		addContent("<br>======================END=========================<br><br>");
		jButton.setText("导出Html文件");
		jButton.setEnabled(true);
	}
}