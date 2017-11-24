package com.broada.carrier.monitor.impl.common;


import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.common.swing.table.*;
import com.broada.carrier.monitor.impl.common.ui.IndicatorTableCellRenderer;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.*;
import com.broada.component.utils.text.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MultiInstanceConfiger extends BaseMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private static final BaseTableColumn COLUMN_INDEX = new BaseTableColumn("index", "序号", 30, new TextTableCellRenderer());
	private static final BaseTableColumn COLUMN_SELECTED = new BaseTableColumn("selected", "监测", 0, 30, 0,
			new BooleanTableCellRenderer(), new BooleanTableCellEditor());
	private static final BaseTableColumn COLUMN_NAME = new BaseTableColumn("name", "名称", 100);		
	private BaseTable table = new BaseTable();
	private ItemTableModel tableModel;

	public MultiInstanceConfiger() {
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
		WinUtil.switchBusy();
		try {
			MonitorResult result = (MonitorResult) collect();
			if (result == null) 
				getTableModel(result).setRows(new MonitorResultRow[0]);
			else {
				if (result.getState() == MonitorState.FAILED)
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
	
	private ItemTableModel getTableModel(MonitorResult result) {
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

	private boolean isModelMatch(MonitorResult result) {
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
