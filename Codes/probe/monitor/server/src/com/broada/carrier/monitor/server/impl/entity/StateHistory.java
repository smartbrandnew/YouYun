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
@Table(name = "mon_state_history")
public class StateHistory extends State {
	private Key key;

	public StateHistory() {
		super();
	}

	public StateHistory(StateType type, String objectId, Date firstTime, Date lastTime, String value, String lastValue, String message, int count) {
		super(type, objectId, firstTime, lastTime, value, lastValue, message, count);
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

	@Transient
	@Override
	public Date getFirstTime() {
		return getKey().getFirstTime();
	}

	@Override
	public void setFirstTime(Date firstTime) {
		getKey().setFirstTime(firstTime);
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
		private Date firstTime;
		
		public Key() {
		}

		public Key(StateType type, String objectId, Date firstTime) {
			this.objectId = objectId;
			this.type = type;
			this.firstTime = firstTime;
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

		public Date getFirstTime() {
			return firstTime;
		}

		public void setFirstTime(Date firstTime) {
			this.firstTime = firstTime;
		}

		@Override
		public int hashCode() {
			return objectId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			Key another = (Key) obj;
			return this.getObjectId().equals(another.getObjectId())
					&& this.getType() == another.getType()
					&& this.getFirstTime().equals(another.getFirstTime());
		}

	}
}
