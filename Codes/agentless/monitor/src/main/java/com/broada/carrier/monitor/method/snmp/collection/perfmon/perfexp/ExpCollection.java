package com.broada.carrier.monitor.method.snmp.collection.perfmon.perfexp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.broada.carrier.monitor.method.snmp.collection.entity.PerfType;

public class ExpCollection {
	private List<Exp> exps = new ArrayList<Exp>();

	public void add(Exp exp) {
		exps.add(exp);
	}

	public ExpCollection get(PerfType type) {
		ExpCollection result = new ExpCollection();
		Iterator<Exp> iter = iterator();
		while (iter.hasNext()) {
			Exp exp = (Exp)iter.next();
			if (exp.getType().equals(type))
				result.add(exp);
		}
		return result;
	}

	public Iterator<Exp> iterator() {
		return exps.iterator();
	}

	public void addAll(ExpCollection exps) {
		Iterator<Exp> iter = exps.iterator();
		while (iter.hasNext())
			this.exps.add(iter.next());
	}

	public void clear() {
		exps.clear();
	}

	public Exp get(String id) {
		Iterator<Exp> iter = iterator();
		while (iter.hasNext()) {
			Exp exp = (Exp)iter.next();
			if (id.equals(exp.getId()))
				return exp;
		}
		return null;
	}

	public int size() {
		return exps.size();
	}

	public boolean remove(String id) {
		Iterator<Exp> iter = iterator();
		while (iter.hasNext()) {
			Exp exp = (Exp)iter.next();
			if (id.equals(exp.getId())) {
				iter.remove();
				return true;
			}
		}
		return false;
	}
}
