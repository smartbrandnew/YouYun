package com.broada.carrier.monitor.impl.db.st.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.impl.db.st.ShentongManager;
import com.broada.carrier.monitor.method.st.ShentongMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * Shentong基本配置监测类
 * 
 * @author Zhouqa
 * Create By 2016年4月12日 下午2:56:03
 */
public class ShentongBaseMonitor implements Monitor {
	private PerfResult[] asseblePerfResults(Map<String, Object> shentongBase) {
		List<PerfResult> perfs = new ArrayList<PerfResult>();
		for (int i = 0; i < ShentongBaseConfiger.keys.length; i++) {
			String value = String.valueOf(shentongBase.get(ShentongBaseConfiger.keys[i]));
			if (!("null".equalsIgnoreCase(value) || value.trim().length() < 1)) {
				perfs.add(new PerfResult("SHENTONG-BASE-" + (i + 1), value));
			}
		}
		return perfs.toArray(new PerfResult[0]);
	}

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		ShentongMethod shentongMethod = new ShentongMethod(context.getMethod());
		ShentongManager sm = new ShentongManager(context.getNode().getIp(), shentongMethod);
		try {
			sm.initConnection();
			Map<String, Object> shentongBase = sm.getShentongBaseInfo();
			result.setPerfResults(asseblePerfResults(shentongBase));
			return result;
		} catch (Exception e) {
			return result;
		}  finally {
			sm.close();
		}
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		ShentongMethod shentongMethod = new ShentongMethod(context.getMethod());
		ShentongManager sm = new ShentongManager(context.getNode().getIp(), shentongMethod);
		try {
			sm.initConnection();
			Map<String, Object> shentongBase = sm.getShentongBaseInfo();
			return new MonitorResult(asseblePerfResults(shentongBase));
		} catch (Exception e) {
			return new MonitorResult();
		} finally {
			sm.close();
		}
	}

}
