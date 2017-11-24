package uyun.bat.gateway.dd_agent.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import uyun.bat.gateway.dd_agent.entity.DDSeriesMetric.DDSeriesMetricJsonDeserializer;
import uyun.bat.gateway.dd_agent.util.DDJsonDeserializer;

/**
 * statsd指标数据
 */
@JsonDeserialize(using = DDSeriesMetricJsonDeserializer.class)
public class DDSeriesMetric {
	/**
	 * tags
	 */
	private List<TagEntry> tags;
	/**
	 * 指标名
	 */
	private String metric;
	/**
	 * statsd聚合的频度 默认秒
	 */
	private double interval;
	/**
	 * 设备名称
	 */
	private String deviceName;
	/**
	 * 主机名
	 */
	private String host;
	/**
	 * 指标组
	 */
	private List<double[]> points;
	/**
	 * 类型
	 */
	private String type;

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
	}

	public String getMetric() {
		return metric;
	}

	public void setMetric(String metric) {
		this.metric = metric;
	}

	public double getInterval() {
		return interval;
	}

	public void setInterval(double interval) {
		this.interval = interval;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public List<double[]> getPoints() {
		return points;
	}

	public void setPoints(List<double[]> points) {
		this.points = points;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static class DDSeriesMetricJsonDeserializer extends JsonDeserializer<DDSeriesMetric> {
		@Override
		public DDSeriesMetric deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {
			JsonNode node = jp.getCodec().readTree(jp);
			DDSeriesMetric seriesMetric = new DDSeriesMetric();

			seriesMetric.metric = node.get("metric").asText();

			seriesMetric.interval = node.get("interval").asDouble();
			seriesMetric.host = node.get("host").asText();
			seriesMetric.type = node.get("type").asText();
			JsonNode temp = node.get("device_name");
			if (temp != null && !temp.isNull())
				seriesMetric.deviceName = node.get("device_name").asText();

			temp = node.get("tags");
			if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
				Iterator<JsonNode> ite = temp.getElements();
				List<TagEntry> tags = new ArrayList<TagEntry>();
				while (ite.hasNext()) {
					TagEntry tag = DDJsonDeserializer.deserializeTagEntry(ite.next());
					tags.add(tag);
				}

				seriesMetric.tags = tags;
			}

			temp = node.get("points");
			if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
				Iterator<JsonNode> ite = temp.getElements();
				double[] point = new double[2];

				List<double[]> points = new ArrayList<double[]>();
				JsonNode n = null;
				while (ite.hasNext()) {
					n = ite.next();
					point[0] = n.get(0).asLong();
					point[1] = n.get(1).asLong();
					points.add(point);
				}

				seriesMetric.points = points;
			}
			return seriesMetric;
		}
	}

}
