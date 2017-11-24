package com.broada.carrier.monitor.method.webspheremq;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.broada.carrier.monitor.method.common.BaseMethodConfiger;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;

public class WebSphereMQMethodConfiger extends BaseMethodConfiger {
	private static final long serialVersionUID = 1L;
	private JSpinner txtPort = new JSpinner();
	private JTextField txtCcsid;

	public WebSphereMQMethodConfiger() {
		setLayout(null);
		
		JLabel lblNewLabel = new JLabel("端口：");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(22, 10, 102, 15);
		add(lblNewLabel);
		
		txtPort.setBounds(135, 7, 102, 22);
		add(txtPort);
		
		JLabel lblcssid = new JLabel("字符集（CSSID）：");
		lblcssid.setHorizontalAlignment(SwingConstants.RIGHT);
		lblcssid.setBounds(22, 42, 102, 15);
		add(lblcssid);
		
		txtCcsid = new JTextField();
		txtCcsid.setBounds(135, 39, 138, 21);
		add(txtCcsid);
		txtCcsid.setColumns(10);
	}	

	@Override
	public boolean getData() {
		WebSphereMQMethod method = new WebSphereMQMethod();
		method.setPort((Integer)txtPort.getValue());
		method.setCcsId(txtCcsid.getText());		
		getContext().getMethod().set(method);
		return true;
	}

	@Override
	protected void setData(MonitorMethod method) {
		WebSphereMQMethod mm = new WebSphereMQMethod(method);
		txtPort.setValue(mm.getPort());
		txtCcsid.setText(mm.getCcsId());
	}
}
