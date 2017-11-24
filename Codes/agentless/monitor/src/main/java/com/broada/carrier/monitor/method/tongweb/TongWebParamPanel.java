package com.broada.carrier.monitor.method.tongweb;

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

public class TongWebParamPanel extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;

	private SpinnerNumberModel snm_port = new SpinnerNumberModel(1099, 1, 65535, 1);

  String host = "";

  JLabel jLabel2 = new JLabel();

  JTextField jTextFieldName = new JTextField();

  JLabel jLabel3 = new JLabel();

  JButton jButtonTest = new JButton(" 测 试 ");

  JSpinner jSpinnerPort = new JSpinner(snm_port);
  
  JLabel jLabel4 = new JLabel();
  
  public TongWebParamPanel() {
    try {
      jbInit();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setLayout(null);

    jLabel2.setText("JNDI名字");
    jLabel2.setBounds(new Rectangle(43, 91, 115, 20));
    jTextFieldName.setBounds(new Rectangle(149, 91, 150, 20));
    jTextFieldName.setText("RMIConnector_teas");

    jLabel4.setText("JNDI端口");
    jLabel4.setBounds(new Rectangle(43, 49, 115, 20));
    jSpinnerPort.setBounds(new Rectangle(149, 49, 150, 20));

    jButtonTest.setBounds(new Rectangle(43, 196, 80, 24));
    this.add(jTextFieldName);
    this.add(jLabel2);
    this.add(jLabel3);
    this.add(jSpinnerPort);
    this.add(jLabel4);
    this.add(jButtonTest);

    jButtonTest.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        if (!verify())
          return;
        
        try {
        	TongWebMonitorMethodOption method = getOptions();
        	if (method == null)
        		return;
        	
        	Object ret = (String) getContext().getServerFactory().getProbeService()
    					.executeMethod(getContext().getNode().getProbeId(), TongWebTest4Probe.class.getName(), "test",
    							getContext().getNode().getIp(), method);
          if(null==ret){
            JOptionPane.showMessageDialog(TongWebParamPanel.this, "成功连接到TongWeb", "成功",JOptionPane.INFORMATION_MESSAGE);
            return;
          }else{
            throw new Exception(ret.toString());
          }
        } catch (Throwable t) {
          ErrorDlg.createErrorDlg(TongWebParamPanel.this, "连接到TongWeb失败", t).setVisible(true);
        }
      }
    });
  }

  public TongWebMonitorMethodOption getOptions() {
    TongWebMonitorMethodOption option = new TongWebMonitorMethodOption();
    option.setJndiName(jTextFieldName.getText());
    option.setPort(snm_port.getNumber().intValue());
    return option;
  }

  public void setOptions(TongWebMonitorMethodOption option) {
    jTextFieldName.setText(option.getJndiName());
    snm_port.setValue(new Integer(option.getPort()));
  }

  public boolean verify() {
    return FieldValidator.textRequired(this, jTextFieldName, "JNDI名字");
  }

  public void setHost(String host) {
    this.host = host;
  }

  @Override
	public boolean getData() {
		if (!verify())
			return false;
		
		TongWebMonitorMethodOption method = getOptions();
		if (method == null)
			return false;
			
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new TongWebMonitorMethodOption(method));		
	}  
}
