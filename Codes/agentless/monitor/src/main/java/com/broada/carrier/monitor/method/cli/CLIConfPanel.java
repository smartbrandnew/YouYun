package com.broada.carrier.monitor.method.cli;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;
import com.broada.utils.FieldValidator;
import com.broada.utils.StringUtil;

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
public class CLIConfPanel extends JPanel {
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1532078528114534353L;

	private SpinnerNumberModel snm_port = new SpinnerNumberModel(22, 1, 65535, 1);

	private SpinnerNumberModel snm_timeout = new SpinnerNumberModel(15000, 1, 600000, 1);

	JLabel jLabelStyle = new JLabel();

	JComboBox jComboBoxType = new JComboBox();

	JLabel jLabelPort = new JLabel();

	JSpinner jSpinnerPort = new JSpinner(snm_port);

	JLabel jLabelPrompt = new JLabel();

	JPromptPanel jTextFieldPrompt = new JPromptPanel();

	JLabel jLabelUser = new JLabel();
	JLabel jLabelTermType = new JLabel();
	JLabel jlWindowsUser = new JLabel();

	JTextField jTextFieldUser = new JTextField();
	JTextField jTextFieldTermType = new JTextField();
	JTextField jtfWindowsUser = new JTextField();

	JLabel jLabelPassword = new JLabel();

	JLabel jlWindowsPasswd = new JLabel();

	JPasswordField jPassword = new JPasswordField();

	JPasswordField jPasswdWindows = new JPasswordField();

	JLabel jLabelSystem = new JLabel();

	JComboBox jComboBoxSystem = new JComboBox();

	JLabel jLabelVersion = new JLabel();

	JTextField jTextFieldVersion = new JTextField();

	private JButton jButtonCheckSetting = new JButton();

	JLabel jLabelTimeout = new JLabel();

	JSpinner jSpinnerTimeout = new JSpinner(snm_timeout);

	TitledBorder titledBorder = new TitledBorder("");

	JPanel jPanel1 = new JPanel();

	JLabel jLabel1 = new JLabel();

	JTextField jTextFieldLoginPrompt = new JTextField();

	JLabel jLabel2 = new JLabel();

	JTextField jTextFieldPasswordPrompt = new JTextField();

	JPanel jPanel2 = new JPanel();

	JPanel jpWindows = new JPanel();

	private MonitorNode monitorNode;

	private boolean agentOnly = false;// db2代理监测用
	protected MonitorMethodConfigContext context;

	public void setContext(MonitorMethodConfigContext context) {
		this.context = context;

		jComboBoxSystem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setComboBoxType();
			}
		});

		List<String> names = getSysNames();
		for (String name : names) {
			jComboBoxSystem.addItem(name);
		}
		jComboBoxSystem.setSelectedIndex(getCliSelSysNameIndex());
	}

	public JComboBox getjComboBoxSystem() {
		return jComboBoxSystem;
	}

	public void setjComboBoxSystem(JComboBox jComboBoxSystem) {
		this.jComboBoxSystem = jComboBoxSystem;
	}

	public CLIConfPanel() {
		try {
			jbInit();
			init();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private List<String> getSysNames() {
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) context.getServerFactory().getProbeService()
				.executeMethod(context.getNode().getProbeId(), CLITester.class.getName(), "getSysNames");
		//list.remove("Windows");
		return list;
	}

	public int getCliSelSysNameIndex() {
		return (Integer) context.getServerFactory().getProbeService()
				.executeMethod(context.getNode().getProbeId(), CLITester.class.getName(), "getSelSysNameIndex");
	}

	private void init() throws Exception {

		jComboBoxType.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String sessionType = (String) jComboBoxType.getSelectedItem();
				if (sessionType == null)
					return;
				enableChildComponent();
				chgVersinoPosition(sessionType.equalsIgnoreCase(CLIConstant.SESSION_WMI));
				jSpinnerPort.setEnabled(!sessionType.equalsIgnoreCase(CLIConstant.SESSION_WMI));
				jTextFieldPrompt.setEnabled(!sessionType.equalsIgnoreCase(CLIConstant.SESSION_WMI));
				if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_AGENT)) {
					snm_port.setValue(new Integer(1850));
					jPanel1.setVisible(false);
					jpWindows.setVisible(false);
					jPanel2.validate();
					jPanel2.setVisible(true);
				} else if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
					snm_port.setValue(new Integer(161));
					jPanel1.setVisible(false);
					jPanel2.setVisible(false);
					jpWindows.validate();
					jpWindows.setVisible(true);
				} else if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_TELNET)) {
					snm_port.setValue(new Integer(23));
					jPanel1.setVisible(true);
					jPanel2.setVisible(false);
					jpWindows.setVisible(false);
				} else if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_SSH)) {
					snm_port.setValue(new Integer(22));
					jPanel1.setVisible(true);
					jPanel2.setVisible(false);
					jpWindows.setVisible(false);
				}
			}
		});
	}

	private void enableChildComponent() {
		Component[] components = getComponents();
		for (int index = 0; index < components.length; index++) {
			components[index].setEnabled(true);
		}
	}

	private void jbInit() throws Exception {
		titledBorder = new TitledBorder("登录配置");
		this.setLayout(null);
		jLabelStyle.setText("访问方式");
		jLabelStyle.setBounds(new Rectangle(222, 24, 60, 20));
		jComboBoxType.setBounds(new Rectangle(301, 24, 104, 25));

		jLabelPort.setText("端口");
		jLabelPort.setBounds(new Rectangle(9, 57, 38, 12));
		jSpinnerPort.setBounds(new Rectangle(80, 57, 76, 25));

		jLabelTimeout.setText("超时时间(ms)");
		jLabelTimeout.setBounds(new Rectangle(216, 6, 76, 20));
		jSpinnerTimeout.setBounds(new Rectangle(295, 6, 63, 25));
		jLabelPrompt.setText("命令提示符");
		jLabelPrompt.setBounds(new Rectangle(3, 6, 64, 20));
		jTextFieldPrompt.setBounds(new Rectangle(74, 6, 77, 25));

		jLabelPassword.setText("登录密码");
		jLabelPassword.setBounds(new Rectangle(216, 70, 62, 20));
		jPassword.setBounds(new Rectangle(295, 70, 104, 25));

		jLabelUser.setText("登录用户名");
		jLabelUser.setBounds(new Rectangle(3, 70, 60, 20));
		jTextFieldUser.setBounds(new Rectangle(74, 70, 104, 25));

		jLabelTermType.setText("终端类型");
		jLabelTermType.setBounds(new Rectangle(3, 95, 60, 20));
		jTextFieldTermType.setText("dumb");
		jTextFieldTermType.setBounds(new Rectangle(74, 95, 104, 25));

		jlWindowsUser.setText("登录用户名");
		jlWindowsUser.setBounds(new Rectangle(3, 2, 60, 20));
		jtfWindowsUser.setBounds(new Rectangle(74, 2, 104, 25));

		jlWindowsPasswd.setText("登录密码");
		jlWindowsPasswd.setBounds(new Rectangle(216, 2, 62, 20));
		jPasswdWindows.setBounds(new Rectangle(295, 2, 104, 25));

		jLabelSystem.setText("系统名称");
		jLabelSystem.setBounds(new Rectangle(9, 24, 55, 20));
		jComboBoxSystem.setBounds(new Rectangle(80, 24, 104, 25));
		jLabelVersion.setText("系统版本");
		jLabelVersion.setBounds(new Rectangle(222, 57, 53, 20));
		jTextFieldVersion.setBounds(new Rectangle(301, 57, 104, 25));
		jButtonCheckSetting.setBounds(new Rectangle(179, 215, 100, 23));
		jButtonCheckSetting.setText("检查设置");
		this.setBorder(titledBorder);
		jPanel1.setBounds(new Rectangle(6, 86, 449, 120));
		jPanel1.setLayout(null);
		jLabel1.setText("登录提示");
		jLabel1.setBounds(new Rectangle(3, 40, 63, 15));
		jTextFieldLoginPrompt.setText("login:");
		jTextFieldLoginPrompt.setBounds(new Rectangle(74, 40, 104, 25));
		jLabel2.setText("密码提示");
		jLabel2.setBounds(new Rectangle(216, 40, 63, 15));
		jTextFieldPasswordPrompt.setText("Password:");
		jTextFieldPasswordPrompt.setBounds(new Rectangle(295, 40, 104, 25));
		jPanel2.setBounds(new Rectangle(6, 86, 449, 30));
		jPanel2.setLayout(null);
		jpWindows.setBounds(new Rectangle(6, 86, 449, 120));
		jpWindows.setLayout(null);

		this.add(jComboBoxType);
		this.add(jLabelStyle, null);
		this.add(jTextFieldVersion);
		this.add(jLabelVersion);
		this.add(jSpinnerPort);
		this.add(jLabelSystem);
		this.add(jComboBoxSystem);
		this.add(jLabelPort);
		this.add(jPanel1);
		jPanel1.add(jSpinnerTimeout);
		jPanel1.add(jLabelTimeout);
		jPanel1.add(jLabelPrompt);
		jPanel1.add(jTextFieldPrompt);
		jPanel1.add(jPassword);
		jPanel1.add(jLabelUser);
		jPanel1.add(jTextFieldUser);
		jPanel1.add(jLabelPassword);
		jPanel1.add(jLabel1);
		jPanel1.add(jTextFieldLoginPrompt);
		jPanel1.add(jLabel2);
		jPanel1.add(jTextFieldPasswordPrompt);
		jPanel1.add(jLabelTermType);
		jPanel1.add(jTextFieldTermType);
		this.add(jPanel2);
		jPanel2.setVisible(false);
		this.add(jpWindows);
		jpWindows.add(jlWindowsUser);
		jpWindows.add(jtfWindowsUser);
		jpWindows.add(jlWindowsPasswd);
		jpWindows.add(jPasswdWindows);
		jpWindows.setVisible(false);
		this.add(jButtonCheckSetting);
		jTextFieldPrompt.setText("#");
		jTextFieldUser.setText("root");

		jComboBoxSystem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String os = (String) jComboBoxSystem.getSelectedItem();
				if (os == null)
					os = "";
				if (os.equalsIgnoreCase("AIX")) {
					jTextFieldPasswordPrompt.setText("root's Password:");
				} else if (os.equalsIgnoreCase("Windows")) {
					jTextFieldPasswordPrompt.setText("");
				} else {
					jTextFieldPasswordPrompt.setText("Password:");
				}
			}
		});

		jButtonCheckSetting.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CLITestResult result;
				try {
					result = (CLITestResult) context.getServerFactory().getProbeService()
							.executeMethod(context.getNode().getProbeId(), CLITester.class.getName(), "doTest",
									context.getNode(), getOptions());
				} catch (Exception e2) {
					ErrorDlg.show("远程探针执行方法失败:" + monitorNode, e2);
					return;
				}
				Object errorMessage = result.getError();
				if (errorMessage != null && !errorMessage.equals("")) {
					ErrorDlg.show(errorMessage.toString());
					return;
				}

				String os = result.getOs();
				String version = result.getVersion();

				String sysname = ((String) jComboBoxSystem.getSelectedItem()).toLowerCase();
				if (((String) jComboBoxSystem.getSelectedItem()).equalsIgnoreCase("SCOUNIX"))
					sysname = "sco";
				if (os.toLowerCase().indexOf(sysname) == -1) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(CLIConfPanel.this,
							"检测到的系统名称与指定的系统名称不一致\n是-继续\n否-重新设置", "询问", JOptionPane.YES_NO_OPTION)) {
						return;
					}
				}

				String fullPrompt = result.getFullPrompt();
				jTextFieldPrompt.setFullPrompt(fullPrompt);
				//修改为自动获取版本号
				jTextFieldVersion.setText(version);
			
			}

		});
	}

	private void setComboBoxType() {
		String sysName = jComboBoxSystem.getSelectedItem() == null ? "" : jComboBoxSystem.getSelectedItem().toString();
		Object obj = jComboBoxType.getSelectedItem();
		String session = (obj == null ? "" : obj.toString());
		jComboBoxType.removeAllItems();
		if (agentOnly) {
			jComboBoxType.addItem(CLIConstant.SESSION_AGENT);
			return;
		}
		boolean isWindows = sysName.equalsIgnoreCase("Windows");
		chgVersinoPosition(isWindows);
		if (isWindows) {
			jComboBoxType.addItem(CLIConstant.SESSION_WMI);
			jComboBoxType.addItem(CLIConstant.SESSION_AGENT);
		} else {
			jComboBoxType.addItem(CLIConstant.SESSION_TELNET);
			jComboBoxType.addItem(CLIConstant.SESSION_AGENT);
			jComboBoxType.addItem(CLIConstant.SESSION_SSH);
			jComboBoxType.setSelectedItem(session);
		}
	}

	/**
	 * 改变版本信息的位置 如果为windows系统，那么隐藏端口信息，并将版本信息移动到端口信息的位置，
	 * 如果为非windows系统，那么显示端口信息，并将版本信息移动到原来应该在的位置
	 * 
	 * @param isWdinows
	 */
	private void chgVersinoPosition(boolean isWdinows) {
		if (isWdinows) {
			// 隐藏端口信息
			jLabelPort.setVisible(false);
			jSpinnerPort.setVisible(false);
			// 移动版本信息位置
			jLabelVersion.setBounds(new Rectangle(9, 57, 53, 20));
			jTextFieldVersion.setBounds(new Rectangle(80, 57, 104, 25));
		} else {
			// 显示端口信息
			jLabelPort.setVisible(true);
			jSpinnerPort.setVisible(true);
			// 移动版本信息位置
			jLabelVersion.setBounds(new Rectangle(222, 57, 53, 20));
			jTextFieldVersion.setBounds(new Rectangle(301, 57, 104, 25));
		}
	}

	public void setOptions(CLIMonitorMethodOption options) {
		jComboBoxSystem.setSelectedItem(options.getSysname());
		jComboBoxType.setSelectedItem(options.getSessionName());
		snm_port.setValue(options.getRemotePort());
		jTextFieldPrompt.setText(StringUtil.convertNull2Blank(options.getPrompt()));
		if (options.getSessionName().toString().equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
			jtfWindowsUser.setText(StringUtil.convertNull2Blank(options.getLoginName()));
			jPasswdWindows.setText(StringUtil.convertNull2Blank(options.getPassword()));
		} else {
			jTextFieldUser.setText(StringUtil.convertNull2Blank(options.getLoginName()));
			jPassword.setText(StringUtil.convertNull2Blank(options.getPassword()));
		}
		snm_timeout.setValue(options.getLoginTimeout());
		jTextFieldVersion.setText(StringUtil.convertNull2Blank(options.getSysversion()));
		jTextFieldLoginPrompt.setText(StringUtil.convertNull2Blank(options.getLoginPrompt()));
		jTextFieldPasswordPrompt.setText(StringUtil.convertNull2Blank(options.getPasswdPrompt()));
		jTextFieldTermType.setText(StringUtil.convertNull2Blank(options.getTerminalType()));
	}

	public CLIMonitorMethodOption getOptions() {
		CLIMonitorMethodOption properties = new CLIMonitorMethodOption();
		String sessionName = (String) jComboBoxType.getSelectedItem();
		properties.setSessionName(sessionName);
		properties.setSysname((String) jComboBoxSystem.getSelectedItem());
		properties.setSysversion(jTextFieldVersion.getText());
		properties.setRemotePort(snm_port.getNumber().intValue());

		if (sessionName.equalsIgnoreCase(CLIConstant.SESSION_TELNET)
				|| sessionName.equalsIgnoreCase(CLIConstant.SESSION_SSH)) {
			properties.setLoginName(jTextFieldUser.getText());
			properties.setPassword(new String(jPassword.getPassword()));
			properties.setPrompt(jTextFieldPrompt.getText());
			properties.setLoginTimeout(snm_timeout.getNumber().intValue());
			properties.setLoginPrompt(jTextFieldLoginPrompt.getText());
			properties.setPasswdPrompt(jTextFieldPasswordPrompt.getText());
			properties.setLoginTimeout(snm_timeout.getNumber().intValue());
			properties.setTerminalType(jTextFieldTermType.getText());
		} else if (sessionName.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
			properties.setLoginName(jtfWindowsUser.getText());
			properties.setPassword(new String(jPasswdWindows.getPassword()));
		}
		return properties;
	}

	public boolean verify() {
		if (StringUtil.isNullOrBlank(jTextFieldVersion.getText())) {
			jTextFieldVersion.setText("all");
		}

		String sessionType = (String) jComboBoxType.getSelectedItem();
		if(sessionType.equalsIgnoreCase(CLIConstant.SESSION_AGENT)){
			return true;
		}else	if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
			return FieldValidator.textRequired(this, jtfWindowsUser, "用户名");
		} else {
			return FieldValidator.textRequired(this, jTextFieldUser, "用户名");
		}

	}

	public void setAgentOptionOnly(String host) {
		agentOnly = true;
		setComboBoxType();
		jButtonCheckSetting.setBounds(new Rectangle(179, 140, 100, 23));
	}

	public void setMonitorNode(MonitorNode monitorNode) {
		this.monitorNode = monitorNode;
	}
}

class JPromptPanel extends JComboBox implements FocusListener, MouseListener, DocumentListener {
	private static final long serialVersionUID = 1L;
	private JTextComponent editor;
	private Color oldColor;
	private String fullPrompt;
	private boolean isFullPromptSetted = true;

	public JPromptPanel() {
		super(new String[] { "完整提示符" });
		setUI(new BasicComboBoxUI() {
			@Override
			protected JButton createArrowButton() {
				return new JButton() {
					private static final long serialVersionUID = 1L;

					@Override
					public int getWidth() {
						return 0;
					}
				};
			}

			@Override
			protected ComboBoxEditor createEditor() {
				return new ComboBoxEditor();
			}
		});
		setEditable(true);
		editor = (JTextComponent) getEditor().getEditorComponent();
		oldColor = editor.getBackground();
		editor.addFocusListener(this);
		editor.addMouseListener(this);
		editor.getDocument().addDocumentListener(this);
	}

	protected void onFocus() {
		if (isShowing() && isPopupVisible() == isFullPromptSetted)
			setPopupVisible(!isFullPromptSetted);
	}

	protected void onChanged() {
		boolean setted = fullPrompt == null || StringUtils.equals(fullPrompt, editor.getText());
		if (isFullPromptSetted == setted)
			return;
		if (setted) {
			editor.setBackground(oldColor);
			editor.setToolTipText(null);
		} else {
			editor.setBackground(Color.ORANGE);
			StringBuilder sb = new StringBuilder();
			String prompt = fullPrompt.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
			sb.append("<html>您设置的命令提示符不完整，请重新设置！<br>检测到完整的的命令提示符是：<strong>").append(prompt).append("</strong>");
			if (fullPrompt.endsWith(" "))
				sb.append("<br>注意：完整提示符的末尾带有空格。");
			sb.append("</html>");
			editor.setToolTipText(sb.toString());
		}
		isFullPromptSetted = setted;
		onFocus();
	}

	public void focusGained(FocusEvent e) {
		onFocus();
	}

	public void focusLost(FocusEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
		onFocus();
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("comboBoxEdited".equals(e.getActionCommand())) {
			if (fullPrompt == null)
				setPopupVisible(false);
			else
				setText(fullPrompt);
		}
	}

	public String getFullPrompt() {
		return fullPrompt;
	}

	public void setFullPrompt(String fullPrompt) {
		if (!StringUtils.equals(this.fullPrompt, fullPrompt)) {
			this.fullPrompt = fullPrompt;
			onChanged();
		}
	}

	public String getText() {
		return editor.getText();
	}

	public void setText(String text) {
		editor.setText(text);
		onChanged();
	}

	private static class ComboBoxEditor extends BasicComboBoxEditor.UIResource {
		public ComboBoxEditor() {
			editor = new JTextField();
		}
	}

	public void insertUpdate(DocumentEvent e) {
		onChanged();
	}

	public void removeUpdate(DocumentEvent e) {
		onChanged();
	}

	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void configureEditor(javax.swing.ComboBoxEditor anEditor, Object anItem) {
		if (anItem != null && fullPrompt != null)
			setText(fullPrompt);
	}
}
