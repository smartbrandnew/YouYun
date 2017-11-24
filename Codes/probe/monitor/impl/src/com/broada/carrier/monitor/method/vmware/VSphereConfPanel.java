package com.broada.carrier.monitor.method.vmware;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;

public class VSphereConfPanel extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;
	
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JTextField jTxtUserName = new JTextField("administrator");
	JPasswordField jPwdPassword = new JPasswordField();
	JButton jButtonTest = new JButton(" 测 试 ");

	public VSphereConfPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ErrorDlg.createErrorDlg(this, "错误", "初始化出错", ex).setVisible(true);
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(null);

		jLabel1.setText("用户名");
		jLabel1.setBounds(new Rectangle(43, 10, 42, 22));
		jTxtUserName.setBounds(new Rectangle(149, 10, 115, 22));

		jLabel2.setText("密码");
		jLabel2.setBounds(new Rectangle(43, 40, 42, 22));
		jPwdPassword.setBounds(new Rectangle(149, 40, 115, 22));

		jButtonTest.setBounds(new Rectangle(43, 130, 80, 24));
		this.add(jLabel1);
		this.add(jLabel2);
		this.add(jTxtUserName);
		this.add(jPwdPassword);
		this.add(jButtonTest);

		jButtonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doTest();
			}
		});
	}

	private void doTest() {
		if (!this.verify()) {
			return;
		}

		Object callResult = null;
		try {
			String hostIpAddr = getContext().getNode().getIp();
			String userName = jTxtUserName.getText();
			String password = new String(jPwdPassword.getPassword());
			
			callResult = getContext().getServerFactory().getProbeService().executeMethod(getContext().getNode().getProbeId(), 
					VSphereConnection.class.getName(), "testConnect", hostIpAddr, userName, password);
			if (callResult == null) {
				JOptionPane.showMessageDialog(this, "成功连接到vSphere SDK", "成功", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, callResult, "失败", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e2) {
			ErrorDlg.createErrorDlg(this, "调用VSphereConnection.testConnect方法失败.", e2).setVisible(true);;
		}
	}

	public boolean verify() {
		if (jTxtUserName.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "用户名不能为空.");
			jTxtUserName.requestFocus();
			return false;
		}
		String pass = new String(jPwdPassword.getPassword());
		if (pass.trim().length() == 0) {
			JOptionPane.showMessageDialog(this, "密码不能为空.");
			jPwdPassword.requestFocus();
			return false;
		}
		return true;
	}

	public VSphereMonitorMethodOption getOptions() {
		VSphereMonitorMethodOption option = new VSphereMonitorMethodOption();
		option.setUsername(jTxtUserName.getText());
		option.setPassword(new String(jPwdPassword.getPassword()));
		return option;
	}

	public void setOptions(VSphereMonitorMethodOption option) {
		jTxtUserName.setText(option.getUsername());
		jPwdPassword.setText(option.getPassword());
	}

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		VSphereMonitorMethodOption method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new VSphereMonitorMethodOption(method));		
	}  	
}
