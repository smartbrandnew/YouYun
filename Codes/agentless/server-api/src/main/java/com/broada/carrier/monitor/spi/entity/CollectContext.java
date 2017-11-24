package com.broada.carrier.monitor.spi.entity;

import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;

public class CollectContext extends CollectParams {
	private static final long serialVersionUID = 1L;
	private CollectResult result;

	public CollectResult getResult() {
		return result;
	}

	public void setResult(CollectResult result) {
		this.result = result;
	}

	public CollectContext(CollectContext copy) {
		super(copy);
	}

	public CollectContext(CollectParams copy) {
		super(copy);
	}

	public CollectContext(MonitorContext mc) {
		super(mc.getTask().getTypeId(), mc.getNode(), mc.getResource(), mc.getMethod(), mc.getInstances(), mc.getTask()
				.getParameter());
	}
}
