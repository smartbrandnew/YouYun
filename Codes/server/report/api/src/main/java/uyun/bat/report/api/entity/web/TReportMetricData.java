package uyun.bat.report.api.entity.web;

import java.io.Serializable;

public class TReportMetricData implements Serializable, Comparable<TReportMetricData> {

    private static final long serialVersionUID = 1L;
    private String metricName;

    private Double valAvg;

    private String points;

    private String unit;

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

    @Override
    public int compareTo(TReportMetricData o) {
        if (o == null || o.getMetricName() == null) {
            return -1;
        }
        byte[] bytes = o.getMetricName().getBytes();
        long lengthO = 0;
        for (byte aByte : bytes) {
            lengthO += aByte;
        }
        long lengthM = 0;
        for (byte a : metricName.getBytes()) {
            lengthM += a;
        }
        if (lengthM > lengthO) {
            return 1;
        } else {
            return -1;
        }
    }
}