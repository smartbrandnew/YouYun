package com.broada.carrier.monitor.spi.entity;

import java.util.Date;

import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;

public abstract class MonitorContext extends TestParams {
	private MonitorRecord record;
	private MonitorPolicy policy;

	public MonitorContext(MonitorNode node, MonitorResource resource, MonitorMethod method, MonitorTask task,
			MonitorInstance[] instances, MonitorPolicy policy, MonitorRecord record) {
		super(node, resource, method, task, instances);
		this.policy = policy;
		this.record = record;
	}

	public void setRecord(MonitorRecord record) {
		this.record = record;
	}

	public void setPolicy(MonitorPolicy policy) {
		this.policy = policy;
	}

	public MonitorContext(MonitorContext copy) {
		super(copy);
		this.policy = copy.policy;
		this.record = copy.record;
	}

	public MonitorContext(TestParams copy) {
		super(copy);
	}

	public abstract void setTempData(MonitorTempData data);

	public void setTempData(byte[] data) {
		setTempData(new MonitorTempData(getTask().getId(), new Date(), data));
	}

	public MonitorRecord getRecord() {
		return record;
	}

	public MonitorPolicy getPolicy() {
		return policy;
	}

	public abstract MonitorTempData getTempData();

	public void setTempData(Object data) {
		setTempData(SerializeUtil.encodeBytes(data));
	}

	public <T> T getTempData(Class<T> cls) {
		MonitorTempData temp = getTempData();
		if (temp == null)
			return null;
		return temp.getData(cls);
	}
}
