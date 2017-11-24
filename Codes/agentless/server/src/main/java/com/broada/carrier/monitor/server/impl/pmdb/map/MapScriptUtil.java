package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.Collection;

import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.impl.entity.LocalRemoteKey;
import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;

public class MapScriptUtil {
	private static final String OPER_SUM = "sum";
	private static final String OPER_AVG = "avg";
	private static final String OPER_MAX = "max";
	
	private MapInput input;
	private MapOutput output;
	private LocalRemoteMapper mapper;

	public MapScriptUtil(MapInput input, MapOutput output, LocalRemoteMapper mapper) {
		this.input = input;
		this.output = output;
		this.mapper = mapper;
	}

	public Object setOutputValue(String indicatorCode, String oper, String remoteCode) {
		Double value = null;
		if (OPER_AVG.equalsIgnoreCase(oper))
			value = avg(indicatorCode);
		else if (OPER_SUM.equalsIgnoreCase(oper))
			value = sum(indicatorCode);
		else if (OPER_MAX.equalsIgnoreCase(oper))
			value = max(indicatorCode);
		else
			throw new IllegalArgumentException("未知的运算函数：" + oper);
		
		if (value == null)
			return null;
		
		output.setValue(input, remoteCode, value);
		return value;
	}

	private Double max(String indicatorCode) {
		boolean hasValue = false;
		Double max = 0 - Double.MAX_VALUE;
		for (MonitorResultRow row : input.getResult().getRows()) {
			Double value = getIndicatorValue(row, indicatorCode);
			if (value == null)
				continue;
			if (max < value)
				max = value;
			hasValue = true;
		}
		if (hasValue)
			return max;
		else
			return null;
	}

	private Double getIndicatorValue(MonitorResultRow row, String indicatorCode) {
		Object value = row.getIndicator(indicatorCode);
		if (value == null)
			return null;
		if (!(value instanceof Number)) {
			try {
				value = Double.parseDouble(value.toString());
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException(String.format("指标[%s]值[%s]不是数字型", indicatorCode, value));				
			}			
		}
		return ((Number) value).doubleValue();		
	}

	private Double sum(String indicatorCode) {
		boolean hasValue = false;
		Double sum = (double) 0;
		for (MonitorResultRow row : input.getResult().getRows()) {
			Double value = getIndicatorValue(row, indicatorCode);
			if (value == null)
				continue;
			sum += value;
			hasValue = true;
		}
		if (hasValue)
			return sum;
		else
			return null;
	}

	private Double avg(String indicatorCode) {		
		Double sum = (double) 0;
		int count = 0;
		for (MonitorResultRow row : input.getResult().getRows()) {
			Double value = getIndicatorValue(row, indicatorCode);
			if (value == null)
				continue;
			sum += value;
			count++;			
		}
		if (count > 0)
			return sum / count;
		else
			return (double) 0;
	}
	
	public String getFirstRemoteKeyByLocalKey(String key) {
		Collection<LocalRemoteKey> keys = mapper.getKeysByLocalKey(key);		
		if (keys.isEmpty())
			return null;
		else
			return keys.iterator().next().getRemoteKey();
	}
}
