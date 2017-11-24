package uyun.bat.web.api.resource.entity;

import java.util.List;

import uyun.bat.datastore.api.entity.ResourceStatusCount;

/**
 * 前端说后续可能还会有参数再封装一层。。。
 */
public class StatusCount {
	private List<ResourceStatusCount> counts;

	public List<ResourceStatusCount> getCounts() {
		return counts;
	}

	public void setCounts(List<ResourceStatusCount> counts) {
		this.counts = counts;
	}

	public StatusCount() {
		super();
	}

	public StatusCount(List<ResourceStatusCount> counts) {
		super();
		this.counts = counts;
	}

}
