package uyun.bat.monitor.core.entity;

/**
 * 暂时tag都为包含条件
 * 没有!开头不包含条件
 */
public class TagEntry {

	private static final String TAG_SEPARATOR = ":";
	private String key;
	private String value;

	public TagEntry() {
		super();
	}

	public TagEntry(String key, String value) {
		super();
		this.key = key;
		this.value = value;
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

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TagEntry other = (TagEntry) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return key + TAG_SEPARATOR + value;
	}
}
