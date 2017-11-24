package uyun.bat.report.impl.facade;

import uyun.bat.common.utils.StringUtils;
import uyun.bat.report.api.entity.Report;
import uyun.bat.report.api.entity.ReportData;
import uyun.bat.report.api.entity.ReportDataAll;
import uyun.bat.report.api.entity.web.TReportDataAll;
import uyun.bat.report.impl.logic.LogicManager;

import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-2-27.
 */
public class ReportFacade {

    public Report getReportById(Report report) {
        return LogicManager.getInstance().getReportLogic().getReportById(report);
    }

    public List<Report> getReportsByGroupId(Report report) {
        return LogicManager.getInstance().getReportLogic().getReportByGroupId(report);
    }

    public List<Report> getAllValidReport() {
        return LogicManager.getInstance().getReportLogic().getAllValidReport();
    }

    public Report createReport(Report report) {
        checkReportData(report);
        return LogicManager.getInstance().getReportLogic().createReport(report);
    }

    public Report updateReport(Report report) {
        checkReportData(report);
        return LogicManager.getInstance().getReportLogic().updateReport(report);
    }

    public int deleteReport(Report report) {
        return LogicManager.getInstance().getReportLogic().deleteReport(report);
    }

    private void checkReportData(Report report) {
        if (report == null) {
            throw new IllegalArgumentException("illegal report arg: report cannot be null");
        }
        if (StringUtils.isBlank(report.getReportId())) {
            throw new IllegalArgumentException("illegal report arg: reportId is null");
        }
        if (report.getGroupId() != null && report.getGroupId().length() != 32) {
            //illegal groupId
            report.setGroupId(null);
        }
        if (report.getModified() == null) {
            throw new IllegalArgumentException("illegal report arg: modified is null");
        }
    }

    public ReportDataAll createReportDataAll(ReportDataAll reportDataAll) {
        checkReportDataAll(reportDataAll);
        return LogicManager.getInstance().getReportLogic().createReportDataAll(reportDataAll);
    }

    private void checkReportDataAll(ReportDataAll reportDataAll) {
        if (reportDataAll == null) {
            throw new IllegalArgumentException("illegal report arg: report cannot be null");
        }
        if (StringUtils.isBlank(reportDataAll.getReportId()) ||
                StringUtils.isBlank(reportDataAll.getReportDataId())) {
            throw new IllegalArgumentException("illegal report arg: id is null");
        }
        if (reportDataAll.getEndDate() == null || reportDataAll.getStartDate() == null) {
            throw new IllegalArgumentException("illegal report arg: startDate or endDate is null");
        }
    }

    public List<ReportData> getReportDataByReportId(String reportId) {
        return LogicManager.getInstance().getReportLogic().getReportDataByReportId(reportId);
    }

    public TReportDataAll getAllDataByConditions(Map<String, Object> map) {
        return LogicManager.getInstance().getReportLogic().getAllDataByConditions(map);
    }

    public ReportData getReportDataByDate(ReportData reportData) {
        return LogicManager.getInstance().getReportLogic().getReportByDate(reportData);
    }

    public void deleteReportDataAll(String reportId) {
        LogicManager.getInstance().getReportLogic().deleteAllReportData(reportId);
    }
}
