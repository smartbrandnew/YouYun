package com.broada.carrier.monitor.common.swing.table;

import java.awt.Component;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.broada.component.utils.text.DateUtil;

public class DateTableCellRenderer extends BaseTableCellRenderer {
	private JLabel com = new JLabel();
	
	public DateTableCellRenderer() {
		com.setHorizontalAlignment(SwingConstants.CENTER);
		com.setOpaque(true);
	}
	
	@Override
	protected Component getComponent(JTable table, Object value) {			
		if (value == null)
			com.setText("");
		else if (value instanceof Date)
			com.setText(DateUtil.format((Date)value));
		else if (value instanceof Long)
			com.setText(DateUtil.format(new Date((Long)value)));
		else
			throw new IllegalArgumentException("不支持的日期数据类型：" + value);
		com.setFont(table.getFont());
		return com;
	}
}