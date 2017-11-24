package com.broada.carrier.monitor.server.impl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "mon_state_last")
public class StateLast extends State {
	private Key key;
	private Date firstTime;

	public StateLast() {
		super();
	}

	public StateLast(StateType type, String objectId, Date time, String value, String lastValue, String message) {
		super(type, objectId, time, time, value, lastValue, message, 1);
	}

	@Override
	public Date getLastTime() {
		return super.getLastTime();
	}

	@Column(length = 500)
	@Override
	public String getMessage() {
		return super.getMessage();
	}

	@Override
	public int getCount() {
		return super.getCount();
	}

	@Column(length = 50)
	@Override
	public String getValue() {
		return super.getValue();
	}

	@Transient
	@Override
	public StateType getType() {
		return getKey().getType();
	}

	@Override
	public void setType(StateType type) {
		getKey().setType(type);
	}

	@Override
	public Date getFirstTime() {
		return firstTime;
	}

	@Override
	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	@Transient
	@Override
	public String getObjectId() {
		return getKey().getObjectId();
	}

	@Override
	public void setObjectId(String objectId) {
		getKey().setObjectId(objectId);
	}

	@EmbeddedId
	public Key getKey() {
		if (key == null)
			key = new Key();
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}

	@Embeddable
	public static class Key implements Serializable {
		private static final long serialVersionUID = 1L;
		private String objectId;
		private StateType type;

		public Key() {
		}

		public Key(StateType type, String objectId) {
			this.objectId = objectId;
			this.type = type;
		}

		@Column(name = "object_id")
		public String getObjectId() {
			return objectId;
		}

		public void setObjectId(String objectId) {
			this.objectId = objectId;
		}

		@Column(name = "type")
		public StateType getType() {
			return type;
		}

		public void setType(StateType type) {
			this.type = type;
		}

		@Override
		public int hashCode() {
			return objectId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			Key another = (Key) obj;
			return this.getObjectId().equals(another.getObjectId())
					&& this.getType() == another.getType();
		}

	}
}
