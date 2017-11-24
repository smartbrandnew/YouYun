package com.broada.carrier.monitor.client.impl.task.step;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;

import com.broada.carrier.monitor.client.impl.config.Config;
import com.broada.carrier.monitor.client.impl.task.TaskEditStepPanel;
import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

public class TaskTestPanel extends TaskEditStepPanel {
	private static final long serialVersionUID = 1L;
	private MonitorConfigContext context;
	private JTextArea txtMessage = new JTextArea();
	private JTextArea txtSummary = new JTextArea();
	private JLabel lblState = new JLabel("");

	public TaskTestPanel() {

		JLabel lblNewLabel = new JLabel("任务汇总：");

		JScrollPane scrollPane = new JScrollPane();

		JLabel label = new JLabel("监测状态：");

		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));

		JLabel label_1 = new JLabel("监测信息：");

		JScrollPane scrollPane_1 = new JScrollPane();

		JButton btnTest = new JButton("测试");
		btnTest.addActionListener(new BtnTestActionListener());
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								groupLayout
										.createParallelGroup(Alignment.LEADING)
										.addGroup(
												groupLayout
														.createSequentialGroup()
														.addComponent(lblNewLabel)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 493,
																Short.MAX_VALUE))
										.addGroup(
												groupLayout
														.createSequentialGroup()
														.addComponent(label)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(panel, GroupLayout.PREFERRED_SIZE, 319,
																GroupLayout.PREFERRED_SIZE))
										.addComponent(btnTest, Alignment.TRAILING)
										.addGroup(
												Alignment.TRAILING,
												groupLayout
														.createSequentialGroup()
														.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 60,
																GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 493,
																Short.MAX_VALUE))).addGap(18)));
		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(
				groupLayout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								groupLayout
										.createParallelGroup(Alignment.LEADING)
										.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 135,
												GroupLayout.PREFERRED_SIZE).addComponent(lblNewLabel))
						.addGap(17)
						.addGroup(
								groupLayout
										.createParallelGroup(Alignment.LEADING, false)
										.addComponent(label, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(panel, GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE))
						.addGap(18)
						.addGroup(
								groupLayout.createParallelGroup(Alignment.LEADING).addComponent(label_1)
										.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE))
						.addGap(18).addComponent(btnTest).addGap(11)));
		panel.setLayout(new BorderLayout(0, 0));

		panel.add(lblState, BorderLayout.CENTER);
		txtMessage.setLineWrap(true);

		scrollPane_1.setViewportView(txtMessage);
		txtSummary.setLineWrap(true);

		scrollPane.setViewportView(txtSummary);
		txtSummary.setTabSize(4);
		txtSummary.setEditable(false);
		setLayout(groupLayout);
	}

	@Override
	public boolean getData() {
		return true;
	}

	@Override
	public void setData(MonitorConfigContext context) {
		this.context = context;
		setSummary();
		if ("-1".equals(context.getTask().getId())) {
			MonitorRecord record = context.getServerFactory().getTaskService().getRecord(context.getTask().getId());
			setState(record.getState());
			txtMessage.setText(record.getMessage());
		} else {
			setState(MonitorState.UNMONITOR);
			txtMessage.setText("");
		}
	}

	private void setState(MonitorState state) {
		lblState.setIcon(Config.getDefault().getIcon(state));
		lblState.setText(state.getDescr());
	}

	private void setSummary() {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("监测节点：").append(context.getNode().retDisplayName()).append('\n');
			if (context.getResource() != null)
				sb.append("监测资源：").append(context.getNode().retDisplayName()).append('\n');
			sb.append("任务类型：").append(context.getType().getName()).append('\n');
			sb.append("任务名称：").append(context.getTask().getName()).append('\n');
			MonitorPolicy policy = ServerUtil.checkPolicy(context.getServerFactory().getPolicyService(), context
					.getTask().getPolicyCode());
			sb.append("监测策略：").append(policy.retDisplayName()).append('\n');
		} catch (Throwable e) {
			ErrorDlg.show("监测信息汇总生成失败", e);
		}
		txtSummary.setText(sb.toString());
	}

	private void setData(MonitorResult result) {
		setState(result.getState());
		txtMessage.setText(result.getMessage());
	}

	private class BtnTestActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				TestParams params = new TestParams(context.getNode(), context.getResource(), null, context.getTask(),
						context.getInstances());
				if (context.getTask().getMethodCode() != null)
					params.setMethod(ServerUtil.checkMethod(context.getServerFactory().getMethodService(), context
							.getTask().getMethodCode()));
				MonitorResult result = context.getServerFactory().getTaskService().testTask(params);
				setData(result);
			} catch (Throwable error) {
				ErrorDlg.show("测试失败", error);
			}
		}
	}
}
