package uyun.bat.datastore.api.mq;

import java.io.Serializable;
import java.util.List;

import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceDetail;

/**
 * 异步处理复杂指标数据 包含指标和资源数据
 */
public class ComplexMetricData implements Serializable {

	/**
	 * <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	// 这部分的数字请按顺序增加，不要特意整改。其对应list数据的index
	public static int TYPE_OPENAPI = 0;
	public static int TYPE_DDAGENT = 1;
	public static int TYPE_DDAGENT_STATSD = 2;
	public static int TYPE_DDAGENT_NETEQUIPMENT = 3;
	public static int TYPE_OPENAPI_SINGLE_METRICS = 4;
	/**
	 * 资源
	 */
	private Resource resource;
	/**
	 * 指标列表
	 */
	private List<PerfMetric> perfMetricList;
	
	private List<EventInfo> eventInfoList;
	/**
	 * 采用什么样的处理方式
	 */
	private int type;

	/**
	 * 资源详情
	 */
	private ResourceDetail resourceDetail;

	public ComplexMetricData() {
		super();
	}

	public ComplexMetricData(Resource resource, List<PerfMetric> perfMetricList, int type) {
		this(resource, perfMetricList, type, null);
	}

	public ComplexMetricData(Resource resource, List<PerfMetric> perfMetricList, int type, ResourceDetail resourceDetail) {
		super();
		this.resource = resource;
		this.perfMetricList = perfMetricList;
		this.type = type;
		this.resourceDetail = resourceDetail;
	}

	public List<EventInfo> getEventInfoList() {
		return eventInfoList;
	}

	public void setEventInfoList(List<EventInfo> eventInfoList) {
		this.eventInfoList = eventInfoList;
	}

	public ResourceDetail getResourceDetail() {
		return resourceDetail;
	}

	public void setResourceDetail(ResourceDetail resourceDetail) {
		this.resourceDetail = resourceDetail;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public List<PerfMetric> getPerfMetricList() {
		return perfMetricList;
	}

	public void setPerfMetricList(List<PerfMetric> perfMetricList) {
		this.perfMetricList = perfMetricList;
	}

}
