package com.broada.carrier.monitor.probe.impl.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.broada.carrier.monitor.common.util.SerializeUtil;
import com.broada.carrier.monitor.server.api.entity.EntityConst;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "mon_method")
public class ProbeSideMonitorMethod extends MonitorMethod {
	private static final long serialVersionUID = 1L;

	public ProbeSideMonitorMethod() {
	}

	@Column(length = EntityConst.NAME_LENGTH)
	@Override
	public String getName() {
		return super.getName();
	}

	@Column(name = "type_id", length = EntityConst.ID_LENGTH, nullable = false)
	@Override
	public String getTypeId() {
		return super.getTypeId();
	}

	public ProbeSideMonitorMethod(MonitorMethod copy) {
		super(copy);
	}

	@Id
	@Column(length = EntityConst.CODE_LENGTH)
	@Override
	public String getCode() {
		return super.getCode();
	}

	@Lob
	@Column(name = "properties", columnDefinition = "clob")
	@JsonIgnore
	public String getOptionsJson() {
		return SerializeUtil.encodeJson(getProperties());
	}

	public void setOptionsJson(String json) {
		Map<?, ?> options = SerializeUtil.decodeJson(json, Map.class);
		Map<String, Object> result = new HashMap<String, Object>();
		if (options != null) {
			for (Entry<?, ?> entry : options.entrySet())
				result.put(entry.getKey().toString(), entry.getValue());
		}
		setProperties(result);
	}

	@Override
	@Column(length = EntityConst.DESCR_LENGTH)
	public String getDescr() {
		return super.getDescr();
	}

	@Override
	public long getModified() {
		return super.getModified();
	}

	@Override
	@Column(name = "extra", columnDefinition = "clob")
	public String getExtra() {
		return super.getExtra();
	}	
}
