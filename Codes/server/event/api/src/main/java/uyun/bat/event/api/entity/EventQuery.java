package uyun.bat.event.api.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class EventQuery extends PageEvent {

    private String tenantId;
    private String resId;
    private Date occurTime;
    private List<String> eventIds=new ArrayList<String>();
    private Integer granularity;
    private String faultId;
    private String eventId;
    private Short serverity;
    private Short sourceType;
    private String searchValue;
    private Set<String> searchValues;
    private String[] objectIds;
    public EventQuery(){}

    public EventQuery(String tenantId,int currentPage, int pageSize, String searchValue, Date beginTime, Date endTime, int granularity){
        this.setTenantId(tenantId);
        this.setCurrentPage(currentPage);
        this.setPageSize(pageSize);
        this.setSearchValue(searchValue);
        this.setBeginTime(beginTime);
        this.setEndTime(endTime);
        this.setGranularity(granularity);
    }

    public EventQuery(String tenantId,int currentPage, int pageSize, String searchValue, Date beginTime, Date endTime){
        this.setTenantId(tenantId);
        this.setCurrentPage(currentPage);
        this.setPageSize(pageSize);
        this.setSearchValue(searchValue);
        this.setBeginTime(beginTime);
        this.setEndTime(endTime);
    }

    public EventQuery(String tenantId,String eventId,String faultId,int currentPage, int pageSize){
        this.setTenantId(tenantId);
        this.setCurrentPage(currentPage);
        this.setPageSize(pageSize);
        this.setFaultId(faultId);
        this.setEventId(eventId);
    }

    public EventQuery(String tenantId, int currentPage, int pageSize, String searchValue, Short serverity, Date beginTime, Date endTime) {
        this.setTenantId(tenantId);
        this.setCurrentPage(currentPage);
        this.setPageSize(pageSize);
        this.setSearchValue(searchValue);
        this.setBeginTime(beginTime);
        this.setEndTime(endTime);
        this.setServerity(serverity);
    }

    public EventQuery(String tenantId, String eventId, String faultId) {
        this.setTenantId(tenantId);
        this.setFaultId(faultId);
        this.setEventId(eventId);
    }

    public String[] getObjectIds() {
        return objectIds;
    }

    public void setObjectIds(String[] objectIds) {
        this.objectIds = objectIds;
    }

    public Short getSourceType() {
        return sourceType;
    }

    public void setSourceType(Short sourceType) {
        this.sourceType = sourceType;
    }

    public Set<String> getSearchValues() {
        return searchValues;
    }

    public void setSearchValues(Set<String> searchValues) {
        this.searchValues = searchValues;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public Short getServerity() {
        return serverity;
    }

    public void setServerity(Short serverity) {
        this.serverity = serverity;
    }


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public EventQuery(String tenantId, String searchValue, Date beginTime, Date endTime, int granularity) {
        this.setTenantId(tenantId);
        this.setSearchValue(searchValue);
        this.setBeginTime(beginTime);
        this.setEndTime(endTime);
        this.setGranularity(granularity);
    }

    public String getFaultId() {
        return faultId;
    }

    public void setFaultId(String faultId) {
        this.faultId = faultId;
    }

    public Integer getGranularity() {
        return granularity;
    }

    public void setGranularity(Integer granularity) {
        this.granularity = granularity;
    }


    public List<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }


    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public Date getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(Date occurTime) {
        this.occurTime = occurTime;
    }
}
