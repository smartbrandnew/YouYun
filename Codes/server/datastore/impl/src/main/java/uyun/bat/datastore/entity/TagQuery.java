package uyun.bat.datastore.entity;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.api.entity.TimeUnit;

public class TagQuery implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(TagQuery.class);
	private RelativeTime start_relative;
	private List<TagMetric> metrics = new ArrayList<TagMetric>();

	public TagQuery(RelativeTime start_relative, String metricName, String tenantId) {
		this.metrics.add(new TagMetric(metricName, tenantId));
		this.start_relative = start_relative;
	}

	public TagQuery(RelativeTime start_relative, TagMetric metric) {
		this.metrics.add(metric);
		this.start_relative = start_relative;
	}

	public void setRelativeTime(int duration, TimeUnit unit) {
		checkArgument(duration > 0);
		checkNotNull(unit);
		this.start_relative = new RelativeTime(duration, unit);
		checkArgument(start_relative.getTimeRelativeTo(System.currentTimeMillis()) <= System.currentTimeMillis(),
				"Start time cannot be in the future.");
	}

	public List<TagMetric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<TagMetric> metrics) {
		this.metrics = metrics;
	}

	public void setStart_relative(RelativeTime start_relative) {
		this.start_relative = start_relative;
	}

	public RelativeTime getStart_relative() {
		return start_relative;
	}

	public String toJsonString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String json = mapper.writeValueAsString(this);
			return json;
		} catch (JsonGenerationException e) {
			logger.warn("convert to json failed: ", e);
		} catch (JsonMappingException e) {
			logger.warn("convert to json failed： ", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.warn("IO Exception：", e);
		}
		return null;
	}

	public static void main(String[] args) {
		TagQuery tagQuery = new TagQuery(new RelativeTime(24, TimeUnit.HOURS), "cpu", UUID.randomUUID().toString());
		System.out.println(tagQuery.toJsonString());
	}

}
