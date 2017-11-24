package uyun.bat.datastore.api.entity;

import java.io.Serializable;

import uyun.bat.datastore.api.util.StringUtils;

public class MetricMetaData implements Serializable {

	private static final long serialVersionUID = 1L;
	private String name;
	private String unit;
	private Double valueMin;
	private Double valueMax;
	private int precision;
	private MetricType type;
	private String typeName;
	private String cName;
	private String cDescr;
	private String integration;
	private String tenantId;

	public MetricMetaData(String name, String unit, Double valueMin, Double valueMax, int precision, MetricType type,
			String cName, String cDescr, String integration, String tenantId) {
		this.name = name;
		this.unit = unit;
		this.valueMin = valueMin;
		this.valueMax = valueMax;
		this.precision = precision;
		this.type = type;
		this.typeName = type.name();
		this.cName = cName;
		this.cDescr = cDescr;
		this.integration = integration;
		this.tenantId = tenantId;
	}

	public MetricMetaData() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getIntegration() {
		return integration;
	}

	public void setIntegration(String integration) {
		this.integration = integration;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Double getValueMin() {
		return valueMin;
	}

	public void setValueMin(Double valueMin) {
		this.valueMin = valueMin;
	}

	public Double getValueMax() {
		return valueMax;
	}

	public void setValueMax(Double valueMax) {
		this.valueMax = valueMax;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public MetricType getType() {
		if (type == null && StringUtils.isNotNull(typeName)) {
			type = MetricType.valueOf(typeName);
		}
		return type;
	}

	public void setType(MetricType type) {
		this.type = type;
	}

	public String getcName() {
		return cName;
	}

	public void setcName(String cName) {
		this.cName = cName;
	}

	public String getcDescr() {
		return cDescr;
	}

	public void setcDescr(String cDescr) {
		this.cDescr = cDescr;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public String toString() {
		return "MetricMetaData [name=" + name + ", unit=" + unit + ", valueMin=" + valueMin + ", valueMax=" + valueMax
				+ ", precision=" + precision + ", type=" + getType() + ", typeName=" + typeName + ", cName=" + cName
				+ ", cDescr=" + cDescr + ", tenantId=" + tenantId + "]";
	}

}
