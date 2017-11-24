package com.broada.carrier.monitor.method.websphere;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.impl.mw.websphere.WASUtil;
import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.StringUtil;

/**
 * 
 * @author lixy (lixy@broada.com.cn) Create By 2008-6-2 下午02:33:41
 */
public class WASParamPanel extends BaseMethodConfiger {

  /**
   * <code>serialVersionUID</code>
   */
  private static final long serialVersionUID = -5655826272545049764L;
  
  public static String USER_DIER = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/";
  public static String CERPATH = System.getProperty("user.dir").replaceAll("\\\\", "/") + "/conf/cer/";
  public static String TRUSTSTORE_NAME = "truststore.jks";
  public static String TRUSTSTORE_PWD = "123456";
  public static String TEMP_NAME = "keystore.jks";
  public static String TEMP_PWD = "123456";

  BorderLayout borderLayout1 = new BorderLayout();

  private SpinnerNumberModel was_port = new SpinnerNumberModel(9080, 1, 65535, 1);
  private SpinnerNumberModel connector_port = new SpinnerNumberModel(8880, 1, 65535, 1);

  JPanel jPanParam = new JPanel();
  JPanel jPanButton = new JPanel();//测试按钮面板
  JLabel jLabPassword = new JLabel();

  JLabel jLabUsername = new JLabel();

  JCheckBox jChkDomain = new JCheckBox();

  JLabel jLabel1 = new JLabel();

  JCheckBox jChkAuth = new JCheckBox();
  
  JSpinner jSpnPort = new JSpinner(was_port);
  JSpinner jSpnConnectorPort = new JSpinner(connector_port);
  JTextField jTxtUsername = new JTextField();
  JPasswordField jPwdPassword = new JPasswordField();
  JTextField jTextDomain = new JTextField();
  JLabel jLabConnectorType = new JLabel();
  JComboBox jComboxConnectorType = new JComboBox();
  JLabel jLabConnectorPort = new JLabel();
  JLabel jLabConnectorHost = new JLabel();
  JTextField jTextConnectorHost = new JTextField();
  
  //SSL认证组件
  JCheckBox jChkSSL = new JCheckBox();
  JLabel jLabSSLServerCer = new JLabel();
  JTextField jTextSSLServerCer = new JTextField();
  JButton jbtnSSLServerCer = new JButton("选择");
  JFileChooser jfcSSLServerCer = new JFileChooser(CERPATH);
  JLabel jLabSSLClientKey = new JLabel();
  JTextField jTextSSLClientKey = new JTextField();
  JButton jbtnSSLClientKey = new JButton("选择");
  JFileChooser jfcSSLClientKey = new JFileChooser(CERPATH);
  JLabel jLabSSLClientKeyPwd = new JLabel();
  JPasswordField jTextSSLClientKeyPwd = new JPasswordField();
  
  JButton jButtonTest = new JButton(" 测 试 ");
  
  private MonitorNode monitorNode = null;
  
  public WASParamPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(borderLayout1);
      jPanParam.setLayout(null);
      jPanParam.setPreferredSize(new Dimension(480, 400));
      //第一行
      jLabel1.setText("端口(Port)");
      jLabel1.setHorizontalAlignment(SwingConstants.LEFT);
      jLabel1.setBounds(new Rectangle(15, 5, 70, 24));//
      
      jSpnPort.setOpaque(false);
      jSpnPort.setBounds(new Rectangle(85, 5, 70, 24));//
      
      jChkDomain.setText("使用域名");
      jChkDomain.setBounds(new Rectangle(160, 5, 80, 24));
      jChkDomain.addItemListener(new java.awt.event.ItemListener() {
          public void itemStateChanged(ItemEvent e) {
          jChkDomain_itemStateChanged(e);
        }
      });
      jTextDomain.setEnabled(false);
      jTextDomain.setEditable(true);
      jTextDomain.setText("");
      jTextDomain.setBounds(new Rectangle(245, 5, 140, 24));
      //第二行
      jChkAuth.setText("启用认证");
      jChkAuth.setBounds(new Rectangle(15, 32, 80, 24));
      jChkAuth.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          jChkAuth_itemStateChanged(e);
        }
      });
      jLabUsername.setText("用户名");
      jLabUsername.setHorizontalAlignment(SwingConstants.LEFT);
      jLabUsername.setBounds(new Rectangle(105, 32, 40, 24));
      jTxtUsername.setText("");
      jTxtUsername.setEnabled(false);
      jTxtUsername.setBounds(new Rectangle(150, 32, 80, 24));
      jLabPassword.setText("密码");
      jLabPassword.setHorizontalAlignment(SwingConstants.LEFT);
      jLabPassword.setBounds(new Rectangle(240, 32, 40, 24));
      jPwdPassword.setText("");
      jPwdPassword.setEnabled(false);
      jPwdPassword.setBounds(new Rectangle(285, 32, 80, 24));
      //第三行
      jLabConnectorType.setText("connector类型");
      jLabConnectorType.setHorizontalAlignment(SwingConstants.LEFT);
      jLabConnectorType.setBounds(new Rectangle(15, 59, 90, 24));
      jComboxConnectorType.setEnabled(false);
      jComboxConnectorType.addItem("SOAP");
      jComboxConnectorType.addItem("RMI");
      jComboxConnectorType.setBounds(new Rectangle(110, 59, 60, 24));
      jLabConnectorPort.setText("connector端口");
      jLabConnectorPort.setHorizontalAlignment(SwingConstants.LEFT);
      jLabConnectorPort.setBounds(new Rectangle(180, 59, 90, 24));
      jSpnConnectorPort.setOpaque(false);
      jSpnConnectorPort.setEnabled(false);
      jSpnConnectorPort.setBounds(new Rectangle(275, 59, 90, 24));
      //第四行
      jLabConnectorHost.setText("主机名");
      jLabConnectorHost.setHorizontalAlignment(SwingConstants.LEFT);
      jLabConnectorHost.setBounds(new Rectangle(15, 86, 40, 24));
      jTextConnectorHost.setText("");
      jTextConnectorHost.setEnabled(false);
      jTextConnectorHost.setBounds(new Rectangle(60, 86, 120, 24));
      //第五行    
      jChkSSL.setText("启用SSL认证");
      jChkSSL.setBounds(new Rectangle(15, 112, 100, 24));
      jChkSSL.addItemListener(new java.awt.event.ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          jChkSSL_itemStateChanged(e);
        }
      });
      //第六行
      jLabSSLServerCer.setText("签署者证书");
      jLabSSLServerCer.setHorizontalAlignment(SwingConstants.LEFT);
      jLabSSLServerCer.setBounds(new Rectangle(15, 139, 80, 24));
      jTextSSLServerCer.setText("");
      jTextSSLServerCer.setEnabled(false);
      jTextSSLServerCer.setBounds(new Rectangle(100, 139, 180, 24));
      jbtnSSLServerCer.setBounds(new Rectangle(285, 139, 70, 24));
      jbtnSSLServerCer.setEnabled(false);
      jbtnSSLServerCer.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          chooseServerCer(e);
          }
        });
      
      //第七行
      jLabSSLClientKey.setText("个人证书");
      jLabSSLClientKey.setHorizontalAlignment(SwingConstants.LEFT);
      jLabSSLClientKey.setBounds(new Rectangle(15, 165, 80, 24));
      jTextSSLClientKey.setText("");
      jTextSSLClientKey.setEnabled(false);
      jTextSSLClientKey.setBounds(new Rectangle(100, 165, 180, 24));
      jbtnSSLClientKey.setBounds(new Rectangle(285, 165, 70, 24));
      jbtnSSLClientKey.setEnabled(false);
      jbtnSSLClientKey.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          chooseClientKey(e);
          }
        });
      //第八行
      jLabSSLClientKeyPwd .setText("个人证书密码");
      jLabSSLClientKeyPwd .setHorizontalAlignment(SwingConstants.LEFT);
      jLabSSLClientKeyPwd .setBounds(new Rectangle(15, 191, 80, 24));
      jTextSSLClientKeyPwd.setText("");
      jTextSSLClientKeyPwd.setEnabled(false);
      jTextSSLClientKeyPwd.setBounds(new Rectangle(100, 191, 80, 24));
      //第九行，测试
      jButtonTest.setBounds(new Rectangle(200, 40, 80, 24));
      jButtonTest.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            doTest();
          }
        });
      jPanButton.setLayout(null);
      jPanButton.setPreferredSize(new Dimension(1, 70));
      this.add(jPanParam, BorderLayout.CENTER);
      this.add(jPanButton,BorderLayout.SOUTH);
      jPanParam.add(jChkDomain, null);
      jPanParam.add(jTextDomain, null);
      jPanParam.add(jLabel1, null);
      jPanParam.add(jSpnPort, null);
      jPanParam.add(jChkAuth, null);
      jPanParam.add(jLabUsername, null);
      jPanParam.add(jLabConnectorType,null);
      jPanParam.add(jComboxConnectorType, null);
      jPanParam.add(jLabConnectorPort,null);
      jPanParam.add(jLabConnectorHost, null);
      jPanParam.add(jTextConnectorHost,null);
      jPanParam.add(jSpnConnectorPort, null);
      jPanParam.add(jTxtUsername, null);
      jPanParam.add(jLabPassword, null);
      jPanParam.add(jPwdPassword, null);
      jPanParam.add(jChkSSL,null);
      jPanParam.add(jLabSSLServerCer,null);
      jPanParam.add(jLabSSLClientKey,null);
      jPanParam.add(jTextSSLServerCer,null);
      jPanParam.add(jTextSSLClientKey,null);
      jPanParam.add(jbtnSSLServerCer,null);
      jPanParam.add(jbtnSSLClientKey,null);
      jPanParam.add(jLabSSLClientKeyPwd,null);
      jPanParam.add(jTextSSLClientKeyPwd,null);
      jPanButton.add(jButtonTest, null);
  }

  void jChkDomain_itemStateChanged(ItemEvent e) {
    jTextDomain.setEnabled(jChkDomain.isSelected());
  }

  void jChkAuth_itemStateChanged(ItemEvent e) {
    jTxtUsername.setEnabled(jChkAuth.isSelected());
    jPwdPassword.setEnabled(jChkAuth.isSelected());
    jComboxConnectorType.setEnabled(jChkAuth.isSelected());
    jSpnConnectorPort.setEnabled(jChkAuth.isSelected());
    jTextConnectorHost.setEnabled(jChkAuth.isSelected());
  }
  
  protected void jChkSSL_itemStateChanged(ItemEvent e) {
    jTextSSLServerCer.setEnabled(jChkSSL.isSelected());
    jbtnSSLServerCer.setEnabled(jChkSSL.isSelected());
    jTextSSLClientKey.setEnabled(jChkSSL.isSelected());
    jbtnSSLClientKey.setEnabled(jChkSSL.isSelected());
    jTextSSLClientKeyPwd.setEnabled(jChkSSL.isSelected());
  }
  
  public void setOptions(WASMonitorMethodOption option) {
    jSpnPort.setValue(new Integer(option.getPort()));
    boolean chkDomain = option.isChkDomain();
    jChkDomain.setSelected(chkDomain);
    if (chkDomain) {
      jTextDomain.setText(option.getDomain());
    }
    boolean chkAuth = option.isChkAuth();
    jChkAuth.setSelected(chkAuth);
    if (chkAuth) {
      jTxtUsername.setText(option.getUsername());
      jPwdPassword.setText(option.getPassword());
    }
    jComboxConnectorType.setSelectedItem(option.getConnectorType());
    jSpnConnectorPort.setValue(new Integer(option.getConnectorPort()));
    jTextConnectorHost.setText(option.getConnectorHost());
    
    boolean chkssl = option.isChkSSL();
    jChkSSL.setSelected(chkssl);
    if (chkssl) {
    	if(option.getServerCerFilePath()==null || option.getServerCerFilePath() == CERPATH || option.getServerCerFilePath().equals(CERPATH)) {
    		jTextSSLServerCer.setText("");
    	} else {
    		jTextSSLServerCer.setText(USER_DIER + option.getServerCerFilePath());
    	}
    	if(option.getClientKeyPath() == null || option.getClientKeyPath() == CERPATH || option.getClientKeyPath().equals(CERPATH)) {
    		jTextSSLClientKey.setText("");
    	} else {
    		jTextSSLClientKey.setText(USER_DIER + option.getClientKeyPath());
    	}
      jTextSSLClientKeyPwd.setText(option.getClientKeyPwd());
    }
  }

  public WASMonitorMethodOption getOptions() {
    WASMonitorMethodOption option = new WASMonitorMethodOption();

    option.setPort(was_port.getNumber().intValue());
    if (jChkDomain.isSelected()) {
      option.setDomain(jTextDomain.getText());
    } else {
    	option.setDomain("");      
    }
    if (jChkAuth.isSelected()) {      
      option.setUsername(jTxtUsername.getText());
      option.setPassword(new String(jPwdPassword.getPassword()));
    } else {
    	option.setUsername("");
      option.setPassword("");      
    }
    option.setConnectorPort(connector_port.getNumber().intValue());
    option.setConnectorType(jComboxConnectorType.getSelectedItem().toString());
    option.setConnectorHost(jTextConnectorHost.getText().trim());
    
    //ssl认证配置
  	String serverCerPath = jTextSSLServerCer.getText().trim().replaceAll("\\\\", "/");
  	String clientKeyPath = jTextSSLClientKey.getText().trim().replaceAll("\\\\", "/");
  	String clientKeyPwd = new String(jTextSSLClientKeyPwd.getPassword());
    if (jChkSSL.isSelected()) {
      //1.导入服务端证书
      /*int idx = new File(System.getProperty("user.dir", ".")).getAbsolutePath().replaceAll("\\\\", "/").length() + 1;
    	String serverCerName = "";
      if (serverCerPath.trim().length() != 0) {
        if (WASUtil.verifyServerCerPath(serverCerPath, true)) {
          try {
          	WASUtil.importCer(serverCerPath, WASParamPanel.TRUSTSTORE_NAME, WASParamPanel.TRUSTSTORE_PWD, true);
          } catch (IOException e) {
          	e.printStackTrace();
          } catch (InterruptedException e) {
          	e.printStackTrace();
          }
          //serverCerName = WASUtil.copyFile(serverCerPath);
          serverCerName = new File(serverCerPath).getAbsolutePath().substring(idx);
        }
      }
      //2.导入客户端证书
      String clientKeyName = "";
      if (WASUtil.verifyClientKeyPath(clientKeyPath, true)) {
      	//clientKeyName = WASUtil.copyFile(clientKeyPath);
        clientKeyName = new File(clientKeyName).getAbsolutePath().substring(idx);
      }*/
      //3.记录到WASMMO
      
      
      try {
        String[] sc = synCert(serverCerPath,clientKeyPath);        
        option.setServerCerPath("conf/cer/" + TRUSTSTORE_NAME);
        option.setServerCerFilePath(sc[0]);
        option.setClientKeyPath(sc[1]);
        option.setClientKeyPwd(clientKeyPwd);
      } catch (Exception e) {
        //ErrorDlg.createErrorDlg(this, "同步证书到探针出错，证书文件必须放在目录：" + CERPATH + "或者其子目录中。", e).setVisible(true);
        throw new RuntimeException(e);
      }      
    }
    else {
      option.setServerCerPath("");
      option.setServerCerFilePath("");
      option.setClientKeyPath("");
      option.setClientKeyPwd("");
    }
    return option;
  }

  public boolean verify() {
    if (new String(was_port.getNumber().toString()).trim().length() == 0) {
      int opt = JOptionPane.showConfirmDialog(this, "端口真的为空吗？", "提问？", JOptionPane.YES_NO_OPTION);
      if (opt != JOptionPane.YES_OPTION) {
        jSpnPort.requestFocus();
        return false;
      }
    }
    return true;
  }

  private void doTest() {
    if (!this.verify()) {
      return;
    }
    try {
    	WASMonitorMethodOption method = getOptions();
    	if (method == null)
    		return;
			Object ret = getContext().getServerFactory().getProbeService().executeMethod(getContext().getNode().getProbeId(), WASTest4Probe.class.getName(), "test",
					getContext().getNode().getIp(), method);
      if (null == ret) {
        JOptionPane.showMessageDialog(this, "成功连接到Websphere服务器", "成功", JOptionPane.INFORMATION_MESSAGE);
        return;
      } else {
        throw new Exception(ret.toString());
      }
    } catch (Throwable t) {
      ErrorDlg.createErrorDlg(this, "测试失败", t).setVisible(true);
    }

  }

  private String[] synCert(String _serverCer, String _clinetKey) throws Exception {
    String scerDir = CERPATH;
    File cerDir = new File(scerDir);
    if (!cerDir.exists() || !cerDir.isDirectory()) {
      cerDir.mkdirs();
    }

    File scer = new File(_serverCer);
    File ccer = new File(_clinetKey);
    if(!StringUtil.isNullOrBlank(_serverCer)&&(!scer.exists() || !scer.isFile() || !scer.canRead())){
      throw new RuntimeException("证书文件[" + scer.getAbsolutePath() + "不存在或者无读取权限。");
    }
    if (scer.exists() && !isFileInDirectory(scer, cerDir)) {
      /*JOptionPane.showMessageDialog(this, "证书文件必须放在目录：" + cerDir + "或者其子目录中。", "信息", JOptionPane.INFORMATION_MESSAGE);
      return null;*/
      throw new RuntimeException("同步证书到探针出错，证书文件必须放在目录：" + CERPATH + "或者其子目录中。");
    }

    if(!StringUtil.isNullOrBlank(_clinetKey)&&(!ccer.exists() || !ccer.isFile() || !ccer.canRead())){
      throw new RuntimeException("证书文件[" + ccer.getAbsolutePath() + "不存在或者无读取权限。");
    }
    if (ccer.exists() && !isFileInDirectory(ccer, cerDir)) {
      /*JOptionPane.showMessageDialog(this, "证书文件必须放在目录：" + cerDir + "或者其子目录中。", "信息", JOptionPane.INFORMATION_MESSAGE);
      return null;*/
      throw new RuntimeException("同步证书到探针出错，证书文件必须放在目录：" + CERPATH + "或者其子目录中。");
    }

    try {
      String[] sccer = new String[2];
      //同步证书到probe
      int idx = new File(System.getProperty("user.dir", ".")).getAbsolutePath().replaceAll("\\\\", "/").length() + 1;
      if (scer.exists() && scer.isFile()) {
        sccer[0] = scer.getAbsolutePath().replaceAll("\\\\", "/").substring(idx);
        //server导入服务器证书到trustedstore,在同步配置文件的时候，该trutstore中将包含该证书
        WASUtil.importServerCer2Trusted(sccer[0]);
        getContext().getServerFactory().getProbeService().uploadFile(getContext().getNode().getProbeId(), sccer[0], "conf/cer/");
        getContext().getServerFactory().getProbeService().executeMethod(getContext().getNode().getProbeId(), WASUtil.class.getName(), "importServerCer2Trusted", sccer[0]);
			}
			if (ccer.exists() && ccer.isFile()) {
				sccer[1] = ccer.getCanonicalPath().replaceAll("\\\\", "/").substring(idx);
        getContext().getServerFactory().getProbeService().uploadFile(getContext().getNode().getProbeId(), sccer[1], "conf/cer/");
			}
      return sccer;
    } catch (Exception e) {
      //ErrorDlg.createErrorDlg(this, "同步证书到探针失败.", e).setVisible(true);
      throw e;
    }
  }
  
  private void chooseServerCer(ActionEvent e) {
    int returnVal = jfcSSLServerCer.showOpenDialog(new Frame());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      jTextSSLServerCer.setText(jfcSSLServerCer.getSelectedFile().getAbsolutePath());
      //String p = "conf/cer/" + jfcSSLServerCer.getSelectedFile().getName();
      //jTextSSLServerCer.setText(p);
    }
  }
  
  private void chooseClientKey(ActionEvent e) {
    int returnVal = jfcSSLClientKey.showOpenDialog(new Frame());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      jTextSSLClientKey.setText(jfcSSLClientKey.getSelectedFile().getAbsolutePath());
      //String p = "conf/cer/" + jfcSSLClientKey.getSelectedFile().getName();
      //jTextSSLClientKey.setText(p);
    }
  }

  public MonitorNode getMonitorNode() {
    return monitorNode;
  }

  public void setMonitorNode(MonitorNode monitorNode) {
    this.monitorNode = monitorNode;
  }
  
/*  private boolean checkCerPath(String scer){
    String scerDir = System.getProperty("user.dir", ".") + "/conf/cer";
    File cerDir = new File(scerDir);
    if(!cerDir.exists() || !cerDir.isDirectory()){
      cerDir.mkdirs();
    }
    File cer = new File(scer);
    return cerDir.getAbsolutePath().equals(cer.getParent());
  }*/
  
  private static boolean isFileInDirectory(File f1,File d) throws IOException{
    boolean ret = false;
    if(!d.exists() || !d.isDirectory()){
      return ret;
    }
    File f = f1;
    while(f.getParentFile()!=null && !d.getCanonicalPath().equals(f.getParentFile().getCanonicalPath())){
      f = f.getParentFile();
    }
    if(null != f && null != f.getParent()){
      ret = true;
    }
    return ret;
  }
 
	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		WASMonitorMethodOption method = getOptions();		
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		WASMonitorMethodOption option = new WASMonitorMethodOption(method);
		setOptions(option);
	}    
}