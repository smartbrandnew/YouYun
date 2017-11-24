package uyun.bat.report.impl.dao;

import uyun.bat.report.api.entity.ReportMetricData;

import java.util.List;

/**
 * Created by lilm on 17-3-6.
 */
public interface ReportMetricDataDao {

    int insertBatch(List<ReportMetricData> list);

    int deleteByReportId(String reportId);

}
