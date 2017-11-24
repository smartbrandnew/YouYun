package uyun.bat.report.api.service;

import uyun.bat.report.api.entity.Report;
import uyun.bat.report.api.entity.ReportGroup;

import java.util.List;
import java.util.Map;

public interface ReportService {

    Report createReport(Report report);

    Report updateReport(Report report);

    String deleteReport(String tenantId, String reportId);

    String exportReport(String tenantId, Map<String, Object> params);

    String reportSwitch(String tenantId, String reportId);

    Report queryReportById(String tenantId, String reportId);

    Report queryReport(String reportId, String reportDataId, String tenantId, String sortField, String sortOrder);

    /*从datastore查询实时数据*/
    Map<String, Object> queryResourceMetricsList(List<String> resTags, List<String> metricsArr,
                                                 String tenantId, String sortField, String sortOrder,
                                                 String type, Long start, Long end);

    List<Report> getReportsByGroupId(String tenantId, String groupId);

    /*保存报表历史数据*/
    void createReportDataAll(Report report);

    /*同步报表历史数据*/
    boolean createReportDataAllSync(Report report, boolean isCheck);

    /*----导航----*/
    ReportGroup createReportGroup(ReportGroup report);

    ReportGroup updateReportGroup(ReportGroup report);

    void deleteReportGroup(ReportGroup report);

    List<ReportGroup> getAllReportGroups(String tenantId);

    /**
     * 查询历史所有记录
     * @param tenantId
     * @return
     */
    List<ReportGroup> getHistoryReportGroups(String tenantId);

    int batchInsertDefaultGroups(List<ReportGroup> list);

    byte[] getReportFile(String file);

    void removeAllInvalidFiles();
}
