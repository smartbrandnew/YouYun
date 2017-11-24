package com.broada.carrier.monitor.impl.stdsvc.dns;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.broada.utils.WinUtil;

/**
 * 添加和显示DNS监测条件的控制面板
 * 
 */
public class ConditionPanel extends JPanel {
  private static final long serialVersionUID = 5676580732126908824L;

  private List<DNSMonitorCondition> dnsConditions = new ArrayList<DNSMonitorCondition>();

  private DNSConditionTableModel dnsConditionTableModel = new DNSConditionTableModel();

  private JTable jDNSConditionTable = new JTable(dnsConditionTableModel);

  private JScrollPane jScrollPane = new JScrollPane();
  
  private JPanel jButtonPanel = new JPanel();
  private JPanel jMainPanel = new JPanel();

  private JPopupMenu menu = new JPopupMenu();
  
  private JMenuItem addMenuItem = new JMenuItem("添加");

  private JMenuItem delMenuItem = new JMenuItem("删除");
  
  private JButton addButton = new JButton("添加");
  private JButton delButton = new JButton("删除");
  private Boolean showLabel4 = true;

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
   * 判断某个端口+域名是否已经被监测
   * @param condition
   * @return
   */
  private boolean isPortAndNameMornitered(int port,String parsename){
    for(int index = 0; index < dnsConditions.size(); index++){
      DNSMonitorCondition cond = (DNSMonitorCondition) dnsConditions.get(index);
      if(cond.getPort() == port&&cond.getParsename()!=null&&cond.getParsename().equals(parsename))
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
    jDNSConditionTable.addMouseListener(new MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          menu.show(jDNSConditionTable, e.getX(), e.getY());
          int row = jDNSConditionTable.rowAtPoint(e.getPoint());
          jDNSConditionTable.setRowSelectionInterval(row,row);
        }
      }
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2){
          int row = jDNSConditionTable.rowAtPoint(e.getPoint());
          showLabel4 = false;
          ConditionSetDlg dlg = createDialog(ConditionPanel.this,"修改监测条件" ,true);
          dlg.setCondition((DNSMonitorCondition)dnsConditions.get(row));
          dlg.setVisible(true);;
          if (dlg.OK) {
            dnsConditionTableModel.fireTableDataChanged();
          }
        }
      }
    });
  }

  private void initPanel() {
    this.setBorder(new TitledBorder("DNS监测条件设置-按右键添加或删除"));
    jDNSConditionTable.setRowHeight(20);
    jDNSConditionTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    jScrollPane.getViewport().add(jDNSConditionTable);
    jButtonPanel.add(addButton,null);
    jButtonPanel.add(delButton,null);
    setLayout(new BorderLayout());
    add(jButtonPanel,BorderLayout.NORTH);
    add(jScrollPane,BorderLayout.CENTER);
    add(jMainPanel, BorderLayout.SOUTH);
  }

  public List<DNSMonitorCondition> getDnsConditions() {
    return dnsConditions;
  }

  public void setDnsConditions(List<DNSMonitorCondition> dnsConditions) {
  	this.dnsConditions.clear();
    this.dnsConditions.addAll(dnsConditions);
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
  	showLabel4 = true;
    ConditionSetDlg dlg = createDialog(ConditionPanel.this, "添加监测条件", true);
     dlg.setVisible(true);;
     if (dlg.OK) {
    //modified by shiting
       if(dlg.conditions.size()<=1){
    	   dnsConditions.add(dlg.condition);
       }else{
    	   Iterator<DNSMonitorCondition> its = dlg.conditions.iterator();
    	   while(its.hasNext()){
    	   dnsConditions.add(its.next());
    	   }
       }
       dnsConditionTableModel.fireTableDataChanged();
     }
  }

  private void deleteCondition(ActionEvent event) {
    /* 是否选定了记录 */
     if (jDNSConditionTable.getSelectedRowCount() == 0) {
     JOptionPane.showMessageDialog(ConditionPanel.this, "请选定要删除的记录");
     return;
     }
     /* 询问用户是否要删除 */
     if (JOptionPane.showConfirmDialog(ConditionPanel.this, "确实要删除选定记录吗？", "警告",
     JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
     return;
     int[] selectedIndexs = jDNSConditionTable.getSelectedRows();
     dnsConditions.remove(selectedIndexs[0]);
     dnsConditionTableModel.fireTableDataChanged();
  }

  class DNSConditionTableModel extends AbstractTableModel {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2626189556147454554L;

    private final String[] columns = new String[] { "端口", "延时(毫秒)", "服务运行", "域名" };

    public int getRowCount() {
      return dnsConditions.size();
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
      DNSMonitorCondition condition = (DNSMonitorCondition) dnsConditions.get(rowIndex);
      switch (columnIndex) {
      case 0:
        return "" + condition.getPort();
      case 1:
        return "" + condition.getTimeout();
      case 2:
        return "正常运行";
      case 3:
    	return "" + condition.getParsename()==null?"":condition.getParsename();
      }
      return "";
    }
  }
  /** 设置DNS监测条件的对话框 * */
  class ConditionSetDlg extends JDialog {
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -3396542409177189739L;
    private JLabel jLabel1 = new JLabel("端口(port)");
    private JLabel jLabel2 = new JLabel("延时(timeout)");
    private JLabel jLabel3 = new JLabel("(毫秒)");
    private JLabel jLabel4 = new JLabel("注：可以添加多个域名，各个域名之间用;(分号)隔开.");
    //private JLabel jLabel5 = new JLabel("失败重试");
    //private JLabel jLabel6 = new JLabel("(次)");
    private SpinnerNumberModel snm_port = new SpinnerNumberModel(53, 1, 65535, 1);
    private JSpinner jSpnPort = new JSpinner(snm_port);
    private SpinnerNumberModel snm_timeout = new SpinnerNumberModel(5000, 1,
        Integer.MAX_VALUE, 1);
    private JSpinner jSpnTimeout = new JSpinner(snm_timeout);
    
    /*private ButtonGroup bG = new ButtonGroup();
    private JRadioButton jRdBtnUp = new JRadioButton("端口Up");
    private JRadioButton jRdBtnDown = new JRadioButton("端口Down");
    private JLabel jLabel4 = new JLabel("端口状态");*/
    
    private JCheckBox jChkRun = new JCheckBox();
    private JCheckBox jChkParse = new JCheckBox();
    private JTextField jTxtName = new JTextField();
    
    private JPanel jPanParam = new JPanel();
    private JPanel jPanWonted = new JPanel();
    
    private JButton okButton = new JButton("确定");
    private JButton closeButton = new JButton("关闭");
    
    boolean OK = false;
    
    private DNSMonitorCondition condition = null;
    private List<DNSMonitorCondition> conditions = new ArrayList<DNSMonitorCondition>();
    public void setCondition(DNSMonitorCondition condition){
      this.condition = condition;
      /*jRdBtnUp.setSelected(condition.getValue().equals("1"));
      jRdBtnDown.setSelected(condition.getValue().equals("0"));*/
      snm_port.setValue(new Integer(condition.getPort()));
      snm_timeout.setValue(new Integer(condition.getTimeout()));
      jChkParse.setSelected(condition.isChkParse());
      if(condition.isChkParse()){
    	  jTxtName.setText(condition.getParsename());
      }else{
    	  jTxtName.setEnabled(false);
      }
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
    	/*jChkParse.addItemListener(new ItemListener() {
    		public void itemStateChanged(ItemEvent itemEvent) {
    			jChkParse_itemStateChanged(itemEvent);
    		}
    	});*/
      okButton.addActionListener(new ActionListener(){

        public void actionPerformed(ActionEvent e) {
          
          /*if(!(jRdBtnUp.isSelected() || jRdBtnDown.isSelected())){
            JOptionPane.showMessageDialog(ConditionSetDlg.this, "请选择端口状态");
            return;
          }*/
          int port=((Integer)snm_port.getValue()).intValue();
          String parsename = jChkParse.isSelected()?jTxtName.getText().trim():null;
          if(isPortAndNameMornitered(port,parsename)){
            if(condition==null || (port!=condition.getPort()&&parsename!=condition.getParsename())){
              JOptionPane.showMessageDialog(ConditionSetDlg.this, "端口：" + snm_port.getValue() +"域名：" + parsename + "已经处于监测状态，不能再监测该端口");
              return;
            }
          }
        	if (jChkParse.isSelected() && jTxtName.getText().trim().length() == 0) {
        	      JOptionPane.showMessageDialog(ConditionSetDlg.this, "请输入要解析的域名.");
        	      jTxtName.requestFocus();
        	      return;
        	    }
          if(condition == null)
            condition = new DNSMonitorCondition();
          	condition.setPort(((Integer)snm_port.getValue()).intValue());
          	condition.setTimeout(((Integer)snm_timeout.getValue()).intValue());
          	condition.setChkrun("运行正常");
          if (jChkParse.isSelected()){
        	  //modified by shiting for monitoring more than one parse of the DNS
        	  String[] parseNames = jTxtName.getText().trim().split(";");
        	  if(parseNames.length>1){
        		  for(int i=0;i<parseNames.length;i++){
        			  DNSMonitorCondition dnscondition= new DNSMonitorCondition();
        			  dnscondition.setPort(((Integer)snm_port.getValue()).intValue());
        			  dnscondition.setTimeout(((Integer)snm_timeout.getValue()).intValue());
        	      dnscondition.setChkrun("运行正常");
        			  dnscondition.setParsename(parseNames[i]);
        			  dnscondition.setField(((Integer)snm_port.getValue()).intValue(), parseNames[i]);
        			  dnscondition.setValue("" +  1);
        			  conditions.add(dnscondition);
        		  }
        	  }else{
        		  condition.setParsename(jTxtName.getText().trim());
        		  condition.setField(((Integer)snm_port.getValue()).intValue(), jTxtName.getText().trim());
        	  }
          }else{
        	  condition.setParsename("");
        	  condition.setField(((Integer)snm_port.getValue()).intValue(), null);
          }
          condition.setValue("" +  1);//这里的值有什么用?
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
      jLabel4.setBounds(new Rectangle(24, 88, 305, 25));
      
      jChkRun.setBounds(new Rectangle(16, 24, 163, 22));
      jChkRun.setText("服务正常运行");
      jChkRun.setEnabled(false);
      jChkRun.setOpaque(false);
      jChkRun.setToolTipText("");
      jChkRun.setSelected(true);
      
      jChkParse.setOpaque(false);
      jChkParse.setToolTipText("");
      jChkParse.setText("能正确解析域名");
      jChkParse.setBounds(new Rectangle(16, 61, 127, 22));
      jChkParse.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent itemEvent) {
            jChkParse_itemStateChanged(itemEvent);
          }
        });
      
      jTxtName.setEnabled(false);
      jTxtName.setToolTipText("");
      jTxtName.setText("");
      jTxtName.setBounds(new Rectangle(146, 61, 219, 22));
      
      jPanParam.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.
          white, new Color(148, 145, 140)), "基本条件"));
      jPanParam.setLayout(null);
      jPanParam.add(jLabel1,null);
      jPanParam.add(jSpnPort,null);
      
      jPanParam.add(jLabel2,null);
      jPanParam.add(jLabel3,null);
     
      jPanParam.add(jSpnTimeout,null);
      //jPanParam.add(jSpnTimes,null);
      
      jPanWonted.setLayout(null);
      jPanWonted.setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.
          white, new Color(148, 145, 140)), "正常判断条件"));
      jPanWonted.add(jChkRun, null);
      jPanWonted.add(jChkParse, null);
      jPanWonted.add(jTxtName, null);
      if(showLabel4){
      	jPanWonted.add(jLabel4,null);
      }
      
      jPanParam.setBounds(10,10,370,120);
      jPanWonted.setBounds(10,170,370,140);
      okButton.setBounds(115,320,80,26);
      closeButton.setBounds(200,320,80,26);
      getContentPane().setLayout(null);
      getContentPane().add(jPanParam,null);
      getContentPane().add(jPanWonted,null);
      getContentPane().add(okButton,null);
      getContentPane().add(closeButton,null);
      setSize(400, 400);
    }
    
    void jChkParse_itemStateChanged(ItemEvent e) {
    	jTxtName.setEnabled(jChkParse.isSelected());
    }
  }
  
}
