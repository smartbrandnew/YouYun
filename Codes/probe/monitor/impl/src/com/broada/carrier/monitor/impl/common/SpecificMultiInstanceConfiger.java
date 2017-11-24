package com.broada.carrier.monitor.impl.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.BooleanTableCellEditor;
import com.broada.carrier.monitor.common.swing.table.BooleanTableCellRenderer;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.impl.common.ui.IndicatorTableCellRenderer;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.component.utils.lang.ThreadUtil;
import com.broada.component.utils.text.DateUtil;

public class SpecificMultiInstanceConfiger extends SpecificMonitorConfig {
	private static final long serialVersionUID = 1L;
	private static final BaseTableColumn COLUMN_INDEX = new BaseTableColumn("index", "序号", 30,
			new TextTableCellRenderer());
	private static final BaseTableColumn COLUMN_SELECTED = new BaseTableColumn("selected", "监测", 0, 30, 0,
			new BooleanTableCellRenderer(), new BooleanTableCellEditor());
	private static final BaseTableColumn COLUMN_NAME = new BaseTableColumn("name", "名称", 100);
	private BaseTable table = new BaseTable();
	private ItemTableModel tableModel;
	private CollectResult result;

	public SpecificMultiInstanceConfiger() {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		JButton[] buttons = getButtons();
		if (buttons != null) {
			for (JButton btn : buttons)
				panel.add(btn);
		}

		JButton btnRefresh = new JButton("刷新");
		btnRefresh.addActionListener(new BtnRefreshActionListener());
		panel.add(btnRefresh);

		JButton btnSelectAll = new JButton("全选");
		btnSelectAll.addActionListener(new BtnSelectAllActionListener());
		panel.add(btnSelectAll);

		JButton btnUnselectAll = new JButton("清除");
		btnUnselectAll.addActionListener(new BtnUnselectAllActionListener());
		panel.add(btnUnselectAll);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(true);

		scrollPane.setViewportView(table);
	}

	private class CollectResultDialog extends JDialog {

		private static final long serialVersionUID = 1L;
		private JLabel lblMessage = new JLabel("");
		private JProgressBar pbrProgress = new JProgressBar();
		private JButton btnCancel = new JButton("取消");
		private JButton btnFinish = new JButton("完成");
		private String nodeId;
		private String taskId;
		private Thread thread;
		private boolean cancelSign = false;

		public boolean isCancelSign() {
			return cancelSign;
		}

		public CollectResultDialog(String nodeId, String taskId) {
			this.taskId = taskId;
			this.nodeId = nodeId;
			setTitle("监测采集进度");
			setModal(true);
			getContentPane().setLayout(null);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setPreferredSize(new Dimension(595, 228));

			btnCancel.setBounds(340, 154, 93, 23);
			btnCancel.addActionListener(new BtnCancelActionListener());
			getContentPane().add(btnCancel);

			btnFinish.setBounds(468, 154, 98, 23);
			btnFinish.addActionListener(new BtnFinishActionListener());
			getContentPane().add(btnFinish);

			JLabel lblNewLabel = new JLabel("状态：");
			lblNewLabel.setBounds(10, 25, 54, 15);
			getContentPane().add(lblNewLabel);
			this.setIconImage(IconLibrary.getDefault().getImage(
					"resources/images/app.png"));
			pbrProgress.setBounds(10, 66, 551, 23);
			pbrProgress.setMinimum(0);
			pbrProgress.setMaximum(100);
			lblMessage.setBounds(52, 25, 509, 15);
			getContentPane().add(pbrProgress);
			getContentPane().add(lblMessage);
			pack();
			this.setLocationRelativeTo(null);
			thread = ThreadUtil.createThread(new RefreshThread());
			thread.start();
			setVisible(true);
		}

		public class RefreshThread implements Runnable {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						break;
					}
					result = getCollectResult(nodeId, taskId);
					lblMessage.setText(result.getMessage());
					pbrProgress.setValue(result.getProgress());
					CollectResultDialog.this.repaint();
					if (cancelSign)
						break;
					if (result.getProgress() >= 100)
						break;
				}
			}
		}

		@Override
		public void dispose() {
			if (thread != null)
				thread.interrupt();
			super.dispose();
		}

		private class BtnCancelActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				cancelSign = true;
				dispose();
			}
		}

		private class BtnFinishActionListener implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		}
	}

	protected JButton[] getButtons() {
		return null;
	}

	private class BtnRefreshActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			stopTableEditor();
			refresh();
		}
	}

	@Override
	public void refresh() {
		try {
			CollectTaskSign taskSign = commit();
			String taskId = taskSign.getTaskId();
			String nodeId = taskSign.getNodeId();
			result = getCollectResult(nodeId, taskId);
			if (result.getProgress() != 100) {
				CollectResultDialog dialog = new CollectResultDialog(nodeId, taskId);
				if (dialog.isCancelSign()) {
					cancelCollect(nodeId, taskId);
				}
			}
			if (result == null)
				getTableModel(result).setRows(new MonitorResultRow[0]);
			else {
				if (result.getState() == CollectMonitorState.FAILED)
					ErrorDlg.show(result.getMessage());
				else if (result.getRows() != null) {
					if (getContext().getInstanceCount() == 0) {
						for (MonitorResultRow row : result.getRows()) {
							if (row.isInstMonitor())
								getContext().addInstance(row.retInstance());
						}
					}
					getTableModel(result).setRows(result.getRows());
				} else {
					getTableModel(result).getRows().clear();
					getTableModel(result).fireTableDataChanged();
				}
			}
		} catch (Throwable e) {
			ErrorDlg.show(e);
		} finally {
			WinUtil.switchIdle();
		}
	}

	protected String[] getItemCodes() {
		return null;
	}

	private ItemTableModel getTableModel(CollectResult result) {
		if (isModelMatch(result))
			return tableModel;

		Map<String, BaseTableColumn> columns = new LinkedHashMap<String, BaseTableColumn>();
		String[] itemCodes = getItemCodes();
		if (itemCodes != null) {
			for (String itemCode : itemCodes) {
				if (columns.containsKey(itemCode))
					continue;
				columns.put(itemCode, createColumn(itemCode));
			}
		} else if (result != null && result.getRows() != null) {
			for (MonitorResultRow row : result.getRows()) {
				for (Entry<String, Object> entry : row.entrySet()) {
					if (MonitorResultRow.isIndicator(entry.getKey())) {
						if (columns.containsKey(entry.getKey()))
							continue;
						columns.put(entry.getKey(), createColumn(entry.getKey()));
					}
				}
			}
		}

		BaseTableColumn[] temp = new BaseTableColumn[3 + columns.size()];
		int index = 0;
		temp[index++] = COLUMN_INDEX;
		temp[index++] = COLUMN_SELECTED;
		temp[index++] = createColumnForName();
		for (BaseTableColumn column : columns.values()) {
			temp[index++] = column;
		}

		tableModel = new ItemTableModel(temp);
		table.setModel(tableModel);
		return tableModel;
	}

	protected BaseTableColumn createColumnForName() {
		return COLUMN_NAME;
	}

	//TODO 临时使用这种方法
	protected MonitorItem checkItem(String itemCode) {
		return ServerUtil.checkItem(getServerFactory().getTypeService(), itemCode);
	}

	private BaseTableColumn createColumn(String itemCode) {
		MonitorItem item = checkItem(itemCode);
		String name;
		if (item.getUnit() == null || item.getUnit().isEmpty())
			name = item.getName();
		else
			name = item.getName() + "(" + item.getUnit() + ")";
		return new BaseTableColumn(itemCode, name, 0, 100, 0, new IndicatorTableCellRenderer());
	}

	private boolean isModelMatch(CollectResult result) {
		if (tableModel == null)
			return false;

		if (result != null && result.getRows() != null) {
			for (MonitorResultRow row : result.getRows()) {
				for (Entry<String, Object> entry : row.entrySet()) {
					if (MonitorResultRow.isIndicator(entry.getKey())) {
						if (tableModel.getColumn(entry.getKey()) == null)
							return false;
					}
				}
			}
		}

		return true;
	}

	private class ItemTableModel extends BeanTableModel<MonitorResultRow> {
		private static final long serialVersionUID = 1L;

		public ItemTableModel(BaseTableColumn[] columns) {
			super(columns);
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 1 || columnIndex == 2;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			MonitorResultRow row = getRows().get(rowIndex);
			if (columnIndex == 1) {
				Boolean selected = (Boolean) aValue;
				if (selected)
					getContext().addInstance(row.retInstance());
				else
					getContext().removeInstance(row.getInstCode());
			} else if (columnIndex == 2) {
				MonitorInstance inst = getContext().getInstance(row.getInstCode());
				if (inst != null)
					inst.setName((String) aValue);
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return rowIndex + 1;

			MonitorResultRow row = getRows().get(rowIndex);
			if (columnIndex == 1 || columnIndex == 2) {
				MonitorInstance inst = getContext().getInstance(row.getInstCode());
				if (columnIndex == 1)
					return inst != null;
				else
					return inst == null ? row.getInstName() : inst.getName();
			}

			Object value = row.getIndicator(getColumn(columnIndex).getCode());
			if (value instanceof Date)
				value = DateUtil.format((Date) value);
			return value;
		}
	}

	public static class InstanceVO {
		private boolean selected;
		private MonitorResultRow row;

		public InstanceVO(MonitorResultRow row, boolean selected) {
			this.row = row;
			this.selected = selected;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public MonitorResultRow getRow() {
			return row;
		}
	}

	private class BtnSelectAllActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (tableModel != null) {
				stopTableEditor();
				for (MonitorResultRow row : tableModel.getRows()) {
					getContext().addInstance(row.retInstance());
				}
				tableModel.fireTableDataChanged();
			}
		}
	}

	private class BtnUnselectAllActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			stopTableEditor();
			getContext().removeInstanceAll();
			if (tableModel != null)
				tableModel.fireTableDataChanged();
		}
	}

	private void stopTableEditor() {
		if (table.getCellEditor() != null)
			table.getCellEditor().stopCellEditing();
	}

	@Override
	public boolean getData() {
		stopTableEditor();

		if (getContext().getInstanceCount() <= 0) {
			JOptionPane.showMessageDialog(this, "请选择至少一个监测实例");
			return false;
		}

		return super.getData();
	}
}
