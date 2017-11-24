package uyun.bat.datastore.service;

import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.datastore.api.service.TagService;
import uyun.bat.event.api.service.EventService;

public abstract class ServiceManager {
	private static ServiceManager instance = new ServiceManager() {
	};

	public static ServiceManager getInstance() {
		return instance;
	}

	private ResourceService resourceService;

	private StateService stateService;

	private MetricService metricService;

	private TagService tagService;
	
	private EventService eventService;

	public EventService getEventService() {
		return eventService;
	}

	public void setEventService(EventService eventService) {
		this.eventService = eventService;
	}

	public ResourceService getResourceService() {
		return resourceService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public StateService getStateService() {
		return stateService;
	}

	public void setStateService(StateService stateService) {
		this.stateService = stateService;
	}

	public MetricService getMetricService() {
		return metricService;
	}

	public void setMetricService(MetricService metricService) {
		this.metricService = metricService;
	}

	public TagService getTagService() {
		return tagService;
	}

	public void setTagService(TagService tagService) {
		this.tagService = tagService;
	}

}
