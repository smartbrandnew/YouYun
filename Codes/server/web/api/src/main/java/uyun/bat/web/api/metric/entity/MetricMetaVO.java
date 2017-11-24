package uyun.bat.web.api.metric.entity;

import java.util.List;

public class MetricMetaVO implements Comparable {
	private String integration;
	private List<String> metaDatas;

	public String getIntegration() {
		return integration;
	}

	public void setIntegration(String integration) {
		this.integration = integration;
	}

	public List<String> getMetaDatas() {
		return metaDatas;
	}

	public void setMetaDatas(List<String> metaDatas) {
		this.metaDatas = metaDatas;
	}

	@Override
	public int compareTo(Object o) {
		MetricMetaVO metaVO = (MetricMetaVO) o;
		if (integration.compareTo(metaVO.getIntegration()) < 0) {
			return -1;
		}
		if (integration.compareTo(metaVO.getIntegration()) > 0) {
			return 1;
		}
		return 0;
	}
}
