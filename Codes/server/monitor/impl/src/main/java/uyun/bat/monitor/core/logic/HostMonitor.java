package uyun.bat.monitor.core.logic;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.SimpleResource;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.api.entity.EventTag;
import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.CheckContext;
import uyun.bat.monitor.core.entity.HostMonitorParam;
import uyun.bat.monitor.core.entity.ResourceData;
import uyun.bat.monitor.core.entity.Symbol;
import uyun.bat.monitor.core.entity.TagEntry;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.core.util.TagUtil;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bat.monitor.impl.util.JsonUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HostMonitor implements Checker {

    private Monitor monitor;
    private HostMonitorParam hostMonitorParam;

    private List<HostDataPoint> hostDataPoints;

    private ResourceData resourceData;

    private String query;

    public HostMonitor(Monitor monitor, HostMonitorParam hostMonitorParam) {
        this.monitor = monitor;
        this.hostMonitorParam = hostMonitorParam;
    }

    public HostMonitor(Monitor monitor, HostMonitorParam hostMonitorParam, ResourceData resourceData) {
        this.monitor = monitor;
        this.hostMonitorParam = hostMonitorParam;
        this.resourceData=resourceData;
    }

    public void setResourceData(ResourceData resourceData) {
        this.resourceData = resourceData;
    }

    /**
     * @return
     */
    @Override
    public MonitorState checkIfMonitorStatusRollover() {
        PeriodUtil.Period period= PeriodUtil.generatePeriod(hostMonitorParam.getPeriod());
        List<SimpleResource> simpleResources= ServiceManager.getInstance().getResourceService().queryByTenantIdAndTags(monitor.getTenantId(),buildTags(hostMonitorParam.getTags()));
        if (null==hostDataPoints){
            hostDataPoints=new ArrayList<>();
        }
        MonitorState state= MonitorState.OK;
        if (resourceData.getOnlineStatus().equals(OnlineStatus.OFFLINE)){
            Resource res=ServiceManager.getInstance().getResourceService().queryResById(resourceData.getResourceId(), getMonitor().getTenantId());
            if (res.getLastCollectTime().getTime()<period.getStart()){
                state=MonitorState.checkByCode(hostMonitorParam.getStatus());
            }
        }
        hostDataPoints.add(new HostDataPoint(state,resourceData.getResourceId(),resourceData.getHostname(),resourceData.getIpaddr()));
        //如果监测器没有其他匹配的资源则接收到资源的状态则反映监测器状态
        if(null==simpleResources||simpleResources.isEmpty()){
            return state;
        }
        for(SimpleResource resource:simpleResources){
            //新的监测数据不判断监测器状态
            if (resourceData.getResourceId().equals(resource.getResourceId())){
                continue;
            }
            //后续考虑优化
            Resource res=ServiceManager.getInstance().getResourceService().queryResById(resource.getResourceId(), getMonitor().getTenantId());
            if (res.getLastCollectTime().getTime()<period.getStart()){
                state= MonitorState.checkByCode(hostMonitorParam.getStatus());
                hostDataPoints.add(new HostDataPoint(state,resource.getResourceId(),resource.getResourceName(),resource.getIpaddr()));
            }else{
                hostDataPoints.add(new HostDataPoint(MonitorState.OK,resource.getResourceId(),resource.getResourceName(),resource.getIpaddr()));
            }
        }
        return state;
    }

    //构建datastore需要的tag
    private List<Tag> buildTags(List<TagEntry> tagEntrys) {
        if (null == tagEntrys || tagEntrys.size() < 1) {
            return new ArrayList<>();
        }
        List<Tag> tags = new ArrayList<>();
        for (TagEntry tagEntry : tagEntrys) {
            Tag tag = new Tag(tagEntry.getKey(), tagEntry.getValue());
            tags.add(tag);
        }
        return tags;
    }

    @Override
    public Monitor getMonitor() {
        return monitor;
    }

    public HostMonitorParam getHostMonitorParam(){
        return hostMonitorParam;
    }

    @Override
    public void doAfterCheck() {
        if (null==hostDataPoints||hostDataPoints.isEmpty()){
            return;
        }
        for (HostDataPoint data : hostDataPoints) {
            Resource resource=ServiceManager.getInstance().getResourceService().queryResById(data.getResId(), getMonitor().getTenantId());
            Event event = generateEvent(data,resource);
            if (event == null)
                continue;
            dotrigger(event, data.getMonitorStatus(), data.getHostName(),data.getIp(),data.getResId());
        }
    }

    private Event generateEvent(HostDataPoint dataPoint,Resource resource) {
        Event event = new Event();
        EventServerityType eventServerityType = MonitorQueryUtil.getEventServerityType(dataPoint.getMonitorStatus());
        if (eventServerityType == null)
            return null;
        else
            event.setServerity(eventServerityType.getKey());

        event.setMsgTitle(monitor.getName());
        event.setResId(dataPoint.getResId());
        event.setOccurTime(new Timestamp(System.currentTimeMillis()));

        event.setSourceType(EventSourceType.MONITOR.getKey());
        event.setMonitorId(monitor.getId());
        event.setMonitorType(monitor.getMonitorType().getCode());
        // 监测器tag
        List<EventTag> monitorTags = new ArrayList<>();
        if (hostMonitorParam.getTags() != null && hostMonitorParam.getTags().size() > 0) {
            for (TagEntry te : hostMonitorParam.getTags()) {
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

    private void dotrigger(Event event, MonitorState monitorState, String hostName, String ip, String resourceId) {
        CheckContext context = new CheckContext(event, null, hostName, ip, null, monitorState, hostMonitorParam,MonitorType.HOST);
        boolean trigger = CheckController.getInstance().trigger(monitor, context,
                generateSymbol(event.getResId(), monitorState));
        if(!trigger){
            return;
        }
        checkResState(resourceId,monitorState);
        if (monitor.getNotify() && monitor.getNotifyUserIdList() != null
                && monitor.getNotifyUserIdList().size() > 0){
            CheckController.getInstance().notify(context, monitor.getNotifyUserIdList());
        }

    }

    //更新资源状态
    private void checkResState(String resourceId,MonitorState monitorState){
        Resource resource = ServiceManager.getInstance().getResourceService()
                .queryResById(resourceId, getMonitor().getTenantId());
        if (null == resource) {
            return;
        }
        if (monitorState.getValue() == MonitorState.OK.getValue()) {
            resource.setOnlineStatus(OnlineStatus.ONLINE);
        } else {
            resource.setOnlineStatus(OnlineStatus.OFFLINE);
        }
        // 同步更新redis以及mysql资源状态
        ServiceManager.getInstance().getResourceService().saveResourceSyncOnly(resource);
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

    private String getQuery() {
        if (query == null) {
            if (monitor.getOptions() != null && monitor.getOptions().getThresholds() != null
                    && monitor.getOptions().getThresholds().size() > 0) {
                StringBuilder sb = new StringBuilder(monitor.getQuery());
                try {
                    String thresholds = JsonUtil.encode(monitor.getOptions().getThresholds());
                    sb.append(" ");
                    sb.append(thresholds);
                } catch (Exception e) {
                }
                query = sb.toString();
            } else {
                query = monitor.getQuery();
            }
        }
        return query;
    }

    /**
     * 主机监测器触发条件[host:hostname, 或者其他主机标签];
     * 主机监测器，多标签好像是或关系
     * @param hostname
     * @param tags
     * @return
     */
	public boolean match(String hostname, List<String> tags) {
		List<TagEntry> resourceTags = new ArrayList<TagEntry>();
		if (hostname != null && !hostname.isEmpty())
			resourceTags.add(new TagEntry("host", hostname));
		if (tags != null && !tags.isEmpty()) {
			for (String tag : tags) {
				if (tag == null || tag.trim().length() == 0)
					continue;
				int index = tag.indexOf(":");
				if (index != -1) {
					resourceTags.add(new TagEntry(tag.substring(0, index), tag.substring(index + 1)));
				} else {
					resourceTags.add(new TagEntry(tag, null));
				}
			}
		}

		for (TagEntry resourceTag : resourceTags) {
		    if (hostMonitorParam.getTags() != null && hostMonitorParam.getTags().size() > 0) {
                for (TagEntry triggerTag : hostMonitorParam.getTags()) {
                    if (triggerTag.equals(resourceTag)) {
                        return true;
                    }
                }
            } else {
		        // 该主机监测器没有资源标签
                // 代表该监测器匹配所有资源
		        return true;
            }
		}

		return false;
	}

    public void setHostMonitorParam(HostMonitorParam hostMonitorParam) {
        this.hostMonitorParam = hostMonitorParam;
    }

    private static class HostDataPoint{
        private MonitorState monitorStatus;
        private String resId;
        private String hostName;
        private String ip;

        public HostDataPoint(MonitorState monitorStatus, String resId, String hostName, String ip) {
            this.monitorStatus = monitorStatus;
            this.resId = resId;
            this.hostName=hostName;
            this.ip=ip;
        }

        public HostDataPoint(MonitorState monitorStatus, String resId) {
            this.monitorStatus = monitorStatus;
            this.resId = resId;
        }

        public HostDataPoint() {
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

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }

}
