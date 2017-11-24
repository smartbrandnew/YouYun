package com.broada.carrier.monitor.probe.impl.entity;

public enum MetricType {
	PERF(0, "PERF"), STATUS(1, "STATUS");
	private int index;
	private String name;

	private MetricType(int index, String name) {
		this.index = index;
		this.name = name;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static MetricType checkByIndex(int index) {
		for (MetricType type : MetricType.values()) {
			if (type.getIndex() == index)
				return type;
		}
		// default
		return MetricType.PERF;
	}
	
	

}
