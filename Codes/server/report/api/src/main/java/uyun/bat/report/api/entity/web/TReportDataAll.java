package uyun.bat.report.api.entity.web;

import java.io.Serializable;
import java.util.*;

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
 * 返回前台去掉些多余字段
 */
public class TReportDataAll implements Serializable{
    private static final long serialVersionUID = 1L;
    private String reportDataId;

    private int totalCount = 0;
    private int pageSize;
    private int currentPage = 1;

    private List<TReportResourceMetrics> resources;

    public String getReportDataId() {
        return reportDataId;
    }

    public void setReportDataId(String reportDataId) {
        this.reportDataId = reportDataId;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List<TReportResourceMetrics> getResources() {
        return resources;
    }

    public void setResources(List<TReportResourceMetrics> resources) {
        this.resources = resources;
    }
}
