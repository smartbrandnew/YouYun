package uyun.bat.report.api.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lilm on 17-3-6.
 * 包含一条报表所有数据的实体类 ReportDataAll
 * {
 *     ReportData, //报表数据 日期标号等等
 *     "resources" : [ //资源列表以及资源下每个指标及数据
 *          {
 *              ReportResource, //资源数据
 *              "metricDataList" : [
 *                  {
 *                      ReportMetricData //指标及数据
 *                  }
 *              ]
 *          }
 *     ]
 * }
 */
public class ReportDataAll extends ReportData implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<ReportResourceMetrics> resources;

    public List<ReportResourceMetrics> getResources() {
        return resources;
    }

    public void setResources(List<ReportResourceMetrics> resources) {
        this.resources = resources;
    }

    public ReportData getReportData() {
        ReportData reportData = new ReportData();
        reportData.setReportDataId(this.getReportDataId());
        reportData.setReportId(this.getReportId());
        reportData.setCreateTime(this.getCreateTime());
        reportData.setStartDate(this.getStartDate());
        reportData.setEndDate(this.getEndDate());
        return reportData;
    }

    public List<ReportResource> getReportResources() {
        List<ReportResource> pureResources = new ArrayList<ReportResource>();
        Set<String> resIds = new HashSet<String>();
        if (resources != null && resources.size() > 0) {
            for (ReportResourceMetrics reportResourceMetrics : resources) {
                //resource去重
                if (resIds.contains(reportResourceMetrics.getResourceId())) {
                    continue;
                }
                resIds.add(reportResourceMetrics.getResourceId());
                pureResources.add(reportResourceMetrics.getReportResource());
            }
        }
        return pureResources;
    }

    public List<ReportMetricData> getReportMetricDataList() {
        List<ReportMetricData> reportMetricDataList = new ArrayList<>();
        if (resources != null && resources.size() > 0) {
            for (ReportResourceMetrics reportResourceMetrics : resources) {
                List<ReportMetricData> oneMetricDataList = reportResourceMetrics.getMetricDataList();
                if (oneMetricDataList != null) {
                    reportMetricDataList.addAll(oneMetricDataList);
                }
            }
        }
        return reportMetricDataList;
    }
}
