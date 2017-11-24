package com.broada.carrier.monitor.client.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.broada.carrier.monitor.client.impl.cache.ClientCache;
import com.broada.carrier.monitor.client.impl.common.ActionTasksDisable;
import com.broada.carrier.monitor.client.impl.common.ActionTasksDispatch;
import com.broada.carrier.monitor.client.impl.common.ActionTasksEnable;
import com.broada.carrier.monitor.client.impl.common.TargetTypeIconLibrary;
import com.broada.carrier.monitor.client.impl.node.NodeEditPanel;
import com.broada.carrier.monitor.client.impl.node.NodeTreeNode;
import com.broada.carrier.monitor.client.impl.resource.ResourceEditPanel;
import com.broada.carrier.monitor.client.impl.resource.ResourceTreeNode;
import com.broada.carrier.monitor.client.impl.target.TargetManagePanel;
import com.broada.carrier.monitor.client.impl.target.TargetSelectedListener;
import com.broada.carrier.monitor.client.impl.task.TaskManagePanel;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.common.swing.action.Action;
import com.broada.carrier.monitor.common.swing.action.ActionPopupMenu;
import com.broada.carrier.monitor.common.swing.tree.BaseTree;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeModel;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNodeFilter;
import com.broada.carrier.monitor.common.swing.tree.TreeSelectedGetter;
import com.broada.carrier.monitor.common.swing.tree.TreeUtil;
import com.broada.carrier.monitor.server.api.client.EventListener;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetGroup;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.OperatorResult;
import com.broada.carrier.monitor.server.api.event.NodeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ObjectChangedType;
import com.broada.carrier.monitor.server.api.event.ProbeChangedEvent;
import com.broada.carrier.monitor.server.api.event.ResourceChangedEvent;
import com.broada.swing.util.WinUtil;

public class MainPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField txtProbeFilter;
	private GroupTreeModel treeGroupModel = new GroupTreeModel();
	private JTree treeGroup = new BaseTree(treeGroupModel);
	private ProbeTreeModel treeProbeModel = new ProbeTreeModel();
	private JTree treeProbe = new BaseTree(treeProbeModel);
	private TaskManagePanel taskManagePanel = new TaskManagePanel();
	private TargetManagePanel targetManagePanel = new TargetManagePanel();
	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	private JSplitPane splContent = new JSplitPane();
	private static Map<String, NodeTreeNode> nodeTreeNodeMap = new ConcurrentHashMap<String, NodeTreeNode>();
	private static Map<String, ProbeTreeNode> probeTreeNodeMap = new ConcurrentHashMap<String, ProbeTreeNode>();

	public MainPanel() {
		setLayout(new BorderLayout(0, 0));

		tabbedPane.addChangeListener(new TabbedPaneChangeListener());
		tabbedPane.setPreferredSize(new Dimension(200, 0));
		tabbedPane.setMinimumSize(new Dimension(200, 0));
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		JScrollPane scrollPane = new JScrollPane();
		tabbedPane.addTab("按导航", null, scrollPane, null);
		treeGroup.addTreeSelectionListener(new TreeGroupTreeSelectionListener());

		treeGroup.setRowHeight(22);
		treeGroup.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		scrollPane.setViewportView(treeGroup);

		JSplitPane splitPane = new JSplitPane();
		add(splitPane);
		splitPane.add(tabbedPane, JSplitPane.LEFT);

		JScrollPane scrollPane_1 = new JScrollPane();
		tabbedPane.addTab("按探针", null, scrollPane_1, null);

		JPanel panel_1 = new JPanel();
		panel_1.setLayout(new BorderLayout(0, 0));
		scrollPane_1.setViewportView(panel_1);

		JPanel panelProbeFilter = new JPanel();
		panel_1.add(panelProbeFilter, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("过滤：");

		txtProbeFilter = new JTextField();
		txtProbeFilter.addKeyListener(new TxtProbeFilterKeyListener());
		txtProbeFilter.setColumns(10);
		GroupLayout gl_panelProbeFilter = new GroupLayout(panelProbeFilter);
		gl_panelProbeFilter.setHorizontalGroup(
				gl_panelProbeFilter.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelProbeFilter.createSequentialGroup()
								.addContainerGap()
								.addComponent(lblNewLabel)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(txtProbeFilter, GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
								.addContainerGap())
				);
		gl_panelProbeFilter.setVerticalGroup(
				gl_panelProbeFilter.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panelProbeFilter
										.createSequentialGroup()
										.addGap(5)
										.addGroup(
												gl_panelProbeFilter
														.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblNewLabel)
														.addComponent(txtProbeFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)))
				);
		panelProbeFilter.setLayout(gl_panelProbeFilter);
		panelProbeFilter.setPreferredSize(new Dimension(0, 32));

		treeProbe.addTreeSelectionListener(new TreeProbeTreeSelectionListener());
		treeProbe.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel_1.add(treeProbe);
		splitPane.add(splContent, JSplitPane.RIGHT);
		treeProbe.setModel(treeProbeModel);

		ActionPopupMenu popGroup = new ActionPopupMenu(new TreeSelectedGetter(treeGroup), new Action[] {
				new ActionRefreshGroup()
		});
		popGroup.addPopup(treeGroup);

		ActionPopupMenu popProbe = new ActionPopupMenu(new TreeSelectedGetter(treeProbe), new Action[] {
				new ActionRefreshProbe(),
				null,
				new ActionTasksDispatch(),
				new ActionTasksEnable(),
				new ActionTasksDisable(),
				null,
				new ActionCreateNode(),
				new ActionEditNode(),
				new ActionDeleteNode(),
				null,
				new ActionCreateResource(),
				new ActionEditResource(),
				new ActionDeleteResource(),
		});
		popProbe.addPopup(treeProbe);

		splContent.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splContent.add(targetManagePanel, JSplitPane.TOP);
		splContent.add(taskManagePanel, JSplitPane.BOTTOM);
		splContent.setDividerLocation(200);

		targetManagePanel.setListener(new TargetSelectedListenerImpl());

		if (ServerContext.isConnected())
			ServerContext.registerObjectChangedListener(new ObjectChangedEventListener());
		
		new LoadCache().start();
	}
	
	//加载缓存
	private class LoadCache extends Thread{
		@Override
		public void run() {
			ClientCache.reloadCache();
		}
	}

	private class TargetSelectedListenerImpl implements TargetSelectedListener {

		@Override
		public void onSelected(MonitorTarget target) {
			boolean isGroup = tabbedPane.getSelectedIndex() == 0;
			if (isGroup)
				taskManagePanel.setTasksScope(target);
		}

	}

	private static class TargetGroupTreeNode extends BaseTreeNode {
		private MonitorTargetGroup group;

		@Override
		public Icon getIcon() {
			return TargetTypeIconLibrary.getDefault().getIcon(group.getTargetTypeId());
		}

		public TargetGroupTreeNode(MonitorTargetGroup group) {
			this.group = group;
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		protected BaseTreeNode[] loadChilds() {
			MonitorTargetGroup[] groups = ServerContext.getTargetGroupService().getGroupsByParentId(group.getId());
			BaseTreeNode[] childs = new BaseTreeNode[groups.length];
			for (int i = 0; i < groups.length; i++)
				childs[i] = new TargetGroupTreeNode(groups[i]);
			return childs;
		}

		@Override
		public Object getKey() {
			return group.getId();
		}

		@Override
		public String toString() {
			return group.getName();
		}

		public MonitorTargetGroup getGroup() {
			return group;
		}
	}

	private static class RootGroupTreeNode extends BaseTreeNode {
		private static Icon icon = IconLibrary.getDefault().getIcon("resources/images/tree_target.png");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		protected BaseTreeNode[] loadChilds() {
			MonitorTargetGroup[] groups = ServerContext.getTargetGroupService().getGroupsByParentId(null);
			BaseTreeNode[] childs = new BaseTreeNode[groups.length];
			for (int i = 0; i < groups.length; i++)
				childs[i] = new TargetGroupTreeNode(groups[i]);
			return childs;
		}

		@Override
		public String toString() {
			return "监测项导航";
		}

		@Override
		public Object getKey() {
			return "root";
		}
	}

	private static class RootProbeTreeNode extends BaseTreeNode {
		private static Icon icon = IconLibrary.getDefault().getIcon("resources/images/tree_probe_all.png");

		@Override
		public Icon getIcon() {
			return icon;
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		protected BaseTreeNode[] loadChilds() {
			MonitorProbe[] probes = ServerContext.getProbeService().getProbes();
			BaseTreeNode[] childs = new BaseTreeNode[probes.length];
			for (int i = 0; i < probes.length; i++)
				childs[i] = new ProbeTreeNode(probes[i]);
			return childs;
		}

		@Override
		public String toString() {
			return "所有探针";
		}

		@Override
		public Object getKey() {
			return "root";
		}
	}

	private static class ProbeTreeNode extends BaseTreeNode {
		private static Icon icon = IconLibrary.getDefault().getIcon("resources/images/tree_probe.png");
		private MonitorProbe probe;

		public void setProbe(MonitorProbe probe) {
			this.probe = probe;
		}

		@Override
		public Icon getIcon() {
			return icon;
		}

		public ProbeTreeNode(MonitorProbe probe) {
			this.probe = probe;
		}

		public MonitorProbe getProbe() {
			return probe;
		}

		@Override
		public String toString() {
			return String.format("%s[%s]", getProbe().getCode(), getProbe().getHost());
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		protected BaseTreeNode[] loadChilds() {
			Map<String, MonitorNode> nodeMap = ClientCache.getNodeMap();
			MonitorNode[] nodes = null;
			List<MonitorNode> list = new ArrayList<MonitorNode>();
			for(String nodeId : nodeMap.keySet()){
				if(nodeMap.get(nodeId).getProbeId() == getProbe().getId())
					list.add(nodeMap.get(nodeId));
			}
			nodes = list.toArray(new MonitorNode[0]);
			if (nodes == null)
				return new BaseTreeNode[0];
			BaseTreeNode[] childs = new BaseTreeNode[nodes.length];
			for (int i = 0; i < childs.length; i++) {
				childs[i] = new NodeTreeNode(nodes[i]);
			}
			Arrays.sort(childs);
			return childs;
		}

		@Override
		public Object getKey() {
			return "probe." + getProbe().getId();
		}
	}

	private class GroupTreeModel extends BaseTreeModel {
		private static final long serialVersionUID = 1L;

		public GroupTreeModel() {
			super(new RootGroupTreeNode(), true);
		}

		public void refresh(BaseTreeNode node, boolean includeChilds) {
			ClientCache.reloadCache();
			if (includeChilds)
				node.removeChilds();
			treeGroup.updateUI();
		}
	}

	private class ProbeTreeModel extends BaseTreeModel {
		private static final long serialVersionUID = 1L;

		public ProbeTreeModel() {
			super(new RootProbeTreeNode(), true);
		}

		public RootProbeTreeNode getRoot() {
			return (RootProbeTreeNode) super.getRoot();
		}

		public void setFilter(BaseTreeNodeFilter filter) {
			getRoot().setFilter(filter);
			fireTreeStructureChanged(this, new Object[] { getRoot() }, null, null);
		}

		public void refresh(BaseTreeNode node, boolean includeChilds) {
			ClientCache.reloadCache();
			if (includeChilds)
				node.removeChilds();
			treeProbe.updateUI();
		}
	}

	private void changeProbeTreeSelected(TreePath path) {
		if (path == null) {
			taskManagePanel.setTasksScope("root");
			return;
		}

		BaseTreeNode node = (BaseTreeNode) path.getLastPathComponent();
		if (node instanceof RootProbeTreeNode) {
			taskManagePanel.setTasksScope(node.getKey());
		} else if (node instanceof ProbeTreeNode) {
			taskManagePanel.setTasksScope(((ProbeTreeNode) node).getProbe());
		} else if (node instanceof NodeTreeNode) {
			taskManagePanel.setTasksScope(((NodeTreeNode) node).getNode());
		} else if (node instanceof ResourceTreeNode) {
			taskManagePanel.setTasksScope(((ResourceTreeNode) node).getResource());
		}
	}

	private class TreeProbeTreeSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			changeProbeTreeSelected(e.getNewLeadSelectionPath());
		}
	}

	private class TxtProbeFilterKeyListener extends KeyAdapter {
		private String lastKey;

		@Override
		public void keyReleased(KeyEvent e) {
			if (10 != e.getKeyCode()) 
				return;
			lastKey = txtProbeFilter.getText();
			if (lastKey.isEmpty()) 
				treeProbeModel.setFilter(null);
			else 
				treeProbeModel.setFilter(new ProbeTreeNodeFilter(lastKey));
			TreeUtil.expandTree(treeProbe);
		}
	}

	private abstract class ActionNode extends Action {
		@Override
		public boolean isVisible(Object obj) {
			return obj instanceof NodeTreeNode;
		}
	}

	private abstract class ActionResource extends Action {
		@Override
		public boolean isVisible(Object obj) {
			return obj instanceof ResourceTreeNode;
		}
	}

	private class ActionCreateNode extends Action {
		@Override
		public String getText() {
			return "建立节点";
		}

		@Override
		public void execute(Object obj) {
			ProbeTreeNode node = (ProbeTreeNode) obj;
			MonitorNode mn = NodeEditPanel.show(WinUtil.getWindowForComponent(MainPanel.this), node
					.getProbe().getId());
			if (mn != null) {
				probeTreeNodeMap.put(mn.getId(), node);
				treeProbeModel.refresh(node, true);
			}
		}

		@Override
		public boolean isVisible(Object obj) {
			return obj instanceof ProbeTreeNode;
		}
	}

	private class ActionEditNode extends ActionNode {
		@Override
		public String getText() {
			return "修改节点";
		}

		@Override
		public void execute(Object obj) {
			NodeTreeNode node = (NodeTreeNode) obj;
			int lastProbeId = node.getNode().getProbeId();
			MonitorNode mn = NodeEditPanel.show(WinUtil.getWindowForComponent(MainPanel.this), node.getNode());
			if (mn != null) {
				node.getNode().set(mn);
				if (lastProbeId != mn.getProbeId()) {
					treeProbeModel.refresh(node.getParent(), true);
					BaseTreeNode targetProbeNode = treeProbeModel.getNode("probe." + mn.getProbeId());
					if (targetProbeNode != null) {
						ServerContext.getProbeService().syncProbe(mn.getProbeId());
						treeProbeModel.refresh(targetProbeNode, true);
					}
				} else
					treeProbeModel.refresh(node, false);
			}
		}
	}

	private class ActionDeleteNode extends ActionNode {
		@Override
		public String getText() {
			return "删除节点";
		}

		@Override
		public void execute(Object obj) {
			NodeTreeNode node = (NodeTreeNode) obj;
			OperatorResult result = ServerContext.deleteNode(node.getNode().getId());
			String nodeId = node.getNode().getId();
			if (probeTreeNodeMap.get(nodeId) == null) {
				ProbeTreeNode probeTreeNode = (ProbeTreeNode) node.getParent();
				probeTreeNodeMap.put(nodeId, probeTreeNode);
			}
			if (result == OperatorResult.DELETED) {
				probeTreeNodeMap.remove(nodeId);
			}
			treeProbeModel.refresh(node.getParent(), true);
		}
	}

	private class ActionCreateResource extends ActionNode {
		@Override
		public String getText() {
			return "建立资源";
		}

		@Override
		public void execute(Object obj) {
			NodeTreeNode node = (NodeTreeNode) obj;
			MonitorResource resource = ResourceEditPanel
					.show(WinUtil.getWindowForComponent(MainPanel.this), node.getNode());
			if (resource != null) {
				nodeTreeNodeMap.put(resource.getId(), node);
				treeProbeModel.refresh(node, true);
				treeProbe.expandPath(treeProbe.getSelectionPath());
			}
		}
	}

	private class ActionEditResource extends ActionResource {
		@Override
		public String getText() {
			return "修改资源";
		}

		@Override
		public void execute(Object obj) {
			ResourceTreeNode node = (ResourceTreeNode) obj;
			NodeTreeNode parent = (NodeTreeNode) node.getParent();
			MonitorResource mn = ResourceEditPanel.show(WinUtil.getWindowForComponent(MainPanel.this), parent.getNode(),
					node.getResource());
			if (mn != null) {
				node.setResource(mn);
				treeProbeModel.refresh(node, false);
			}
		}
	}

	private class ActionDeleteResource extends ActionResource {
		@Override
		public String getText() {
			return "删除资源";
		}

		@Override
		public void execute(Object obj) {
			ResourceTreeNode node = (ResourceTreeNode) obj;
			OperatorResult result = ServerContext.deleteResource(node.getResource().getId());
			String resId = node.getResource().getId();
			if (nodeTreeNodeMap.get(resId) == null) {
				NodeTreeNode nodeTreeNode = (NodeTreeNode) node.getParent();
				nodeTreeNodeMap.put(resId, nodeTreeNode);
			}
			if (result == OperatorResult.DELETED) {
				nodeTreeNodeMap.remove(resId);
			}
			treeProbeModel.refresh(node.getParent(), true);
		}
	}

	private class ActionRefreshProbe extends Action {
		@Override
		public String getText() {
			return "刷新";
		}

		@Override
		public void execute(Object node) {
			if (node instanceof ProbeTreeNode) {
				ProbeTreeNode temp = (ProbeTreeNode) node;
				temp.setProbe(ServerUtil.checkProbe(ServerContext.getProbeService(), temp.getProbe().getId()));
			} else if (node instanceof NodeTreeNode) {
				NodeTreeNode temp = (NodeTreeNode) node;
				temp.setNode(ServerUtil.checkNode(ServerContext.getNodeService(), temp.getNode().getId()));
			} else if (node instanceof ResourceTreeNode) {
				ResourceTreeNode temp = (ResourceTreeNode) node;
				temp.setResource(ServerUtil.checkResource(ServerContext.getResourceService(), temp.getResource().getId()));
			}
			treeProbeModel.refresh((BaseTreeNode) node, true);
		}
	}

	private class ActionRefreshGroup extends Action {
		@Override
		public String getText() {
			return "刷新";
		}

		@Override
		public void execute(Object node) {
			treeGroupModel.refresh((BaseTreeNode) node, true);
		}
	}

	private double lastDivierLocation;

	private class TabbedPaneChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			boolean isGroup = tabbedPane.getSelectedIndex() == 0;
			if (isGroup) {
				if (lastDivierLocation <= 0)
					lastDivierLocation = 0.5;
				else if (lastDivierLocation > 1)
					lastDivierLocation = lastDivierLocation / splContent.getHeight();
				splContent.setDividerLocation(lastDivierLocation);
				taskManagePanel.setTasksScope(targetManagePanel.getSelected());
			} else {
				lastDivierLocation = splContent.getDividerLocation();
				changeProbeTreeSelected(treeProbe.getSelectionPath());
			}
			targetManagePanel.setVisible(isGroup);
			splContent.setDividerSize(isGroup ? 5 : 0);
		}
	}

	private class TreeGroupTreeSelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			if (e.getNewLeadSelectionPath() == null) {
				targetManagePanel.setGroup(null);
				return;
			}

			BaseTreeNode node = (BaseTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
			if (node instanceof TargetGroupTreeNode)
				targetManagePanel.setGroup(((TargetGroupTreeNode) node).getGroup());
			else
				targetManagePanel.setGroup(null);
		}
	}

	private static class ProbeTreeNodeFilter implements BaseTreeNodeFilter {
		private String key;

		public ProbeTreeNodeFilter(String key) {
			this.key = key;
		}

		@Override
		public boolean match(BaseTreeNode node) {
			if (node instanceof ProbeTreeNode)
				return match(((ProbeTreeNode) node).getProbe().getHost(), key)
						|| match(((ProbeTreeNode) node).getProbe().getCode(), key)
						|| match(((ProbeTreeNode) node).getProbe().getName(), key);
			else if (node instanceof NodeTreeNode)
				return match(((NodeTreeNode) node).getNode().getIp(), key)
						|| match(((NodeTreeNode) node).getNode().getName(), key);
			else if (node instanceof ResourceTreeNode)
				return match(((ResourceTreeNode) node).getResource().getName(), key);
			else
				return false;
		}

		private static boolean match(String text, String key) {
			if (text == null)
				return false;
			else
				return text.toUpperCase().contains(key.toUpperCase());
		}
	}

	private class ObjectChangedEventListener implements EventListener {
		@Override
		public void receive(Object event) {
			if (event instanceof ProbeChangedEvent)
				treeProbeModel.refresh(treeProbeModel.getRoot(), true);
			else if (event instanceof NodeChangedEvent) {
				boolean isGroup = tabbedPane.getSelectedIndex() == 0;
				if (isGroup) {
					treeProbeModel.refresh(treeProbeModel.getRoot(), true);
				}

				ObjectChangedType type = ((NodeChangedEvent) event).getType();
				MonitorNode oldNode = ((NodeChangedEvent) event).getOldObject();

				if (type == ObjectChangedType.UPDATED && oldNode == null) {
					MonitorNode node = ((NodeChangedEvent) event).getNewObject();
					if (node != null) {
						ProbeTreeNode treeNode = probeTreeNodeMap.get(node.getId());
						if (treeNode != null) {
							treeNode.setProbe(ServerUtil.checkProbe(ServerContext.getProbeService(), treeNode.getProbe().getId()));
							treeProbeModel.refresh(treeNode, true);
						}
					}
				}
			} else if (event instanceof ResourceChangedEvent) {
				MonitorResource oldRes = ((ResourceChangedEvent) event).getOldObject();
				ObjectChangedType type = ((ResourceChangedEvent) event).getType();

				if (type == ObjectChangedType.UPDATED && oldRes == null) {
					MonitorResource resource = ((ResourceChangedEvent) event).getNewObject();
					if (resource != null) {
						NodeTreeNode treeNode = nodeTreeNodeMap.get(resource.getId());
						if (treeNode != null) {
							treeNode.setNode(ServerUtil.checkNode(ServerContext.getNodeService(), treeNode.getNode().getId()));
							treeProbeModel.refresh(treeNode, true);
						}
					}
				}
			}
		}
	}

	public MonitorTask[] getSelectedTasks() {
		return taskManagePanel.getSelectedTasks();
	}
}
