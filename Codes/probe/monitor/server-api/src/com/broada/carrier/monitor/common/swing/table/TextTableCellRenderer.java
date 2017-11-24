package com.broada.carrier.monitor.common.swing.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class TextTableCellRenderer extends BaseTableCellRenderer {
	private JLabel component = new JLabel();
	
	public TextTableCellRenderer() {
		this(SwingConstants.CENTER);
	}
	
	public TextTableCellRenderer(int horizontalAlignment) {
		component.setHorizontalAlignment(horizontalAlignment);
		component.setOpaque(true);
	}
	
	@Override
	protected Component getComponent(JTable table, Object value) {			
		component.setText(getText(value));
		component.setFont(table.getFont());
		component.setToolTipText(component.getText());
		return component;
	}
	
	protected JLabel getComponent() {
		return component;
	}

	protected String getText(Object value) {
		return value == null ? null : value.toString();
	}
}
