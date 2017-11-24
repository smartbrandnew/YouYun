package com.broada.carrier.monitor.impl.storage.netapp.info;

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpErrorUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.utils.StringUtil;

/**
 * 
 * @author shoulw (shoulw@broada.com.cn) Create By 2016-5-9 下午02:16:29
 */
public class SnmpHostInfoMonitor extends BaseMonitor {
	private static final Log logger = LogFactory
			.getLog(SnmpHostInfoMonitor.class);

	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			SnmpHostInfoMgr hostInfoMgr = new SnmpHostInfoMgr(walk);
			logger.warn("hostInfoMgr初始化snmpwalk完成！");
			long replyTime = System.currentTimeMillis();
			Map hostInfo = hostInfoMgr.generateHostInfo();
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			for (int i = 0; i < HostBaseInfo.keys.length; i++) {
				Object value = hostInfo.get(HostBaseInfo.keys[i]);
				if (value == null || StringUtil.isNullOrBlank(value.toString())
						|| "null".equalsIgnoreCase(value.toString()))
					continue;
				if ("noSuchObject".equalsIgnoreCase(value.toString())) {
					hostInfo.clear();
					throw new Exception("该设备不支持相关监测");
				}
				logger.warn("NETAPP-BASEINFO-" + (i + 1) + ":" + value);

				result.addPerfResult(new PerfResult("NETAPP-BASEINFO-"
						+ (i + 1), value));
			}
		} catch (SnmpException e) {
			throw SnmpErrorUtil.createError(e);
		} catch (Exception e) {
			throw ErrorUtil.createRuntimeException("主机基本信息采集失败", e);
		} finally {
			walk.close();
		}
		return result;
	}

}
