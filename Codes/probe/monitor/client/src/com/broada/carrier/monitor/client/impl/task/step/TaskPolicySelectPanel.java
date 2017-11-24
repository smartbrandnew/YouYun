package com.broada.carrier.monitor.client.impl.task.step;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.policy.PolicyEditPanel;
import com.broada.carrier.monitor.client.impl.task.TaskEditStepPanel;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.common.util.Unit;
import com.broada.swing.util.WinUtil;

public class TaskPolicySelectPanel extends TaskEditStepPanel {
	private static final long serialVersionUID = 1L;
	private static String lastSelectPolicyCode;
	private PolicyTableModel tableModel = new PolicyTableModel();
	private BaseTable table = new BaseTable(tableModel);
	private MonitorTask task;

	public TaskPolicySelectPanel() {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "说明", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setPreferredSize(new Dimension(10, 65));
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		JTextArea textArea = new JTextArea();
		textArea.setText("监测策略决定监测任务的运行频率。");
		textArea.setEditable(false);
		textArea.setBackground(UIManager.getColor("Panel.background"));
		panel.add(textArea, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.NORTH);

		JButton btnCreate = new JButton("添加");
		btnCreate.addActionListener(new BtnCreateActionListener());
		panel_1.add(btnCreate);

		JButton btnEdit = new JButton("修改");
		btnEdit.addActionListener(new BtnEditActionListener());
		panel_1.add(btnEdit);

		JButton btnDelete = new JButton("删除");
		btnDelete.addActionListener(new BtnDeleteActionListener());
		panel_1.add(btnDelete);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		table.addMouseListener(new TableMouseListener());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollPane.setViewportView(table);
	}

	@Override
	public boolean getData() {
		MonitorPolicy policy = getSelected();
		if (policy == null)
			return false;

		task.setPolicyCode(policy.getCode());
		lastSelectPolicyCode = policy.getCode();
		return true;
	}

	@Override
	public void setData(MonitorConfigContext context) {
		if (this.task == null) {
			this.task = context.getTask();
			refresh();		
		}
	}

	private void refresh() {
		tableModel.setRows(ServerContext.getPolicyService().getPolicies());
		int index = getIndex(tableModel.getRows(), task.getPolicyCode());
		table.getSelectionModel().setSelectionInterval(index, index);
	}

	private int getIndex(List<MonitorPolicy> rows, String policyCode) {
		if (policyCode == null) {
			policyCode = lastSelectPolicyCode;
			if (policyCode == null && rows.size() > 0)
				policyCode = rows.get(0).getCode();
		}
		
		for (int i = 0; i < rows.size(); i++) {
			MonitorPolicy policy = rows.get(i);
			if (policy.getCode().equalsIgnoreCase(policyCode))
				return i;
		}
		return -1;
	}

	private static class SecondTableCellRender extends TextTableCellRenderer {
		@Override
		protected String getText(Object value) {
			return Unit.second.formatPrefer(((Number) value).intValue());
		}
	}

	private static class PolicyTableModel extends BeanTableModel<MonitorPolicy> {
		private static final long serialVersionUID = 1L;
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("index", "序号", 45, new TextTableCellRenderer()),
				new BaseTableColumn("name", "名称"),
				new BaseTableColumn("interval", "正常周期", new SecondTableCellRender()),
				new BaseTableColumn("errorInterval", "异常周期", new SecondTableCellRender()),
				new BaseTableColumn("descr", "说明", 200),
		};

		public PolicyTableModel() {
			super(columns);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return rowIndex + 1;
			else
				return super.getValueAt(rowIndex, columnIndex);
		}
	}

	private class BtnCreateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {			
			MonitorPolicy policy = PolicyEditPanel.show(WinUtil.getWindowForComponent(TaskPolicySelectPanel.this));
			if (policy != null)
				tableModel.addRow(policy);
		}
	}
	
	private MonitorPolicy getSelected() {
		MonitorPolicy mp = tableModel.checkSelectedRow(table);
		if(mp == null){
			JOptionPane.showMessageDialog(this, "必须选择一个监测策略");
			return null;
		}
		return mp;
	}
	
	private void editSelected() {
		MonitorPolicy policy = getSelected();
		if (policy != null) {
			MonitorPolicy newPolicy = PolicyEditPanel.show(WinUtil.getWindowForComponent(TaskPolicySelectPanel.this), policy);
			if (newPolicy != null) {
				policy.set(newPolicy);
				tableModel.fireTableDataChanged();
			}
		}
	}

	private class BtnEditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {			
			editSelected();
		}
	}

	private class BtnDeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorPolicy policy = getSelected();
			if (policy != null) {
				if (policy.retDefault()) {
					JOptionPane.showMessageDialog(TaskPolicySelectPanel.this, "默认监测策略不允许删除");					
					return;
				}					
				Page<MonitorTask> page = ServerContext.getTaskService().getTasksByPolicyCode(PageNo.ONE, policy.getCode());
				if (page.getRows().length > 0) {
					if (JOptionPane.showConfirmDialog(TaskPolicySelectPanel.this, "此监测策略正在使用中，删除它将导致监测任务使用默认监测策略，是否确认？", "操作确认", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
						return;
				}
				try {
					ServerContext.getPolicyService().deletePolicy(policy.getCode());
					tableModel.removeRow(policy);				
				} catch (Throwable err) {
					ErrorDlg.show("删除策略失败", err);
				}
			}
		}
	}

	private class TableMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) 
				editSelected();			
		}
	}
}
