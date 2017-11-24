package com.broada.carrier.monitor.method.jboss;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;
import com.broada.utils.StringUtil;
import com.broada.utils.TextUtil;

public class JbossJMXPanel extends BaseMethodConfiger {

	private static final long serialVersionUID = 1L;

	private MonitorNode monitorNode = null;

	public JbossJMXPanel() {
		try {
			jbInit();
			initPanel();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(null);
		jLabel1.setText("IP地址");
		jLabel1.setBounds(new Rectangle(61, 21, 96, 15));
		jTextFieldHost.setBounds(new Rectangle(177, 18, 212, 22));
		jLabel2.setText("端口");
		jLabel2.setBounds(new Rectangle(61, 53, 96, 15));
		jSpinner1.setBounds(new Rectangle(177, 50, 80, 20));

		jLabel5.setText("用户名");
		jLabel5.setBounds(new Rectangle(61, 173, 42, 15));
		jTextFieldUsername.setBounds(new Rectangle(177, 170, 212, 22));
		jLabel6.setText("密码");
		jLabel6.setBounds(new Rectangle(61, 203, 42, 15));
		jTextFieldPassword.setBounds(new Rectangle(177, 200, 212, 22));
		jButtonTest.setBounds(new Rectangle(177, 226, 80, 24));
		jLabelVerion.setText("软件版本:");
		jLabelVerion.setBounds(new Rectangle(277, 53, 96, 15));
		jCmbVersion.setBounds(new Rectangle(357, 53, 72, 24));
		notePanel.setNote("JBOSS 4.X和5.X默认端口为1099，JBOSS 6.X使用端口8080，JBOSS 6.X-eap使用端口9990，JBOSS 7.X使用端口9990.");
		notePanel.setBounds(61, 85, 370, 80);
		this.add(jTextFieldHost);
		this.add(jLabel1);
		this.add(jLabel2);
		this.add(jSpinner1);
		this.add(jLabel3);
		this.add(jLabel4);
		this.add(jLabel5);
		this.add(jTextFieldUsername);
		this.add(jLabel6);
		this.add(jTextFieldPassword);
		this.add(jButtonTest);
		this.add(jLabel7);
		this.add(jLabelVerion);
		this.add(jCmbVersion);
		this.add(notePanel);

		jButtonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				doTest();
			}
		});
	}

	JLabel jLabel1 = new JLabel();
	JTextField jTextFieldHost = new JTextField();
	JLabel jLabel2 = new JLabel();

	private JLabel jLabelVerion = new JLabel();
	private JComboBox jCmbVersion = new JComboBox();

	JLabel jLabel3 = new JLabel();
	JLabel jLabel4 = new JLabel();
	JLabel jLabel5 = new JLabel();
	JTextField jTextFieldUsername = new JTextField();
	JLabel jLabel6 = new JLabel();
	JPasswordField jTextFieldPassword = new JPasswordField();
	private final SpinnerNumberModel snm_port = new SpinnerNumberModel(1099, 1, 65535, 1);
	JSpinner jSpinner1 = new JSpinner(snm_port);
	JButton jButtonTest = new JButton(" 测 试 ");
	JLabel jLabel7 = new JLabel();
	SimpleNotePanel notePanel = new SimpleNotePanel();

	private void initPanel() {
		jCmbVersion.addItem("4.x");
		jCmbVersion.addItem("5.x");
		jCmbVersion.addItem("6.x");
		jCmbVersion.addItem("6.x-eap");
		jCmbVersion.addItem("7.x");
		jCmbVersion.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if ("4.x".equals(e.getItem()) || "5.x".equals(e.getItem()))
						snm_port.setValue(1099);
					else if ("6.x".equals(e.getItem()))
						snm_port.setValue(8080);
					else if ("6.x-eap".equals(e.getItem()))
						snm_port.setValue(9990);
					else
						snm_port.setValue(9990);
				}
			}
		});
	}

	public void setJBossJMXOption(JbossJMXOption jbossJMXOption) {
		if (!StringUtil.isNullOrBlank(jbossJMXOption.getIpAddr()))
			jTextFieldHost.setText(jbossJMXOption.getIpAddr());
		snm_port.setValue(new Integer(jbossJMXOption.getPort()));
		jTextFieldUsername.setText(jbossJMXOption.getUsername());
		jTextFieldPassword.setText(jbossJMXOption.getPassword());
		jCmbVersion.setSelectedItem(jbossJMXOption.getVersion());

	}

	public JbossJMXOption getJBossJMXOption() {
		JbossJMXOption jbossJMXOption = new JbossJMXOption();
		jbossJMXOption.setIpAddr(jTextFieldHost.getText().trim());
		jbossJMXOption.setPassword(new String(jTextFieldPassword.getPassword()));
		jbossJMXOption.setUsername(jTextFieldUsername.getText().trim());
		jbossJMXOption.setVersion(jCmbVersion.getSelectedItem().toString());
		jbossJMXOption.setPort(snm_port.getNumber().intValue());
		return jbossJMXOption;
	}

	public boolean verify() {
		if (StringUtil.isNullOrBlank(jTextFieldUsername.getText())
				|| StringUtil.isNullOrBlank(new String(jTextFieldPassword.getPassword()))) {
			if (JOptionPane.showConfirmDialog(this, "用户名或密码不能为空",
					"警告", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
				return false;
			}
		}
		return FieldValidator.textRequired(this, jTextFieldHost, "IP地址");
	}

	private void doTest() {
		if (!verify()) {
			return;
		}
		String _host = jTextFieldHost.getText().trim();
		try {
			JbossJMXOption method = getJBossJMXOption();
			if (method == null)
				return;
			Object ret = getContext().getServerFactory().getProbeService()
					.executeMethod(getContext().getNode().getProbeId(), JbossJMXTest4Probe.class.getName(), "test",
							method.getIpAddr(), method.getPort(), method.getVersion(), method.getUsername(),
							method.getPassword());
			if (null == ret) {
				JOptionPane.showMessageDialog(JbossJMXPanel.this, "测试jboss连接成功", "成功", JOptionPane.INFORMATION_MESSAGE);
				return;
			} else {
				throw new Exception(ret.toString());
			}
		} catch (Throwable t) {
			ErrorDlg.createErrorDlg(JbossJMXPanel.this, "测试jboss连接失败,Host:" + _host, t).setVisible(true);
			return;
		}

	}

	public MonitorNode getMonitorNode() {
		return monitorNode;
	}

	public void setMonitorNode(MonitorNode monitorNode) {
		this.monitorNode = monitorNode;
	}

	@Override
	public boolean getData() {
		if (!verify())
			return false;

		JbossJMXOption method = getJBossJMXOption();
		if (method == null)
			return false;

		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		JbossJMXOption option = new JbossJMXOption(method);
		if (TextUtil.isEmpty(option.getIpAddr()))
			option.setIpAddr(getContext().getNode().getIp());
		setJBossJMXOption(option);
	}
}
