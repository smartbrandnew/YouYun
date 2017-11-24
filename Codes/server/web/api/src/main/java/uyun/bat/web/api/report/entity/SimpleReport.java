package uyun.bat.web.api.report.entity;

import java.io.Serializable;

/**
 * Created by lilm on 17-3-14.
 */
public class SimpleReport implements Serializable {
    private static final long serialVersionUID = 1L;

    public SimpleReport() {
    }

    public SimpleReport(String reportId, String reportType, String reportName, String diagramType, Short status, String groupId) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.reportName = reportName;
        this.diagramType = diagramType;
        this.status = status;
        this.groupId = groupId;
    }

    private String reportId;
    private String reportType;
    private String reportName;
    private String diagramType;
    private Short status;
    private String groupId;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
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
}
