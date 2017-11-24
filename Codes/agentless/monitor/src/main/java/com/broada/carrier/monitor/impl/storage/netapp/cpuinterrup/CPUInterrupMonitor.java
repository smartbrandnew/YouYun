package com.broada.carrier.monitor.impl.storage.netapp.cpuinterrup;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.host.snmp.util.NetAppSnmpProcessManager;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpErrorUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;

/**
 *NETAPP CPU中断率监测器实现
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
 * @author Shoulw
 * @version 1.0
 */

public class CPUInterrupMonitor extends BaseMonitor {
	
    public static final String CPUCPINTERRUPTPERCENT = ".1.3.6.1.4.1.789.1.2.1.11";

	private static final Log logger = LogFactory.getLog(CPUInterrupMonitor.class);

	private static final String ITEMCODE_CPUINTERRUP = "NETAPP-CPUINTERRUP-1";
	
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			NetAppSnmpProcessManager mgr = new NetAppSnmpProcessManager(walk);
			SnmpResult[] walkResult = null;
			CPUInterrup[] cpui = null;
			int count = 0;
			if (mgr.getClass().isAssignableFrom(NetAppSnmpProcessManager.class)) {
				walkResult = mgr.getCPUInterrup(walk);
				cpui = getcpus(walkResult);
				Thread.sleep(10000);
				 for (CPUInterrup cpu : cpui) {		
					MonitorResultRow row = new MonitorResultRow();
					row.setInstCode(cpu.getMonitorInst());
					row.setInstName(cpu.getMonitorName());
					row.setIndicator(ITEMCODE_CPUINTERRUP, cpu.getCurrentValue());
					result.addRow(row);
			}
			}
		} catch (InterruptedException e) {
			throw new MonitorException("执行过程被中断", e);
		} catch (SnmpException e) {
			throw SnmpErrorUtil.createError(e);
		}  finally {
			walk.close();
		}
		return result;
	}
	
	/**
	 * 得到监测参数和数据将其封装至实体返回,如果计算当前值失败则返回对应数据为空,用于自动发现
	 * 
	 * @param getter
	 * @param insts
	 * @return
	 */
	private CPUInterrup[] getcpus(SnmpResult[] walkResult ) { 
		CPUInterrup[] cpus = new CPUInterrup[walkResult.length];
		for (int i = 0; i < walkResult.length; i++) {
			CPUInterrup cpu = new CPUInterrup();
			cpus[i] = cpu;
			cpu.setMonitorIns("default-cpu-"+(i+1));
			cpu.setMonitorName("CPU["+(i+1)+"]");
			// cpu.setMonitorName(insts[i].getName());
			cpu.setExpression(walkResult[i].getOid().toString());
			// 计算当前值
			try {
				String val =walkResult[i].getValue().toString();//SnmpUtil.getSnmpExpressionValue(getter,insts[i].getUtilizeExp(), insts[i].getIndex());
//				if (Double.isNaN(val)) {// 如果此 Double 值是非数字,则直接赋为0
//					val = 0;
//				}
				cpu.setCurrentValue(new Double(val));
				cpu.setVavel(new Double(val + 5));
			} catch (Exception e) {
				logger.error("实例:" + "default-cpu-"+(i+1) + ",计算NETAPP CPU当前值失败");
				continue;
			}
		}
		return cpus;
	}


	// private static final MonitorInstance[] MonitorInstance = null;

}