package com.broada.carrier.monitor.client.impl.method;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.swing.ShowWindow;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.BeanTableRowFilter;
import com.broada.carrier.monitor.common.swing.table.TextTableCellRenderer;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;
import com.broada.swing.util.WinUtil;

public class MethodManagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private MethodTableModel tableModel = new MethodTableModel();
	private BaseTable table = new BaseTable(tableModel);
	private ServerServiceFactory serverFactory;
	private String[] methodTypeIds;
	private MonitorNode node;
	private MonitorResource resource;
	private boolean modified = false;
	private JPopupMenu createMenu = new JPopupMenu();
	private JButton btnCreate = new JButton("添加");
	private JTextField txtFilter;

	public MethodManagePanel() {
		setLayout(new BorderLayout(0, 0));
		setPreferredSize(new Dimension(600, 400));

		JPanel panel = new JPanel();
		add(panel, BorderLayout.NORTH);

		btnCreate.addActionListener(new BtnCreateActionListener());
		panel.add(btnCreate);

		JButton btnEdit = new JButton("修改");
		btnEdit.addActionListener(new BtnEditActionListener());
		panel.add(btnEdit);

		JButton btnDelete = new JButton("删除");
		btnDelete.addActionListener(new BtnDeleteActionListener());
		panel.add(btnDelete);

		JLabel lblNewLabel = new JLabel("过滤：");
		panel.add(lblNewLabel);

		txtFilter = new JTextField();
		txtFilter.addKeyListener(new TxtFilterKeyListener());
		txtFilter.setPreferredSize(new Dimension(120, (int) txtFilter.getPreferredSize().getHeight()));
		panel.add(txtFilter);
		txtFilter.setColumns(10);

		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		table.addMouseListener(new TableMouseListener());

		scrollPane.setViewportView(table);
	}

	private class TxtFilterKeyListener extends KeyAdapter {
		private String lastKey;

		@Override
		public void keyReleased(KeyEvent e) {
			if (txtFilter.getText().equals(lastKey))
				return;
			lastKey = txtFilter.getText();
			if (lastKey.isEmpty())
				tableModel.setFilter(null);
			else
				tableModel.setFilter(new MonitorMethodFilter(lastKey));
		}
	}

	public static class MonitorMethodFilter implements BeanTableRowFilter<MonitorMethodVO> {
		private String key;

		public MonitorMethodFilter(String key) {
			this.key = key;
		}

		@Override
		public boolean match(MonitorMethodVO row) {
			return row.getMethod().getCode().toUpperCase().contains(key.toUpperCase()) ||
						row.getMethod().getName().toUpperCase().contains(key.toUpperCase()) ||
						row.getMethodTypeName().toUpperCase().contains(key.toUpperCase());
		}

	}

	private class BtnCreateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (methodTypeIds.length == 1)
				createMethod(methodTypeIds[0]);
			else if (methodTypeIds.length > 0)
				createMenu.show(btnCreate, 0, btnCreate.getHeight());
		}
	}

	private void editSelected() {
		MonitorMethod method = tableModel.checkSelectedRow(table).getMethod();
		MonitorMethodConfigContext methodContext = new MonitorMethodConfigContext(serverFactory, node, resource, method);
		if (MethodEditPanel.show(WinUtil.getWindowForComponent(MethodManagePanel.this), methodContext))
			tableModel.fireTableDataChanged();
		modified = true;
	}

	public void createMethod(String methodTypeId) {
		MonitorMethod method = new MonitorMethod();
		method.setTypeId(methodTypeId);
		MonitorMethodConfigContext methodContext = new MonitorMethodConfigContext(serverFactory, node, resource, method);
		if (MethodEditPanel.showCreate(WinUtil.getWindowForComponent(MethodManagePanel.this), methodContext))
			tableModel.addRow(new MonitorMethodVO(methodContext.getMethod()));
		modified = true;
	}

	private class BtnEditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			editSelected();
		}
	}

	private class BtnDeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MonitorMethodVO method = tableModel.checkSelectedRow(table);
			ServerContext.getServerFactory().getMethodService().deleteMethod(method.getMethod().getCode());
			tableModel.removeRow(method);
			modified = true;
		}
	}

	private class TableMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1)
				editSelected();
		}
	}

	private class CreateActionListener implements ActionListener {
		private String methodTypeId;

		public CreateActionListener(String methodTypeId) {
			this.methodTypeId = methodTypeId;
		}

		public void actionPerformed(ActionEvent e) {
			createMethod(methodTypeId);
		}
	}

	public static class MonitorMethodVO {
		private MonitorMethod method;
		private String methodTypeName;

		public MonitorMethodVO(MonitorMethod method) {
			this.method = method;
		}

		public MonitorMethod getMethod() {
			return method;
		}

		public String getMethodTypeName() {
			if (methodTypeName == null)
				methodTypeName = ServerContext.checkMethodType(getMethod().getTypeId()).getName();
			return methodTypeName;
		}
	}

	private static class MethodTableModel extends BeanTableModel<MonitorMethodVO> {
		private static final long serialVersionUID = 1L;
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("index", "序号", 30, new TextTableCellRenderer()),
				new BaseTableColumn("methodTypeName", "类型", 120),
				new BaseTableColumn("method.code", "编码", 80),
				new BaseTableColumn("method.name", "名称", 100),
				new BaseTableColumn("method.descr", "说明", 200),
		};

		public MethodTableModel() {
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

	private void setData(ServerServiceFactory serverFactory, String[] methodTypeIds, MonitorNode node,
			MonitorResource resource) {
		this.serverFactory = serverFactory;
		this.methodTypeIds = methodTypeIds;
		this.node = node;
		this.resource = resource;

		List<MonitorMethodVO> vos = new ArrayList<MonitorMethodVO>();
		for (String methodTypeId : methodTypeIds) {
			if (methodTypeIds.length > 1) {
				JMenuItem mi = new JMenuItem(ServerContext.checkMethodType(methodTypeId).getName());
				createMenu.add(mi);
				mi.addActionListener(new CreateActionListener(methodTypeId));
			}
			MonitorMethod[] methods = ServerContext.getMethodService().getMethodsByTypeId(methodTypeId);
			for (int i = 0; i < methods.length; i++)
				vos.add(new MonitorMethodVO(methods[i]));
		}
		tableModel.setRows(vos);
	}

	public void setSelected(String methodCode) {
		table.setSelected(-1);
		if (methodCode == null) {
			for (int i = 0; i < tableModel.getRowCount(); i++) {
				MonitorMethodVO row = tableModel.getRow(i);
				if (row.getMethod().getCode().equals(methodCode)) {
					table.setSelected(i);
					break;
				}
			}
		}
	}

	public MonitorMethod getSelected() {
		if (table.getSelectedRow() < 0)
			return null;
		return tableModel.checkSelectedRow(table).getMethod();
	}

	public boolean isModified() {
		return modified;
	}

	public void show(Window owner, ServerServiceFactory serverFactory, String[] methodTypeIds, MonitorNode node,
			MonitorResource resource, String methodCode) {
		setData(serverFactory, methodTypeIds, node, resource);
		setSelected(methodCode);
		ShowWindow.show(owner, "监测方法管理", this);
	}
}
