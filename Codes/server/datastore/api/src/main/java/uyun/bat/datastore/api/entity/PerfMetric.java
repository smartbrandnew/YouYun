package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uyun.bat.datastore.api.util.PreConditions;

public class PerfMetric implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private List<DataPoint> dataPoints;
	private Map<String, List<String>> tags;
	private static final String TENANT_ID = "tenantId";
	private static final String RESOURCE_ID = "resourceId";

	public PerfMetric() {
		this.tags = new HashMap<String, List<String>>();
		this.dataPoints = new ArrayList<DataPoint>();
	}

	@Override
	public String toString() {
		return "Metric [name=" + name + ", dataPoints=" + dataPoints + ", tags=" + tags + "]";
	}

	public PerfMetric(String name) {
		this();
		this.name = name;
	}

	public PerfMetric(String name, List<DataPoint> dataPoints, Map<String, List<String>> tags) {

		this.name = name;
		this.dataPoints = dataPoints;
		this.tags = tags;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, List<String>> getTags() {
		return tags;
	}

	public PerfMetric addTags(Map<String, String> tags) {
		PreConditions.checkNotNull(tags);
		for (String key : tags.keySet()) {
			if (this.tags.get(key) == null) {
				this.tags.put(key, new ArrayList<String>(Arrays.asList(tags.get(key))));
			} else {
				String str = tags.get(key);
				if (str == null)
					str = "";
				this.tags.get(key).add(str);
			}

		}
		return this;
	}

	public void setTags(Map<String, List<String>> tags) {
		this.tags = tags;
	}

	public PerfMetric addTag(String key, String value) {
		if (this.tags.get(key) == null) {
			this.tags.put(key, new ArrayList<String>(Arrays.asList(value)));
		} else {
			if (value == null)
				value = "";
			this.tags.get(key).add(value);
		}
		return this;
	}

	public List<DataPoint> getDataPoints() {
		return dataPoints;
	}

	public void setDataPoints(List<DataPoint> dataPoints) {
		this.dataPoints = dataPoints;
	}

	public PerfMetric addTenantId(String tenantId) {
		if (this.tags.get(TENANT_ID) == null) {
			this.tags.put(TENANT_ID, new ArrayList<String>(Arrays.asList(tenantId)));
		} else {
			this.tags.get(TENANT_ID).clear();
			this.tags.get(TENANT_ID).add(tenantId);
		}
		return this;
	}

	public PerfMetric addResourceId(String resourceId) {
		if (this.tags.get(RESOURCE_ID) == null) {
			this.tags.put(RESOURCE_ID, new ArrayList<String>(Arrays.asList(resourceId)));
		} else {
			this.tags.get(RESOURCE_ID).clear();
			this.tags.get(RESOURCE_ID).add(resourceId);
		}
		return this;
	}

	public String getResourceId() {
		if (this.tags.get(RESOURCE_ID) != null && this.tags.get(RESOURCE_ID).size() > 0)
			return this.tags.get(RESOURCE_ID).get(0);
		return null;
	}

	public PerfMetric addDataPoint(DataPoint point) {
		this.dataPoints.add(point);
		return this;
	}

	public void checkSynstax() {
		PreConditions.checkNotNull(this.getTags().get(TENANT_ID), "tenantId不能为空");
		PreConditions.checkArgument(this.getTags().size() >= 2, "metric 必须含有2个以上标签");
	}

	public String getTenantId() {
		if (this.tags.get(TENANT_ID) != null && this.tags.get(TENANT_ID).size() > 0)
			return this.tags.get(TENANT_ID).get(0);
		return null;
	}

	// 转换精度
	public PerfMetric changePrecision(int precision) {
		if (precision < 0) {
			return this;
		}
		StringBuilder builder = new StringBuilder("#");
		if (precision > 0) {
			builder.append(".");
			for (int i = 0; i < precision; i++) {
				builder.append("0");
			}
		}
		DecimalFormat format = new DecimalFormat(builder.toString());
		for (DataPoint point : this.getDataPoints()) {
			String val = format.format(point.getValue());
			point.setValue(Double.parseDouble(val));
		}
		return this;
	}

	/**
	 * 自定义拷贝,临时仅拷贝指标名称和标签列表
	 */
	public PerfMetric clonePerfMetric() {
		PerfMetric temp = new PerfMetric();
		temp.name = name;
		temp.dataPoints.addAll(dataPoints);
		// 深拷贝指标值
		for (Entry<String, List<String>> entry : tags.entrySet()) {
			temp.tags.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
		}
		return temp;
	}

}
