package uyun.bat.report.api.entity;

import uyun.bat.report.api.entity.web.TReportData;
import uyun.bat.report.api.entity.web.TReportDataAll;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    public Report(String reportId, String tenantId) {
        this.reportId = reportId;
        this.tenantId = tenantId;
    }

    public Report() {
    }

    private String reportId;

    private String reportName;

    private String reportType;

    private String diagramType;

    private Short status;

    private String groupId;

    private Date modified;

    private String tenantId;

    private String sortField;

    private String sortOrder;

    private Integer defaultSize;

    private List<String> metrics;

    private List<String> resTags;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getDiagramType() {
        return diagramType;
    }

    public void setDiagramType(String diagramType) {
        this.diagramType = diagramType;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getDefaultSize() {
        return defaultSize;
    }

    public void setDefaultSize(Integer defaultSize) {
        this.defaultSize = defaultSize;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<String> metrics) {
        this.metrics = metrics;
    }

    public List<String> getResTags() {
        return resTags;
    }

    public void setResTags(List<String> resTags) {
        this.resTags = resTags;
    }

    //报表日历 startDate降序排列
    private List<TReportData> calendar = null;

    private TReportDataAll data = null;

    public List<TReportData> getCalendar() {
        return calendar;
    }

    public void setCalendar(List<TReportData> calendar) {
        this.calendar = calendar;
    }

    public TReportDataAll getData() {
        return data;
    }

    public void setData(TReportDataAll data) {
        this.data = data;
    }
}