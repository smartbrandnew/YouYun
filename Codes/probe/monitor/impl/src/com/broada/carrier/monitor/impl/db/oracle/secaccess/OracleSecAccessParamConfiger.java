package com.broada.carrier.monitor.impl.db.oracle.secaccess;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

/**
 * <p>
 * Title: OracleSecAccessParamConfiger
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * @author plx
 * @version 2.3
 */

public class OracleSecAccessParamConfiger implements MonitorConfiger {
  class SecAccessParamPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    BorderLayout borderLayout1 = new BorderLayout();

    JPanel jPanel1 = new JPanel();

    Border border1;

    TitledBorder titledBorder1;

    Border border2;

    TitledBorder titledBorder2;

    BorderLayout borderLayout2 = new BorderLayout();

    JScrollPane jPanel2 = new JScrollPane();

    JPanel jPanel3 = new JPanel();

    JButton jBtnAdd = new JButton();

    JButton jBtnDel = new JButton();

    Border border3;

    TitledBorder titledBorder3;

    Border border4;

    TitledBorder titledBorder4;

    Border border5;

    TitledBorder titledBorder5;

    JButton jBtnMod = new JButton();

    Border border6;

    Border border7;

    private RulesTableModel model = new RulesTableModel();

    JTable jTabRules = new JTable(model);

    private OracleSecAccessParameter param = new OracleSecAccessParameter();

    Border border8;

    Border border9;

    Border border10;

    Border border11;

    public SecAccessParamPanel() {
      try {
        jbInit();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    /*
     * @see com.broada.srvmonitor.impl.host.ConditionConfig#getConditions()
     */
    public List getConditions() {
      param.removeAllCondition();
      List ruleList = new ArrayList(model.rules);
      for (Iterator itr = ruleList.iterator(); itr.hasNext();) {
        OracleSecAccessMonitorCondition cond = (OracleSecAccessMonitorCondition) itr.next();
        param.addCondition(cond);
      }
      return param.getConditions();
    }
    
    public void resetSecAccessInfo() {
      model.setContent(param.getConditions());
    }
    
    private void jbInit() throws Exception {
      border8 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
      border9 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
      border10 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
      border11 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
      jTabRules.setRowHeight(20);
      border2 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
      titledBorder2 = new TitledBorder(border2, "基本配置");
      border5 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
      titledBorder5 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140)),
          "允许访问Oracle数据库的规则列表");
      border6 = BorderFactory.createEmptyBorder();
      border7 = BorderFactory.createEtchedBorder(Color.white, new Color(148, 145, 140));
      this.setLayout(borderLayout1);
      this.setPreferredSize(new Dimension(20, 30));
      jPanel1.setBorder(titledBorder5);
      jPanel1.setPreferredSize(new Dimension(10, 10));
      jPanel1.setLayout(borderLayout2);
      jBtnAdd.setBounds(new Rectangle(200, 2, 62, 26));
      jBtnAdd.setHorizontalAlignment(SwingConstants.RIGHT);
      jBtnAdd.setText("添加");
      jBtnAdd.addActionListener(new SecAccessParamPanel_jBtnAdd_actionAdapter(this));
      jBtnDel.setBounds(new Rectangle(352, 2, 62, 26));
      jBtnDel.setMinimumSize(new Dimension(20, 26));
      jBtnDel.setPreferredSize(new Dimension(20, 26));
      jBtnDel.setHorizontalAlignment(SwingConstants.RIGHT);
      jBtnDel.setText("删除");
      jBtnDel.addActionListener(new SecAccessParamPanel_jBtnDel_actionAdapter(this));
      jPanel3.setLayout(null);
      jPanel3.setMinimumSize(new Dimension(20, 30));
      jPanel3.setPreferredSize(new Dimension(453, 30));
      jBtnMod.setText("修改");
      jBtnMod.addActionListener(new SecAccessParamPanel_jBtnMod_actionAdapter(this));
      jBtnMod.setHorizontalAlignment(SwingConstants.RIGHT);
      jBtnMod.setBounds(new Rectangle(276, 2, 62, 26));
      // jPanel2.setBorder(border11);
      jPanel2.setBorder(null);
      // jPanel2.setDebugGraphicsOptions(0);
      jPanel2.setPreferredSize(new Dimension(453, 300));
      this.add(jPanel1, BorderLayout.CENTER);
      jPanel1.add(jPanel2, BorderLayout.CENTER);
      jPanel1.add(jPanel3, BorderLayout.NORTH);
      jPanel3.add(jBtnMod, null);
      jPanel3.add(jBtnAdd, null);
      jPanel3.add(jBtnDel, null);
      jTabRules.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          jTabRules_mouseClicked(e);
        }
      });
      jPanel2.getViewport().add(jTabRules);
    }

    public boolean verify() {
      CellEditor ce = jTabRules.getCellEditor();
      if (jTabRules.isEditing() && ce != null) {
        ce.stopCellEditing();
      }

      if (model.getRowCount() == 0) {
        JOptionPane.showMessageDialog(this, "请至少配置一个访问规则，否则该监测没有任何意义！", "提示", JOptionPane.ERROR_MESSAGE);
        return false;
      }
      return true;
    }

    private class RulesTableModel extends AbstractTableModel {
			private static final long serialVersionUID = 1L;
      private String[] colnames = { "远程主机", "数据库用户", "特定业务程序" };
      /** Rule对象的列表 */
      private List rules = new ArrayList();

      public RulesTableModel() {

      }

      public int getRowCount() {
        return rules.size();
      }

      public int getColumnCount() {
        return colnames.length;
      }

      public Class getColumnClass(int columnIndex) {
        return Object.class;
      }

      public String getColumnName(int column) {
        if (column < 0 || column >= colnames.length) {
          return "未知列";
        }
        return colnames[column];
      }

      public Object getValueAt(int row, int column) {
        OracleSecAccessMonitorCondition rule = (OracleSecAccessMonitorCondition) rules.get(row);
        switch (column) {
          case 0:
            return rule.getIpAddr();
          case 1:
            return rule.getDbUser();
            // case 2:
            // return rule.getWeekDesc() + rule.getTimeDesc();
            // case 3:
            // return rule.getOsUser();
          case 2:
            return rule.getProgram();
          default:
            return new Object();
        }
      }

      public void addRule(OracleSecAccessMonitorCondition rule) {
        rules.add(rule);
        int row = rules.size();
        fireTableRowsInserted(row, row);
      }

      public OracleSecAccessMonitorCondition updateRule(int row, OracleSecAccessMonitorCondition cond) {
        OracleSecAccessMonitorCondition tmp = (OracleSecAccessMonitorCondition) rules.remove(row);
        rules.add(row, cond);
        if (tmp != null) {
          fireTableRowsUpdated(row, row);
        } else {
          fireTableRowsInserted(row, row);
        }
        return tmp;
      }

      public OracleSecAccessMonitorCondition getRule(int row) {
        if (row < 0 || row >= rules.size()) {
          return null;
        } else {
          return (OracleSecAccessMonitorCondition) rules.get(row);
        }
      }

      public OracleSecAccessMonitorCondition removeRule(int row) {
        OracleSecAccessMonitorCondition rule = (OracleSecAccessMonitorCondition) rules.remove(row);
        if (rule != null) {
          fireTableRowsDeleted(row, row);
        }
        return rule;
      }

      /**
       * 向表格模型添加数据
       * @param rules
       */
      public void setContent(List content) {
        if (rules != null) {
          rules.clear();
        }
        this.rules = content;
        fireTableDataChanged();
      }

    }

    void jBtnAdd_actionPerformed(ActionEvent e) {
      RuleEditDlg dlg = RuleEditDlg.createDialog(this, "增加Oracle数据库安全访问规则");
      dlg.setVisible(true);
      // 是否按确定
      if (dlg.isOk) {
        model.addRule(dlg.getCondition());
      }
    }

    void jBtnMod_actionPerformed(ActionEvent e) {
      int row = jTabRules.getSelectedRow();
      OracleSecAccessMonitorCondition cond = model.getRule(row);
      if (cond == null) {
        JOptionPane.showMessageDialog(this, "请先选择要修改的访问规则！");
        return;
      }
      RuleEditDlg dlg = RuleEditDlg.createDialog(this, "修改Oracle数据库安全访问规则", cond);
      dlg.setVisible(true);
      // 是否按确定
      if (dlg.isOk) {
        model.updateRule(row, dlg.getCondition());
      }
    }

    void jBtnDel_actionPerformed(ActionEvent e) {
      int row = jTabRules.getSelectedRow();
      OracleSecAccessMonitorCondition cond = model.getRule(row);
      if (cond == null) {
        JOptionPane.showMessageDialog(this, "请先选择要删除的访问规则！");
        return;
      }
      int opt = JOptionPane.showConfirmDialog(this, "您真的要删除远程主机为:(" + cond.getIpAddr() + ")的规则吗？", "提问",
          JOptionPane.OK_CANCEL_OPTION);
      if (opt != JOptionPane.OK_OPTION) {
        return;
      }
      model.removeRule(row);
    }

    private void jTabRules_mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        modRule();
      }
    }

    public void modRule() {
      int row = jTabRules.getSelectedRow();
      OracleSecAccessMonitorCondition cond = model.getRule(row);
      if (cond == null) {
        JOptionPane.showMessageDialog(this, "请先选择要修改的规则！");
        return;
      }

      RuleEditDlg dlg = RuleEditDlg.createDialog(this, "修改Oracle数据库安全访问规则", cond);
      dlg.setVisible(true);
      // 是否按确定
      if (dlg.isOk) {
        model.updateRule(row, dlg.getCondition());
      }
    }

  }

  class SecAccessParamPanel_jBtnDel_actionAdapter implements java.awt.event.ActionListener {
    SecAccessParamPanel adaptee;

    SecAccessParamPanel_jBtnDel_actionAdapter(SecAccessParamPanel adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
      adaptee.jBtnDel_actionPerformed(e);
    }
  }

  class SecAccessParamPanel_jBtnAdd_actionAdapter implements java.awt.event.ActionListener {
    SecAccessParamPanel adaptee;

    SecAccessParamPanel_jBtnAdd_actionAdapter(SecAccessParamPanel adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
      adaptee.jBtnAdd_actionPerformed(e);
    }
  }

  class SecAccessParamPanel_jBtnMod_actionAdapter implements java.awt.event.ActionListener {
    SecAccessParamPanel adaptee;

    SecAccessParamPanel_jBtnMod_actionAdapter(SecAccessParamPanel adaptee) {
      this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
      adaptee.jBtnMod_actionPerformed(e);
    }
  }


  private SecAccessParamPanel panel = new SecAccessParamPanel();
  private MonitorConfigContext data;

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public boolean getData() {
		if (!panel.verify())
			return false;
		
		data.getTask().setParameterObject(panel.param);		
		return true;
	}

	@Override
	public void setData(MonitorConfigContext data) {
		this.data = data;
		panel.param = data.getTask().getParameterObject(OracleSecAccessParameter.class);
		if (panel.param == null)
			panel.param = new OracleSecAccessParameter();
		panel.resetSecAccessInfo();
	}

	@Override
	public void setMethod(MonitorMethod method) {
	}

}
