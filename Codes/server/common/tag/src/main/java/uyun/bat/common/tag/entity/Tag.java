package uyun.bat.common.tag.entity;

import java.io.Serializable;

import uyun.bat.common.tag.util.TagUtil;

/**
 * 样例：<br>
 * 标准：[key:value]<br>
 * 只有key： [key:]
 */
public class Tag implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String key = "";
	private String value = "";

	public Tag() {
		super();
	}

	public Tag(String key) {
		super();
		this.key = key;
	}

	public Tag(String key, String value) {
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

	@Override
	public String toString() {
		if (value != null && value.length() > 0)
			return key + TagUtil.TAG_SEPARATOR + value;
		else
			return key + TagUtil.TAG_SEPARATOR;
	}

}
