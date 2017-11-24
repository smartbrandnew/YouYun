package com.broada.carrier.monitor.common.restful;

import java.io.IOException;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * <pre>
 * 自定义序列化类
 * 
 * 本类实现原理：使用jackson序列化
 * 
 * 开发自定义序列化类的步骤：
 * 1. 确定要自定义序列化的目标类：T
 * 2. 实现一个类继承于CustomBeanSerializer <T>
 * 3. 提供若干CustomBeanOption，来说明要自定义序列化的字段
 * 4. 修改 com.newdream.joint.comm.http.impl.util.CustomObjectMapper.afterPropertiesSet() 方法，增加module.addSerializer(T.class, new TSerializer());
 * </pre>
 * @param <T>
 */
public class CustomBeanSerializer <T> extends JsonSerializer<T> {
	private static final String ATTR_OPTION = "_custom_option";
	private CustomBeanOption defaultOption;
	
	@Override
	public void serialize(T entity, JsonGenerator gen, SerializerProvider provider) throws IOException,
			JsonProcessingException {		
		if (entity == null)
			return;
		
		CustomBeanOption option = getOption();
		if (option == null)
			option = getDefaultOption(entity.getClass());
				
		try {						
			gen.writeStartObject();	
			for (CustomBeanField field : option.getFields()) {
				try {
					gen.writeObjectField(field.getName(), field.getValue(entity));
				} catch (Throwable e) {
					throw new JsonGenerationException(String.format("序列化Bean类[%s]属性[%s]失败，错误：%s", entity.getClass().getName(),
							field, e), e);
				}
			}
			gen.writeEndObject();
		} catch (Throwable e) {						
			if (e instanceof JsonGenerationException)
				throw (JsonGenerationException) e;
			throw new JsonGenerationException(String.format("序列化Bean类[%s]失败，错误：%s", entity.getClass().getName(), e), e);
		}
	}	

	/**
	 * 如果需要自定义默认实现，则可重载此方法
	 * @param cls
	 * @return
	 */
	protected CustomBeanOption getDefaultOption(Class<? extends Object> cls) {
		if (defaultOption == null) {
			synchronized (this) {
				if (defaultOption == null) {
					defaultOption = new CustomBeanOption(cls);
				}
			}
		}
		return defaultOption;
	}

	/**
	 * 从当前HTTP请求会话中获取类序列化选项
	 * @return
	 */
	public static CustomBeanOption getOption() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs == null)
			return null;
		return (CustomBeanOption) attrs.getAttribute(ATTR_OPTION, RequestAttributes.SCOPE_REQUEST);
	}	
	
	/**
	 * 从当前HTTP请求会话中设置类序列化选项
	 * @return
	 */
	public static void setOption(CustomBeanOption option) {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs == null)
			return;	
		RequestContextHolder.getRequestAttributes().setAttribute(ATTR_OPTION, option, RequestAttributes.SCOPE_REQUEST);
	}
}
