package com.broada.carrier.monitor.common.swing.table;

import javax.swing.JTable;

import com.broada.carrier.monitor.common.swing.action.ActionTargetGetter;

public class TableSelectedGetter implements ActionTargetGetter {
	private JTable table;
	private BeanTableModel<?> tableModel;

	public TableSelectedGetter(BaseTable table, BeanTableModel<?> tableModel) {		
		this.table = table;
		this.tableModel = tableModel;
	}

	@Override
	public Object getTarget() {
		int count = table.getSelectedRowCount();
		if (count <= 0)
			return null;
		else if (count == 1)
			return tableModel.getSelectedRow(table);
		else
			return tableModel.getSelectedRows(table);
	}
}
