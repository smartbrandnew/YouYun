package com.broada.carrier.monitor.impl.common.ui;

import javax.swing.SwingConstants;

import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;

public class IndicatorTableCellRenderer extends TextTableCellRenderer {
	@Override
	protected String getText(Object value) {
		if (value == null)
			return "";
		if (value instanceof Double || value instanceof Float) {
			getComponent().setHorizontalAlignment(SwingConstants.RIGHT);
			Number dou = (Number) value;
			long num = dou.longValue();
			if (dou.doubleValue() > num)
				return String.format("%.1f", dou);
			else
				return Long.toString(num);
		}
		if (value instanceof Number) {
			getComponent().setHorizontalAlignment(SwingConstants.RIGHT);
			return value.toString();
		} else if  (value instanceof Boolean) {
			getComponent().setHorizontalAlignment(SwingConstants.CENTER);
			return (Boolean) value ? "是" : "否";
		} else {
			getComponent().setHorizontalAlignment(SwingConstants.LEFT);
			return value.toString();
		}
	}
}