package com.broada.carrier.monitor.method.mssql;

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

import org.apache.commons.lang.StringUtils;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;
/**
 * SQLServer配置主面板
 * 
 * @author zhoucy(zhoucy@broada.com.cn)
 * Create By May 5, 2008 10:10:02 AM
 */
public class MSSQLConfPanel extends BaseMethodConfiger {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -7945628636220063742L;
  private SpinnerNumberModel snm_port = new SpinnerNumberModel(1433, 1, 65535, 1);

  JLabel jLabel1 = new JLabel();
  JTextField jTextFieldInstanceName = new JTextField("");
  
  JLabel jLabel2 = new JLabel();

  JTextField jTextFieldUsername = new JTextField("sa");

  JLabel jLabel3 = new JLabel();

  JPasswordField jPasswordFieldPassword = new JPasswordField();

  JLabel jLabel4 = new JLabel();
  
  JLabel jLabel5 = new JLabel();
  JTextField jTextFieldDomain = new JTextField();

  JButton jButtonTest = new JButton(" 测 试 ");

  JSpinner jSpinnerPort = new JSpinner(snm_port);
  
  public MSSQLConfPanel() {
    try {
      jbInit();
    } catch (Exception ex) {
      ErrorDlg.createErrorDlg(this, "错误", "初始化出错", ex).setVisible(true);
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);

    jLabel1.setText("实例名");
    jLabel1.setBounds(new Rectangle(43, 10, 42, 22));
    jTextFieldInstanceName.setBounds(new Rectangle(149, 10, 114, 22));

    jLabel2.setText("端口");
    jLabel2.setBounds(new Rectangle(43, 40, 42, 22));
    jSpinnerPort.setBounds(new Rectangle(149, 40, 114, 22));

    jLabel3.setText("用户名");
    jLabel3.setBounds(new Rectangle(43, 70, 42, 22));
    jTextFieldUsername.setBounds(new Rectangle(149, 70, 115, 22));

    jLabel4.setText("密码");
    jLabel4.setBounds(new Rectangle(43, 100, 42, 22));
    jPasswordFieldPassword.setBounds(new Rectangle(149, 100, 115, 22));
    
    jLabel5.setText("域名");
    jLabel5.setBounds(new Rectangle(43, 130, 42, 22));
    jTextFieldDomain.setBounds(new Rectangle(149, 130, 115, 22));

    jButtonTest.setBounds(new Rectangle(43, 160, 80, 24));
    this.add(jTextFieldUsername);
    this.add(jLabel1);
    this.add(jLabel2);
    this.add(jLabel3);
    this.add(jLabel4);
    this.add(jLabel5);
    this.add(jPasswordFieldPassword);
    this.add(jSpinnerPort);
    this.add(jTextFieldDomain);
    this.add(jTextFieldInstanceName);
    this.add(jButtonTest);

    jButtonTest.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
      String result = "";
 			try {
 				if (!verify())
 					return;
 				
 				MSSQLMonitorMethodOption method = getOptions();
				if (method == null)
					return;
 				result = (String) getContext().getServerFactory().getProbeService()
						.executeMethod(getContext().getNode().getProbeId(), MSSQLTester.class.getName(), "doTest",
								getContext().getNode().getIp(), method.getPort(), method.getUsername(), method.getPassword(), method.getDomain(), method.getInstanceName());
 			} catch (Exception e2) {
 				ErrorDlg.createErrorDlg(MSSQLConfPanel.this, "远程探针执行方法失败,ProbeCode:" + getContext().getNode().getProbeId(), e2).setVisible(true);
 			  return;
 			}
 			if(Boolean.valueOf(result)){
 				JOptionPane.showMessageDialog(MSSQLConfPanel.this, "成功连接到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
 			}else {
 				Exception exception = new Exception(result);
 				ErrorDlg.createErrorDlg(MSSQLConfPanel.this, "错误信息","测试数据库链接失败,Host:" + getContext().getNode().getIp(), exception).setVisible(true);
 			}
      }
    });
  }

  public MSSQLMonitorMethodOption getOptions() {
    MSSQLMonitorMethodOption option = new MSSQLMonitorMethodOption();
    option.setUsername(jTextFieldUsername.getText());
    option.setPassword(new String(jPasswordFieldPassword.getPassword()));
    option.setPort(snm_port.getNumber().intValue());
    String value = jTextFieldDomain.getText();
    if (StringUtils.isNotBlank(value))
      option.setDomain(value);
    value = jTextFieldInstanceName.getText();
    if (StringUtils.isNotBlank(value))
      option.setInstanceName(value);
    return option;
  }

  public void setOptions(MSSQLMonitorMethodOption option) {
    jTextFieldUsername.setText(option.getUsername());
    jPasswordFieldPassword.setText(option.getPassword());
    snm_port.setValue(new Integer(option.getPort()));
    String value = option.getDomain();
    if (value != null)
      jTextFieldDomain.setText(value);
    value = option.getInstanceName();
    if (value != null)
      jTextFieldInstanceName.setText(value);
  }

  public boolean verify() {
    return FieldValidator.textRequired(this, jTextFieldUsername, "用户名");
  }

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		MSSQLMonitorMethodOption method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new MSSQLMonitorMethodOption(method));		
	}
  
}
