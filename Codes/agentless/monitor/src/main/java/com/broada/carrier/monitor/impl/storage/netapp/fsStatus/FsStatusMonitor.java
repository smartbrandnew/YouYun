package com.broada.carrier.monitor.impl.storage.netapp.fsStatus;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.host.snmp.util.NetAppSnmpProcessManager;
import com.broada.carrier.monitor.impl.host.snmp.util.SnmpErrorUtil;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.error.MonitorException;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;

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
 * @author Shoulw
 * @version 1.0
 */

public class FsStatusMonitor extends BaseMonitor {

	private static final Log logger = LogFactory.getLog(FsStatusMonitor.class);

	private static final String ITEMIDX_CPUPERCENT = "NETAPP-FSSTATUS-1";
  
	@Override
	public Serializable collect(CollectContext context) {
		SnmpMethod snmpMethod = new SnmpMethod(context.getMethod());
		SnmpWalk walk = new SnmpWalk(snmpMethod.getTarget(context.getNode().getIp()));
		try {
			NetAppSnmpProcessManager mgr = new NetAppSnmpProcessManager(walk);
			SnmpResult[] walkResult = null;
			String status = "";
			if (mgr.getClass().isAssignableFrom(NetAppSnmpProcessManager.class)) {
				walkResult = mgr.getFsStatus(walk);
				Thread.sleep(10000);
				if(walkResult!=null){
					status = walkResult[0].getValue().toString();
				}
			}
			return new MonitorResult(new PerfResult(ITEMIDX_CPUPERCENT, status));
		} catch (InterruptedException e) {
			throw new MonitorException("执行过程被中断", e);
		} catch (SnmpException e) {
			throw SnmpErrorUtil.createError(e);
		} finally {
			walk.close();
		}
	}

	
}
