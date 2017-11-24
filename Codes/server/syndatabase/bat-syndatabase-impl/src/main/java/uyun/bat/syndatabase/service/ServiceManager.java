package uyun.bat.syndatabase.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.service.PacificResourceService;
import uyun.bat.datastore.api.util.UUIDUtils;
import uyun.bat.event.api.service.EventService;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.entity.ResourceIdTransform;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.impl.ResourceServiceImpl;
import uyun.bat.syndatabase.util.IpUtil;

public class ServiceManager {

	private TagService tagService;
	private AgentService agentService;
	private MetricResourceService metricResourceService;
	private OverviewTagService overviewTagService;
	private OverviewTagResourceService overviewTagResourceService;
	private ResourceServiceImpl resourceService;
	private StateMetricResourceService stateMetricResourceService;
	private ResTemplateService resTemplateService;
	private ResourceIdTransformService transformService;

	private EventService eventService;
	private PacificResourceService pacificResourceLogic;

	private Queue<Resource> failQueue = new LinkedBlockingQueue<Resource>();
	private Queue<Resource> successQueue = new LinkedBlockingQueue<Resource>();
	private boolean should_exist = false;

	public void init(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(!should_exist){
					if(!failQueue.isEmpty()){
						Resource res = failQueue.poll();
						insertTable_ResourceIdTransform(res);
					}
				}
			}
		}).start();
	}

	/**
	 * 插入映射表
	 */
	public void updateResId(){
		List<Resource> res = resourceService.queryAllRes();
		if(res == null || res.size() < 1) return;
		for(Resource r:res){
			if(IpUtil.isIp(r.getIpaddr()))
				insertTable_ResourceIdTransform(r);
		}
	}

	/**
	 * 插入映射表
	 * @param resource
	 */
	public void insertTable_ResourceIdTransform(Resource resource){
		String newResId = pacificResourceLogic.save(resource);
		boolean flag = false;
		if(StringUtils.isBlank(newResId)){
			failQueue.add(resource);
		}else{
			ResourceIdTransform transform = new ResourceIdTransform(resource.getId(), resource.getTenantId());
			transform.setUnitId(newResId);
			transformService.insertResourceIdTransform(transform);
			flag = true;
		}
		if(flag)
			successQueue.add(resource);
	}

	/**
	 * 更新数据库(事务控制)
	 */
	@Transactional
	public void synDatabase(Resource res){
		RecordService[] recordServices = new RecordService[] {agentService, overviewTagService, tagService, resourceService};
		ColumnService[] columnServices = new ColumnService[] {metricResourceService, overviewTagResourceService, 
				resourceService, resTemplateService, stateMetricResourceService};
		ResourceIdTransform transform = transformService.getTransformIdByIds(res.getId(), res.getTenantId());
		updateTags(transform, recordServices);
		updateResId(transform, columnServices);
		updateEventResId(transform);
	}

	/**
	 * 更新agent-tag、overview-tag、tag表
	 */
	private void updateTags(ResourceIdTransform trans, RecordService[] recordServices){
		List<Tag> tags = new ArrayList<Tag>();
		List<Tag> exist = null;
		for(RecordService service:recordServices){
			exist = service.getAllTagByKey("resourceId");   // 无标签
			if(exist == null || exist.isEmpty()) continue;
			boolean changed = false;
			for(Tag tmp : exist){
				if(StringUtils.isNotBlank(tmp.getValue()) && tmp.getValue().toUpperCase().equals(trans.getResId().toUpperCase())){
					tmp.setValue(UUIDUtils.encodeMongodbId(trans.getUnitId()));
					tags.add(tmp);
					changed = true;
				}
			}
			if(changed)
				service.updateTag(tags);
		}
	}

	/**
	 * 更新metric_resource，overview_tag_resource，res_app，res_detail，res_tag，resource， resource_monitor_resource<p>
	 * state_metric_resource，tenant_res_template表
	 * @param trans
	 */
	private void updateResId(ResourceIdTransform trans, ColumnService[] columnServices){
		for(ColumnService service : columnServices){
			List<String> exist = service.getAllResId();
			if(exist == null || exist.isEmpty()) continue;
			boolean changed = false;
			List<ResIdTransform> list = new ArrayList<ResIdTransform>();
			for(String tmp : exist){
				if(StringUtils.isNotBlank(tmp) && tmp.toUpperCase().equals(trans.getResId().toUpperCase())){
					ResIdTransform map = new ResIdTransform();
					map.setOldResId(tmp);
					map.setNewResId(UUIDUtils.encodeMongodbId(trans.getUnitId()));
					list.add(map);
					changed = true;
				}
			}
			if(changed)
				service.updateResId(list);
		}
	}

	/**
	 * 更新es数据库ResId
	 * @param trans
	 */
	private void updateEventResId(ResourceIdTransform trans){
		eventService.updateEventsByOldResId(trans.getTenantId(), trans.getResId(), trans.getUnitId());
	}


	public ResTemplateService getResTemplateService() {
		return resTemplateService;
	}

	public void setResTemplateService(ResTemplateService resTemplateService) {
		this.resTemplateService = resTemplateService;
	}

	public StateMetricResourceService getStateMetricResourceService() {
		return stateMetricResourceService;
	}

	public void setStateMetricResourceService(StateMetricResourceService stateMetricResourceService) {
		this.stateMetricResourceService = stateMetricResourceService;
	}

	public ResourceServiceImpl getResourceService() {
		return resourceService;
	}

	public void setResourceService(ResourceServiceImpl resourceService) {
		this.resourceService = resourceService;
	}

	public OverviewTagResourceService getOverviewTagResourceService() {
		return overviewTagResourceService;
	}

	public void setOverviewTagResourceService(OverviewTagResourceService overviewTagResourceService) {
		this.overviewTagResourceService = overviewTagResourceService;
	}

	public OverviewTagService getOverviewTagService() {
		return overviewTagService;
	}

	public void setOverviewTagService(OverviewTagService overviewTagService) {
		this.overviewTagService = overviewTagService;
	}

	public MetricResourceService getMetricResourceService() {
		return metricResourceService;
	}

	public void setMetricResourceService(MetricResourceService metricResourceService) {
		this.metricResourceService = metricResourceService;
	}

	public AgentService getAgentService() {
		return agentService;
	}

	public void setAgentService(AgentService agentService) {
		this.agentService = agentService;
	}

	public ResourceIdTransformService getTransformService() {
		return transformService;
	}

	public void setTransformService(ResourceIdTransformService transformService) {
		this.transformService = transformService;
	}

	public TagService getTagService() {
		return tagService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	public PacificResourceService getPacificResourceLogic() {
		return pacificResourceLogic;
	}

	public void setPacificResourceLogic(PacificResourceService pacificResourceLogic) {
		this.pacificResourceLogic = pacificResourceLogic;
	}

	public Queue<Resource> getFailQueue() {
		return failQueue;
	}

	public void setFailQueue(Queue<Resource> failQueue) {
		this.failQueue = failQueue;
	}

	public Queue<Resource> getSuccessQueue() {
		return successQueue;
	}

	public void setSuccessQueue(Queue<Resource> successQueue) {
		this.successQueue = successQueue;
	}

	public boolean isShould_exist() {
		return should_exist;
	}

	public void setShould_exist(boolean should_exist) {
		this.should_exist = should_exist;
	}

}
