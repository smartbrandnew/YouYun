package com.broada.carrier.monitor.client.impl.probe;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableCellRenderer;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.DateTableCellRenderer;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.OnlineState;
import com.broada.component.utils.lang.ThreadUtil;
import com.broada.swing.util.WinUtil;

public class ProbeManageWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	private ProbeTableModel tableModel = new ProbeTableModel();
	private BaseTable table = new BaseTable(tableModel);
	private Thread refrehThread;
	private Thread autoRefreshThread;
	private boolean running = false;

	public ProbeManageWindow(Window owner) {
		super(owner);
		setTitle("监测探针管理");
		setModal(true);
		setResizable(true);
		setPreferredSize(new Dimension(800, 500));
		getContentPane().setLayout(new BorderLayout(0, 0));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);

		JButton btnRefresh = new JButton("刷新");
		btnRefresh.addActionListener(new BtnRefreshActionListener());

		JButton btnSync = new JButton("同步");
		btnSync.addActionListener(new BtnSyncActionListener());

		JButton btnCreate = new JButton("添加");
		btnCreate.addActionListener(new BtnCreateActionListener());
		panel.add(btnCreate);

		JButton btnEdit = new JButton("修改");
		btnEdit.addActionListener(new BtnEditActionListener());
		panel.add(btnEdit);

		JButton btnDelete = new JButton("删除");
		btnDelete.addActionListener(new BtnDeleteActionListener());
		panel.add(btnDelete);
		panel.add(btnSync);

		JButton btnView = new JButton("查看");
		btnView.addActionListener(new BtnViewActionListener());
		panel.add(btnView);

		JButton btnReboot = new JButton("重启");
		btnReboot.addActionListener(new BtnRebootActionListener());
		panel.add(btnReboot);
		panel.add(btnRefresh);

		JButton btnExit = new JButton("退出");
		btnExit.addActionListener(new BtnExitActionListener());
		panel.add(btnExit);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		scrollPane.setViewportView(table);
		pack();
		
		if (ServerContext.isConnected()) {			
			autoRefreshThread = ThreadUtil.createThread(new AutoRefresher());
			autoRefreshThread.start();
		}
	}	
	
	private class AutoRefresher implements Runnable {
		@Override
		public void run() {
			running = true;
			while (running) {
				startRefresh(tableModel.getRows());
				try {
					Thread.sleep(120000);
				} catch (InterruptedException e) {
					break;
				}
			}
		}
	}
	
	@Override
	public void dispose() {
		if (autoRefreshThread != null) {
			running = false;
			autoRefreshThread.interrupt();
			autoRefreshThread = null;
		}
		super.dispose();
	}

	private static class OnlineStateTableCellRenderer extends BaseTableCellRenderer {
		private JLabel com = new JLabel();

		public OnlineStateTableCellRenderer() {
			com.setHorizontalAlignment(SwingConstants.CENTER);
			com.setOpaque(true);
		}

		@Override
		protected Component getComponent(JTable table, Object value) {
			OnlineState state = (OnlineState) value;
			com.setText(state.getDescr());
			com.setIcon(Config.getDefault().getIcon(state));
			com.setFont(table.getFont());
			return com;
		}
	}

	private static class ProbeTableModel extends BeanTableModel<MonitorProbeVO> {
		private static final long serialVersionUID = 1L;

		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("index", "序号", 40, new TextTableCellRenderer()),
				new BaseTableColumn("probe.code", "编码", 80),
				new BaseTableColumn("probe.name", "名称", 80),
				new BaseTableColumn("probe.host", "地址", 90),
				new BaseTableColumn("probe.port", "端口", 40),
				new BaseTableColumn("status.onlineState", "在线状态", 60, new OnlineStateTableCellRenderer()),
				new BaseTableColumn("status.time", "采样时间", 100, new DateTableCellRenderer()),
				new BaseTableColumn("probe.descr", "说明", 100),
		};

		public ProbeTableModel() {
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

	private class RefreshThread implements Runnable {
		private List<MonitorProbeVO> rows;

		public RefreshThread(List<MonitorProbeVO> rows) {
			this.rows = rows;
		}

		@Override
		public void run() {
			try {
				for (MonitorProbeVO row : rows) {
					row.getStatus().setOnlineState(OnlineState.TESTING);
					tableModel.fireTableDataChanged();
					row.setStatus(ServerContext.getProbeService().testProbeStatus(row.getProbe().getId()));
					tableModel.fireTableDataChanged();
				}
			} finally {
				refrehThread = null;
			}
		}

	}

	public static class MonitorProbeVO {
		private MonitorProbe probe;
		private MonitorProbeStatus status;

		public MonitorProbe getProbe() {
			return probe;
		}

		public void setProbe(MonitorProbe probe) {
			this.probe = probe;
		}

		public MonitorProbeStatus getStatus() {
			return status;
		}

		public void setStatus(MonitorProbeStatus status) {
			this.status = status;
		}

		public MonitorProbeVO(MonitorProbe probe, MonitorProbeStatus status) {
			this.probe = probe;
			this.status = status;
		}
	}

	private class BtnSyncActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorProbeVO probe = tableModel.checkSelectedRow(table);
			ServerContext.getProbeService().syncProbe(probe.getProbe().getId());
			ProbeSyncWindow.show(ProbeManageWindow.this, probe.getProbe().getId());
		}
	}

	private class BtnExitActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			WinUtil.getWindowForComponent(ProbeManageWindow.this).dispose();
		}
	}

	private class BtnRefreshActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (refrehThread != null)
				return;

			List<MonitorProbeVO> rows = tableModel.getSelectedRows(table);
			startRefresh(rows);
		}
	}
	
	private void startRefresh(MonitorProbeVO row) {
		if (refrehThread != null) 
			return;
		
		List<MonitorProbeVO> rows = new ArrayList<MonitorProbeVO>();
		rows.add(row);
		startRefresh(rows);
	}
	
	private void startRefresh(List<MonitorProbeVO> rows) {
		if (refrehThread != null) 
			return;
		
		if (rows == null || rows.isEmpty())
			rows = tableModel.getRows();
		if (rows.size() > 0) {
			refrehThread = ThreadUtil.createThread(new RefreshThread(rows));
			refrehThread.start();
		}
	}

	private class BtnCreateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorProbe probe = ProbeEditPanel.show(WinUtil.getWindowForComponent(ProbeManageWindow.this));
			if (probe != null) {
				MonitorProbeVO row = new MonitorProbeVO(probe, ServerContext.getProbeService().getProbeStatus(probe.getId())); 
				tableModel.addRow(row);
				startRefresh(row);
			}
		}
	}

	private class BtnEditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorProbeVO probe = tableModel.checkSelectedRow(table);
			MonitorProbe newProbe = ProbeEditPanel.show(WinUtil.getWindowForComponent(ProbeManageWindow.this),
					probe.getProbe());
			if (newProbe != null) {
				probe.getProbe().set(newProbe);
				tableModel.fireTableDataChanged();
				startRefresh(probe);
			}
		}
	}

	private class BtnDeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorProbeVO probe = tableModel.checkSelectedRow(table);
			Page<MonitorNode> page = ServerContext.getNodeService().getNodesByProbeId(PageNo.ONE, probe.getProbe().getId(), false);
			if (!page.isEmpty()) {
				JOptionPane.showMessageDialog(ProbeManageWindow.this, "此探针还部署有监测节点，无法删除");
				return;
			}

			if (JOptionPane.showConfirmDialog(ProbeManageWindow.this, "请确认是否删除此探针？", "操作确认", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;

			ServerContext.getProbeService().deleteProbe(probe.getProbe().getId());
			tableModel.removeRow(probe);
		}
	}

	private class BtnViewActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorProbeVO probe = tableModel.checkSelectedRow(table);
			if (probe == null)
				return;
			ProbeViewPanel.show(WinUtil.getWindowForComponent(ProbeManageWindow.this), probe.getProbe());
		}
	}

	private class BtnRebootActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorProbeVO probe = tableModel.checkSelectedRow(table);
			if (JOptionPane.showConfirmDialog(ProbeManageWindow.this, "重启将导致探针缓存中存在的数据丢失，请确认是否重启此探针？", "操作确认",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;

			// TODO L 2015-03-28 10:59:04 可以写明是什么有用户，从哪个机器上重启
			ServerContext.getProbeService().exitProbe(probe.getProbe().getId(), "用户重启");
			probe.getStatus().setOnlineState(OnlineState.OFFLINE);
			probe.getStatus().setTime(new Date());
			tableModel.fireTableDataChanged();			
		}
	}

	public void refresh() {
		MonitorProbe[] probes = ServerContext.getProbeService().getProbes();
		MonitorProbeStatus[] statuses = ServerContext.getProbeService().getProbeStatuses();
		List<MonitorProbeVO> rows = new ArrayList<MonitorProbeVO>();
		for (int i = 0; i < probes.length; i++) {
			MonitorProbe probe = probes[i];
			MonitorProbeStatus status = null;
			for (int j = 0; j < statuses.length; j++) {
				if (statuses[j].getProbeId() == probe.getId()) {
					status = statuses[j];
					break;
				}
			}
			if (status == null)
				status = ServerContext.getProbeService().getProbeStatus(probe.getId());
			rows.add(new MonitorProbeVO(probe, status));
		}
		tableModel.setRows(rows);
	}

	public static void show(Window owner) {
		ProbeManageWindow window = new ProbeManageWindow(owner);
		window.refresh();
		WinUtil.toCenter(window);
		window.setVisible(true);
	}
}
