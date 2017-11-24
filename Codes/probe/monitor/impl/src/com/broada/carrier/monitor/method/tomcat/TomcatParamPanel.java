package com.broada.carrier.monitor.method.tomcat;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TomcatParamPanel extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;

  private SpinnerNumberModel snm_port = new SpinnerNumberModel(8080, 1, 65535, 1);

  String host = "";

  JLabel jLabel2 = new JLabel();

  JTextField jTextFieldUsername = new JTextField();

  JLabel jLabel3 = new JLabel();

  JPasswordField jPasswordFieldPassword = new JPasswordField();

  JLabel jLabel4 = new JLabel();

  JButton jButtonTest = new JButton(" 测 试 ");

  JSpinner jSpinnerPort = new JSpinner(snm_port);

  public TomcatParamPanel() {
    try {
      jbInit();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);

    jLabel2.setText("用户名");
    jLabel2.setBounds(new Rectangle(43, 91, 42, 15));
    jTextFieldUsername.setBounds(new Rectangle(149, 91, 115, 20));

    jLabel3.setText("密码");
    jLabel3.setBounds(new Rectangle(43, 133, 42, 15));
    jPasswordFieldPassword.setBounds(new Rectangle(149, 133, 115, 20));

    jLabel4.setText("端口");
    jLabel4.setBounds(new Rectangle(43, 49, 42, 15));
    jSpinnerPort.setBounds(new Rectangle(149, 49, 114, 20));

    jButtonTest.setBounds(new Rectangle(43, 196, 80, 24));
    this.add(jTextFieldUsername);
    this.add(jLabel2);
    this.add(jLabel3);
    this.add(jPasswordFieldPassword);
    this.add(jSpinnerPort);
    this.add(jLabel4);
    this.add(jButtonTest);

    jButtonTest.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!verify()){
          return;
        }
        
        try {
        	TomcatMonitorMethodOption method = getOptions();
        	if (method == null)
        		return;
        	
        	Object ret = (String) getContext().getServerFactory().getProbeService()
    					.executeMethod(getContext().getNode().getProbeId(), TomcatTest4Probe.class.getName(), "test",
    							getContext().getNode().getIp(), method);
          if(null==ret){
            JOptionPane.showMessageDialog(TomcatParamPanel.this, "成功连接到Tomcat", "成功", JOptionPane.INFORMATION_MESSAGE);
            return;
          }else{
            throw new Exception(ret.toString());
          }
        } catch (Throwable t) {
          ErrorDlg.createErrorDlg(TomcatParamPanel.this, "测试Tomcat链接失败,请检查访问参数.", t).setVisible(true);;
          return;
        }
      }
    });
  }

  public TomcatMonitorMethodOption getOptions() {
    TomcatMonitorMethodOption option = new TomcatMonitorMethodOption();
    option.setUsername(jTextFieldUsername.getText());
    option.setPassword(new String(jPasswordFieldPassword.getPassword()));
    option.setPort(snm_port.getNumber().intValue());
    return option;
  }

  public void setOptions(TomcatMonitorMethodOption option) {
    jTextFieldUsername.setText(option.getUsername());
    jPasswordFieldPassword.setText(option.getPassword());
    snm_port.setValue(new Integer(option.getPort()));
  }

  public boolean verify() {
    return FieldValidator.textRequired(this, jTextFieldUsername, "用户名")
        && FieldValidator.textRequired(this, jPasswordFieldPassword, "密码");
  }

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		TomcatMonitorMethodOption method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new TomcatMonitorMethodOption(method));		
	}  	 
}