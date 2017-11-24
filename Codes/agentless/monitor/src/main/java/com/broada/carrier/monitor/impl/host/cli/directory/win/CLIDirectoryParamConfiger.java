package com.broada.carrier.monitor.impl.host.cli.directory.win;

import java.awt.Component;

import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

/**
 * Windows目录监测参数配置器
 * 
 * @author lixy Jun 16, 2008 11:28:12 AM
 */
public class CLIDirectoryParamConfiger implements MonitorConfiger {
  private CLIDirectoryParamPanel panel = new CLIDirectoryParamPanel();

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
