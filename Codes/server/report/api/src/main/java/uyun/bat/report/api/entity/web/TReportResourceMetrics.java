package uyun.bat.report.api.entity.web;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lilm on 17-3-6.
 * 返回前台去掉多余字段
 */
public class TReportResourceMetrics implements Serializable {

    private static final long serialVersionUID = 1L;
    private String resourceId;

    private String hostname;

    private String ipaddr;

    private Double valAvg;

    private List<TReportMetricData> metrics;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public Double getValAvg() {
        return valAvg;
    }

    public void setValAvg(Double valAvg) {
        this.valAvg = valAvg;
    }

    public List<TReportMetricData> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<TReportMetricData> metrics) {
        this.metrics = metrics;
    }
}
