package uyun.bat.datastore.api.entity;

public enum AggregatorType {
	max(0, "max"), min(1, "min"), sum(2, "sum"), avg(3, "avg"), last(4, "last");
	private int id;
	private String name;

	private AggregatorType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static AggregatorType checkById(int id) {
		for (AggregatorType type : AggregatorType.values()) {
			if (type.getId() == id) {
				return type;
			}
		}
		return null;
	}

	public static AggregatorType checkByName(String name) {
		for (AggregatorType type : AggregatorType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		return null;
	}

}
