package uyun.bat.syndatabase.entity;

import java.io.Serializable;

public class Tag implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private String key;
	private String value;
	
	public Tag() {
		
	}
	
	public Tag(String key) {
		this.key = key;
	}

	public Tag(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public Tag(String id, String key, String value) {
		this.id = id;
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

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Tag [key=" + key + ", value=" + value + "]";
	}

}
