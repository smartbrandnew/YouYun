package com.broada.carrier.monitor.client.impl.target;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;

import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;

public class TargetTextTableCellRenderer extends TextTableCellRenderer {
	private static Font AUDITING_FONT;

	@Override
	protected Component getComponent(JTable table, Object value) {
		Component com = super.getComponent(table, value);
		if (value instanceof AuditingText) 		
			getComponent().setFont(getAuditingFont(table));			
		else
			getComponent().setFont(table.getFont());
		return com;
	}

	private static Font getAuditingFont(JTable table) {
		if (AUDITING_FONT == null) {			
			AUDITING_FONT = table.getFont().deriveFont(Font.ITALIC);
		}
		return AUDITING_FONT;
	}
	
	public static class AuditingText {
		private String text;

		public AuditingText(Object text) {			
			this.text = text == null ? null : text.toString();
		}

		@Override
		public String toString() {
			return text;
		}		
	}

	public static Object createText(Object value) {
		return new AuditingText(value);
	}
}