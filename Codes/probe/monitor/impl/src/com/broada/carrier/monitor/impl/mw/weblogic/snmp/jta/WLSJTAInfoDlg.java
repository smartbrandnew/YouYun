package com.broada.carrier.monitor.impl.mw.weblogic.snmp.jta;

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
import javax.swing.SwingConstants;

import com.broada.utils.WinUtil;

/**
 * <p>Title: WLS JTA的当前信息框</p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author liudh@broada.com.cnS
 * @version 1.0
 */

public class WLSJTAInfoDlg extends JDialog {
	private static final long serialVersionUID = 1L;
	JPanel panelAll = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel1 = new JPanel();
  JLabel infoRbTaSys = new JLabel();
  JLabel infoTotalTa = new JLabel();
  JLabel jLabel6 = new JLabel();
  JLabel infoRbTaRc = new JLabel();
  JLabel jLabel3 = new JLabel();
  JLabel jLabel5 = new JLabel();
  JPanel jPanel2 = new JPanel();
  JButton jButtonOK = new JButton();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel7 = new JLabel();
  JLabel infoRbTaApp = new JLabel();

  public static WLSJTAInfoDlg createDlg(Component comp) {
    Window win = WinUtil.getWindowForComponent(comp);
    WLSJTAInfoDlg dlg = null;
    if(win instanceof Frame)
      dlg = new WLSJTAInfoDlg((Frame)win,"Weblogic JTA当前信息",true);
    else if (win instanceof Dialog)
      dlg = new WLSJTAInfoDlg((Dialog)win,"Weblogic JTA当前信息",true);
    else
      dlg = new WLSJTAInfoDlg((Dialog)null,"Weblogic JTA当前信息",true);
    WinUtil.toCenter(dlg);
    return dlg;
  }

  private WLSJTAInfoDlg(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private WLSJTAInfoDlg(Dialog dlg, String title, boolean modal) {
    super(dlg, title, modal);
    try {
      jbInit();
      pack();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private WLSJTAInfoDlg() {
    this((Frame)null, "", false);
  }

  private void jbInit() throws Exception {
    panelAll.setLayout(borderLayout1);
    panelAll.setPreferredSize(new Dimension(400, 180));
    jPanel1.setBorder(BorderFactory.createEtchedBorder());
    jPanel1.setOpaque(false);
    jPanel1.setPreferredSize(new Dimension(400, 140));
    jPanel1.setLayout(null);
    infoRbTaSys.setForeground(Color.blue);
    infoRbTaSys.setText("");
    infoRbTaSys.setBounds(new Rectangle(198, 88, 140, 21));
    infoTotalTa.setForeground(Color.blue);
    infoTotalTa.setText("");
    infoTotalTa.setBounds(new Rectangle(198, 37, 142, 21));
    jLabel6.setToolTipText("&nbsp;&nbsp;&nbsp;&nbsp;");
    jLabel6.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel6.setRequestFocusEnabled(true);
    jLabel6.setText("系统错误导致回滚的事务数:");
    jLabel6.setBounds(new Rectangle(25, 88, 165, 21));
    infoRbTaRc.setForeground(Color.blue);
    infoRbTaRc.setText("");
    infoRbTaRc.setBounds(new Rectangle(198, 63, 140, 21));
    jLabel3.setEnabled(true);
    jLabel3.setDebugGraphicsOptions(0);
    jLabel3.setDoubleBuffered(false);
    jLabel3.setMaximumSize(new Dimension(44, 16));
    jLabel3.setRequestFocusEnabled(true);
    jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel3.setIconTextGap(4);
    jLabel3.setText("已处理的事务数:");
    jLabel3.setBounds(new Rectangle(93, 37, 97, 21));
    jLabel5.setBounds(new Rectangle(38, 63, 152, 21));
    jLabel5.setText("资源错误导致回滚的事务数:");
    jLabel5.setRequestFocusEnabled(true);
    jLabel5.setToolTipText("&nbsp;&nbsp;&nbsp;&nbsp;");
    jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
    jPanel2.setPreferredSize(new Dimension(10, 45));
    jButtonOK.setText("确定");
    jButtonOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButtonOK_actionPerformed(e);
      }
    });
    jLabel1.setText("系统成功的连接到Weblogic，并可获取到以下JTA当前信息:");
    jLabel1.setBounds(new Rectangle(15, 8, 325, 23));
    jLabel7.setBounds(new Rectangle(13, 114, 177, 21));
    jLabel7.setText("应用程序错误导致回滚的事务数:");
    jLabel7.setRequestFocusEnabled(true);
    jLabel7.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel7.setToolTipText("&nbsp;&nbsp;&nbsp;&nbsp;");
    infoRbTaApp.setBounds(new Rectangle(199, 113, 140, 21));
    infoRbTaApp.setText("");
    infoRbTaApp.setForeground(Color.blue);
    getContentPane().add(panelAll);
    panelAll.add(jPanel1, BorderLayout.NORTH);
    jPanel1.add(jLabel1, null);
    panelAll.add(jPanel2,  BorderLayout.CENTER);
    jPanel2.add(jButtonOK, null);
    jPanel1.add(jLabel6, null);
    jPanel1.add(jLabel7, null);
    jPanel1.add(infoRbTaSys, null);
    jPanel1.add(infoRbTaApp, null);
    jPanel1.add(jLabel3, null);
    jPanel1.add(infoTotalTa, null);
    jPanel1.add(jLabel5, null);
    jPanel1.add(infoRbTaRc, null);
  }

  /**下面的函数显示当前的情况*/
  public void setTotalTa(String info) {
    this.infoTotalTa.setText(info);
  }

  public void setRbTaRc(String info) {
    this.infoRbTaRc.setText(info);
  }

  public void setRbTaSys(String info) {
    this.infoRbTaSys.setText(info);
  }

  public void setRbTaApp(String info) {
    this.infoRbTaApp.setText(info);
  }

  void jButtonOK_actionPerformed(ActionEvent e) {
    this.dispose();
  }
}
