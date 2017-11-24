package uyun.bat.report.api.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lilm on 17-3-6.
 */
public class ReportResourceMetrics extends ReportResource implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ReportMetricData> metricDataList;

    public List<ReportMetricData> getMetricDataList() {
        return metricDataList;
    }

    public void setMetricDataList(List<ReportMetricData> metricDataList) {
        this.metricDataList = metricDataList;
    }

    public ReportResource getReportResource() {
        ReportResource reportResource = new ReportResource();
        reportResource.setReportDataId(this.getReportDataId());
        reportResource.setResourceId(this.getResourceId());
        reportResource.setHostname(this.getHostname());
        reportResource.setIpaddr(this.getIpaddr());
        reportResource.setReportId(this.getReportId());
        return reportResource;
    }
}
