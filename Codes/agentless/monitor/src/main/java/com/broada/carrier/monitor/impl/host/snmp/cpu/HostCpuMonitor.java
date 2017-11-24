package com.broada.carrier.monitor.impl.host.snmp.cpu;

import java.io.Serializable;
import java.math.BigDecimal;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpErrorUtil;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpProcessManager;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpProcessManagerFactory;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;

/**
 * 进程监测监测器实现
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: NMS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Broada
 * </p>
 * 
 * @author Maico Pang
 * @version 1.0
 */

public class HostCpuMonitor extends BaseMonitor {
	/**
	 * 需要提供snmpwalk的初始化数据
	 */
	public Serializable collect(CollectContext context) {
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			SnmpProcessManager mgr = SnmpProcessManagerFactory.getInstance(walk, context.getNode().getId());
			double val = 0.0f;
			if (mgr.getClass().isAssignableFrom(SnmpProcessManager.class)) {
				val = mgr.getAllProcCPU();
				Thread.sleep(10000);
				int nowVal = mgr.getAllProcCPU();
				int useVal = (int) (nowVal - val);
				int useTime = 10000;

				// 因为得出来的时间单位是百分之一秒,所以计算的时候要注意
				val = (double) useVal / useTime * 1000;
				val = new BigDecimal(val).setScale(2, 4).doubleValue();
				// 这个是迫不得以的,因为Snmp Agent不是实时的,所以有时候会可能超出100%
				if (val >= 100) {
					val = 91;
				} else if (val < 0) {
					val = 0;
				}
			} else {
				val = mgr.getCpuPercentage();
			}
			return new MonitorResult(new PerfResult("SNMP-HOSTCPU-1", val));
		} catch (InterruptedException e) {
			throw new MonitorException("执行过程被中断", e);
		} catch (SnmpException e) {
			throw SnmpErrorUtil.createError(e);
		} finally {
			walk.close();
		}
	}
}
