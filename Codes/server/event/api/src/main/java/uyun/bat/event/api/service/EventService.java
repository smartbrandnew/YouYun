package uyun.bat.event.api.service;

import uyun.bat.event.api.entity.*;

import java.util.Date;
import java.util.List;

public interface EventService {

    /**
     * 插入事件
     * @param event
     * @return
     */
    Event create(Event event);

    /**
     * 根据条件查询事件分页数据
     * @param tenantId
     * @param page
     * @param rp
     * @param searchValue
     * @param beginTime
     * @param endTime
     * @return
     */
    PageEvent searchEvent(String tenantId, int page, int rp, String searchValue,String serverity, long beginTime, long endTime, Integer granularity);

    /**
     * 根据条件查询事件柱状图数据
     * @param tenantId
     * @param searchValue
     * @param beginTime
     * @param endTime
     * @param granularity
     * @return
     */
    EventGraphData searchEventGraphData(String tenantId, String searchValue, long beginTime, long endTime, int granularity);

    /**
     * 批量插入事件
     * @param events
     * @return
     */
    long create(List<Event> events);

    /**
     * 根据故障ID获取事件列表
     * @param tenantId
     * @param id
     * @param faultId
     * @param current
     * @param pageSize
     * @return
     */
    PageEvent getEventsByFaultId(String tenantId, String id, String faultId, int current, int pageSize);

    /**
     *
     * @param tenantId
     * @param sourceTypes
     * @param serveritys
     * @param keyWords
     * @param beginTime
     * @param endTime
     * @return
     */
    List<EventMonitorData> queryMatchedMonitorData(String tenantId, Short[] sourceTypes, Short[] serveritys, String keyWords,String[] tags,  Date beginTime, Date endTime);

    /**
     * 根据事件ID获取其tags
     * @param eventId
     * @return
     */
    List<String> getTagsByEventId(String eventId);

    /**
     * 根据时间获取事件数量
     * @param tenantId
     * @param begin
     * @param end
     * @return
     */
    int getEventCount(String tenantId,Date begin,Date end);


    /**
     * 根据资源ID获取一定时间内事件列表
     * @param tenantId
     * @param page
     * @param rp
     * @param resId
     * @param begin
     * @param end
     * @return
     */
    MinePageEvent searchEvent(String tenantId,int page, int rp, String resId,Date begin,Date end);

    /**
     * 根据总览的key:value获取未恢复的告警和错误事件列表
     * searchValue可查ip hostname msgcontent
     * @param tenantId
     * @param currentPage
     * @param pageSize
     * @param key
     * @param searchValue
     * @param sort
     */
    PageUnrecoveredEvent getUnrecoveredEvents(String tenantId, int currentPage, int pageSize, String key,
			String searchValue, String sort);

    /**
     * 统一资源库由资源oldId更新事件为资源newId
     * @param tenantId
     * @param oldResId
     * @param newResId
     */
    boolean updateEventsByOldResId(String tenantId, String oldResId, String newResId);

    /**
     * 根据资源id查询告警条件及信息
     *
     * @param tenantId
     * @param currentPage
     * @param pageSize
     */
    PageResEvent getAlertResEvents(String tenantId, String resourceId, int currentPage, int pageSize);

}

