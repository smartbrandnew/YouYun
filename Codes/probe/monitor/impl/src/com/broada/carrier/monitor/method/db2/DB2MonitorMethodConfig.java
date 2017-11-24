package com.broada.carrier.monitor.method.db2;

import java.awt.Component;

import com.broada.carrier.monitor.spi.MonitorMethodConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;

/**
 * DB2MonitorMethodConfig
 * 
 * @author lixy (lixy@broada.com.cn)
 * Create By 2007-4-3 上午10:37:09
 */
public class DB2MonitorMethodConfig implements MonitorMethodConfiger {

  private DB2ConfPanel panel = new DB2ConfPanel();
  private MonitorMethodConfigContext context;

	@Override
	public boolean getData() {
		if (!panel.verify())
			return false;
		
		context.getMethod().set(panel.getOptions());
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
		panel.setHost(context.getNode().getIp());
		panel.setOptions(new DB2MonitorMethodOption(context.getMethod()));		
	}
}