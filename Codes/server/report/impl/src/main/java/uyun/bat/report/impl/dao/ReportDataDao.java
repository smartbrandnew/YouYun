package uyun.bat.report.impl.dao;

import uyun.bat.report.api.entity.ReportData;
import uyun.bat.report.api.entity.web.TReportDataAll;

import java.util.List;
import java.util.Map;

/**
 * Created by lilm on 17-3-6.
 */
public interface ReportDataDao {

    int insert(ReportData reportData);

    List<ReportData> selectByReportId(String reportId);

    TReportDataAll selectAllDataByConditions(Map<String, Object> map);

    int deleteByReportId(String reportId);

    ReportData selectReportByDate(ReportData reportData);

}
