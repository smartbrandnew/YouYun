package com.broada.carrier.monitor.server.api.event;

import java.io.Serializable;

public class ObjectChangedEvent<T> implements Serializable {
	public static final String TOPIC = "monitor.object.changed";
	private static final long serialVersionUID = 1L;
	private ObjectChangedType type;
	private T oldObject;
	private T newObject;

	public ObjectChangedEvent(ObjectChangedType type, T oldObject, T newObject) {
		this.type = type;
		this.oldObject = oldObject;
		this.newObject = newObject;
	}

	public ObjectChangedType getType() {
		return type;
	}

	/**
	 * 获取变更前的值，只有UPDATED或DELETED事件中会有此值
	 * 注意：出于性能考虑，一些对象的修改事件并不提供旧值
	 * @return
	 */
	public T getOldObject() {
		return oldObject;
	}

	/**
	 * 获取变更后的值
	 * @return
	 */
	public T getNewObject() {
		return newObject;
	}
	
	public T getObject() {
		return newObject == null ? oldObject : newObject;
	}

	@Override
	public String toString() {
		return String.format("%s[type: %s old: %s new: %s]", getClass().getSimpleName(), getType(), getOldObject(), getNewObject());
	}
}
