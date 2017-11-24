package com.broada.carrier.monitor.method.cli;

import java.awt.Component;

import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.cli.entity.WmiMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorMethodConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;

public class CLIMethodConfiger implements MonitorMethodConfiger {
	private CLIConfPanel cliPanel = new CLIConfPanel();
	private WmiConfPanel wmiPanel = new WmiConfPanel();
	private MonitorMethodConfigContext context;

	@Override
	public boolean getData() {
		MonitorMethod method = context.getMethod();
		String typeId = method.getTypeId();
		if (WmiMonitorMethodOption.TYPE_ID.equals(typeId)) {
			if (!wmiPanel.verify())
				return false;
			method.set(wmiPanel.getOptions());
		} else if (CLIMonitorMethodOption.TYPE_ID.equals(typeId)) {
			if (!cliPanel.verify())
				return false;
			method.set(cliPanel.getOptions());
		}
		context.getMethod().set(method);
		return true;
	}

	@Override
	public Component getComponent() {
		MonitorMethod method = context.getMethod();
		String typeId = method.getTypeId();
		if (WmiMonitorMethodOption.TYPE_ID.equals(typeId)) {
			return wmiPanel;
		} else if (CLIMonitorMethodOption.TYPE_ID.equals(typeId)) {
			return cliPanel;
		}
		return null;
	}

	@Override
	public void setData(MonitorMethodConfigContext context) {
		this.context = context;
		MonitorMethod method = context.getMethod();
		String typeId = method.getTypeId();
		if (WmiMonitorMethodOption.TYPE_ID.equals(typeId)) {
			wmiPanel.setContext(context);
			wmiPanel.setOptions(new WmiMonitorMethodOption(method));
		} else if (CLIMonitorMethodOption.TYPE_ID.equals(typeId)) {
			cliPanel.setContext(context);
			cliPanel.setOptions(new CLIMonitorMethodOption(method));
		}
	}
}
