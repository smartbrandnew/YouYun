package com.broada.carrier.monitor.impl.host.ipmi.health;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.host.ipmi.IPMIConstants;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.HealthInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.HealthType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMICollect;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.IPMICollectImpl;
import com.broada.carrier.monitor.method.ipmi.IPMIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class IPMIHealthMonitor implements Monitor {
	private Logger logger = LoggerFactory.getLogger(IPMIHealthMonitor.class);
	private long startTime;
	private List<String> instanceNames;

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
		result.setResultDesc("健康状态监测正常");
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		Collection<MonitorInstance> instances = new ArrayList<MonitorInstance>(Arrays.asList(context.getInstances()));
		instanceNames = new ArrayList<String>();
		for (MonitorInstance monitorInstance : instances) {
			instanceNames.add(monitorInstance.getCode());
		}
		List<HealthInfo> list = null;
		try {
			IPMICollect collect = new IPMICollectImpl(option.toParameter(context.getMethod().getProperties().get("host").toString()));
			list = collect.getHealthInfo();
			startTime = System.currentTimeMillis();
		} catch (IPMIException e) {
			logger.debug("获取IPMI健康状态结果集失败，错误：", e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取IPMI健康状态结果集失败。");
			return result;
		}
		if (list == null || list.isEmpty()) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("未获取到IPMI健康状态结果集。");
			result.setResponseTime(System.currentTimeMillis() - startTime);
			return result;
		}
		List<PerfResult> prs = new ArrayList<PerfResult>();
		PerfResult pr;
		StringBuffer desc = new StringBuffer();
		for (HealthInfo info : list) {
			logger.debug(String.format("HealthInfo：%s", info.getName() + info.getValue()));
			pr = new PerfResult(info.getName(), IPMIConstants.ITEMIDX_HEALTH_STAS, info.getValue().getLabel());
			if (info.getValue() == HealthType.FAULT) {
				result.setResultDesc(info.getName() + HealthType.FAULT.getValue());
				result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
				desc.append(result.getResultDesc() + "\n");
			}
			prs.add(pr);
		}
		if(!prs.isEmpty())
			for(PerfResult p:prs){
				MonitorResultRow row = new MonitorResultRow(p.getInstKey(), p.getInstKey());
				row.addTag("device:" + p.getInstKey());  // 设备标签
				row.setIndicator(p.getItemCode(), p.getValue());
				result.addRow(row);
			}
		result.setResponseTime(System.currentTimeMillis() - startTime);
		if (desc != null && !"".equals(desc.toString())) {
			result.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			result.setResultDesc(desc.toString());
		}
		return result;
	}

	@Override
	public Serializable collect(CollectContext context) {
		CollectResult cr = context.getResult();
		cr.setProgress(1);
		cr.setMessage("采集指标开始....");
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		List<HealthInfo> list;
		try {
			IPMICollect collect = new IPMICollectImpl(option.toParameter(context.getMethod().getProperties().get("host").toString()));
			cr.setMessage("采集指标进行中....");
			cr.setState(CollectMonitorState.PROCESSING);
			list = collect.getHealthInfo();
		} catch (IPMIException e) {
			logger.warn("获取IPMI底盘信息结果集失败，错误：", e);
			cr.setState(CollectMonitorState.FAILED);
			cr.setProgress(100);
			cr.setMessage("获取IPMI健康状态结果集失败。");
			cr.setResultDesc(e.getMessage());
			throw new CollectException("采集健康状态错误。", e);
		}
		MonitorInstance mi;
		List<MonitorInstance> mis = new ArrayList<MonitorInstance>();
		PerfResult pr;
		List<PerfResult> prs = new ArrayList<PerfResult>();
		for (HealthInfo quotaInfo : list) {
			mi = new MonitorInstance();
			mi.setInstanceKey(quotaInfo.getName());
			mi.setInstanceName(quotaInfo.getName());
			mis.add(mi);
			pr = new PerfResult(mi.getCode(), IPMIConstants.ITEMIDX_HEALTH_STAS, quotaInfo.getValue().getLabel());
			prs.add(pr);
		}
		cr.setPerfResults(prs.toArray(new PerfResult[prs.size()]));
		cr.setMessage("采集工作完成。");
		cr.setProgress(100);
		return cr;
	}
}
