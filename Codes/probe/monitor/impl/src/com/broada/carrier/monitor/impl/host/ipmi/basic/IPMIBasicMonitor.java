package com.broada.carrier.monitor.impl.host.ipmi.basic;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.host.ipmi.IPMIConstants;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.BasicInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMICollect;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.IPMICollectImpl;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.common.Constants;
import com.broada.carrier.monitor.method.ipmi.IPMIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IPMIBasicMonitor implements Monitor {
	private Logger logger = LoggerFactory.getLogger(IPMIBasicMonitor.class);
	private long startTime;

	@Override
	public Serializable collect(CollectContext context) {
		CollectResult result = context.getResult();
		result.setProgress(1);
		result.setMessage("采集指标开始....");
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		List<BasicInfo> list = null;
		try {
			IPMICollect collect = new IPMICollectImpl(option.toParameter(context.getMethod().getProperties().get("host").toString()));
			result.setMessage("采集指标进行中....");
			result.setState(CollectMonitorState.PROCESSING);
			list = collect.getBasicInfo();
			startTime = System.currentTimeMillis();
		} catch (IPMIException e) {
			logger.warn("获取IPMI基本信息结果集失败，错误：", e);
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
		MonitorNode node = context.getNode();
		PerfResult[] prs = assemblePerfResults(list, node);
		result.setPerfResults(prs);
		result.setResponseTime(System.currentTimeMillis() - startTime);
		result.setMessage("采集工作完成。");
		result.setProgress(100);
		return result;
	}

	private PerfResult[] assemblePerfResults(List<BasicInfo> list, MonitorNode node) {
		List<PerfResult> prs = new ArrayList<PerfResult>();
		PerfResult pr;
		for (BasicInfo info : list) {
			if (Constants.BASIC_SEVER_NAME.equalsIgnoreCase(info.getTitle())) {
				info.setTitle(Constants.BASIC_SEVER_NAME + "-" + node.getName());
			}
			if (StringUtils.isNotBlank(info.getMfg())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_MFG, info.getMfg());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getName())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_PROD, info.getName());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getSerial())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_SEZ, info.getSerial());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getPartNum())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_NUM, info.getPartNum());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getInVoltRange())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_VRAG, info.getInVoltRange());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getInFreqRange())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_FRAG, info.getInFreqRange());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getCapacity())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_CAP, info.getCapacity());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getFlags())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_FLAG, info.getFlags());
				prs.add(pr);
			}
			if (StringUtils.isNotBlank(info.getFlags())) {
				pr = new PerfResult(info.getTitle(), IPMIConstants.ITEMIDX_BASIC_IP, node.getIp());
				prs.add(pr);
			}
		}
		return prs.toArray(new PerfResult[prs.size()]);
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
		result.setResultDesc("基本信息监测正常");
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		List<BasicInfo> list = null;
		try {
			IPMICollect collect = new IPMICollectImpl(option.toParameter(context.getMethod().getProperties().get("host").toString()));
			list = collect.getBasicInfo();
			startTime = System.currentTimeMillis();
		} catch (IPMIException e) {
			logger.debug("获取IPMI基本信息结果集失败，错误：", e);
			result.setState(MonitorState.FAILED);
			result.setResultDesc("获取IPMI基本信息结果集失败。");
			return result;
		}
		if (list == null || list.isEmpty()) {
			result.setState(MonitorState.FAILED);
			result.setResultDesc("未获取到IPMI基本信息结果集。");
			result.setResponseTime(System.currentTimeMillis() - startTime);
			return result;
		}
		MonitorNode node = context.getNode();
		PerfResult[] prs = assemblePerfResults(list, node);
		result.setPerfResults(prs);
		result.setResponseTime(System.currentTimeMillis() - startTime);
		return result;
	}
}
