package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.utils.TextUtil;

/** 对应映射配置monitor下的一级子对象，表示一个处理任务
 * 
 * @author Jiangjw */
public class MapTask {
	private static final MonitorState[] DEFAULT_MATCH_STATES = new MonitorState[] { MonitorState.SUCCESSED,
			MonitorState.OVERSTEP };
	private MonitorState[] matchStates;
	private String name;

	public MapTask(Element root, String name) {
		this.name = name;

		String text = root.attributeValue("states");
		if (TextUtil.isEmpty(text))
			matchStates = DEFAULT_MATCH_STATES;
		else {
			List<MonitorState> items = new ArrayList<MonitorState>(3);
			String[] states = text.split(",");
			for (String state : states) {
				items.add(MonitorState.checkByName(state));
			}
			matchStates = items.toArray(new MonitorState[0]);
		}
	}

	public boolean isMatchState(MonitorState state) {
		for (MonitorState temp : matchStates)
			if (temp == state)
				return true;
		return false;
	}

	public String getName() {
		return name;
	}

}
