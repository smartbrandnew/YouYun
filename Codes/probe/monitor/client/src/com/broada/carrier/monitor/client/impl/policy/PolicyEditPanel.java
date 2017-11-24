package com.broada.carrier.monitor.client.impl.policy;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.swing.BeanEditPanel;
import com.broada.carrier.monitor.common.swing.BeanEditWindow;
import com.broada.carrier.monitor.common.swing.table.BaseTable;
import com.broada.carrier.monitor.common.swing.table.BaseTableColumn;
import com.broada.carrier.monitor.common.swing.table.BeanTableModel;
import com.broada.carrier.monitor.common.swing.table.DateTableCellRenderer;
import com.broada.carrier.monitor.common.util.TimeRange;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.TimeRangeInfo;

public class PolicyEditPanel extends BeanEditPanel<MonitorPolicy> {
	private static final long serialVersionUID = 1L;

	private JTextField txtCode = new JTextField();
	private JTextField txtName = new JTextField();
	private JTextField txtDescr = new JTextField();
	private JCheckBox chkWorkWeekDay1 = new JCheckBox("周一");
	private JCheckBox chkWorkWeekDay2 = new JCheckBox("周二");
	private JCheckBox chkWorkWeekDay3 = new JCheckBox("周三");
	private JCheckBox chkWorkWeekDay4 = new JCheckBox("周四");
	private JCheckBox chkWorkWeekDay5 = new JCheckBox("周五");
	private JCheckBox chkWorkWeekDay6 = new JCheckBox("周六");
	private JCheckBox chkWorkWeekDay0 = new JCheckBox("周日");
	private JCheckBox[] chkWorkWeekDays = new JCheckBox[] {
			chkWorkWeekDay0,
			chkWorkWeekDay1,
			chkWorkWeekDay2,
			chkWorkWeekDay3,
			chkWorkWeekDay4,
			chkWorkWeekDay5,
			chkWorkWeekDay6,
	};
	private TimeRangeTableModel tableModelStopTimeRanges = new TimeRangeTableModel();
	private BaseTable tableStopTimeRanges = new BaseTable(tableModelStopTimeRanges);
	private JSpinner txtInterval = new JSpinner();
	private JSpinner txtErrorInterval = new JSpinner();
	private JComboBox cbxIntervalUnit = new JComboBox();
	private JComboBox cbxErrorIntervalUnit = new JComboBox();
	private SpinnerDateModel dmWorkTimeStart = new SpinnerDateModel();
	private SpinnerDateModel dmWorkTimeEnd = new SpinnerDateModel();
	private JSpinner txtWorkTimeStart = new JSpinner(dmWorkTimeStart);
	private JSpinner txtWorkTimeEnd = new JSpinner(dmWorkTimeEnd);

	public PolicyEditPanel() {
		setPreferredSize(new Dimension(570, 483));
		setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "基本信息", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(10, 10, 549, 177);
		panel.setLayout(null);
		add(panel);

		JLabel lblNewLabel = new JLabel("编码：");
		lblNewLabel.setBounds(10, 26, 36, 15);
		panel.add(lblNewLabel);

		txtCode.setBounds(50, 23, 208, 21);
		txtCode.setColumns(10);
		panel.add(txtCode);

		JLabel label = new JLabel("名称：");
		label.setBounds(10, 57, 36, 15);
		panel.add(label);

		txtName.setBounds(50, 54, 208, 21);
		txtName.setColumns(10);
		panel.add(txtName);

		JLabel label_3 = new JLabel("描述：");
		label_3.setBounds(10, 88, 36, 15);
		panel.add(label_3);

		txtDescr.setColumns(10);
		txtDescr.setBounds(50, 85, 489, 21);
		panel.add(txtDescr);

		JLabel label_1 = new JLabel("监测正常时，每隔");
		label_1.setBounds(10, 116, 103, 15);
		panel.add(label_1);

		txtInterval.setBounds(115, 113, 80, 22);
		panel.add(txtInterval);

		cbxIntervalUnit.setBounds(205, 113, 53, 21);
		panel.add(cbxIntervalUnit);
		cbxIntervalUnit.setModel(new DefaultComboBoxModel(TimeUnit.values()));
		cbxIntervalUnit.setSelectedIndex(1);

		JLabel label_6 = new JLabel("监测一次");
		label_6.setBounds(267, 116, 103, 15);
		panel.add(label_6);

		JLabel label_7 = new JLabel("监测一次");
		label_7.setBounds(267, 148, 103, 15);
		panel.add(label_7);

		cbxErrorIntervalUnit.setBounds(204, 145, 53, 21);
		panel.add(cbxErrorIntervalUnit);
		cbxErrorIntervalUnit.setModel(new DefaultComboBoxModel(TimeUnit.values()));
		cbxErrorIntervalUnit.setSelectedIndex(1);

		txtErrorInterval.setBounds(114, 145, 80, 22);
		panel.add(txtErrorInterval);

		JLabel label_2 = new JLabel("监测异常时，每隔");
		label_2.setBounds(10, 148, 100, 15);
		panel.add(label_2);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "监测时段", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(10, 185, 549, 290);
		add(panel_1);
		panel_1.setLayout(null);

		JLabel label_4 = new JLabel("在一周中的以下日期进行监测：");
		label_4.setBounds(10, 83, 185, 15);
		panel_1.add(label_4);

		JLabel label_5 = new JLabel("至");
		label_5.setBounds(262, 58, 21, 15);
		panel_1.add(label_5);

		txtWorkTimeStart.setBounds(113, 55, 139, 22);
		txtWorkTimeStart.setEditor(new JSpinner.DateEditor(txtWorkTimeStart, MonitorPolicy.WORK_TIME_FORMAT));
		panel_1.add(txtWorkTimeStart);

		txtWorkTimeEnd.setBounds(282, 55, 139, 22);
		txtWorkTimeEnd.setEditor(new JSpinner.DateEditor(txtWorkTimeEnd, MonitorPolicy.WORK_TIME_FORMAT));
		panel_1.add(txtWorkTimeEnd);

		chkWorkWeekDay1.setBounds(22, 105, 53, 23);
		panel_1.add(chkWorkWeekDay1);

		chkWorkWeekDay2.setBounds(97, 105, 53, 23);
		panel_1.add(chkWorkWeekDay2);

		chkWorkWeekDay3.setBounds(172, 105, 53, 23);
		panel_1.add(chkWorkWeekDay3);

		chkWorkWeekDay4.setBounds(247, 105, 53, 23);
		panel_1.add(chkWorkWeekDay4);

		chkWorkWeekDay5.setBounds(322, 105, 53, 23);
		panel_1.add(chkWorkWeekDay5);

		chkWorkWeekDay6.setBounds(397, 105, 53, 23);
		panel_1.add(chkWorkWeekDay6);

		chkWorkWeekDay0.setBounds(472, 105, 53, 23);
		panel_1.add(chkWorkWeekDay0);

		JLabel label_8 = new JLabel("在以下时间段停止监测（右键可添加修改）：");
		label_8.setBounds(10, 134, 250, 15);
		panel_1.add(label_8);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 159, 529, 121);
		panel_1.add(scrollPane);

		JPopupMenu popupMenu = new JPopupMenu();
		addPopup(scrollPane, popupMenu);
		addPopup(tableStopTimeRanges, popupMenu);

		JMenuItem mnuStopCreate = new JMenuItem("添加");
		mnuStopCreate.addActionListener(new MnuStopCreateActionListener());
		popupMenu.add(mnuStopCreate);

		JMenuItem mnuStopEdit = new JMenuItem("修改");
		mnuStopEdit.addActionListener(new MnuStopEditActionListener());
		popupMenu.add(mnuStopEdit);

		JMenuItem mnuStopDelete = new JMenuItem("删除");
		mnuStopDelete.addActionListener(new MnuStopDeleteActionListener());
		popupMenu.add(mnuStopDelete);

		scrollPane.setViewportView(tableStopTimeRanges);

		JLabel label_9 = new JLabel("在一天中的以下时段进行监测：");
		label_9.setBounds(10, 30, 185, 15);
		panel_1.add(label_9);

		dmWorkTimeStart.setStart(MonitorPolicy.WORK_TIME_MIN);
		dmWorkTimeStart.setEnd(MonitorPolicy.WORK_TIME_MAX);
		dmWorkTimeEnd.setStart(MonitorPolicy.WORK_TIME_MIN);
		dmWorkTimeEnd.setEnd(MonitorPolicy.WORK_TIME_MAX);
	}

	private static class TimeRangeTableModel extends BeanTableModel<TimeRangeInfo> {
		private static final long serialVersionUID = 1L;
		private static final BaseTableColumn[] columns = new BaseTableColumn[] {
				new BaseTableColumn("start", "开始时间", new DateTableCellRenderer()),
				new BaseTableColumn("end", "结束时间", new DateTableCellRenderer()),
				new BaseTableColumn("descr", "描述"),
		};

		public TimeRangeTableModel() {
			super(columns);
		}
	}

	public static MonitorPolicy show(Window owner) {
		return show(owner, null);
	}

	public static MonitorPolicy show(Window owner, MonitorPolicy policy) {
		return BeanEditWindow.show(owner, new PolicyEditPanel(), policy);
	}

	@Override
	public MonitorPolicy getData() {
		MonitorPolicy policy = new MonitorPolicy();
		policy.setCode(txtCode.getText().trim());
		policy.setName(txtName.getText().trim());
		policy.setDescr(txtDescr.getText().trim());
		policy.setInterval(getInterval(txtInterval, cbxIntervalUnit));
		policy.setErrorInterval(getInterval(txtErrorInterval, cbxErrorIntervalUnit));
		policy.putWorkTimeRange(new TimeRange(dmWorkTimeStart.getDate(), dmWorkTimeEnd.getDate()));
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chkWorkWeekDays.length; i++) {
			if (chkWorkWeekDays[i].isSelected())
				sb.append(Integer.toString(i));
		}
		policy.setWorkWeekDays(sb.toString());
		policy.putStopTimeRanges(tableModelStopTimeRanges.getRows().toArray(new TimeRangeInfo[0]));
		policy.verify();		
		ServerContext.getPolicyService().savePolicy(policy);
		return policy;
	}

	@Override
	public void setData(MonitorPolicy policy) {
		txtCode.setEditable(policy == null);
		if (policy == null)
			policy = new MonitorPolicy();		
		txtCode.setText(policy.getCode());
		txtName.setText(policy.getName());
		txtDescr.setText(policy.getDescr());
		setInterval(txtInterval, cbxIntervalUnit, policy.getInterval());
		setInterval(txtErrorInterval, cbxErrorIntervalUnit, policy.getErrorInterval());
		dmWorkTimeStart.setValue(new Date(policy.retWorkTimeRange().getStart()));
		dmWorkTimeEnd.setValue(new Date(policy.retWorkTimeRange().getEnd()));
		for (int i = 0; i < chkWorkWeekDays.length; i++)
			chkWorkWeekDays[i].setSelected(policy.retWorkWeekDaysContains(i));
		tableModelStopTimeRanges.setRows(policy.retStopTimeRanges());
	}

	private static int getInterval(JSpinner value, JComboBox unit) {
		int num = (Integer) value.getValue();
		return ((TimeUnit) unit.getSelectedItem()).getSeconds() * num;
	}

	private static void setInterval(JSpinner value, JComboBox unit, int seconds) {
		TimeUnit perfectUnit = TimeUnit.getPerfectUnit(seconds);
		value.setValue(seconds / perfectUnit.getSeconds());
		unit.setSelectedItem(perfectUnit);
	}

	public static void main(String[] args) {
		show(null);
	}

	@Override
	public String getTitle() {
		return "监测策略编辑";
	}

	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) 
					editStopTimeRange();				
			}
		});
	}

	private class MnuStopCreateActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			TimeRangeInfo info = TimeRangeInfoEditPanel.show(getWindow());
			if (info != null)
				tableModelStopTimeRanges.addRow(info);
		}
	}

	private TimeRangeInfo getSelected() {
		int index = tableStopTimeRanges.getSelectedRow();
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "必须选择一行停用时段");
			return null;
		}

		return tableModelStopTimeRanges.getRows().get(index);
	}
	
	private void editStopTimeRange() {
		TimeRangeInfo info = getSelected();
		if (info != null) {
			TimeRangeInfo newInfo = TimeRangeInfoEditPanel.show(getWindow(), info);
			if (newInfo != null) {
				info.set(newInfo);
				tableModelStopTimeRanges.fireTableDataChanged();
			}
		}
	}

	private class MnuStopEditActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			editStopTimeRange();
		}
	}

	private class MnuStopDeleteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			TimeRangeInfo info = getSelected();
			if (info != null)
				tableModelStopTimeRanges.removeRow(info);
		}
	}
}
