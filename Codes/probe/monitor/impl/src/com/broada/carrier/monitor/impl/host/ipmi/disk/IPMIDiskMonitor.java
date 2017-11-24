package com.broada.carrier.monitor.impl.host.ipmi.disk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.host.ipmi.IPMIConstants;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMICollect;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.IPMICollectImpl;
import com.broada.carrier.monitor.method.ipmi.IPMIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class IPMIDiskMonitor extends BaseMonitor{

	private Logger LOG = LoggerFactory.getLogger(IPMIDiskMonitor.class);
	private long startTime;

	@Override
	public Serializable collect(CollectContext context) {
		CollectResult result = context.getResult();
		result.setProgress(1);
		result.setMessage("采集指标开始....");
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		List<DiskInfo> list = null;
		try {
			IPMICollect collect = new IPMICollectImpl(option.toParameter(context.getNode().getIp()));
			result.setMessage("采集指标进行中....");
			result.setState(CollectMonitorState.PROCESSING);
			list = collect.getDiskInfo();
			startTime = System.currentTimeMillis();
		} catch (IPMIException e) {
			LOG.warn("获取IPMI基本信息结果集失败，错误：", e);
			result.setState(CollectMonitorState.FAILED);
			result.setProgress(100);
			result.setMessage("获取IPMI基本信息结果集失败。");
			result.setResultDesc(e.getMessage());
			return result;
		}
		if (list == null || list.isEmpty()) {
			result.setState(CollectMonitorState.FAILED);
			result.setProgress(100);
			result.setMessage("未获取到IPMI基本信息结果集。");
			result.setResponseTime(System.currentTimeMillis() - startTime);
			return result;
		}
		// 此处list 必不为空
		List<MonitorResultRow> rows = assemblePerfResults(list);
		for(MonitorResultRow row:rows)
			result.addRow(row);
		result.setResponseTime(System.currentTimeMillis() - startTime);
		result.setMessage("采集工作完成。");
		result.setProgress(100);
		return result;
	}

	/**
	 * 封装采集信息
	 * @param infos
	 * @return
	 */
	private List<MonitorResultRow> assemblePerfResults(List<DiskInfo> infos){
		List<MonitorResultRow> rows = new ArrayList<MonitorResultRow>(infos.size());
		for(DiskInfo info:infos){
			MonitorResultRow row = new MonitorResultRow(info.getDiskName(), info.getDiskName());
			row.addTag("DiskName:" + info.getDiskName());
			row.setIndicator(IPMIConstants.ITEMIDX_DISK_STAT, info.getDiskStat().equals(DiskState.ERROR.getCode())? 0:1);
			rows.add(row);
		}
		return rows;
	}

}
