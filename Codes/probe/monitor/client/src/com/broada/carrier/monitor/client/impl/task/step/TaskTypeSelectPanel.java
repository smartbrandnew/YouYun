package com.broada.carrier.monitor.client.impl.task.step;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.common.TargetTreeNode;
import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.client.impl.node.NodeTreeNode;
import com.broada.carrier.monitor.client.impl.resource.ResourceTreeNode;
import com.broada.carrier.monitor.client.impl.task.TaskEditStepPanel;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeCellRenderer;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.common.swing.tree.TreeUtil;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

import edu.emory.mathcs.backport.java.util.Collections;

public class TaskTypeSelectPanel extends TaskEditStepPanel {
	private static final long serialVersionUID = 1L;
	private TypeTableModel tableModel = new TypeTableModel();
	private JTable table = new BaseTable(tableModel);
	private JTree tree = new JTree();
	private NodeTreeModel treeModel = new NodeTreeModel();
	private JTextField txtTargetType;
	private JTextField txtTargetIp;
	private JTextField txtTargetName;
	private MonitorConfigContext context;

	public JTextField getTxtTargetType() {
		return txtTargetType;
	}

	public TaskTypeSelectPanel() {
		setPreferredSize(new Dimension(750, 450));
		setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		add(splitPane);

		tree.setPreferredSize(new Dimension(200, 0));
		tree.setMinimumSize(new Dimension(200, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(tree);

		JPanel panel = new JPanel();

		splitPane.add(scrollPane, JSplitPane.LEFT);
		splitPane.add(panel, JSplitPane.RIGHT);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "监测项", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setPreferredSize(new Dimension(0, 65));
		panel.add(panel_1, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("类型：");

		txtTargetType = new JTextField();
		txtTargetType.setEditable(false);
		txtTargetType.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("IP：");

		txtTargetIp = new JTextField();
		txtTargetIp.setText("255.255.255.255");
		txtTargetIp.setEditable(false);
		txtTargetIp.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("名称：");

		txtTargetName = new JTextField();
		txtTargetName.setEditable(false);
		txtTargetName.setColumns(10);
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
								.addContainerGap()
								.addComponent(lblNewLabel)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(txtTargetType, GroupLayout.PREFERRED_SIZE, 91, GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addComponent(lblNewLabel_1)
								.addGap(5)
								.addComponent(txtTargetIp, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
								.addGap(18)
								.addComponent(lblNewLabel_2)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(txtTargetName, GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
								.addContainerGap())
				);
		gl_panel_1.setVerticalGroup(
				gl_panel_1
						.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panel_1
										.createSequentialGroup()
										.addGap(5)
										.addGroup(
												gl_panel_1
														.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblNewLabel)
														.addComponent(txtTargetType, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_panel_1.createSequentialGroup()
								.addGap(8)
								.addComponent(lblNewLabel_1))
						.addGroup(
								gl_panel_1
										.createSequentialGroup()
										.addGap(5)
										.addGroup(
												gl_panel_1
														.createParallelGroup(Alignment.BASELINE)
														.addComponent(txtTargetIp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(lblNewLabel_2)
														.addComponent(txtTargetName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addGap(48))
				);
		panel_1.setLayout(gl_panel_1);

		JScrollPane scrollPane_1 = new JScrollPane();
		panel.add(scrollPane_1, BorderLayout.CENTER);
		table.addMouseListener(new TableMouseListener());
		scrollPane_1.setViewportView(table);

		tree.addTreeSelectionListener(new TreeNodeTreeSelectionListener());
		tree.setRowHeight(22);
		tree.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		tree.setCellRenderer(new BaseTreeCellRenderer());
		tree.setModel(treeModel);
	}

	private static class TypeTableModel extends BeanTableModel<MonitorType> {
		private static final long serialVersionUID = 1L;
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("index", "序号", 40, new TextTableCellRenderer()),
				new BaseTableColumn("id", "类型"),
				new BaseTableColumn("name", "名称"),
		};

		public TypeTableModel() {
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

	private class NodeTreeModel extends DefaultTreeModel {
		private static final long serialVersionUID = 1L;

		public NodeTreeModel() {
			super(null, true);
		}
	}

	public class TreeNodeTreeSelectionListener implements TreeSelectionListener {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (e.getNewLeadSelectionPath() == null)
				setSelectedTarget(null);
			else {
				TargetTreeNode node = (TargetTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
				setSelectedTarget(node.getTarget());
			}
		}
	}

	private class TableMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				finish();
			}
		}
	}

	/**
	 * 当用户在监测类型选择窗口左侧的节点与资源选择树中操作时触发此方法，用于更新当前目标资源可用的监测任务
	 * @param target
	 */
	private void setSelectedTarget(MonitorTarget target) {
		MonitorTargetType type = ServerUtil.checkTargetType(ServerContext.getTargetTypeService(), target.getTypeId());		
		txtTargetType.setText(type.getName());
		txtTargetName.setText(target.getName());

		MonitorType[] types = ServerContext.getTypeService().getTypesByTargetTypeId(target.getTypeId());
		
		List<MonitorType> filterTypes = new ArrayList<MonitorType>();				
		MonitorTask[] existsTasks;
		if (target instanceof MonitorResource)
			existsTasks = ServerContext.getTaskService().getTasksByResourceId(target.getId());
		else
			existsTasks = ServerContext.getTaskService().getTasksByNodeId(target.getId());
		String[] multiTypes = Config.getDefault().getMonitorMultiTypes();
		for (MonitorType item : types) {
			boolean exists = false;
			for (String typeId : multiTypes) {
				if (typeId.equalsIgnoreCase(item.getId())) {
					exists = true;
					break;
				}
			}
			
			if (exists) {
				filterTypes.add(item);
				continue;
			}
						
			for (MonitorTask task : existsTasks) {
				if (task.getTypeId().equalsIgnoreCase(item.getId())) {
					exists = true;
					break;
				}
			}
			
			if (!exists) 
				filterTypes.add(item);
		}
		
		Collections.sort(filterTypes, new Comparator<MonitorType>() {
			@Override
			public int compare(MonitorType o1, MonitorType o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		tableModel.setRows(filterTypes);
		
		if (target instanceof MonitorResource)
			context.setResource((MonitorResource)target);
	}

	@Override
	public boolean getData() {
		MonitorType type = tableModel.getSelectedRow(table);
		if (type == null) {
			JOptionPane.showMessageDialog(this, "必须选择一个监测任务");
			return false;
		}
		
		if (!type.getId().equals(context.getTask().getTypeId())) {
			context.setTask(new MonitorTask());
			context.removeInstanceAll();
		}		
		context.getTask().setTypeId(type.getId());
		return true;
	}

	@Override
	public void setData(MonitorConfigContext context) {
		this.context = context;
		txtTargetIp.setText(context.getNode().getIp());

		NodeTreeNode root = new NodeTreeNode(context.getNode());
		treeModel.setRoot(root);
		TreeUtil.expandTree(tree);
		if (context.getResource() != null) {
			for (int i = 0; i < root.getChildCount(); i++) {
				BaseTreeNode child = root.getChildAt(i);
				if (((ResourceTreeNode) child).getResource().getId().equals(context.getResource().getId())) {
					tree.setSelectionPath(new TreePath(new Object[] { root, child }));
					break;
				}
			}
		} else {
			tree.setSelectionPath(new TreePath(new Object[] { root }));
		}		
	}
}
