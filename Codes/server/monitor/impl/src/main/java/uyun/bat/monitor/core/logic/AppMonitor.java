package uyun.bat.monitor.core.logic;

import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.StateStatus;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.api.entity.EventTag;
import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.*;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.core.util.TagUtil;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bat.monitor.impl.util.JsonUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AppMonitor implements Checker {

    private Monitor monitor;
    private AppMonitorParam appMonitorParam;

    private List<StateMetricDataPoint> stateMetricDataPoints;

    /**
     * 查询表达式
     */
    private String query;

    public AppMonitor(Monitor monitor, AppMonitorParam appMonitorParam){
        this.monitor=monitor;
        this.appMonitorParam=appMonitorParam;
    }


    public boolean match(StateMetricData data) {
        return appMonitorParam.match(data);
    }

    @Override
    public MonitorState checkIfMonitorStatusRollover() {
        PeriodUtil.Period period= PeriodUtil.generatePeriod(appMonitorParam.getPeriod());
        MonitorState state= MonitorState.OK;
        StateService stateService = ServiceManager.getInstance().getStateService();
        Checkperiod[] checkperiods = stateService.getLastCheckperiods(appMonitorParam.getState(), generateTags(),period.getStart(),period.getEnd());
        if (null==checkperiods||0==checkperiods.length){
            return state;
        }
        stateMetricDataPoints=new ArrayList<>();
        for(Checkperiod checkperiod:checkperiods){
            //判断检查点状态是否紧急并且此状态最后上报时间小于设置时间
           if (checkperiod.getValue().equals(StateStatus.CRITICAL.getId()+"") && checkperiod.getFirstTime()<period.getStart()){
               state= MonitorState.checkByCode(appMonitorParam.getStatus());
               stateMetricDataPoints.add(new StateMetricDataPoint(state,checkperiod.getTags()));
           }else{
               stateMetricDataPoints.add(new StateMetricDataPoint(MonitorState.OK,checkperiod.getTags()));
           }
        }
        return state;
    }

    private String[] generateTags() {
        List<TagEntry> tagEntries=appMonitorParam.getTags();
        List<TagEntry> tagList = new ArrayList<TagEntry>();
        if (tagEntries != null && tagEntries.size() > 0) {
            tagList.addAll(tagEntries);
        }
        TagUtil.generateTags(tagList);
        //添加tenantID标签
        tagList.add(0, new TagEntry(StateUtil.TENANT_ID, monitor.getTenantId()));

        String[] temps = new String[tagList.size()];
        for (int i = 0, length = tagList.size(); i < length; i++) {
            TagEntry te = tagList.get(i);
            temps[i] = te.getKey() + TagUtil.SEPARATOR + te.getValue();
        }
        return temps;
    }

    public void doAfterCheck() {
        if (null==stateMetricDataPoints||stateMetricDataPoints.isEmpty()){
            return;
        }
        for (StateMetricDataPoint data : stateMetricDataPoints) {
            Resource resource=ServiceManager.getInstance().getResourceService().queryResById(data.getResourceId(),data.getTenantId());
            Event event = generateEvent(data,resource);
            if (event == null)
                continue;
            dotrigger(event, data.getMonitorStatus(), resource.getHostname(),resource.getIpaddr());
        }
    }

    private Event generateEvent(StateMetricDataPoint dataPoint,Resource resource) {
        Event event = new Event();
        EventServerityType eventServerityType = MonitorQueryUtil.getEventServerityType(dataPoint.getMonitorStatus());
        if (eventServerityType == null)
            return null;
        else
            event.setServerity(eventServerityType.getKey());

        event.setMsgTitle(monitor.getName());
        event.setResId(dataPoint.getResourceId());
        event.setOccurTime(new Timestamp(System.currentTimeMillis()));

        event.setSourceType(EventSourceType.MONITOR.getKey());
        event.setMonitorId(monitor.getId());
        event.setMonitorType(monitor.getMonitorType().getCode());
        // 监测器tag
        List<EventTag> monitorTags = new ArrayList<>();
        if (appMonitorParam.getTags() != null && appMonitorParam.getTags().size() > 0) {
            for (TagEntry te : appMonitorParam.getTags()) {
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

    private void dotrigger(Event event, MonitorState monitorState, String hostName, String ip) {
        CheckContext context = new CheckContext(event, null, hostName, ip, null, monitorState, appMonitorParam,MonitorType.APP);
        boolean trigger = CheckController.getInstance().trigger(monitor, context,
                generateSymbol(event.getResId(), monitorState));
        if (trigger && monitor.getNotify() && monitor.getNotifyUserIdList() != null
                && monitor.getNotifyUserIdList().size() > 0){
            CheckController.getInstance().notify(context, monitor.getNotifyUserIdList());
        }
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
        //添加app标签
        tags.add(new TagEntry("app",generateApp()));
        TagUtil.generateTags(tags);
        symbol.setTags(tags);
        return symbol;
    }

    private String generateApp(){
        int index=appMonitorParam.getState().lastIndexOf(".");
        if (index!=-1){
             return appMonitorParam.getState().substring(0,index);
        }
        return null;
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

    public Monitor getMonitor(){
        return monitor;
    }

    private static class StateMetricDataPoint {
        private MonitorState monitorStatus;
        private String[] tags;

        public StateMetricDataPoint() {
        }

        private StateMetricDataPoint(MonitorState monitorStatus, String[] tags) {
            this.monitorStatus=monitorStatus;
            this.tags=tags;
        }

        public MonitorState getMonitorStatus() {
            return monitorStatus;
        }

        public void setMonitorStatus(MonitorState monitorStatus) {
            this.monitorStatus = monitorStatus;
        }

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] tags) {
            this.tags = tags;
        }

        private String getResourceId() {
            if (null==tags||0==tags.length){
                return null;
            }
            for(String tag:tags){
                if (tag.startsWith(StateUtil.RESOURCE_ID)){
                    return tag.substring(StateUtil.RESOURCE_ID.length()+1);
                }
            }
            return null;
        }
        
        private String getTenantId() {
          if (null==tags||0==tags.length){
              return null;
          }
          for(String tag:tags){
              if (tag.startsWith(StateUtil.TENANT_ID)){
                  return tag.substring(StateUtil.TENANT_ID.length()+1);
              }
          }
          return null;
      }
    }

    public AppMonitorParam getAppMonitorParam() {
        return appMonitorParam;
    }

    public void setAppMonitorParam(AppMonitorParam appMonitorParam) {
        this.appMonitorParam = appMonitorParam;
    }
}
