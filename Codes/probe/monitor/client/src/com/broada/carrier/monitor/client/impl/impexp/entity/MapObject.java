package com.broada.carrier.monitor.client.impl.impexp.entity;

public enum MapObject {
	EXCEL_TABLE_METHOD("监测方法"),
	EXCEL_TABLE_POLICY("监测策略"),
	EXCEL_TABLE_PROBE("监测探针"),
	EXCEL_TABLE_NODE("监测节点"),
	EXCEL_TABLE_RESOURCE("监测资源"),
	EXCEL_TABLE_TASK("监测任务"),

	EXCEL_FIELD_TYPE("类型"),
	EXCEL_FIELD_INDEX("序号"),
	EXCEL_FIELD_CODE("编号"),
	EXCEL_FIELD_NAME("名称"),
	EXCEL_FIELD_DOMAIN("所属域"),
	EXCEL_FIELD_IP("IP"),
	EXCEL_FIELD_PORT("端口"),
	EXCEL_FIELD_INTERVAL("监测周期"),
	EXCEL_FIELD_ERROR_INTERVAL("异常周期"),
	EXCEL_FIELD_DESCR("说明"),
	EXCEL_FIELD_INSTANCE("监测实例"),
	EXCEL_FIELD_PROBE_CODE("监测探针编码"),
	EXCEL_FIELD_NODE_IP("监测节点IP"),
	EXCEL_FIELD_NODE_NAME("监测节点名称"),
	EXCEL_FIELD_RESOURCE_NAME("监测资源名称"),
	EXCEL_FIELD_POLICY_CODE("监测策略编码"),
	EXCEL_FIELD_METHOD_CODE("监测方法编码"),
	EXCEL_FIELD_ENABLED("激活"),

	;

	public static final MapObject[] EXCEL_TABLES = new MapObject[] {
			EXCEL_TABLE_METHOD, EXCEL_TABLE_POLICY, EXCEL_TABLE_PROBE, EXCEL_TABLE_NODE, EXCEL_TABLE_NODE,
			EXCEL_TABLE_RESOURCE, EXCEL_TABLE_TASK
	};

	public static final MapObject[] EXCEL_FIELDS = new MapObject[] {
			EXCEL_FIELD_TYPE
	};

	private DataSource dataSource;
	private String id;

	private MapObject(String id) {
		this.id = id;
		this.dataSource = getDataSource(name());
	}

	private static DataSource getDataSource(String name) {
		int pos = name.indexOf("_");
		return DataSource.check(name.substring(0, pos));
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public String getId() {
		return id;
	}

	public static MapObject checkExcelTable(String id) {
		return check(EXCEL_TABLES, id);
	}

	public static MapObject checkExcelField(String id) {
		return check(EXCEL_FIELDS, id);
	}

	public static MapObject get(MapObject[] objects, String id) {
		for (MapObject mo : objects) {
			if (mo.getId().equalsIgnoreCase(id))
				return mo;
		}
		return null;
	}

	public static MapObject check(MapObject[] objects, String id) {
		MapObject obj = get(objects, id);
		if (obj == null)
			throw new IllegalArgumentException(id);
		return obj;
	}

}
