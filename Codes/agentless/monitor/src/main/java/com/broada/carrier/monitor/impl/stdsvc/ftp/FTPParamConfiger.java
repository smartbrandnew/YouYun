package com.broada.carrier.monitor.impl.stdsvc.ftp;

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
 * FTP 监测类型的监测参数配置界面
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class FTPParamConfiger extends NumenMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private BorderLayout borderLayout1 = new BorderLayout();
  private SimpleNotePanel notePanel = new SimpleNotePanel();
  private JPanel jPanBody = new JPanel();
  private SpinnerNumberModel snm_port = new SpinnerNumberModel(1, 1, 65535, 1);
  JPanel jPanWonted = new JPanel();
  JTextField jTxtFilename = new JTextField();
  JCheckBox jChkFile = new JCheckBox();
  JPanel jPanParam = new JPanel();
  JLabel jLabel1 = new JLabel();
  JSpinner jSpnPort = new JSpinner(snm_port);
  BorderLayout borderLayout2 = new BorderLayout();
  TitledBorder titledBorder1;
  JCheckBox jChkAM = new JCheckBox();
  JCheckBox jChkNotAM = new JCheckBox();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JTextField jTxtUser = new JTextField();
  JPasswordField jTxtPasswd = new JPasswordField();
  JLabel jLabel4 = new JLabel();
  JCheckBox jChkLogin = new JCheckBox();

  /*对应编辑的参数*/
  private FTPParameter param = new FTPParameter();
  JLabel jLabel5 = new JLabel();

  public FTPParamConfiger() {
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.
        white, new Color(148, 145, 140)), "正常判断条件");
    notePanel.setNote("使用FTP协议监测指定的FTP服务，可以指定端口." +
                      "如果要更精确的监测，请输入登录用户名和密码，以及要监测的文件路径.");
    this.setLayout(borderLayout1);
    jPanBody.setLayout(borderLayout2);
    jTxtFilename.setEnabled(false);
    jTxtFilename.setToolTipText("");
    jTxtFilename.setText("");
    jTxtFilename.setBounds(new Rectangle(66, 142, 249, 22));
    jChkFile.setOpaque(false);
    jChkFile.setText("文件");
    jChkFile.setBounds(new Rectangle(17, 142, 50, 22));
    jChkFile.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkFile_itemStateChanged(e);
      }
    });
    jPanParam.setLayout(null);
    jLabel1.setToolTipText("");
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("端口(Port)");
    jLabel1.setBounds(new Rectangle(3, 4, 100, 23));
    jSpnPort.setOpaque(false);
    jSpnPort.setBounds(new Rectangle(110, 4, 77, 24));
    jPanParam.setMinimumSize(new Dimension(1, 1));
    jPanParam.setPreferredSize(new Dimension(1, 30));
    jPanParam.setRequestFocusEnabled(true);
    jPanParam.setToolTipText("");
    jPanWonted.setBorder(titledBorder1);
    jPanWonted.setLayout(null);
    jChkAM.setBounds(new Rectangle(17, 21, 163, 22));
    jChkAM.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkAM_itemStateChanged(e);
      }
    });
    jChkAM.setText("允许匿名登录");
    jChkAM.setOpaque(false);
    jChkNotAM.setBounds(new Rectangle(17, 46, 119, 22));
    jChkNotAM.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkNotAM_itemStateChanged(e);
      }
    });
    jChkNotAM.setText("不允许匿名登录");
    jChkNotAM.setOpaque(false);
    jLabel2.setBounds(new Rectangle(324, 141, 66, 23));
    jLabel2.setText("存在");
    jLabel2.setToolTipText("");
    jLabel2.setHorizontalAlignment(SwingConstants.LEADING);
    jLabel3.setBounds(new Rectangle(0, 105, 58, 22));
    jLabel3.setText("用户名");
    jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel3.setToolTipText("");
    jTxtUser.setBounds(new Rectangle(59, 105, 106, 22));
    jTxtUser.setText("");
    jTxtUser.setToolTipText("");
    jTxtUser.setEnabled(false);
    jTxtPasswd.setEnabled(false);
    jTxtPasswd.setToolTipText("");
    jTxtPasswd.setText("");
    jTxtPasswd.setBounds(new Rectangle(227, 105, 119, 22));
    jLabel4.setToolTipText("");
    jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel4.setText("密码");
    jLabel4.setBounds(new Rectangle(170, 105, 54, 22));
    jChkLogin.setOpaque(false);
    jChkLogin.setText("以下用户成功登录");
    jChkLogin.setBounds(new Rectangle(17, 73, 163, 22));
    jChkLogin.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkLogin_itemStateChanged(e);
      }
    });
    jLabel5.setText("输入相对于登录用户的根目录的路径，例如:files/myfile.txt.");
    jLabel5.setBounds(new Rectangle(22, 166, 340, 19));
    jPanWonted.add(jChkAM, null);
    jPanWonted.add(jChkNotAM, null);
    jPanWonted.add(jChkLogin, null);
    jPanWonted.add(jTxtPasswd, null);
    jPanWonted.add(jTxtUser, null);
    jPanWonted.add(jLabel3, null);
    jPanWonted.add(jLabel4, null);
    jPanWonted.add(jChkFile, null);
    jPanWonted.add(jTxtFilename, null);
    jPanWonted.add(jLabel2, null);
    jPanWonted.add(jLabel5, null);
    this.add(notePanel, BorderLayout.SOUTH);
    this.add(jPanBody, BorderLayout.CENTER);
    jPanBody.add(jPanParam, BorderLayout.NORTH);
    jPanParam.add(jSpnPort, null);
    jPanParam.add(jLabel1, null);
    jPanBody.add(jPanWonted, BorderLayout.CENTER);
  }

  /**
   * 设置初始值
   * @param xml
   */
  public void setParameters(String xml) {
    param = new FTPParameter(xml);
    jSpnPort.setValue(new Integer(param.getPort()));
    jChkAM.setSelected(param.isAnonymous());
    jChkNotAM.setSelected(param.isNotAnonymous());
    jChkLogin.setSelected(param.isChkLogin());
    if (jChkLogin.isSelected()) {
      jTxtUser.setText(param.getUser());
      jTxtPasswd.setText(param.getPassword());
    }
    jChkFile.setSelected(param.isChkFile());
    if (jChkFile.isSelected()) {
      jTxtFilename.setText(param.getFilename());
    }
  }

  /**
   * 获取编辑结果
   * @return
   */
  public String getParameters() {
    param.setPort(snm_port.getNumber().intValue());
    param.setAnonymous(jChkAM.isSelected());
    param.setNotAnonymous(jChkNotAM.isSelected());
    param.setChkLogin(jChkLogin.isSelected());
    if (jChkLogin.isSelected()) {
      param.setUser(jTxtUser.getText().trim());
      param.setPassword(new String(jTxtPasswd.getPassword()));
    }
    if (jChkFile.isSelected()) {
      param.setFilename(jTxtFilename.getText().trim());
    } else {
      param.setFilename(null);
    }
    try {
      return param.getParameters();
    } catch (Exception ex) {
      ErrorDlg.createErrorDlg(this, "错误", "FTP服务监测参数转换成字符串版本错误.", true, ex).
          setVisible(true);
      return "";
    }
  }

  public boolean verify() {
    if (jChkFile.isSelected() && (!jChkLogin.isSelected())) {
      JOptionPane.showMessageDialog(this, "如果要监测文件是否存在，一定要校验用户登录.");
      jChkLogin.setSelected(true);
      return false;
    }
    if (jChkLogin.isSelected() && jTxtUser.getText().trim().length() == 0) {
      JOptionPane.showMessageDialog(this, "请输入登录的用户名和密码.");
      jTxtUser.requestFocus();
      return false;
    }
    if (jChkFile.isSelected() && jTxtFilename.getText().trim().length() == 0) {
      JOptionPane.showMessageDialog(this, "请输入要监测的文件路径.");
      jTxtFilename.requestFocus();
      return false;
    }

    return true;
  }

  public Component getConfigUI() {
    return this;
  }

  /**
   * 如果要监测文件是否存在，那么一定要设置用户名和密码
   * @param e
   */
  void jChkFile_itemStateChanged(ItemEvent e) {
    jTxtFilename.setEnabled(jChkFile.isSelected());
    if (jChkFile.isSelected()) {
      jChkLogin.setSelected(true);
      jTxtFilename.requestFocus();
    }
  }

  /**
   * 允许匿名和不允许匿名只能选其中一个
   * @param e
   */
  void jChkNotAM_itemStateChanged(ItemEvent e) {
    if (jChkNotAM.isSelected()) {
      jChkAM.setSelected(false);
    }
  }

  /**
   * 允许匿名和不允许匿名只能选其中一个
   * @param e
   */
  void jChkAM_itemStateChanged(ItemEvent e) {
    if (jChkAM.isSelected()) {
      jChkNotAM.setSelected(false);
    }
  }

  void jChkLogin_itemStateChanged(ItemEvent e) {
    jTxtUser.setEnabled(jChkLogin.isSelected());
    jTxtPasswd.setEnabled(jChkLogin.isSelected());
    if (jChkLogin.isSelected()) {
      jTxtUser.requestFocus();
    }
  }
}
