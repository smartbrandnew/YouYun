package com.broada.carrier.monitor.probe.impl.dispatch;

/**
 * 监测并发策略
 */
public class ConcurrencePolicy {
	public static final int RUN_NO_LIMIT = 0;
	
	/**
	 * 默认策略，一个节点只允许一个监测任务，同个监测类型允许任意数量的监测任务
	 */
	public static final ConcurrencePolicy DEFAULT_POLICY = new ConcurrencePolicy("default", 1, RUN_NO_LIMIT);	
	private String id;
	private int nodeRunMax;
	private int typeRunMax;

	public ConcurrencePolicy(String id, int nodeRunMax, int typeRunMax) {
		super();
		this.id = id;
		this.nodeRunMax = nodeRunMax;
		this.typeRunMax = typeRunMax;
	}
	
	/**
	 * 表示此监测任务在一个节点上最多允许同时运行几个任务
	 * @return
	 */
	public int getNodeRunMax() {
		return nodeRunMax;
	}

	/**
	 * 表示此监测任务同类型的全局最多允许同时运行几个任务
	 * @return
	 */
	public int getTypeRunMax() {
		return typeRunMax;
	}

	/**
	 * 策略ID
	 * @return
	 */
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return String.format("%s[id: %s nm: %d tm: %d]", getClass().getSimpleName(), id, nodeRunMax, typeRunMax);
	}

	/**
	 * 从字符中中解析策略
	 * @param item
	 * @return
	 */
	public static ConcurrencePolicy parse(String item) {
		String[] fields = item.split(",");
		if (fields.length != 3)
			throw new IllegalArgumentException(String.format("错误的解析格式[%s]", item));
		return new ConcurrencePolicy(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return this.id.equals(((ConcurrencePolicy)obj).id);
	}
}
