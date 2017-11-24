package com.broada.carrier.monitor.method.postgresql;

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

public class PostgreSQLConfPanel extends BaseMethodConfiger {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -2039152720784258305L;
  private SpinnerNumberModel snm_port = new SpinnerNumberModel(5432, 1, 65535, 1);  
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JTextField jTextFieldDatabaseName = new JTextField("postgres");
  JTextField jTextFieldUsername = new JTextField("postgres");
  JLabel jLabel3 = new JLabel();
  JPasswordField jPasswordFieldPassword = new JPasswordField();
  JLabel jLabel4 = new JLabel();
  JButton jButtonTest = new JButton(" 测 试 ");
  JSpinner jSpinnerPort = new JSpinner(snm_port);
  
  public PostgreSQLConfPanel() {
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
    
    jLabel1.setText("数据库实例");
    jLabel1.setBounds(new Rectangle(43, 100, 84, 22));
    jTextFieldDatabaseName.setBounds(new Rectangle(149, 100, 115, 22));
    jButtonTest.setBounds(new Rectangle(43, 130, 80, 24));
    this.add(jTextFieldUsername);
    this.add(jLabel2);
    this.add(jLabel3);
    this.add(jPasswordFieldPassword);
    this.add(jTextFieldDatabaseName);
    this.add(jSpinnerPort);
    this.add(jLabel4);
    this.add(jLabel1);
    this.add(jButtonTest);

    jButtonTest.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
      	String result = null;
 			try {
 				if (!verify())
 					return;
 				
 				PostgreSQLMonitorMethodOption method = getOptions();
				if (method == null)
					return;			 
				result = (String) getContext().getServerFactory().getProbeService().executeMethod(getContext().getNode().getProbeId(), PostgreSQLTester.class.getName(), "doTest",
						getContext().getNode().getIp(), (Integer)method.getPort(), method.getDb(), method.getUsername(), method.getPassword());
 			} catch (Exception e2) {
 				ErrorDlg.createErrorDlg(PostgreSQLConfPanel.this, "远程探针执行方法失败,ProbeCode:" + getContext().getNode().getProbeId(), e2).setVisible(true);
 			  return;
 			}
 			if(Boolean.valueOf(result)){
 				JOptionPane.showMessageDialog(PostgreSQLConfPanel.this, "成功连接到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
 			}else {
 				Exception exception = new Exception(result);
 				ErrorDlg.createErrorDlg(PostgreSQLConfPanel.this, "错误信息","测试数据库链接失败,Host:" + getContext().getNode().getIp(), exception).setVisible(true);
 			}        
      }
    });
  }

  public PostgreSQLMonitorMethodOption getOptions() {
    PostgreSQLMonitorMethodOption option = new PostgreSQLMonitorMethodOption();
    option.setUsername(jTextFieldUsername.getText());
    option.setPassword(new String(jPasswordFieldPassword.getPassword()));
    option.setPort(snm_port.getNumber().intValue());
    option.setDb(jTextFieldDatabaseName.getText());
    return option;
  }

  public void setOptions(PostgreSQLMonitorMethodOption option) {
    jTextFieldUsername.setText(option.getUsername());
    jPasswordFieldPassword.setText(option.getPassword());
    snm_port.setValue(new Integer(option.getPort()));
    jTextFieldDatabaseName.setText(option.getDb());
  }

  public boolean verify() {
    return FieldValidator.textRequired(this, jTextFieldUsername, "用户名")
    && FieldValidator.textRequired(this, jTextFieldDatabaseName, "数据库名称");
  }

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		PostgreSQLMonitorMethodOption method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {		
		setOptions(new PostgreSQLMonitorMethodOption(method));		
	}  
}
