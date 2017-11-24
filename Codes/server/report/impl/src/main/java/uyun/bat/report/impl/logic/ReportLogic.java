package uyun.bat.report.impl.logic;

import org.springframework.stereotype.Component;
import uyun.bat.report.api.entity.*;
import uyun.bat.report.api.entity.web.TReportDataAll;
import uyun.bat.report.impl.dao.ReportDao;
import uyun.bat.report.impl.dao.ReportDataDao;
import uyun.bat.report.impl.dao.ReportMetricDataDao;
import uyun.bat.report.impl.dao.ReportResourceDao;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-2-27.
 */
@Component
public class ReportLogic {

    @Resource
    ReportDao reportDao;
    @Resource
    ReportDataDao reportDataDao;
    @Resource
    ReportResourceDao reportResourceDao;
    @Resource
    ReportMetricDataDao reportMetricDataDao;

    /*-- 报表模板report --*/
    public Report createReport(Report report) {
        int flag = reportDao.createReport(report);
        if (flag <= 0) {
            return null;
        }
        return report;
    }

    public Report updateReport(Report report) {
        reportDao.updateReport(report);
        return report;
    }

    public int deleteReport(Report report) {
        return reportDao.deleteReport(report);
    }

    public Report getReportById(Report report) {
        return reportDao.getReportById(report);
    }

    public List<Report> getReportByGroupId(Report report) {
        return reportDao.getReportByGroupId(report);
    }

    public List<Report> getAllValidReport() {
        return reportDao.getAllValidReport();
    }

    /*-- 报表数据data --*/
    public ReportDataAll createReportDataAll(ReportDataAll reportDataAll) {
        int flag = reportDataDao.insert(reportDataAll.getReportData());
        if (flag == 1) {
            List<ReportResource> resources = reportDataAll.getReportResources();
            if (resources != null && resources.size() > 0) {
                int count = reportResourceDao.insertBatch(resources);
                if (count <= 0) {
                    return null;
                }
            }
            List<ReportMetricData> metricDataList = reportDataAll.getReportMetricDataList();
            if (metricDataList != null && metricDataList.size() > 0) {
                int count = reportMetricDataDao.insertBatch(metricDataList);
                if (count <= 0) {
                    return null;
                }
            }
        } else {
            return null;
        }
        return reportDataAll;
    }

    public List<ReportData> getReportDataByReportId(String reportId) {
        return reportDataDao.selectByReportId(reportId);
    }

    public TReportDataAll getAllDataByConditions(Map<String, Object> map) {
        return reportDataDao.selectAllDataByConditions(map);
    }

    public ReportData getReportByDate(ReportData reportData) {
        return reportDataDao.selectReportByDate(reportData);
    }

    public void deleteAllReportData(String reportId) {
        reportDataDao.deleteByReportId(reportId);
        reportResourceDao.deleteByReportId(reportId);
        reportMetricDataDao.deleteByReportId(reportId);
    }

}
