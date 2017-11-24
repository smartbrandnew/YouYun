package com.broada.carrier.monitor.impl.common;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.impl.common.ui.IndicatorTableCellRenderer;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;

public class SingleInstanceConfiger extends BaseMonitorConfiger {
	private static final long serialVersionUID = 1L;
	private ItemTableModel tableModel = new ItemTableModel();
	private BaseTable table = new BaseTable(tableModel);

	public SingleInstanceConfiger() {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);
		
		JButton[] buttons = getButtons();
		if (buttons != null) {
			for (JButton btn : buttons)
				panel.add(btn);
		}		

		JButton btnRefresh = new JButton("刷新");
		btnRefresh.addActionListener(new BtnRefreshActionListener());
		panel.add(btnRefresh);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);

		scrollPane.setViewportView(table);
	}

	private class BtnRefreshActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			refresh();
		}
	}
	
	protected JButton[] getButtons() {
		return null;
	}

	@Override
	public void refresh() {
		WinUtil.switchBusy();
		try {
			List<ItemVO> rows = new ArrayList<ItemVO>();
	
			try {
				MonitorResult result = (MonitorResult) collect();
				if (result != null) {
					if (result.getState() == MonitorState.FAILED)
						ErrorDlg.show(result.getMessage());			
					else if (result.getRows() != null){
						if (result.getRows().size() > 1)
							throw new IllegalArgumentException("单实例配置界面无法用于多实例监测任务");
						if (result.getRows().size() > 0) {
							MonitorResultRow row = result.getRows().get(0);
							for (Entry<String, Object> entry : row.entrySet()) {
								if (MonitorResultRow.isIndicator(entry.getKey())) {
									MonitorItem item = ServerUtil.checkItem(getServerFactory().getTypeService(), entry.getKey());
									rows.add(new ItemVO(item, entry.getValue()));
								}
							}
						}
					}
				}
			} catch (Throwable e) {
				ErrorDlg.show(e);
			}
	
			tableModel.setRows(rows);
		} finally {
			WinUtil.switchIdle();
		}
	}

	private static class ItemTableModel extends BeanTableModel<ItemVO> {
		private static final long serialVersionUID = 1L;
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("name", "指标名称", 60, new TextTableCellRenderer()),
				new BaseTableColumn("value", "当前值", 160, new IndicatorTableCellRenderer()),
				new BaseTableColumn("unit", "单位", 20, new TextTableCellRenderer()),
				new BaseTableColumn("descr", "说明", new TextTableCellRenderer(SwingConstants.LEFT)),
		};

		public ItemTableModel() {
			super(columns);
		}
	}

	public static class ItemVO {
		private MonitorItem item;
		private Object value;

		public ItemVO(MonitorItem item, Object value) {
			this.item = item;
			this.value = value;
		}

		public String getName() {
			return item.getName();
		}

		public Object getValue() {
			return value;
		}

		public String getUnit() {
			return item.getUnit();
		}

		public String getDescr() {
			return item.getDescr();
		}
	}
}
