package uyun.bat.datastore.api.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by lilm on 17-3-3.
 */
public class ResourceMetrics implements Comparable<ResourceMetrics>, Serializable {
    private static final long serialVersionUID= 1L;
    private String id;
    private Date modified;
    private String hostname;
    private String ipaddr;

    private List<MetricVal> metrics;
    private String sortField;
    private String sortOrder = "asc";
    private Double valAvg;

    @Override
    public int compareTo(ResourceMetrics rm) {
        if (rm == null || rm.valAvg == null || valAvg == null) {
            return -1;
        }
        if ("desc".equals(sortOrder)) {
            //降序 desc
            if (valAvg > rm.valAvg) {
                return -1;
            } else {
                return 1;
            }
        } else {
            //升序 asc
            if (valAvg > rm.valAvg) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
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

    public List<MetricVal> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricVal> metrics) {
        this.metrics = metrics;
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

    public Double getValAvg() {
        return valAvg;
    }

    public void setValAvg(Double valAvg) {
        this.valAvg = valAvg;
    }

    public class MetricVal implements Serializable {
        private static final long serialVersionUID= 1L;
        private String metricName;
        private String unit;
        private Double valAvg;
        private double[][] points;

        public MetricVal(double[][] points, String metricName) {
            this.points = points;
            this.metricName = metricName;
        }

        public MetricVal() {}

        public String getMetricName() {
            return metricName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Double getValAvg() {
            return valAvg;
        }

        public void setValAvg(Double valAvg) {
            this.valAvg = valAvg;
        }

        public double[][] getPoints() {
            return points;
        }

        public void setPoints(double[][] points) {
            this.points = points;
        }
    }
}
