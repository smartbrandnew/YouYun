package com.broada.carrier.monitor.method.informix;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author lixy (lixy@broada.com.cn) Create By 2008-5-30 下午05:07:42
 */
public class InformixParamPanel extends BaseMethodConfiger {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -2999155743262491280L;

  private SpinnerNumberModel snm_port = new SpinnerNumberModel(1526, 1, 65535, 1);

  JLabel jLabel1 = new JLabel();

  JTextField jTextServerName = new JTextField();

  JLabel jLabel2 = new JLabel();

  JTextField jTextFieldUsername = new JTextField();

  JLabel jLabel3 = new JLabel();

  JPasswordField jPasswordFieldPassword = new JPasswordField();

  JLabel jLabel4 = new JLabel();

  JButton jButtonTest = new JButton(" 测 试 ");

  JSpinner jSpinnerPort = new JSpinner(snm_port);
  
  public InformixParamPanel() {
    try {
      jbInit();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);
    jLabel1.setText("服务名");
    jLabel1.setBounds(new Rectangle(43, 85, 51, 15));
    jTextServerName.setBounds(new Rectangle(149, 85, 115, 20));

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
    this.add(jTextServerName);
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
					InformixMonitorMethodOption method = getOptions();
					if (method == null)
						return;
					
					result = (String) getContext().getServerFactory().getProbeService()
							.executeMethod(getContext().getNode().getProbeId(), InformixTester.class.getName(), "doTest",
									getContext().getNode().getIp(), method);
				} catch (Exception e2) {
					ErrorDlg.createErrorDlg(InformixParamPanel.this, "远程探针执行方法失败,ProbeCode:" + getContext().getNode().getProbeId(), e2).setVisible(true);
				  return;
				}
				if (Boolean.valueOf(result)) {
					JOptionPane.showMessageDialog(InformixParamPanel.this, "成功连接到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
				} else {
					Exception exception = new Exception(result);
					ErrorDlg.createErrorDlg(InformixParamPanel.this, "错误信息", "测试数据库链接失败,Host:" + getContext().getNode().getIp(), exception).setVisible(true);
				}
        
      }
    });
  }

  public InformixMonitorMethodOption getOptions() {
    InformixMonitorMethodOption option = new InformixMonitorMethodOption();
    option.setServername(jTextServerName.getText());
    option.setUsername(jTextFieldUsername.getText());
    option.setPassword(new String(jPasswordFieldPassword.getPassword()));
    option.setPort(snm_port.getNumber().intValue());    
    return option;
  }

  public void setOptions(InformixMonitorMethodOption option) {
    jTextServerName.setText(option.getServername());
    jTextFieldUsername.setText(option.getUsername());
    jPasswordFieldPassword.setText(option.getPassword());
    snm_port.setValue(new Integer(option.getPort()));
  }

  public boolean verify() {
    return FieldValidator.textRequired(this, jTextServerName, "服务名")
        && FieldValidator.textRequired(this, jTextFieldUsername, "用户名")
        && FieldValidator.textRequired(this, jPasswordFieldPassword, "密码");
  }
  
	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		InformixMonitorMethodOption method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new InformixMonitorMethodOption(method));		
	}  
}
