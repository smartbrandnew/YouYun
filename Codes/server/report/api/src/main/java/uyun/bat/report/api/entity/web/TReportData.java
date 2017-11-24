package uyun.bat.report.api.entity.web;

import java.io.Serializable;

/**
 * Created by lilm on 17-3-7.
 */
public class TReportData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportDataId;

    private String startDate;

    private String endDate;

    public String getReportDataId() {
        return reportDataId;
    }

    public void setReportDataId(String reportDataId) {
        this.reportDataId = reportDataId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
