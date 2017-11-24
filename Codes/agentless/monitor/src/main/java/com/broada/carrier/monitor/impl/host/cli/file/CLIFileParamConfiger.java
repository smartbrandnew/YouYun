package com.broada.carrier.monitor.impl.host.cli.file;

import java.awt.Component;

import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

public class CLIFileParamConfiger implements MonitorConfiger {
  private CLIFileConfig panel = new CLIFileConfig();

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public boolean getData() {
		return panel.getData();		
	}

	@Override
	public void setData(MonitorConfigContext data) {
		panel.setData(data);
		if (data.getTask().getMethodCode() != null)
			setMethod(ServerUtil.checkMethod(data.getServerFactory().getMethodService(), data.getTask().getMethodCode()));
	}

	@Override
	public void setMethod(MonitorMethod method) {
		panel.setMethod(method);
	}
}
