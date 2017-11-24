package com.broada.carrier.monitor.impl.mw.apache;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.impl.common.BaseMonitorConfiger;
import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.swing.util.ErrorDlg;

/**
 * APACHE 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang,,amyson
 * @version 1.0
 */

public class ApacheParamConfiger extends BaseMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private BorderLayout borderLayout1 = new BorderLayout();
  private SimpleNotePanel notePanel = new SimpleNotePanel();
  private JPanel jPanBody = new JPanel();
  private SpinnerNumberModel snm_port = new SpinnerNumberModel(1, 1, 65535, 1);

  JPanel jPanWonted = new JPanel();
  JPanel jPanParam = new JPanel();
  JLabel jLabel1 = new JLabel();
  JTextField jTxtURL = new JTextField();
  JSpinner jSpnPort = new JSpinner(snm_port);
  BorderLayout borderLayout2 = new BorderLayout();
  JCheckBox jkretCode = new JCheckBox();
  /*对应编辑的参数*/
  private ApacheParameter param = new ApacheParameter();
  private JCheckBox jChkDomain = new JCheckBox();
  private JTextField jTextDomain = new JTextField();
  JLabel jLabel10 = new JLabel();
  JCheckBox jChkDomain1 = new JCheckBox();
  JCheckBox jChkUseSSL = new JCheckBox();
  public ApacheParamConfiger() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    notePanel.setNote("使用HTTP协议监测指定的APACHE服务性能，可以指定端口." +
                      "当'使用SSL'选项选中时,使用HTTPS协议监测指定的APACHE服务性能.");
    this.setLayout(borderLayout1);
    jPanBody.setLayout(borderLayout2);
    jPanParam.setLayout(null);
    jLabel1.setToolTipText("");
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("端口(Port)");
    jLabel1.setBounds(new Rectangle(10, 7, 93, 23));
    jTxtURL.setEnabled(false);
    jTxtURL.setText("/server-status?auto");
    jTxtURL.setBounds(new Rectangle(110, 38, 195, 23));
    jSpnPort.setOpaque(false);
    jSpnPort.setBounds(new Rectangle(110, 6, 50, 24));
    jPanParam.setMinimumSize(new Dimension(1, 1));
    jPanParam.setPreferredSize(new Dimension(1, 80));
    jPanParam.setRequestFocusEnabled(true);
    jPanParam.setToolTipText("");
    jPanWonted.setEnabled(false);
    jPanWonted.setLayout(null);
    jkretCode.setBounds(new Rectangle(14, 22, 221, 22));
    jkretCode.setRolloverEnabled(false);
    jkretCode.setSelected(true);
    jkretCode.setText("回应状态码等于200(成功)");
    jChkDomain.setText("使用域名");
    jChkDomain.setBounds(new Rectangle(178, 6, 86, 25));
    jChkDomain.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkDomain_itemStateChanged(e);
      }
    });
    jTextDomain.setEnabled(false);
    jTextDomain.setEditable(true);
    jTextDomain.setText("");
    jTextDomain.setBounds(new Rectangle(269, 7, 136, 22));
    jChkDomain1.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkDomain1_itemStateChanged(e);
      }
    });
    jChkDomain1.setBounds(new Rectangle(0, 0, 66, 25));
    jChkDomain1.setText("使用域名");
    jChkUseSSL.setBounds(new Rectangle(178, 33, 104, 25));
    jChkUseSSL.setText("使用SSL");
    this.add(notePanel, BorderLayout.SOUTH);
    this.add(jPanBody, BorderLayout.CENTER);
    jPanBody.add(jPanWonted, BorderLayout.CENTER);
    jPanBody.add(jPanParam, BorderLayout.NORTH);
    jPanParam.add(jSpnPort, null);
    jPanParam.add(jLabel1, null);
    jPanParam.add(jChkDomain, null);
    jPanParam.add(jTextDomain, null);
    jPanParam.add(jChkUseSSL, null);
    jPanWonted.add(jkretCode, null);
  }

  /**
   * 设置初始值
   * @param xml
   */
  @Override
	public void setData(MonitorConfigContext data) {
  	super.setData(data);
    param = getTask().getParameterObject(ApacheParameter.class);
    if (param == null)
    	param = new ApacheParameter();
    jSpnPort.setValue(new Integer(param.getPort()));
    if(param.getURL() != null && param.getURL().length() > 1)
      jTxtURL.setText(param.getURL());
    if(param.isChkDomain()){
      jChkDomain.setSelected(true);
      jTextDomain.setText(param.getDomain());
      jTextDomain.setEnabled(true);
    }
    if(param.isUseSSL()){
      jChkUseSSL.setSelected(true);
    }
  }

  /**
   * 获取编辑结果
   * @return
   */
	@Override
	public boolean getData() {		
		if (!verify())
			return false;
		
    param.setPort(snm_port.getNumber().intValue());
    param.setURL(jTxtURL.getText());
    if(jChkDomain.isSelected()){
      param.setDomain(jTextDomain.getText());
    }else{
      param.setDomain(null);
    }
    if(jChkUseSSL.isSelected()){
      param.setUseSSL("true");
    }else{
      param.setUseSSL(null);
    }
    if (jChkDomain.isSelected()) {//domain
      param.setDomain(jTextDomain.getText());
    } else {
      param.setDomain(null);
    }

    param.setChkStatusCode(jkretCode.isSelected());
    try {      
      getTask().setParameterObject(param);
      return true;
    } catch (Exception ex) {
      ErrorDlg.createErrorDlg(this, "错误", "APACHE服务性能监测参数转换成字符串版本错误.", true, ex).setVisible(true);
      return false;
    }
  }

  public boolean verify() {
    String domain = jTextDomain.getText();
    if(domain == null)
      domain = "";
    if (jChkDomain.isSelected() && domain.length() == 0) {
      JOptionPane.showMessageDialog(this, "请输入域名.");
      jTextDomain.requestFocus();
      return false;
    }
    return true;
  }

  public Component getConfigUI() {
    return this;
  }



  void jChkDomain_itemStateChanged(ItemEvent e) {
    jTextDomain.setEnabled(jChkDomain.isSelected());
  }


  void jChkDomain1_itemStateChanged(ItemEvent e) {

  }




}
