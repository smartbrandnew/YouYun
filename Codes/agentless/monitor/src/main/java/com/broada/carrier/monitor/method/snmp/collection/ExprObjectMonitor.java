package com.broada.carrier.monitor.method.snmp.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.device.ifstatus.IfstatusMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.InstanceDiscoverException;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.snmp.SnmpableInstance;
import com.broada.carrier.monitor.method.snmp.collection.perfmon.discover.snmp.SnmpableInstanceDiscover;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.snmp.SnmpGet;
import com.broada.snmp.SnmpUtil;
import com.broada.snmputil.SnmpTarget;

/**
 * 实例使用率监测器实现
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

public abstract class ExprObjectMonitor implements Monitor {

	private static final Log logger = LogFactory.getLog(ExprObjectMonitor.class);

	protected abstract String getItem();
	
	protected abstract PerfType getPerfType();

	public ExprObjectMonitor() {
	}

	public MonitorResult monitor(MonitorContext context) {
		MonitorResult rs = (MonitorResult) collect(new CollectContext(context));
		List<MonitorInstance> instanceList = new ArrayList<MonitorInstance>();
		if (rs.getRows() != null) {
			for (MonitorResultRow row : rs.getRows()) {
				instanceList.add(row.retInstance());
			}
		}
		MonitorInstance[] instanceArray = instanceList.toArray(new MonitorInstance[] {});
		context.setInstances(instanceArray);
		
		MonitorResult result = new MonitorResult();
		StringBuffer msgSB = new StringBuffer();

		ExprObject[] cpus = getCpuInstances(context);
		if (cpus == null) {
			msgSB.append("无法获取实例列表.");
			result.setMessage(msgSB.toString());
			result.setState(MonitorState.FAILED);
			return result;
		}
		int temp = 0;
		for (int i = 0; i < cpus.length; i++) {
			ExprObject cpu = cpus[i];
			if (cpu != null) {
				result.addPerfResult(new PerfResult(cpu.getMonitorInst(), getItem(), cpu.getValue()));
				temp++;
			}
		}
		// 效验是否所有实例都获取不到
		if (temp == 0) {
			msgSB.append("无法获取实例列表.");
			result.setMessage(msgSB.toString());
			result.setState(MonitorState.FAILED);
			return result;
		}

		return result;
	}

	/**
	 * 获取所有 实例监测实例,有错误时返回对应cpu对象为null
	 * 
	 * @return 实例对象列表
	 */
	private ExprObject[] getCpuInstances(MonitorContext context) {
		SnmpTarget target = new SnmpMethod(context.getMethod()).getTarget(context.getNode().getIp());
		List<ExprObject> result = ExprObjectUtil.getExprObjects(context.getTask(), target, context.getInstances(), 100, ExprObject.class);
		return result.toArray(new ExprObject[0]);
	}

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod method = new SnmpMethod(context.getMethod());
		SnmpTarget target = method.getTarget(context.getNode().getIp());
		final SnmpGet snmpGet = new SnmpGet(target);
		SnmpableInstanceDiscover sid = new SnmpableInstanceDiscover();
		SnmpableInstance[] insts = null;
		try {
			insts = (SnmpableInstance[]) sid.discover(target, snmpGet.getTimeout(), getPerfType());
		} catch (InstanceDiscoverException e) {
			//throw new CollectException("获取实例时发生异常,请查看配置参数是否正确.");
			throw new NullPointerException(e.getMessage());
		}
		
		if (insts == null || insts.length == 0) {
			throw new NullPointerException("无法获取相关指标，该设备不支持此监测.");
		}
		
		// 逐一获取个实例的当前值
		for (int i = 0; i < insts.length; i++) {
			SnmpableInstance inst = insts[i];
			// 计算当前值
			try {
				double val = SnmpUtil.getSnmpExpressionValue(snmpGet, inst.getUtilizeExp(), inst.getIndex());

				MonitorResultRow row = new MonitorResultRow();
				row.setInstCode(inst.getKey());
				row.setInstName(inst.getName());
				row.setInstExtra(inst.getUtilizeExp());
				row.setIndicator(getItem(), val);
				if (val == 0)
					row.setInstMonitor(false);
				result.addRow(row);
			} catch (Exception e) {
				logger.warn(String.format("采集实例时失败[ip: %s exp: %s inst: %s]。错误：%s", context.getNode().getIp(),
						inst.getUtilizeExp(), inst.getIndex(), e));
				logger.debug("堆栈：", e);
			}
		}

		return result;
	}
}
