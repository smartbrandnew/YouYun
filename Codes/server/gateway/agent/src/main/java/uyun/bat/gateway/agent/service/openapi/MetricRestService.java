package uyun.bat.gateway.agent.service.openapi;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import uyun.bat.common.rest.ext.TimeException;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.PerfMetricBuilder;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.gateway.agent.entity.PerfMetricVO;
import uyun.bat.gateway.agent.service.api.AgentMetricService;
import uyun.bat.gateway.api.common.GatewayConstants;
import uyun.bat.gateway.api.selfmonitor.AtomicGetter;
import uyun.bat.gateway.api.service.ServiceManager;
import uyun.bat.gateway.api.service.util.Assert;
import uyun.bat.gateway.api.service.util.TimeUtil;

import com.alibaba.dubbo.config.annotation.Service;

/**
 * 只解析推送的指标数据、但推送的指标必须至少包含一个标签
 * @author WIN
 *
 */
@Service(protocol = "rest-agent", delay = 3000)
@Path("v2")
public class MetricRestService extends AtomicGetter implements AgentMetricService {
	private static AtomicLong metricAtomic = new AtomicLong();

	@POST
	@Path("single/datapoints")
	@Consumes(MediaType.APPLICATION_JSON)
	public void intakePerfMetric(List<PerfMetricVO> metrics, @Context HttpServletRequest request) {
		if (metrics == null || metrics.size() == 0)
			return;

		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);

		long currentTime = TimeUtil.getExpireTime();

		for (PerfMetricVO metric : metrics) {
			// 插入数据时间不能过早
			if (metric.getTimestamp() > currentTime)
				throw new TimeException();
			// metric必填
			if (metric.getMetric() == null || metric.getMetric().length() == 0) {
				throw new IllegalArgumentException("metric is empty！");
			}
		}

		int count = 0;
		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		for (PerfMetricVO metric : metrics) {
			PerfMetric perfMetric = builder.addMetric(metric.getMetric())
					.addDataPoint(new DataPoint(metric.getTimestamp(), metric.getValue())).addTenantId(tenantId);
			if (metric.getTags() != null && metric.getTags().size() > 0) {
				for (String t : metric.getTags()) {
					int index = t.indexOf(":");
					if (index == -1) {
						perfMetric.addTag(t, null);
					} else {
						if ((index + 1) < (t.length()))
							perfMetric.addTag(t.substring(0, index), t.substring(index + 1));
						else
							perfMetric.addTag(t, null);
					}
				}
			}
		}
		ComplexMetricData data = new ComplexMetricData(null, builder.getMetrics(),
				ComplexMetricData.TYPE_OPENAPI_SINGLE_METRICS);
		count += ServiceManager.getInstance().getCustomMetricService().insertPerf(data);
		metricAtomic.addAndGet(count);
		Assert.assertEquals(count, metrics.size());
	}

	@Override
	public long getMetricSize() {
		return metricAtomic.longValue();
	}

	@Override
	public long getEventSize() {
		return 0;
	}

}
