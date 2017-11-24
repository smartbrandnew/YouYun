package com.broada.carrier.monitor.method.db2;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;
import com.broada.utils.StringUtil;

/**
 * DB2 database connection config panel
 * 
 * @author lixy (lixy@broada.com.cn)
 * Create By 2007-4-3 上午10:23:04
 */
public class DB2ConfPanel extends JPanel {
  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -7945628636220063742L;

  public static final String NORMAL = "Normal";
  
  public static final String AS400 = "As400";
  
  public static final String DRIVER_NORMAL = "com.ibm.db2.jcc.DB2Driver";
  
  public static final String DRIVER_AS400 = "com.ibm.as400.access.AS400JDBCDriver";

  public static final String URL_NORMAL = "jdbc:db2://{0}:{1}/{2}";
  
  public static final String URL_AS400 = "jdbc:as400://{0}:{1}/{2}";
  
  private String host = "";

  JPanel jPanelCommon = new JPanel();
  
  JLabel jLabelOption = new JLabel();
  JComboBox jComboBoxOption = new JComboBox();
  
  //jdbc
  JLabel jLabel5 = new JLabel();
  JComboBox jComBoxDriverType = new JComboBox();
  SpinnerNumberModel snm_port = new SpinnerNumberModel(50000, 1, 65535, 1);
  JLabel jLabel4 = new JLabel();
  JSpinner jSpinnerPort = new JSpinner(snm_port);
  JLabel jLabel1 = new JLabel();
  JTextField jTextFieldSid = new JTextField();
  JLabel jLabel2 = new JLabel();
  JTextField jTextFieldUsername = new JTextField();
  JLabel jLabel3 = new JLabel();
  JPasswordField jPasswordFieldPassword = new JPasswordField();
  JLabel jLabel6 = new JLabel();
  JTextField jTextFieldDbversion = new JTextField();
  
  JButton jButtonTest = new JButton(" 测 试 ");
  
  //cli
  DB2CliConfPanel cliPanel = null;
  
  JLabel jLabelCli1 = new JLabel();
  JTextField jTextFieldCliSid = new JTextField();
  JLabel jLabelCli2 = new JLabel();
  JTextField jTextFieldCliUsername = new JTextField();
  JLabel jLabelCli6 = new JLabel();
  JTextField jTextFieldCliDbversion = new JTextField();
  
  JButton jButtonTestCli = new JButton(" 测试数据库 ");
  
  private MonitorMethodConfigContext context;

	public void setContext(MonitorMethodConfigContext context) {
		this.context = context;		
		this.cliPanel.setContext(context);
	}
  
  public DB2ConfPanel() {
    try {
      jbInit();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);
    jLabelOption.setText("采集方式");
    jLabelOption.setBounds(new Rectangle(49, 20, 55, 20));
    jComboBoxOption.setBounds(new Rectangle(155, 20, 104, 25));
    jComboBoxOption.addItem(DB2MonitorMethodOption.JDBC4DB2MONITORMETHOD);
    jComboBoxOption.addItem(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD);
    
    //JDBC方式
    jLabel5.setText("驱动");
    jLabel5.setBounds(new Rectangle(43, 10, 42, 15));
    jComBoxDriverType.setBounds(new Rectangle(149, 10, 115, 20));
    jComBoxDriverType.setEnabled(true);
    addItem(jComBoxDriverType);
    jComBoxDriverType.setSelectedIndex(0);
    
    jLabel4.setText("端口");
    jLabel4.setBounds(new Rectangle(43, 40, 42, 15));
    jSpinnerPort.setBounds(new Rectangle(149, 40, 114, 20));
    
    jLabel1.setText("数据库名");
    jLabel1.setBounds(new Rectangle(43, 70, 51, 15));
    jTextFieldSid.setBounds(new Rectangle(149, 70, 115, 20));

    jLabel2.setText("用户名");
    jLabel2.setBounds(new Rectangle(43, 100, 42, 15));
    jTextFieldUsername.setBounds(new Rectangle(149, 100, 115, 20));

    jLabel3.setText("密码");
    jLabel3.setBounds(new Rectangle(43, 130, 42, 15));
    jPasswordFieldPassword.setBounds(new Rectangle(149, 130, 115, 20));
    
    jLabel6.setText("版本号");
    jLabel6.setBounds(new Rectangle(43, 160, 42, 15));
    jTextFieldDbversion.setBounds(new Rectangle(149, 160, 115, 20));
    
    jButtonTest.setBounds(new Rectangle(43, 190, 80, 23));
    
    //CLI方式
    jLabelCli1.setText("数据库名");
    jLabelCli1.setBounds(new Rectangle(43, 235, 51, 15));
    jTextFieldCliSid.setBounds(new Rectangle(110, 235, 100, 20));
    
    jLabelCli2.setText("用户名");
    jLabelCli2.setBounds(new Rectangle(235, 235, 42, 24));
    jTextFieldCliUsername.setBounds(new Rectangle(280, 235, 80, 24));
    
    jLabelCli6.setText("版本号");
    jLabelCli6.setBounds(new Rectangle(43, 265, 51, 15));
    jTextFieldCliDbversion.setBounds(new Rectangle(110, 265, 100, 20));
    
    jButtonTestCli.setBounds(new Rectangle(185, 295, 100, 23));
    
    jPanelCommon.setLayout(null);
    jPanelCommon.setBounds(new Rectangle(6, 60, 450, 230));
    jPanelCommon.setBorder( new TitledBorder(""));
    jPanelCommon.add(jLabel5);
    jPanelCommon.add(jComBoxDriverType);
    jPanelCommon.add(jLabel4);
    jPanelCommon.add(jSpinnerPort);
    jPanelCommon.add(jLabel1);
    jPanelCommon.add(jTextFieldSid);
    jPanelCommon.add(jLabel2);
    jPanelCommon.add(jTextFieldUsername);
    jPanelCommon.add(jLabel3);
    jPanelCommon.add(jPasswordFieldPassword);
    jPanelCommon.add(jLabel6);
    jPanelCommon.add(jTextFieldDbversion);
    
    jPanelCommon.add(jButtonTest);
    
    jLabelCli1.setVisible(false);
    jTextFieldCliSid.setVisible(false);
    jLabelCli2.setVisible(false);
    jTextFieldCliUsername.setVisible(false);
    jLabelCli6.setVisible(false);
    jTextFieldCliDbversion.setVisible(false);
    jButtonTestCli.setVisible(false);
    this.add(jLabelCli1);
    this.add(jTextFieldCliSid);
    this.add(jLabelCli2);
    this.add(jTextFieldCliUsername);
    this.add(jLabelCli6);
    this.add(jTextFieldCliDbversion);
    this.add(jButtonTestCli);
    this.add(jLabelOption);
    this.add(jComboBoxOption);
    this.add(jPanelCommon);
    cliPanel = new DB2CliConfPanel();
    cliPanel.setLayout(null);
    cliPanel.setBounds(new Rectangle(6, 46, 449, 170));
    cliPanel.setVisible(false);
    this.add(cliPanel);

    jComboBoxOption.addActionListener(new ActionListener(){
      
      public void actionPerformed(ActionEvent e) {
        String optType = (String) jComboBoxOption.getSelectedItem();
        if(optType.equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)){
          jPanelCommon.setVisible(false);
          cliPanel.setVisible(true);
          jLabelCli1.setVisible(true);
          jTextFieldCliSid.setVisible(true);
          jLabelCli2.setVisible(true);
          jTextFieldCliUsername.setVisible(true);
          jLabelCli6.setVisible(true);
          jTextFieldCliDbversion.setVisible(true);
          jButtonTestCli.setVisible(true);
        }else{
          jPanelCommon.setVisible(true);
          cliPanel.setVisible(false);
          jLabelCli1.setVisible(false);
          jTextFieldCliSid.setVisible(false);
          jLabelCli2.setVisible(false);
          jTextFieldCliUsername.setVisible(false);
          jLabelCli6.setVisible(false);
          jTextFieldCliDbversion.setVisible(false);
          jButtonTestCli.setVisible(false);
        }
      }
      
    });
    
    jButtonTest.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!verify())
          return;

	      String result = ""; 			
	 			try {
	 				result = DB2Tester.doTest(context.getServerFactory().getProbeService(), context.getNode().getProbeId(), 
	 						(String)jComBoxDriverType.getSelectedItem(), host, snm_port.getValue().toString(), jTextFieldSid
	            .getText(),jTextFieldUsername.getText(), new String(jPasswordFieldPassword.getPassword())); 			
	 			} catch (Exception e2) {
	 				ErrorDlg.createErrorDlg(DB2ConfPanel.this, "远程探针执行方法失败,ProbeCode:" + context.getNode().getProbeId(), e2).setVisible(true);
	 			  return;
	 			}
	 			String [] arrRes = result.split(",");
	 			if(Boolean.valueOf(arrRes[0])){
	 				jTextFieldDbversion.setText(arrRes[1]);
	 				JOptionPane.showMessageDialog(DB2ConfPanel.this, "成功连接到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
	 			}else {
	 				Exception exception = new Exception(arrRes[1]);
	 				ErrorDlg.createErrorDlg(DB2ConfPanel.this, "错误信息","测试数据库链接失败,Host:" + host, exception).setVisible(true);
	 			}
      }
        
    });
    
    jButtonTestCli.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!verify())
          return;

	      String result = "";
	 			try {
	 				result = DB2Tester.doTestCli(context.getServerFactory().getProbeService(), context.getNode().getProbeId(), 
	 						context.getNode().getIp(), (String)jComboBoxOption.getSelectedItem(), 
	 						cliPanel.getOptions().getSysname(), cliPanel.getOptions().getSessionName(), cliPanel.getOptions().getRemotePort(), cliPanel.getOptions().getSysversion(), 
	 						jTextFieldCliSid.getText(), jTextFieldCliUsername.getText());
	 			} catch (Exception e2) {
	 				ErrorDlg.createErrorDlg(DB2ConfPanel.this, "远程探针执行方法失败,ProbeCode:" + context.getNode().getProbeId(), e2).setVisible(true);
	 			  return;
	 			}
	 			String [] arrRes = result.split(",");
	 			if(Boolean.valueOf(arrRes[0])){
	 				jTextFieldCliDbversion.setText(arrRes[1]);
	 				JOptionPane.showMessageDialog(DB2ConfPanel.this, "成功连接到数据库", "成功", JOptionPane.INFORMATION_MESSAGE);
	 			}else {
	 				Exception exception = new Exception(arrRes[1]);
	 				ErrorDlg.createErrorDlg(DB2ConfPanel.this, "错误信息","测试数据库失败:" + result, exception).setVisible(true);
	 			}
      }
        
    });
  }

  private void addItem(JComboBox comBox) {
    comBox.addItem(NORMAL);
    comBox.addItem(AS400);
  }
  
  public DB2MonitorMethodOption getOptions() {

    DB2MonitorMethodOption option = new DB2MonitorMethodOption();
    String optType = (String) jComboBoxOption.getSelectedItem();
    option.setOptType(optType);
    if (optType.equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      option.updateCliOption(cliPanel.getOptions());
      option.setDb(jTextFieldCliSid.getText());
      option.setUsername(jTextFieldCliUsername.getText());
      option.setDbversion(jTextFieldCliDbversion.getText());
    } else {
      option.setDb(jTextFieldSid.getText());
      option.setUsername(jTextFieldUsername.getText());
      option.setDbversion(jTextFieldDbversion.getText());
      option.setPassword(new String(jPasswordFieldPassword.getPassword()));
      option.setPort(snm_port.getNumber().intValue());
      option.setDriverType(StringUtil.isNullOrBlank((String) jComBoxDriverType.getSelectedItem()) ? NORMAL
          : (String) jComBoxDriverType.getSelectedItem());
    }
    return option;
  }

  public void setOptions(DB2MonitorMethodOption option) {
    jComboBoxOption.setSelectedItem(option.getOptType());
    if (option.getOptType().equalsIgnoreCase(DB2MonitorMethodOption.CLI4DB2MONITORMETHOD)) {
      cliPanel.setOptions(option.gotCliOption());
      jTextFieldCliSid.setText(option.getDb());
      jTextFieldCliUsername.setText(option.getUsername());
      jTextFieldCliDbversion.setText(option.getDbversion());
    } else {
      jTextFieldSid.setText(option.getDb());
      jTextFieldUsername.setText(option.getUsername());
      jPasswordFieldPassword.setText(option.getPassword());
      jTextFieldDbversion.setText(option.getDbversion());
      snm_port.setValue(new Integer(option.getPort()));
      jComBoxDriverType.setSelectedItem(StringUtil.isNullOrBlank(option.getDriverType()) ? NORMAL : option
          .getDriverType());
    }
  }

  public boolean verify() {
    if (jComboBoxOption.getSelectedItem().toString().equalsIgnoreCase(DB2MonitorMethodOption.JDBC4DB2MONITORMETHOD)) {
      return FieldValidator.textRequired(this, jTextFieldSid, "数据库实例名")
          && FieldValidator.textRequired(this, jTextFieldUsername, "用户名")
          && FieldValidator.textRequired(this, jPasswordFieldPassword, "密码");
    } else {
      return FieldValidator.textRequired(this, jTextFieldCliSid, "数据库实例名")
          && FieldValidator.textRequired(this, jTextFieldCliUsername, "用户名");
    }
  }

  public void setHost(String host) {
    this.host = host;
    cliPanel.setAgentOptionOnly(host);
  }
  
}
