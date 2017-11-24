package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.ArrayList;
import java.util.List;

public class MapMonitorContext {
	private List<MapOutput> outputs = new ArrayList<MapOutput>();

	public List<MapOutput> getOutputs() {
		return outputs;
	}
	
	public MapOutput createOutput(String localKey, String remoteClassCode) {
		MapOutput output = new MapOutput();
		output.setLocalKey(localKey);
		output.setRemoteClassCode(remoteClassCode);
		outputs.add(output);
		return output;
	}

}
