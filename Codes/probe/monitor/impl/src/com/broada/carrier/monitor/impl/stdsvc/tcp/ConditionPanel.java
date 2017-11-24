package com.broada.carrier.monitor.impl.stdsvc.tcp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.broada.utils.WinUtil;

/**
 * 添加和显示TCP监测条件的控制面板
 * 
 * @author zhoucy (zhoucy@broada.com.cn)
 * Create By 2006-5-23 下午02:27:38
 */
public class ConditionPanel extends JPanel {
  private static final long serialVersionUID = 5676580732126908824L;

  private List<TCPMonitorCondition> tcpConditions = new ArrayList<TCPMonitorCondition>();

  private TCPConditionTableModel tcpConditionTableModel = new TCPConditionTableModel();

  private JTable jTCPConditionTable = new JTable(tcpConditionTableModel);

  private JScrollPane jScrollPane = new JScrollPane();
  
  private JPanel jButtonPanel = new JPanel();
  private JPanel jMainPanel = new JPanel();

  private JPopupMenu menu = new JPopupMenu();
  
  private JMenuItem addMenuItem = new JMenuItem("添加");

  private JMenuItem delMenuItem = new JMenuItem("删除");
  
  private JButton addButton = new JButton("添加");
  private JButton delButton = new JButton("删除");

  public ConditionPanel() {
    try {
      initPanel();
      initAction();
      initMenu();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * 判断某个端口是否已经被监测
   * @param condition
   * @return
   */
  private boolean isPortMornitered(int port){
    for(int index = 0; index < tcpConditions.size(); index++){
      TCPMonitorCondition cond = (TCPMonitorCondition) tcpConditions.get(index);
      if(cond.getPort() == port)
        return true;
    }
    return false;
  }
  private void initMenu() {
    menu.add(addMenuItem);
    menu.add(delMenuItem);
    addMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        addCondition(event);
      }
    });
    delMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
         deleteCondition(event);
      }
    });
  }

  private void initAction() {
    addButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent event) {
        addCondition(event);
      }});
    delButton.addActionListener(new ActionListener(){

      public void actionPerformed(ActionEvent event) {
        deleteCondition(event);
      }});
    jScrollPane.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          menu.show(jScrollPane, e.getX(), e.getY());
        }
      }
    });
    jTCPConditionTable.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          menu.show(jTCPConditionTable, e.getX(), e.getY());
          int row = jTCPConditionTable.rowAtPoint(e.getPoint());
          jTCPConditionTable.setRowSelectionInterval(row,row);
        }
      }
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2){
          int row = jTCPConditionTable.rowAtPoint(e.getPoint());
          ConditionSetDlg dlg = createDialog(ConditionPanel.this,"修改监测条件" ,true);
          dlg.setCondition((TCPMonitorCondition)tcpConditions.get(row));
          dlg.setVisible(true);;
          if (dlg.OK) {
            tcpConditionTableModel.fireTableDataChanged();
          }
        }
      }
    });
  }

  private void initPanel() {
    this.setBorder(new TitledBorder("TCP监测条件设置-按右键添加或删除"));
    jTCPConditionTable.setRowHeight(20);
    jTCPConditionTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    jScrollPane.getViewport().add(jTCPConditionTable);
    jButtonPanel.add(addButton,null);
    jButtonPanel.add(delButton,null);
    setLayout(new BorderLayout());
    add(jButtonPanel,BorderLayout.NORTH);
    add(jScrollPane,BorderLayout.CENTER);
    add(jMainPanel, BorderLayout.SOUTH);
  }

  public List<TCPMonitorCondition> getTcpConditions() {
    return tcpConditions;
  }

  public void setTcpConditions(List<TCPMonitorCondition> tcpConditions) {
  	this.tcpConditions.clear();
    this.tcpConditions.addAll(tcpConditions);
  }
  /**
   * 创建条件编辑对话框
   * 
   * @param parent
   * @param modal
   * @return
   */
  ConditionSetDlg createDialog(Component parent, String title, boolean modal) {
    Window window = WinUtil.getWindowForComponent(parent);
    ConditionSetDlg dlg = null;
    if (window instanceof Frame) {
      dlg = new ConditionSetDlg((Frame) window, title, modal);
    } else {
      dlg = new ConditionSetDlg((Dialog) window, title, modal);
    }
    WinUtil.toCenter(dlg);
    return dlg;
  }
  private void addCondition(ActionEvent event) {
    ConditionSetDlg dlg = createDialog(ConditionPanel.this, "添加监测条件", true);
     dlg.setVisible(true);;
     if (dlg.OK) {
       tcpConditions.add(dlg.condition);
       tcpConditionTableModel.fireTableDataChanged();
     }
  }

  private void deleteCondition(ActionEvent event) {
    /* 是否选定了记录 */
     if (jTCPConditionTable.getSelectedRowCount() == 0) {
     JOptionPane.showMessageDialog(ConditionPanel.this, "请选定要删除的记录");
     return;
     }
     /* 询问用户是否要删除 */
     if (JOptionPane.showConfirmDialog(ConditionPanel.this, "确实要删除选定记录吗？", "警告",
     JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
     return;
     int[] selectedIndexs = jTCPConditionTable.getSelectedRows();
     tcpConditions.remove(selectedIndexs[0]);
     tcpConditionTableModel.fireTableDataChanged();
  }

  class TCPConditionTableModel extends AbstractTableModel {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2626189556147454554L;

    private final String[] columns = new String[] { "端口", "延时(秒)", "重试次数(次)", "正常条件" };

    public int getRowCount() {
      return tcpConditions.size();
    }

    public String getColumnName(int column) {
      if (column < 0 || column >= columns.length) {
        return "未知列";
      }
      return columns[column];
    }

    public int getColumnCount() {
      return columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      TCPMonitorCondition condition = (TCPMonitorCondition) tcpConditions.get(rowIndex);
      switch (columnIndex) {
      case 0:
        return "" + condition.getPort();
      case 1:
        return "" + condition.getTimeout();
      case 2:
        return "" + condition.getTimes();
      case 3:
        return condition.getValue().equals("0")? "端口Down" : "端口Up";
      }
      return "";
    }
  }
  /** 设置TCP监测条件的对话框 * */
  class ConditionSetDlg extends JDialog {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3396542409177189739L;
    private JLabel jLabel1 = new JLabel("端口(port)");
    private JLabel jLabel2 = new JLabel("延时(timeout)");
    private JLabel jLabel3 = new JLabel("(秒)");
    private JLabel jLabel5 = new JLabel("失败重试");
    private JLabel jLabel6 = new JLabel("(次)");
    private SpinnerNumberModel snm_port = new SpinnerNumberModel(1, 1, 65535, 1);
    private JSpinner jSpnPort = new JSpinner(snm_port);
    private SpinnerNumberModel snm_timeout = new SpinnerNumberModel(5, 1,
        Integer.MAX_VALUE, 1);
    private JSpinner jSpnTimeout = new JSpinner(snm_timeout);
    private SpinnerNumberModel snm_times = new SpinnerNumberModel(0, 0, 10, 1);
    private JSpinner jSpnTimes = new JSpinner(snm_times);
    
    private ButtonGroup bG = new ButtonGroup();
    private JRadioButton jRdBtnUp = new JRadioButton("端口Up");
    private JRadioButton jRdBtnDown = new JRadioButton("端口Down");
    private JLabel jLabel4 = new JLabel("端口状态");
    
    private JPanel jPanParam = new JPanel();
    private JPanel jPanWonted = new JPanel();
    
    private JButton okButton = new JButton("确定");
    private JButton closeButton = new JButton("关闭");
    
    boolean OK = false;
    
    private TCPMonitorCondition condition = null;
    public void setCondition(TCPMonitorCondition condition){
      this.condition = condition;
      jRdBtnUp.setSelected(condition.getValue().equals("1"));
      jRdBtnDown.setSelected(condition.getValue().equals("0"));
      snm_port.setValue(new Integer(condition.getPort()));
      snm_timeout.setValue(new Integer(condition.getTimeout()));
      snm_times.setValue(new Integer(condition.getTimes()));
    }
    public ConditionSetDlg(Frame parent, String title, boolean modal) {
      super(parent, title, modal);
      init();
      initAction();
    }

    public ConditionSetDlg(Dialog parent, String title, boolean modal) {
      super(parent, title, modal);
      init();
      initAction();
    }
    private void initAction(){
      okButton.addActionListener(new ActionListener(){

        public void actionPerformed(ActionEvent e) {
          
          if(!(jRdBtnUp.isSelected() || jRdBtnDown.isSelected())){
            JOptionPane.showMessageDialog(ConditionSetDlg.this, "请选择端口状态");
            return;
          }
          int port=((Integer)snm_port.getValue()).intValue();
          if(isPortMornitered(port)){
            if(condition==null || port!=condition.getPort()){
              JOptionPane.showMessageDialog(ConditionSetDlg.this, "端口" + snm_port.getValue() + "已经处于监测状态，不能再监测该端口");
              return;
            }
          }
          if(condition == null)
            condition = new TCPMonitorCondition();
          condition.setPort(((Integer)snm_port.getValue()).intValue());
          condition.setTimeout(((Integer)snm_timeout.getValue()).intValue());
          condition.setTimes(((Integer)snm_times.getValue()).intValue());
          condition.setValue("" + (jRdBtnDown.isSelected() ? 0 : 1));
          OK = true;
          dispose();
        }});
      closeButton.addActionListener(new ActionListener(){

        public void actionPerformed(ActionEvent e) {
          OK = false;
          dispose();
        }});
    }
    private void init(){
      jLabel1.setBounds(new Rectangle(14, 18, 100, 23));
      jSpnPort.setBounds(new Rectangle(145, 17, 77, 24));
      
      jLabel2.setBounds(new Rectangle(14, 66, 100, 23));
      jSpnTimeout.setBounds(new Rectangle(145, 65, 77, 24));
      jLabel3.setBounds(new Rectangle(226, 65, 100, 23));
      
      jLabel5.setBounds(new Rectangle(14, 111, 100, 23));
      jSpnTimes.setBounds(new Rectangle(145, 111, 77, 24));
      jLabel6.setBounds(new Rectangle(224, 111, 100, 23));
     
      jLabel4.setBounds(new Rectangle(15, 22, 100, 23));
      jRdBtnDown.setSelected(false);
      jRdBtnUp.setSelected(true);
      jRdBtnUp.setBounds(new Rectangle(141, 23, 76, 20));
      jRdBtnDown.setBounds(new Rectangle(220, 23, 101, 20));
      
      bG.add(jRdBtnUp);
      bG.add(jRdBtnDown);
      
      jPanParam.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.
          white, new Color(148, 145, 140)), "基本条件"));
      jPanParam.setLayout(null);
      jPanParam.add(jLabel1,null);
      jPanParam.add(jSpnPort,null);
      
      jPanParam.add(jLabel2,null);
      jPanParam.add(jLabel3,null);
      jPanParam.add(jLabel5,null);
      jPanParam.add(jLabel6,null);
      
      jPanParam.add(jSpnTimeout,null);
      jPanParam.add(jSpnTimes,null);
      
      jPanWonted.setLayout(null);
      jPanWonted.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.
          white, new Color(148, 145, 140)), "正常判断条件"));
      jPanWonted.add(jLabel4, null);
      jPanWonted.add(jRdBtnUp, null);
      jPanWonted.add(jRdBtnDown, null);
      
      jPanParam.setBounds(10,10,350,160);
      jPanWonted.setBounds(10,170,350,60);
      okButton.setBounds(115,240,80,26);
      closeButton.setBounds(200,240,80,26);
      getContentPane().setLayout(null);
      getContentPane().add(jPanParam,null);
      getContentPane().add(jPanWonted,null);
      getContentPane().add(okButton,null);
      getContentPane().add(closeButton,null);
      setSize(380, 320);
    }
  }
}
