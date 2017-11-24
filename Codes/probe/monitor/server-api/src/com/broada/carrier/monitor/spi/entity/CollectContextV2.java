package com.broada.carrier.monitor.spi.entity;

import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;

public class CollectContextV2 extends CollectContext {
	private static final long serialVersionUID = 1L;

	private CollectResult result;

	public CollectResult getResult() {
		return result;
	}

	public void setResult(CollectResult result) {
		this.result = result;
	}

	public CollectContextV2(CollectContextV2 copy) {
		super(copy);
	}

	public CollectContextV2(CollectParams copy) {
		super(copy);
	}
	
	public CollectContextV2(MonitorContext copy) {
		super(copy);
	}

}
