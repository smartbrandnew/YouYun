package uyun.bat.gateway.dd_agent.entity;

import java.util.List;

/**
 * statsd上传的数据集合
 */
public class DDSeries {
	private List<DDSeriesMetric> series;
	/**
	 * agent对应的uuid
	 */
	private String uuid;
	
	private String ip;
	
	public List<DDSeriesMetric> getSeries() {
		return series;
	}

	public void setSeries(List<DDSeriesMetric> series) {
		this.series = series;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}
