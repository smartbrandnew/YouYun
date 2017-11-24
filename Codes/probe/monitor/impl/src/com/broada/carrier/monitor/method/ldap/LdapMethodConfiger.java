package com.broada.carrier.monitor.method.ldap;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;

public class LdapMethodConfiger extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;
	private JSpinner txtPort = new JSpinner();
	private JSpinner txtVersion = new JSpinner();
	private JTextField txtBaseDN;
	private JTextField txtUsername;
	private JPasswordField txtPassword;
	private JCheckBox chkAppendBaseDn = new JCheckBox("自动加入");
	private JCheckBox chkAnonymous = new JCheckBox("匿名登录");	

	public LdapMethodConfiger() {
		setLayout(null);
		
		JLabel lblNewLabel = new JLabel("端口：");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(53, 14, 71, 15);
		add(lblNewLabel);
		
		txtPort.setBounds(135, 10, 102, 22);
		add(txtPort);
		
		JLabel label = new JLabel("版本：");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(53, 45, 71, 15);
		add(label);
		
		txtVersion.setBounds(135, 42, 141, 21);
		add(txtVersion);
		
		JLabel lblBaseDn = new JLabel("Base DN：");
		lblBaseDn.setHorizontalAlignment(SwingConstants.RIGHT);
		lblBaseDn.setBounds(53, 76, 71, 15);
		add(lblBaseDn);
		
		txtBaseDN = new JTextField();
		txtBaseDN.setColumns(10);
		txtBaseDN.setBounds(135, 73, 141, 21);
		add(txtBaseDN);
		
		chkAppendBaseDn.setBounds(295, 72, 103, 23);
		add(chkAppendBaseDn);
		
		JLabel label_1 = new JLabel("用户名：");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		label_1.setBounds(53, 107, 71, 15);
		add(label_1);
		
		txtUsername = new JTextField();
		txtUsername.setColumns(10);
		txtUsername.setBounds(135, 104, 141, 21);
		add(txtUsername);
		
		JLabel label_2 = new JLabel("密码：");
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		label_2.setBounds(53, 138, 71, 15);
		add(label_2);
		
		txtPassword = new JPasswordField();
		txtPassword.setBounds(135, 135, 141, 21);
		add(txtPassword);
		
		chkAnonymous.setBounds(295, 103, 103, 23);
		add(chkAnonymous);
	}	

	@Override
	public boolean getData() {
		LdapMethod method = new LdapMethod();
		method.setPort((Integer)txtPort.getValue());
		method.setVersion((Integer) txtVersion.getValue());
		method.setBaseDN(txtBaseDN.getText());
		method.setAppendBaseDN(chkAppendBaseDn.isSelected());
		method.setAnonymous(chkAnonymous.isSelected());
		method.setUsername(txtUsername.getText());
		method.setPassword(new String(txtPassword.getPassword()));
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		LdapMethod mm = new LdapMethod(method);
		txtPort.setValue(mm.getPort());
		txtVersion.setValue(mm.getVersion());
		txtBaseDN.setText(mm.getBaseDN());
		chkAppendBaseDn.setSelected(mm.isAppendBaseDN());
		chkAnonymous.setSelected(mm.isAnonymous());
		txtUsername.setText(mm.getUsername());
		txtPassword.setText(mm.getPassword());		
	}
}
