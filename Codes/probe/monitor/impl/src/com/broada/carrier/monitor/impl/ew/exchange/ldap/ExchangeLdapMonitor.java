package com.broada.carrier.monitor.impl.ew.exchange.ldap;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.impl.common.SpeedUtil;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;
import com.broada.component.utils.lang.SimpleProperties;

public class ExchangeLdapMonitor implements Monitor{
	public static final String ITEM_ACTIVE_RPC_THREADS = "exc-active-rpcThreads";
	public static final String ITEM_LDAP_RESULTS_PS = "exc-ldap-ResultsPersec";
	public static final String ITEM_LDAP_SEARCH_CALLS_PS = "exc-ldap-SearchCallsPersec";
	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorTempData tempData = context.getTempData();
		if (tempData == null) 
			tempData = new MonitorTempData();
		MonitorResult mr = collect(context.getTask().getId(), new CollectContext(context), tempData);
		context.setTempData(tempData);
		return mr;
	}

	@Override
	public Serializable collect(CollectContext context) {
		MonitorTempData tempData = new MonitorTempData();
		return collect("-1", context, tempData);
	}
	private MonitorResult collect(String taskId, CollectContext context, MonitorTempData tempData) {
		CLIResult result = null;
		result = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(), "exchange-ldap");
		long now = System.currentTimeMillis();

		List<Properties> items = result.getListTableResult();
		MonitorResult mr = new MonitorResult();
			for(int index=0;index<items.size();index++){
			SimpleProperties props = new SimpleProperties(items.get(index));
			String name=props.get("name");
			if (!"_total".equals(name.trim()) && items.size() > 1)
				continue;
			Long activeRPCthreads = props.get("activeRPCthreads", 0l);
			Long ldapSearchcalls = props.get("ldapSearchcalls", 0l);
			Long ldapResults = props.get("ldapResults", 0l);

			Double ldapResultsPS = SpeedUtil.calSpeed(tempData, ITEM_LDAP_RESULTS_PS, ldapResults, now);
			Double ldapSearchcallsPS = SpeedUtil.calSpeed(tempData, ITEM_LDAP_SEARCH_CALLS_PS, ldapSearchcalls, now);
			
			
			MonitorResultRow row = new MonitorResultRow();
			row.setIndicator(ITEM_ACTIVE_RPC_THREADS, activeRPCthreads);
			row.setIndicator(ITEM_LDAP_RESULTS_PS, ldapResultsPS);
			row.setIndicator(ITEM_LDAP_SEARCH_CALLS_PS, ldapSearchcallsPS);
			
			mr.addRow(row);		
		}
		tempData.setTime(new Date(now));
		return mr;
	}
}

