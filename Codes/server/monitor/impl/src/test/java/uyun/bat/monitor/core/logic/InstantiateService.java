package uyun.bat.monitor.core.logic;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.service.MetricMetaDataService;
import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.datastore.api.serviceapi.entity.ResourceServiceQuery;
import uyun.bat.datastore.api.serviceapi.entity.ServiceApiResMetrics;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventGraphData;
import uyun.bat.event.api.entity.EventMonitorData;
import uyun.bat.event.api.entity.MinePageEvent;
import uyun.bat.event.api.entity.PageEvent;
import uyun.bat.event.api.entity.PageResEvent;
import uyun.bat.event.api.entity.PageUnrecoveredEvent;
import uyun.bat.event.api.service.EventService;

import java.util.*;

/**
 * 各种服务实例化，方便调用
 * @author admit
 * @StateService
 * @EventService
 * @MetricMetaDataService
 * @ResourceService
 */
public class InstantiateService {
	
	public static StateService stateService = new StateService() {
			
			@Override
			public String saveState(String tenantId, State state) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void saveCheckpoint(Checkpoint cp) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public List<String> getTagsByState(String tenantId, String state) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public State[] getStates(String tenantId) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Checkperiod[] getLastCheckperiods(String state, String[] tags, long firstTime, long lastTime) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getCheckpointsCount(String state, String[] tags, String value) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public Checkpoint[] getCheckpoints(String state, String[] tags) {
				
				return null;
			}
			
			@Override
			public Checkpoint getCheckpoint(String state, String[] tags) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public Checkperiod[] getCheckperiods(String tenantId, String state, String objectId, long firstTime,
					long lastTime) {
				// TODO Auto-generated method stub
				return null;
			}

		@Override
		public Checkperiod getLastCheckperiod(String tenantId, String state, String objectId) {
			return null;
		}

		@Override
			public Checkperiod[] getCheckperiods(String state, String[] tags, long firstTime, long lastTime) {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void deleteState(String tenantId, String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void deleteCheckpoints(String state, String tenantId) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void deleteCheckpoints(String state, String[] tags) {
				// TODO Auto-generated method stub
				
			}
		};
	
	
	
	public static EventService eventService = new EventService() {
		
		@Override
		public List<EventMonitorData> queryMatchedMonitorData(String tenantId, Short[] sourceTypes, Short[] serveritys,
													   String keyWords, String[] tags, Date beginTime, Date endTime) {
			List<EventMonitorData> list = new ArrayList<EventMonitorData>();
			EventMonitorData eventMonitorData = new EventMonitorData();
			eventMonitorData.setCount(1);
			eventMonitorData.setResId(ConstantDef.TEST_RESID);
			list.add(eventMonitorData);
			return list;
		}
		
		@Override
		public List<String> getTagsByEventId(String eventId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PageEvent getEventsByFaultId(String tenantId, String id, String faultId, int current, int pageSize) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public int getEventCount(String tenantId, Date begin, Date end) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public long create(List<Event> events) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public Event create(Event event) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PageEvent searchEvent(String tenantId, int page, int rp, String searchValue, String serverity, long beginTime, long endTime, Integer granularity) {
			return null;
		}

		@Override
		public EventGraphData searchEventGraphData(String tenantId, String searchValue, long beginTime, long endTime, int granularity) {
			return null;
		}

		@Override
		public MinePageEvent searchEvent(String tenantId, int page, int rp, String resId, Date begin, Date end) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PageUnrecoveredEvent getUnrecoveredEvents(String tenantId, int currentPage, int pageSize, String key, String searchValue, String sort) {
			return null;
		}

		@Override
		public boolean updateEventsByOldResId(String tenantId, String oldResId, String newResId) {
			return false;
		}

		@Override
		public PageResEvent getAlertResEvents(String tenantId, String resourceId, int currentPage, int pageSize) {
			return null;
		}
	};
	
	public static MetricMetaDataService metricMetaDataService = new MetricMetaDataService() {
		
		@Override
		public boolean update(MetricMetaData data) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public MetricMetaData queryByName(String metricName) {
			MetricMetaData metaData = new MetricMetaData();
			metaData.setUnit(metricName);
			return metaData;
		}
		
		@Override
		public List<MetricMetaData> queryAll(String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean insert(MetricMetaData data) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public List<MetricMetaData> getMetricMetaDataByKey(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<String> getAllMetricMetaDataName() {
			return null;
		}

		@Override
		public boolean delete(String metricName) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<MetricMetaData> getMetricsUnitByList(List<String> metricNames) {
			return null;
		}

		@Override
		public List<MetricMetaData> queryRangedMetaData(String tenantId) {
			return null;
		}
	};

	public static ResourceService resourceService = new ResourceService() {
		@Override
		public List<SimpleResource> query(OnlineStatus onlineStatus, long lastCollectTime) {
			return null;
		}

		@Override
		public boolean saveResourceDetail(ResourceDetail resourceDetail) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public List<Tag> queryResTags(String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<String> queryResTagNames(String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PageResource queryResListByCondition(ResourceOpenApiQuery query) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Resource queryResById(String id, String tenantId) {
			Resource res = new Resource();
			res.setTenantId(tenantId);
			res.setId(id);
			res.setOnlineStatus(OnlineStatus.ONLINE);
			return res;
		}
		
		@Override
		public Resource queryResByAgentId(String agentId, String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<SimpleResource> queryByTenantIdAndTags(String tenantId, List<Tag> tags) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public ResourceDetail queryByResId(String resId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PageResource queryByKeyAndSortBy(String tenantId, String filter, ResourceOrderBy orderBy, int pageNo,
				int size, OnlineStatus onlineStatus) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PageResource queryByKey(String tenantId, String key, int pageNo, int size, OnlineStatus onlineStatus) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PageResourceGroup queryByFilterAndGroupByTag(String tenantId, String filter, String groupBy, int pageNo,
				int size, OnlineStatus onlineStatus) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<String> queryAllResTags(String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PageResource queryAllRes(String tenantId, int pageNo, int pageSize, OnlineStatus onlineStatus) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<Resource> queryAllRes(String tenantId, boolean isContainNetwork) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<String> getResTagsByTag(String tenantId, String tags) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<ResourceStatusCount> getResStatusCount(String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<ResourceCount> getResCountByDate(Date startTime, Date endTime) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<ResourceCount> getResCount() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean deleteResourceDetail(String resId) {
			// TODO Auto-generated method stub
			return false;
		}


		@Override
		public boolean insertAsync(Resource resource) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean updateAsync(Resource resource) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public long insertAsync(List<Resource> resources, String tenantId) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean saveResourceSync(Resource resource) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean saveResourceSyncOnly(Resource resource) {
			return false;
		}

		@Override
		public List<ResourceCount> getResCountByOnlineStatus(OnlineStatus status) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean delete( String tenantId, String resourceId) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public List<Map<String, String>> queryResTagsByMetrics(String tenantId, List<String> metrics) {
			return null;
		}

		@Override
		public List<Resource> queryTenantResByTags(String tenantId, List<String> resTags, String sortField, String sortOrder) {
			return null;
		}

		@Override
		public List<Resource> queryResourcesByIpaddrs(String tenantId, List<String> ipaddrs) {
			return null;
		}

		@Override
		public void updateUserTagsBatch(List<Resource> list) {

		}

		@Override
		public String resIdTransform(String tenantId, String resId) {
			return null;
		}
	};
	
	public static MetricService metricService = new MetricService() {

		@Override
		public List<PerfMetric> queryTopN(QueryBuilder builder, int n) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public Map<String, PerfMetric> querySeriesGroupBy(QueryBuilder queryBuilder, int interval) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<PerfMetric> querySeries(QueryBuilder queryBuilder, int interval) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<PerfMetric> queryPerfForMonitor(QueryBuilder queryBuilder) {
			List<PerfMetric> perfMetrics = new ArrayList<PerfMetric>();
			PerfMetric pm = new PerfMetric();
			pm.setName(ConstantDef.HOST_NAME);
			perfMetrics.add(pm);
			return perfMetrics;
		}
		
		@Override
		public List<PerfMetric> queryPerfForCircle(QueryBuilder queryBuilder) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PerfMetric queryPerf(QueryBuilder builder) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public PerfMetric queryLastPerf(QueryBuilder queryBuilder) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<Tag> getTagsByTag(String tenantId, String metricName, List<Tag> tags) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<Tag> getTags(String tenantId, String metricName) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<String> getMetricNamesByTenantId(String tenantId) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public List<String> getMetricNamesByResId(String resourceId) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<String> getGroupTagName(String tenantId, String metricName) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public boolean deleteTrashData(String metricName, String tenantId, Map<String, String> tags) {
			// TODO Auto-generated method stub
			return false;
		}
		
		@Override
		public boolean delete(QueryBuilder queryBuilder) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public PerfMetric queryCurrentPerfMetric(QueryBuilder builder) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<ResourceMetrics> queryPerfForEachResource(List<Resource> resources, List<String> metricsArr, String tenantId, String sortField, String sortOrder, String type, Long start, Long end) {
			return null;
		}

		@Override
		public List<ServiceApiResMetrics> getMetricNames(ResourceServiceQuery query) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
}
