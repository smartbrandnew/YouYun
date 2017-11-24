package uyun.bat.web.api.report.entity;

/**
 * Created by lilm on 17-3-10.
 */
public class ReportExport {

    private String reportId;

    private String reportDataId;

    private String file;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getReportDataId() {
        return reportDataId;
    }

    public void setReportDataId(String reportDataId) {
        this.reportDataId = reportDataId;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
