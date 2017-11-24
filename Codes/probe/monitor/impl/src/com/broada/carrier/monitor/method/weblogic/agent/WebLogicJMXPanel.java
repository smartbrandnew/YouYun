package com.broada.carrier.monitor.method.weblogic.agent;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;
import com.broada.utils.StringUtil;
import com.broada.utils.TextUtil;

/**
 * Modified by hesy at 2008-2-25 上午11:08:48 NUM-469（参数配置面板统一加入测试按钮）
 */
public class WebLogicJMXPanel extends BaseMethodConfiger {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  private MonitorNode monitorNode = null;
  public WebLogicJMXPanel() {
      try {
          jbInit();
      } catch (Exception ex) {
          ex.printStackTrace();
      }
  }

  private void jbInit() throws Exception {
      //NUM-469 适当调整各组件间的间隔距离，以便加入测试按钮
      this.setLayout(null);
      jLabel1.setText("代理IP或域名");
      jLabel1.setBounds(new Rectangle(61, 21, 96, 15));
      jTextFieldHost.setBounds(new Rectangle(177, 18, 212, 22));
      jLabel2.setText("控制台端口");
      jLabel2.setBounds(new Rectangle(61, 53, 96, 15));
      jSpinner1.setBounds(new Rectangle(177, 50, 80, 20));
      jLabel3.setText("代理名称");
      jLabel3.setBounds(new Rectangle(61, 113, 61, 15));
      jTextFieldAgentName.setBounds(new Rectangle(177, 110, 212, 23));
      jLabel4.setForeground(Color.blue);
      jLabel4.setText("如果安装了代理请填写代理名称");
      jLabel4.setBounds(new Rectangle(61, 83, 319, 15));
      jCheckBoxCluster.setText("代理端口(集群)");
      jCheckBoxCluster.setSelected(false);
      jCheckBoxCluster.setBounds(new Rectangle(61, 143, 105, 15));
      jCheckBoxCluster.addItemListener(new ItemListener(){
        public void itemStateChanged(ItemEvent e) {
          jSpinner2.setEnabled(jCheckBoxCluster.isSelected());
        }
      });
      
      jSpinner2.setBounds(new Rectangle(177, 140, 80, 20));
      jSpinner2.setEnabled(false);
      jLabel5.setText("用户名");
      jLabel5.setBounds(new Rectangle(61, 173, 42, 15));
      jTextFieldUsername.setBounds(new Rectangle(177, 170, 212, 22));
      jLabel6.setText("密码");
      jLabel6.setBounds(new Rectangle(61, 203, 42, 15));
      jTextFieldPassword.setBounds(new Rectangle(177, 200, 212, 22));
      jTextFieldAgentName.setText("WebLogicMonitor");
      jButtonTest.setBounds(new Rectangle(177, 226, 80, 24)); //NUM-469
      
      
      this.add(jTextFieldHost);
      this.add(jLabel1);
      this.add(jLabel2);
      this.add(jSpinner1);
      this.add(jTextFieldAgentName);
      this.add(jLabel3);
      this.add(jLabel4);
      this.add(jLabel5);
      this.add(jTextFieldUsername);
      this.add(jLabel6);
      this.add(jTextFieldPassword);
      this.add(jButtonTest); //NUM-469
      this.add(jLabel7);
      this.add(jSpinner2);
      this.add(jCheckBoxCluster);
      
      // NUM-469
      jButtonTest.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          //测试参数配置的有效性
          doTest();
        }
      });
      // end
  }

  JLabel jLabel1 = new JLabel();
  JTextField jTextFieldHost = new JTextField();
  JLabel jLabel2 = new JLabel();
  
  JLabel jLabel3 = new JLabel();
  JTextField jTextFieldAgentName = new JTextField();
  JLabel jLabel4 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JTextField jTextFieldUsername = new JTextField();
  JLabel jLabel6 = new JLabel();
  JPasswordField jTextFieldPassword = new JPasswordField();
  private final SpinnerNumberModel snm_port = new SpinnerNumberModel(7001, 1, 65535, 1);
  JSpinner jSpinner1 = new JSpinner(snm_port);
  JButton jButtonTest = new JButton(" 测 试 "); //NUM-469
  JLabel jLabel7 = new JLabel();
  SpinnerNumberModel snm_proxyPort = new SpinnerNumberModel(8001, 1, 65535, 1);//集群代理
  JSpinner jSpinner2 = new JSpinner(snm_proxyPort);
  JCheckBox jCheckBoxCluster = new JCheckBox();
  
  public void setWebLogicJMXOption(WebLogicJMXOption webLogicJMXOption) {
    if(!StringUtil.isNullOrBlank(webLogicJMXOption.getHost()))
      jTextFieldHost.setText(webLogicJMXOption.getHost());
    snm_port.setValue(new Integer(webLogicJMXOption.getPort()));
    jTextFieldAgentName.setText(webLogicJMXOption.getAgentName());
    jTextFieldUsername.setText(webLogicJMXOption.getUsername());
    jTextFieldPassword.setText(webLogicJMXOption.getPassword());
    if (webLogicJMXOption.isIfCluster()) {
      snm_proxyPort.setValue(new Integer(webLogicJMXOption.getProxyPort()));
      jCheckBoxCluster.setSelected(true);
    }
  }

  public WebLogicJMXOption getWebLogicJMXOption() {
    WebLogicJMXOption webLogicJMXOption = new WebLogicJMXOption();
    webLogicJMXOption.setHost(jTextFieldHost.getText());
    webLogicJMXOption.setAgentName(jTextFieldAgentName.getText());
    webLogicJMXOption.setPassword(new String(jTextFieldPassword.getPassword()));
    webLogicJMXOption.setPort(snm_port.getNumber().intValue());
    webLogicJMXOption.setUsername(jTextFieldUsername.getText());
    webLogicJMXOption.setProxyPort(snm_proxyPort.getNumber().intValue());
    webLogicJMXOption.setIfCluster(jCheckBoxCluster.isSelected());
    return webLogicJMXOption;
  }

  public boolean verify() {
    if(StringUtil.isNullOrBlank(jTextFieldUsername.getText()) || StringUtil.isNullOrBlank(new String(jTextFieldPassword.getPassword()))){
      if(JOptionPane.showConfirmDialog(this, "WebLogic登录用户名或者密码没有填写会导致有些参数不能正常获取,\n您确实不打算填写用户名和密码吗?", "警告", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
        return false;
      }
    }
    return FieldValidator.textRequired(this, jTextFieldHost, "代理IP或域名");
  }
  
  //NUM-469
  private void doTest() {
    if (!verify()) {
      return;
    }
    
    String _host = jTextFieldHost.getText().trim();
    
    try {
    	WebLogicJMXOption method = getWebLogicJMXOption();
    	if (method == null)
    		return;
			Object ret = getContext().getServerFactory().getProbeService().executeMethod(getContext().getNode().getProbeId(), WebLogicJMXTest4Probe.class.getName(), "test",
					method.getHost(), method.getPort(), method.getAgentName(), method.getUsername(), 
					method.getPassword(), method.getProxyPort(), method.isIfCluster());
      if(null==ret){
        JOptionPane.showMessageDialog(WebLogicJMXPanel.this, "测试WebLogic连接成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        return;
      }else{
        throw new Exception(ret.toString());
      }
    } catch (Throwable t) {
      ErrorDlg.createErrorDlg(WebLogicJMXPanel.this, "测试WebLogic连接失败,Host:" + _host, t).setVisible(true);
      return;
    }
    
  }
  
  public MonitorNode getMonitorNode() {
    return monitorNode;
  }

  public void setMonitorNode(MonitorNode monitorNode) {
    this.monitorNode = monitorNode;
  }

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		WebLogicJMXOption method = getWebLogicJMXOption();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		WebLogicJMXOption option = new WebLogicJMXOption(method);
		if (TextUtil.isEmpty(option.getHost()))
				option.setHost(getContext().getNode().getIp());
		setWebLogicJMXOption(option);		
	}  
}

