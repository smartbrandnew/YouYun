package com.broada.carrier.monitor.client.impl.common;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.common.swing.table.BaseTableCellRenderer;
import com.broada.carrier.monitor.server.api.entity.MonitorState;

public class MonitorStateTableCellRenderer extends BaseTableCellRenderer {
	private JLabel com = new JLabel();

	public MonitorStateTableCellRenderer() {
		com.setHorizontalAlignment(SwingConstants.CENTER);
		com.setOpaque(true);
	}

	@Override
	protected JLabel getComponent(JTable table, Object value) {
		if (value == null) {
			com.setText("");
			com.setIcon(null);
		} else {
			MonitorState state = (MonitorState) value;
			com.setText(state.getDisplayName());
			com.setIcon(Config.getDefault().getIcon(state));
			com.setFont(table.getFont());
		}
		return com;
	}
}