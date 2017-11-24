package com.broada.carrier.monitor.common.swing.table;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

public class BooleanTableCellEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;
	private JCheckBox com = new JCheckBox();
	
	public BooleanTableCellEditor() {
		com.setHorizontalAlignment(SwingConstants.CENTER);
	}	

	@Override
	public Object getCellEditorValue() {
		return com.isSelected();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		com.setSelected(value == null ? false : (Boolean) value);	
		return com;
	}
}
