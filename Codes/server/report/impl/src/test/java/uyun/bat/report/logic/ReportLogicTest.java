package uyun.bat.report.logic;

import org.junit.Assert;
import org.junit.Test;
import uyun.bat.report.Startup;
import uyun.bat.report.api.entity.*;
import uyun.bat.report.impl.logic.ReportLogic;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by lilm on 17-3-17.
 */
public class ReportLogicTest {
    private static ReportLogic reportLogic = (ReportLogic) Startup.getInstance().getBean("reportLogic");

    private static final String TENANT_ID = "e0a67e986a594a61b3d1e523a0a39c77";
    private static final String GROUP_ID = "9c10286e10fc464fa702de6aa7aedab1";
    private static final String REPORT_ID = "12345678900987654321123456789012";

    @Test
    public void save() {
        Report r = new Report();
        r.setReportId(UUID.randomUUID().toString());
        r.setReportName("unit test report");
        r.setTenantId(TENANT_ID);
        r.setModified(new Date());
        r.setDiagramType(ReportConstant.DIAGRAM_LINE);
        r.setReportType(ReportConstant.REPORT_DAILY);
        r.setGroupId(GROUP_ID);
        r.setStatus((short)1);
        reportLogic.createReport(r);
        int flag = reportLogic.deleteReport(r);
        Assert.assertEquals(1, flag);
    }

    @Test
    public void testCreateReportDataAll() {
        ReportDataAll rd = new ReportDataAll();
        rd.setReportDataId(UUID.randomUUID().toString());
        rd.setReportId(REPORT_ID);
        rd.setCreateTime(new Date());
        rd.setStartDate(new Date());
        rd.setEndDate(new Date());
        List<ReportResourceMetrics> resources = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ReportResourceMetrics rrm = new ReportResourceMetrics();
            rrm.setResourceId(UUID.randomUUID().toString());
            rrm.setReportId(REPORT_ID);
            rrm.setReportDataId(rd.getReportDataId());
            rrm.setHostname("local" + i);
            rrm.setIpaddr("10.0.0.1");
            List<ReportMetricData> metrics = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                ReportMetricData metricData = new ReportMetricData();
                metricData.setReportId(REPORT_ID);
                metricData.setReportDataId(rd.getReportDataId());
                metricData.setResourceId(rrm.getResourceId());
                metricData.setMetricName("test.cpu.usage" + j);
                metricData.setUnit("%");
                metricData.setValAvg(22.22);
                metricData.setPoints("[[1488882343,33.33]]");
                metrics.add(metricData);
            }
            rrm.setMetricDataList(metrics);
            resources.add(rrm);
        }
        rd.setResources(resources);
        reportLogic.createReportDataAll(rd);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        reportLogic.deleteAllReportData(REPORT_ID);
    }

    @Test
    public void testGetReportByReportId() {
        Report r = new Report();
        r.setReportId(UUID.randomUUID().toString());
        r.setTenantId(TENANT_ID);
        reportLogic.getReportById(r);
    }

    @Test
    public void testGetReportByGroupId() {
        Report r = new Report();
        r.setGroupId(GROUP_ID);
        r.setTenantId(TENANT_ID);
        reportLogic.getReportByGroupId(r);
    }

    @Test
    public void testGetAllValidReport() {
        List<Report> list = reportLogic.getAllValidReport();
        Assert.assertNotNull(list);
    }

}
