package com.broada.carrier.monitor.client.impl.policy;

import java.awt.Dimension;
import java.awt.Window;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.common.swing.BeanEditPanel;
import com.broada.carrier.monitor.common.swing.BeanEditWindow;
import com.broada.carrier.monitor.common.util.TimeRange;
import com.broada.carrier.monitor.server.api.entity.TimeRangeInfo;
import com.broada.component.utils.text.DateUtil;

public class TimeRangeInfoEditPanel extends BeanEditPanel<TimeRangeInfo> {
	private static final long serialVersionUID = 1L;
	private SpinnerDateModel dmStart = new SpinnerDateModel();
	private SpinnerDateModel dmEnd = new SpinnerDateModel();	
	private JSpinner txtStart = new JSpinner(dmStart);
	private JSpinner txtEnd = new JSpinner(dmEnd);
	private JTextField txtDescr = new JTextField();
	
	public TimeRangeInfoEditPanel() {
		setLayout(null);	
		setPreferredSize(new Dimension(418, 120));
		
		JLabel label = new JLabel("起始时间：");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(13, 25, 73, 15);
		add(label);
		
		txtStart.setBounds(96, 21, 149, 22);
		txtStart.setEditor(new JSpinner.DateEditor(txtStart, DateUtil.PATTERN_YYYYMMDD_HHMMSS));
		add(txtStart);
		
		JLabel label_1 = new JLabel("结束时间：");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		label_1.setBounds(13, 54, 73, 15);
		add(label_1);
		
		txtEnd.setBounds(96, 50, 149, 22);
		txtEnd.setEditor(new JSpinner.DateEditor(txtEnd, DateUtil.PATTERN_YYYYMMDD_HHMMSS));
		add(txtEnd);
		
		JLabel lblNewLabel = new JLabel("描述：");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(13, 83, 73, 15);
		add(lblNewLabel);
				
		txtDescr.setBounds(96, 80, 302, 21);
		add(txtDescr);
		txtDescr.setColumns(10);
	}

	@Override
	public String getTitle() {
		return "停用时段编辑";
	}

	@Override
	public TimeRangeInfo getData() {
		return new TimeRangeInfo(
				dmStart.getDate(),
				dmEnd.getDate(),
				txtDescr.getText().trim());
	}

	@Override
	public void setData(TimeRangeInfo bean) {
		if (bean == null) {
			TimeRange today = TimeRange.getToday();
			bean = new TimeRangeInfo(new Date(today.getStart()), new Date(today.getEnd()), null);
		}
		
		dmStart.setValue(new Date(bean.getStart()));
		dmEnd.setValue(new Date(bean.getEnd()));
		txtDescr.setText(bean.getDescr());
	}

	public static TimeRangeInfo show(Window owner) {
		return BeanEditWindow.show(owner, new TimeRangeInfoEditPanel(), null);
	}
	
	public static TimeRangeInfo show(Window owner, TimeRangeInfo info) {
		return BeanEditWindow.show(owner, new TimeRangeInfoEditPanel(), info);
	}
}
