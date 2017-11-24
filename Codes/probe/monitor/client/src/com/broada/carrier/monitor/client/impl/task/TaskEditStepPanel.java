package com.broada.carrier.monitor.client.impl.task;

import java.awt.Dimension;

import javax.swing.JPanel;

import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

public abstract class TaskEditStepPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Wizard wizard;

	public TaskEditStepPanel() {
		setPreferredSize(new Dimension(750, 450));
	}

	public void setWizard(Wizard wizard) {
		this.wizard = wizard;
	}

	public void finish() {
		if (getData())
			wizard.next();
	}

	public abstract boolean getData();

	public abstract void setData(MonitorConfigContext data);
}
