package com.broada.carrier.monitor.client.impl.task.step;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.method.MethodManagePanel;
import com.broada.carrier.monitor.client.impl.task.TaskEditStepPanel;
import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.MonitorType;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.swing.util.WinUtil;

public class TaskConfigPanel extends TaskEditStepPanel {
	private static final long serialVersionUID = 1L;
	private JTextField txtName = new JTextField();
	private JTextArea txtDescr = new JTextArea();
	private JComboBox cbxMethod = new JComboBox();
	private JButton btnManageMethod = new JButton("管理");
	private MonitorConfigContext context;
	private MonitorTask task;
	private MonitorConfiger configer;
	private Component configerComponent;
	private MonitorType type;
	private boolean disableSetMethod = false;

	public MonitorType getType() {
		return type;
	}

	public TaskConfigPanel() {
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "基本信息", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setPreferredSize(new Dimension(10, 90));
		add(panel, BorderLayout.NORTH);

		JLabel lblNewLabel = new JLabel("任务名称：");
		txtName.setColumns(10);

		JLabel lblNewLabel_1 = new JLabel("监测方法：");

		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
										.addGroup(gl_panel.createSequentialGroup()
												.addComponent(lblNewLabel)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(txtName))
										.addGroup(gl_panel.createSequentialGroup()
												.addComponent(lblNewLabel_1)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(cbxMethod, GroupLayout.PREFERRED_SIZE, 364, GroupLayout.PREFERRED_SIZE)))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(btnManageMethod)
								.addContainerGap(132, Short.MAX_VALUE))
				);
		cbxMethod.addItemListener(new CbxMethodItemListener());
		btnManageMethod.addActionListener(new BtnManageMethodActionListener());
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
						.addGroup(
								gl_panel
										.createSequentialGroup()
										.addGroup(
												gl_panel
														.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblNewLabel)
														.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addGroup(
												gl_panel
														.createParallelGroup(Alignment.BASELINE)
														.addComponent(lblNewLabel_1)
														.addComponent(cbxMethod, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
																GroupLayout.PREFERRED_SIZE)
														.addComponent(btnManageMethod))
										.addContainerGap(15, Short.MAX_VALUE))
				);
		panel.setLayout(gl_panel);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "\u8BF4\u660E", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setPreferredSize(new Dimension(10, 65));
		add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		txtDescr.setBackground(UIManager.getColor("Panel.background"));
		txtDescr.setEditable(false);
		panel_1.add(txtDescr);
	}

	@Override
	public boolean getData() {
		if (txtName.getText() == null || txtName.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "必须输入任务名称");
			return false;
		}

		try {
			if (!configer.getData())
				return false;
		} catch (Throwable e) {
			ErrorDlg.show(e);
			return false;
		}

		task.setName(txtName.getText());
		return true;
	}

	@Override
	public void setData(MonitorConfigContext data) {
		this.context = data;
		if (this.task != null && this.task.equals(data.getTask()))
			return;
		this.task = data.getTask();

		type = ServerUtil.checkType(ServerContext.getTypeService(), task.getTypeId());
		initConfiger(type);
		initMethods(type, task.getMethodCode());

		if (task.getName() == null || task.getName().length() == 0)
			task.setName(type.getName());
		txtName.setText(task.getName());
		txtDescr.setText(type.getDescription());
	}

	private void initConfiger(MonitorType type) {
		if (configerComponent != null) {
			remove(configerComponent);
			configerComponent = null;
		}
		configer = null;

		try {
			Class<?> configClass = Class.forName(type.getConfiger());
			configer = (MonitorConfiger) configClass.newInstance();
			// TODO 2015-03-10 11:50:21 增加时间			
		} catch (Throwable e) {
			throw ErrorUtil.createRuntimeException(
					String.format("初始化监测配置类失败[type: %s class: %s]", type.getId(), type.getConfiger()), e);
		}

		configerComponent = configer.getComponent();
		add(configerComponent, BorderLayout.CENTER);
		try {
			configer.setData(context);
		} catch (Throwable e) {
			ErrorDlg.show(e);
		}
	}

	private void initMethods(String methodCode) {
		MonitorType type = ServerUtil.checkType(ServerContext.getTypeService(), task.getTypeId());
		initMethods(type, methodCode);
	}

	private void initMethods(MonitorType type, String methodCode) {
		cbxMethod.removeAllItems();
		if (type.getMethodTypeIds() == null || type.getMethodTypeIds().length == 0) {
			cbxMethod.setEnabled(false);
			btnManageMethod.setEnabled(false);
		} else {
			disableSetMethod = true;
			try {
				Set<String> methods = new HashSet<String>();
				for (String typeId : type.getMethodTypeIds()) {
					MonitorMethod[] items = ServerContext.getMethodService().getMethodsByTypeId(typeId);
					for (MonitorMethod item : items) {
						if (methods.contains(item.getCode()))
							continue;
						cbxMethod.addItem(new MonitorMethodVO(item));
					}
				}
				cbxMethod.setSelectedIndex(-1);
			} finally {
				disableSetMethod = false;
			}

			if (methodCode == null) {
				MonitorTask[] tasks = ServerContext.getTaskService().getTasksByNodeId(context.getNode().getId());
				if (tasks != null && tasks.length > 0) {
					for (int i = 0; i < cbxMethod.getItemCount() && methodCode == null; i++) {
						MonitorMethodVO mv = (MonitorMethodVO) cbxMethod.getItemAt(i);
						for (MonitorTask task : tasks) {
							if (mv.getMethod().getCode().equals(task.getMethodCode())) {
								methodCode = mv.getMethod().getCode();
								break;
							}
						}
					}
				}
			}

			if (methodCode == null) {
				for (String typeId : type.getMethodTypeIds()) {
					MonitorMethod[] items = ServerContext.getMethodService().getMethodsByNodeIdAndType(context.getNode().getId(),
							typeId);
					for (MonitorMethod item : items) {
						if (item.getCode() != null) {
							methodCode = item.getCode();
							break;
						}

					}
				}
			}
			setMethod(methodCode);
		}
	}

	private static class MonitorMethodVO {
		private MonitorMethod method;

		public MonitorMethodVO(MonitorMethod method) {
			this.method = method;
		}

		public MonitorMethod getMethod() {
			return method;
		}

		@Override
		public String toString() {
			return method.getName();
		}
	}

	private class CbxMethodItemListener implements ItemListener {
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (disableSetMethod)
					return;

				try {
					MonitorMethodVO item = (MonitorMethodVO) e.getItem();
					task.setMethodCode(item.getMethod().getCode());
					configer.setMethod(item.getMethod());
				} catch (Throwable error) {
					ErrorDlg.show("修改监测方法失败", error);
				}
			}
		}
	}

	private void setMethod(String methodCode) {
		if (methodCode == null)
			cbxMethod.setSelectedIndex(-1);
		else {
			for (int i = 0; i < cbxMethod.getItemCount(); i++) {
				MonitorMethodVO item = (MonitorMethodVO) cbxMethod.getItemAt(i);
				if (item.getMethod().getCode().equals(methodCode)) {
					cbxMethod.setSelectedIndex(i);
					break;
				}
			}
		}
	}

	private class BtnManageMethodActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MethodManagePanel panel = new MethodManagePanel();
			panel.show(WinUtil.getWindowForComponent(TaskConfigPanel.this), context.getServerFactory(),
					context.getType().getMethodTypeIds(), context.getNode(), context.getResource(), task.getMethodCode());
			MonitorMethod method = panel.getSelected();
			if (panel.isModified())
				initMethods(method == null ? task.getMethodCode() : method.getCode());
			else if (method != null && !method.getCode().equals(task.getMethodCode()))
				setMethod(method.getCode());
		}
	}
}
