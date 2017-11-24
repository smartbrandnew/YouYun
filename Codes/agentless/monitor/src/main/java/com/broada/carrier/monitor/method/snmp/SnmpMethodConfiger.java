package com.broada.carrier.monitor.method.snmp;

import java.awt.Component;

import com.broada.carrier.monitor.spi.MonitorMethodConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;

public class SnmpMethodConfiger implements MonitorMethodConfiger {
	private SnmpBaseConfigPanel panel = new SnmpBaseConfigPanel();
	private MonitorMethodConfigContext context;
	
	@Override
	public boolean getData() {
		SnmpMethod method = panel.getParameter();
		if (method == null)
			return false;
		context.getMethod().set(method);
		return true; 
	}

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public void setData(MonitorMethodConfigContext context) {
		this.context = context;
		panel.setContext(context);		
		panel.setParameter(new SnmpMethod(context.getMethod()));
	}
}
