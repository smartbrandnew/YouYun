package com.broada.carrier.monitor.impl.host.snmp.info;

import java.io.Serializable;
import java.util.Map;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpErrorUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.snmputil.SnmpException;
import com.broada.utils.StringUtil;

/**
 * 
 * @author lixy (lixy@broada.com.cn)
 * Create By 2007-12-2 下午05:16:29
 */
public class SnmpHostInfoMonitor extends BaseMonitor {
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		WinSnmpHostInfoMgr hostInfoMgr = new WinSnmpHostInfoMgr(context.getNode().getIp(), snmpMethod);
		try {
			hostInfoMgr.initSnmpWalk();
			long replyTime = System.currentTimeMillis();
			Map hostInfo = hostInfoMgr.generateHostInfo();
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			for (int i = 0; i < HostBaseInfo.keys.length; i++) {
				Object value = hostInfo.get(HostBaseInfo.keys[i]);
				if (value == null || StringUtil.isNullOrBlank(value.toString()) || "null".equalsIgnoreCase(value.toString()))
					continue;
				if("noSuchObject".equalsIgnoreCase(value.toString())){
        	hostInfo.clear();
        	throw new Exception("该设备不支持相关监测");
        }
                
				result.addPerfResult(new PerfResult("SNMP-HOSTINFO-" + (i + 1), value));
			}
		} catch (SnmpException e) {
			throw SnmpErrorUtil.createError(e);
		} catch (Exception e) {
			throw ErrorUtil.createRuntimeException("主机基本信息采集失败", e);
		} finally {
			hostInfoMgr.close();
		}
		return result;
	}

}
