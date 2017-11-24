package uyun.bat.datastore.api.entity;

/**
 * 状态指标元数据，对应于Metric
 */
public class State {
	private String id;
	private String name;

	public State() {
	}

	public State(String name) {
		this.name = name;
	}

	/**
	 * 标识符
	 * @return
	 */
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 指标名称，如 service_running, port_work 等
	 * @return
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		State state = (State) o;

		if (name != null ? !name.equals(state.name) : state.name != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "State{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
