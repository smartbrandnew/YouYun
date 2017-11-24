package uyun.bat.report.logic;

import org.junit.Assert;
import org.junit.Test;
import uyun.bat.report.Startup;
import uyun.bat.report.api.entity.ReportGroup;
import uyun.bat.report.impl.logic.ReportGroupLogic;

import java.util.Date;
import java.util.UUID;

/**
 * Created by lilm on 17-3-17.
 */
public class ReportGroupLogicTest {

    private static ReportGroupLogic reportGroupLogic =
            (ReportGroupLogic) Startup.getInstance().getBean("reportGroupLogic");

    private static final String TENANT_ID = "e0a67e986a594a61b3d1e523a0a39c77";

    @Test
    public void save() {
        ReportGroup g = new ReportGroup();
        g.setGroupId(UUID.randomUUID().toString());
        g.setStatus((short) 1);
        g.setTenantId(TENANT_ID);
        g.setModified(new Date());
        g.setGroupName("unit test group");
        reportGroupLogic.createReportGroup(g);
        //update
        g.setModified(new Date());
        g.setGroupName("test");
        reportGroupLogic.updateReportGroup(g);
        //逻辑删除
        int i = reportGroupLogic.deleteReportGroupOnLogic(g);
        Assert.assertEquals(i, 1);
        //物理删除
        i = reportGroupLogic.deleteGroup(g);
        Assert.assertEquals(i, 1);
    }

    @Test
    public void getGroupsByTenantIdTest() {
        reportGroupLogic.getGroupsByTenantId(TENANT_ID);
    }

    @Test
    public void getHistoryGroupsByTenantIdTest() {
        reportGroupLogic.getHistoryGroupsByTenantId(TENANT_ID);
    }

}
