package com.broada.carrier.monitor.common.swing.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public abstract class BaseTableCellRenderer implements TableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component component = getComponent(table, value);
		if (isSelected) {
			component.setForeground(table.getSelectionForeground());
			component.setBackground(table.getSelectionBackground());
		} else {
			component.setForeground(table.getForeground());
			component.setBackground(table.getBackground());
		}
		return component;
	}

	protected abstract Component getComponent(JTable table, Object value);
}
