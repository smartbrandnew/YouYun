package uyun.bat.monitor.core.logic;

import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.event.api.entity.*;
import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.*;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.core.util.TagUtil;
import uyun.bat.monitor.impl.common.ServiceManager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventMonitor implements Checker {
	private Monitor monitor;
	private EventMonitorParam eventMonitorParam;

	private List<EventDataPoint> eventDataPoints;
	private EventData eventData;

	/**
	 * 查询表达式
	 */
	private String query;

	public EventMonitor(Monitor monitor, EventMonitorParam eventMonitorParam,EventData eventData) {
		super();
		this.monitor = monitor;
		this.eventMonitorParam = eventMonitorParam;
		this.eventData=eventData;
	}
	
	/**
	 * 组装监测器的查询逻辑表达式
	 */
	private String getQuery() {
		if (query == null) {
			query = monitor.getQuery();
			if (monitor.getOptions() != null && monitor.getOptions().getEventRecover() != null
					&& monitor.getOptions().getEventRecover().length > 0) {
				StringBuilder sb = new StringBuilder(query);
				sb.append(" [");
				for (String s : monitor.getOptions().getEventRecover()) {
					sb.append(s);
					sb.append(",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("]");
				query = sb.toString();
			}

		}
		return query;
	}

	public EventMonitor() {
	}

	public EventMonitorParam getEventMonitorParam() {
		return eventMonitorParam;
	}

	public boolean match(EventData data) {
		String recover = MonitorQueryUtil.getRecoversByKeywords(monitor.getOptions().getEventRecover());
		if (null != recover && eventMonitorParam.match(recover, data)) {
			data.setRecover(true);
			return true;
		}
		return eventMonitorParam.match(eventMonitorParam.getKeyWords(), data);
	}

	public MonitorState checkIfMonitorStatusRollover() {
		if (eventData.isRecover())
			return checkIfEventRecover();
		else {
			return checkIfEventAlert();
		}
	}

	public MonitorState checkIfEventAlert() {
		Short[] sourceTypes = EventSourceType.getSourceType(eventMonitorParam.getSources());
		Short[] serveritys = EventServerityType.getServerityType(eventMonitorParam.getStatus());
		PeriodUtil.Period period = PeriodUtil.generatePeriod(eventMonitorParam.getPeriod());
		Date beginTime = new Date(period.getStart());
		Date endTime = new Date(period.getEnd());

		List<EventMonitorData> eventMonitorDatas = ServiceManager
				.getInstance()
				.getEventService()
				.queryMatchedMonitorData(monitor.getTenantId(), sourceTypes, serveritys, eventMonitorParam.getKeyWords(),
						eventMonitorParam.getArrTags(), beginTime, endTime);
		MonitorState monitorStatus = MonitorState.OK;
		eventDataPoints = new ArrayList<>();
		for (EventMonitorData data : eventMonitorDatas) {
			if (eventMonitorParam.getComparison().match((double) data.getCount(), eventMonitorParam.getThreshold())) {
				monitorStatus = MonitorState.ERROR;
				eventDataPoints.add(new EventDataPoint(MonitorState.ERROR, data.getResId(), data.getCount()));
			} else {
				eventDataPoints.add(new EventDataPoint(MonitorState.OK, data.getResId(), data.getCount()));
			}
		}

		return monitorStatus;
	}

	public Monitor getMonitor() {
		return monitor;
	}

	public Event generateEvent(MonitorState status,Resource resource) {
		Event event = new Event();
		EventServerityType eventServerityType = MonitorQueryUtil.getEventServerityType(status);
		if (eventServerityType == null)
			return null;
		else
			event.setServerity(eventServerityType.getKey());

		event.setMsgTitle(monitor.getName());
		event.setResId(resource.getId());
		event.setOccurTime(new Timestamp(System.currentTimeMillis()));
		if(eventData != null)
			event.setMsgContent(eventData.getContent());
		
		event.setSourceType(EventSourceType.MONITOR.getKey());
		event.setMonitorId(monitor.getId());
		event.setMonitorType(monitor.getMonitorType().getCode());
		// 监测器tag
		List<EventTag> monitorTags = new ArrayList<>();
		if (eventMonitorParam.getTags() != null && eventMonitorParam.getTags().size() > 0) {
			for (TagEntry te : eventMonitorParam.getTags()) {
				EventTag et = new EventTag();
				et.setTenantId(monitor.getTenantId());
				et.setTagk(te.getKey());
				et.setTagv(te.getValue() != null ? te.getValue() : "");
				monitorTags.add(et);
			}
		}
		//资源tag
		List<EventTag> tags=generateEventTags(resource);
		tags.removeAll(monitorTags);
		tags.addAll(monitorTags);
		event.setEventTags(tags);
		event.setTenantId(monitor.getTenantId());
		return event;
	}

	private List<EventTag> generateEventTags(Resource resource) {
		List<EventTag> tags=new ArrayList<>();
		tags.add(new EventTag(monitor.getTenantId(),"host",resource.getHostname()));
		tags.add(new EventTag(monitor.getTenantId(),"ip",resource.getIpaddr()));
		List<String> resTags=resource.getResTagsAll();
		if (resTags!=null&&resTags.size()>0){
			for(String str:resTags){
				int index = str.indexOf(":");
				EventTag et;
				if (index==-1){
					et=new EventTag(monitor.getTenantId(),str,"");
				}else{
					et=new EventTag(monitor.getTenantId(),str.substring(0, index), str.substring(index + 1));
				}
				tags.add(et);
			}
		}
		return tags;
	}

	/**
	 * 创建事件标识
	 * 
	 * @return
	 */
	public Symbol generateSymbol(String resId, MonitorState monitorStatus) {
		Symbol symbol = new Symbol();
		symbol.setTenantId(monitor.getTenantId());
		symbol.setMonitorId(monitor.getId());
		symbol.setQuery(getQuery());
		symbol.setMonitorType(monitor.getMonitorType());
		symbol.setMonitorState(monitorStatus);
		List<TagEntry> tags = new ArrayList<TagEntry>();
		if (null != resId && resId.length() > 0) {
			symbol.setResourceId(resId);
			tags.add(new TagEntry(StateUtil.RESOURCE_ID, resId));
		}
		TagUtil.generateTags(tags);
		symbol.setTags(tags);
		return symbol;
	}

	public void doAfterCheck() {
		// 事件监测器获取不到关联的资源host和ip需要进行一次查询
		if (eventData.isRecover()) {
			Resource res = ServiceManager.getInstance().getResourceService().queryResById(eventData.getResId(),eventData.getTenantId());
			Event event = generateEvent(MonitorState.OK,res);
			if (null == event) {
				return;
			}
			dotrigger(event, MonitorState.OK, res.getHostname(), res.getIpaddr(), 0);
		} else {
			if (eventDataPoints == null || eventDataPoints.size() == 0)
				return;
			for (EventMonitor.EventDataPoint data : eventDataPoints) {
				Resource res = ServiceManager.getInstance().getResourceService().queryResById(data.getResId(), getMonitor().getTenantId());
				Event event = generateEvent(data.getMonitorStatus(),res);
				if (event == null)
					continue;
				dotrigger(event, data.getMonitorStatus(), res.getHostname(), res.getIpaddr(), data.count);
			}
		}

	}

	private void dotrigger(Event event, MonitorState monitorState, String hostName, String ip, int count) {
		CheckContext context = new CheckContext(event, null, hostName, ip, null, monitorState, eventMonitorParam,MonitorType.EVENT);
		context.setCount(count);
		context.setEventRecover(monitor.getOptions().getEventRecover());
		boolean trigger = CheckController.getInstance().trigger(monitor, context,
				generateSymbol(event.getResId(), monitorState));
		if (trigger && monitor.getNotify() && monitor.getNotifyUserIdList() != null
				&& monitor.getNotifyUserIdList().size() > 0){
			CheckController.getInstance().notify(context, monitor.getNotifyUserIdList());
		}
	}

	// 处理收到某关键词事件恢复
	private MonitorState checkIfEventRecover() {
		Symbol symbol = generateSymbol(null, MonitorState.ERROR);
		String[] tags = symbol.generateTags();
		String state = symbol.generateState();
		StateService stateService = ServiceManager.getInstance().getStateService();
		Checkpoint[] checkpoints = stateService.getCheckpoints(state, tags);
		MonitorState monitorStatus = MonitorState.OK;
		if (null == checkpoints||checkpoints.length<1) {
			return monitorStatus;
		}
		for (Checkpoint checkpoint : checkpoints) {
			if (!eventData.getResId().equals(getResIdByTags(checkpoint.getTags()))
					&& checkpoint.getValue().equals(MonitorState.ERROR.getCode())) {
				monitorStatus = MonitorState.ERROR;
			}
		}
		return monitorStatus;
	}

	public String getResIdByTags(String[] tags) {
		if (null == tags || tags.length < 1) {
			return "";
		}
		String key = "";
		for (String tag : tags) {
			if (tag.indexOf(StateUtil.RESOURCE_ID) >= 0) {
				key = tag.split(":")[1];
				break;
			}
		}
		return key;
	}

	public void setEventMonitorParam(EventMonitorParam eventMonitorParam) {
		this.eventMonitorParam = eventMonitorParam;
	}

	private static class EventDataPoint {
		private MonitorState monitorStatus;
		private String resId;
		private int count;

		public EventDataPoint() {
		}

		public EventDataPoint(MonitorState monitorStatus, String resId, int count) {
			this.monitorStatus = monitorStatus;
			this.resId = resId;
			this.count = count;
		}

		public MonitorState getMonitorStatus() {
			return monitorStatus;
		}

		public void setMonitorStatus(MonitorState monitorStatus) {
			this.monitorStatus = monitorStatus;
		}

		public String getResId() {
			return resId;
		}

		public void setResId(String resId) {
			this.resId = resId;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}
	}
}