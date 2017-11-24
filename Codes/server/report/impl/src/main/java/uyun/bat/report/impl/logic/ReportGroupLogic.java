package uyun.bat.report.impl.logic;

import org.springframework.stereotype.Component;
import uyun.bat.report.api.entity.ReportGroup;
import uyun.bat.report.impl.dao.ReportGroupDao;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by lilm on 17-2-27.
 */
@Component
public class ReportGroupLogic {

    @Resource
    ReportGroupDao reportGroupDao;

    public ReportGroup createReportGroup(ReportGroup group) {
        reportGroupDao.createReportGroup(group);
        return group;
    }

    public ReportGroup updateReportGroup(ReportGroup group) {
        reportGroupDao.updateReportGroup(group);
        return group;
    }

    public int deleteReportGroupOnLogic(ReportGroup group) {
        return reportGroupDao.deleteReportGroupOnLogic(group);
    }

    public List<ReportGroup> getGroupsByTenantId(String tenantId) {
        return reportGroupDao.getAllReportGroups(tenantId);
    }

    public List<ReportGroup> getHistoryGroupsByTenantId(String tenantId) {
        return reportGroupDao.getHistoryGroups(tenantId);
    }

    public int batchInsertGroups(List<ReportGroup> list) {
        return reportGroupDao.batchInsertGroups(list);
    }

    public int deleteGroup(ReportGroup group) {
        return reportGroupDao.deleteGroup(group);
    }
}
