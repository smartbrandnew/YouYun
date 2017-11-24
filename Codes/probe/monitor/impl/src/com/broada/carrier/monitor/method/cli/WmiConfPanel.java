package com.broada.carrier.monitor.method.cli;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.WmiMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;
import com.broada.utils.FieldValidator;
import com.broada.utils.StringUtil;

public class WmiConfPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private SpinnerNumberModel snm_port = new SpinnerNumberModel(22, 1, 65535, 1);

	JSpinner jSpinnerPort = new JSpinner(snm_port);

	JLabel jLabelStyle = new JLabel();

	JComboBox jComboBoxType = new JComboBox();

	JLabel jLabelPort = new JLabel();

	JLabel jLabelPrompt = new JLabel();

	JPromptPanel jTextFieldPrompt = new JPromptPanel();

	JLabel jlWindowsUser = new JLabel();

	JTextField jtfWindowsUser = new JTextField();

	JLabel jlWindowsPasswd = new JLabel();

	JPasswordField jPasswdWindows = new JPasswordField();

	JLabel jLabelSystem = new JLabel();

	JLabel jLabelVersion = new JLabel();

	JTextField jTextFieldVersion = new JTextField();

	private JButton jButtonCheckSetting = new JButton();

	TitledBorder titledBorder = new TitledBorder("");

	JPanel jPanel2 = new JPanel();

	JPanel jpWindows = new JPanel();

	private MonitorNode monitorNode;

	protected MonitorMethodConfigContext context;

	public WmiConfPanel() {
		try {
			jbInit();
			init();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
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
				if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_AGENT)) {
					snm_port.setValue(new Integer(1850));
					jpWindows.setVisible(false);
					jPanel2.validate();
					jPanel2.setVisible(true);
				} else if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
					snm_port.setValue(new Integer(161));
					jPanel2.setVisible(false);
					jpWindows.validate();
					jpWindows.setVisible(true);
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

		jlWindowsUser.setText("登录用户名");
		jlWindowsUser.setBounds(new Rectangle(3, 2, 60, 20));
		jtfWindowsUser.setBounds(new Rectangle(74, 2, 104, 25));

		jlWindowsPasswd.setText("登录密码");
		jlWindowsPasswd.setBounds(new Rectangle(216, 2, 62, 20));
		jPasswdWindows.setBounds(new Rectangle(295, 2, 104, 25));

		jLabelVersion.setText("系统版本");
		jLabelVersion.setBounds(new Rectangle(222, 57, 53, 20));
		jTextFieldVersion.setBounds(new Rectangle(301, 57, 104, 25));
		jButtonCheckSetting.setBounds(new Rectangle(179, 215, 100, 23));
		jButtonCheckSetting.setText("检查设置");
		this.setBorder(titledBorder);

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
		this.add(jLabelPort);
		this.add(jPanel2);
		jPanel2.setVisible(false);
		this.add(jpWindows);
		jpWindows.add(jlWindowsUser);
		jpWindows.add(jtfWindowsUser);
		jpWindows.add(jlWindowsPasswd);
		jpWindows.add(jPasswdWindows);
		jpWindows.setVisible(false);
		this.add(jButtonCheckSetting);

		jButtonCheckSetting.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				CLITestResult result;
				try {
					result = (CLITestResult) context.getServerFactory().getProbeService()
							.executeMethod(context.getNode().getProbeId(), CLITester.class.getName(), "doTest",
									context.getNode(), getCliOptions());
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

				String sysname = "windows";
				if (os.toLowerCase().indexOf(sysname) == -1) {
					JOptionPane.showMessageDialog(WmiConfPanel.this, "检测到当前系统非Windows系统，不能使用WMI面板进行配置。", "提示",
							JOptionPane.INFORMATION_MESSAGE);
				}

				String fullPrompt = result.getFullPrompt();
				jTextFieldPrompt.setFullPrompt(fullPrompt);

				String message = "";
				if (StringUtil.isNullOrBlank(jTextFieldVersion.getText())) {
					message = "您没有指定系统版本,自动获取到的版本为:" + version + ",是否设置版本号?";
				} else {
					message = "自动获取到的版本为:" + version + ",您设置的版本为:" + jTextFieldVersion.getText() + ",是否替换成自动获取的版本号";
				}
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(WmiConfPanel.this, message, "询问",
						JOptionPane.YES_NO_OPTION)) {
					jTextFieldVersion.setText(version);
				}
			}

		});
	}

	private void setComboBoxType() {
		jComboBoxType.removeAllItems();
		chgVersinoPosition(true);
		jComboBoxType.addItem(CLIConstant.SESSION_WMI);
		jComboBoxType.addItem(CLIConstant.SESSION_AGENT);

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
			jLabelVersion.setBounds(new Rectangle(9, 24, 53, 20));
			jTextFieldVersion.setBounds(new Rectangle(80, 24, 104, 25));
		} else {
			// 显示端口信息
			jLabelPort.setVisible(true);
			jSpinnerPort.setVisible(true);
			// 移动版本信息位置
			jLabelVersion.setBounds(new Rectangle(222, 57, 53, 20));
			jTextFieldVersion.setBounds(new Rectangle(301, 57, 104, 25));
		}
	}

	public void setOptions(WmiMonitorMethodOption options) {
		jComboBoxType.setSelectedItem(options.getSessionName());
		snm_port.setValue(options.getRemotePort());
		if (options.getSessionName().toString().equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
			jtfWindowsUser.setText(StringUtil.convertNull2Blank(options.getLoginName()));
			jPasswdWindows.setText(StringUtil.convertNull2Blank(options.getPassword()));
		}
		jTextFieldVersion.setText(StringUtil.convertNull2Blank(options.getSysversion()));
	}

	public CLIMonitorMethodOption getCliOptions() {
		CLIMonitorMethodOption properties = new CLIMonitorMethodOption();
		String sessionName = (String) jComboBoxType.getSelectedItem();
		properties.setSessionName(sessionName);
		properties.setSysname("windows");
		properties.setSysversion(jTextFieldVersion.getText());
		properties.setRemotePort(snm_port.getNumber().intValue());

		if (sessionName.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
			properties.setLoginName(jtfWindowsUser.getText());
			properties.setPassword(new String(jPasswdWindows.getPassword()));
		}
		return properties;
	}

	public WmiMonitorMethodOption getOptions() {
		WmiMonitorMethodOption properties = new WmiMonitorMethodOption();
		String sessionName = (String) jComboBoxType.getSelectedItem();
		properties.setSessionName(sessionName);
		properties.setSysname("windows");
		properties.setSysversion(jTextFieldVersion.getText());
		properties.setRemotePort(snm_port.getNumber().intValue());

		if (sessionName.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
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
		if (sessionType.equalsIgnoreCase(CLIConstant.SESSION_WMI)) {
			return FieldValidator.textRequired(this, jtfWindowsUser, "用户名");
		}
		return false;
	}

	public void setContext(MonitorMethodConfigContext context) {
		this.context = context;
		setComboBoxType();
	}

	public void setAgentOptionOnly(String host) {
		setComboBoxType();
		jButtonCheckSetting.setBounds(new Rectangle(179, 140, 100, 23));
	}

	public void setMonitorNode(MonitorNode monitorNode) {
		this.monitorNode = monitorNode;
	}
}
