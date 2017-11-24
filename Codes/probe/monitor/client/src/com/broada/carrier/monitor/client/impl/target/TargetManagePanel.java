package com.broada.carrier.monitor.client.impl.target;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.client.impl.MainWindow;
import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.common.ActionTasksDisable;
import com.broada.carrier.monitor.client.impl.common.ActionTasksDispatch;
import com.broada.carrier.monitor.client.impl.common.ActionTasksEnable;
import com.broada.carrier.monitor.client.impl.common.MonitorStateTableCellRenderer;
import com.broada.carrier.monitor.client.impl.node.NodeEditPanel;
import com.broada.carrier.monitor.client.impl.probe.ProbeSyncWindow;
import com.broada.carrier.monitor.client.impl.resource.ResourceEditPanel;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.common.swing.action.Action;
import com.broada.carrier.monitor.common.swing.action.ActionPopupMenu;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.BeanTableRowFilter;
import com.broada.carrier.monitor.common.swing.table.TableSelectedGetter;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.common.util.DelayTask;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetAuditState;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ResourceChangedEvent;
import com.broada.carrier.monitor.server.api.event.TargetStatusChangedEvent;
import com.broada.component.utils.error.ErrorUtil;

public class TargetManagePanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(TargetManagePanel.class);
	private static final long serialVersionUID = 1L;
	private TargetTableModel tableModel = new TargetTableModel();
	private BaseTable table = new BaseTable(tableModel);
	private MonitorTargetGroup group;
	private MonitorTargetType groupType;
	private TargetSelectedListener listener;
	private JLabel lblNewLabel;
	private JTextField txtFilter;
	private DelayTask refreshTask;
	private PageNo pageNo;
	private TargetTableRow[] rows;
	private int rowLength;

	public void setListener(TargetSelectedListener listener) {
		this.listener = listener;
	}

	public TargetManagePanel() {
		setPreferredSize(new Dimension(600, 280));
		setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		table.addMouseListener(new TableMouseListener());
		scrollPane.setViewportView(table);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		JButton btnCreateTarget = new JButton("添加");
		btnCreateTarget.addActionListener(new BtnCreateTargetActionListener());
		panel.add(btnCreateTarget);

		JButton btnEditTarget = new JButton("修改");
		btnEditTarget.addActionListener(new BtnEditTargetActionListener());
		panel.add(btnEditTarget);

		JButton btnDeleteTarget = new JButton("删除");
		btnDeleteTarget.addActionListener(new BtnDeleteTargetActionListener());
		panel.add(btnDeleteTarget);

		lblNewLabel = new JLabel("过滤：");
		panel.add(lblNewLabel);

		txtFilter = new JTextField();
		txtFilter.addKeyListener(new TxtFilterKeyListener());
		txtFilter.setPreferredSize(new Dimension(120, (int) txtFilter.getPreferredSize().getHeight()));
		panel.add(txtFilter);
		txtFilter.setColumns(10);

		JButton btnNextPage = new JButton("加载更多");
		btnNextPage.addActionListener(new BtnNextPageActionListener());
		panel.add(btnNextPage);

		JButton btnEndPage = new JButton("加载全部");
		btnEndPage.addActionListener(new BtnEndPageActionListener());
		panel.add(btnEndPage);

		ActionPopupMenu menu = new ActionPopupMenu(new TableSelectedGetter(table, tableModel), new Action[] {
				new ActionTasksDispatch(),
				new ActionTasksEnable(),
				new ActionTasksDisable()
		});
		menu.addPopup(table);

		if (ServerContext.isConnected()) {
			refresh();
			ServerContext.registerObjectChangedListener(new MyChangedEventListener());
		}

		refreshTask = new DelayTask("targetRefresher", 5, new RefreshRunnable());
	}

	private class RefreshRunnable implements Runnable {
		@Override
		public void run() {
			refresh();
		}
	}

	private class MyChangedEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (event instanceof TargetStatusChangedEvent)
				processEvent((TargetStatusChangedEvent) event);
			else if (event instanceof NodeChangedEvent)
				processEvent((NodeChangedEvent) event);
			else if (event instanceof ResourceChangedEvent)
				processEvent((ResourceChangedEvent) event);
		}
	}

	private void processEvent(TargetStatusChangedEvent event) {
		if (tableModel.getRows() == null)
			return;

		for (TargetTableRow target : tableModel.getRows()) {
			if (event.getObject().getTargetId().equals(target.getTarget().getId())) {
				target.setStatus(event.getObject());
				tableModel.fireTableDataChanged();
				break;
			}
		}
	}

	public void processEvent(ResourceChangedEvent event) {
		if (groupType != null && groupType.isResource())
			refreshTask.execute();
	}

	public void processEvent(NodeChangedEvent event) {
		if (groupType != null && groupType.isNode())
			refreshTask.execute();
	}

	public void setGroup(MonitorTargetGroup group) {
		if (group == null || group.equals(this.group))
			return;
		this.group = group;
		this.groupType = ServerUtil.checkTargetType(ServerContext.getTargetTypeService(), group.getTargetTypeId());
		refresh();
	}

	public synchronized void refresh() {
		if (!ServerContext.isConnected())
			return;

		if (group == null)
			tableModel.setRows(new TargetTableRow[0]);
		else {
			List<TargetTableRow> oldSelecteds = tableModel.getSelectedRows(table);
			pageNo = PageNo.createByFirst(0, 200);
			if (groupType.isNode()) {
				Page<MonitorNode> page = ServerContext.getNodeService().getNodesByGroupId(pageNo, group.getId());
				rowLength = page.getRowsCount();
				rows = nodeToTargets(page.getRows());
			} else {
				Page<MonitorResource> page = ServerContext.getResourceService().getResourcesByGroupId(pageNo, group.getId());
				rowLength = page.getRowsCount();
				rows = resourcesToTargets(page.getRows());
			}
			tableModel.setRows(rows);
			if (oldSelecteds != null && oldSelecteds.size() > 0)
				tableModel.setSelectedRows(table, oldSelecteds);
		}
		fireSelected();
	}
	
	private TargetTableRow[] nodeToTargets(MonitorNode[] nodes){
		if(nodes == null)
			return new TargetTableRow[0];
		List<TargetTableRow> list = new ArrayList<TargetTableRow>();
		String [] nodeIds = new String[nodes.length];
		for(int i = 0; i < nodes.length; i++)
			nodeIds[i] = nodes[i].getId();
		MonitorTargetStatus[] MonitorTargetsStatus = ServerContext.getNodeService().getNodesStatus(nodeIds);
		for(MonitorNode node : nodes){
			MonitorTargetStatus s = null;
			for(MonitorTargetStatus monitorTargetStatus : MonitorTargetsStatus){
				if(monitorTargetStatus != null && node.getId().equals(monitorTargetStatus.getTargetId())){
					s = monitorTargetStatus;
					break;
				}
			}
			list.add(new TargetTableRow(node, s));
		}
		return list.toArray(new TargetTableRow[0]);
	}
	
	private TargetTableRow[] resourcesToTargets(MonitorResource[] resources){
		if(resources == null)
			return new TargetTableRow[0];
		List<TargetTableRow> list = new ArrayList<TargetTableRow>();
		List<String> ids = new ArrayList<String>();
		for(MonitorResource resource : resources){
			ids.add(resource.getNodeId());
		}
		MonitorNode[] listNodes = null;
		if(ids.size() == 0)
			listNodes = new MonitorNode[0];
		else
			listNodes = ServerContext.getNodeService().getNodes(ids, true);
		String [] resourceIds = new String[resources.length];
		for(int i = 0; i < resources.length; i++)
			resourceIds[i] = resources[i].getId();
		MonitorTargetStatus[] MonitorTargetsStatus = ServerContext.getResourceService().getResourcesStatus(resourceIds);
		for(MonitorResource resource : resources){
			MonitorNode n = null;
			MonitorTargetStatus s = null;
			for(MonitorNode node : listNodes){
				if(node != null && resource.getNodeId() != null && resource.getNodeId().equals(node.getId())){
					n = node;
					break;
				}
			}
			for(MonitorTargetStatus monitorTargetStatus : MonitorTargetsStatus){
				if(resource.getId().equals(monitorTargetStatus.getTargetId())){
					s = monitorTargetStatus;
					break;
				}
			}
			list.add(new TargetTableRow(resource, n, s));
		}
		return list.toArray(new TargetTableRow[0]);
	}

	private static class TargetTableModel extends BeanTableModel<TargetTableRow> {
		private static final long serialVersionUID = 1L;
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("index", "序号", 35, new TextTableCellRenderer()),
				new BaseTableColumn("ip", "IP", 90, new TargetTextTableCellRenderer()),
				new BaseTableColumn("name", "名称", 150, new TargetTextTableCellRenderer()),
				new BaseTableColumn("typeName", "类型", 80, new TargetTextTableCellRenderer()),
				new BaseTableColumn("probeName", "所属探针", 80),
				new BaseTableColumn("state", "监测状态", 60, new MonitorStateTableCellRenderer()),
				new BaseTableColumn("taskCount", "监测任务数", 40)
		};

		public TargetTableModel() {
			super(columns);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			try {
				Object value;
				if (columnIndex == 0)
					value = rowIndex + 1;
				else
					value = super.getValueAt(rowIndex, columnIndex);
				if (columnIndex >= 1 && columnIndex <= 3) {
					TargetTableRow row = getRow(rowIndex);
					if (row.getTarget().getAuditState() == MonitorTargetAuditState.AUDITING)
						return TargetTextTableCellRenderer.createText(value);
				}
				return value;
			} catch (Throwable e) {
				ErrorUtil.warn(logger, "刷新表格数据失败", e);
				return null;
			}
		}
	}

	private class TableMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			fireSelected();
		}
	}

	private class BtnNextPageActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (groupType != null) {
				pageNo = PageNo.createByIndex(pageNo.getIndex() + 1, 200);
				getDataByPage(pageNo, group.getId());
			}
		}
	}

	private class BtnEndPageActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (groupType != null) {
				getDataByPage(PageNo.ALL, group.getId());
			}
		}
	}

	public void getDataByPage(PageNo pageNo, String groupId) {
		if (group == null)
			tableModel.setRows(new TargetTableRow[0]);
		else {
			List<TargetTableRow> oldSelecteds = tableModel.getSelectedRows(table);
			if (groupType.isNode()) {
				Page<MonitorNode> page = ServerContext.getNodeService().getNodesByGroupId(pageNo, group.getId());
				if (pageNo.getSize() == Integer.MAX_VALUE) {
					rows = new TargetTableRow[page.getRowsCount()];
				} else {
					rowLength = rowLength + page.getRowsCount();
					rows = Arrays.copyOf(rows, rowLength);
				}
				TargetTableRow[] tmpRows = nodeToTargets(page.getRows());
				for (int i = 0; i < page.getRowsCount(); i++) {
					rows[i + pageNo.getFirst()] = tmpRows[i];
				}
				tableModel.setRows(rows);
			} else {
				Page<MonitorResource> page = ServerContext.getResourceService().getResourcesByGroupId(pageNo, group.getId());
				if (pageNo.getSize() == Integer.MAX_VALUE) {
					rows = new TargetTableRow[page.getRowsCount()];
				} else {
					rowLength = rowLength + page.getRowsCount();
					rows = Arrays.copyOf(rows, rowLength);
				}
				TargetTableRow[] tmpRows = resourcesToTargets(page.getRows());
				for (int i = 0; i < tmpRows.length; i++) {
					rows[i + pageNo.getFirst()] = tmpRows[i];
				}
				tableModel.setRows(rows);
			}
			if (oldSelecteds != null && oldSelecteds.size() > 0)
				tableModel.setSelectedRows(table, oldSelecteds);
		}
		fireSelected();
	}

	private class BtnCreateTargetActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorTarget mt = null;
			if (groupType == null) {
				JOptionPane.showMessageDialog(TargetManagePanel.this, "请选择一个导航节点");
				return;
			}
			if (groupType.isNode()) {
				mt = NodeEditPanel.show(WinUtil.getWindowForComponent(TargetManagePanel.this), groupType);
			} else {
				mt = ResourceEditPanel.show(WinUtil.getWindowForComponent(TargetManagePanel.this), groupType);
			}
			if (mt != null)
				refresh();
		}
	}

	private class BtnEditTargetActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			List<TargetTableRow> rows = tableModel.checkSelectedRows(table);
			boolean needRefresh = false;
			TargetTableRow target = rows.get(0);
			int probeId1 = target.getNode().getProbeId();
			boolean probeChange = false;
			List<Integer> probeIds = new ArrayList<Integer>();
			if (rows.size() == 1) {
				if (target.getTargetType().isNode()) {
					MonitorNode node = NodeEditPanel.show(WinUtil.getWindowForComponent(TargetManagePanel.this), target.getNode());
					//点击取消按钮
					if(node == null)
						return;
					int probeId2 = node.getProbeId();
					MonitorProbeStatus status = ServerContext.getProbeService().getProbeStatus(probeId2);
					if (probeId1 != probeId2 && !status.getOnlineState().toString().equals("离线")) {
						ServerContext.getProbeService().syncProbe(probeId2);
						ProbeSyncWindow.show(MainWindow.getDefault(), probeId2);
					}
					needRefresh = node != null;
				} else {
					needRefresh = ResourceEditPanel.show(WinUtil.getWindowForComponent(TargetManagePanel.this), target.getNode(),
							target.getResource()) != null;
				}
			} else {
				if (target.getTargetType().isNode()) {
					List<MonitorNode> nodes = new ArrayList<MonitorNode>(rows.size());
					for (TargetTableRow row : rows) {
						probeIds.add(row.getProbe().getId());
						nodes.add(row.getNode());
					}
					needRefresh = NodeEditPanel.show(WinUtil.getWindowForComponent(TargetManagePanel.this), nodes);
					int probId2 = target.getNode().getProbeId();
					for (int probeId : probeIds) {
						if (probeId != probId2)
							probeChange = true;
					}
					MonitorProbeStatus status = ServerContext.getProbeService().getProbeStatus(probId2);
					if (probeChange && !status.getOnlineState().toString().equals("离线")) {
						ServerContext.getProbeService().syncProbe(probId2);
						ProbeSyncWindow.show(MainWindow.getDefault(), probId2);
					}
				} else
					JOptionPane.showMessageDialog(WinUtil.getWindowForComponent(TargetManagePanel.this), "批量修改只能用于监测节点");
			}
			if (needRefresh)
				refresh();
		}
	}

	private class BtnDeleteTargetActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			List<TargetTableRow> rows = tableModel.checkSelectedRows(table);
			if (JOptionPane.showConfirmDialog(MainWindow.getDefault(), "删除节点将删除导致其下所有资源与监测任务，请问是否确定？", "操作确认",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;
			boolean needRefresh = false;
			for (TargetTableRow row : rows) {
				if (groupType.isNode()) {
					if (ServerContext.getNodeService().deleteNode(row.getTarget().getId()) != OperatorResult.NONE)
						needRefresh = true;
				} else {
					if (ServerContext.getResourceService().deleteResource(row.getTarget().getId()) != OperatorResult.NONE)
						needRefresh = true;
				}
			}
			if (needRefresh)
				refresh();
		}
	}

	private void fireSelected() {
		TargetTableRow target = tableModel.getSelectedRow(table);
		if (listener != null)
			listener.onSelected(target == null ? null : target.getTarget());
	}

	public MonitorTarget getSelected() {
		TargetTableRow target = tableModel.getSelectedRow(table);
		return target == null ? null : target.getTarget();
	}

	private class TxtFilterKeyListener extends KeyAdapter {
		private String lastKey;

		@Override
		public void keyReleased(KeyEvent e) {
			if (10 != e.getKeyCode()) {
				if (txtFilter.getText().equals(lastKey))
					return;
				lastKey = txtFilter.getText();
			}
			if (lastKey.isEmpty())
				tableModel.setFilter(null);
			else {
				if (10 == e.getKeyCode())
					tableModel.setFilter(new MonitorTargetFilter(lastKey));
			}
		}
	}

	public static class MonitorTargetFilter implements BeanTableRowFilter<TargetTableRow> {
		private String key;

		public MonitorTargetFilter(String key) {
			this.key = key;
		}

		@Override
		public boolean match(TargetTableRow row) {
			return row.getName().toUpperCase().contains(key.toUpperCase())
					|| row.getIp().contains(key)
					|| row.getTargetType().getName().toUpperCase().contains(key.toUpperCase())
					|| row.getProbe().getName().toUpperCase().contains(key.toUpperCase());

		}
	}
}
