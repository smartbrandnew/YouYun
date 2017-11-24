package com.broada.carrier.monitor.impl.host.ipmi.perfs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.CollectException;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.host.ipmi.IPMIConstants;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.EntityType;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMICollect;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIParameter;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.QuotaInfo;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.SensorType;
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

public class IPMIPerfsMonitor implements Monitor {

	public static final Log logger = LogFactory.getLog(IPMIPerfsMonitor.class);
	private Map<EntityType, List<SensorType>> map;
	private List<String> instanceNames;
	private long startTime;
	private List<String> fields;

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult(MonitorConstant.MONITORSTATE_NICER);
		result.setResultDesc("指标信息监测正常");
		IPMIMonitorMethodOption option = new IPMIMonitorMethodOption(context.getMethod());
		Collection<MonitorInstance> instances = new ArrayList<MonitorInstance>(Arrays.asList(context.getInstances()));
		instanceNames = new ArrayList<String>();
		fields = new ArrayList<String>();
		for (MonitorInstance monitorInstance : instances) {
			instanceNames.add(monitorInstance.getCode());
			fields.add(monitorInstance.getCode());
		}
		List<QuotaInfo> list;
		try {
			list = collect(option.toParameter(context.getMethod().getProperties().get("host").toString()));
			startTime = System.currentTimeMillis();
		} catch (Exception e) {
			logger.debug("获取IPMI指标信息结果集失败，错误：", e);
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取IPMI指标信息结果集失败。");
			return result;
		}
		if (list == null || list.isEmpty()) {
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("未采集到指标信息。");
			result.setResponseTime(System.currentTimeMillis() - startTime);
			return result;
		}

		StringBuffer desc = new StringBuffer();
		for (QuotaInfo quotaInfo : list) {
			MonitorResultRow row = new MonitorResultRow(quotaInfo.getName(), quotaInfo.getName());
			row.addTag("device:" + row.getInstCode());
			if(quotaInfo.getCurrent() > 0){
				row.setIndicator(IPMIConstants.ITEMIDX_IPMI_CURR, quotaInfo.getCurrent());
			} else if (quotaInfo.getCurrent() < 0) {
				logger.error(String.format("实例%s电流值%s为负数", quotaInfo.getName(), quotaInfo.getCurrent()));
			}
			if (quotaInfo.getFanSpeed() > 0) {
				row.setIndicator(IPMIConstants.ITEMIDX_IPMI_FAN, quotaInfo.getFanSpeed());
			} else if (quotaInfo.getFanSpeed() < 0) {
				logger.error(String.format("实例%s转速值%s为负数", quotaInfo.getName(), quotaInfo.getFanSpeed()));
			}
			if (quotaInfo.getPower() > 0) {
				row.setIndicator(IPMIConstants.ITEMIDX_IPMI_POW, quotaInfo.getPower());
			} else if (quotaInfo.getPower() < 0) {
				logger.error(String.format("实例%s功率值%s为负数", quotaInfo.getName(), quotaInfo.getPower()));
			}
			if (quotaInfo.getTemperature() > 0) {
				row.setIndicator(IPMIConstants.ITEMIDX_IPMI_TEMP, quotaInfo.getTemperature());
			} else if (quotaInfo.getTemperature() < 0) {
				logger.error(String.format("实例%s温度值%s为负数", quotaInfo.getName(), quotaInfo.getTemperature()));
			}
			if (quotaInfo.getVoltage() > 0) {
				row.setIndicator(IPMIConstants.ITEMIDX_IPMI_VOLT, quotaInfo.getVoltage());
			} else if (quotaInfo.getVoltage() < 0) {
				logger.error(String.format("实例%s电压值%s为负数", quotaInfo.getName(), quotaInfo.getVoltage()));
			}
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
		cr.setMessage("采集指标进行中....");
		cr.setState(CollectMonitorState.PROCESSING);
		List<QuotaInfo> list = null;
		try {
			list = collect(option.toParameter(context.getNode().getIp()));
		} catch (IPMIException e) {
			logger.warn("获取IPMI底盘信息结果集失败，错误：", e);
			cr.setState(CollectMonitorState.FAILED);
			cr.setProgress(100);
			cr.setMessage("获取IPMI指标信息结果集失败。");
			cr.setResultDesc(e.getMessage());
			return cr;
		}
		MonitorInstance mi;
		List<MonitorInstance> mis = new ArrayList<MonitorInstance>();
		PerfResult pr;
		List<PerfResult> prs = new ArrayList<PerfResult>();
		for (QuotaInfo quotaInfo : list) {
			mi = new MonitorInstance();
			mi.setInstanceKey(quotaInfo.getName());
			mi.setInstanceName(quotaInfo.getType().getLabel());
			mis.add(mi);
			pr = new PerfResult(mi.getCode(), IPMIConstants.ITEMIDX_IPMI_QUOTA, quotaInfo.getAllQuota());
			prs.add(pr);
		}
		cr.setMessage("采集工作完成。");
		cr.setProgress(100);
		cr.setPerfResults(prs.toArray(new PerfResult[prs.size()]));
		return cr;
	}

	private List<QuotaInfo> collect(IPMIParameter param) throws CollectException, IPMIException {
		IPMICollect collect = new IPMICollectImpl(param);
		if (map == null || map.isEmpty()) {
			map = new HashMap<EntityType, List<SensorType>>();
			List<SensorType> list = new ArrayList<SensorType>();
			list.add(SensorType.TEM);
			list.add(SensorType.VOL);
			map.put(EntityType.PROCE, list);
			list = new ArrayList<SensorType>();
			list.add(SensorType.TEM);
			list.add(SensorType.VOL);
			map.put(EntityType.MEMORY, list);
			list = new ArrayList<SensorType>();
			list.add(SensorType.TEM);
			list.add(SensorType.VOL);
			list.add(SensorType.POW);
			list.add(SensorType.CUR);
			map.put(EntityType.BOARD, list);
			list = new ArrayList<SensorType>();
			list.add(SensorType.TEM);
			list.add(SensorType.VOL);
			list.add(SensorType.CUR);
			list.add(SensorType.POW);
			map.put(EntityType.POWER, list);
		}
		try {
			Date date = new Date();
			List<QuotaInfo> list = collect.getQuotaInfos(map);
			logger.debug(String.format("获取IPMI指标信息结果耗时：%s", new Date().getTime() - date.getTime()));
			return list;
		} catch(IPMIException e){
			throw e;
		} catch (Exception e) {
			throw new CollectException("采集指标信息错误。", e);
		}
	}
}
