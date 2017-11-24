package uyun.bat.report.impl.facade;

import uyun.bat.common.utils.StringUtils;
import uyun.bat.report.api.entity.ReportGroup;
import uyun.bat.report.impl.logic.LogicManager;

import java.util.List;

/**
 * Created by lilm on 17-2-27.
 */
public class ReportGroupFacade {

    public ReportGroup createReportGroup(ReportGroup group) {
        checkGroupData(group);
        return LogicManager.getInstance().getReportGroupLogic().createReportGroup(group);
    }

    public ReportGroup updateReportGroup(ReportGroup group) {
        checkGroupData(group);
        return LogicManager.getInstance().getReportGroupLogic().updateReportGroup(group);
    }

    /**
     * 只做逻辑删除, 保留历史记录
     * @param group
     */
    public int deleteReportGroupOnLoic(ReportGroup group) {
        return LogicManager.getInstance().getReportGroupLogic().deleteReportGroupOnLogic(group);
    }

    public List<ReportGroup> getReportsByTenantId(String tenantId) {
        return LogicManager.getInstance().getReportGroupLogic().getGroupsByTenantId(tenantId);
    }

    public List<ReportGroup> getHistoryReportsByTenantId(String tenantId) {
        return LogicManager.getInstance().getReportGroupLogic().getHistoryGroupsByTenantId(tenantId);
    }

    public int batchInsertGroups(List<ReportGroup> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }
        for (ReportGroup group : list) {
            checkGroupData(group);
        }
        return LogicManager.getInstance().getReportGroupLogic().batchInsertGroups(list);
    }

    private void checkGroupData(ReportGroup group) {
        if (group == null) {
            throw new IllegalArgumentException("Invalid report parameter: the Group cannot be empty");
        }
        if (StringUtils.isBlank(group.getGroupId())) {
            throw new IllegalArgumentException("Invalid report parameter: group groupId cannot be empty");
        }
        if (group.getModified() == null) {
            throw new IllegalArgumentException("Invalid report parameter: modified cannot be empty");
        }
    }

}
