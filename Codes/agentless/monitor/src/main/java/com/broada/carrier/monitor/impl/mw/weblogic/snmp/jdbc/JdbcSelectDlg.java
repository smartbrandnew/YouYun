package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jdbc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.broada.swing.util.WinUtil;

/**
 * <p>Title: </p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 1.0
 */

public class JdbcSelectDlg extends JDialog {
	private static final long serialVersionUID = 1L;
	JComboBox jCmbJdbcPool = new JComboBox();
  JLabel jLabel1 = new JLabel();
  JPanel panel1 = new JPanel();
  JPanel jPanel1 = new JPanel();
  JButton jBtnOK = new JButton();
  JButton jBtnCancel = new JButton();
  boolean isOk = true;

  private JdbcSelectDlg(Frame frame, String title, boolean modal) {
	    super(frame, title, modal);
	    initDlg();
  }

  private JdbcSelectDlg(Dialog dialog, String title, boolean modal) {
    super(dialog, title, modal);
    initDlg();
  }

  private void initDlg() {
    try {
      jbInit();
      WinUtil.toCenter(this);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * 创建一个对话框
   * @param owner
   * @param title
   * @param modal
   * @return
   */
  static JdbcSelectDlg createDialog(Component parent, String title) {
    Window window = WinUtil.getWindowForComponent(parent);
    JdbcSelectDlg dlg = null;
    if (window instanceof Frame) {
      dlg = new JdbcSelectDlg( (Frame) window, title, true);
    } else {
      dlg = new JdbcSelectDlg( (Dialog) window, title, true);
    }
    return dlg;
  }

  /**
   * 创建一个对话框
   * @param owner
   * @return
   */
  static JdbcSelectDlg createDialog(Component parent) {
    return createDialog(parent, "选择JDBC连接池");
  }

  /**
   * 关闭处理
   * @param e
   */
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jBtnCancel_actionPerformed(null);
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(315, 140));
    panel1.setLayout(null);
    panel1.setBorder(BorderFactory.createEtchedBorder());
    jLabel1.setBounds(new Rectangle(16, 31, 121, 20));
    jLabel1.setText("请选择Jdbc连接池");
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setFont(new java.awt.Font("Dialog", 0, 12));
    jLabel1.setForeground(Color.black);
    jCmbJdbcPool.setBounds(new Rectangle(144, 30, 130, 24));
    jBtnOK.setText("确  定");
    jBtnOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jBtnOK_actionPerformed(e);
      }
    });
    jBtnCancel.setText("取  消");
    jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jBtnCancel_actionPerformed(e);
      }
    });
    panel1.add(jCmbJdbcPool, null);
    panel1.add(jLabel1, null);
    this.getContentPane().add(jPanel1,  BorderLayout.SOUTH);
    jPanel1.add(jBtnOK, null);
    jPanel1.add(jBtnCancel, null);
    this.getContentPane().add(panel1, BorderLayout.CENTER);
  }

  /**
   * 设置参数并初始化显示
   * @param ipAddr
   * @param param
   */
  void setParameter(List list) {
    for (Iterator iter = list.iterator(); iter.hasNext(); ) {
      Object item = (Object) iter.next();
      jCmbJdbcPool.addItem(item);
    }
  }

  /******Action*******************************/

  void jBtnCancel_actionPerformed(ActionEvent e) {
    isOk = false;
    dispose();
  }

  void jBtnOK_actionPerformed(ActionEvent e) {
    isOk = true;
    this.dispose();
  }

}