package uyun.bat.gateway.dd_agent.service.rest;

import com.alibaba.dubbo.config.annotation.Service;
import uyun.bat.common.rest.ext.TimeException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.api.mq.EventInfo;
import uyun.bat.datastore.api.mq.StateMetricData;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.gateway.api.common.GatewayConstants;
import uyun.bat.gateway.api.selfmonitor.AtomicGetter;
import uyun.bat.gateway.api.service.ServiceManager;
import uyun.bat.gateway.api.service.util.TimeUtil;
import uyun.bat.gateway.dd_agent.entity.*;
import uyun.bat.gateway.dd_agent.service.api.DD_AgentService;
import uyun.bat.gateway.dd_agent.util.DDJsonDeserializer;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

@Service(protocol = "rest-dd-agent")
@Path("v2/gateway/dd-agent")
public class DD_AgentRESTService extends AtomicGetter implements DD_AgentService {
	private static String ipRegex = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
	private static Pattern pattern = Pattern.compile(ipRegex);
	
	private static AtomicLong metricAtomic = new AtomicLong();
	private static AtomicLong eventAtomic = new AtomicLong();


	@POST
	@Path("intake")
	@Consumes(MediaType.APPLICATION_JSON)
	public void intake(DDAgentData data, @Context HttpServletRequest request) {
		if (data == null)
			return;

		// 插入数据时间不能过早
		if (data.getTimestamp() > TimeUtil.getExpireTime())
			throw new TimeException();

		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);

		Set<String> tagSet = new HashSet<String>();
		if (data.getHostTags() != null) {
			// 暂时只处理system
			List<TagEntry> temp = data.getHostTags().get("system");
			if (temp != null && temp.size() > 0) {
				for (TagEntry te : temp) {
					// datastore那边tagv不能为null
					String tagV = te.getValue() != null ? te.getValue() : "";
					tagSet.add(te.getKey() + TagUtil.TAG_SEPARATOR + tagV);
				}
			}
		}

		// 创建apps
		List<String> metrics = null;
		if (data.getMetrics() != null && !data.getMetrics().isEmpty()) {
			metrics = new ArrayList<String>();
			for (int i = 0; i < data.getMetrics().size(); i++) {
				DDMetric metric = data.getMetrics().get(i);
				if (metric.getMetric().startsWith("datadog") || metric.getMetric().startsWith("datamonitor")) {
					data.getMetrics().remove(i);
					i--;
				} else {
					// hostname不一致时中间件将放在新资源显示，这里剔除
					if (metric.getHostName().equals(data.getInternalHostname()))
						metrics.add(metric.getMetric());
				}
			}
		}

		// 后续或许创建app的逻辑移交datastore
		List<String> apps = generateAPPs(metrics);

		// 资源详情及ip
		String resourceDetail = data.getGohai();
		String ip = data.getIp();
		if (ip == null) {
			// 旧版agent,没有ip,统一资源库后对应不到资源
			try {
				ip = DDJsonDeserializer.getIp(resourceDetail);
			} catch (Exception e) {
				// 如果解析gohai出异常，则可能是gohai非json格式，不予存储
				resourceDetail = null;
			}
		}

		// 插入或者更新资源
		Resource resource = new Resource();
		resource.setHostname(data.getInternalHostname());
		resource.setIpaddr(ip);

		resource.setResTags(new ArrayList<String>(tagSet));

		resource.setAgentId(data.getUuid());
		resource.setTenantId(tenantId);
		resource.setApps(apps);
		resource.setOs(data.getOs());
		//Machine_type可能为null VM PM
		ResourceType type = ResourceType.checkByCode(null == data.getMachine_type()?ResourceType.SERVER.getCode():data.getMachine_type());
		resource.setType(null == type ? ResourceType.SERVER : type);
		ResourceDetail detail = null;
		if (resourceDetail != null && resourceDetail.length() > 0) {
			String agentDesc = String.format("Monitor-Agent(%s)", data.getAgentVersion());
			detail = new ResourceDetail(null, tenantId, resourceDetail, agentDesc);
		}

		List<PerfMetric> list = null;
		if (data.getMetrics() != null && !data.getMetrics().isEmpty()) {
			PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
			PerfMetric m = null;

			for (DDMetric metric : data.getMetrics()) {
				m = builder.addMetric(metric.getMetric()).addDataPoint(new DataPoint(metric.getTimestamp(), metric.getValue()))
						.addTenantId(resource.getTenantId());

				List<TagEntry> tagList = new ArrayList<TagEntry>();

				if (metric.getTags() != null && metric.getTags().size() > 0) {
					tagList.addAll(metric.getTags());
				}
				if (metric.getHostName() != null && metric.getHostName().length() > 0) {
					tagList.add(new TagEntry("host", metric.getHostName()));
				} else {
					tagList.add(new TagEntry("host", resource.getHostname()));
				}
				if (metric.getDeviceName() != null && metric.getDeviceName().length() > 0) {
					tagList.add(new TagEntry("device", metric.getDeviceName()));
				}
				
				Matcher matcher = null;
				for (TagEntry tag : tagList) {
					// 虚拟设备特殊处理
					if ("ip".equals(tag.getKey()) && "None".equals(tag.getValue())) {
						if (matcher == null)
							matcher = pattern.matcher(metric.getHostName());
						tag.setValue(matcher.find() ? matcher.group() : "unknown");
					}
					m.addTag(tag.getKey(), tag.getValue());
				}

			}
			list = builder.getMetrics();
		}

		List<StateMetric> stateMetrics = null;
		if (null != data.getServiceChecks() && !data.getServiceChecks().isEmpty()) {
			StateMetricBuilder builder = StateMetricBuilder.getInstance();
			StateMetric stateMetric;
			for (DDServiceCheck serviceCheck : data.getServiceChecks()) {
				// 暂时只处理应用可用性状态指标
				if (serviceCheck.isNeedSave()) {
					stateMetric = builder.addMetric(serviceCheck.getCheck(), serviceCheck.getStatus() + "",
							serviceCheck.getTimestamp()).addTenantId(tenantId);
					List<TagEntry> tagList = new ArrayList<TagEntry>();
					if (serviceCheck.getTags() != null && serviceCheck.getTags().size() > 0) {
						tagList.addAll(serviceCheck.getTags());
					}
					if (serviceCheck.getHostName() != null && serviceCheck.getHostName().length() > 0) {
						tagList.add(new TagEntry("host", serviceCheck.getHostName()));
					} else {
						tagList.add(new TagEntry("host", resource.getHostname()));
					}
					for (TagEntry tag : tagList) {
						stateMetric.addTag(tag.getKey(), tag.getValue());
					}
				}
			}
			stateMetrics = builder.getMetrics();
		}

		// 插入事件暂时与指标放一块，不进行资源数据获取dubbo调用，按理说估计数据量不大
		List<EventInfo> eventList = generateEvents(data.getEvents(), resource.getHostname());
		ComplexMetricData complexMetricData = new ComplexMetricData(resource, list, ComplexMetricData.TYPE_DDAGENT, detail);
		complexMetricData.setEventInfoList(eventList);

		ServiceManager.getInstance().getCustomMetricService().insertPerf(complexMetricData);
		ServiceManager.getInstance().getCustomMetricService()
				.insertStateMetric(new StateMetricData(resource, stateMetrics));

		if (list != null)
			metricAtomic.addAndGet(list.size());

		if (eventList.size() > 0) {
			eventAtomic.addAndGet(eventList.size());
		}
	}

	/**
	 * 由于指标插入改版，资源都是异步插入的，故事件的tag先不添加资源tag
	 */
	private List<EventInfo> generateEvents(List<DDServiceEvents> events, String hostName) {
		List<EventInfo> eventList = new ArrayList<EventInfo>();
		if (events == null || events.size() == 0)
			return eventList;

		long currentTime = TimeUtil.getExpireTime();

		for (DDServiceEvents ddServiceEvents : events) {
			List<DDEvent> ddEvents = ddServiceEvents.getEvents();
			if (ddEvents != null && ddEvents.size() > 0) {
				for (DDEvent ddEvent : ddEvents) {
					// TODO 插入数据时间不能早于当前时间
					if (ddEvent.getTimestamp() > currentTime)
						throw new TimeException();

					// agent在第一次启动运行的时候,会有个启动事件且事件标题是null,故做特殊处理
					// 此类事件不进行保存 update 2016-11-18
					if (ddEvent.getMsgTitle() == null && "Agent Startup".equals(ddEvent.getEventType())) {
						continue;
					}

					EventInfo event = new EventInfo();
					event.setId(UUIDTypeHandler.createUUID());
					event.setOccurTime(new Timestamp(ddEvent.getTimestamp()));
					event.setMsgTitle(ddEvent.getMsgTitle());
					event.setMsgContent(ddEvent.getMsgText());

					// 虽然感觉getkey挺怪异的，但是也只能遵从作者
					event.setSourceType(EventSourceType.DATADOG_AGENT.getKey());

					EventServerityType eventServerityType = null;
					if (EventServerityType.ERROR.getValue().equals(ddEvent.getAlertType())) {
						eventServerityType = EventServerityType.ERROR;
					} else if (EventServerityType.WARNING.getValue().equals(ddEvent.getAlertType())) {
						eventServerityType = EventServerityType.WARNING;
					} else if (EventServerityType.SUCCESS.getValue().equals(ddEvent.getAlertType())) {
						eventServerityType = EventServerityType.SUCCESS;
					} else {
						eventServerityType = EventServerityType.INFO;
					}

					event.setServerity(eventServerityType.getKey());

					Set<String> tagSet = new HashSet<String>();
					// 事件默认带host标签
					if (ddEvent.getHost() != null && ddEvent.getHost().length() > 0) {
						tagSet.add("host" + TagUtil.TAG_SEPARATOR + ddEvent.getHost());
					} else {
						tagSet.add("host" + TagUtil.TAG_SEPARATOR + hostName);
					}
					if (ddEvent.getTags() != null && ddEvent.getTags().size() > 0) {
						for (TagEntry tagEntry : ddEvent.getTags()) {
							String tagV = tagEntry.getValue() != null ? tagEntry.getValue() : "";
							tagSet.add(tagEntry.getKey() + TagUtil.TAG_SEPARATOR + tagV);
						}
					}

					List<Tag> eventTags = new ArrayList<Tag>();
					for (String tag : tagSet) {
						Tag one = TagUtil.string2Tag(tag);
						if (one == null) {
							continue;
						}
						eventTags.add(one);
					}

					event.setEventTags(eventTags);

					// 创建事件标识,暂时没地方保存汇聚id
					String identity = ddEvent.getEventType();
					if (identity == null || identity.length() == 0)
						identity = event.getMsgTitle();
					if (identity == null || identity.length() == 0)
						identity = ddEvent.getAggregationKey();
					if (identity == null || identity.length() == 0)
						identity = GatewayConstants.UNKNOWN;
					event.setIdentity(identity);

					eventList.add(event);
				}
			}
		}

		return eventList;
	}

	private List<String> generateAPPs(List<String> metrics) {
		if (metrics == null || metrics.size() == 0)
			return new ArrayList<String>();

		List<String> apps = new ArrayList<String>();
		String app = null;
		int index = -1;
		for (String metric : metrics) {
			if (metric == null)
				continue;
			index = metric.indexOf('.');
			if (index != -1) {
				app = metric.substring(0, index);
				if (!apps.contains(app))
					apps.add(app);
			}
		}
		return apps;
	}

	@POST
	@Path("intake/metrics")
	public void intakeMetrics(String data, @Context HttpServletRequest request) {
	}

	@POST
	@Path("intake/metadata")
	public void intakeMedata(String data, @Context HttpServletRequest request) {
	}

	@POST
	@Path("api/v1/series")
	@Consumes(MediaType.APPLICATION_JSON)
	public void series(DDSeries series, @Context HttpServletRequest request) {
		// 不能定位到agent
		if (series == null || series.getUuid() == null || series.getUuid().length() == 0)
			return;
		// 没有指标
		if (series.getSeries() == null || series.getSeries().size() == 0)
			return;

		// BAT-34 隐藏系统内置指标
		for (int i = 0; i < series.getSeries().size(); i++) {
			DDSeriesMetric metric = series.getSeries().get(i);
			if (metric.getMetric().startsWith("datadog") || metric.getMetric().startsWith("datamonitor")) {
				series.getSeries().remove(i);
				i--;
			}
		}
		if (series.getSeries().size() == 0)
			return;

		// 生成对应的资源数据
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);

		String hostname = null;
		List<String> metrics = new ArrayList<String>();
		// 按理说所有的host是一样的
		for (DDSeriesMetric metric : series.getSeries()) {
			metrics.add(metric.getMetric());
			if (hostname == null && metric.getHost() != null && metric.getHost().length() > 0) {
				hostname = metric.getHost();
			}
		}

		List<String> apps = generateAPPs(metrics);

		Resource resource = new Resource();
		resource.setHostname(hostname);
		resource.setAgentId(series.getUuid());
		resource.setTenantId(tenantId);
		resource.setApps(apps);
		resource.setIpaddr(series.getIp());
		
		long currentTime = TimeUtil.getExpireTime();
		PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
		PerfMetric m = null;
		for (DDSeriesMetric metric : series.getSeries()) {
			if (metric.getPoints() != null && metric.getPoints().size() > 0) {
				m = builder.addMetric(metric.getMetric()).addTenantId(resource.getTenantId());
				for (double[] data : metric.getPoints()) {
					long timestamp = (long) data[0];
					if (timestamp > currentTime)
						throw new TimeException();
					m.addDataPoint(new DataPoint(timestamp, data[1]));
				}
			} else {
				continue;
			}

			List<TagEntry> tagList = new ArrayList<TagEntry>();

			if (metric.getTags() != null && metric.getTags().size() > 0) {
				tagList.addAll(metric.getTags());
			}
			if (metric.getHost() != null && metric.getHost().length() > 0) {
				tagList.add(new TagEntry("host", metric.getHost()));
			}
			if (metric.getDeviceName() != null && metric.getDeviceName().length() > 0) {
				tagList.add(new TagEntry("device", metric.getDeviceName()));
			}

			if (tagList.size() > 0) {
				for (TagEntry tag : tagList) {
					m.addTag(tag.getKey(), tag.getValue());
				}
			}
		}
		List<PerfMetric> perfMetricList = builder.getMetrics();
		if (perfMetricList.isEmpty())
			return;

		ComplexMetricData complexMetricData = new ComplexMetricData(resource, perfMetricList,
				ComplexMetricData.TYPE_DDAGENT_STATSD);
		ServiceManager.getInstance().getCustomMetricService().insertPerf(complexMetricData);
		metricAtomic.addAndGet(perfMetricList.size());
	}

	@POST
	@Path("api/v1/check_run")
	@Consumes(MediaType.APPLICATION_JSON)
	public void check_run(List<DDServiceCheck> checks, @Context HttpServletRequest request) {
		if (checks == null || checks.size() == 0)
			return;
		// 暂时不插入状态指标
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);

		// 插入或者更新资源
		Resource resource = new Resource();
		resource.setAgentId(checks.get(0).getUuid());
		resource.setTenantId(tenantId);
		resource.setIpaddr(checks.get(0).getIp());
		List<StateMetric> stateMetrics = null;
		if (null != checks && !checks.isEmpty()) {
			StateMetricBuilder builder = StateMetricBuilder.getInstance();
			StateMetric stateMetric;
			for (DDServiceCheck serviceCheck : checks) {
				// 暂时只处理应用可用性状态指标
				if (serviceCheck.isNeedSave()) {
					stateMetric = builder.addMetric(serviceCheck.getCheck(), serviceCheck.getStatus() + "",
							serviceCheck.getTimestamp()).addTenantId(tenantId);
					List<TagEntry> tagList = new ArrayList<TagEntry>();
					if (serviceCheck.getTags() != null && serviceCheck.getTags().size() > 0) {
						tagList.addAll(serviceCheck.getTags());
					}
					if (serviceCheck.getHostName() != null && serviceCheck.getHostName().length() > 0) {
						tagList.add(new TagEntry("host", serviceCheck.getHostName()));
					} /*
						 * else { tagList.add(new TagEntry("host", resource.getHostname()));
						 * }
						 */
					for (TagEntry tag : tagList) {
						stateMetric.addTag(tag.getKey(), tag.getValue());
					}
				}
			}
			stateMetrics = builder.getMetrics();
		}
		ServiceManager.getInstance().getCustomMetricService()
				.insertStateMetric(new StateMetricData(resource, stateMetrics));
	}

	@POST
	@Path("intake/ping")
	@Consumes(MediaType.APPLICATION_JSON)
	public void ping(NetDevData data, @Context HttpServletRequest request) {
		if (data == null)
			return;
		// 插入数据时间不能过早
		if (data.getTimestamp() > TimeUtil.getExpireTime())
			throw new TimeException();

		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);

		// agent的网络设备详情和指标分开传
		Resource resource = new Resource();
		resource.setTenantId(tenantId);
		resource.setAgentId(data.getIdentity());
		ResourceDetail detail = null;
		if (data.getNetEquipment() != null) {
			// 插入资源tag
			Set<String> tags = new HashSet<String>();
			for (TagEntry te : getNetDevTags(data)) {
				String tagV = te.getValue() != null ? te.getValue() : "";
				tags.add(te.getKey() + TagUtil.TAG_SEPARATOR + tagV);
			}

			// 插入或者更新资源
			resource.setHostname(data.getNetEquipment().getHost());
			resource.setIpaddr(data.getNetEquipment().getIp());
			resource.setResTags(new ArrayList<String>(tags));
			resource.setOs("netdev");

			if (data.getNetEquipment().getDescr() != null && data.getNetEquipment().getDescr().length() > 0) {
				String agentDesc = String.format("DataMonitor-Agent(%s)", data.getAgentVersion());
				detail = new ResourceDetail(null, tenantId, data.getNetEquipment().getDescr(), agentDesc);
			}
		}

		List<PerfMetric> list = null;
		if (data.getMetrics() != null && !data.getMetrics().isEmpty()) {
			// 设置apps
			List<String> metrics = null;
			metrics = new ArrayList<String>();
			for (DDMetric metric : data.getMetrics()) {
				metrics.add(metric.getMetric());
			}
			List<String> apps = generateAPPs(metrics);
			resource.setApps(apps);

			// 创建指标
			PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
			PerfMetric m = null;

			for (DDMetric metric : data.getMetrics()) {
				m = builder.addMetric(metric.getMetric()).addDataPoint(new DataPoint(metric.getTimestamp(), metric.getValue()))
						.addTenantId(tenantId);

				List<TagEntry> tagList = new ArrayList<TagEntry>();

				if (metric.getTags() != null && metric.getTags().size() > 0) {
					tagList.addAll(metric.getTags());
				}

				for (TagEntry tag : tagList) {
					m.addTag(tag.getKey(), tag.getValue());
				}

			}
			list = builder.getMetrics();
		}
		// list可能是null，即发现了资源，但是其指标都取不到
		ComplexMetricData complexMetricData = new ComplexMetricData(resource, list,
				ComplexMetricData.TYPE_DDAGENT_NETEQUIPMENT, detail);
		ServiceManager.getInstance().getCustomMetricService().insertPerf(complexMetricData);
		if (list != null)
			metricAtomic.addAndGet(list.size());
	}

	/**
	 * 获取网络设备的tags
	 */
	private List<TagEntry> getNetDevTags(NetDevData data) {
		List<TagEntry> tes = new ArrayList<TagEntry>();

		if (data.getNetEquipment().getProducer() != null && data.getNetEquipment().getProducer().length() > 0)
			tes.add(new TagEntry("producer", data.getNetEquipment().getProducer()));

		if (data.getNetEquipment().getType() != null && data.getNetEquipment().getType().length() > 0)
			tes.add(new TagEntry("equipment", data.getNetEquipment().getType()));

		if (data.getNetCollectorTags() != null && data.getNetCollectorTags().size() > 0) {
			for (TagEntry te : data.getNetCollectorTags()) {
				if (!tes.contains(te))
					tes.add(new TagEntry(te.getKey(), te.getValue()));
			}
		}

		return tes;
	}

	@GET
	@Path("correct/time")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public long getCurrentTime(@Context HttpServletRequest request) {
		return System.currentTimeMillis();
	}

	@Override
	public long getMetricSize() {
		return metricAtomic.longValue();
	}

	@Override
	public long getEventSize() {
		return eventAtomic.longValue();
	}

}
