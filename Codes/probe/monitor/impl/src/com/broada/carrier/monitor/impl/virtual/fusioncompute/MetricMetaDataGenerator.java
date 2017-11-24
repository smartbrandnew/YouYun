package com.broada.carrier.monitor.impl.virtual.fusioncompute;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MetricMetaDataGenerator {
	private static final Logger logger=LoggerFactory.getLogger(MetricMetaDataGenerator.class);
	private ConcurrentHashMap<String, String> metricMap=new ConcurrentHashMap<String, String>();
	private ObjectMapper mapper=new ObjectMapper();
	private static MetricMetaDataGenerator instance=new MetricMetaDataGenerator();
	public static MetricMetaDataGenerator getInstance(){
		return instance;
	}
	
	public void addMetaData(String resourceType,String name,String unit){
		metricMap.put(resourceType+"--"+name, unit);
		try{
			String json=mapper.writeValueAsString(metricMap);
			logger.info("指标元数据信息: "+json);
		}catch (Exception e) {
			logger.warn("json转换异常: ", e);
		}
	}
}
