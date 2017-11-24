package com.broada.carrier.monitor.impl.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.impl.common.ui.IndicatorTableCellRenderer;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.MonitorItem;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.component.utils.lang.ThreadUtil;

public class SpecificSingleInstanceConfig extends SpecificMonitorConfig {
	private static final long serialVersionUID = 1L;
	private ItemTableModel tableModel = new ItemTableModel();
	private BaseTable table = new BaseTable(tableModel);
	private CollectResult result;

	public SpecificSingleInstanceConfig() {
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

	private class CollectResultDialog extends JDialog {

		private static final long serialVersionUID = 1L;
		private JLabel lblMessage = new JLabel("");
		private JProgressBar pbrProgress = new JProgressBar();
		private JButton btnCancel = new JButton("取消");
		private String nodeId;
		private String taskId;
		private Thread thread;
		private boolean cancelSign = false;
		private JButton btnFinish = new JButton("完成");

		public boolean isCancelSign() {
			return cancelSign;
		}

		public CollectResultDialog(String nodeId, String taskId) {
			this.taskId = taskId;
			this.nodeId = nodeId;
			setTitle("监测采集进度");
			setModal(true);
			getContentPane().setLayout(null);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setPreferredSize(new Dimension(595, 228));

			btnCancel.setBounds(340, 154, 93, 23);
			btnCancel.addActionListener(new BtnCancelActionListener());
			getContentPane().add(btnCancel);

			btnFinish.setBounds(468, 154, 98, 23);
			btnFinish.addActionListener(new BtnFinishActionListener());
			getContentPane().add(btnFinish);

			JLabel lblNewLabel = new JLabel("状态：");
			lblNewLabel.setBounds(10, 25, 54, 15);
			getContentPane().add(lblNewLabel);
			this.setIconImage(IconLibrary.getDefault().getImage(
					"resources/images/app.png"));
			pbrProgress.setBounds(10, 66, 551, 23);
			pbrProgress.setMinimum(0);
			pbrProgress.setMaximum(100);
			lblMessage.setBounds(52, 25, 509, 15);
			getContentPane().add(pbrProgress);
			getContentPane().add(lblMessage);
			pack();
			this.setLocationRelativeTo(null);
			thread = ThreadUtil.createThread(new RefreshThread());
			thread.start();
			setVisible(true);
		}

		public class RefreshThread implements Runnable {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						break;
					}
					result = getCollectResult(nodeId, taskId);
					lblMessage.setText(result.getMessage());
					pbrProgress.setValue(result.getProgress());
					CollectResultDialog.this.repaint();
					if (cancelSign)
						break;
					if (result.getProgress() >= 100)
						break;
				}
			}
		}

		@Override
		public void dispose() {
			if (thread != null)
				thread.interrupt();
			super.dispose();
		}

		private class BtnCancelActionListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				cancelSign = true;
				dispose();
			}
		}

		private class BtnFinishActionListener implements ActionListener {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		}
	}

	@Override
	public void refresh() {
		try {
			List<ItemVO> rows = new ArrayList<ItemVO>();

			try {
				CollectTaskSign taskSign = commit();
				String taskId = taskSign.getTaskId();
				String nodeId = taskSign.getNodeId();
				result = getCollectResult(nodeId, taskId);
				if (result.getProgress() != 100) {
					CollectResultDialog dialog = new CollectResultDialog(nodeId, taskId);
					if (dialog.isCancelSign())
						cancelCollect(nodeId, taskId);
				}
				if (result != null) {
					if (result.getState() == CollectMonitorState.FAILED)
						ErrorDlg.show(result.getMessage());
					else if (result.getRows() != null) {
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
