package com.broada.carrier.monitor.method.jdbc;

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

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class JdbcMethodConfiger extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;
	private SpinnerNumberModel snm_port = new SpinnerNumberModel(1521, 1, 65535, 1);
	private JLabel jLabel1 = new JLabel();
	private JTextField jTextFieldSid = new JTextField();
	private JLabel jLabel2 = new JLabel();
	private JTextField jTextFieldUsername = new JTextField();
	private JLabel jLabel3 = new JLabel();
	private JPasswordField jPasswordFieldPassword = new JPasswordField();
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();
	private JComboBox jComboBoxType = new JComboBox();
	JLabel jLabel6 = new JLabel();
	// 字符编码,默认是GBK,也可能是UTF-8等其他方式
	JTextField jTextFieldEncoding = new JTextField("GBK");
	
	private JButton jButtonTest = new JButton(" 测 试 ");
	private JSpinner jSpinnerPort = new JSpinner(snm_port);

	public JdbcMethodConfiger() {
		this.setLayout(null);
		
		jLabel5.setText("数据库");
		jLabel5.setBounds(new Rectangle(43, 49, 51, 15));
		jComboBoxType.setBounds(new Rectangle(149, 49, 115, 20));
		jComboBoxType.addItem("oracle");
		jComboBoxType.addItem("mysql");
		jComboBoxType.addItem("sybase");
		jComboBoxType.setSelectedIndex(0);
		jLabel6.setVisible(false);
		jTextFieldEncoding.setVisible(false);
		jComboBoxType.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if ("oracle".equals(e.getItem())) {
						snm_port.setValue(1521);
						jLabel6.setVisible(false);
						jTextFieldEncoding.setVisible(false);
						jTextFieldSid.setText("orcl");
					} else if ("mysql".equals(e.getItem())) {
						snm_port.setValue(3306);
						jLabel6.setVisible(true);
						jTextFieldEncoding.setVisible(true);
						jTextFieldSid.setText("carrier");
					} else if ("sybase".equals(e.getItem())) {
						snm_port.setValue(5000);
						jLabel6.setVisible(false);
						jTextFieldEncoding.setVisible(false);
						jTextFieldSid.setText("carrier");
					}
				}
			}
		});
		
		
		jLabel1.setText("数据库/实例名称");
		jLabel1.setBounds(new Rectangle(43, 130, 101, 15));
		jTextFieldSid.setBounds(new Rectangle(149, 130, 115, 20));

		jLabel2.setText("用户名");
		jLabel2.setBounds(new Rectangle(43, 175, 42, 15));
		jTextFieldUsername.setBounds(new Rectangle(149, 175, 115, 20));

		jLabel3.setText("密码");
		jLabel3.setBounds(new Rectangle(43, 220, 42, 15));
		jPasswordFieldPassword.setBounds(new Rectangle(149, 220, 115, 20));

		jLabel4.setText("端口");
		jLabel4.setBounds(new Rectangle(43, 85, 42, 15));
		jSpinnerPort.setBounds(new Rectangle(149, 85, 114, 20));

		jLabel6.setText("字符编码");
		jLabel6.setBounds(new Rectangle(43, 260, 84, 22));
		jTextFieldEncoding.setBounds(new Rectangle(149, 260, 115, 22));
		
		jButtonTest.setBounds(new Rectangle(43, 290, 80, 24));
		this.add(jTextFieldUsername);
		this.add(jLabel1);
		this.add(jTextFieldSid);
		this.add(jLabel2);
		this.add(jLabel3);
		this.add(jPasswordFieldPassword);
		this.add(jSpinnerPort);
		this.add(jLabel4);
		this.add(jButtonTest);
		this.add(jLabel5);
		this.add(jComboBoxType);
		this.add(jLabel6);
		this.add(jTextFieldEncoding);

		jButtonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value;
				try {
					JdbcMonitorMethodOption method = getJdbcMethod();
					if (method == null)
						return;					
					value = (String) getContext().getServerFactory().getProbeService()
							.executeMethod(getContext().getNode().getProbeId(), JdbcTester.class.getName(), "doTest",
									getContext().getNode().getIp(), method.getPort(), method.getSid(), method.getUsername(), method.getPassword(), method.getDbType(), method.getEncoding());
				} catch (Exception e2) {
					ErrorDlg.show(e2);
					return;
				}
				if (Boolean.parseBoolean(value)) {
					JOptionPane.showMessageDialog(JdbcMethodConfiger.this, "连接数据库测试成功");
				} else {
					JOptionPane.showMessageDialog(JdbcMethodConfiger.this, "连接数据库测试失败：" + value);
				}
			}

		});
	}
	
	private JdbcMonitorMethodOption getJdbcMethod() {
		// TODO Auto-generated method stub
		JdbcMonitorMethodOption option = new JdbcMonitorMethodOption();
		option.setSid(jTextFieldSid.getText());
		option.setUsername(jTextFieldUsername.getText());
		option.setPassword(new String(jPasswordFieldPassword.getPassword()));
		option.setPort(snm_port.getNumber().intValue());
		option.setDbType((String) jComboBoxType.getSelectedItem());
		option.setEncoding(jTextFieldEncoding.getText());
		return option;
	}

	@Override
	public boolean getData() {
		JdbcMonitorMethodOption method = getJdbcMethod();
		if (method == null)
			return false;
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	public void setData(MonitorMethod method) {
		JdbcMonitorMethodOption option = new JdbcMonitorMethodOption(method);
		jTextFieldSid.setText(option.getSid());
		jTextFieldUsername.setText(option.getUsername());
		jPasswordFieldPassword.setText(option.getPassword());
		jComboBoxType.setSelectedItem(option.getDbType());
		snm_port.setValue(new Integer(option.getPort()));
		jTextFieldEncoding.setText(option.getEncoding());
	}
}
