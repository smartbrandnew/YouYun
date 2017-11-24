package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

/**
 * 
 * @author Maico Pang (panghf@broada.com.cn)
 * Create By 2007-5-22 14:36:50
 */
public class ExpGroupCollection {

  private List<ExpGroup> expGroups = new ArrayList<ExpGroup>();

  public void add(ExpGroup group) {
    expGroups.add(group);
  }
  
  /**
   * 根据类型获取表达式组列表
   * @param type
   * @return
   */
  public ExpGroupCollection getByType(PerfType type) {
    if(type==null){
      return null;
    }
    ExpGroupCollection result = new ExpGroupCollection();
    Iterator<ExpGroup> iter = expGroups.iterator();
    while (iter.hasNext()) {
      ExpGroup group = iter.next();
      if (type.equals(group.getType())){
        result.add(group);
      }
    }
    return result;
  }

  public Iterator<ExpGroup> iterator() {
    return Collections.unmodifiableList(expGroups).iterator(); 
  }

  public void addAll(ExpGroupCollection groups) {
    Iterator<ExpGroup> iter = groups.iterator();
    while (iter.hasNext()){
      add(iter.next());
    }
  }

  void clear() {
    expGroups.clear();
  }
  
	/**
	 * 将当前ExpGroupCollection转换为ExpCollection
	 * @return
	 */
	public ExpCollection toExps() {
		ExpCollection result = new ExpCollection();
		Iterator<ExpGroup> iter = expGroups.iterator();
    while (iter.hasNext()) {      
      result.addAll(iter.next().getExps());
    }
    return result;
	}  

	/**
	 * 根据表达式ID获取表达式
	 * @param type 
	 * @param id
	 * @return
	 */
	public Exp getExp(PerfType type, String id) {
		Iterator<ExpGroup> iter = expGroups.iterator();
		while (iter.hasNext()) {
			Iterator<Exp> expIter = iter.next().getExps().iterator();
			while (expIter.hasNext()) {
				Exp exp = (Exp) expIter.next();
				if (exp.getId().equals(id) && exp.getType().equals(type))
					return exp;
			}
		}
		return null;
	}

  public ExpGroup get(String id) {
    if(id==null){
      id="";
    }
    Iterator<ExpGroup> iter = expGroups.iterator();
    while (iter.hasNext()) {
      ExpGroup group = (ExpGroup)iter.next();
      if (id.equals(group.getId())){
        return group;
      }
    }
    return null;
  }

  public int size() {
    return expGroups.size();
  }
}
