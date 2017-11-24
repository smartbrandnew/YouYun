package com.broada.carrier.monitor.impl.stdsvc.smtp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.impl.common.NumenMonitorConfiger;
import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.swing.util.ErrorDlg;

/**
 * SMTP 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class SMTPParamConfiger extends NumenMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private SimpleNotePanel notePanel = new SimpleNotePanel();
  private BorderLayout borderLayout1 = new BorderLayout();
  private BorderLayout borderLayout2 = new BorderLayout();
  private JPanel jPanBody = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JLabel jLabel2 = new JLabel();
  private JLabel jLabel3 = new JLabel();
  private ButtonGroup bG = new ButtonGroup();
  private SpinnerNumberModel snm_port = new SpinnerNumberModel(1, 1, 65535, 1);
  private JSpinner jSpnPort = new JSpinner(snm_port);
  private SpinnerNumberModel snm_timeout = new SpinnerNumberModel(3, 1,
      Integer.MAX_VALUE, 1);
  private JSpinner jSpnTimeout = new JSpinner(snm_timeout);
  private JPanel jPanParam = new JPanel();
  private JPanel jPanWonted = new JPanel();
  private TitledBorder titledBorder1;
  private JRadioButton jRdBtnUp = new JRadioButton();
  //private JRadioButton jRdBtnDown = new JRadioButton();
  private JLabel jLabel4 = new JLabel();
  private JLabel jLabel5 = new JLabel();
  private JCheckBox jChkReplyTime = new JCheckBox();
  private SpinnerNumberModel pop3_replyTime = new SpinnerNumberModel(3, 1, 65535, 1);
  private JSpinner jSpnReplyTime = new JSpinner(pop3_replyTime);

  /*对应编辑的参数*/
  private SMTPParameter param = new SMTPParameter();

  public SMTPParamConfiger() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
        white, new Color(148, 145, 140)), "正常判断条件");
    this.setLayout(borderLayout1);
    notePanel.setNote("监测指定的SMTP服务是否正常,可以设定端口、超时时间" +
                      "和正常判断条件.");
    jLabel1.setToolTipText("");
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("端口(Port)");
    jLabel1.setBounds(new Rectangle(14, 18, 100, 23));
    jPanBody.setLayout(borderLayout2);
    jSpnPort.setBounds(new Rectangle(145, 17, 77, 24));
    jSpnTimeout.setBounds(new Rectangle(145, 65, 77, 24));
    jLabel2.setBounds(new Rectangle(14, 66, 100, 23));
    jLabel2.setText("延时(Timeout)");
    jLabel2.setToolTipText("");
    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel3.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel3.setToolTipText("");
    jLabel3.setText("(秒)");
    jLabel3.setBounds(new Rectangle(226, 65, 100, 23));
    jPanParam.setLayout(null);
    jPanParam.setMinimumSize(new Dimension(1, 1));
    jPanParam.setPreferredSize(new Dimension(1, 100));
    jPanParam.setRequestFocusEnabled(true);
    jPanParam.setBounds(new Rectangle(0, 0, 10, 10));
    jPanWonted.setBorder(titledBorder1);
    jPanWonted.setPreferredSize(new Dimension(10, 60));
    jPanWonted.setRequestFocusEnabled(true);
    jPanWonted.setToolTipText("");
    jPanWonted.setBounds(new Rectangle(0, 128, 400, 95));
    jPanWonted.setLayout(null);
    jRdBtnUp.setSelected(true);
    jRdBtnUp.setText("服务运行");
    jRdBtnUp.setBounds(new Rectangle(161, 23, 76, 20));
//    jRdBtnDown.setSelected(false);
//    jRdBtnDown.setText("服务停止");
//    jRdBtnDown.setBounds(new Rectangle(240, 23, 101, 20));
    jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel4.setToolTipText("");
    jLabel4.setText("服务状态");
    jLabel4.setBounds(new Rectangle(20, 22, 120, 25));
    jChkReplyTime.setBounds(new Rectangle(20, 50, 120, 25));
    jChkReplyTime.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkReplyTime_itemStateChanged(e);
      }
    });
    jChkReplyTime.setEnabled(true);
    jChkReplyTime.setToolTipText("");
    jChkReplyTime.setText("响应时间小于等于");
    jSpnReplyTime.setEnabled(false);
    jSpnReplyTime.setBounds(new Rectangle(161, 50, 76, 20));
    jLabel5.setHorizontalAlignment(SwingConstants.LEFT);
    jLabel5.setToolTipText("");
    jLabel5.setText("(秒)");
    jLabel5.setBounds(new Rectangle(242, 50, 100, 23));
    this.add(notePanel, BorderLayout.SOUTH);
    this.add(jPanBody, BorderLayout.CENTER);
    jPanParam.add(jLabel2, null);
    jPanParam.add(jLabel1, null);
    jPanParam.add(jSpnTimeout, null);
    jPanParam.add(jLabel3, null);
    jPanParam.add(jSpnPort, null);
    jPanBody.add(jPanParam, BorderLayout.NORTH);
    jPanBody.add(jPanWonted, BorderLayout.CENTER);
    jPanWonted.add(jLabel4, null);
    jPanWonted.add(jRdBtnUp, null);
    //jPanWonted.add(jRdBtnDown, null);
    jPanWonted.add(jChkReplyTime, null);
    jPanWonted.add(jSpnReplyTime, null);
    jPanWonted.add(jLabel5, null);
    bG.add(jRdBtnUp);
    //bG.add(jRdBtnDown);
  }

  /**
   * 设置初始值
   * @param xml
   */
  public void setParameters(String xml) {
    param = new SMTPParameter(xml);
    jSpnPort.setValue(new Integer(param.getPort()));
    jSpnTimeout.setValue(new Integer(param.getTimeout() / 1000));
    jChkReplyTime.setSelected(param.isChkReplyTime());
    if (param.getWonted() == 0) {} else {
      jRdBtnUp.setSelected(true);
    }
    if (jChkReplyTime.isSelected()) {
      this.jSpnReplyTime.setValue(new Integer(param.getReplyTime()));
    }
  }

  /**
   * 获取编辑结果
   * @return
   */
  public String getParameters() {
    param.setPort(snm_port.getNumber().intValue());
    param.setTimeout(snm_timeout.getNumber().intValue() * 1000);
    param.setWonted(jRdBtnUp.isSelected() ? 1 : 0);
    if (jChkReplyTime.isSelected()) {
      param.setReplyTime(pop3_replyTime.getNumber().intValue());
    } else {
      param.setReplyTime(-1);
    }
    try {
      return param.getParameters();
    } catch (Exception ex) {
      ErrorDlg.createErrorDlg(this, "错误", "SMTP服务监测参数转换成字符串版本错误.", true, ex).
          setVisible(true);
      return "";
    }
  }

  public boolean verify() {
    return true;
  }

  public Component getConfigUI() {
    return this;
  }
  

  void jChkReplyTime_itemStateChanged(ItemEvent e) {
    if(jChkReplyTime.isSelected())
      jSpnReplyTime.setEnabled(true);
    else
      jSpnReplyTime.setEnabled(false);
  }
}
