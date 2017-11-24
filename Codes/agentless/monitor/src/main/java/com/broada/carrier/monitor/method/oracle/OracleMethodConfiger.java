package com.broada.carrier.monitor.method.oracle;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class OracleMethodConfiger extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;
	private SpinnerNumberModel snm_port = new SpinnerNumberModel(1521, 1, 65535, 1);
	private JLabel jLabel1 = new JLabel();
	private JTextField jTextFieldSid = new JTextField();
	private JLabel jLabel2 = new JLabel();
	private JTextField jTextFieldUsername = new JTextField();
	private JLabel jLabel3 = new JLabel();
	private JPasswordField jPasswordFieldPassword = new JPasswordField();
	private JLabel jLabel4 = new JLabel();
	private JButton jButtonTest = new JButton(" 测 试 ");
	private JSpinner jSpinnerPort = new JSpinner(snm_port);

	public OracleMethodConfiger() {
		this.setLayout(null);
		jLabel1.setText("实例名称");
		jLabel1.setBounds(new Rectangle(43, 85, 51, 15));
		jTextFieldSid.setBounds(new Rectangle(149, 85, 115, 20));

		jLabel2.setText("用户名");
		jLabel2.setBounds(new Rectangle(43, 129, 42, 15));
		jTextFieldUsername.setBounds(new Rectangle(149, 129, 115, 20));

		jLabel3.setText("密码");
		jLabel3.setBounds(new Rectangle(43, 171, 42, 15));
		jPasswordFieldPassword.setBounds(new Rectangle(149, 171, 115, 20));

		jLabel4.setText("端口");
		jLabel4.setBounds(new Rectangle(43, 49, 42, 15));
		jSpinnerPort.setBounds(new Rectangle(149, 49, 114, 20));

		jButtonTest.setBounds(new Rectangle(43, 196, 80, 24));
		this.add(jTextFieldUsername);
		this.add(jLabel1);
		this.add(jTextFieldSid);
		this.add(jLabel2);
		this.add(jLabel3);
		this.add(jPasswordFieldPassword);
		this.add(jSpinnerPort);
		this.add(jLabel4);
		this.add(jButtonTest);

		jButtonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value;
				try {
					OracleMethod method = getOracleMethod();
					if (method == null)
						return;					
					value = (String) getContext().getServerFactory().getProbeService()
							.executeMethod(getContext().getNode().getProbeId(), OracleTester.class.getName(), "doTest",
									getContext().getNode().getIp(), method.getPort(), method.getSid(), method.getUsername(), method.getPassword());
				} catch (Exception e2) {
					ErrorDlg.show(e2);
					return;
				}
				if (Boolean.parseBoolean(value)) {
					JOptionPane.showMessageDialog(OracleMethodConfiger.this, "连接数据库测试成功");
				} else {
					JOptionPane.showMessageDialog(OracleMethodConfiger.this, "连接数据库测试失败：" + value);
				}
			}
		});
	}

	private OracleMethod getOracleMethod() {
		OracleMethod option = new OracleMethod();
		option.setSid(jTextFieldSid.getText());
		option.setUsername(jTextFieldUsername.getText());
		option.setPassword(new String(jPasswordFieldPassword.getPassword()));
		option.setPort(snm_port.getNumber().intValue());
		return option;
	}

	@Override
	public boolean getData() {
		OracleMethod method = getOracleMethod();
		if (method == null)
			return false;
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	public void setData(MonitorMethod method) {
		OracleMethod option = new OracleMethod(method);
		jTextFieldSid.setText(option.getSid());
		jTextFieldUsername.setText(option.getUsername());
		jPasswordFieldPassword.setText(option.getPassword());
		snm_port.setValue(new Integer(option.getPort()));
	}
}
