package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jvm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.broada.utils.NumberUtil;
import com.broada.utils.WinUtil;

/**
 * <p>Title: WLS JVM的当前信息框</p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author liudh@broada.com.cnS
 * @version 1.0
 */

public class WLSJVMInfoDlg extends JDialog {
	private static final long serialVersionUID = 1L;
	JPanel panelAll = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JLabel jLabel4 = new JLabel();
  JLabel jLabelHeapSize = new JLabel();
  JPanel jPanel1 = new JPanel();
  JLabel jLabelJVMVersion = new JLabel();
  JLabel jLabelJVMName = new JLabel();
  JLabel jLabelHeapUtil = new JLabel();
  JLabel jLabel6 = new JLabel();
  JLabel jLabelJVMVendor = new JLabel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel7 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JPanel jPanel2 = new JPanel();
  JButton jButtonOK = new JButton();
  JLabel jLabel1 = new JLabel();

  public static WLSJVMInfoDlg createDlg(Component comp) {
    Window win = WinUtil.getWindowForComponent(comp);
    WLSJVMInfoDlg dlg = null;
    if(win instanceof Frame)
      dlg = new WLSJVMInfoDlg((Frame)win,"Weblogic JVM当前信息",true);
    else if (win instanceof Dialog)
      dlg = new WLSJVMInfoDlg((Dialog)win,"Weblogic JVM当前信息",true);
    else
      dlg = new WLSJVMInfoDlg((Dialog)null,"Weblogic JVM当前信息",true);
    WinUtil.toCenter(dlg);
    return dlg;
  }

  private WLSJVMInfoDlg(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private WLSJVMInfoDlg(Dialog dlg, String title, boolean modal) {
    super(dlg, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private WLSJVMInfoDlg() {
    this((Frame)null, "", false);
  }

  private void jbInit() throws Exception {
    panelAll.setLayout(borderLayout1);
    panelAll.setPreferredSize(new Dimension(400, 160));
    jLabel4.setBounds(new Rectangle(213, 36, 94, 21));
    jLabel4.setText("当前堆栈大小:");
    jLabel4.setRequestFocusEnabled(true);
    jLabelHeapSize.setBounds(new Rectangle(313, 36, 102, 21));
    jLabelHeapSize.setForeground(Color.blue);
    jLabelHeapSize.setText("");
    jPanel1.setBorder(BorderFactory.createEtchedBorder());
    jPanel1.setOpaque(false);
    jPanel1.setPreferredSize(new Dimension(400, 120));
    jPanel1.setLayout(null);
    jLabelJVMVersion.setForeground(Color.blue);
    jLabelJVMVersion.setText("");
    jLabelJVMVersion.setBounds(new Rectangle(73, 86, 140, 21));
    jLabelJVMName.setForeground(Color.blue);
    jLabelJVMName.setText("");
    jLabelJVMName.setBounds(new Rectangle(73, 36, 142, 21));
    jLabelHeapUtil.setForeground(Color.blue);
    jLabelHeapUtil.setText("");
    jLabelHeapUtil.setBounds(new Rectangle(313, 61, 86, 21));
    jLabel6.setToolTipText("&nbsp;&nbsp;&nbsp;&nbsp;");
    jLabel6.setRequestFocusEnabled(true);
    jLabel6.setText("JVM版本:");
    jLabel6.setBounds(new Rectangle(15, 87, 63, 21));
    jLabelJVMVendor.setForeground(Color.blue);
    jLabelJVMVendor.setText("");
    jLabelJVMVendor.setBounds(new Rectangle(73, 61, 140, 21));
    jLabel3.setRequestFocusEnabled(true);
    jLabel3.setText("JVM名称:");
    jLabel3.setBounds(new Rectangle(15, 36, 62, 21));
    jLabel7.setRequestFocusEnabled(true);
    jLabel7.setText("当前堆栈利用率:");
    jLabel7.setBounds(new Rectangle(213, 61, 106, 21));
    jLabel5.setBounds(new Rectangle(16, 61, 60, 21));
    jLabel5.setText("JVM厂商:");
    jLabel5.setRequestFocusEnabled(true);
    jLabel5.setToolTipText("&nbsp;&nbsp;&nbsp;&nbsp;");
    jPanel2.setPreferredSize(new Dimension(10, 45));
    jButtonOK.setText("确定");
    jButtonOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButtonOK_actionPerformed(e);
      }
    });
    jLabel1.setText("系统成功的连接到Weblogic，并可获取到以下JVM当前信息:");
    jLabel1.setBounds(new Rectangle(15, 8, 325, 23));
    getContentPane().add(panelAll);
    panelAll.add(jPanel1, BorderLayout.NORTH);
    jPanel1.add(jLabelJVMName, null);
    jPanel1.add(jLabel6, null);
    jPanel1.add(jLabelJVMVersion, null);
    jPanel1.add(jLabel3, null);
    jPanel1.add(jLabel5, null);
    jPanel1.add(jLabelJVMVendor, null);
    jPanel1.add(jLabel1, null);
    jPanel1.add(jLabel4, null);
    jPanel1.add(jLabel7, null);
    jPanel1.add(jLabelHeapSize, null);
    jPanel1.add(jLabelHeapUtil, null);
    panelAll.add(jPanel2,  BorderLayout.CENTER);
    jPanel2.add(jButtonOK, null);
  }

  /**下面的函数显示当前的情况*/
  public void setJVMName(String name) {
    this.jLabelJVMName.setText(name);
  }

  public void setJVMVendor(String vendor) {
    this.jLabelJVMVendor.setText(vendor);
  }

  public void setJVMVersion(String version) {
    this.jLabelJVMVersion.setText(version);
  }

  public void setJVMHeapSize(double byteSize) {
    this.jLabelHeapSize.setText(NumberUtil.round(byteSize/1024/1024,2) + "M");
  }

  public void setJVMHeapUtil(double heapUtil) {
    this.jLabelHeapUtil.setText(NumberUtil.round(heapUtil,2) + "%");
  }

  void jButtonOK_actionPerformed(ActionEvent e) {
    this.dispose();
  }
}
