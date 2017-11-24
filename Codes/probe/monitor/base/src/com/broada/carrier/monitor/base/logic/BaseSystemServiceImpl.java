package com.broada.carrier.monitor.base.logic;

import java.util.Date;

import com.broada.carrier.monitor.common.util.ObjectUtil;
import com.broada.carrier.monitor.server.api.service.BaseSystemService;

public abstract class BaseSystemServiceImpl implements BaseSystemService {

	@Override
	public Date getTime() {
		return new Date();
	}
	
	@Override
	public Object executeMethod(String className, String methodName, Object... params) {		
		// TODO 缺少超时的控制
		return ObjectUtil.executeMethod(className, methodName, params);
	}
}
