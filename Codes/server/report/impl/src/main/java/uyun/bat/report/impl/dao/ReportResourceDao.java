package uyun.bat.report.impl.dao;

import uyun.bat.report.api.entity.ReportResource;

import java.util.List;

/**
 * Created by lilm on 17-3-6.
 */
public interface ReportResourceDao {

    int insertBatch(List<ReportResource> list);

    int deleteByReportId(String reportId);
}
