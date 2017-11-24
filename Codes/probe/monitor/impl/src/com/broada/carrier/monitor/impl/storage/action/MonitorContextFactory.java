package com.broada.carrier.monitor.impl.storage.action;

import com.broada.cid.action.api.entity.ActionMetadata;
import com.broada.cid.action.api.entity.ActionRequest;
import com.broada.cid.action.spi.action.ContextFactory;

public class MonitorContextFactory implements ContextFactory {
	@Override
	public Object createContext(ActionMetadata metadata, ActionRequest request, String code) {
		if (code.equalsIgnoreCase("result"))
			return new MonitorActionResult();
		else
			return null;
	}

	@Override
	public void destroyContext(ActionMetadata metadata, ActionRequest request, String code, Object context) {
	}

}
