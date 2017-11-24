package com.broada.carrier.monitor.method.domino;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.swing.util.ErrorDlg;
import lotus.domino.NotesException;
import lotus.domino.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DominoParamPanel extends BaseMethodConfiger {

  private static final long serialVersionUID = 1698447891046312756L;

  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel jPanParam = new JPanel();
  private JLabel jLabelUser = new JLabel();
  private JTextField jTextFieldUser = new JTextField();
  private JLabel jLabelPassword = new JLabel();
  private JPasswordField jPasswordFieldPass = new JPasswordField();
  private JLabel jLabelPort = new JLabel();
  private SpinnerNumberModel iorPortModel = new SpinnerNumberModel(63148,1,65535,1);
  private JSpinner jSpnIORPort = new JSpinner(iorPortModel);
  private JLabel jLabelVerion = new JLabel();
  private JComboBox jCmbVersion = new JComboBox();
  private Session sess = null;
  private boolean isConnectOK = true;

  //NUM-469
  JButton jButtonTest = new JButton("测试连接");

  public DominoParamPanel() {
    try {
      jbInit();
      initPanel();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    this.setLayout(borderLayout1);
    jPanParam.setLayout(null);
    jLabelUser.setToolTipText("");
    jLabelUser.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabelUser.setText("监测用户名:");
    jLabelUser.setBounds(new Rectangle( -1, 4, 86, 23));
    jTextFieldUser.setText("");
    jTextFieldUser.setBounds(new Rectangle(92, 4, 77, 24));
    jPanParam.setMinimumSize(new Dimension(1, 1));
    jPanParam.setPreferredSize(new Dimension(1, 70));
    jPanParam.setRequestFocusEnabled(true);
    jPanParam.setToolTipText("");
    jLabelPassword.setBounds(new Rectangle(197, 4, 89, 23));
    jLabelPassword.setText("用户密码:");
    jLabelPassword.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabelPassword.setToolTipText("");
    jPasswordFieldPass.setText("");
    jPasswordFieldPass.setBounds(new Rectangle(293, 4, 70, 24));
    jLabelPort.setBounds(new Rectangle( -1, 43, 86, 23));
    jLabelPort.setText("IOR获取端口:");
    jLabelPort.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabelPort.setToolTipText("");
    jSpnIORPort.setFont(new java.awt.Font("Dialog", 0, 12));
    jSpnIORPort.setBounds(new Rectangle(92, 42, 102, 24));
    jLabelVerion.setToolTipText("");
    jLabelVerion.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabelVerion.setText("软件版本:");
    jLabelVerion.setBounds(new Rectangle(197, 43, 89, 23));
    jCmbVersion.setBounds(new Rectangle(292, 42, 72, 24));
    jButtonTest.setBounds(new Rectangle(92, 82, 102, 24)); //NUM-469

    // NUM-469
    jButtonTest.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        //测试参数配置的有效性
        connectionTest();
      }
    });
    //

    this.add(jPanParam, BorderLayout.CENTER);
    jPanParam.add(jLabelUser, null);
    jPanParam.add(jTextFieldUser, null);
    jPanParam.add(jPasswordFieldPass, null);
    jPanParam.add(jLabelPassword, null);
    jPanParam.add(jSpnIORPort, null);
    jPanParam.add(jLabelVerion, null);
    jPanParam.add(jCmbVersion, null);
    jPanParam.add(jLabelPort, null);
    jPanParam.add(jButtonTest, null); //NUM-469
  }

  private void initPanel() {
    jCmbVersion.addItem("R5");
    jCmbVersion.addItem("R6");
    jCmbVersion.addItem("R7");
  }

  private void connectionTest() {
  	if (!verify())
      return;

    isConnectOK = false;
		try {
			DominoMonitorMethodOption method = getOptions();
				if (method == null)
					return;

			Object ret = (String) getContext().getServerFactory().getProbeService()
					.executeMethod(getContext().getNode().getProbeId(), DominoTest4Probe.class.getName(), "test",
							getContext().getNode().getIp(), method);
			if (null == ret) {
				isConnectOK = true;
				JOptionPane.showMessageDialog(this, "连接Domino服务器成功!");
				return;
      }else{
        throw new Exception(ret.toString());
      }
    } catch (Throwable t) {
      ErrorDlg.createErrorDlg(DominoParamPanel.this, "错误提示", "连接Domino服务器失败", t).setVisible(true);
    }

  }

  private void recycleSession() {
    try {
      if (sess != null) {
        isConnectOK = false;
        sess.recycle();
      }
    } catch (NotesException ex) {
    }
  }

  public boolean verify() {
    if (!isConnectOK) {
      recycleSession();
      return false;
    }
    return true;
  }

  public DominoMonitorMethodOption getOptions() {
    DominoMonitorMethodOption option = new DominoMonitorMethodOption();
    option.setUsername(jTextFieldUser.getText());
    option.setPassword(new String(jPasswordFieldPass.getPassword()));
    option.setPort(iorPortModel.getNumber().intValue());
    option.setVersion(jCmbVersion.getSelectedItem().toString());
    return option;
  }

  public void setOptions(DominoMonitorMethodOption option) {
    jTextFieldUser.setText(option.getUsername());
    jPasswordFieldPass.setText(option.getPassword());
    iorPortModel.setValue(new Integer(option.getPort()));
    jCmbVersion.setSelectedItem(option.getVersion());
  }

	@Override
	public boolean getData() {
		if (!verify())
			return false;

		DominoMonitorMethodOption method = getOptions();
		if (method == null)
			return false;

		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		setOptions(new DominoMonitorMethodOption(method));
	}
}
