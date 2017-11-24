package com.broada.carrier.monitor.client.impl.node;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.common.TargetTypeTreeNode;
import com.broada.carrier.monitor.client.impl.entity.DisplayObject;
import com.broada.carrier.monitor.common.error.ServiceException;
import com.broada.carrier.monitor.common.swing.BeanEditPanel;
import com.broada.carrier.monitor.common.swing.BeanEditWindow;
import com.broada.carrier.monitor.common.swing.WinUtil;
import com.broada.carrier.monitor.common.swing.tree.BaseTree;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeModel;
import com.broada.carrier.monitor.common.swing.tree.BaseTreeNode;
import com.broada.carrier.monitor.common.swing.tree.TreeComboBox;
import com.broada.carrier.monitor.common.net.IPUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;

public class NodeEditPanel extends BeanEditPanel<MonitorNode> {
	private static final long serialVersionUID = 1L;
	private static String lastTargetTypeId;
	private JTextField txtIp;
	private JTextField txtName;
	private BaseTree treeTargetType = new NodeTypeTree();
	private TreeComboBox cbxTargetType = new TreeComboBox(treeTargetType);
	private JComboBox cbxProbe = new JComboBox();
	private String nodeId;
	private List<MonitorNode> nodes;

	public NodeEditPanel(List<MonitorNode> nodes) {
		this();
		this.nodes = nodes;
	}

	public NodeEditPanel() {
		setLayout(null);
		setPreferredSize(new Dimension(350, 140));

		JLabel lblNewLabel = new JLabel("类型：");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(10, 13, 55, 15);
		add(lblNewLabel);

		cbxTargetType.setBounds(71, 10, 159, 21);
		add(cbxTargetType);

		JLabel lblIp = new JLabel("IP地址：");
		lblIp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIp.setBounds(10, 44, 55, 15);
		add(lblIp);

		JLabel label = new JLabel("名称：");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(10, 75, 55, 15);
		add(label);

		txtIp = new JTextField();
		txtIp.addFocusListener(new TxtIpFocusListener());
		txtIp.setBounds(71, 41, 159, 21);
		add(txtIp);
		txtIp.setColumns(10);

		txtName = new JTextField();
		txtName.setBounds(71, 72, 261, 21);
		add(txtName);
		txtName.setColumns(10);

		JLabel label_1 = new JLabel("探针：");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		label_1.setBounds(10, 106, 55, 15);
		add(label_1);

		cbxProbe.setBounds(71, 103, 159, 21);
		add(cbxProbe);

		refreshProbe();
		treeTargetType.expend();
	}

	private static class NodeTypeTree extends BaseTree {
		private static final long serialVersionUID = 1L;

		public NodeTypeTree() {
			super(new NodeTypeTreeModel());
			setRootVisible(false);
		}
	}

	private static class NodeTypeTreeModel extends BaseTreeModel {
		private static final long serialVersionUID = 1L;

		public NodeTypeTreeModel() {
			super(new NodeTypeTreeNode(), true);
		}
	}

	private void refreshProbe() {
		cbxProbe.removeAllItems();
		MonitorProbe[] probes = ServerContext.getProbeService().getProbes();
		for (MonitorProbe probe : probes)
			cbxProbe.addItem(new DisplayObject<MonitorProbe>(probe));
	}

	@Override
	public String getTitle() {
		return "监测节点";
	}

	@SuppressWarnings("unchecked")
	@Override
	public MonitorNode getData() {
		if (nodes != null)
			return getDataMulti();

		MonitorNode node = new MonitorNode();

		TargetTypeTreeNode type = (TargetTypeTreeNode) cbxTargetType.getSelectedItem();

		if ("".equals(type) || type == null)
			throw new NullPointerException("类型不能为空。");

		node.setTypeId(type.getKey());
		lastTargetTypeId = type.getKey();
		node.setId(nodeId);
		node.setIp(txtIp.getText().trim());

		if ("".equals(node.getIp()) || node.getIp() == null) {
			throw new NullPointerException("IP地址不能为空。");
		}

		if(!IPUtil.isIPAddress(node.getIp())){
			if (JOptionPane.showConfirmDialog(WinUtil.getWindowForComponent(this),
					String.format("[%s]不是一个合法的IP地址，请确认是否作为域名添加？", node.getIp()), "操作确认", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return null;
		}
		
		if (txtName.getText().trim().length() > 30)
			throw new NullPointerException("名称不能超过 30 个汉字、字母或英文。");
		node.setName(txtName.getText().trim());
		if (cbxProbe.getSelectedIndex() >= 0)
			node.setProbeId(((DisplayObject<MonitorProbe>) cbxProbe.getSelectedItem()).getObject().getId());
		node.verify();
		try {
			node.setId(ServerContext.getNodeService().saveNode(node));
		} catch (ServiceException e) {
			if (e.getMessage().contains("虚拟基础类型")) {
				JOptionPane.showMessageDialog(this, "此监测项类型为虚拟类型，不允许建立实例");
				return null;
			}
			throw e;
		}

		return node;
	}

	@SuppressWarnings("unchecked")
	private MonitorNode getDataMulti() {
		for (MonitorNode node : nodes) {
			node.setProbeId(((DisplayObject<MonitorProbe>) cbxProbe.getSelectedItem()).getObject().getId());
			ServerContext.getNodeService().saveNode(node);
		}
		return nodes.get(0);
	}

	@Override
	public void setData(MonitorNode bean) {
		if (nodes != null) {
			setDataMulti();
			return;
		}
		if (bean == null)
			bean = new MonitorNode();
		boolean isCreate = bean.getId() == null;
		cbxTargetType.setEnabled(isCreate);

		nodeId = bean.getId();
		txtIp.setText(bean.getIp());
		txtName.setText(bean.getName());
		setTargetType(bean.getTypeId());
		setProbe(bean.getProbeId());
	}

	private void setDataMulti() {
		cbxTargetType.setEnabled(false);
		txtIp.setText("...");
		txtIp.setEnabled(false);
		txtName.setText("...");
		txtName.setEnabled(false);
		setProbe(nodes.get(0).getProbeId());
	}

	private void setProbe(Integer probeId) {
		if (probeId == null || probeId <= 0) {
			if (cbxProbe.getItemCount() > 0)
				cbxProbe.setSelectedIndex(0);
		} else {
			for (int i = 0; i < cbxProbe.getItemCount(); i++) {
				DisplayObject<?> obj = (DisplayObject<?>) cbxProbe.getItemAt(i);
				MonitorProbe probe = (MonitorProbe) obj.getObject();
				if (probeId.equals(probe.getId())) {
					cbxProbe.setSelectedIndex(i);
					return;
				}
			}
			cbxProbe.setSelectedIndex(-1);
		}
	}

	private void setTargetType(String typeId) {
		if (typeId == null)
			typeId = lastTargetTypeId;

		BaseTreeNode node = treeTargetType.getModel().getNode(typeId);
		cbxTargetType.setSelectedItem(node);
	}

	public static MonitorNode show(Window owner, MonitorTargetType defaultType) {
		MonitorNode node = new MonitorNode();
		node.setTypeId(defaultType.getId());
		return BeanEditWindow.show(owner, new NodeEditPanel(), node);
	}

	public static MonitorNode show(Window owner, MonitorNode node) {
		return BeanEditWindow.show(owner, new NodeEditPanel(), node);
	}

	public static boolean show(Window owner, List<MonitorNode> nodes) {
		return BeanEditWindow.show(owner, new NodeEditPanel(nodes), nodes.get(0)) != null;
	}

	public static MonitorNode show(Window owner, int probeId) {
		MonitorNode node = new MonitorNode();
		node.setProbeId(probeId);
		return BeanEditWindow.show(owner, new NodeEditPanel(), node);
	}

	private class TxtIpFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			if (txtName.getText().isEmpty())
				txtName.setText(txtIp.getText());
		}
	}
}
