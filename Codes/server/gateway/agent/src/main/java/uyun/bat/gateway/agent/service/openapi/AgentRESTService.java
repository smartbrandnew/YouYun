package uyun.bat.gateway.agent.service.openapi;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;

import org.apache.commons.lang.ArrayUtils;

import uyun.bat.common.constants.RestConstants;
import uyun.bat.common.rest.ext.TimeException;
import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.exception.Illegalargumentexception;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.BeanUtils;

import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.MetricType;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PageResource;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.PerfMetricBuilder;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.QueryMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceOpenApiQuery;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.api.mq.ResourceInfo;
import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.api.entity.EventTag;
import uyun.bat.event.api.entity.PageEvent;
import uyun.bat.gateway.agent.entity.AgentData;
import uyun.bat.gateway.agent.entity.BatchHostRequestParam;
import uyun.bat.gateway.agent.entity.CheckPointRecord;
import uyun.bat.gateway.agent.entity.CheckpointVO;
import uyun.bat.gateway.agent.entity.DataValue;
import uyun.bat.gateway.agent.entity.Device;
import uyun.bat.gateway.agent.entity.EventList;
import uyun.bat.gateway.agent.entity.EventVO;
import uyun.bat.gateway.agent.entity.HostList;
import uyun.bat.gateway.agent.entity.HostVO;
import uyun.bat.gateway.agent.entity.MetricMetaDataVO;
import uyun.bat.gateway.agent.entity.MetricSnapshoot;
import uyun.bat.gateway.agent.entity.PerfMetricVO;
import uyun.bat.gateway.agent.entity.ResourceDetailVO;
import uyun.bat.gateway.agent.entity.ResourcesTags;
import uyun.bat.gateway.agent.entity.Series;
import uyun.bat.gateway.agent.entity.SeriesRequestParam;
import uyun.bat.gateway.agent.entity.SingleHost;
import uyun.bat.gateway.agent.entity.SingleValueRequestParam;
import uyun.bat.gateway.agent.entity.StateSnapshoot;
import uyun.bat.gateway.agent.entity.newentity.EventList1;
import uyun.bat.gateway.agent.entity.newentity.EventVO1;
import uyun.bat.gateway.agent.entity.newentity.GroupBy1;
import uyun.bat.gateway.agent.entity.newentity.HostList1;
import uyun.bat.gateway.agent.entity.newentity.PerfMetricVO1;
import uyun.bat.gateway.agent.entity.newentity.SeriesRequestParam1;
import uyun.bat.gateway.agent.entity.newentity.SingleHost1;
import uyun.bat.gateway.agent.service.api.AgentService;
import uyun.bat.gateway.agent.util.HostDetail;
import uyun.bat.gateway.api.common.GatewayConstants;
import uyun.bat.gateway.api.selfmonitor.AtomicGetter;
import uyun.bat.gateway.api.service.ServiceManager;
import uyun.bat.gateway.api.service.util.Assert;
import uyun.bat.gateway.api.service.util.TimeUtil;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;
import uyun.whale.common.util.text.DateUtil;

@Service(protocol = "rest-agent", delay = 3000)
@Path("v2")
public class AgentRESTService extends AtomicGetter implements AgentService {
	private static AtomicLong metricAtomic = new AtomicLong();
	private static AtomicLong eventAtomic = new AtomicLong();

	@POST
	@Path("datapoints/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public void intakePerfMetric(List<PerfMetricVO1> metrics, @Context HttpServletRequest request) {
		if (metrics == null || metrics.size() == 0)
			return;
		String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);

		generateIntakePerfMetric(metrics, tenantId);
	}

	/**
	 * @param metrics
	 * @param tenantId
	 * @param type  用于指明数据来自agent(0)还是agentless(1)
	 */
	private void generateIntakePerfMetric(List<PerfMetricVO1> metrics, String tenantId) {
		// 用户传的资源id，及其对应的指标列表
		Map<RData, List<PerfMetricVO1>> resMetricMap = new HashMap<RData, List<PerfMetricVO1>>();
		//获取有大小值的指标元数据
		List<MetricMetaData> listMetricMetaData = ServiceManager.getInstance().getMetricMetaDataService().queryRangedMetaData(tenantId);
		Map<String, MetricMetaData> rangedMetaData = listMetricMetaData.stream().collect(
				Collectors.toMap(MetricMetaData::getName, (m) -> m));
		for (PerfMetricVO1 metric : metrics) {
			checkPerfMetricData(metric, rangedMetaData);
			// 对传的数据分组处理。
			RData rData = new RData(metric.getHost_id(), metric.getHost());
			List<PerfMetricVO1> ps = resMetricMap.get(rData);
			if (ps == null) {
				ps = new ArrayList<PerfMetricVO1>();
				resMetricMap.put(rData, ps);
			}
			ps.add(metric);
		}

		int count = 0;
		for (Map.Entry<RData, List<PerfMetricVO1>> entry : resMetricMap.entrySet()) {
			Resource resource = new Resource();
			resource.setAgentId(entry.getKey().resourceId);
			resource.setHostname(entry.getKey().hostName);
			resource.setTenantId(tenantId);
			List<String> apps = generateAPPs(entry.getValue());
			resource.setApps(apps);

			PerfMetricBuilder builder = PerfMetricBuilder.getInstance();
			for (PerfMetricVO1 metric : entry.getValue()) {
				PerfMetric perfMetric = builder.addMetric(metric.getMetric())
						.addDataPoint(new DataPoint(metric.getTimestamp(), metric.getValue())).addTenantId(tenantId);
				if (metric.getTags() != null && metric.getTags().size() > 0) {
					for (String t : metric.getTags()) {
						int index = t.indexOf(":");
						if (index == -1) {
							perfMetric.addTag(t, "");
						} else {
							if ((index + 1) < (t.length())){
								String key=t.substring(0, index);
								String val=t.substring(index + 1);
								//解析生成ip、资源类型、OS
								if("ip".equals(key)){
									resource.setIpaddr(val);
								}else if("resourceType".equals(key)){
									resource.setType(ResourceType.checkByCode(val) == null? ResourceType.SERVER:ResourceType.checkByCode(val));
								}else if("os".equals(key)){
									resource.setOs(val);
								}else
								perfMetric.addTag(key, val);
							}
							else
								perfMetric.addTag(t, "");
						}
					}
				}
			}
			ComplexMetricData data = new ComplexMetricData(resource, builder.getMetrics(), ComplexMetricData.TYPE_OPENAPI);
			count += ServiceManager.getInstance().getCustomMetricService().insertPerf(data);
			metricAtomic.addAndGet(count);
		}
		Assert.assertEquals(count, metrics.size());
	}

	//指标数据数据格式校验
	private void checkPerfMetricData(PerfMetricVO1 metric, Map<String, MetricMetaData> rangedMetaData) {
		long currentTime = TimeUtil.getExpireTime();
		// 插入数据时间不能过早
		if (String.valueOf(metric.getTimestamp()).length() != 13 && String.valueOf(metric.getTimestamp()).length() != 10)
			throw new IllegalArgumentException("The insert timestamp format is incorrect！It should be 10 or 13 bits timestamps");
		if (metric.getTimestamp() > currentTime)
			throw new IllegalArgumentException("Insert time cannot be greater than the current time of the system！");
		// metric必填
		if (metric.getMetric() == null || metric.getMetric().length() == 0) {
			throw new IllegalArgumentException("The metric name cannot be null！");
		}

		if (metric.getHost() != null && metric.getHost().length() > 64) {
			throw new IllegalArgumentException("The Host length should not exceed 64");
		}
		if (metric.getHost_id() != null && metric.getHost_id().length() > 128) {
			throw new IllegalArgumentException("The Host_id length should not exceed 128");
		}
    	// TODO 指标格式校验需要修改
//		String[] n = metric.getMetric().split("\\.");
//		int i = 0;
//		for (String string : n) {
//			i++;
//		}
//		if (i != 3)
//			throw new IllegalArgumentException("指标名格式必须为xxx.xxx.xxx的形式");

		if (null != rangedMetaData && rangedMetaData.size() > 0) {
			MetricMetaData m = rangedMetaData.get(metric.getMetric());
			if (null != m && (m.getValueMax() < metric.getValue() || m.getValueMin() > metric.getValue())) {
				throw new IllegalArgumentException("The Value must be between the maximum and the minimum");
			}
		}
		if (null == metric.getHost_id() && null == metric.getHost()
				&& (metric.getTags() == null || metric.getTags().size() == 0)) {
			throw new IllegalArgumentException("When host_id and host are empty, add at least one tag！");
		}
	}

	@POST
	@Path("datapoints/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Series> getPerfMetricList(SeriesRequestParam1 param, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		return generateGetPerfMetricList(param, tenantId);
	}

	private List<Series> generateGetPerfMetricList(SeriesRequestParam1 param, String tenantId) {
		QueryBuilder queryBuilder = new QueryBuilder();
		long currentTime = System.currentTimeMillis();
		if(StringUtils.isEmpty(param.getMetric()))
			throw new IllegalArgumentException("The metric name cannot be null!");
		if(param.getFrom() ==0 || param.getTo() == 0)
			throw new IllegalArgumentException("Start or end time can not be empty!");
		if (param.getFrom() > currentTime)
			throw new IllegalArgumentException("The start time should not be greater than the current system time!");
		if (param.getFrom() > param.getTo())
			throw new IllegalArgumentException("The start time should not be greater than the end!");
		if (param.getTo() > currentTime)
			throw new IllegalArgumentException("The end time cannot be greater than the current system time!");
		if (compareDate(new Date(param.getFrom()), new Date(param.getTo())) >= 3)
			throw new IllegalArgumentException("The starting time and ending time span cannot exceed 3 months!");
		if(null == param.getAggregator() || null == AggregatorType.checkByName(param.getAggregator()))
			throw new IllegalArgumentException("The Aggregator Type is an empty or sink mode error!Aggregator Type is optional：max、min、sum、avg、last");
		if(param.getInterval() == 0)
			throw new IllegalArgumentException("Interval can not be null!");
		queryBuilder.setStartAbsolute(param.getFrom());
		queryBuilder.setEndAbsolute(param.getTo());
		QueryMetric metric = queryBuilder.addMetric(param.getMetric()).addTenantId(tenantId);
		metric.addAggregatorType(AggregatorType.checkByName(param.getAggregator()));
		String groupBy = "";
		if (null != param.getGroup_by() && !StringUtils.isEmpty(param.getGroup_by().getTag_key())) {
			groupBy = param.getGroup_by().getTag_key();
			metric.addGrouper(groupBy);
		}
		String scope = "*";
		StringBuilder sb = new StringBuilder();
		if (param.getTags() != null && param.getTags().size() > 0) {
			for (String t : param.getTags()) {
				metric.addTag(t.split(":")[0], t.split(":").length > 1 ? t.split(":")[1] : "");
				sb.append(t);
				sb.append(",");
			}
			scope = sb.deleteCharAt(sb.length() - 1).toString();
		}
		int interval = param.getInterval();
		List<PerfMetric> perfMetricList = ServiceManager.getInstance().getMetricService()
				.querySeries(queryBuilder, interval);
		MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(param.getMetric());
		List<Series> list = new ArrayList<Series>();
		if (perfMetricList != null && perfMetricList.size() == 0)
			list.add(new Series(scope, new double[0][0], metaData != null ? metaData.getUnit() : null));
		if (perfMetricList != null && perfMetricList.size() > 0) {
			for (PerfMetric p : perfMetricList) {
				String groupScope = null;
				if (p.getDataPoints().size() >= 1000)
					throw new IllegalArgumentException("Set the granularity too small, please reset the interval's value！");
				Series series = new Series();
				if (null != param.getGroup_by() && !StringUtils.isEmpty(param.getGroup_by().getTag_key())) {
					groupScope = groupBy + ":" + p.getTags().get(groupBy);
				}
				groupScope = null == groupScope ? scope : (null == param.getTags() ? groupScope : scope + "," + groupScope);
				series.setScope(groupScope);
				List<DataPoint> dataPoints = p.getDataPoints();
				if (dataPoints != null && dataPoints.size() > 0) {
					double[][] point = new double[dataPoints.size()][2];
					for (int j = 0; j < dataPoints.size(); j++) {
						point[j][0] = dataPoints.get(j).getTimestamp();
						point[j][1] = Double.parseDouble(dataPoints.get(j).getValue().toString());
					}
					series.setPoints(point);
					series.setUnit(metaData != null ? metaData.getUnit() : null);
					list.add(series);
				}
			}
		}
		return list;
	}

	@POST
	@Path("events/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public void intakeEvent(List<EventVO1> events, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		if (events == null || events.size() == 0)
			throw new IllegalArgumentException("The query event list cannot be empty");
		generateIntakeEvent(events, tenantId);

	}

	private void generateIntakeEvent(List<EventVO1> events, String tenantId) {
		long currentTime = TimeUtil.getExpireTime();
		List<Event> eventList = new ArrayList<Event>();
		for (EventVO1 e : events) {
			// TODO 插入数据时间不能早于当前时间
			if (e.getTimestamp() > currentTime)
				throw new TimeException();
			if(e.getHost_id()==null)
				throw new Illegalargumentexception("Host_id cannot be empty");
			if(e.getMessage()==null)
				throw new Illegalargumentexception("Message cannot be empty");
			if(e.getName()==null)
				throw new Illegalargumentexception("The name cannot be empty");
			if(e.getState()==null)
				throw new Illegalargumentexception("state cannot be empty,optional:error,warning,info,success");
			if (e.getHost_id().length() > 128)
				throw new Illegalargumentexception("Host_id cannot exceed 128 characters");
			if (e.getName().length() > 100)
				throw new Illegalargumentexception("The name cannot exceed 100 characters");
			if (e.getMessage().length() > 300)
				throw new Illegalargumentexception("The message cannot exceed 300 characters");
			if(e.getTimestamp()==0)
				e.setTimestamp(System.currentTimeMillis());
			// 插入或者更新资源
			AgentData data = new AgentData(e.getHost_id());
			data.setHost(false);
			Resource resource = treateNormalResource(data, tenantId);
			Event event = new Event();
			event.setId(UUIDTypeHandler.createUUID());
			event.setOccurTime(new Timestamp(e.getTimestamp()));
			event.setMsgTitle(e.getName());
			event.setMsgContent(e.getMessage());
			event.setResId(resource.getId());
			event.setIdentity(e.getType());
			// 虽然感觉getkey挺怪异的，但是也只能遵从作者
			event.setSourceType(EventSourceType.OPEN_API.getKey());
			EventServerityType serverityType = EventServerityType.checkByValue(e.getState());
			if (null == serverityType)
				throw new IllegalArgumentException("state error,optional:error,warning,info,success");
			event.setServerity(serverityType.getKey());

			List<EventTag> eventTags = new ArrayList<EventTag>();
			if (e.getTags() != null && e.getTags().size() > 0) {

				for (String t : e.getTags()) {
					String[] temp = t.split(":");
					EventTag eventTag = new EventTag();
					if (temp.length > 1) {
						eventTag.setTagk(temp[0]);
						eventTag.setTagv(temp[1]);
						eventTag.setTenantId(tenantId);
					} else {
						eventTag.setTagk(temp[0]);
						eventTag.setTagv("");
						eventTag.setTenantId(tenantId);
					}
					eventTags.add(eventTag);
					// 事件默认带host标签
					eventTags.add(new EventTag(tenantId, "host", resource.getHostname()));
				}
				event.setEventTags(eventTags);
			}
			event.setTenantId(tenantId);
			eventList.add(event);
		}
		if (eventList.size() > 0) {
			long count = ServiceManager.getInstance().getEventService().create(eventList);
			Assert.assertEquals(count, eventList.size());
			eventAtomic.addAndGet(count);
		}
	}

	@POST
	@Path("hosts/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public void intakeHost(List<HostVO> hosts, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		if (hosts == null || hosts.size() == 0)
			return;
		for (HostVO h : hosts) {
			if (h.getId() == null)
				throw new IllegalArgumentException("Id can't be empty!");
			if (h.getIp() == null)
				throw new IllegalArgumentException("IP can't be empty!");
			if (h.getName() == null)
				throw new IllegalArgumentException("name can't be empty!");
			String regex = "^[a-z0-9A-z_.]+$";
			if (!h.getId().matches(regex))
				throw new IllegalArgumentException("Ids can only be Numbers or letters or underscores!");
			if (h.getId().length() > 128)
				throw new IllegalArgumentException("The host_id length must not exceed 128");
			if(h.getName().length()>64)
				throw new IllegalArgumentException("The name length should not exceed 64");
			if(h.getIp().length()>55)
				throw new IllegalArgumentException("IP length should not exceed 55");
			String ipRegex = "(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)\\.(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)";
			if (!h.getIp().matches(ipRegex))
				throw new IllegalArgumentException("Invalid format of the IP");
			if (h.getType() != null && h.getType().length() > 50)
				throw new IllegalArgumentException("The type length should not exceed 50");
			if (h.getOs() != null && h.getOs().length() > 50)
				throw new IllegalArgumentException("The Os length should not exceed 50");

			AgentData data = new AgentData(h.getId(), h.getName(), h.getIp(), h.getTags(), h.getApps(), h.getType(), null != h.getOs() ? h.getOs() : "", h.isOnline_state(), true);
			treateNormalResource(data, tenantId);

		}
	}

	@GET
	@Path("events")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public EventList1 getEvents(@Context HttpServletRequest request,
			@QueryParam("page_index") @DefaultValue("1") int page_index,
			@QueryParam("page_size") @DefaultValue("20") int page_size, @QueryParam("search_value") String search_value,
			@QueryParam("severity") String severity, @QueryParam("begin_time") long begin_time,
			@QueryParam("end_time") long end_time) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		if (page_size >= 1000)
			throw new IllegalArgumentException("The page_size cannot be greater than 1000！");
		Map<String, Long> map = verifySearchTime(begin_time, end_time);
		PageEvent pageEvents = ServiceManager
				.getInstance()
				.getEventService()
				.searchEvent(tenantId, page_index, page_size, search_value, severity, map.get("beginTime"),
						map.get("endTime"), null);
		List<EventVO1> list = new ArrayList<>();
		List<Event> events = pageEvents.getRows();
		if (null == events || events.size() < 1) {
			return new EventList1(page_size, page_index, new ArrayList<EventVO1>());
		}
		for (Event e : events) {
			List<String> tags = ServiceManager.getInstance().getEventService().getTagsByEventId(e.getId());
			EventVO1 event = new EventVO1(e.getId(), e.getIdentity(), e.getMsgTitle(), e.getOccurTime()
					.getTime(), e.getMsgContent(), EventServerityType.checkName(e.getServerity()), tags,
					EventSourceType.checkName(e.getSourceType()));
			list.add(event);
		}
		EventList1 eventList = new EventList1(pageEvents.getTotal(), page_size, page_index, list);
		return eventList;
	}

	@GET
	@Path("host/get")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public SingleHost1 getHostById(@Context HttpServletRequest request, @QueryParam("id") String id) {
		if(id != null && id.isEmpty())
			throw new IllegalArgumentException("hostId can not be null!");
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		Resource r = ServiceManager.getInstance().getResourceService().queryResByAgentId(id, tenantId);
		if (null == r)
			throw new IllegalArgumentException("The resource does not exist！");
		List<String> tags = new ArrayList<>();
		if (r.getTags() != null && r.getTags().size() > 0) {
			for (ResourceTag t : r.getTags()) {
				String s = t.getKey() + ":" + t.getValue();
				tags.add(s);
			}
		}
		ResourceDetailVO rVO = HostDetail.getResourceDetailById(tenantId, r.getId());
		SingleHost1 host = new SingleHost1(r.getAgentId(), r.getHostname(), r.getIpaddr(),
				r.getType().getName(), r.getModified(), tags, r.getApps(), rVO.getDev(), rVO.getInfo(), r.getOs());
		return host;
	}

	@GET
	@Path("hosts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public HostList1 getPageHosts(@Context HttpServletRequest request,
			@QueryParam("page_index") @DefaultValue("1") int page_index,
			@QueryParam("page_size") @DefaultValue("20") int page_size, @QueryParam("ip") String ip,
			@QueryParam("name") String name, @QueryParam("type") String type, @QueryParam("tags") List<String> tags,
			@QueryParam("apps") List<String> apps, @QueryParam("min_update_time") String min_update_time) {
		Date minUpdateTime = null != min_update_time ? DateUtil.parse(min_update_time) : null;
		if (null != minUpdateTime && compareDate(minUpdateTime, new Date()) >= 3)
			throw new IllegalArgumentException("The latest update cannot exceed 3 months!");
		if (page_size >= 1000)
			throw new IllegalArgumentException("The page_size cannot be greater than 1000！");
		return generateGetHosts(request, page_index, page_size, ip, name, type, tags, apps, min_update_time);
	}

	@GET
	@Path("hosts/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<HostVO> getHosts(@Context HttpServletRequest request, @QueryParam("ip") String ip,
			@QueryParam("name") String name, @QueryParam("type") String type, @QueryParam("tags") List<String> tags,
			@QueryParam("apps") List<String> apps, @QueryParam("min_update_time") String min_update_time) {

		HostList1 temp = generateGetHosts(request, 1, Integer.MAX_VALUE, ip, name, type, tags, apps, min_update_time);
		return temp.getLists();
	}

	private HostList1 generateGetHosts(HttpServletRequest request, int page_index, int page_size, String ip, String name,
			String type, List<String> tags, List<String> apps, String min_update_time) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		ResourceOpenApiQuery query = new ResourceOpenApiQuery(tenantId, ip, name, type, tags, apps,
				null != min_update_time ? DateUtil.parse(min_update_time) : null, 1, Integer.MAX_VALUE);
		PageResource pr = ServiceManager.getInstance().getResourceService().queryResListByCondition(query);
		List<HostVO> list = new ArrayList<HostVO>();
		List<Resource> resources = pr.getResources();
		if (null == resources || resources.size() < 1) {
			return new HostList1(page_size, page_index, new ArrayList<HostVO>());
		}
		for (Resource r : resources) {
			List<String> tempTags = new ArrayList<>();
			if (r.getTags() != null && r.getTags().size() > 0) {
				for (ResourceTag t : r.getTags()) {
					String s = t.getKey() + ":" + t.getValue();
					tempTags.add(s);
				}
			}
			HostVO host = new HostVO(r.getAgentId(), r.getHostname(), r.getIpaddr(), r.getType().getName(),
					r.getModified(), tempTags, r.getApps(), r.getOs(), r.getOnlineStatus().equals(OnlineStatus.ONLINE)? true : false);
			list.add(host);
		}
		HostList1 hostList = new HostList1(pr.getCount(), page_size, page_index, list);
		return hostList;
	}

	@GET
	@Path("hosts/metric_snapshoot")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<MetricSnapshoot> getMetricSnapshoot(@Context HttpServletRequest request, @QueryParam("id") String id,
			@QueryParam("metric_name") String metric_name, @QueryParam("group_by") String group_by) {
		if(id != null && id.isEmpty())
			throw new IllegalArgumentException("HostId cannot be empty!");
		if(metric_name != null && metric_name.isEmpty())
			throw new IllegalArgumentException("The performance metric name cannot be null!");
		if(group_by != null && group_by.isEmpty())
			throw new IllegalArgumentException("Grouping conditions cannot be empty!");
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		// 通过id获取设备的主键uuid
		String resId = UUIDUtils.generateResId(tenantId, id);
		List<Tag> resTag = new ArrayList<Tag>();
		resTag.add(new Tag("resourceId", resId));
		List<Tag> temp = ServiceManager.getInstance().getMetricService().getTagsByTag(tenantId, metric_name, resTag);
		List<Tag> tags = new ArrayList<Tag>();
		for (Tag t : temp) {
			if (!StringUtils.isEmpty(group_by) && t.getKey().equals(group_by))
				tags.add(t);
		}
		QueryBuilder builder = QueryBuilder.getInstance();
		List<MetricSnapshoot> list = new ArrayList<>();
		MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metric_name);
		for (Tag t : tags) {
			builder.addMetric(metric_name).addTenantId(tenantId).addResourceId(resId).addTag(t.getKey(), t.getValue());
			PerfMetric p = ServiceManager.getInstance().getMetricService().queryLastPerf(builder);
			if (p != null && p.getDataPoints().size() > 0) {
				double[] point = new double[2];
				point[0] = p.getDataPoints().get(0).getTimestamp();
				point[1] = Double.parseDouble(p.getDataPoints().get(0).getValue().toString());
				String scope = t.getKey() + ":" + t.getValue();
				MetricSnapshoot ms = new MetricSnapshoot(scope, point, metaData != null ? metaData.getUnit() : null);
				list.add(ms);
			}

		}
		return list;
	}

	@POST
	@Path("checkpoints/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public void intakeCheckPoints(List<CheckpointVO> checkpoints, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		long currentTime = TimeUtil.getExpireTime();
		if (checkpoints == null || checkpoints.size() == 0)
			return;
		for (CheckpointVO c : checkpoints) {
			if (c.getTimestamp() > currentTime)
				throw new TimeException();
			if (c.getHost_id() == null)
				throw new IllegalArgumentException("Host_id cannot be empty！");
			if (c.getState() == null)
				throw new IllegalArgumentException("State cannot be empty！");
			if (c.getValue() == null)
				throw new IllegalArgumentException("Value cannot be empty！");
			//数据逻辑验证
			if (c.getHost_id().length() > 128)
				throw new IllegalArgumentException("The host_id length must not exceed 128");
			if (c.getState().length() > 200)
				throw new IllegalArgumentException("The State is too long and the length should not exceed 200！");
			if (c.getValue().length() > 50)
				throw new IllegalArgumentException("Value is too long and the length should not exceed50！");
			if (c.getTags() != null) {
				for (String tag : c.getTags()) {
					if (tag.length() > 130)
						throw new IllegalArgumentException("The tag is too long, the key and the value length should not exceed 65！");
				}
			}
			// 通过id获取设备的主键uuid
			String resId = UUIDUtils.generateResId(tenantId, c.getHost_id());
			// 默认带租户id和资源id标签
			String[] tags = (String[]) ArrayUtils.addAll(new String[] { "tenantId:" + tenantId, "resourceId:" + resId },
					c.getTags());
			Checkpoint checkPoint = new Checkpoint(c.getState(), c.getTimestamp(), c.getValue(), tags);
			ServiceManager.getInstance().getStateService().saveCheckpoint(checkPoint);
		}
		metricAtomic.addAndGet(checkpoints.size());
	}

	@GET
	@Path("hosts/state_snapshoot")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<StateSnapshoot> getStateSnapshoot(@Context HttpServletRequest request, @QueryParam("id") String id,
			@QueryParam("state") String state) {
		if(null != id && id.isEmpty())
			throw new IllegalArgumentException("HostId cannot be empty!");
		if( null != state && state.isEmpty())
			throw new IllegalArgumentException("The state metric name cannot be null!");
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		// 通过id获取设备的主键uuid
		String resId = UUIDUtils.generateResId(tenantId, id);
		Checkpoint[] checkpoints = ServiceManager.getInstance().getStateService()
				.getCheckpoints(state, new String[] { "tenantId:" + tenantId, "resourceId:" + resId });
		List<StateSnapshoot> list = new ArrayList<>();
		for (Checkpoint c : checkpoints) {
			List<String> tags = new ArrayList<>();
			for (String t : c.getTags()) {
				if (!(t.startsWith("tenantId") || t.startsWith("resourceId")))
					tags.add(t);
			}
			StateSnapshoot s = new StateSnapshoot(tags, c.getValue());
			list.add(s);
		}
		return list;
	}

	@GET
	@Path("hosts/state_history")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<CheckPointRecord> getStateHistory(@Context HttpServletRequest request, @QueryParam("state") String state,
			@QueryParam("id") String id, @QueryParam("tags") String[] tags, @QueryParam("first_time") long first_time,
			@QueryParam("last_time") long last_time) {
		long currentTime = System.currentTimeMillis();
		if (null != id && id.isEmpty())
			throw new IllegalArgumentException("HostId cannot be empty!");
		if (null != state && state.isEmpty())
			throw new IllegalArgumentException("State metric name cannot be empty!");
		if (first_time == 0 || last_time == 0)
			throw new IllegalArgumentException("Start or end time can not be empty!!");
		if (first_time > currentTime)
			throw new IllegalArgumentException("The start time should not be greater than the current system time!");
		if (first_time > last_time)
			throw new IllegalArgumentException("The start time should not be greater than the end!");
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		// 通过id获取设备的主键uuid
		String resId = UUIDUtils.generateResId(tenantId, id);
		String[] tagList = (String[]) ArrayUtils.addAll(new String[] { "tenantId:" + tenantId, "resourceId:" + resId },
				tags);
		Checkperiod[] checkperiods = ServiceManager.getInstance().getStateService()
				.getCheckperiods(state, tagList, first_time, last_time);
		List<CheckPointRecord> checkPointRecord = new ArrayList<>();
		if (checkperiods != null && checkperiods.length > 0)
			for (Checkperiod c : checkperiods) {
				CheckPointRecord cr = new CheckPointRecord(c.getFirstTime(), c.getLastTime(), c.getValue(), c.getCount());
				checkPointRecord.add(cr);
			}
		return checkPointRecord;
	}

	@POST
	@Path("metadata/metric/create")
	@Consumes(MediaType.APPLICATION_JSON)
	public void intakeMetricMetaData(List<MetricMetaDataVO> metricMetaDatas, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		if (metricMetaDatas == null || metricMetaDatas.size() == 0)
			return;
		List<String> lists = ServiceManager.getInstance().getMetricMetaDataService().getAllMetricMetaDataName();
		for (MetricMetaDataVO m : metricMetaDatas) {
			if (m.getName() == null)
				throw new IllegalArgumentException("The metric name cannot be null！");
			if (m.getName().length() > 65)
				throw new IllegalArgumentException("The index name should not exceed 65");
			if (m.getUnit().length() > 65)
				throw new IllegalArgumentException("Unit should not exceed 65");
			if (m.getData_type().length() > 65)
				throw new IllegalArgumentException("DataType should not exceed 65");
			if (m.getCn().length() > 255)
				throw new IllegalArgumentException("The Chinese name is no longer than 255");
			if (null != m.getCdescr() && m.getCdescr().length() > 255)
				throw new IllegalArgumentException("The length of the Chinese description is no more than 255");
			if (null != m.getIntegration() && m.getIntegration().length() > 255)
				throw new IllegalArgumentException("Integration is no longer than 65");
			if (null != m.getValue_max() && m.getValue_max() < m.getValue_min())
				throw new IllegalArgumentException("The maximum is not less than the minimum");
			//yaoyao提议批量插入元数据存在的指标直接过滤的
			if (null != lists && !lists.contains(m.getName())) {
				MetricType metricType = MetricType.checkByCode(m.getData_type());
				if (null == metricType)
					throw new IllegalArgumentException("data_type error,optional:gauge,counter,rate");
				MetricMetaData metaData = new MetricMetaData(m.getName(), m.getUnit(), m.getValue_min(), m.getValue_max(),
						m.getAccuracy(), metricType, m.getCn(), m.getCdescr(), m.getIntegration(), tenantId);
				ServiceManager.getInstance().getMetricMetaDataService().insert(metaData);
			}
		}
		metricAtomic.addAndGet(metricMetaDatas.size());
	}

	@POST
	@Path("hosts/user_tag/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateResourceUserTag(ResourcesTags resourcesTags, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		if (resourcesTags == null || tenantId == null) {
			return;
		}
		List<String> ipaddrs = resourcesTags.getIpaddrs();
		List<String> tags = resourcesTags.getTags();
		if (ipaddrs == null || tags == null
				|| ipaddrs.size() < 1 || tags.size() < 1) {
			return;
		}
		List<Resource> resources =
				ServiceManager.getInstance().getResourceService().queryResourcesByIpaddrs(tenantId, ipaddrs);
		if (resources != null && resources.size() > 0) {
			for (Resource resource : resources) {
				List<String> userTags = resource.getUserTags();
				userTags = generateNewUserTags(tags, userTags);
				resource.setUserTags(userTags);
			}
			ServiceManager.getInstance().getResourceService().updateUserTagsBatch(resources);
		}
	}

	@POST
	@Path("last_value/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public DataValue getSingleValueList(SingleValueRequestParam param, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		QueryBuilder queryBuilder = new QueryBuilder();
		long currentTime = System.currentTimeMillis();
		if(StringUtils.isEmpty(param.getMetric()))
			throw new IllegalArgumentException("The metric name cannot be null!");
		if (param.getFrom() > currentTime)
			throw new IllegalArgumentException("The start time cannot be greater than the current system!");
		if (param.getTo() != 0 &&param.getFrom() > param.getTo())
			throw new IllegalArgumentException("The start time should not be greater than the end!");
		if (param.getTo() != 0 &&compareDate(new Date(param.getFrom()), new Date(param.getTo())) >= 3)
			throw new IllegalArgumentException("The starting time and ending time span cannot exceed 3 months!");
		QueryMetric metric = queryBuilder.addMetric(param.getMetric()).addTenantId(tenantId);
		if (param.getTags() != null && param.getTags().size() > 0) {
			for (String t : param.getTags()) {
				metric.addTag(t.split(":")[0], t.split(":").length > 1 ? t.split(":")[1] : "");
			}
		}
		PerfMetric perfMetric = null;
		if (param.getFrom() == 0) {
			perfMetric = ServiceManager.getInstance().getMetricService().queryLastPerf(queryBuilder);
		} else {
			queryBuilder.setStartAbsolute(param.getFrom());
			if (param.getTo() != 0)
				queryBuilder.setEndAbsolute(param.getTo());
			perfMetric = ServiceManager.getInstance().getMetricService().queryCurrentPerfMetric(queryBuilder);
		}

		if (perfMetric != null) {
			MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(param.getMetric());
			List<DataPoint> dataPoints = perfMetric.getDataPoints();
			if (dataPoints != null && dataPoints.size() > 0) {
				DataPoint dataPoint = dataPoints.get(dataPoints.size() - 1);
				DataValue dataValue = new DataValue();
				dataValue.setTimestamp(dataPoint.getTimestamp());
				dataValue.setValue((double)dataPoint.getValue());
				dataValue.setUnit(metaData != null ? metaData.getUnit() : null);
				return dataValue;
			}
		}
		return null;
	}

	private List<String> generateNewUserTags(List<String> tags, List<String> userTags) {
		Set<String> tagSet = new HashSet<String>();
		if (tags != null)
			tagSet.addAll(tags);
		if (userTags != null)
			tagSet.addAll(userTags);
		return new ArrayList<String>(tagSet);
	}

	@Override
	public long getMetricSize() {
		return metricAtomic.longValue();
	}

	@Override
	public long getEventSize() {
		return eventAtomic.longValue();
	}

	private Map<String, Long> verifySearchTime(long beginTime, long endTime) {
		if (endTime==0&& beginTime == 0) {
			endTime = System.currentTimeMillis();
			beginTime = System.currentTimeMillis() - DEFAULT_SPAN_TIME;
		} else if (beginTime == 0) {
			beginTime = endTime - DEFAULT_SPAN_TIME;
		} else if (endTime == 0) {
			endTime = beginTime + DEFAULT_SPAN_TIME;
		} else {
			if (endTime-beginTime > (long)1000*60*60*24*90) {
				throw new IllegalArgumentException("The start time and end time span should not exceed 3 months!");
			}
		}
		Map<String, Long> map = new HashMap<>();
		map.put("beginTime", beginTime);
		map.put("endTime", endTime);
		return map;
	}

	// 默认查询时间间隔一小时
	private static final long DEFAULT_SPAN_TIME = 60 * 60 * 1000;

	private Resource treateNormalResource(AgentData data, String tenantId) {
		// 根据租户id和hostId或者hostName生成资源id
		String resId = UUIDUtils.generateResId(tenantId,
				!StringUtils.isEmpty(data.getHostId()) ? data.getHostId() : data.getHostName());

		Resource resource = ServiceManager.getInstance().getResourceService().queryResById(resId,tenantId);
		Resource resource_tmp = new Resource();   // 用于判断资源上下线发送时间判断
		if(resource == null)
			resource_tmp = null;
		else
			BeanUtils.copyProperties(resource, resource_tmp);
		List<String> apps = new ArrayList<String>();
		if (null != data.getApps())
			apps = data.getApps();
		boolean flag;
		boolean rollover = true;
		ResourceType type = ResourceType.checkByCode(data.getType());
		if (resource == null) {
			// 插入资源
			Date currentDate = new Date();
			if (null == type && data.isHost())
				throw new IllegalArgumentException("type error,optional:Server,Network,VM");
			resource = new Resource(resId, currentDate, data.getHostName(), data.getHostIp(), null != type ? type
					: ResourceType.SERVER, "openapi", data.getHostId(), tenantId, apps,
					null == data.getOnlineStatus() ? OnlineStatus.ONLINE : data.getOnlineStatus(), AlertStatus.OK,
					currentDate, currentDate, data.getOs(), new ArrayList<String>(), new ArrayList<String>(),
					data.getTags() != null ? data.getTags() : new ArrayList<String>());
			flag = ServiceManager.getInstance().getResourceService().insertAsync(resource);
		} else {
			if (OnlineStatus.ONLINE.equals(resource.getOnlineStatus())) {
				rollover = false;
			} else {
				resource.setOnlineStatus(OnlineStatus.ONLINE);
			}
			// 更新apps
			if (apps.size() > 0) {
				resource.setApps(apps);
			}
//			resource.setOnlineStatus(data.getOnlineStatus());
			resource.setAgentlessTags(data.getTags() != null ? data.getTags()
					: new ArrayList<String>());
			
			if (org.apache.commons.lang.StringUtils.isNotBlank(data.getHostName()) && 
					!data.getHostName().equals(GatewayConstants.UNKNOWN) && data.getHostIp() != null)
				resource.setHostname(data.getHostName());

			if (!data.getHostIp().equals(GatewayConstants.UNKNOWN) && data.getHostIp() != null)
				resource.setIpaddr(data.getHostIp());

			resource.setLastCollectTime(new Date());
			resource.setOs(data.getOs());
			if (null != type)
				resource.setType(type);
			//如果资源下线转上线则同步处理否则异步
			if (rollover)
				flag = ServiceManager.getInstance().getResourceService().saveResourceSyncOnly(resource);
			else flag = ServiceManager.getInstance().getResourceService().updateAsync(resource);
		}

		if (!flag)
			return null;
		//	新资源上线或离线资源上线，发送一条上线消息
		if (rollover)
			ServiceManager.getInstance().getCustomResourceService().resourceOnline(resource);
		//资源下线统一由监测器判断，不在此发消息
//		if (null != resource_tmp && OnlineStatus.ONLINE.equals(resource_tmp.getOnlineStatus())
//				&& OnlineStatus.OFFLINE.equals(data.getOnlineStatus())) {
//			// 下线事件
//			ResourceInfo ri = new ResourceInfo(resource_tmp.getId(), resource_tmp.getTenantId(),
//					resource_tmp.getHostname(), resource_tmp.getLastCollectTime(), EventSourceType.OPEN_API.getKey(),
//					OnlineStatus.OFFLINE, resource_tmp.getIpaddr(), resource.getResTagsAll());
//			ServiceManager.getInstance().getCustomResourceService().readOffline(ri);
//		}

		return resource;
	}

	private List<String> generateAPPs(List<PerfMetricVO1> metrics) {
		if (metrics == null || metrics.size() == 0)
			return new ArrayList<String>();

		List<String> apps = new ArrayList<String>();
		String app = null;
		int index = -1;
		for (PerfMetricVO1 metric : metrics) {
			if (metric == null)
				continue;
			index = metric.getMetric().indexOf('.');
			if (index != -1) {
				app = metric.getMetric().substring(0, index);
				if (!apps.contains(app))
					apps.add(app);
			}
		}
		return apps;
	}

	private static class RData {
		private String hostId;
		private String hostName;

		private String resourceId;

		private RData(String hostId, String hostName) {
			super();
			this.hostId = hostId;
			this.hostName = null == hostName ? "unknown" : hostName;

			resourceId = hostId;
			if (resourceId == null || resourceId.length() == 0) {
				resourceId = null == hostName ? "unknown" : hostName;
			}
		}

		@Override
		public int hashCode() {
			return resourceId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			RData other = (RData) obj;
			if (!hostName.equals(other.hostName))
				return false;
			if (hostId == null) {
				if (other.hostId != null)
					return false;
			} else if (!hostId.equals(other.hostId))
				return false;
			return true;
		}

	}

	/**
	 * 校验时间不超过三个月
	 */
	private int compareDate(Date from, Date to) {
		int n = 0;
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		try {
			c1.setTime(from);
			c2.setTime(to);
		} catch (Exception e3) {
			throw new IllegalArgumentException("Time conversion error!");
		}
		while (!c1.after(c2)) {
			n++;
			c1.add(Calendar.MONTH, 1); // 比较月份，月份+1
		}
		n = n - 1;
		return n;
	}

	/******************************************************************************
	 * 以上是按新规范修改后的接口 以下是旧接口后续会删除
	 ******************************************************************************/

	@Deprecated
	@POST
	@Path("datapoints")
	@Consumes(MediaType.APPLICATION_JSON)
	public void oldIntakePerfMetric(List<PerfMetricVO> metrics, @Context HttpServletRequest request) {
		if (metrics == null || metrics.size() == 0)
			return;
		String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);
		List<PerfMetricVO1> newMetrics = new ArrayList<>();
		for (PerfMetricVO p : metrics) {
			newMetrics.add(new PerfMetricVO1(p.getHost(), p.getHostId(), p.getMetric(), p.getTimestamp(), p.getValue(), p
					.getTags(), p.getType()));
		}
		generateIntakePerfMetric(newMetrics, tenantId);
	}

	@Deprecated
	@POST
	@Path("datapoints/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<Series> oldGetPerfMetricList(SeriesRequestParam param, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		long currentTime = System.currentTimeMillis();
		if(StringUtils.isEmpty(param.getMetric()))
			throw new IllegalArgumentException("The metric name cannot be null!");
		if(param.getFrom() ==0 || param.getTo() == 0)
			throw new IllegalArgumentException("Start or end time is not empty!");
		if (param.getFrom() > currentTime)
			throw new IllegalArgumentException("The start time should not be greater than the current system time!");
		if (param.getFrom() > param.getTo())
			throw new IllegalArgumentException("The start time should not be greater than the end!");
		if (compareDate(new Date(param.getFrom()), new Date(param.getTo())) >= 3)
			throw new IllegalArgumentException("The starting time and ending time span cannot exceed 3 months!");
		if(null == param.getAggregator() || null == AggregatorType.checkByName(param.getAggregator()))
			throw new IllegalArgumentException("The Aggregator Type is an empty or sink mode error!Aggregator Type is optional：max、min、sum、avg、last");
		if(param.getInterval() == 0)
			throw new IllegalArgumentException("Interval can not be null!");
		String tagKey = null != param.getGroupBy()?param.getGroupBy().getTagKey():null;
		String aggregator = null != param.getGroupBy()?param.getGroupBy().getAggregator():null;
		GroupBy1 gb = new GroupBy1(tagKey, aggregator);
		SeriesRequestParam1 p1 = new SeriesRequestParam1(param.getMetric(), param.getTags(), param.getFrom(),
				param.getTo(), param.getAggregator(), param.getInterval(), gb);
		return generateGetPerfMetricList(p1, tenantId);
	}

	@Deprecated
	@POST
	@Path("events")
	@Consumes(MediaType.APPLICATION_JSON)
	public void oldIntakeEvent(List<EventVO> events, @Context HttpServletRequest request) {
		String tenantId = (String) request.getAttribute(GatewayConstants.TENANT_ID);
		if (events == null || events.size() == 0)
			return;
		List<EventVO1> newEvents = new ArrayList<>();
		for (EventVO e : events) {
			newEvents.add(new EventVO1(e.getId(), e.getType(), e.getName(), e.getTimestamp(), e.getMessage(),
					e.getState(), e.getTags(), e.getSource()));
		}
		generateIntakeEvent(newEvents, tenantId);

	}

	@Deprecated
	@POST
	@Path("hosts")
	@Consumes(MediaType.APPLICATION_JSON)
	public void oldIntakeHost(List<HostVO> hosts, @Context HttpServletRequest request) {
		intakeHost(hosts, request);
	}

	@Deprecated
	@GET
	@Path("events/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public EventList oldGetEvents(@Context HttpServletRequest request,
			@QueryParam("current") @DefaultValue("1") int current, @QueryParam("pageSize") @DefaultValue("20") int pageSize,
			@QueryParam("searchValue") String searchValue, @QueryParam("serverity") String serverity,
			@QueryParam("beginTime") long beginTime, @QueryParam("endTime") long endTime) {
		EventList1 list = getEvents(request, current, pageSize, searchValue, serverity, beginTime, endTime);
		List<EventVO> events = new ArrayList<>();
		for (EventVO1 e : list.getLists()) {
			events.add(new EventVO(e.getHost_id(), null, e.getType(), e.getName(), e.getTimestamp(), e.getMessage(), e
					.getState(), e.getTags(), e.getSource()));
		}
		return new EventList(list.getTotal(), list.getPage_index(), list.getPage_index(), events);
	}

	@Deprecated
	@GET
	@Path("hosts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public SingleHost oldGetHostById(@Context HttpServletRequest request, @QueryParam("id") String id) {
		SingleHost1 sh1 = getHostById(request, id);
		Device dev = new Device(sh1.getDev().getDesc(), sh1.getDev().isOnline_state(), sh1.getDev().getTags(), sh1.getDev()
				.getAgent_descr());
		SingleHost host = new SingleHost(dev, sh1.getInfo());
		return host;
	}

	@Deprecated
	@POST
	@Path("hosts/query")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public HostList oldGetHosts(@Context HttpServletRequest request,
			@QueryParam("current") @DefaultValue("1") int current, @QueryParam("pageSize") @DefaultValue("20") int pageSize,
			BatchHostRequestParam param) {
		HostList1 temp = getPageHosts(request, current, pageSize, param.getIp(), param.getName(), param.getType(),
				param.getTags(), param.getApps(), null != param.getMinUpdateTime()?DateUtil.format(param.getMinUpdateTime()):null);
		HostList hostList = new HostList(temp.getTotal(), temp.getPage_size(), temp.getPage_index(), temp.getLists());
		return hostList;
	}

	@Deprecated
	@GET
	@Path("hosts/metricSnapshoot")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<MetricSnapshoot> oldGetMetricSnapshoot(@Context HttpServletRequest request, @QueryParam("id") String id,
			@QueryParam("metricName") String metricName, @QueryParam("groupBy") String groupBy) {
		return getMetricSnapshoot(request, id, metricName, groupBy);
	}

	@Deprecated
	@POST
	@Path("checkpoints")
	@Consumes(MediaType.APPLICATION_JSON)
	public void oldIntakeCheckPoints(List<CheckpointVO> checkpoints, @Context HttpServletRequest request) {
		intakeCheckPoints(checkpoints, request);
	}

	@Deprecated
	@GET
	@Path("hosts/stateSnapshoot")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<StateSnapshoot> oldGetStateSnapshoot(@Context HttpServletRequest request, @QueryParam("id") String id,
			@QueryParam("state") String state) {
		return getStateSnapshoot(request, id, state);
	}

	@Deprecated
	@GET
	@Path("hosts/stateHistory")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<CheckPointRecord> oldGetStateHistory(@Context HttpServletRequest request,
			@QueryParam("state") String state, @QueryParam("id") String id, @QueryParam("tags") String[] tags,
			@QueryParam("firstTime") long firstTime, @QueryParam("lastTime") long lastTime) {
		return getStateHistory(request, state, id, tags, firstTime, lastTime);
	}

}
