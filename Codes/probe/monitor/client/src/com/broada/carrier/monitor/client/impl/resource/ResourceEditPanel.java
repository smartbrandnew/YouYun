package com.broada.carrier.monitor.client.impl.resource;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.common.TargetTypeTreeNode;
import com.broada.carrier.monitor.client.impl.target.TargetTableRow;
import com.broada.carrier.monitor.common.entity.Page;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.common.error.ServiceException;
import com.broada.carrier.monitor.common.swing.BeanEditPanel;
import com.broada.carrier.monitor.common.swing.BeanEditWindow;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.ObjectDecoder;
import com.broada.carrier.monitor.common.swing.table.TableComboBox;
import com.broada.carrier.monitor.common.swing.table.TableDataProvider;
import com.broada.carrier.monitor.common.swing.tree.BaseTree;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeModel;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.common.swing.tree.TreeComboBox;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;

public class ResourceEditPanel extends BeanEditPanel<MonitorResource> {
	private static final long serialVersionUID = 1L;
	private static String lastTargetTypeId;
	private TableComboBox txtNode;
	private JTextField txtName;
	private BaseTree treeTargetType = new ResourceTypeTree();
	private TreeComboBox cbxTargetType = new TreeComboBox(treeTargetType);
	private MonitorNode node;
	private String resourceId;
	private JTextField txtProbe;
	private JLabel lblProbe = new JLabel("探针：");
	private BeanTableModel<Object> nodeTableModel = new BeanTableModel<Object>(
			new BaseTableColumn[] {
					new BaseTableColumn("name", "节点"),
					new BaseTableColumn("ip", "IP地址"),
					new BaseTableColumn("typeName", "类型"),
			});
	private TableDataProvider<Object> dataProvider = new TableDataProvider<Object>() {
		@Override
		public Page<Object> getData(PageNo pageNo, String key) {
			Page<MonitorNode> page = ServerContext.getNodeService().getNodesByIp(pageNo, key);
			Object[] rows = new Object[page.getRowsCount()];
			for (int i = 0; i < rows.length; i++)
				rows[i] = new TargetTableRow(page.getRows()[i], null);
			return new Page<Object>(rows, page.isMore());
		}
	};

	public ResourceEditPanel() {
		setLayout(null);
		setPreferredSize(new Dimension(350, 140));

		JLabel lblNewLabel = new JLabel("类型：");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(10, 13, 55, 15);
		add(lblNewLabel);
		cbxTargetType.addItemListener(new CbxTargetTypeItemListener());
		cbxTargetType.setBounds(71, 10, 159, 21);
		add(cbxTargetType);

		JLabel lblNode = new JLabel("节点：");
		lblNode.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNode.setBounds(10, 44, 55, 15);
		add(lblNode);

		JLabel label = new JLabel("名称：");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(10, 75, 55, 15);
		add(label);

		txtNode = new TableComboBox(nodeTableModel, dataProvider, 5, new ObjectDecoder() {
			@Override
			public String getName(Object value) {
				return ((TargetTableRow) value).getIp();
			}
		});
		txtNode.setEditable(false);
		txtNode.setBounds(71, 41, 261, 21);
		add(txtNode);
		txtNode.setColumns(10);

		txtName = new JTextField();
		txtName.setBounds(71, 72, 261, 21);
		add(txtName);
		txtName.setColumns(10);
		lblProbe.setHorizontalAlignment(SwingConstants.RIGHT);
		lblProbe.setBounds(10, 106, 55, 15);
		add(lblProbe);

		txtProbe = new JTextField();
		txtProbe.setEditable(false);
		txtProbe.setColumns(10);
		txtProbe.setBounds(71, 103, 159, 21);
		add(txtProbe);

		treeTargetType.expend();
	}

	@Override
	public String getTitle() {
		return "监测资源";
	}

	@Override
	public MonitorResource getData() {
		MonitorResource resource = new MonitorResource();
		resource.setId(resourceId);
		if (isNodeEditable()) {
			TargetTableRow node = (TargetTableRow) txtNode.getValue();
			if (node == null)
				throw new IllegalArgumentException("请输入并选择有效的监测节点");
			resource.setNodeId(node.getNode().getId());
		} else
			resource.setNodeId(node.getId());
		if (txtName.getText().trim().length() > 40)
			throw new NullPointerException("名称不能超过 40 个汉字、字母或英文。");
		resource.setName(txtName.getText().trim());
		TargetTypeTreeNode type = (TargetTypeTreeNode) cbxTargetType.getSelectedItem();

		if ("".equals(cbxTargetType.getSelectedItem()) || cbxTargetType.getSelectedItem() == null) {
			throw new NullPointerException("类型不能为空。");
		} else if (type != null) {
			resource.setTypeId(type.getKey());
			lastTargetTypeId = type.getKey();
		}
		resource.verify();

		try {
			resource.setId(ServerContext.getResourceService().saveResource(resource));
		} catch (ServiceException e) {
			if (e.getMessage().contains("虚拟基础类型")) {
				JOptionPane.showMessageDialog(this, "此监测项类型为虚拟类型，不允许建立实例");
				return null;
			}
			throw e;
		}
		return resource;
	}

	private boolean isNodeEditable() {
		return node == null;
	}

	@Override
	public void setData(MonitorResource bean) {
		if (bean == null)
			bean = new MonitorResource();
		boolean isCreate = bean.getId() == null;
		cbxTargetType.setEnabled(isCreate);

		resourceId = bean.getId();
		txtName.setText(bean.getName());
		setTargetType(bean.getTypeId());

		if (isNodeEditable()) {
			txtNode.setEditable(true);
			txtProbe.setVisible(false);
			lblProbe.setVisible(false);
		} else {
			txtNode.setEditable(false);
			txtNode.setText(node.retDisplayName());
			txtProbe.setVisible(true);
			lblProbe.setVisible(true);
			setProbe(node.getProbeId());
		}
	}

	private void setProbe(Integer probeId) {
		if (probeId == null || probeId == 0)
			throw new IllegalArgumentException("还未给监测节点分配探针，请修改分配探针");
		MonitorProbe probe = ServerUtil.checkProbe(ServerContext.getProbeService(), node.getProbeId());
		txtProbe.setText(probe.retDisplayName());
	}

	private void setTargetType(String typeId) {
		if (typeId == null)
			typeId = lastTargetTypeId;

		BaseTreeNode node = treeTargetType.getModel().getNode(typeId);
		cbxTargetType.setSelectedItem(node);
	}

	public static MonitorResource show(Window owner, MonitorNode node, MonitorResource resource) {
		if (node == null) {
			JOptionPane.showMessageDialog(owner, "此资源没有关联监测节点");
			return null;
		}
		ResourceEditPanel panel = new ResourceEditPanel();
		panel.node = node;
		return BeanEditWindow.show(owner, panel, resource);
	}

	public static MonitorResource show(Window owner, MonitorNode node) {
		return show(owner, node, null);
	}

	public static MonitorResource show(Window owner, MonitorTargetType type) {
		MonitorResource resource = new MonitorResource();
		resource.setTypeId(type.getId());
		ResourceEditPanel panel = new ResourceEditPanel();
		return BeanEditWindow.show(owner, panel, resource);
	}

	private class CbxTargetTypeItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (txtName.getText().isEmpty())
					txtName.setText(e.getItem().toString());
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				if (txtName.getText().equals(e.getItem().toString()))
					txtName.setText("");
			}
		}
	}

	private static class ResourceTypeTree extends BaseTree {
		private static final long serialVersionUID = 1L;

		public ResourceTypeTree() {
			super(new ResourceTypeTreeModel());
			setRootVisible(false);
		}
	}

	private static class ResourceTypeTreeModel extends BaseTreeModel {
		private static final long serialVersionUID = 1L;

		public ResourceTypeTreeModel() {
			super(new ResourceTypeTreeNode(), true);
		}
	}
}
