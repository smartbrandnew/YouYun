package com.broada.carrier.monitor.method.storage;

import java.awt.Component;

import com.broada.carrier.monitor.method.cli.CLIConfPanel;
import com.broada.carrier.monitor.method.cli.entity.CLIMonitorMethodOption;
import com.broada.carrier.monitor.method.smis.SmisConfigPanel;
import com.broada.carrier.monitor.method.smis.SmisMethod;
import com.broada.carrier.monitor.method.snmp.SnmpBaseConfigPanel;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.spi.MonitorMethodConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;

public class StorageMethodConfig implements MonitorMethodConfiger {
	private CLIConfPanel cliPanel = new CLIConfPanel();
	private SnmpBaseConfigPanel snmpPanel = new SnmpBaseConfigPanel();
	private SmisConfigPanel smisPanel = new SmisConfigPanel();
	MonitorMethodConfigContext context = null;
	

	public StorageMethodConfig() {
	}

	@Override
	public boolean getData() {
		MonitorMethod method = context.getMethod();
		String typeId = method.getTypeId();
		if (SmisMethod.TYPE_ID.equals(typeId)) {
			if (!smisPanel.verify()) {
				return false;
			}
			method.set(smisPanel.getOptions());
		} else if (SnmpMethod.TYPE_ID.equals(typeId)) {
			if (!snmpPanel.verify()) {
				return false;
			}
			method.set(snmpPanel.getParameter());
		} else if (CLIMonitorMethodOption.TYPE_ID.equals(typeId)) {
			if (!cliPanel.verify()) {
				return false;
			}
			method.set(cliPanel.getOptions());
		}
		return true;
	}

	@Override
	public void setData(MonitorMethodConfigContext context) {
		this.context = context;
		MonitorMethod method = context.getMethod();
		String typeId = method.getTypeId();
		if (SmisMethod.TYPE_ID.equals(typeId)) {
			smisPanel.setOption(new SmisMethod(method));
			smisPanel.setContext(context);
		} else if (SnmpMethod.TYPE_ID.equals(typeId)) {
			snmpPanel.setParameter(new SnmpMethod(method));
			snmpPanel.setContext(context);
		} else {
			cliPanel.setOptions(new CLIMonitorMethodOption(method));
			cliPanel.setContext(context);
		}
		
	}

	@Override
	public Component getComponent() {
		MonitorMethod method = context.getMethod();
		String typeId = method.getTypeId();
		if (SmisMethod.TYPE_ID.equals(typeId)) {
			return smisPanel;
		} else if (SnmpMethod.TYPE_ID.equals(typeId)) {
			return snmpPanel;
		} else if (CLIMonitorMethodOption.TYPE_ID.equals(typeId)) {
			return cliPanel;
		}
		return null;
	}  	
}
