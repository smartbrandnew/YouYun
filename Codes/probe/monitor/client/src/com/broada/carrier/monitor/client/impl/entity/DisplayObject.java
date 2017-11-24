package com.broada.carrier.monitor.client.impl.entity;

import com.broada.carrier.monitor.common.util.BeanUtil;

public class DisplayObject<T> {
	private T object;

	public DisplayObject(T object) {
		super();
		this.object = object;
	}

	public T getObject() {
		return object;
	}

	@Override
	public String toString() {
		String text = (String) BeanUtil.getPropertyValue(object, "displayName");
		if (text != null)
			return text;
		text = (String) BeanUtil.getPropertyValue(object, "name");
		if (text != null)
			return text;
		return object.toString();
	}

	@Override
	public int hashCode() {
		return getObject().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		DisplayObject<?> other = (DisplayObject<?>) obj;
		return this.getObject().equals(other.getObject());
	}
}
