package uyun.bat.report.impl.dao;

import uyun.bat.report.api.entity.ReportGroup;

import java.util.List;

public interface ReportGroupDao {
    int createReportGroup(ReportGroup report);

    int updateReportGroup(ReportGroup report);

    int deleteReportGroupOnLogic(ReportGroup group);

    List<ReportGroup> getAllReportGroups(String tenantId);

    List<ReportGroup> getHistoryGroups(String tenantId);

    int batchInsertGroups(List<ReportGroup> groups);

    int deleteGroup(ReportGroup group);
}