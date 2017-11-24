package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uyun.bat.datastore.api.util.PreConditions;

public class QueryMetric implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Order
	{
		ASCENDING("asc"),
		DESCENDING("desc");

		private String text;

		private Order() {

		}

		Order(String text)
		{
			this.text = text;
		}

		@Override
		public String toString()
		{
			return this.text;
		}

		public static Order checkByName(String name) {
			for (Order type : Order.values()) {
				if (type.text.equals(name)) {
					return type;
				}
			}
			return null;
		}
	}

	private static final String tenant_id = "tenantId";

	private static final String resource_id = "resourceId";

	private String name;

	private Map<String, List<String>> tags = new HashMap<String, List<String>>();
	/**
	 * group by根据tagName
	 */
	private final List<String> groupers = new ArrayList<String>();
	
	private final List<String> excludes = new ArrayList<String>();

	private AggregatorType aggregatorType;

	private Integer limit;

	private Order order;

	QueryMetric() {

	}

	QueryMetric(String name)
	{
		this.name = PreConditions.checkNotNull(name);
		this.aggregatorType = AggregatorType.avg;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getLimit() {
		return limit;
	}

	public QueryMetric setLimit(int limit) {
		PreConditions.checkArgument(limit > 0, "limit must be greater than 0");
		this.limit = limit;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setTags(Map<String, List<String>> tags) {
		this.tags = tags;
	}

	public Map<String, List<String>> getTags() {
		return tags;
	}

	public List<String> getGroupers() {
		return groupers;
	}

	public Order getOrder() {
		return order;
	}

	public QueryMetric addMultiValuedTags(Map<String, List<String>> tags)
	{
		PreConditions.checkNotNull(tags);

		for (String key : tags.keySet())
		{
			if (tags.get(key) == null) {
				this.tags.put(key, tags.get(key));
			} else {
				this.tags.get(key).addAll(tags.get(key));
			}
		}

		return this;
	}

	public QueryMetric addTags(Map<String, String> tags)
	{
		PreConditions.checkNotNull(tags);
		for (String key : tags.keySet())
		{
			if (this.tags.get(key) == null) {
				this.tags.put(key, new ArrayList<String>(Arrays.asList(tags.get(key))));
			} else {
				this.tags.get(key).add(tags.get(key));
			}

		}
		return this;
	}

	public QueryMetric addTag(String name, String... values)
	{
		PreConditions.checkNotNull(name);
		PreConditions.checkArgument(values.length > 0);

		for (String value : values)
		{
			PreConditions.checkNotNull(value);
		}
		if (tags.get(name) == null) {
			tags.put(name, new ArrayList<String>(Arrays.asList(values)));
		} else {
			tags.get(name).addAll(Arrays.asList(values));
		}

		return (this);
	}

	public QueryMetric addAggregatorType(AggregatorType aggregatorType)
	{
		PreConditions.checkNotNull(aggregatorType);
		this.aggregatorType = aggregatorType;
		return this;
	}

	public QueryMetric addGrouper(String tagName)
	{
		PreConditions.checkNotNull(tagName);
		groupers.add(tagName);
		return this;
	}

	public QueryMetric addExclude(String exclude)
	{
		PreConditions.checkNotNull(exclude);
		excludes.add(exclude);
		return this;
	}
	
	public List<String> getExcludes()
	{
		return excludes;
	}
	
	public AggregatorType getAggregatorType() {
		return aggregatorType;
	}

	public QueryMetric setOrder(Order order)
	{
		PreConditions.checkNotNull(order);
		this.order = order;
		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		QueryMetric that = (QueryMetric) o;

		if (!name.equals(that.name))
			return false;
		if (tags != null ? !tags.equals(that.tags) : that.tags != null)
			return false;
		if (groupers != null ? !groupers.equals(that.groupers) : that.groupers != null)
			return false;
		if (aggregatorType != null ? !aggregatorType.equals(that.aggregatorType) : that.aggregatorType != null)
			return false;
		if (limit != null ? !limit.equals(that.limit) : that.limit != null)
			return false;
		return order == that.order;
	}

	@Override
	public int hashCode()
	{
		int result = name.hashCode();
		result = 31 * result + (tags != null ? tags.hashCode() : 0);
		result = 31 * result + (groupers != null ? groupers.hashCode() : 0);
		result = 31 * result + (aggregatorType != null ? aggregatorType.hashCode() : 0);
		result = 31 * result + (limit != null ? limit.hashCode() : 0);
		result = 31 * result + (order != null ? order.hashCode() : 0);
		return result;
	}

	public QueryMetric addTenantId(String tenantId) {
		if (this.tags.get(tenant_id) == null) {
			this.tags.put(tenant_id, new ArrayList<String>(Arrays.asList(tenantId)));
		} else {
			this.tags.get(tenant_id).add(tenantId);
		}
		return this;
	}

	public QueryMetric addResourceId(String resourceId) {
		if (this.tags.get(resource_id) == null) {
			this.tags.put(resource_id, new ArrayList<String>(Arrays.asList(resourceId)));
		} else {
			this.tags.get(resource_id).add(resourceId);
		}
		return this;
	}

	public boolean checkSyntax() {
		if (this.tags.get(tenant_id) == null || this.tags.get(tenant_id).size() <= 0)
			return false;
		return true;
	}
}
