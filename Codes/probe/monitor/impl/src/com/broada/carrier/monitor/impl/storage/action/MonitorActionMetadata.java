package com.broada.carrier.monitor.impl.storage.action;

import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.api.entity.ActionMetadata;

/**
 * 发现Action元数据
 */
public class MonitorActionMetadata extends ActionMetadata {
	private static final Logger logger = LoggerFactory.getLogger(MonitorActionMetadata.class);
	private static final long serialVersionUID = 1L;
	private static final String PROPERTY_PREFIX = "monitor.";
	private static final String PROPERTY_OUTPUT = PROPERTY_PREFIX + "output";
	private static final String PROPERTY_PRIORITY = PROPERTY_PREFIX + "priority";
	
	private String output;
	private Integer priority;

	public MonitorActionMetadata(ActionMetadata action) {
		super(action);
	}

	/**
	 * 发现Action的输出类型
	 * @return
	 */
	public String getOutput() {
		if (output != null) {
			return output;
		}
		output = getProperty(PROPERTY_OUTPUT);
		return output != null ? output.toLowerCase() : "";
	}
	


	/**
	 * Action优先级，默认为100
	 * @return
	 */
	public int getPriority() {
		if (priority != null)
			return priority;
		
		String value = getProperty(PROPERTY_PRIORITY);
		if (value != null) {
			try {
				priority = Integer.parseInt(value.trim());
			} catch (NumberFormatException e) {
				logger.warn(String.format("Action[%s]优先级字段值[%s]不是一个正确的整形字段。错误：%s", getCode(), value, e));
				logger.debug("堆栈：", e);
			}
		}
		if (priority == null)
			priority = 100;
		
		return priority;
	}
	
	/**
	 * MonitorAction优先级排序器
	 * 使priority较小的排在列表前面
	 */
	public static class PriorityComparator implements Comparator<MonitorActionMetadata> {
		@Override
		public int compare(MonitorActionMetadata o1, MonitorActionMetadata o2) {
			if (o1.getPriority() > o2.getPriority())
				return 1;
			else if (o1.getPriority() == o2.getPriority())
				return 0;
			else
				return -1;
		}		
	}

}
