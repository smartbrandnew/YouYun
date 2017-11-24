package uyun.bat.report.api.entity;

import java.io.Serializable;

public class ReportMetricData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportDataId;

    private String resourceId;
    
    private String metricName;

    private Double valAvg;

    private String points;

    private String unit;

    private String reportId;

    public String getReportDataId() {
        return reportDataId;
    }

    public void setReportDataId(String reportDataId) {
        this.reportDataId = reportDataId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName == null ? null : metricName.trim();
    }

    public Double getValAvg() {
        return valAvg;
    }

    public void setValAvg(Double valAvg) {
        this.valAvg = valAvg;
    }

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}