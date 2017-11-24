package com.broada.carrier.monitor.client.impl.probe;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.client.impl.ServerContext;
import com.broada.carrier.monitor.common.swing.BeanEditPanel;
import com.broada.carrier.monitor.common.swing.BeanEditWindow;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import javax.swing.JSpinner;

public class ProbeEditPanel extends BeanEditPanel<MonitorProbe> {
	private static final long serialVersionUID = 1L;
	private JSpinner txtPort = new JSpinner();	
	private JTextField txtIp;
	private JTextField txtName;
	private JTextField txtCode;
	private JTextField txtDescr;
	private int probeId;

	public ProbeEditPanel() {
		setLayout(null);
		setPreferredSize(new Dimension(350, 165));

		JLabel lblIp = new JLabel("IP地址：");
		lblIp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblIp.setBounds(10, 69, 55, 15);
		add(lblIp);

		JLabel label = new JLabel("名称：");
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setBounds(10, 41, 55, 15);
		add(label);

		txtIp = new JTextField();
		txtIp.addFocusListener(new TxtIpFocusListener());
		txtIp.setBounds(71, 66, 159, 21);
		add(txtIp);
		txtIp.setColumns(10);

		txtName = new JTextField();
		txtName.setBounds(71, 38, 261, 21);
		add(txtName);
		txtName.setColumns(10);

		JLabel label_1 = new JLabel("端口：");
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
		label_1.setBounds(10, 100, 55, 15);
		add(label_1);
		
		txtCode = new JTextField();
		txtCode.setColumns(10);
		txtCode.setBounds(71, 10, 159, 21);
		add(txtCode);
		
		JLabel label_2 = new JLabel("编码：");
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		label_2.setBounds(10, 13, 55, 15);
		add(label_2);
		
		txtPort.setBounds(71, 97, 68, 22);
		txtPort.setValue(9145);
		add(txtPort);
		
		JLabel label_3 = new JLabel("说明：");
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
		label_3.setBounds(10, 128, 55, 15);
		add(label_3);
		
		txtDescr = new JTextField();
		txtDescr.setColumns(10);
		txtDescr.setBounds(71, 125, 261, 21);
		add(txtDescr);
	}	

	@Override
	public String getTitle() {
		return "监测探针";
	}

	@Override
	public MonitorProbe getData() {		
		MonitorProbe probe = new MonitorProbe();
		probe.setId(probeId);
		probe.setCode(txtCode.getText().trim());
		probe.setName(txtName.getText().trim());
		probe.setHost(txtIp.getText().trim());
		probe.setDescr(txtDescr.getText());
		probe.setPort((Integer)txtPort.getValue());
		probe.verify();		
		probe.setId(ServerContext.getProbeService().saveProbe(probe));		
		return probe;
	}

	@Override
	public void setData(MonitorProbe bean) {
		if (bean == null) 
			bean = new MonitorProbe();		
		probeId = bean.getId();
		txtCode.setText(bean.getCode());
		txtName.setText(bean.getName());
		txtIp.setText(bean.getHost());
		txtPort.setValue(bean.getPort());
		txtDescr.setText(bean.getDescr());
	}

	public static MonitorProbe show(Window owner, MonitorProbe probe) {		
		return BeanEditWindow.show(owner, new ProbeEditPanel(), probe);
	}

	public static MonitorProbe show(Window owner) {		
		return BeanEditWindow.show(owner, new ProbeEditPanel(), null);
	}

	private class TxtIpFocusListener extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			if (txtName.getText().isEmpty())
				txtName.setText(txtIp.getText());
		}
	}
}
