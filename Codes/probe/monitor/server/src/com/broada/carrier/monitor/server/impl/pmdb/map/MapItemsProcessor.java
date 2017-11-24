package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;
import com.broada.component.utils.error.ErrorUtil;

public class MapItemsProcessor implements MapProcessor {
	private static final Logger logger = LoggerFactory.getLogger(MapItemsProcessor.class);
	private MapConfig config;
	private List<MapItem> items;

	public MapItemsProcessor(MapConfig config, List<MapItem> items) {
		this.config = config;
		this.items = items;
	}

	@Override
	public MapOutput process(MapInput input, LocalRemoteMapper mapper) {
		MapOutput output = new MapOutput();		
		for (MapItem item : items) {
			Object value = null;
			try {
				value = input.getValue(item.getLocalType(), item.getLocalCode());
				if (item.getFunction() != null) {
					try {
						MapFunction function = config.getFunction(item.getFunction());
						if (function == null) 
							throw new IllegalArgumentException("未知的函数：" + item.getFunction());
						value = function.run(value);
					} catch (Throwable e) {
						ErrorUtil.warn(logger, String.format("指标映射项目函数执行失败，将忽略本次执行[function: %s input: %s]", item.getFunction(), value), e);
						continue;
					}
				}
				output.setValue(input, item.getRemoteType(), item.getRemoteCode(), value);
			} catch (Throwable e) {
				throw ErrorUtil.createRuntimeException(String.format("映射项目处理失败[%s 值：%s]", item, value), e);
			}
		}
		return output;
	}
}
