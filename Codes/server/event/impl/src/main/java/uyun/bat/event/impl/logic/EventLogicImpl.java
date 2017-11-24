package uyun.bat.event.impl.logic;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.index.query.support.QueryInnerHitBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.InternalTopHits;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHitsBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uyun.bat.common.config.Config;
import uyun.bat.event.api.entity.*;
import uyun.bat.event.api.logic.EventLogic;
import uyun.bat.event.api.mq.EventInfo;
import uyun.bat.event.api.util.StringUtils;
import uyun.bat.event.impl.common.ServiceManager;
import uyun.bat.event.impl.logic.elasticsearch.ElasticSearchService;
import uyun.bat.event.impl.logic.redis.EventRedisService;
import uyun.bat.event.impl.mq.MQManager;
import uyun.bat.event.impl.util.DateUtil;
import uyun.bat.event.impl.util.EventSearchTimeCalculateUtil;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;
import uyun.whale.common.util.error.ErrorUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@Component(value = "eventLogic")
public class EventLogicImpl implements EventLogic {

	private static final Logger logger = LoggerFactory.getLogger(EventLogicImpl.class);
	//ttl暂写死为32天
	private int ttl = Config.getInstance().get("tenant.authority.event.ttl", 32);
	@Autowired
	private EventRedisService eventRedisService;

	@Autowired
	private ElasticSearchService elasticSearchService;
	//事件插入计数器
	private AtomicLong insertAtomic = new AtomicLong();
	//分页事件查询计数器
	private AtomicLong queryEventPageAtomic = new AtomicLong();
	//事件统计台查询计数器
	private AtomicLong queryEventGraphAtomic = new AtomicLong();
	//事件插入失败计数器
	private AtomicLong insertFailedAtomic = new AtomicLong();
	//事件查询失败计数器
	private AtomicLong queryFailedAtomic = new AtomicLong();
	/**
	 * 关联事件数最多1k个
	 */
	public static final int MAX_RELATE_COUNT = 1000;

	public long getInsertAtomic() {
		return insertAtomic.longValue();
	}

	public long getEventPageAtomic() {
		return queryEventPageAtomic.longValue();
	}

	public long getEventGraphAtomic() {
		return queryEventGraphAtomic.longValue();
	}

	public long getInsertFailedAtomic() {
		return insertFailedAtomic.longValue();
	}

	public long getQueryFailedAtomic() {
		return queryFailedAtomic.longValue();
	}

	@Override
	public Event create(Event event) {
		// 发送事件到 alert队列
		MQManager.getInstance().getEventMQService().alertEventSaved(event);
		event.verifyEvent();
		String faultId = UUIDTypeHandler.createUUID();
		try {
			EventFault eventFault = eventRedisService.getEventByFaultId(event, faultId);
			if (null != eventFault && eventFault.getRelateCount() <= MAX_RELATE_COUNT) {
				event.setFirstRelateTime(new Date(eventFault.getFirstRelateTime()));
				event.setRelateCount((int) eventFault.getRelateCount());
				event.setFaultId(eventFault.getFaultId());
				elasticSearchService.getBulkProcess().add(
						new IndexRequest(elasticSearchService.getIndexName(), elasticSearchService.faultType, event.getFaultId())
								.source(buildFaultJson(event,eventFault.isRecover())).ttl(new TimeValue(ttl, TimeUnit.DAYS)));
				elasticSearchService.getBulkProcess().add(
						new IndexRequest(elasticSearchService.getIndexName(), elasticSearchService.eventType, event.getId())
								.source(buildEventJson(event)).parent(event.getFaultId()).ttl(new TimeValue(ttl, TimeUnit.DAYS)));
				insertAtomic.incrementAndGet();
				pushToEventMQ(event);
			}
		} catch (Exception e) {
			insertFailedAtomic.incrementAndGet();
			throw new RuntimeException("Event create failed!");
		}
		return event;
	}

	@Override
	public long create(List<Event> events) {
		for (Event event : events) {
			create(event);
		}
		return events.size();
	}

	private void pushToEventMQ(Event event) {
		EventInfo eventInfo = new EventInfo(event.getMsgTitle(), event.getMsgContent(), event.getTenantId(),
				event.getResId(), event.getIdentity(), event.getOccurTime(), event.getServerity());
		List<Tag> tags = new ArrayList<Tag>();
		if (null != event.getEventTags() && event.getEventTags().size() > 0) {
			for (EventTag eventTag : event.getEventTags()) {
				tags.add(new Tag(eventTag.getTagk(), eventTag.getTagv()));
			}
		}
		eventInfo.setTags(tags);
		MQManager.getInstance().getEventMQService().eventSaved(eventInfo);
	}

	private XContentBuilder buildFaultJson(Event event, boolean recover) {
		try {
			XContentBuilder builder = jsonBuilder()
					.startObject()
					.field("fault_id", event.getFaultId())
					.field("tenant_id", event.getTenantId())
					.field("relate_count", event.getRelateCount())
					.field("first_time", event.getFirstRelateTime())
					.field("recover", recover)
					.endObject();
			return builder;
		} catch (IOException e) {
			ErrorUtil.warn(logger, "Event create build fault Json data failed", e);
		}
		return null;
	}

	private XContentBuilder buildEventJson(Event event) {
		try {
			XContentBuilder builder = jsonBuilder()
					.startObject()
					.field("id", event.getId())
					.field("tenant_id", event.getTenantId())
					.field("fault_id", event.getFaultId())
					.field("res_id", event.getResId())
					.field("msg_title", event.getMsgTitle())
					.field("msg_content", event.getMsgContent())
					.field("source_type", event.getSourceType())
					.field("serverity", event.getServerity())
					.field("monitor_id", event.getMonitorId())
					.field("identity", event.getIdentity())
					.field("occur_time", event.getOccurTime())
					.field("sort_id", getSortId(event.getOccurTime().getTime()))
					//查询需要的格式host xxx ip 10.1.10.111o
					.field("tags", getTags(event.getEventTags(),pattern3,pattern3))
					//原本的tags格式host:xxx;ip:10.1.10.111
					.field("origin_tags", getTags(event.getEventTags(),pattern1,pattern2))
					.field("host", getIpAndHostName(event.getEventTags()).get("host"))
					.field("ip", getIpAndHostName(event.getEventTags()).get("ip"))
					.endObject();
			return builder;
		} catch (IOException e) {
			ErrorUtil.warn(logger, "Event create build event Json data failed", e);
		}
		return null;
	}

	private int getSortId(long now) {
		long begin = DateUtil.str2Time("2016-08-25 00:00:00").getTime();
		long a = (now / 1000 - begin / 1000);
		return (int) a;
	}

	private static final String pattern1=":";
	private static final String pattern2=";";
	private static final String pattern3=" ";

	private String getTags(List<EventTag> eventTags,String p1,String p2) {
		StringBuilder sb = new StringBuilder();
		if (null != eventTags && eventTags.size() > 0) {
			int size = eventTags.size();
			for (int i = 0; i < size; i++) {
				EventTag tag = eventTags.get(i);
				//tag移除默认的host和ip标签
				if(tag.getTagk().equals("host") || tag.getTagk().equals("ip"))
					continue;
				sb.append(tag.getTagk());
				if (null != tag.getTagv() && !"".equals(tag.getTagv())) {
					sb.append(p1);
					sb.append(tag.getTagv());
				}
				if (i < size - 1) {
					sb.append(p2);
				}
			}
		}
		return sb.toString();
	}

	private Map<String, String> getIpAndHostName(List<EventTag> eventTags) {
		Map<String, String> map = new HashMap<>();
		if (null != eventTags && eventTags.size() > 0) {
			for (EventTag t : eventTags) {
				if (t.getTagk().equals("host"))
					map.put("host", t.getTagv());
				if (t.getTagk().equals("ip"))
					map.put("ip", t.getTagv());
			}
		} else {
			map.put("host", "unknown");
			map.put("ip", "unknown");
		}
		return map;
	}

	@Override
	public PageEvent searchEvent(String tenantId, int currentPage, int pageSize, String searchValue, String serverity,
			long beginTime, long endTime, Integer granularity) {
		Date begin;
		Date end;
		if (null != granularity) {
			Map<String, Object> map = EventSearchTimeCalculateUtil.getSearchTime(beginTime, endTime, granularity);
			begin = (Date) map.get("begin");
			end = (Date) map.get("end");
		} else {
			begin =new Date(beginTime);
			end = new Date(endTime);
		}
		int from = (currentPage - 1) * pageSize;
		HasChildQueryBuilder builder = QueryBuilders
				.hasChildQuery(elasticSearchService.eventType, QueryBuilders.functionScoreQuery(
						getCommonQueryBuilder(tenantId, begin, end, searchValue, serverity))
						.add(new ScriptScoreFunctionBuilder(new Script("doc['sort_id'].value")))
						.boostMode("replace"))
				.scoreMode("max")
				.innerHit(new QueryInnerHitBuilder().setSize(1).addSort(new FieldSortBuilder("sort_id").order(SortOrder.DESC)));
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setQuery(builder)
				.addSort(new FieldSortBuilder("_score").order(SortOrder.DESC))
				.setFrom(from)
				.setSize(pageSize)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		int total = (int) hits.getTotalHits();
		List<Event> events = new ArrayList<>();
		if (null != searchHists && searchHists.length > 0) {
			for (SearchHit schHit : searchHists) {
				buildSearchEvent(events, schHit);
			}
		}
		buildEventMeta(events);
		PageEvent pageEvent = new PageEvent();
		pageEvent.setCurrentPage(currentPage);
		pageEvent.setPageSize(pageSize);
		pageEvent.setBeginTime(begin);
		pageEvent.setEndTime(end);
		pageEvent.setRows(events);
		pageEvent.setTotal(total);
		queryEventPageAtomic.incrementAndGet();
		return pageEvent;
	}

	private void buildEventMeta(List<Event> events) {
		int size = INIT_LATTICE_PAGE_COUNT * INIT_FAULT_PAGE_SIZE;
		for (Event event : events) {
			EventMeta eventMeta = getEventMeta(event.getTenantId(), event.getFaultId(), 0, size);
			event.setEventMeta(eventMeta);
		}
	}

	private EventMeta getEventMeta(String tenantId, String faultId, int from, int size) {
		Map<String, Object> map = getEventsMapByFaultId(tenantId, faultId, from, size);
		List<Event> events = (List<Event>) map.get("events");
		int total = (int) map.get("total");
		List<Integer> list = new ArrayList<>();
		for (Event event : events) {
			list.add(event.getServerity().intValue());
		}
		EventMeta meta = new EventMeta(total, list);
		return meta;
	}

	private void buildSearchEvent(List<Event> events, SearchHit schHit) {
		Map<String, SearchHits> innerHits = schHit.getInnerHits();
		int relateCount = (Integer) schHit.getSource().get("relate_count");
		String firstTimeStr = (String) schHit.getSource().get("first_time");
		Date firstTime = DateUtil.fmtUTC2Date(firstTimeStr);
		SearchHits schHits = innerHits.get(elasticSearchService.eventType);
		SearchHit[] schHitsHits = schHits.getHits();
		if (schHitsHits.length > 0) {
			SearchHit hit = schHitsHits[0];
			String id = (String) hit.getSource().get("id");
			String faultId = (String) hit.getSource().get("fault_id");
			String resId = (String) hit.getSource().get("res_id");
			String msgTitle = (String) hit.getSource().get("msg_title");
			String msgContent = (String) hit.getSource().get("msg_content");
			String occurTime = (String) hit.getSource().get("occur_time");
			String monitorId = (String) hit.getSource().get("monitor_id");
			String tenantId = (String) hit.getSource().get("tenant_id");
			short serverity = ((Integer) hit.getSource().get("serverity")).shortValue();
			Integer type = (Integer) hit.getSource().get("source_type");
			Short sourceType = null == type ? EventSourceType.OPEN_API.getKey() : type.shortValue();
			Date time = DateUtil.fmtUTC2Date(occurTime);
			Event event = new Event();
			event.setTenantId(tenantId);
			event.setRelateCount(relateCount);
			event.setFirstRelateTime(firstTime);
			event.setId(id);
			event.setFaultId(faultId);
			event.setResId(resId);
			event.setMsgTitle(msgTitle);
			event.setMsgContent(msgContent);
			event.setOccurTime(time);
			event.setMonitorId(monitorId);
			event.setServerity(serverity);
			event.setSourceType(sourceType);
			events.add(event);
		}
	}

	/**
	 * @param tenantId
	 * @param searchValue
	 * @param beginTime
	 * @param endTime
	 * @param granularity
	 * @return
	 */
	public EventGraphData searchEventGraphData(String tenantId, String searchValue, long beginTime, long endTime,
			int granularity) {
		Map<String, Object> map = EventSearchTimeCalculateUtil.getSearchTime(beginTime, endTime, granularity);
		Date begin = (Date) map.get("begin");
		Date end = (Date) map.get("end");
		long interval = (long) map.get("interval");
		long diff = (long) map.get("diffTime");
		Date schEnd = new Date(end.getTime());
		end = new Date(end.getTime() - interval);
		BoolQueryBuilder queryBuilder = getCommonQueryBuilder(tenantId, begin, schEnd, searchValue, null);
		SearchResponse searchResponse = elasticSearchService
				.getClient()
				.prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType)
				.setQuery(queryBuilder)
				.addAggregation(
						AggregationBuilders.dateHistogram("date_group").field("occur_time").interval(interval).minDocCount(0)
								.offset(getOffset(begin, interval))
								.extendedBounds(begin.getTime(), end.getTime()).subAggregation(
										AggregationBuilders.terms("serverity_count").field("serverity")
								))
				.setSize(0)
				.execute()
				.actionGet();
		List<EventGraphBuild> graphs = new ArrayList<>();
		Histogram agg = searchResponse.getAggregations().get("date_group");
		for (Histogram.Bucket entry : agg.getBuckets()) {
			String keyAsString = entry.getKeyAsString(); // Key as String
			long docCount = entry.getDocCount(); // 总的事件数
			Terms terms = entry.getAggregations().get("serverity_count");
			EventAlert alert = new EventAlert();
			for (Terms.Bucket bucket : terms.getBuckets()) {
				short serverity = ((Long) bucket.getKey()).shortValue();
				int count = (int) bucket.getDocCount();
				if (serverity == EventServerityType.SUCCESS.getKey()) {
					alert.setSuccess(count);
				} else if (serverity == EventServerityType.INFO.getKey()) {
					alert.setInfo(count);
				} else if (serverity == EventServerityType.WARNING.getKey()) {
					alert.setWarnning(count);
				} else {
					alert.setCritical(count);
				}
			}
			EventGraphBuild graphBuild = new EventGraphBuild();
			graphBuild.setTime(DateUtil.fmtUTC2Date(keyAsString));
			graphBuild.setTotal((int) docCount);
			graphBuild.setAlerts(alert);
			graphs.add(graphBuild);
		}
		EventGraphData data = new EventGraphData();
		data.setBeginTime(begin);
		data.setEndTime(end);
		data.setDiffTime(diff);
		data.setGraphs(graphs);
		queryEventGraphAtomic.incrementAndGet();
		return data;
	}

	private String getOffset(Date begin, long interval) {
		long off = (begin.getTime() % interval) / 3600000;
		StringBuilder sb = new StringBuilder();
		sb.append("+");
		sb.append(off);
		sb.append("h");
		return sb.toString();
	}

	private BoolQueryBuilder getCommonQueryBuilder(String tenantId, Date begin, Date end, String searchValue,
			String serverity) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("occur_time").from(begin).to(end))
				.must(QueryBuilders.termQuery("tenant_id", tenantId));
		if (null != searchValue && !"".equals(searchValue)) {
			for(String s:searchValue.split(";")){
				queryBuilder = queryBuilder.must(QueryBuilders.multiMatchQuery(s, "msg_title", "msg_content", "tags", "host", "ip").type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX));
			}
		}
		if (null != serverity && !"".equals(serverity)) {
			String[] arr = serverity.split(";");
			List<Integer> list = new ArrayList<>();
			for (String a : arr) {
				list.add(Integer.parseInt(a));
			}
			queryBuilder = queryBuilder.must(QueryBuilders.termsQuery("serverity", list));
		}
		return queryBuilder;
	}

	public PageEvent getEventsByFaultId(String tenantId, String eventId, String faultId, int current, int pageSize) {
		int from = (current == 0) ? 0 : (current - 1) * pageSize;
		Map<String, Object> map = getEventsMapByFaultId(tenantId, faultId, from, pageSize);
		List<Event> events = (List<Event>) map.get("events");
		int total = (int) map.get("total");
		EventMeta metas = getEventMeta(tenantId, faultId, 0, MAX_RELATE_COUNT);
		PageEvent pageEvent = new PageEvent();
		Map<String, Object> result = calculateCurrentAndEvents(events, eventId, current, INIT_FAULT_PAGE_SIZE);
		int retCurrent = (int) result.get("current");
		pageEvent.setCurrentPage(retCurrent);
		metas.setCurrentPage(retCurrent);
		pageEvent.setRows(events);
		pageEvent.setPageSize(pageSize);
		pageEvent.setTotal(total);
		pageEvent.setMetas(metas);
		return pageEvent;
	}

	/**
	 * 计算出事件在关联事件中的位置及要返回的关联事件
	 *
	 * @param events
	 * @param eventId
	 * @param current
	 * @return
	 */
	private Map<String, Object> calculateCurrentAndEvents(List<Event> events, String eventId, int current, int size) {
		Map<String, Object> map = new HashMap<>();
		if (current != 0) {
			map.put("events", events);
			map.put("current", current);
			return map;
		}
		int index = 0;
		for (Event event : events) {
			index++;
			if (event.getId().equals(eventId)) {
				break;
			}
		}
		current = index % size == 0 ? (index / size) : (index / size + 1);
		List<Event> resultEvents = new ArrayList<>();
		int begin = (current - 1) * size;
		int length = events.size();
		int end = (begin + size) > length ? length : (begin + size);
		for (int i = begin; i < end; i++) {
			resultEvents.add(events.get(i));
		}
		map.put("events", resultEvents);
		map.put("current", current);
		return map;
	}

	private Map<String, Object> getEventsMapByFaultId(String tenantId, String faultId, int from,
			int pageSize) {
		List<Event> events = new ArrayList<>();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("tenant_id", tenantId))
				.must(QueryBuilders.termQuery("fault_id", faultId));
		FieldSortBuilder sortBuilder = new FieldSortBuilder("occur_time");
		sortBuilder.order(SortOrder.DESC);
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType)
				.setQuery(queryBuilder)
				.addSort(sortBuilder)
				.setFrom(from)
				.setSize(pageSize)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		int total = (int) hits.getTotalHits();
		SearchHit[] searchHists = hits.getHits();
		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				String id = (String) hit.getSource().get("id");
				String resId = (String) hit.getSource().get("res_id");
				String msgTitle = (String) hit.getSource().get("msg_title");
				String msgContent = (String) hit.getSource().get("msg_content");
				String occurTime = (String) hit.getSource().get("occur_time");
				String monitorId = (String) hit.getSource().get("monitor_id");
				short serverity = ((Integer) hit.getSource().get("serverity")).shortValue();
				Date time = DateUtil.fmtUTC2Date(occurTime);
				Event event = new Event();
				event.setId(id);
				event.setFaultId(faultId);
				event.setResId(resId);
				event.setMsgTitle(msgTitle);
				event.setMsgContent(msgContent);
				event.setOccurTime(time);
				event.setMonitorId(monitorId);
				event.setServerity(serverity);
				events.add(event);
			}
		}
		Map<String, Object> map = new HashMap<>();
		map.put("total", total);
		map.put("events", events);
		return map;
	}

	@Override
	public List<EventMonitorData> queryMatchedMonitorData(String tenantId, Short[] sourceTypes, Short[] serveritys,
			String keyWords, String[] tags, Date beginTime, Date endTime) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("tenant_id", tenantId))
				.must(QueryBuilders.rangeQuery("occur_time").from(beginTime).to(endTime));
		if (null != keyWords && !"".equals(keyWords)) {
			keyWords = keyWords.replaceAll(";", " ");
			queryBuilder = queryBuilder.must(QueryBuilders.multiMatchQuery(keyWords, "msg_title", "msg_content", "tags", "host", "ip"));
		}
		if (null != serveritys && serveritys.length > 0) {
			queryBuilder.must(QueryBuilders.termsQuery("serverity", serveritys));
		}
		if (null != sourceTypes && sourceTypes.length > 0) {
			queryBuilder.must(QueryBuilders.termsQuery("source_type", serveritys));
		}

		List<String> searchValues=buildSearchTagValues(tags);
		if (null!=searchValues&&!searchValues.isEmpty()){
			for(String schValue:searchValues){
				queryBuilder.must(QueryBuilders.termQuery("tags",schValue));
			}
		}

		FieldSortBuilder sortBuilder = new FieldSortBuilder("occur_time");
		sortBuilder.order(SortOrder.DESC);
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType)
				.setQuery(queryBuilder)
				.addAggregation(AggregationBuilders.terms("fault_group").field("res_id"))
				.setSize(0)
				.execute()
				.actionGet();
		Terms terms = searchResponse.getAggregations().get("fault_group");
		List<EventMonitorData> eventMonitorDatas = new ArrayList<>();
		if (terms.getBuckets().size() > 0) {
			for (Terms.Bucket bucket : terms.getBuckets()) {
				String resId = (String) bucket.getKey();
				int count = (int) bucket.getDocCount();
				EventMonitorData data = new EventMonitorData();
				data.setCount(count);
				data.setResId(resId);
				eventMonitorDatas.add(data);
			}
		}
		return eventMonitorDatas;
	}

	//构建搜索tags的数据
	private List<String> buildSearchTagValues(String[] tags){
		if (null==tags||tags.length==0){
			return null;
		}
		List<String> searchValues=new ArrayList<>();
		for(String tag:tags){
			String[] temp=tag.split(":");
			if (temp.length==1){
				searchValues.addAll(buildSchTag(temp[0]));
			}else if (temp.length==2){
				searchValues.addAll(buildSchTag(temp[0]));
				searchValues.addAll(buildSchTag(temp[1]));
			}
		}
		return searchValues;
	}

	//中文标签必须要进行特殊处理
	private List<String> buildSchTag(String tag){
		char[] chars=tag.toCharArray();
		Set<String> set=new HashSet<>();
		StringBuilder sb=new StringBuilder();
		for(char c:chars){
			if (StringUtils.isChinese(c)){
				set.add(String.valueOf(c));
				if (sb.length()>0){
					set.add(sb.toString());
				}
				sb=new StringBuilder();
			}else{
				sb.append(String.valueOf(c));
			}
		}
		if (sb.length()>0){
			set.add(sb.toString());
		}
		List<String> list=new ArrayList<>();
		list.addAll(set);
		return list;
	}

	public int deleteByTime(Date beginTime, Date endTime) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("occur_time").from(beginTime).to(endTime));
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setQuery(queryBuilder)
				.setFrom(0)
				.setSize(1000)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		List<Event> events = new ArrayList<>();
		if (null != searchHists && searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				String id = (String) hit.getSource().get("id");
				String faultId = (String) hit.getSource().get("fault_id");
				Event event = new Event();
				event.setId(id);
				event.setFaultId(faultId);
				events.add(event);
			}
		}
		for (Event event : events) {
			elasticSearchService.getClient()
					.prepareDelete(elasticSearchService.getIndexName(), elasticSearchService.eventType, event.getId())
					.setParent(event.getFaultId())
					.execute()
					.actionGet();

			elasticSearchService.getClient()
					.prepareDelete(elasticSearchService.getIndexName(), elasticSearchService.faultType, event.getFaultId())
					.execute()
					.actionGet();
		}

		return events.size();
	}

	@Override
	public List<String> getTagsByEventId(String eventId) {
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType)
				.setQuery(QueryBuilders.termQuery("id", eventId))
				.setSize(1)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		String tags = null;
		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				tags = (String) hit.getSource().get("origin_tags");
			}
		}
		List<String> results = new ArrayList<>();
		if (null != tags) {
			String[] tempArr = tags.split(";");
			results = Arrays.asList(tempArr);
		}
		return results;
	}

	@Override
	public int getEventCount(String tenantId, Date begin, Date end) {
		if (begin.getTime() > end.getTime()) {
			throw new IllegalArgumentException("The start time should not be earlier than the end of time!");
		}
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.rangeQuery("occur_time").from(begin).to(end))
				.must(QueryBuilders.termQuery("tenant_id", tenantId));
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setQuery(queryBuilder)
				.setFrom(0)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		int total = (int) hits.getTotalHits();
		return total;
	}

	public Event getEventById(String id) {
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType)
				.setQuery(QueryBuilders.termQuery("id", id))
				.setSize(1)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		Event event = new Event();
		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				String eventId = (String) hit.getSource().get("id");
				String faultId = (String) hit.getSource().get("fault_id");
				event.setId(eventId);
				event.setFaultId(faultId);
			}
		}
		return event;
	}

	public EventFault getFaultById(String id) {
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.faultType)
				.setQuery(QueryBuilders.termQuery("fault_id", id))
				.setSize(1)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		EventFault fault = new EventFault();
		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				String faultId = (String) hit.getSource().get("fault_id");
				fault.setFaultId(faultId);
			}
		}
		return fault;
	}


	/**
	 * 关联事件默认一页10条
	 */
	private static final int INIT_FAULT_PAGE_SIZE = 10;

	/**
	 * 默认格子显示页数 5页
	 */
	private static final int INIT_LATTICE_PAGE_COUNT = 5;

	@Override
	public List<EventSpanTime> getEventSpanTime() {
		List<EventSpanTime> list = new ArrayList<EventSpanTime>();
		Map<String, EventSpanTime> eventSpanMap = new HashMap<String, EventSpanTime>();
		SearchResponse searchResponse = elasticSearchService
				.getClient()
				.prepareSearch(elasticSearchService.getIndexName())
				.setTypes(ElasticSearchService.eventType)
				.addAggregation(
						new TermsBuilder("t").field("tenant_id").subAggregation(
								new TopHitsBuilder("ltime").addSort("occur_time", SortOrder.DESC)
										.setFetchSource(new String[] { "tenant_id", "occur_time" }, new String[] {}).setFrom(0).setSize(1)))
				.execute().actionGet();
		Aggregations aggregations = searchResponse.getAggregations();
		Terms aggregation = aggregations.get("t");
		for (Bucket bucket : aggregation.getBuckets()) {
			Aggregations aggs = bucket.getAggregations();
			InternalTopHits topHits = aggs.get("ltime");
			SearchHit[] hits = topHits.getHits().getHits();
			for (SearchHit hit : hits) {
				Map<String, Object> map = hit.getSource();
				String tenantId = (String) map.get("tenant_id");
				String occurTime = (String) map.get("occur_time");
				EventSpanTime time = new EventSpanTime();
				time.setEndTime(DateUtil.fmtUTC2Date(occurTime).getTime());
				time.setTenantId(tenantId);
				eventSpanMap.put(tenantId, time);
			}
		}
		SearchResponse response = elasticSearchService
				.getClient()
				.prepareSearch(elasticSearchService.getIndexName())
				.setTypes(ElasticSearchService.faultType)
				.addAggregation(
						new TermsBuilder("t").field("tenant_id").subAggregation(
								new TopHitsBuilder("ftime").addSort("first_time", SortOrder.ASC)
										.setFetchSource(new String[] { "tenant_id", "first_time" }, new String[] {}).setFrom(0).setSize(1)))
				.execute().actionGet();
		Aggregations aggregations1 = response.getAggregations();
		Terms aggregation1 = aggregations1.get("t");
		for (Bucket bucket : aggregation1.getBuckets()) {
			Aggregations aggs = bucket.getAggregations();
			InternalTopHits topHits = aggs.get("ftime");
			SearchHit[] hits = topHits.getHits().getHits();
			for (SearchHit hit : hits) {
				Map<String, Object> map = hit.getSource();
				String tenantId = (String) map.get("tenant_id");
				String fTime = (String) map.get("first_time");
				long startTime = DateUtil.fmtUTC2Date(fTime).getTime();
				EventSpanTime time = eventSpanMap.get(tenantId);
				if (time == null) {
					time = new EventSpanTime();
					time.setStartTime(startTime);
					time.setTenantId(tenantId);
					time.setSpanTime(0);
					eventSpanMap.put(tenantId, time);
				} else {
					time.setStartTime(startTime);
					long endTime = time.getEndTime();
					long lTime = endTime - startTime;
					time.setSpanTime(lTime > 0 ? lTime : 0);
				}
			}
		}
		list.addAll(eventSpanMap.values());
		return list;
	}

	@Override
	public MinePageEvent searchEvent(String tenantId, int currentPage, int pageSize, String resId, Date begin, Date end) {
		int from = (currentPage - 1) * pageSize;
		List<Event> events = new ArrayList<>();
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("tenant_id", tenantId))
				.must(QueryBuilders.termQuery("res_id", resId))
				.must(QueryBuilders.rangeQuery("occur_time").from(begin).to(end));
		FieldSortBuilder sortBuilder = new FieldSortBuilder("occur_time");
		sortBuilder.order(SortOrder.DESC);
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType)
				.setQuery(queryBuilder)
				.addSort(sortBuilder)
				.setFrom(from)
				.setSize(pageSize)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		int total = (int) hits.getTotalHits();
		SearchHit[] searchHists = hits.getHits();
		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				String id = (String) hit.getSource().get("id");
				String msgTitle = (String) hit.getSource().get("msg_title");
				String msgContent = (String) hit.getSource().get("msg_content");
				String occurTime = (String) hit.getSource().get("occur_time");
				String monitorId = (String) hit.getSource().get("monitor_id");
				short serverity = ((Integer) hit.getSource().get("serverity")).shortValue();
				Date time = DateUtil.fmtUTC2Date(occurTime);
				Event event = new Event();
				event.setId(id);
				event.setResId(resId);
				event.setMsgTitle(msgTitle);
				event.setMsgContent(msgContent);
				event.setOccurTime(time);
				event.setMonitorId(monitorId);
				event.setServerity(serverity);
				events.add(event);
			}
		}
		MinePageEvent pageEvent=new MinePageEvent();
		pageEvent.setCount(total);
		pageEvent.setEvents(events);
		return pageEvent;
	}

	@Override
	public boolean delete(String tenantId, String resourceId) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("tenant_id", tenantId))
				.must(QueryBuilders.termQuery("res_id", resourceId));
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType)
				.setQuery(queryBuilder)
				.execute()
				.actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		Set<String> identities=new HashSet<>();
		Set<String> faultIds=new HashSet<>();
		Map<String,String> map=new HashMap<>();
		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				String identity = (String) hit.getSource().get("identity");
				String faultId = (String) hit.getSource().get("fault_id");
				String id = (String) hit.getSource().get("id");
				faultIds.add(faultId);
				identities.add(identity);
				map.put(id,faultId);
			}
		}
		Set<String> set=map.keySet();
		for(String id:set){
			String faultId=map.get(id);
			elasticSearchService.getClient()
					.prepareDelete(elasticSearchService.getIndexName(), elasticSearchService.eventType, id)
					.setParent(faultId)
					.execute()
					.actionGet();
		}
		for(String faultId:faultIds){
			elasticSearchService.getClient()
					.prepareDelete(elasticSearchService.getIndexName(), elasticSearchService.faultType, faultId)
					.execute()
					.actionGet();
		}

		List<String> list=new ArrayList<>();
		list.addAll(identities);
		eventRedisService.deleteByResAndIdentity(tenantId,resourceId,list);
		return true;
	}

	private XContentBuilder buildEventGenerateJson(SearchHit hit, String tenantId, String tags, Integer serverity, String newResId) {
		try {
			XContentBuilder builder = jsonBuilder()
					.startObject()
					.field("id", (String) hit.getSource().get("id"))
					.field("tenant_id", tenantId)
					.field("fault_id", (String) hit.getSource().get("fault_id"))
					.field("res_id", null == newResId ? (String) hit.getSource().get("res_id") : newResId)
					.field("msg_title", (String) hit.getSource().get("msg_title"))
					.field("msg_content", (String) hit.getSource().get("msg_content"))
					.field("source_type", hit.getSource().get("source_type"))
					.field("serverity", null == serverity ? hit.getSource().get("serverity"): serverity)
					.field("monitor_id", (String) hit.getSource().get("monitor_id"))
					.field("identity", (String) hit.getSource().get("identity"))
					.field("occur_time", (String) hit.getSource().get("occur_time"))
					.field("sort_id", hit.getSource().get("sort_id"))
					//查询需要的格式host xxx ip 10.1.10.111
					.field("tags", null == tags ? (String) hit.getSource().get("tags") : tags.replace(":", " ").replace(";", " "))
					//原本的tags格式host:xxx;ip:10.1.10.111
					.field("origin_tags", null == tags ? (String) hit.getSource().get("origin_tags") : tags)
					.field("host", (String) hit.getSource().get("host"))
					.field("ip", (String) hit.getSource().get("ip"))
					.endObject();
			return builder;
		} catch (IOException e) {
			ErrorUtil.warn(logger, "Event create build event Json data failed", e);
		}
		return null;
	}

	public boolean updateEventsByResTags(String tenantId, String resourceId, String resTags) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("res_id", resourceId));
		return updateEvents(queryBuilder, tenantId, resTags, null, null);

	}

	public PageUnrecoveredEvent getUnrecoveredEvents(String tenantId, int currentPage, int pageSize, String key,
			String searchValue, String sort) {

		int from = (currentPage - 1) * pageSize;
		HasParentQueryBuilder builder = QueryBuilders.hasParentQuery(
				elasticSearchService.faultType,
				QueryBuilders.boolQuery().must(QueryBuilders.termQuery("tenant_id", tenantId))
						.must(QueryBuilders.termQuery("recover", false)));

		BoolQueryBuilder postBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("serverity", Arrays.asList(2, 3)))
				.must(QueryBuilders.termQuery("source_type", 1));
		if (null != key) {
			postBuilder = key.equals(":") ? postBuilder.filter(QueryBuilders.scriptQuery(new Script("_source.origin_tags.length() == 0")))
					: postBuilder.must(QueryBuilders.matchPhraseQuery("tags", key.replace(":其他", " ").replace(":", " ")));
		}
		if (null != searchValue && !searchValue.isEmpty()) {
			// postBuilder = postBuilder.should(QueryBuilders.multiMatchQuery(searchValue, "msg_content", "host", "ip"));
			// searchValue = searchValue.replaceAll(";", " ");
			postBuilder = postBuilder.should(QueryBuilders.wildcardQuery("msg_content",  "*" + searchValue + "*"))
					.should(QueryBuilders.wildcardQuery("host", "*" + searchValue + "*"))
					.should(QueryBuilders.wildcardQuery("ip", "*" + searchValue + "*"));
		}
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setQuery(builder).setFrom(0).setSize(10000).setPostFilter(postBuilder).execute().actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		Map<RData, UnrecoveredEvent> tempEventMap = new HashMap<>();
		if (null != searchHists && searchHists.length > 0) {
			for (SearchHit schHit : searchHists) {
				String id = (String) schHit.getSource().get("id");
				String resId = (String) schHit.getSource().get("res_id");
				String resName = (String) schHit.getSource().get("host");
				String msgTitle = (String) schHit.getSource().get("msg_title");
				String msgContent = (String) schHit.getSource().get("msg_content");
				String occurTime = (String) schHit.getSource().get("occur_time");
				String monitorId = (String) schHit.getSource().get("monitor_id");
				String faultId = (String) schHit.getSource().get("fault_id");
				Date time = DateUtil.fmtUTC2Date(occurTime);
				boolean outInterval = System.currentTimeMillis() - time.getTime() > 7 * 24 * 60 * 60 * 1000;
				if(outInterval)
					continue;
				//判断是不是脏数据
				List<String> monitors = new ArrayList<>();
				try {
					monitors = ServiceManager.getInstance().getMonitorService().getIdListByTenantId(tenantId);
					if (!monitors.contains(monitorId))
						continue;
				} catch (NullPointerException e) {
					//不处理
				}finally{
					short serverity = ((Integer) schHit.getSource().get("serverity")).shortValue();
					String ip = (String) schHit.getSource().get("ip");
					if (null == resName || null == ip) {
						resName = ServiceManager.getInstance().getResourceService().queryResById(resId, tenantId).getHostname();
						ip = ServiceManager.getInstance().getResourceService().queryResById(resId, tenantId).getIpaddr();
					}
					String tags = (String) schHit.getSource().get("origin_tags");
					// TODO: 2-21 0021   jiangjw说对搜索结果进行内存过滤 以后性能不够再优化 可以新增mq队列
					RData rData = new RData(faultId, monitorId);
					UnrecoveredEvent ue = tempEventMap.get(rData);
					if (ue == null || time.getTime() > tempEventMap.get(new RData(faultId, monitorId)).getOccurTime().getTime()) {
						tempEventMap.put(rData, new UnrecoveredEvent(id, time, serverity, resId, resName,
								msgTitle, msgContent, tenantId, ip, tags, faultId, monitorId));
					}
				}
			}
		}
		List<UnrecoveredEvent> temp = new ArrayList<>(tempEventMap.values());
		//按时间排序降序
		Collections.sort(temp);
		List<UnrecoveredEvent> events = new ArrayList<>();
		PageUnrecoveredEvent pageEvent = new PageUnrecoveredEvent();
		pageEvent.setCurrentPage(currentPage);
		pageEvent.setPageSize(pageSize);
		events
				.addAll(temp.size() > from ? temp.subList(from, temp.size() < from + pageSize ? temp.size() : from + pageSize)
						: temp);
		pageEvent.setLists(events);
		pageEvent.setTotal(tempEventMap.size());
		queryEventPageAtomic.incrementAndGet();
		return pageEvent;
	}

	@Override
	public boolean updateEventTypeByMonitorId(String tenantId, String monitorId) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("monitor_id", monitorId));
		return updateEvents(queryBuilder, tenantId, null, 0, null);
	}

	public boolean updateEventsByOldResId(String tenantId, String oldResId, String newResId) {
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("res_id", oldResId));
		return updateEvents(queryBuilder, tenantId, null, null, newResId);

	}

	/**
	 * 更新事件通用方法
	 * */
	private boolean updateEvents(BoolQueryBuilder queryBuilder,String tenantId, String resTags, Integer serverity, String newResId) {
		queryBuilder.must(QueryBuilders.termQuery("tenant_id", tenantId));
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setTypes(elasticSearchService.eventType).setQuery(queryBuilder).setSize(10000).execute().actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();

		if (searchHists.length > 0) {
			for (SearchHit hit : searchHists) {
				String id = (String) hit.getSource().get("id");
				String faultId = (String) hit.getSource().get("fault_id");
				try {
					elasticSearchService.getBulkProcess().add(
							new IndexRequest(elasticSearchService.getIndexName(), elasticSearchService.eventType, id)
									.source(buildEventGenerateJson(hit, tenantId, resTags, serverity, newResId)).parent(faultId)
									.ttl(new TimeValue(ttl, TimeUnit.DAYS)));
					insertAtomic.incrementAndGet();
				} catch (Exception e) {
					insertFailedAtomic.incrementAndGet();
					throw new RuntimeException("Event update failed!");
				}
			}
		}
		return true;
	}

	public PageResEvent getAlertResEvents(String tenantId,String resourceId, int currentPage, int pageSize) {
		int from = (currentPage - 1) * pageSize;
		BoolQueryBuilder preBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("tenant_id", tenantId))
				.must(QueryBuilders.termQuery("recover", false));
		HasParentQueryBuilder builder = QueryBuilders.hasParentQuery(elasticSearchService.faultType,preBuilder);

		BoolQueryBuilder postBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("res_id", resourceId)).must(QueryBuilders.termsQuery("serverity", Arrays.asList(2, 3)))
				.must(QueryBuilders.termQuery("source_type", 1));
		SearchResponse searchResponse = elasticSearchService.getClient().prepareSearch(elasticSearchService.getIndexName())
				.setQuery(builder).setFrom(0).setSize(10000).setPostFilter(postBuilder).execute().actionGet();
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHists = hits.getHits();
		Map<String, ResEvent> tempEventMap = new HashMap<>();
		if (null != searchHists && searchHists.length > 0) {
			for (SearchHit schHit : searchHists) {
				String id = (String) schHit.getSource().get("id");
				String identity = (String) schHit.getSource().get("identity");
				short serverity = ((Integer) schHit.getSource().get("serverity")).shortValue();
				String faultId = (String) schHit.getSource().get("fault_id");
				String monitorId = (String) schHit.getSource().get("monitor_id");
				String occurTime = (String) schHit.getSource().get("occur_time");
				String msgContent = (String) schHit.getSource().get("msg_content");
				Date time = DateUtil.fmtUTC2Date(occurTime);
				// TODO: 2-21 0021   jiangjw说对搜索结果进行内存过滤 以后性能不够再优化 可以新增mq队列
				ResEvent ue = tempEventMap.get(monitorId);
				if (ue == null || time.getTime() > tempEventMap.get(monitorId).getOccurTime().getTime()) {
					tempEventMap.put(monitorId, new ResEvent(id, serverity, faultId, monitorId, identity, time, msgContent));
				}
			}
		}
		List<ResEvent> temp = new ArrayList<>(tempEventMap.values());
		//按时间排序降序
		Collections.sort(temp);
		List<ResEvent> events = new ArrayList<>();
		PageResEvent pageEvent = new PageResEvent();
		pageEvent.setCurrentPage(currentPage);
		pageEvent.setPageSize(pageSize);
		events.addAll(temp.size() > from ? temp.subList(from, temp.size() < from + pageSize ? temp.size() : from + pageSize)
				: temp);
		pageEvent.setLists(events);
		pageEvent.setTotal(tempEventMap.size());
		queryEventPageAtomic.incrementAndGet();
		return pageEvent;
	}

	private static class RData{
		private String faultId;
		private String monitorId;

		public RData(String faultId, String monitorId) {
			this.faultId = faultId;
			this.monitorId = monitorId;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			RData rData = (RData) o;

			if (faultId != null ? !faultId.equals(rData.faultId) : rData.faultId != null) return false;
			return monitorId != null ? monitorId.equals(rData.monitorId) : rData.monitorId == null;

		}

		@Override
		public int hashCode() {
			int result = faultId != null ? faultId.hashCode() : 0;
			result = 31 * result + (monitorId != null ? monitorId.hashCode() : 0);
			return result;
		}
	}
}
