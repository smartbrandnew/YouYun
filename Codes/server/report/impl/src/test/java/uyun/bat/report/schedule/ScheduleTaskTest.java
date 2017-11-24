package uyun.bat.report.schedule;

import org.junit.Test;
import uyun.bat.report.Startup;
import uyun.bat.report.impl.schedule.ScheduleTask;

/**
 * Created by lilm on 17-3-9.
 */
public class ScheduleTaskTest {

    public static ScheduleTask st = Startup.getInstance().getBean(ScheduleTask.class);

    @Test
    public void testGenerateReport() {
//        st.generateReportDailyCheck();
    }
}
