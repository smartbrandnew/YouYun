package com.broada.carrier.monitor.client.impl.task;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.client.impl.task.step.TaskConfigPanel;
import com.broada.carrier.monitor.client.impl.task.step.TaskPolicySelectPanel;
import com.broada.carrier.monitor.client.impl.task.step.TaskTestPanel;
import com.broada.carrier.monitor.client.impl.task.step.TaskTypeSelectPanel;
import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.common.swing.IconLibrary;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.swing.util.WinUtil;

public class TaskEditWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int STEP_TASK_TYPE_SELECT = 0;
	private static final int STEP_TASK_CONFIG = 1;
	private JSplitPane panelStep = new JSplitPane();
	private JPanel panelStepContent = new JPanel();
	private TaskEditWizard wizard = new TaskEditWizard();
	private JLabel lblStep0 = new JLabel("选择监测任务类型");
	private JLabel lblStep1 = new JLabel("配置监测参数");
	private JLabel lblStep2 = new JLabel("配置监测策略");
	private JLabel lblStep3 = new JLabel("完成");
	private JButton btnPrior = new JButton("上一步");
	private JButton btnNext = new JButton("下一步");
	private JButton btnCancel = new JButton("取消");
	private JLabel[] stepLabels = new JLabel[] {
			lblStep0, lblStep1, lblStep2, lblStep3
	};
	private int step = -1;
	private int minStep;
	private Class<?>[] stepPanelClasses = new Class<?>[] {
			TaskTypeSelectPanel.class,
			TaskConfigPanel.class,
			TaskPolicySelectPanel.class,
			TaskTestPanel.class
	};
	private TaskEditStepPanel[] stepPanels = new TaskEditStepPanel[stepPanelClasses.length];
	private MonitorConfigContext context;
	private final JLabel lblStepImage = new JLabel("");
	private final JPanel panel_2 = new JPanel();
	private final JPanel panel_3 = new JPanel();
	private final JButton btnConfigNext = new JButton("配置下一个");

	/**
	 * @param owner
	 */
	public TaskEditWindow(Window owner) {
		super(owner);
		setTitle("监测任务配置");
		setModal(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		JPanel panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(0, 45));
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		panel_1.add(panel_2, BorderLayout.CENTER);

		panel_1.add(panel_3, BorderLayout.EAST);
		panel_3.add(btnPrior);
		panel_3.add(btnConfigNext);
		panel_3.add(btnNext);
		panel_3.add(btnCancel);
		
		btnConfigNext.addActionListener(new BtnConfigNextActionListener());
		
		btnCancel.addActionListener(new BtnCancelActionListener());

		btnNext.addActionListener(new BtnNextActionListener());

		btnPrior.addActionListener(new BtnPriorActionListener());
		panelStep.setPreferredSize(new Dimension(750, 550));
		getContentPane().add(panelStep, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setPreferredSize(new Dimension(160, 0));
		panel.setMinimumSize(new Dimension(160, 0));
		panelStep.setLeftComponent(panel);
		panel.setLayout(null);

		lblStep0.setBackground(Color.WHITE);
		lblStep0.setBounds(35, 16, 144, 23);
		panel.add(lblStep0);

		lblStep1.setBackground(Color.WHITE);
		lblStep1.setBounds(35, 46, 144, 23);
		panel.add(lblStep1);

		lblStep2.setBackground(Color.WHITE);
		lblStep2.setBounds(35, 76, 144, 23);
		panel.add(lblStep2);

		lblStep3.setBackground(Color.WHITE);
		lblStep3.setBounds(35, 106, 144, 23);
		panel.add(lblStep3);
		lblStepImage.setBounds(16, 149, 18, 15);

		panel.add(lblStepImage);

		panelStep.setRightComponent(panelStepContent);
		panelStepContent.setLayout(new BorderLayout(0, 0));

		CbxStepMouseListener listener = new CbxStepMouseListener();
		for (JLabel lblStep : stepLabels) {
			lblStep.addMouseListener(listener);
		}

		lblStepImage.setIcon(IconLibrary.getDefault().getIcon("resources/images/step.gif"));

		pack();
	}

	public static MonitorTask show(Window owner, MonitorTask task) {
		MonitorNode node = ServerUtil.checkNode(ServerContext.getNodeService(), task.getNodeId());
		MonitorResource resource = null;
		if (task.getResourceId() != null)
			resource = ServerUtil.checkResource(ServerContext.getResourceService(), task.getResourceId());
		return show(owner, node, resource, task, 1);
	}

	public static MonitorTask show(Window owner, MonitorNode node) {
		return show(owner, node, null, new MonitorTask(), 0);
	}

	public static MonitorTask show(Window owner, MonitorResource resource) {
		if (resource.getNodeId() == null) {
			JOptionPane.showMessageDialog(owner, "此资源没有关联监测节点");
			return null;
		}
		MonitorNode node = ServerUtil.checkNode(ServerContext.getNodeService(), resource.getNodeId());
		return show(owner, node, resource, new MonitorTask(), 0);
	}

	private static MonitorTask show(Window owner, MonitorNode node, MonitorResource resource, MonitorTask task, int step) {
		TaskEditWindow window = new TaskEditWindow(owner);

		MonitorInstance[] instances = null;
		if (!"-1".equals(task.getId()))
			instances = ServerContext.getServerFactory().getTaskService().getInstancesByTaskId(task.getId());

		window.context = new MonitorConfigContext(ServerContext.getServerFactory(), node, resource, task, instances);
		window.minStep = step;
		if (window.setStep(step)) {
			WinUtil.toCenter(window);
			window.setVisible(true);
		} else
			window.dispose();
		return window.context == null ? null : window.context.getTask();
	}

	private class BtnPriorActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setStep(step - 1);
		}
	}

	private class BtnNextActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			setStep(step + 1);
		}
	}

	private class BtnCancelActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			context = null;
			TaskEditWindow.this.dispose();
		}
	}

	private class CbxStepMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			int index = -1;
			for (int i = minStep; i < stepLabels.length; i++) {
				if (e.getSource() == stepLabels[i]) {
					index = i;
					break;
				}
			}

			if (index < 0)
				return;

			switchStepPanel(index);
		}
	}

	private class BtnConfigNextActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			finish(false);
		}
	}

	private class TaskEditWizard implements Wizard {
		@Override
		public void next() {
			setStep(step + 1);
		}

		@Override
		public void prior() {
			setStep(step - 1);
		}
	}

	private boolean finish(boolean close) {
		try {
			context.getTask().setNodeId(context.getNode().getId());
			context.getTask().setResourceId(context.getResource() == null ? null : context.getResource().getId());
			String taskId = context.getServerFactory().getTaskService().saveTask(context.getTask(), context.getInstances());
			context.getTask().setId(taskId);
			if (close)
				TaskEditWindow.this.dispose();
			else  {
				for (int i = stepPanels.length - 1; i >= 0; i--) {
					panelStepContent.remove(stepPanels[i]);
					stepPanels[i] = null;
				}				
				context.setTask(new MonitorTask());
				setStep(0);
			}
			return true;
		} catch (Throwable e) {
			ErrorDlg.show("监测任务保存失败", e);
			return false;
		}
	}

	private boolean setStep(int value) {
		if (value < minStep)
			value = minStep;
		if (value > stepPanels.length)
			value = stepPanels.length;

		if (step == value)
			return false;

		return switchStepPanel(value);
	}

	private boolean switchStepPanel(int newStep) {
		if (step >= 0 && step < stepPanels.length && stepPanels[step] != null && newStep > step) {
			if (!stepPanels[step].getData())
				return false;
		}

		if (newStep == stepPanels.length)
			return finish(true);

		if (newStep == STEP_TASK_CONFIG && stepPanels[newStep] != null) {
			TaskConfigPanel taskConfigPanel = (TaskConfigPanel) stepPanels[newStep];
			if (taskConfigPanel.getType() != null && !taskConfigPanel.getType().getId().equals(context.getTask().getTypeId())) {
				stepPanels[newStep].setVisible(false);
				panelStepContent.remove(stepPanels[newStep]);
				stepPanels[newStep] = null;
			}
		}
		if (stepPanels[newStep] == null) {
			TaskEditStepPanel stepPanel;
			try {
				stepPanel = (TaskEditStepPanel) stepPanelClasses[newStep].newInstance();
			} catch (Throwable e) {
				throw ErrorUtil.createRuntimeException("建立向导面板失败", e);
			}
			stepPanel.setWizard(wizard);
			stepPanel.setVisible(true);
			if (newStep == STEP_TASK_TYPE_SELECT)
				getContentPane().add(stepPanel, BorderLayout.CENTER);
			else
				panelStepContent.add(stepPanel, BorderLayout.CENTER);

			try {
				stepPanel.setData(context);
			} catch (Throwable e) {
				ErrorDlg.show("切换配置界面失败", e);
				if (newStep == 0)
					getContentPane().remove(stepPanel);
				else
					panelStepContent.remove(stepPanel);
				return false;
			}

			stepPanels[newStep] = stepPanel;
		}
		
		boolean isLastStep = newStep + 1 == stepPanels.length;
		btnNext.setText(isLastStep ? "完成" : "下一步");
		btnConfigNext.setVisible(minStep == 0 && isLastStep);
		btnPrior.setVisible(newStep != minStep);

		for (int i = 0; i < stepPanels.length; i++) {
			if (stepPanels[i] != null)
				stepPanels[i].setVisible(newStep == i);
		}
		lblStepImage.setLocation(20, stepLabels[newStep].getY());
		panelStep.setVisible(newStep > 0);
		step = newStep;
		return true;
	}
}
