package com.broada.carrier.monitor.impl.common;

import java.awt.Component;
import java.io.Serializable;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.broada.carrier.monitor.common.swing.ErrorDlg;
import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.spi.MonitorConfiger;
import com.broada.carrier.monitor.spi.entity.MonitorConfigContext;

public class SpecificMonitorConfig extends JPanel implements MonitorConfiger {
	private static final long serialVersionUID = 1L;
	private MonitorConfigContext context;
	private MonitorMethod method;

	public SpecificMonitorConfig() {

	}

	@Override
	public boolean getData() {
		return true;
	}

	@Override
	public void setData(MonitorConfigContext data) {
		this.context = data;
		this.method = null;
	}

	@Override
	public Component getComponent() {
		return this;
	}

	protected MonitorConfigContext getContext() {
		return context;
	}

	/**
	 * 远程调用probe端的doCollect方法，然后调用到相应的monitor的doCollect
	 * @param monitorService
	 * @param monitorMethodParameterId
	 * @param callParams
	 * @return
	 * @throws CollectException
	 */
	protected CollectTaskSign commit(Serializable param) {
		try {
			if (getTask().getMethodCode() == null && context.getType().retNeedMethod()) {
				JOptionPane.showMessageDialog(this, "请选择监测方法");
				return null;
			}
			return getServerFactory().getTaskService().commitTask(
					new CollectParams(getTask().getTypeId(), context.getNode(), context.getResource(), getMethod(), context
							.getInstances(), param));

		} catch (Throwable e) {
			ErrorDlg.show("监测信息采集失败", e);
			return null;
		}
	}

	protected CollectResult getCollectResult(String nodeId, String taskId) {
		return getServerFactory().getTaskService().getCollectResult(nodeId, taskId);
	}

	protected CollectTaskSign commit() {
		return commit(null);
	}

	protected void cancelCollect(String nodeId, String taskId) {
		getServerFactory().getTaskService().cancelCollect(nodeId, taskId);
	}

	@Override
	public void setMethod(MonitorMethod method) {
		this.method = method;
		getTask().setMethodCode(method.getCode());
		refresh();
	}

	protected MonitorMethod getMethod() {
		if (method == null) {
			if (getTask().getMethodCode() == null)
				return null;
			method = getServerFactory().getMethodService().getMethod(getTask().getMethodCode());
		}
		return method;
	}

	protected ServerServiceFactory getServerFactory() {
		return getContext().getServerFactory();
	}

	protected MonitorTask getTask() {
		return getContext().getTask();
	}

	protected void refresh() {
	}
}
