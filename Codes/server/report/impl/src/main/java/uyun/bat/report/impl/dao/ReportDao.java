package uyun.bat.report.impl.dao;

import uyun.bat.report.api.entity.Report;

import java.util.List;

/**
 * Created by lilm on 17-2-27.
 */
public interface ReportDao {

    int createReport(Report report);

    int updateReport(Report report);

    int deleteReport(Report report);

    Report getReportById(Report report);

    List<Report> getReportByGroupId(Report report);

    List<Report> getAllValidReport();
}
