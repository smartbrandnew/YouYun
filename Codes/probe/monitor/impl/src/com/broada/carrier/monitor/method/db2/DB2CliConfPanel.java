package com.broada.carrier.monitor.method.db2;

import com.broada.carrier.monitor.method.cli.CLIConfPanel;
import com.broada.carrier.monitor.spi.entity.MonitorMethodConfigContext;

public class DB2CliConfPanel extends CLIConfPanel {

	private static final long serialVersionUID = 1L;

	@Override
	public void setContext(MonitorMethodConfigContext context) {
		super.context = context;
		getjComboBoxSystem().addItem("AIX");
		getjComboBoxSystem().setSelectedIndex(0);
		getjComboBoxSystem().setEnabled(false);
	}

}
