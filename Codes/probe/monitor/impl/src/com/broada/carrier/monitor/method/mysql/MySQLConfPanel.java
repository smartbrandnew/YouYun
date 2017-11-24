package com.broada.carrier.monitor.method.mysql;

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

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2008-5-30 下午02:10:20
 */
public class MySQLConfPanel extends BaseMethodConfiger {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -2039152720784258305L;

	private SpinnerNumberModel snm_port = new SpinnerNumberModel(3306, 1, 65535, 1);

	JLabel jLabel1 = new JLabel();

	// 字符编码,默认是GBK,也可能是UTF-8等其他方式
	JTextField jTextFieldEncoding = new JTextField("GBK");

	JLabel jLabel2 = new JLabel();

	JTextField jTextFieldUsername = new JTextField("root");

	JLabel jLabel3 = new JLabel();

	JPasswordField jPasswordFieldPassword = new JPasswordField();

	JLabel jLabel4 = new JLabel();

	JButton jButtonTest = new JButton(" 测 试 ");

	JSpinner jSpinnerPort = new JSpinner(snm_port);

	public MySQLConfPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ErrorDlg.createErrorDlg(this, "错误", "初始化出错", ex).setVisible(true);
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(null);

		jLabel2.setText("用户名");
		jLabel2.setBounds(new Rectangle(43, 40, 42, 22));
		jTextFieldUsername.setBounds(new Rectangle(149, 40, 115, 22));

		jLabel3.setText("密码");
		jLabel3.setBounds(new Rectangle(43, 70, 42, 22));
		jPasswordFieldPassword.setBounds(new Rectangle(149, 70, 115, 22));

		jLabel4.setText("端口");
		jLabel4.setBounds(new Rectangle(43, 10, 42, 22));
		jSpinnerPort.setBounds(new Rectangle(149, 10, 114, 22));

		jLabel1.setText("字符编码");
		jLabel1.setBounds(new Rectangle(43, 100, 84, 22));
		jTextFieldEncoding.setBounds(new Rectangle(149, 100, 115, 22));

		jButtonTest.setBounds(new Rectangle(43, 130, 80, 24));
		this.add(jTextFieldUsername);
		this.add(jLabel2);
		this.add(jLabel3);
		this.add(jPasswordFieldPassword);
		this.add(jSpinnerPort);
		this.add(jLabel4);
		this.add(jLabel1);
		this.add(jTextFieldEncoding);
		this.add(jButtonTest);

		jButtonTest.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String result = "";
				try {
	 				if (!verify())
	 					return;
	 				
	 				MySQLMonitorMethodOption method = getOptions();
					if (method == null)
						return;					
					result = (String) getContext().getServerFactory().getProbeService().executeMethod(getContext().getNode().getProbeId(), MySQLTester.class.getName(), "doTest",
							getContext().getNode().getIp(), (Integer)method.getPort(), method.getEncoding(), method.getUsername(), method.getPassword());
				} catch (Exception e2) {
					ErrorDlg.createErrorDlg(MySQLConfPanel.this, "远程探针执行方法失败,ProbeCode:" + getContext().getNode().getProbeId(), e2).setVisible(true);
					return;
				}
				if (Boolean.valueOf(result)) {
					JOptionPane.showMessageDialog(MySQLConfPanel.this, "成功连接到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
				} else {
					Exception exception = new Exception(result);
					ErrorDlg.createErrorDlg(MySQLConfPanel.this, "错误信息", "测试数据库链接失败,Host:" + getContext().getNode().getIp(), exception).setVisible(true);
				}

			}
		});
	}

	public MySQLMonitorMethodOption getOptions() {
		MySQLMonitorMethodOption option = new MySQLMonitorMethodOption();
		option.setUsername(jTextFieldUsername.getText());
		option.setPassword(new String(jPasswordFieldPassword.getPassword()));
		option.setPort(snm_port.getNumber().intValue());
		option.setEncoding(jTextFieldEncoding.getText().trim());
		return option;
	}

	public void setOptions(MySQLMonitorMethodOption option) {
		jTextFieldUsername.setText(option.getUsername());
		jPasswordFieldPassword.setText(option.getPassword());
		snm_port.setValue(new Integer(option.getPort()));
		jTextFieldEncoding.setText(option.getEncoding());

	}

	public boolean verify() {
		return FieldValidator.textRequired(this, jTextFieldUsername, "用户名")
				&& FieldValidator.textRequired(this, jTextFieldEncoding, "字符编码");
	}

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		MySQLMonitorMethodOption method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new MySQLMonitorMethodOption(method));		
	}

}
