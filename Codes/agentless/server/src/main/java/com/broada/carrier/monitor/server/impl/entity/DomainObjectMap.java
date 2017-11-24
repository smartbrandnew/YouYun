package com.broada.carrier.monitor.server.impl.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.broada.carrier.monitor.server.api.entity.EntityConst;

@Entity
@Table(name = "res_domain_obj_map")
public class DomainObjectMap {
	private Key key = new Key();
	private int createRes;
	private Date operTime;
	
	@EmbeddedId
	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
	
	@Transient
	public String getDomainId() {
		return getKey().getDomainId();
	}

	public void setDomainId(String domainId) {
		getKey().setDomainId(domainId);
	}

	@Transient
	public String getTargetId() {
		return getTargetId();
	}
	
	public void setTargetId(String targetId) {
		getKey().setTargetId(targetId);
	}

	@Column(name = "create_res")
	public int getCreateRes() {
		return createRes;
	}

	public void setCreateRes(int createRes) {
		this.createRes = createRes;
	}

	@Column(name = "oper_time")
	public Date getOperTime() {
		return operTime;
	}

	public void setOperTime(Date operTime) {
		this.operTime = operTime;
	}

	@Embeddable
	public static class Key implements Serializable {
		private static final long serialVersionUID = 1L;
		private String domainId;
		private String targetId;

		public Key() {
		}

		public Key(String domainId, String targetId) {
			this.domainId = domainId;
			this.targetId = targetId;
		}
		
		@Column(name = "domain_id", length = EntityConst.ID_LENGTH)
		public String getDomainId() {
			return domainId;
		}

		public void setDomainId(String domainId) {
			this.domainId = domainId;
		}

		@Column(name = "res_id", length = EntityConst.ID_LENGTH)
		public String getTargetId() {
			return targetId;
		}
		
		public void setTargetId(String targetId) {
			this.targetId = targetId;
		}

		@Override
		public int hashCode() {
			return targetId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			Key another = (Key) obj;
			return this.getTargetId().equals(another.getTargetId())
					&& this.getDomainId().equals(another.getDomainId());
		}

		@Override
		public String toString() {
			return String.format("%s[domainId: %s targetId:%s]", getClass().getSimpleName(),
				getDomainId(), getTargetId());
		}
	}
}
