package com.broada.carrier.monitor.impl.host.ipmi.chassis;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.ChassisInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.HealthInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.HealthType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMICollect;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.core.IPMICollectImpl;
import com.broada.carrier.monitor.method.ipmi.IPMIMonitorMethodOption;
import com.broada.carrier.monitor.server.api.entity.CollectMonitorState;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class IPMIChassisMonitor implements Monitor {
	private Logger logger = LoggerFactory.getLogger(IPMIChassisMonitor.class);
	private long startTime;

	@Override
	public Serializable collect(CollectContext context) {
		CollectResult result = context.getResult();
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		result.setProgress(1);
		result.setMessage("采集指标开始....");
		ChassisInfo chassisInfo = null;
		try {
			IPMICollect collect = new IPMICollectImpl(option.toParameter(context.getMethod().getProperties().get("host").toString()));
			result.setMessage("采集指标进行中....");
			result.setState(CollectMonitorState.PROCESSING);
			chassisInfo = collect.getChassisInfo();
			startTime = System.currentTimeMillis();
		} catch (IPMIException e) {
			logger.warn("获取IPMI底盘信息结果集失败，错误：", e);
			result.setState(CollectMonitorState.FAILED);
			result.setProgress(100);
			result.setMessage("获取IPMI底盘信息结果集失败。");
			result.setResultDesc(e.getMessage());
			return result;
		}
		if (chassisInfo == null) {
			result.setState(CollectMonitorState.FAILED);
			result.setProgress(100);
			result.setMessage("未获取到IPMI底盘信息结果集。");
			return result;
		}
		List<PerfResult> prs = new ArrayList<PerfResult>();
		StringBuilder desc = new StringBuilder();
		monitorState(prs, ChassisType.CHASSISINTRUSION, chassisInfo.getChassisIntrusion(), "机箱未启用", desc, result);
		monitorState(prs, ChassisType.DRIVERFAULT, chassisInfo.isDriverFault(), "驱动故障", desc, result);
		monitorState(prs, ChassisType.MAINPOWERFAULT, chassisInfo.isMainPowerFault(), "主电源故障故障", desc, result);
		monitorState(prs, ChassisType.PANELLOCKOUT, chassisInfo.getPanelLockout(), "面板未锁定", desc, result);
		monitorState(prs, ChassisType.POWERCONTROLFAULT, chassisInfo.isPowerControlFault(), "功率控制故障故障", desc, result);
		monitorState(prs, ChassisType.POWERINTERLOCK, chassisInfo.getPowerInterlock(), "电源未连锁", desc, result);
		monitorState(prs, ChassisType.POWEROVERLOAD, chassisInfo.isPowerOverload(), "功率过载", desc, result);
		monitorState(prs, ChassisType.RADIATINGFAULT, chassisInfo.isRadiatingFault(), "制冷故障", desc, result);
		boolean bool = ChassisInterpretation.resolveOn(chassisInfo.getSystemPower()).getValue();
		if (!bool) {
			desc.append("电源故障→");
			for (HealthInfo hi : chassisInfo.getPowers()) {
				if (!hi.getValue().equals(HealthType.NORMAL)) {
					desc.append(hi.getName() + hi.getValue().getValue() + "；");
				}
			}
			desc.append("\n");
		}
		prs.add(new PerfResult(ChassisType.SYSTEMPOWER.getValue(), bool == true?1:0));

		result.setResponseTime(System.currentTimeMillis() - startTime);
		result.setResultDesc(desc.toString());
		result.setPerfResults(prs.toArray(new PerfResult[prs.size()]));
		result.setMessage("采集工作完成。");
		result.setProgress(100);
		return result;
	}

	private void monitorState(List<PerfResult> prs, ChassisType type, boolean value,
			String warnDesc, StringBuilder desc, MonitorResult result) {
		boolean bool = ChassisInterpretation.resolveBool(value).getValue();
		if (bool)
			desc.append(warnDesc).append('\n');
		prs.add(new PerfResult(type.getValue(), bool == true?1:0));
	}

	private void monitorState(List<PerfResult> prs, ChassisType type, boolean value,
			String warnDesc, StringBuilder desc, CollectResult result) {
		boolean bool = ChassisInterpretation.resolveBool(value).getValue();
		if (bool)
			desc.append(warnDesc).append('\n');
		prs.add(new PerfResult(type.getValue(), bool == true?1:0));
	}

	private void monitorState(List<PerfResult> prs, ChassisType type, String value,
			String warnDesc, StringBuilder desc, MonitorResult result) {
		boolean bool = ChassisInterpretation.resolveActive(value).getValue();
		if (!bool)
			desc.append(warnDesc).append('\n');
		prs.add(new PerfResult(type.getValue(), bool == true?1:0));
	}

	private void monitorState(List<PerfResult> prs, ChassisType type, String value,
			String warnDesc, StringBuilder desc, CollectResult result) {
		boolean bool = ChassisInterpretation.resolveActive(value).getValue();
		if (!bool)
			desc.append(warnDesc).append('\n');
		prs.add(new PerfResult(type.getValue(), bool == true?1:0));
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		ChassisInfo chassisInfo = null;
		try {
			IPMICollect collect = new IPMICollectImpl(option.toParameter(context.getMethod().getProperties().get("host").toString()));
			chassisInfo = collect.getChassisInfo();
			startTime = System.currentTimeMillis();
		} catch (IPMIException e) {
			logger.debug("获取IPMI底盘信息结果集失败，错误：", e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取IPMI底盘信息结果集失败。");
		}
		if (chassisInfo == null) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("未获取到IPMI底盘信息结果集。");
			return result;
		}
		List<PerfResult> prs = new ArrayList<PerfResult>();
		StringBuilder desc = new StringBuilder();
		monitorState(prs, ChassisType.CHASSISINTRUSION, chassisInfo.getChassisIntrusion(), "机箱未启用", desc, result);
		monitorState(prs, ChassisType.DRIVERFAULT, chassisInfo.isDriverFault(), "驱动故障", desc, result);
		monitorState(prs, ChassisType.MAINPOWERFAULT, chassisInfo.isMainPowerFault(), "主电源故障故障", desc, result);
		monitorState(prs, ChassisType.PANELLOCKOUT, chassisInfo.getPanelLockout(), "面板未锁定", desc, result);
		monitorState(prs, ChassisType.POWERCONTROLFAULT, chassisInfo.isPowerControlFault(), "功率控制故障故障", desc, result);
		monitorState(prs, ChassisType.POWERINTERLOCK, chassisInfo.getPowerInterlock(), "电源未连锁", desc, result);
		monitorState(prs, ChassisType.POWEROVERLOAD, chassisInfo.isPowerOverload(), "功率过载", desc, result);
		monitorState(prs, ChassisType.RADIATINGFAULT, chassisInfo.isRadiatingFault(), "制冷故障", desc, result);
		boolean bool = ChassisInterpretation.resolveOn(chassisInfo.getSystemPower()).getValue();
		if (!bool) {
			desc.append("电源故障→");
			for (HealthInfo hi : chassisInfo.getPowers()) {
				if (!hi.getValue().equals(HealthType.NORMAL)) {
					desc.append(hi.getName() + hi.getValue().getValue() + "；");
				}
			}
			desc.append("\n");
		}
		prs.add(new PerfResult(ChassisType.SYSTEMPOWER.getValue(), bool == true?1:0));

		result.setResponseTime(System.currentTimeMillis() - startTime);
		result.setResultDesc(desc.toString());
		result.setPerfResults(prs.toArray(new PerfResult[prs.size()]));
		return result;
	}

}
