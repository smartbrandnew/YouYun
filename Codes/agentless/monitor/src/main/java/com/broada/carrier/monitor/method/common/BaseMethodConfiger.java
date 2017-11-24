package com.broada.carrier.monitor.method.common;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorMethodConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;

public abstract class BaseMethodConfiger extends JPanel implements MonitorMethodConfiger {
	private static final long serialVersionUID = 1L;
	private MonitorMethodConfigContext context;
	
	public BaseMethodConfiger() {
		setPreferredSize(new Dimension(550, 250));
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void setData(MonitorMethodConfigContext context) {
		this.context = context;
		setData(context.getMethod());
	}

	protected MonitorMethodConfigContext getContext() {
		return context;
	}

	protected abstract void setData(MonitorMethod method);
}
