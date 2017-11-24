package com.broada.carrier.monitor.common.swing.table;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class BooleanTableCellRenderer extends BaseTableCellRenderer {
	private JCheckBox com = new JCheckBox();
	
	public BooleanTableCellRenderer() {
		com.setHorizontalAlignment(SwingConstants.CENTER);
	}

	@Override
	protected Component getComponent(JTable table, Object value) {
		com.setSelected(value == null ? false : (Boolean) value);		
		return com;
	}

}