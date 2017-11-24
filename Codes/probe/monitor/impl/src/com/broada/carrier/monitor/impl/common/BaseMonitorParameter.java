package com.broada.carrier.monitor.impl.common;

import java.io.Serializable;

import com.broada.carrier.monitor.common.entity.DefaultDynamicObject;
import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 基本的监测参数类，各种具体的监测类型的监测参数可以继承它
 * 也可以直接使用它
 *
 * 该类具有基本的XML文本解析和生成XML文本的功能
 * 有两种构造方式，一是不带参数，二是带一个String类型的参数
 * 该参数必须是一个XML文本，构造器会根据文本解析成参数对象
 *
 * XML文本格式型如：
 * <?xml version="1.0" encoding="GBK" ?>
 * <PARAMETER type="TCP">
 *   <OPTIONS port="80" timeout="30" oid="1.3.2.1">
 *   <CONDITIONS>
 *     <CONDITION field="length" type="1" value="20" desc=""/>
 *   </CONDITIONS>
 * </PARAMETER>
 *
 * 其中CONDITION的field表示条件参数名，其意义由类的实现者来把握
 * type表示运算类型，比如等于、大于、包含等等，现在用一个数字映射.
 * 映射关系请看类Condition里的定义
 *
 *
 *
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */
public class BaseMonitorParameter extends DefaultDynamicObject implements Serializable {
	private static final long serialVersionUID = 1L;

	public BaseMonitorParameter() {			
	}

	public BaseMonitorParameter(String value) {		
		super(SerializeUtil.decodeJson(value, BaseMonitorParameter.class));
	}

	public DefaultDynamicObject getProperties() {
		return this;
	}

	public void setProperties(DefaultDynamicObject properties) {
		set(properties, true);
	}

	public String encode() {
		return SerializeUtil.encodeJson(this);
	}

	@JsonIgnore
	public String getParameters() {
		return encode();
	}
}
