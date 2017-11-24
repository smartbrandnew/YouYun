package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import java.util.HashMap;
import java.util.Map;

public class ProducerCollection {
	private Map<Long, Producer> producers = new HashMap<Long, Producer>();

	public Producer get(long producerCode) {
		return (Producer)producers.get(new Long(producerCode));
	}

	void clear() {
		producers.clear();
	}

	public boolean add(Producer producer) {
		if (producers.containsKey(new Long(producer.getCode())))
			return false;
		
		producers.put(new Long(producer.getCode()), producer);
		return true;
	}
  
  public int size(){
    return producers.size();
  }

}
