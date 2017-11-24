package com.broada.carrier.monitor.impl.stdsvc.http;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.impl.common.NumenMonitorConfiger;
import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.swing.util.ErrorDlg;

/**
 * HTTP 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class HTTPParamConfiger extends NumenMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private BorderLayout borderLayout1 = new BorderLayout();
  private SimpleNotePanel notePanel = new SimpleNotePanel();
  private JPanel jPanBody = new JPanel();
  private SpinnerNumberModel snm_port = new SpinnerNumberModel(1, 1, 65535, 1);
  private SpinnerNumberModel reply_time = new SpinnerNumberModel(5, 1, 65535, 1);
  JPanel jPanWonted = new JPanel();
  JTextField jTxtNotContain = new JTextField();
  JCheckBox jChkNotContain = new JCheckBox();
  JPanel jPanParam = new JPanel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel1 = new JLabel();
  JTextField jTxtURL = new JTextField();
  JSpinner jSpnPort = new JSpinner(snm_port);
  BorderLayout borderLayout2 = new BorderLayout();
  TitledBorder titledBorder1;
  JCheckBox jChkStateCode = new JCheckBox();
  JTextField jTxtContain = new JTextField();
  JCheckBox jChkContain = new JCheckBox();
  JLabel jLabel3 = new JLabel();
  /*对应编辑的参数*/
  private HTTPParameter param = new HTTPParameter();
  private JCheckBox jChkDomain = new JCheckBox();
  private JTextField jTextDomain = new JTextField();
  private JLabel jLabUsername = new JLabel();
  private JTextField jTxtUsername = new JTextField();
  private JLabel jLabPassword = new JLabel();
  private JPasswordField jPwdPassword = new JPasswordField();
  private JCheckBox jChkAuth = new JCheckBox();
  //响应时间阈值
  private JLabel jTxtReplyTime = new JLabel();
  JSpinner jSpnTime = new JSpinner(reply_time);

  public HTTPParamConfiger() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    jPwdPassword.setEnabled(false);
    jTxtUsername.setEnabled(false);
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
        white, new Color(148, 145, 140)), "正常判断条件");
    notePanel.setNote("使用HTTP协议监测指定Web服务,可以指定端口." +
                      "可以设定通过获取某个URL页面的内容,是否包含(或不包含)某些字符串来判断服务是否异常.");
    this.setLayout(borderLayout1);
    jPanBody.setLayout(borderLayout2);
    jTxtNotContain.setEnabled(false);
    jTxtNotContain.setToolTipText("");
    jTxtNotContain.setText("");
    jTxtNotContain.setBounds(new Rectangle(133, 112, 141, 22));
    jChkNotContain.setOpaque(false);
    jChkNotContain.setText("页面不包含内容");
    jChkNotContain.setBounds(new Rectangle(16, 112, 119, 22));
    jChkNotContain.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkNotContain_itemStateChanged(e);
      }
    });
    jPanParam.setLayout(null);
    jLabel2.setBounds(new Rectangle(3, 38, 100, 23));
    jLabel2.setText("页面路径(URL)");
    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel2.setToolTipText("");
    jLabel1.setToolTipText("");
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("端口(Port)");
    jLabel1.setBounds(new Rectangle(10, 7, 93, 23));
    jTxtURL.setText("");
    jTxtURL.setBounds(new Rectangle(110, 38, 195, 23));
    jSpnPort.setOpaque(false);
    jSpnPort.setBounds(new Rectangle(110, 6, 77, 24));
    jPanParam.setMinimumSize(new Dimension(1, 1));
    jPanParam.setPreferredSize(new Dimension(1, 145));
    jPanParam.setRequestFocusEnabled(true);
    jPanParam.setToolTipText("");
    jPanWonted.setEnabled(false);
    jPanWonted.setBorder(titledBorder1);
    jPanWonted.setLayout(null);
    jChkStateCode.setBounds(new Rectangle(16, 56, 221, 22));
    jChkStateCode.setText("回应状态码等于200(成功)");
    jTxtContain.setBounds(new Rectangle(133, 84, 141, 22));
    jTxtContain.setText("");
    jTxtContain.setEnabled(false);
    jTxtContain.setToolTipText("");
    jChkContain.setBounds(new Rectangle(16, 84, 119, 22));
    jChkContain.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkContain_itemStateChanged(e);
      }
    });
    jChkContain.setText("页面内容包含");
    jChkContain.setOpaque(false);
    jLabel3.setMaximumSize(new Dimension(96, 16));
    jLabel3.setToolTipText("");
    jLabel3.setHorizontalAlignment(SwingConstants.LEADING);
    jLabel3.setText("<html>一定要以\"/\"开头,比如\"/test\".监测时会自动组合成\"http://[address]/test\"的正确格式,为空表示取默认页.");
    jLabel3.setBounds(new Rectangle(16, 69, 390, 33));
    jChkDomain.setText("使用域名");
    jChkDomain.setBounds(new Rectangle(198, 6, 80, 25));
    jChkDomain.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkDomain_itemStateChanged(e);
      }
    });
    jTextDomain.setEnabled(false);
    jTextDomain.setEditable(true);
    jTextDomain.setText("");
    jTextDomain.setBounds(new Rectangle(280, 7, 136, 22));
    jLabUsername.setPreferredSize(new Dimension(33, 25));
    jLabUsername.setText("用户名");
    jLabUsername.setBounds(new Rectangle(106, 112, 43, 16));
    jTxtUsername.setPreferredSize(new Dimension(6, 25));
    jTxtUsername.setText("");
    jTxtUsername.setBounds(new Rectangle(159, 109, 88, 22));
    jLabPassword.setPreferredSize(new Dimension(22, 25));
    jLabPassword.setText("密码");
    jLabPassword.setBounds(new Rectangle(267, 112, 34, 16));
    jPwdPassword.setPreferredSize(new Dimension(4, 25));
    jPwdPassword.setText("");
    jPwdPassword.setBounds(new Rectangle(309, 109, 94, 22));
    jChkAuth.setText("启用认证");
    jChkAuth.setBounds(new Rectangle(18, 108, 75, 25));
    jChkAuth.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkAuth_itemStateChanged(e);
      }
    });
    jTxtReplyTime.setText("响应时间阈值");
    jTxtReplyTime.setBounds(new Rectangle(38, 29, 121, 22));
    jSpnTime.setOpaque(false);
    jSpnTime.setBounds(new Rectangle(133,29,80,22));
    this.add(notePanel, BorderLayout.SOUTH);
    this.add(jPanBody, BorderLayout.CENTER);
    jPanBody.add(jPanWonted, BorderLayout.CENTER);
    jPanWonted.add(jChkStateCode, null);
    jPanWonted.add(jChkContain, null);
    jPanWonted.add(jTxtContain, null);
    jPanWonted.add(jChkNotContain, null);
    jPanWonted.add(jTxtNotContain, null);
    jPanWonted.add(jTxtReplyTime, null);
    jPanWonted.add(jSpnTime, null);
    jPanBody.add(jPanParam, BorderLayout.NORTH);
    jPanParam.add(jSpnPort, null);
    jPanParam.add(jLabel1, null);
    jPanParam.add(jLabel2, null);
    jPanParam.add(jTxtURL, null);
    jPanParam.add(jLabel3, null);
    jPanParam.add(jChkDomain, null);
    jPanParam.add(jTextDomain, null);
    jPanParam.add(jChkAuth, null);
    jPanParam.add(jPwdPassword, null);
    jPanParam.add(jTxtUsername, null);
    jPanParam.add(jLabPassword, null);
    jPanParam.add(jLabUsername, null);
  }

  /**
   * 设置初始值
   * @param xml
   */
  public void setParameters(String xml) {
    param = new HTTPParameter(xml);
    jSpnPort.setValue(new Integer(param.getPort()));
    jSpnTime.setValue(new Integer(param.getReplyTime()));
    jTxtURL.setText(param.getURL());
    jChkStateCode.setSelected(param.isChkStatusCode());
    boolean contain = param.isChkContain();
    jChkContain.setSelected(contain);
    if (contain) {
      jTxtContain.setText(param.getContain());
    }
    boolean notcontain = param.isChkNotContain();
    jChkNotContain.setSelected(notcontain);
    if (notcontain) {
      jTxtNotContain.setText(param.getNotContain());
    }
    boolean domain = param.isChkDomain();
    jChkDomain.setSelected(domain);
    if (domain) {
      jTextDomain.setText(param.getDomain());
    }
    boolean auth = param.isChkAuth();
    jChkAuth.setSelected(auth);
    if (auth) {
      jTxtUsername.setText(param.getUsername());
      jPwdPassword.setText(param.getPassword() == null ? "" : param.getPassword());
//      jTxtRealm.setText(param.getRealm() == null ? "":param.getRealm());
    }

  }

  /**
   * 获取编辑结果
   * @return
   */
  public String getParameters() {
    param.setPort(snm_port.getNumber().intValue());
    param.setURL(jTxtURL.getText());
    param.setReplyTime(reply_time.getNumber().intValue());
    if (jChkContain.isSelected()) {
      param.setContain(jTxtContain.getText());
      jChkStateCode.setSelected(true);
    } else {
      param.setContain(null);
    }
    if (jChkNotContain.isSelected()) {
      param.setNotContain(jTxtNotContain.getText());
      jChkStateCode.setSelected(true);
    } else {
      param.setNotContain(null);
    }
    if (jChkDomain.isSelected()) {
      param.setDomain(jTextDomain.getText());
    } else {
      param.setDomain(null);
    }
    if (jChkAuth.isSelected()) {
      param.setUsername(jTxtUsername.getText());
      param.setPassword(new String(jPwdPassword.getPassword()));
//      param.setRealm(jTxtRealm.getText());
    } else {
      param.setUsername(null);
      param.setPassword(null);
      param.setRealm(null);
    }
    param.setChkStatusCode(jChkStateCode.isSelected());
    try {
      return param.getParameters();
    } catch (Exception ex) {
      ErrorDlg.createErrorDlg(this, "错误", "HTTP服务监测参数转换成字符串版本错误.", true, ex).
          setVisible(true);
      return "";
    }
  }

  public boolean verify() {
    boolean isContain = jChkContain.isSelected();
    String contain = jTxtContain.getText().trim();
    boolean isNotContain = jChkNotContain.isSelected();
    String notContain = jTxtNotContain.getText().trim();
    boolean isDomain = jChkDomain.isSelected();
    String domain = jTextDomain.getText().trim();
    boolean isAuth = jChkAuth.isSelected();
    String username = jTxtUsername.getText().trim();

    if (isContain && contain.length() == 0) {
      JOptionPane.showMessageDialog(this, "请输入页面内容包含的字符串.");
      jTxtContain.requestFocus();
      return false;
    }
    if (isNotContain && notContain.length() == 0) {
      JOptionPane.showMessageDialog(this, "请输入页面内容不包含的字符串.");
      jTxtNotContain.requestFocus();
      return false;
    }
    if (isContain && isNotContain) { //如果两个都选,文本不能相同
      if (contain.equals(notContain)) {
        JOptionPane.showMessageDialog(this, "您输入的页面包含内容和不包含相同,这样监测没有任何意义.");
        jTxtContain.requestFocus();
        return false;
      }
    }
    if (isDomain && domain.length() == 0) {
      JOptionPane.showMessageDialog(this, "请输入域名.");
      jTextDomain.requestFocus();
      return false;
    }
    if (isAuth && username.length() == 0) {
      JOptionPane.showMessageDialog(this, "请输入用户名.");
      jTxtUsername.requestFocus();
      return false;
    }
    return true;
  }

  public Component getConfigUI() {
    return this;
  }

  void jChkNotContain_itemStateChanged(ItemEvent e) {
    jTxtNotContain.setEnabled(jChkNotContain.isSelected());
    if (jChkNotContain.isSelected()) {
      jChkStateCode.setSelected(true);
    }
  }

  void jChkContain_itemStateChanged(ItemEvent e) {
    jTxtContain.setEnabled(jChkContain.isSelected());
    if (jChkContain.isSelected()) {
      jChkStateCode.setSelected(true);
    }
  }

  void jChkDomain_itemStateChanged(ItemEvent e) {
    jTextDomain.setEnabled(jChkDomain.isSelected());
    if (jChkDomain.isSelected()) {
      jChkStateCode.setSelected(true);
    }
  }

  void jChkAuth_itemStateChanged(ItemEvent e) {
    jTxtUsername.setEnabled(jChkAuth.isSelected());
    jPwdPassword.setEnabled(jChkAuth.isSelected());
//    jTxtRealm.setEnabled(jChkAuth.isSelected());
    if (jChkDomain.isSelected()) {
      jChkStateCode.setSelected(true);
    }
  }
}