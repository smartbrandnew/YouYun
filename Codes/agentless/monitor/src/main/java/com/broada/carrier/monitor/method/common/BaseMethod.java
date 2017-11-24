package com.broada.carrier.monitor.method.common;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class BaseMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;
	private DefaultDynamicObject extraProperties;

	public BaseMethod() {
		super();
	}

	public BaseMethod(MonitorMethod copy) {
		super(copy);
	}

  @Override
	public String getExtra() {	
		if (extraProperties == null)
			return super.getExtra();
		else
			return SerializeUtil.encodeJson(extraProperties);
	}

	@Override
	public void setExtra(String extra) {		
		super.setExtra(extra);
		extraProperties = null;
	}

	@JsonIgnore
	public DefaultDynamicObject getExtraProperties() {
  	if (extraProperties == null) {
  		extraProperties = SerializeUtil.decodeJson(super.getExtra(), DefaultDynamicObject.class);
  		if (extraProperties == null)
  			extraProperties = new DefaultDynamicObject();
  	}
		return extraProperties;
	}

	public void setExtraProperties(DefaultDynamicObject extraProperties) {
		this.extraProperties = extraProperties;
	}
}
