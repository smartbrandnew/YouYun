package com.broada.carrier.monitor.client.impl.common;

import javax.swing.JLabel;
import javax.swing.JTable;

import com.broada.carrier.monitor.server.api.entity.MonitorRecord;

public class MonitorRecordTableCellRenderer extends MonitorStateTableCellRenderer {

	@Override
	protected JLabel getComponent(JTable table, Object value) {
		MonitorRecord record = (MonitorRecord) value;		
		if (record != null) {
			JLabel com = super.getComponent(table, record.getState());
			com.setToolTipText(record.getMessage());
			return com;
		} else
			return new JLabel();
	}
}