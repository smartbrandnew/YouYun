package com.broada.carrier.monitor.impl.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 * 一个比较丰富的弹出菜单.
 * <p>Title: 广通博大Coss</p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author nile black
 * @version 1.0
 */
public class EditAddrDialog extends JPopupMenu {
	private static final long serialVersionUID = 1L;
	private JScrollPane jspTable = new JScrollPane();
  private JTable jtHosts = new JTable(new DefaultTableModel(new Object[] {"IP"},0));
  private JButton jbOk = new JButton("添加");
  private JButton jbDelete = new JButton("删除");
  private JTextField value = null;

  public EditAddrDialog(JTextField v) {
    super();
    this.value = v;
    setOpaque(true);
    setDoubleBuffered(true);
    try {
      jbInit();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    //填充表格
    if ( (value != null) && (value.getText().indexOf("点击") == -1)) {
      String[] ss = value.getText().split(";");
      for (int i = 0; i < ss.length; i++) {
        if (ss[i].trim().length() != 0) {
          ( (DefaultTableModel) jtHosts.getModel()).addRow(new String[] {ss[i].trim()});
        }
      }
    }
  }

  /**
   * jb产生的初始化函数
   * @throws Exception 可能抛出的异常
   */
  private void jbInit() throws Exception {
    //由于用上了皮肤,整个边框变得很粗,所以,改了
    setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setSize(new Dimension(149, 160));
    setPreferredSize(new Dimension(149, 160));
    setLayout(new BorderLayout());
    JPanel tp = new JPanel(new BorderLayout());
    tp.setLayout(new FlowLayout());
    tp.setPreferredSize(new Dimension(127, 32));
    jbOk.setPreferredSize(new Dimension(64, 24));
    jbOk.setToolTipText("添加新的地址");
    jbOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbOk_actionPerformed(e);
      }
    });
    jbDelete.setPreferredSize(new Dimension(64, 24));
    jbDelete.setToolTipText("删除列表选中的地址");
    jbDelete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbDelete_actionPerformed(e);
      }
    });
    jtHosts.getTableHeader().setPreferredSize(new Dimension(0,0));
    jtHosts.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jtHosts.setToolTipText("鼠标双击某行进行编辑");
    jtHosts.setRowSelectionAllowed(true);
    //add
    add(tp, BorderLayout.SOUTH);
    add(jspTable, BorderLayout.CENTER);
    jtHosts.setSize(149, 80);
    jspTable.getViewport().add(jtHosts);
    tp.add(jbOk);
    tp.add(jbDelete, null);
  }

  /**
   * 重载了setVisible方法,在隐藏的时候把值取出来
   * @param b 是否可见
   */
  public void setVisible(boolean b) {
    if (!b) {
      try {
        if(jtHosts.isEditing()) {
          jtHosts.getCellEditor(jtHosts.getEditingRow(),0).stopCellEditing();
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      DefaultTableModel model = (DefaultTableModel) jtHosts.getModel();
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < model.getRowCount(); i++) {
        String cell = model.getValueAt(i,0).toString().trim();
        if(cell.equals(""))
          continue;
        sb.append(cell).append(";");
      }
      if(sb.length() > 0)
        sb.deleteCharAt(sb.length()-1);
      value.setText(sb.toString());
    }
    super.setVisible(b);
  }

  public void jbOk_actionPerformed(ActionEvent e) {
    DefaultTableModel model = (DefaultTableModel)jtHosts.getModel();
    model.addRow(new String[] {""});
    jtHosts.scrollRectToVisible(jtHosts.getCellRect(model.getRowCount() - 1, 0, false));
    jtHosts.editCellAt(model.getRowCount() - 1, 0);
  }


  public void jbDelete_actionPerformed(ActionEvent e) {
    DefaultTableModel model = (DefaultTableModel)jtHosts.getModel();
    int row = jtHosts.getSelectedRow();
    if (row != -1)
      model.removeRow(row);
    else
      JOptionPane.showMessageDialog(this,"请选择列表中的一行！","提示",JOptionPane.INFORMATION_MESSAGE);
  }
}
