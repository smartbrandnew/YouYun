package com.broada.carrier.monitor.impl.db.oracle.secaccess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.impl.common.ui.EditAddrDialog;
import com.broada.carrier.monitor.impl.common.ui.SimpleNotePanel;
import com.broada.utils.DateUtil;
import com.broada.utils.WinUtil;

/**
 * <p>Title: RuleEditDlg</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class RuleEditDlg extends JDialog {
  private static final long serialVersionUID = 5317630231301872460L;
  public boolean isOk = false;
  JPanel jPanRule = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  SimpleNotePanel notePanel = new SimpleNotePanel();
  JPanel jPanel1 = new JPanel();
  Border border1;
  TitledBorder titledBorder1;
  JPanel jPanel2 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JLabel jLabel4 = new JLabel();
  JPanel jPanDaily = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
  SpinnerDateModel dsModel = new SpinnerDateModel();
  JSpinner jSpnStart = new JSpinner(dsModel);
  BorderLayout borderLayout4 = new BorderLayout();
  JPanel jPanWeek = new JPanel();
  SpinnerDateModel deModel = new SpinnerDateModel();
  JSpinner jSpnEnd = new JSpinner(deModel);
  JPanel jPanFilter = new JPanel();
  JPanel jPanWeekList = new JPanel();
  BorderLayout borderLayout3 = new BorderLayout();
  JCheckBox jChkDaily = new JCheckBox();
  JCheckBox jChkWeekAll = new JCheckBox();
  JPanel jPanel3 = new JPanel();
  JLabel jLabel1 = new JLabel();
  JTextField jTxtIpAddr = new JTextField();
  JLabel jLabel2 = new JLabel();
  JTextField jTxtDbUser = new JTextField();
  JLabel jLabel3 = new JLabel();
  JTextField jTxtOsUser = new JTextField();
  JLabel jLabel5 = new JLabel();
  JTextField jTxtProgram = new JTextField();
  JLabel jLabel6 = new JLabel();
  JPanel jPanel4 = new JPanel();
  JButton jBtnCancel = new JButton();
  JButton jBtnOK = new JButton();
  private JCheckBox[] jChkWeek = {
      new JCheckBox("星期日"), new JCheckBox("星期一"), new JCheckBox("星期二"),
      new JCheckBox("星期三"), new JCheckBox("星期四"), new JCheckBox("星期五"),
      new JCheckBox("星期六")};
  public static final String TIME_FORMAT = "HH:mm:ss";

  private RuleEditDlg(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    initDlg();
  }

  private RuleEditDlg(Dialog dialog, String title, boolean modal) {
    super(dialog, title, modal);
    initDlg();
  }

  public void initDlg() {
    try {
      jbInit();
      WinUtil.toCenter(this);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setSize(450,430);
    border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
    titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"允许访问Oracle数据库的规则配置");
    jPanRule.setDebugGraphicsOptions(0);
    jPanRule.setLayout(borderLayout1);
    notePanel.setNote("Oracle数据库的安全访问规则配置,其中远程主机为必须配置项。" +
                      "远程主机和特定业务程序支持通配符“*”和“?”,“*”能够匹配多个字符,“?”能够匹配一个字符。" +
                      "例如：machine*能够匹配machine123, he??o可以匹配hello。");
    jPanel1.setBorder(titledBorder1);
    jPanel1.setDebugGraphicsOptions(0);
    jPanel1.setLayout(borderLayout2);
    jLabel4.setBounds(new Rectangle(166, 37, 22, 22));
    jLabel4.setText("到");
    jPanDaily.setLayout(null);
    jPanDaily.setRequestFocusEnabled(true);
    jPanDaily.setPreferredSize(new Dimension(14, 70));
    jPanDaily.setBorder(null);
    jPanDaily.setBorder(null);
    jSpnStart.setEnabled(false);
    jSpnStart.setBounds(new Rectangle(76, 37, 83, 22));
    jPanWeek.setLayout(borderLayout3);
    jSpnEnd.setEnabled(false);
    jSpnEnd.setBounds(new Rectangle(192, 37, 83, 22));
    jPanFilter.setLayout(borderLayout4);
    jPanWeekList.setLayout(flowLayout1);
    jChkDaily.setAlignmentX((float) 0.0);
    jChkDaily.setText("在下列时间段允许访问");
    jChkDaily.setBounds(new Rectangle(5, 7, 147, 19));
    jChkDaily.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jChkDaily_actionPerformed(e);
      }
    });
    jChkDaily.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkDaily_itemStateChanged(e);
      }
    });
    jChkWeekAll.setText("全选");
    jChkWeekAll.setBounds(new Rectangle(5, 2, 125, 25));
    jChkWeekAll.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        jChkWeekAll_itemStateChanged(e);
      }
    });
    jPanel3.setBorder(null);
    jPanel3.setDebugGraphicsOptions(0);
    jPanel3.setPreferredSize(new Dimension(1, 30));
    jPanel3.setRequestFocusEnabled(true);
    jPanel3.setLayout(null);
    jPanel2.setLayout(null);
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("远程主机");
    jLabel1.setBounds(new Rectangle(4, 2, 80, 20));
    jTxtIpAddr.setText("");
    jTxtIpAddr.setBackground(Color.white);
    jTxtIpAddr.setDisabledTextColor(Color.black);
    jTxtIpAddr.setEditable(false);
    jTxtIpAddr.setEnabled(false);
    jTxtIpAddr.setBounds(new Rectangle(91, 2, 276, 20));
    jTxtIpAddr.addMouseListener(new MouseAdapter() {
      //点击弹出编辑菜单
      public void mouseClicked(MouseEvent e) {
        EditAddrDialog ead = new EditAddrDialog(jTxtIpAddr);
        ead.show(jTxtIpAddr, 0,22);
      }
    });
    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel2.setText("数据库用户");
    jLabel2.setBounds(new Rectangle(4, 30, 80, 20));
    jTxtDbUser.setText("");
    jTxtDbUser.setBounds(new Rectangle(91, 30, 276, 20));
    jTxtDbUser.setBackground(Color.white);
    jTxtDbUser.setDisabledTextColor(Color.black);
    jTxtDbUser.setEditable(false);
    jTxtDbUser.setEnabled(false);
    jTxtDbUser.addMouseListener(new MouseAdapter() {
      //点击弹出编辑菜单
      public void mouseClicked(MouseEvent e) {
        EditAddrDialog ead = new EditAddrDialog(jTxtDbUser);
        ead.show(jTxtDbUser, 0,22);
      }
    });
    jLabel3.setBounds(new Rectangle(4, 61, 80, 20));
    jLabel3.setText("操作系统用户");
    jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
    jTxtOsUser.setBounds(new Rectangle(91, 61, 276, 20));
    jTxtOsUser.setText("");
    jTxtOsUser.setBackground(Color.white);
    jTxtOsUser.setDisabledTextColor(Color.black);
    jTxtOsUser.setEditable(false);
    jTxtOsUser.setEnabled(false);
    jTxtOsUser.addMouseListener(new MouseAdapter() {
      //点击弹出编辑菜单
      public void mouseClicked(MouseEvent e) {
        EditAddrDialog ead = new EditAddrDialog(jTxtOsUser);
        ead.show(jTxtOsUser, 0,22);
      }
    });
    jLabel5.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel5.setText("特定业务程序");
    jLabel5.setBounds(new Rectangle(4, 89, 80, 20));
    jTxtProgram.setText("");
    jTxtProgram.setBounds(new Rectangle(91, 90, 276, 20));
    jTxtProgram.setBackground(Color.white);
    jTxtProgram.setDisabledTextColor(Color.black);
    jTxtProgram.setEditable(false);
    jTxtProgram.setEnabled(false);
    jTxtProgram.addMouseListener(new MouseAdapter() {
      //点击弹出编辑菜单
      public void mouseClicked(MouseEvent e) {
        EditAddrDialog ead = new EditAddrDialog(jTxtProgram);
        ead.show(jTxtProgram, 0,22);
      }
    });
    jLabel6.setText("(*)");
    jLabel6.setBounds(new Rectangle(371, 4, 17, 16));
    jPanFilter.setMinimumSize(new Dimension(10, 12));
    jPanFilter.setPreferredSize(new Dimension(14, 110));
    jPanel2.setMinimumSize(new Dimension(10, 12));
    jPanel2.setPreferredSize(new Dimension(14, 120));
    jPanel4.setPreferredSize(new Dimension(10, 30));
    jPanel4.setLayout(null);
    jBtnCancel.setBounds(new Rectangle(325, 4, 73, 26));
    jBtnCancel.setText("取  消");
    jBtnCancel.addActionListener(new ActionListener(this));
    jBtnCancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jBtnCancel_actionPerformed(e);
      }
    });
    jBtnOK.setBounds(new Rectangle(245, 4, 73, 26));
    jBtnOK.setText("确  定");
    jBtnOK.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jBtnOK_actionPerformed(e);
      }
    });
    this.getContentPane().add(jPanRule,  BorderLayout.CENTER);
    jPanRule.add(jPanel1,  BorderLayout.CENTER);
    jPanel1.add(jPanel2, BorderLayout.NORTH);
    jPanel1.add(jPanFilter,  BorderLayout.CENTER);
    jPanDaily.add(jSpnStart, null);
    jPanDaily.add(jSpnEnd, null);
    jPanDaily.add(jLabel4, null);
    jPanDaily.add(jChkDaily, null);
    jPanel1.add(jPanel4,  BorderLayout.SOUTH);
    jPanel4.add(jBtnCancel, null);
    jPanel4.add(jBtnOK, null);
    jPanRule.add(notePanel, BorderLayout.SOUTH);
    jPanFilter.add(jPanWeek, BorderLayout.CENTER);
    jPanFilter.add(jPanDaily, BorderLayout.NORTH);
    jPanWeek.add(jPanel3, BorderLayout.NORTH);
    jPanel3.add(jChkWeekAll, null);
    jPanWeek.add(jPanWeekList, BorderLayout.CENTER);
    jPanel2.add(jLabel6, null);
    jPanel2.add(jLabel1, null);
    jPanel2.add(jTxtIpAddr, null);
    jPanel2.add(jLabel3, null);
    jPanel2.add(jTxtOsUser, null);
    jPanel2.add(jTxtDbUser, null);
    jPanel2.add(jLabel2, null);
    jPanel2.add(jTxtProgram, null);
    jPanel2.add(jLabel5, null);
    dsModel.setValue(new Date(0));
    dsModel.setStart(new Date( -28800000)); //00:00:00
    dsModel.setEnd(new Date(57599000)); //23:59:59
    deModel.setValue(new Date(0));
    deModel.setStart(new Date( -28800000));
    deModel.setEnd(new Date(57599000));
    jSpnStart.setEditor(new DateEditor(jSpnStart, TIME_FORMAT));
    jSpnEnd.setEditor(new DateEditor(jSpnEnd, TIME_FORMAT));
    for (int i = 0; i < jChkWeek.length; i++) {
      jPanWeekList.add(jChkWeek[i], null);
    }
  }

  /**
   * 创建一个对话框
   * @param owner
   * @param title
   * @param modal
   * @return
   */
  public static RuleEditDlg createDialog(Component parent, String title, OracleSecAccessMonitorCondition cond) {
    Window window = WinUtil.getWindowForComponent(parent);
    RuleEditDlg dlg = null;
    if (window instanceof Frame) {
      dlg = new RuleEditDlg( (Frame) window, title, true);
    } else {
      dlg = new RuleEditDlg( (Dialog) window, title, true);
    }
    dlg.setCondition(cond);
    return dlg;
  }

  public static RuleEditDlg createDialog(Component parent, String title) {
    return createDialog(parent, title, null);
  }

  public void setCondition(OracleSecAccessMonitorCondition cond){
    if (cond != null){
      jTxtIpAddr.setText(cond.getIpAddr());
      jTxtDbUser.setText(cond.getDbUser());
      jTxtOsUser.setText(cond.getOsUser());
      jTxtProgram.setText(cond.getProgram());
      //设置允许访问的时间段
      if (!cond.getTimeDesc().equals("")){
        jChkDaily.setSelected(true);
        try {
          dsModel.setValue(DateUtil.TIME_FORMAT.parse(cond.getStartTime()));
        } catch (ParseException ex1) {
        }
        try {
          deModel.setValue(DateUtil.TIME_FORMAT.parse(cond.getEndTime()));
        } catch (ParseException ex) {
        }
      } else {
        jChkDaily.setSelected(false);
      }
      //设置允许访问的星期
      if (!cond.getWeek().equals("")){
        String dayOfWeek = cond.getWeek();
        for (int i = 0; i < jChkWeek.length; i++) {
          jChkWeek[i].setSelected(dayOfWeek.indexOf(String.valueOf(i+1)) > -1);
        }
      }
    } else {
      jChkWeekAll.setSelected(true);
    }
  }

  public OracleSecAccessMonitorCondition getCondition(){
    OracleSecAccessMonitorCondition cond = new OracleSecAccessMonitorCondition();
    cond.setIpAddr(jTxtIpAddr.getText());
    cond.setDbUser(jTxtDbUser.getText());
    cond.setOsUser(jTxtOsUser.getText());
    cond.setProgram(jTxtProgram.getText());
    if (jChkDaily.isSelected()){
      cond.setStartTime(DateUtil.TIME_FORMAT.format(dsModel.getDate()));
      cond.setEndTime(DateUtil.TIME_FORMAT.format(deModel.getDate()));
    }
    cond.setWeek(getDayOfWeek());
    return cond;
  }

  /**
   * 获取星期过滤的配置
   * @return
   */
  private String getDayOfWeek() {
    String dayOfWeek = "";
    for (int i = 0; i < jChkWeek.length; i++) {
      if (jChkWeek[i].isSelected()) {
        dayOfWeek += i+1;
      }
    }
    return dayOfWeek;
  }

  public void jBtnOK_actionPerformed(ActionEvent e) {
    String ip = jTxtIpAddr.getText().trim();
    if (ip == null || ip.equals("")){
      JOptionPane.showMessageDialog(this, "请输入远程主机", "输入错误", JOptionPane.ERROR_MESSAGE);
      jTxtIpAddr.requestFocus();
      return;
    }
    if (jChkDaily.isSelected() &&
        deModel.getDate().compareTo(dsModel.getDate()) <= 0) {
      JOptionPane.showMessageDialog(this, "允许访问的时间段中的结束时间一定要比开始时间大！");
      jSpnEnd.requestFocus();
      return;
    }

    if (getDayOfWeek().length() == 0) {
      JOptionPane.showConfirmDialog(this,
          "因为您在一周中都没有允许访问的日期，任何连接都将不被允许，请重新选择！",
          "输入错误",JOptionPane.ERROR_MESSAGE);
      return;
    }

    isOk=true;
    dispose();
  }

  public void jBtnCancel_actionPerformed(ActionEvent e) {
    dispose();
  }

  void jChkDaily_actionPerformed(ActionEvent e) {

  }

  /**
   * 改变时间段编辑框的激活状态
   * @param e
   */
  void jChkDaily_itemStateChanged(ItemEvent e) {
    jSpnStart.setEnabled(jChkDaily.isSelected());
    jSpnEnd.setEnabled(jChkDaily.isSelected());
  }

  /**
   * 修改对应的星期选择
   * @param e
   */
  void jChkWeekAll_itemStateChanged(ItemEvent e) {
    for (int i = 0; i < jChkWeek.length; i++) {
      jChkWeek[i].setSelected(jChkWeekAll.isSelected());
    }
  }
}

class ActionListener implements java.awt.event.ActionListener {
  RuleEditDlg adaptee;

  ActionListener(RuleEditDlg adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jBtnCancel_actionPerformed(e);
  }
}