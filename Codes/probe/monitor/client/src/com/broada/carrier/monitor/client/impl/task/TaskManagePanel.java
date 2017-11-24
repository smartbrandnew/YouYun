package com.broada.carrier.monitor.client.impl.task;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.cache.CacheReloadListener;
import com.broada.carrier.monitor.client.impl.cache.ClientCache;
import com.broada.carrier.monitor.client.impl.common.ActionTasksDisable;
import com.broada.carrier.monitor.client.impl.common.ActionTasksDispatch;
import com.broada.carrier.monitor.client.impl.common.ActionTasksEnable;
import com.broada.carrier.monitor.client.impl.common.MonitorRecordTableCellRenderer;
import com.broada.carrier.monitor.client.impl.target.TargetTextTableCellRenderer;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.swing.action.Action;
import com.broada.carrier.monitor.common.swing.action.ActionPopupMenu;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.BeanTableRowFilter;
import com.broada.carrier.monitor.common.swing.table.BooleanTableCellRenderer;
import com.broada.carrier.monitor.common.swing.table.DateTableCellRenderer;
import com.broada.carrier.monitor.common.swing.table.TableSelectedGetter;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetAuditState;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.error.TargetNotExistsException;
import com.broada.carrier.monitor.server.api.event.RecordChangedEvent;
import com.broada.carrier.monitor.server.api.event.TaskChangedEvent;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.swing.util.WinUtil;

public class TaskManagePanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(TaskManagePanel.class);
	private static final long serialVersionUID = 1L;
	private TaskTableModel tableModel = new TaskTableModel();
	private BaseTable table = new BaseTable(tableModel);
	private JButton btnCreate;
	private JButton btnEdit;
	private JButton btnDelete;
	private Object tasksScope;
	private JLabel lblNewLabel;
	private JCheckBox autoRefreshCkBox;
	private JTextField txtFilter;
	private int refreshInterval = 5 * 60 * 1000;
	private long lastRefreshInterval = 0;
	private Map<String, TaskTableRow> cacheRows = ClientCache.getCacheRows();

	public TaskManagePanel() {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		add(panel, BorderLayout.NORTH);

		btnCreate = new JButton("添加");
		btnCreate.addActionListener(new BtnCreateActionListener());
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		panel.add(btnCreate);

		btnEdit = new JButton("修改");
		btnEdit.addActionListener(new BtnEditActionListener());
		panel.add(btnEdit);

		btnDelete = new JButton("删除");
		btnDelete.addActionListener(new BtnDeleteActionListener());
		panel.add(btnDelete);

		lblNewLabel = new JLabel("过滤：");
		panel.add(lblNewLabel);
		
		txtFilter = new JTextField();
		txtFilter.addKeyListener(new TxtFilterKeyListener());
		txtFilter.setPreferredSize(new Dimension(120, (int) txtFilter.getPreferredSize().getHeight()));
		panel.add(txtFilter);
		txtFilter.setColumns(10);
		
		autoRefreshCkBox = new JCheckBox("自动刷新排序结果");
		autoRefreshCkBox.setSelected(true);
		panel.add(autoRefreshCkBox);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		ActionPopupMenu popProbe = new ActionPopupMenu(new TableSelectedGetter(table, tableModel), new Action[] {
				new ActionTasksDispatch(),
				new ActionTasksEnable(),
				new ActionTasksDisable(),
		});
		popProbe.addPopup(table);
		popProbe.addPopup(scrollPane);
		table.addMouseListener(new TableMouseListener());

		scrollPane.setViewportView(table);

		if (ServerContext.isConnected()) {
			refresh();
			ServerContext.registerObjectChangedListener(new TaskChangedEventListener());
		}
		
		ClientCache.registerCacheReload(new CacheReloadListener() {
			
			@Override
			public void refresh() {
				TaskManagePanel.this.refresh();
			}
		});
	}

	private class TaskChangedEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (event instanceof TaskChangedEvent)
				processTaskEvent((TaskChangedEvent) event);
			else if (event instanceof RecordChangedEvent)
				processRecordEvent((RecordChangedEvent) event);
		}
	}

	private synchronized void refresh() {
		MonitorTask[] items;
		if (tasksScope == null)
			items = new MonitorTask[0];
		else if (tasksScope instanceof String && tasksScope.equals("root"))
			items = ServerContext.getTaskService().getTasks(PageNo.ALL, true).getRows();
		else if (tasksScope instanceof MonitorProbe)
			items = ServerContext.getTaskService().getTasksByProbeId(PageNo.ALL, ((MonitorProbe) tasksScope).getId(), true)
					.getRows();
		else if (tasksScope instanceof MonitorNode)
			items = ServerContext.getTaskService().getTasksByNodeId(((MonitorNode) tasksScope).getId());
		else if (tasksScope instanceof MonitorResource)
			items = ServerContext.getTaskService().getTasksByResourceId(((MonitorResource) tasksScope).getId());
		else
			throw new IllegalArgumentException("未知的任务范围：" + tasksScope);

		List<TaskTableRow> tasks = new ArrayList<TaskTableRow>(items.length);
		TaskTableRow row = null;
		for (MonitorTask task : items) {
			try {
				row = new TaskTableRow(task);
				if(cacheRows.get(task.getId()) != null){
					tasks.add(cacheRows.get(task.getId()));
				}else{
					cacheRows.put(task.getId(), row);
					tasks.add(row);
				}
			} catch (TargetNotExistsException e) {
				ErrorUtil.warn(logger, "监测任务依赖监测项已不存在：" + task, e);
				continue;
			}
		}

		List<TaskTableRow> selected = tableModel.getSelectedRows(table);
		tableModel.setRows(tasks);
		tableModel.setSelectedRows(table, selected);
	}
	
	private synchronized void processRecordEvent(RecordChangedEvent event) {
		long now = System.currentTimeMillis();
		if (now - lastRefreshInterval > refreshInterval) {
			lastRefreshInterval = now;
			refresh();
			return;
		}
		TaskTableRow task = new TaskTableRow(new MonitorTask(event.getNewObject().getTaskId()), null, null,
				event.getNewObject());
		task = tableModel.getRow(task);
		if (task != null) {
			task.setRecord(event.getNewObject());
			List<TaskTableRow> selected = tableModel.getSelectedRows(table);
			if(autoRefreshCkBox.isSelected()){
				tableModel.fireTableDataChanged();
			}else{
				int index = tableModel.getRowIndex(task);
				tableModel.fireTableRowsUpdated(index, index);
			}
			tableModel.setSelectedRows(table, selected);
		}
	}

	private void processTaskEvent(TaskChangedEvent event) {
		refresh();
	}

	private class BtnCreateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (tasksScope != null && tasksScope instanceof MonitorNode) {
				MonitorNode node = (MonitorNode) tasksScope;
				if (node.getProbeId() == 0) {
					JOptionPane.showMessageDialog(TaskManagePanel.this, "监测节点还未配置监测探针，请先配置探针！");
					return;
				}
				TaskEditWindow.show(WinUtil.getWindowForComponent(TaskManagePanel.this), node);
			} else if (tasksScope != null && tasksScope instanceof MonitorResource) {
				TaskEditWindow.show(WinUtil.getWindowForComponent(TaskManagePanel.this), (MonitorResource) tasksScope);
			} else
				JOptionPane.showMessageDialog(TaskManagePanel.this, "必须选择一个监测节点或监测资源");
		}
	}

	private void editCurrent() {
		List<TaskTableRow> tasks = tableModel.checkSelectedRows(table);
		TaskEditWindow.show(WinUtil.getWindowForComponent(TaskManagePanel.this), tasks.get(0)
				.getTask());
	}

	private class BtnEditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			editCurrent();
		}
	}

	private class BtnDeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			List<TaskTableRow> tasks = tableModel.checkSelectedRows(table);
			if (JOptionPane.showConfirmDialog(TaskManagePanel.this, "请确认是否删除监测任务？", "操作确认", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;

			for (TaskTableRow task : tasks)
				ServerContext.getTaskService().deleteTask(task.getTask().getId());
		}
	}

	private class TxtFilterKeyListener extends KeyAdapter {
		private String lastKey;

		@Override
		public void keyReleased(KeyEvent e) {
			if (10 != e.getKeyCode()) 
				return;
			lastKey = txtFilter.getText();
			if (lastKey.isEmpty())
				tableModel.setFilter(null);
			else 
				tableModel.setFilter(new MonitorTaskFilter(lastKey));
		}
	}

	private class TableMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				editCurrent();
			}
		}
	}

	public static class MonitorTaskFilter implements BeanTableRowFilter<TaskTableRow> {
		private String key;

		public MonitorTaskFilter(String key) {
			this.key = key;
		}

		@Override
		public boolean match(TaskTableRow row) {
			return match(row.getName(), key)
					|| match(row.getNodeIp(), key)
					|| match(row.getNodeName(), key)
					|| match(row.getResourceName(), key)
					|| match(row.getState().getDisplayName(), key);
		}

		private static boolean match(String value, String key) {
			if (value == null)
				return false;
			return value.toUpperCase().contains(key.toUpperCase());
		}
	}

	private static class TaskTableModel extends BeanTableModel<TaskTableRow> {
		private static final long serialVersionUID = 1L;
		private static final TargetTextTableCellRenderer targetCellRender = new TargetTextTableCellRenderer();
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("index", "序号", 35, new TextTableCellRenderer()),
				new BaseTableColumn("enabled", "激活", 35, new BooleanTableCellRenderer()),
				new BaseTableColumn("nodeIp", "IP", 90, targetCellRender),
				new BaseTableColumn("nodeName", "节点", 150, targetCellRender),
				new BaseTableColumn("resourceName", "资源", 150, targetCellRender),
				new BaseTableColumn("name", "名称", 150),
				new BaseTableColumn("lastRunTime", "最后监测时间", 130, new DateTableCellRenderer()),
				new BaseTableColumn("record", "状态", 60, new MonitorRecordTableCellRenderer()),
		};

		public TaskTableModel() {
			super(columns);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object value;
			if (columnIndex == 0)
				value = rowIndex + 1;
			else
				value = super.getValueAt(rowIndex, columnIndex);

			if (value != null) {
				if (columnIndex >= 2 && columnIndex <= 3) {
					TaskTableRow row = getRow(rowIndex);
					if (row.getNode().getAuditState() == MonitorTargetAuditState.AUDITING)
						return TargetTextTableCellRenderer.createText(value);
				} else if (columnIndex == 4) {
					TaskTableRow row = getRow(rowIndex);
					if (row.getResource().getAuditState() == MonitorTargetAuditState.AUDITING)
						return TargetTextTableCellRenderer.createText(value);
				}
			}

			return value;
		}
	}
	
	public void setTasksScope(Object tasksScope) {
		this.tasksScope = tasksScope;
		refresh();
	}

	public MonitorTask[] getSelectedTasks() {
		List<TaskTableRow> rows = tableModel.getSelectedRows(table);
		if (rows == null)
			return new MonitorTask[0];

		MonitorTask[] result = new MonitorTask[rows.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = rows.get(i).getTask();
		}
		return result;
	}
}
