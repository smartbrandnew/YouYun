package com.broada.carrier.monitor.server.impl.pmdb.map;

import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;

public interface MapProcessor {
	MapOutput process(MapInput input, LocalRemoteMapper mapper);
}
