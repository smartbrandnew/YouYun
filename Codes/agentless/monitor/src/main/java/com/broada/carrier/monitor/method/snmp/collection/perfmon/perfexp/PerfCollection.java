package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

public class PerfCollection {
	private List<Perf> perfs = new ArrayList<Perf>(); 
	
	public void add(Perf perf) {
    perfs.add(perf);
	}

	public PerfCollection get(PerfType type) {
    if(type==null){
      return null;
    }
    PerfCollection result = new PerfCollection();
    Iterator<Perf> iter = perfs.iterator();
    while (iter.hasNext()) {
      Perf perf = (Perf)iter.next();
      if (type.equals(perf.getType())){
        result.add(perf);
      }
    }
    return result;
	}
	
	/**
	 * 将当前PerfCollection转换为ExpCollection
	 * @return
	 */
	public ExpCollection toExps() {
		ExpCollection result = new ExpCollection();
		Iterator<Perf> iter = perfs.iterator();
    while (iter.hasNext()) {      
      result.add(((Perf)iter.next()).getExp());
    }
    return result;
	}

  public Iterator<Perf> iterator() {
    return Collections.unmodifiableList(perfs).iterator(); 
  }

	public int size() {
		return perfs.size();
	}
  
  void clear() {
    perfs.clear();
  }
}
