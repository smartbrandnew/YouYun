package uyun.bat.datastore.entity;

public class Tag {
	public static final String VALUE_EMPTY = "";
	private String id;
	private String key;
	private String value;

	public Tag() {
	}

	public Tag(String key) {
		this(key, VALUE_EMPTY);
	}

	public Tag(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Tag tag = (Tag) o;

		if (id != null ? !id.equals(tag.id) : tag.id != null) return false;
		if (key != null ? !key.equals(tag.key) : tag.key != null) return false;
		if (value != null ? !value.equals(tag.value) : tag.value != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;
		result = 31 * result + (key != null ? key.hashCode() : 0);
		result = 31 * result + (value != null ? value.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Tag{" +
				"id=" + id +
				", key='" + key + '\'' +
				", value='" + value + '\'' +
				'}';
	}

	/**
	 * 从一个key:value格式解析一个Tag对象出来
	 * @param tag
	 * @return
	 */
	public static Tag decode(String tag) {
		String[] items = tag.split(":");
		if (items.length == 1)
			return new Tag(items[0]);
		else
			return new Tag(items[0], items[1]);
	}

	public String encode() {
		if (value == null || value.isEmpty())
			return key;
		else
			return key + ":" + value;
	}
}
