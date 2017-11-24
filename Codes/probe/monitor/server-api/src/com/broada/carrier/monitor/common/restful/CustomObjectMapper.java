package com.broada.carrier.monitor.common.restful;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * 自定义对象与JSON解析配置
 */
@Component
public class CustomObjectMapper extends ObjectMapper {
	private static final Logger logger = LoggerFactory.getLogger(CustomObjectMapper.class);
	private static final long serialVersionUID = 1L;
	
	public static CustomObjectMapper instance;
	
	public CustomObjectMapper() {
		instance = this;
		SimpleModule module = new SimpleModule("RestModule");
		// TODO 先使用这种临时的方式增加实体类序列化，更严格的方式是应该用明确的初始化方法
		// module.addSerializer(Node.class, new NodeSerializer());		
		this.registerModule(module);	
		this.configure(SerializationFeature.INDENT_OUTPUT, true);
		this.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
		this.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true);
		this.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);		
		// this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		// this.setSerializationInclusion(JsonInclude.Include.NON_NULL);		
		logger.debug("CustomObjectMapper created");		
	}
}