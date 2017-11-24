package com.broada.carrier.monitor.method.resin;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;
import com.broada.utils.FieldValidator;

/**
 * Resin配置面板
 * @author 杨帆
 * 
 */
public class ResinJMXPanel extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;
  JLabel jLabel2 = new JLabel();

  JLabel jLabel3 = new JLabel();

  JLabel jLabel4 = new JLabel();

  JTextField jTextFieldAgentName = new JTextField();

  JTextField jTextFieldWebAppRoot = new JTextField();

  private final SpinnerNumberModel snm_port = new SpinnerNumberModel(8080, 1, 65535, 1);

  JSpinner jSpinner1 = new JSpinner(snm_port);

  JButton jButtonIsTestWebApp = new JButton(" 是否需要验证Web应用 ");

  JButton jButtonTest = new JButton(" 测 试 ");
  
  public ResinJMXPanel() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);
    jLabel2.setText("端口");
    jLabel2.setBounds(new Rectangle(61, 54, 42, 15));
    jSpinner1.setBounds(new Rectangle(177, 54, 80, 20));
    jLabel3.setText("代理名称");
    jLabel3.setBounds(new Rectangle(61, 95, 61, 15));
    jTextFieldAgentName.setBounds(new Rectangle(177, 90, 212, 23));
    jTextFieldAgentName.setText("resinAgent");
    jButtonIsTestWebApp.setBounds(new Rectangle(61, 125, 180, 24));
    jLabel4.setText("Web应用根路径");
    jLabel4.setBounds(new Rectangle(61, 170, 100, 15));
    jLabel4.setVisible(false);
    jTextFieldWebAppRoot.setBounds(new Rectangle(177, 165, 212, 23));
    jTextFieldWebAppRoot.setText("localhost:" + snm_port.getNumber());
    jTextFieldWebAppRoot.setVisible(false);
    jButtonTest.setBounds(new Rectangle(177, 205, 80, 24));
    this.add(jButtonIsTestWebApp);
    this.add(jLabel2);
    this.add(jSpinner1);
    this.add(jLabel3);
    this.add(jLabel4);
    this.add(jTextFieldAgentName);
    this.add(jTextFieldWebAppRoot);

    this.add(jButtonTest);

    jButtonIsTestWebApp.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // 单击给出条件是否要测试WEB应用参数
        if (jTextFieldWebAppRoot.isVisible()) {
          jLabel4.setVisible(false);
          jTextFieldWebAppRoot.setVisible(false);
        } else {
          jLabel4.setVisible(true);
          jTextFieldWebAppRoot.setVisible(true);
        }
      }
    });

    jButtonTest.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // 测试参数配置的有效性
        doTest();
      }
    });

  }

  public void setResinJMXOption(ResinJMXOption resinJMXOption) {
    snm_port.setValue(new Integer(resinJMXOption.getPort()));
    jTextFieldAgentName.setText(resinJMXOption.getAgentName());
    jTextFieldWebAppRoot.setText(resinJMXOption.getWebAppRoot());
  }

  public ResinJMXOption getResinJMXOption() {
    ResinJMXOption resinJMXOption = new ResinJMXOption();
    resinJMXOption.setAgentName(jTextFieldAgentName.getText());
    resinJMXOption.setWebAppRoot(jTextFieldWebAppRoot.getText());
    resinJMXOption.setPort(snm_port.getNumber().intValue());
    return resinJMXOption;
  }

  public boolean verify() {
		return (FieldValidator.textRequired(this,
				jTextFieldAgentName, "代理名称"));
  }

  private void doTest() {
    if (!verify()) {
      return;
    }
    
    try {
    	ResinJMXOption method = getResinJMXOption();
    	if (method == null)
    		return;
    	
    	Object ret = (String) getContext().getServerFactory().getProbeService()
					.executeMethod(getContext().getNode().getProbeId(), ResinJMXTest4Probe.class.getName(), "test",
							getContext().getNode().getIp(), method);
      if(null==ret){
        JOptionPane.showMessageDialog(ResinJMXPanel.this, "测试Resin连接成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        return;
      }else{
        throw new Exception(ret.toString());
      }
    } catch (Throwable t) {
      ErrorDlg.createErrorDlg(ResinJMXPanel.this, "测试Resin连接失败,Host:" + getContext().getNode().getIp(), t).setVisible(true);
      return;
    }
  }

	@Override
	public boolean getData() {
		if (!verify())
			return false;
		
		ResinJMXOption method = getResinJMXOption();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setResinJMXOption(new ResinJMXOption(method));		
	}
}
