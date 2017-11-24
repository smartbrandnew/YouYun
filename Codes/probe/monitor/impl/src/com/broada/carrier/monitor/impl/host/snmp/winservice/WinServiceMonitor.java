package com.broada.carrier.monitor.impl.host.snmp.winservice;

import java.io.Serializable;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.RunState;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;

/**
 * <p>Title: WinServiceMonitor</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Broada</p>
 * @author plx
 * @version 2.3
 */

public class WinServiceMonitor extends BaseMonitor {
  public static final String IDX_SUNSTATE = "SNMP-WINSERVICE-1";
  
  @Override
	public Serializable collect(CollectContext context) {
    SnmpMethod method = new SnmpMethod(context.getMethod());
    SnmpWalk walk = new SnmpWalk(method.getTarget(context.getNode().getIp()));
    MonitorResult result = new MonitorResult();
    List winServices;
		try {
			winServices = WinServiceUtil.getAllWinServices(walk);
		} catch (SnmpException e) {
			throw ErrorUtil.createRuntimeException(e.getMessage(), e);
		}
		
		if (context.getInstances() != null) {
			for (MonitorInstance inst : context.getInstances()) {
				MonitorResultRow row = new MonitorResultRow(inst);
		    row.setIndicator(IDX_SUNSTATE, RunState.STOP);
	      result.addRow(row);
			}
		}
		
    for (int i = 0; i < winServices.size(); i++) {
      WinService winService = (WinService) winServices.get(i);
      MonitorResultRow row = result.getRow(winService.getWinServiceKey());
      if (row == null) {
      	row = new MonitorResultRow(winService.getWinServiceKey(), winService.getWinServiceName());
      	result.addRow(row);
      }
      row.setIndicator(IDX_SUNSTATE, RunState.RUNNING);      
    }
    
    return result;
  }
}
