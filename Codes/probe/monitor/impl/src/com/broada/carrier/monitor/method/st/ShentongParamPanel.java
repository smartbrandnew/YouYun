package com.broada.carrier.monitor.method.st;

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
import com.broada.utils.FieldValidator;

public class ShentongParamPanel extends BaseMethodConfiger {
	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -8455418386307532067L;
	private SpinnerNumberModel snm_port = new SpinnerNumberModel(5000, 1, 65535, 1);
  JLabel jLabel1 = new JLabel();
  
	JTextField jTextFieldSid = new JTextField();
	
  JLabel jLabel2 = new JLabel();

  JTextField jTextFieldUsername = new JTextField("sa");

  JLabel jLabel3 = new JLabel();

  JPasswordField jPasswordFieldPassword = new JPasswordField();

  JLabel jLabel4 = new JLabel();

  JButton jButtonTest = new JButton(" 测 试 ");

  JSpinner jSpinnerPort = new JSpinner(snm_port);

  public ShentongParamPanel() {
    try {
      jbInit();
    } catch (Exception ex) {
      ErrorDlg.createErrorDlg(this, "错误", "初始化出错", ex).setVisible(true);
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);
    jLabel1.setText("实例名称");
	jLabel1.setBounds(new Rectangle(43, 100, 42, 22));
	jTextFieldSid.setBounds(new Rectangle(149, 100, 115, 22));
		
    jLabel2.setText("用户名");
    jLabel2.setBounds(new Rectangle(43, 40, 42, 22));
    jTextFieldUsername.setBounds(new Rectangle(149, 40, 115, 22));

    jLabel3.setText("密码");
    jLabel3.setBounds(new Rectangle(43, 70, 42, 22));
    jPasswordFieldPassword.setBounds(new Rectangle(149, 70, 115, 22));

    jLabel4.setText("端口");
    jLabel4.setBounds(new Rectangle(43, 10, 42, 22));
    jSpinnerPort.setBounds(new Rectangle(149, 10, 114, 22));

    jButtonTest.setBounds(new Rectangle(43, 130, 80, 24));
    this.add(jLabel1);
    this.add(jTextFieldSid);
    this.add(jTextFieldUsername);
    this.add(jLabel2);
    this.add(jLabel3);
    this.add(jPasswordFieldPassword);
    this.add(jSpinnerPort);
    this.add(jLabel4);
    this.add(jButtonTest);

    jButtonTest.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!verify())
          return;
 			String result = "";
 			try {
 				ShentongMethod method = getOptions();
 				if (method == null)
 					return;
 				
				result = (String) getContext().getServerFactory().getProbeService()
						.executeMethod(getContext().getNode().getProbeId(), ShentongTester.class.getName(), "doTest",
								getContext().getNode().getIp(), method);
 			} catch (Exception e2) {
 				ErrorDlg.createErrorDlg(ShentongParamPanel.this, "远程探针执行方法失败,ProbeCode:" + getContext().getNode().getProbeId(), e2).setVisible(true);
 			  return;
 			}
 			if(Boolean.valueOf(result)){
 				JOptionPane.showMessageDialog(ShentongParamPanel.this, "成功连接到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
 			}else {
 				Exception exception = new Exception(result);
 				ErrorDlg.createErrorDlg(ShentongParamPanel.this, "错误信息","测试数据库链接失败,Host:" + getContext().getNode().getIp(), exception).setVisible(true);
 			}
        
      }
    });
  }

  public ShentongMethod getOptions() {
  	ShentongMethod option = new ShentongMethod();
    option.setUsername(jTextFieldUsername.getText());
    option.setPassword(new String(jPasswordFieldPassword.getPassword()));
    option.setPort(snm_port.getNumber().intValue());
    option.setSid(jTextFieldSid.getText());
    return option;
  }

  public void setOptions(ShentongMethod option) {
    jTextFieldUsername.setText(option.getUsername());
    jPasswordFieldPassword.setText(option.getPassword());
    snm_port.setValue(new Integer(option.getPort()));
    jTextFieldSid.setText(option.getSid());
  }

  public boolean verify() {
    return FieldValidator.textRequired(this, jTextFieldUsername, "用户名");
  }
	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		ShentongMethod method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new ShentongMethod(method));
	}
}
