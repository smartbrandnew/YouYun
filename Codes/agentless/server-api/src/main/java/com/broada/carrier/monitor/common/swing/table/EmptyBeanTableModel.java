package com.broada.carrier.monitor.common.swing.table;

public class EmptyBeanTableModel extends BeanTableModel<Object> {
	private static final long serialVersionUID = 1L;

	public EmptyBeanTableModel() {
		super(new BaseTableColumn[0]);
	}

	@Override
	public int getRowCount() {
		return 0;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return null;
	}
}