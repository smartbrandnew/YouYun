package uyun.bat.web.api.report.service;

import uyun.bat.report.api.entity.Report;
import uyun.bat.report.api.entity.ReportGroup;
import uyun.bat.web.api.report.entity.SimpleReport;
import uyun.bat.web.api.report.entity.SimpleReportGroup;

import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-2-28.
 */
public interface ReportWebService {

    Report createReport(String tenantId, Report report);

//    Report updateReport(String tenantId, Report report);

    /**
     * 报表开关
     * @param tenantId
     * @param reportId
     *
     */
    void reportSwitch(String tenantId, String reportId);

    Map exportReport(String tenantId, Map<String, Object> params);

    /**
     * 报表删除
     * @param tenantId
     * @param reportId
     *
     */
    void deleteReport(String tenantId, String reportId);

    /**
     * 报表查询
     * @param tenantId
     * @param reportId 报表模板id
     * @param reportDataId 查询的报表数据id
     * @return
     */
    Report queryReport(String tenantId, String reportId, String reportDataId,
                       String sortField, String sortOrder, Integer pageSize, Integer current);

    /**
     * 查询租户单个分组下报表模板
     * @param tenantId
     * @param groupId
     * @return
     */
    List<SimpleReport> queryReportsByGroupId(String tenantId, String groupId);

    /**
     * 创建报表导航
     * @param tenantId
     * @return
     */
    ReportGroup createReportGroup(String tenantId, ReportGroup group);

    /**
     * 更新报表导航
     * @param tenantId
     * @param group
     * @return
     */
    ReportGroup updateReportGroup(String tenantId, ReportGroup group);

    /**
     * 查询租户所有导航分组
     * @param tenantId
     * @return
     */
    List<SimpleReportGroup> getAllGroupsByTenantId(String tenantId);

    /**
     * 删除单个导航
     * @param tenantId
     * @param group
     */
    void deleteReportGroup(String tenantId, ReportGroup group);

    void downloadFile(String tenantId, String file, String name);

}
