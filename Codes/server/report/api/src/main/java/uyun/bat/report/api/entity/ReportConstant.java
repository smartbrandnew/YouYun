package uyun.bat.report.api.entity;

/**
 * Created by lilm on 17-3-7.
 */
public class ReportConstant {

    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";
    public static final String REPORT_DAILY = "daily";
    public static final String REPORT_WEEKLY = "weekly";
    public static final String REPORT_MONTHLY = "monthly";
    public static final String DIAGRAM_COLUMN = "column";
    public static final String DIAGRAM_LINE = "line";

    public static final String REDIS_FILE_KEY = "bat-report-files";
    public static final String REDIS_REPORT_FAILED_SET = "bat-report-failed-set";
    public static final String REDIS_REPORT_LOCK_KEY = "bat-report-lock";
    public static final String REDIS_REPORT_LOCKING = "LOCKING";
    public static final String REDIS_REPORT_COMPLETE = "COMPLETE";
    public static final int REDIS_REPORT_LOCK_EXPIRE = 360;

}
