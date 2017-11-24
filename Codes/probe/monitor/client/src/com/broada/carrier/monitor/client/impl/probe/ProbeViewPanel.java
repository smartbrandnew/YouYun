package com.broada.carrier.monitor.client.impl.probe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.swing.ShowWindow;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.SystemInfo;

public class ProbeViewPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private SystemInfoTableModel tableModel = new SystemInfoTableModel();
	private BaseTable table = new BaseTable(tableModel);

	public ProbeViewPanel() {
		setPreferredSize(new Dimension(600, 400));
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);

		scrollPane.setViewportView(table);
	}

	public void setData(MonitorProbe bean) {
		SystemInfo[] infos = ServerContext.getProbeService().getProbeInfos(bean.getId());
		tableModel.setRows(infos);
	}

	private static class SystemInfoTableModel extends BeanTableModel<SystemInfo> {
		private static final long serialVersionUID = 1L;
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("index", "序号", 40, new TextTableCellRenderer()),
				new BaseTableColumn("name", "运行指标", 150),
				new BaseTableColumn("value", "运行值", 350),
		};

		public SystemInfoTableModel() {
			super(columns);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return rowIndex + 1;
			else
				return super.getValueAt(rowIndex, columnIndex);
		}
	}

	public static void show(Window owner, MonitorProbe probe) {
		ProbeViewPanel panel = new ProbeViewPanel();
		panel.setData(probe);
		ShowWindow.show(owner, "探针信息", panel);
	}
}
