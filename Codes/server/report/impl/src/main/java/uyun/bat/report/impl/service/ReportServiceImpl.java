package uyun.bat.report.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uyun.bat.common.utils.StringUtils;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceMetrics;
import uyun.bat.datastore.api.service.MetricService;
import uyun.bat.datastore.api.service.ResourceService;
import uyun.bat.datastore.api.util.DateUtil;
import uyun.bat.report.api.entity.Report;
import uyun.bat.report.api.entity.ReportConstant;
import uyun.bat.report.api.entity.ReportData;
import uyun.bat.report.api.entity.ReportDataAll;
import uyun.bat.report.api.entity.ReportGroup;
import uyun.bat.report.api.entity.ReportMetricData;
import uyun.bat.report.api.entity.ReportResourceMetrics;
import uyun.bat.report.api.entity.web.TReportData;
import uyun.bat.report.api.entity.web.TReportDataAll;
import uyun.bat.report.api.entity.web.TReportMetricData;
import uyun.bat.report.api.entity.web.TReportResourceMetrics;
import uyun.bat.report.api.service.ReportService;
import uyun.bat.report.impl.dao.redis.RedisDao;
import uyun.bat.report.impl.facade.FacadeManager;
import uyun.bat.report.impl.service.threadPool.ExecutorThreadPool;
import uyun.bat.report.impl.utils.ReportExcelUtils;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lilm on 17-2-28.
 */
@Service(protocol = "dubbo")
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    MetricService metricService;
    @Autowired
    ResourceService resourceService;
    @Autowired
    RedisDao redisDao;

    @Override
    public Report createReport(Report report) {
        return FacadeManager.getInstance().getReportFacade().createReport(report);
    }

    @Override
    public Report updateReport(Report report) {
        return FacadeManager.getInstance().getReportFacade().updateReport(report);
    }

    @Override
    public String deleteReport(String tenantId, String reportId) {
        Report report = new Report();
        report.setReportId(reportId);
        report.setTenantId(tenantId);
        int flag = FacadeManager.getInstance().getReportFacade().deleteReport(report);
        if (flag > 0) {
            FacadeManager.getInstance().getReportFacade().deleteReportDataAll(reportId);
        }
        return reportId;
    }


    @Override
    public String reportSwitch(String tenantId, String reportId) {
        Report report =
                FacadeManager.getInstance().getReportFacade().getReportById(new Report(reportId, tenantId));
        if (report != null) {
            Report switchReport = new Report();
            switchReport.setReportId(reportId);
            switchReport.setModified(new Date());
            if (report.getStatus() == 1) {
                //状态为已打开 则将其关闭
                switchReport.setStatus((short) 0);
            } else {
                switchReport.setStatus((short) 1);
            }
            FacadeManager.getInstance().getReportFacade().updateReport(switchReport);
            return reportId;
        }
        return null;
    }

    @Override
    public Report queryReportById(String tenantId, String reportId) {
        return FacadeManager.getInstance().getReportFacade().getReportById(new Report(reportId, tenantId));
    }

    @Override
    public Report queryReport(String reportId, String reportDataId, String tenantId,
                              String sortField, String sortOrder) {
        Report report =
                FacadeManager.getInstance().getReportFacade().getReportById(new Report(reportId, tenantId));
        if (report != null) {
            //查询报表日历历史数据 时间降序
            List<ReportData> dataList =
                    FacadeManager.getInstance().getReportFacade().getReportDataByReportId(reportId);
            List<TReportData> tDataList = new ArrayList<>();
            String lastDateReportDateId = null;
            if (dataList != null && dataList.size() > 0) {
                for (ReportData r : dataList) {
                    if (lastDateReportDateId == null) {
                        //取最近一条日报id
                        lastDateReportDateId = r.getReportDataId();
                    }
                    TReportData tData = new TReportData();
                    tData.setReportDataId(r.getReportDataId());
                    tData.setStartDate(DateUtil.formatDate(r.getStartDate(), DateUtil.FORMAT_TO_D));
                    tData.setEndDate(DateUtil.formatDate(r.getEndDate(), DateUtil.FORMAT_TO_D));
                    tDataList.add(tData);
                }
            }
            report.setCalendar(tDataList);
            if (StringUtils.isBlank(sortField)) {
                sortField = report.getSortField();
            }
            if (StringUtils.isBlank(sortOrder)) {
                sortOrder = report.getSortOrder();
            }
            if (StringUtils.isNotBlank(reportDataId)) {
                TReportDataAll allData = querySingleReportData(report, reportDataId, tenantId,
                        sortField, sortOrder);
                report.setData(setterResourcesValAvg(allData, sortField));
            } else {
                if (lastDateReportDateId != null) {
                    TReportDataAll allData = querySingleReportData(report, lastDateReportDateId,
                            tenantId, sortField, sortOrder);
                    report.setData(setterResourcesValAvg(allData, sortField));
                }
            }
        }
        return report;
    }

    private TReportDataAll querySingleReportData(Report report, String reportDataId, String tenantId,
                                                 String sortField, String sortOrder) {
        if (report == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("reportId", report.getReportId());
        map.put("tenantId", tenantId);
        map.put("sortField", sortField);
        map.put("sortOrder", sortOrder);
        map.put("reportDataId", reportDataId);
        return FacadeManager.getInstance().getReportFacade().getAllDataByConditions(map);
    }

    private TReportDataAll setterResourcesValAvg(TReportDataAll allData, String sortField) {
        if (allData == null || sortField == null) {
            return allData;
        }
        List<TReportResourceMetrics> resources = allData.getResources();
        if (resources != null && resources.size() > 0) {
            for (TReportResourceMetrics resource : resources) {
                List<TReportMetricData> dataList = resource.getMetrics();
                if (dataList != null && dataList.size() > 0) {
                    //进行一次指标名称排序
                    for (TReportMetricData metricData : dataList) {
                        if (sortField.equals(metricData.getMetricName())) {
                            resource.setValAvg(metricData.getValAvg());
                        }
                    }
                    Collections.sort(dataList);
                }
            }
        }
        return allData;
    }

    @Override
    public Map<String, Object> queryResourceMetricsList(List<String> resTags, List<String> metricsArr,
                                                        String tenantId, String sortField, String sortOrder,
                                                        String type, Long start, Long end) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Resource> resources = resourceService.queryTenantResByTags(tenantId, resTags, sortField, sortOrder);
        if (!resources.isEmpty()) {
            List<ResourceMetrics> results = metricService.queryPerfForEachResource(resources,
                    metricsArr, tenantId, sortField, sortOrder, type, start, end);
            if (results != null && results.size() > 0) {
                dataMap.put("list", results);
            }
        }
        return dataMap;
    }

    @Override
    public ReportGroup createReportGroup(ReportGroup group) {
        return FacadeManager.getInstance().getReportGroupFacade().createReportGroup(group);
    }

    @Override
    public ReportGroup updateReportGroup(ReportGroup group) {
        return FacadeManager.getInstance().getReportGroupFacade().updateReportGroup(group);
    }

    @Override
    public void deleteReportGroup(ReportGroup group) {
        FacadeManager.getInstance().getReportGroupFacade().deleteReportGroupOnLoic(group);
        deleteReportDataAllAsync(group);
    }

    //删除分组后异步清除报表历史数据
    private void deleteReportDataAllAsync (final ReportGroup group) {
        ExecutorThreadPool.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                Report report = new Report();
                report.setGroupId(group.getGroupId());
                report.setTenantId(group.getTenantId());
                List<Report> list = FacadeManager.getInstance().getReportFacade().getReportsByGroupId(report);
                if (list != null && list.size() > 0) {
                    for (Report r : list) {
                        deleteReport(group.getTenantId(), r.getReportId());
                    }
                }
            }
        });
    }

    @Override
    public List<ReportGroup> getAllReportGroups(String tenantId) {
        return FacadeManager.getInstance().getReportGroupFacade().getReportsByTenantId(tenantId);
    }

    @Override
    public List<ReportGroup> getHistoryReportGroups(String tenantId) {
        return FacadeManager.getInstance().getReportGroupFacade().getHistoryReportsByTenantId(tenantId);
    }

    @Override
    public int batchInsertDefaultGroups(List<ReportGroup> list) {
        return FacadeManager.getInstance().getReportGroupFacade().batchInsertGroups(list);
    }

    @Override
    public List<Report> getReportsByGroupId(String tenantId, String groupId) {
        Report report = new Report();
        if (StringUtils.isNotBlank(groupId)) {
            report.setGroupId(groupId);
        }
        report.setTenantId(tenantId);
        return FacadeManager.getInstance().getReportFacade().getReportsByGroupId(report);
    }

    @Override
    public void createReportDataAll(Report report) {
        reportDataStoreAsync(report);
    }

    /**
     * 同步生成，专为定时任务执行
     * @param report
     * @param isCheck 是否检查指标数据为空
     * @return
     */
    @Override
    public boolean createReportDataAllSync(Report report, boolean isCheck) {
        return reportDataStoreSync(report, isCheck);
    }

    // 异步任务，创建报表时调用
    public void reportDataStoreAsync(final Report t) {
        ExecutorThreadPool.getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                reportDataStoreSync(t, false);
            }
        });
    }

    /**
     * 查询dataStore模块数据并解析存储历史数据
     * @param t
     * @param isCheck 是否检查指标数据为空
     * @return true 存储成功
     *
     */
    private boolean reportDataStoreSync(Report t, boolean isCheck) {
        if (t == null) {
            return true;
        }
        List<String> resTags = t.getResTags();
        List<String> metricsArr = t.getMetrics();
        // type 取得数据类型daily昨日, weekly上周, monthly上月
        String type = String.valueOf(t.getReportType());
        String tenantId = t.getTenantId();
        String sortField = t.getSortField();
        String sortOrder = t.getSortOrder();
        ReportDataAll allData = new ReportDataAll();
        allData.setReportDataId(UUIDTypeHandler.createUUID());
        allData.setReportId(t.getReportId());
        allData.setCreateTime(new Date());
        switch (type) {
            case ReportConstant.REPORT_DAILY:
                // 获取昨天时间
                Date lastDate = DateUtil.getAnyDate(-1);
                allData.setStartDate(DateUtil.getMorning(lastDate));
                // 日报起止日期相同
                allData.setEndDate(allData.getStartDate());
                break;
            case ReportConstant.REPORT_WEEKLY:
                // 获取上周时间
                Date lastWeekDate = DateUtil.getAnyDate(-7);
                allData.setStartDate(DateUtil.getFirstDayofWeek(lastWeekDate));
                allData.setEndDate(DateUtil.getLastDayofWeek(lastWeekDate));
                break;
            case ReportConstant.REPORT_MONTHLY:
                Calendar c = Calendar.getInstance();
                // 获取上月时间
                int lastMonth = c.get(Calendar.MONTH) - 1;
                int year = c.get(Calendar.YEAR);
                allData.setStartDate(DateUtil.getFirstDayofMonth(lastMonth, year));
                allData.setEndDate(DateUtil.getLastDayofMonth(lastMonth, year));
                break;
            default:
                break;
        }
        synchronized (this) {
            // 查询 startDate 是否已生成过
            // 锁住这段查询保证不再重复
            ReportData dataDB =
                    FacadeManager.getInstance().getReportFacade().getReportDataByDate(allData);
            if (dataDB != null) {
                log.info("report data already stored==>" + t.getReportId());
                return true;
            }
        }
        List<Resource> resources = resourceService.queryTenantResByTags(tenantId, resTags, sortField, sortOrder);
        if (!resources.isEmpty()) {
            List<ResourceMetrics> results = metricService.queryPerfForEachResource(resources,
                    metricsArr, tenantId, sortField, sortOrder, type, null, null);
            // 自动生成报表数据时检查查询的指标数据
            if (isCheck && !checkMetricValNotNull(results)) {
                return false;
            }
            List<ReportResourceMetrics> resourceMetrics = new ArrayList<>();
            for (ResourceMetrics oneResource : results) {
                ReportResourceMetrics rrM = new ReportResourceMetrics();
                rrM.setHostname(oneResource.getHostname());
                rrM.setIpaddr(oneResource.getIpaddr());
                rrM.setResourceId(oneResource.getId());
                rrM.setReportDataId(allData.getReportDataId());
                rrM.setReportId(t.getReportId());
                List<ResourceMetrics.MetricVal> metricValList = oneResource.getMetrics();
                if (metricValList != null && metricValList.size() > 0) {
                    List<ReportMetricData> metricDataList = new ArrayList<>();
                    for (ResourceMetrics.MetricVal metricVal : metricValList) {
                        ReportMetricData rrD = new ReportMetricData();
                        rrD.setReportDataId(allData.getReportDataId());
                        rrD.setResourceId(oneResource.getId());
                        rrD.setMetricName(metricVal.getMetricName());
                        rrD.setUnit(metricVal.getUnit());
                        rrD.setPoints(covertPointsToStr(metricVal.getPoints()));
                        rrD.setValAvg(countPointsAvgVal(metricVal.getPoints()));
                        rrD.setReportId(t.getReportId());
                        metricDataList.add(rrD);
                    }
                    rrM.setMetricDataList(metricDataList);
                }
                resourceMetrics.add(rrM);
            }
            try {
                allData.setResources(resourceMetrics);
                ReportDataAll a = FacadeManager.getInstance().getReportFacade().createReportDataAll(allData);
                return a != null;
            } catch (Exception e) {
                log.error("report data store error: {} , reportId: " + t.getReportId() + " will try it again later..", e);
                return false;
            }
        } else {
            // 防止 DataStore 模块查询超时 or 网络问题
            // 返回 false 由集群其他服务器再尝试一次
            return false;
        }
    }

    /**
     * 自动生成报表数据时检查查询的指标数据
     * metricVal == null ? false : true
     * 防止查询超时,没有数据则生成失败等待重试;
     * @return
     */
    private boolean checkMetricValNotNull( List<ResourceMetrics> results) {
        if (results == null || results.size() == 0) {
            // 防止 DataStore 模块查询超时or网络问题
            // 返回 false 由重试进程重新尝试
            return false;
        }
        try {
            for (ResourceMetrics result : results) {
                if (result == null) {
                    continue;
                }
                for (ResourceMetrics.MetricVal metricVal : result.getMetrics()) {
                    if (metricVal != null) {
                        double[][] points = metricVal.getPoints();
                        Double valAvg = metricVal.getValAvg();
                        if (valAvg != null || (points != null && points.length > 0)) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("查询资源指标数据解析异常", e);
        }
        return false;
    }

    /**
     * [
     *    [144888450000, 144.22],
     *    [144488389900, 124.33]
     * ]
     * @param points
     * @return
     */
    private String covertPointsToStr(double[][] points) {
        if (points != null && points.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < points.length; i++) {
                if (i == 0) {
                    sb.append("[");
                }
                sb.append("[" );
                BigDecimal bd = new BigDecimal(points[i][0]);
                sb.append(bd.toString());
                sb.append(",");
                sb.append(points[i][1]);
                sb.append("]");
                if (i == points.length - 1) {
                    sb.append("]");
                } else {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
        return null;
    }

    @Override
    public String exportReport(String tenantId, Map<String, Object> params) {
        String reportDataId = params.get("reportDataId") == null ? null : params.get("reportDataId").toString();
        String reportId = params.get("reportId") == null ? null : params.get("reportId").toString();
        byte[] bytes;
        if (StringUtils.isNotBlank(reportDataId)) {
            //history data
            bytes = exportReportHistory(tenantId, params);
        } else {
            bytes = exportReportNow(tenantId, params);
        }
        if (bytes != null && bytes.length > 0) {
            String uuid;
            if (StringUtils.isBlank(reportId)) {
                uuid = UUIDTypeHandler.createUUID();
            } else {
                uuid = "reportId:" + reportId;
            }
            redisDao.hset(ReportConstant.REDIS_FILE_KEY.getBytes(), uuid.getBytes(), bytes);
            return uuid;
        }
        return null;
    }

    /**
     * 导出历史数据
     * @param tenantId
     * @param params
     */
    private byte[] exportReportHistory(String tenantId, Map<String, Object> params) {
        String reportId = String.valueOf(params.get("reportId"));
        String reportDataId = String.valueOf(params.get("reportDataId"));
        String file = String.valueOf(params.get("file"));
        if (StringUtils.isBlank(reportDataId) || StringUtils.isBlank(reportId)) {
            throw new IllegalArgumentException("The export report id cannot be empty");
        }
        Report r = queryReportById(tenantId, reportId);
        if (r != null) {
            Report allR = queryReport(reportId, reportDataId, tenantId,
                    r.getSortField(), r.getSortOrder());
            if (allR != null) {
                return ReportExcelUtils.expReportExs(allR, file);
            }
        } else {
            throw new IllegalArgumentException("No corresponding report data is found, please confirm whether the parameter is incorrect");
        }
        return null;
    }

    /**
     * 导出实时数据
     * @param tenantId
     * @param params
     */
    private byte[] exportReportNow(String tenantId, Map<String, Object> params) {
        List<String> resTags = (ArrayList<String>) params.get("resTags");
        List<String> metricsArr = (ArrayList<String>) params.get("metrics");
        String sortField = params.get("sortField") == null ? null : params.get("sortField").toString();
        String sortOrder = params.get("sortOrder") == null ? null : params.get("sortOrder").toString();
        String file = params.get("file") == null ? null : params.get("file").toString();
        Long start = (Long) params.get("start");
        Long end = (Long) params.get("end");
        //type 取得数据类型daily昨日, weekly上周, monthly上月
        String type = String.valueOf(params.get("type"));
        Map<String, Object> dataMap = queryResourceMetricsList(resTags, metricsArr, tenantId,
                sortField, sortOrder, type, start, end);
        if (dataMap != null) {
            List<ResourceMetrics> list = (ArrayList<ResourceMetrics>) dataMap.get("list");
            if (list != null && list.size() > 0) {
                Date startDate = null;
                Date endDate = null;
                // 前端传的参数为报表类型而不是时间戳
                if (type != null && start == null && end == null) {
                    switch (type) {
                        case ReportConstant.REPORT_DAILY:
                            // 获取昨天时间
                            startDate = DateUtil.getAnyDate(-1);
                            endDate = startDate;
                            break;
                        case ReportConstant.REPORT_WEEKLY:
                            // 获取上周时间
                            Date lastWeekDate = DateUtil.getAnyDate(-7);
                            startDate = DateUtil.getFirstDayofWeek(lastWeekDate);
                            endDate = DateUtil.getLastDayofWeek(lastWeekDate);
                            break;
                        case ReportConstant.REPORT_MONTHLY:
                            Calendar c = Calendar.getInstance();
                            // 获取上月时间
                            int lastMonth = c.get(Calendar.MONTH) - 1;
                            int year = c.get(Calendar.YEAR);
                            startDate = DateUtil.getFirstDayofMonth(lastMonth, year);
                            endDate = DateUtil.getLastDayofMonth(lastMonth, year);
                            break;
                        default:
                            break;
                    }
                } else if (start != null && end != null) {
                    startDate = new Date(start);
                    endDate = new Date(end);
                }
                return ReportExcelUtils.expDataExsNow(list, file,
                        DateUtil.formatDate(startDate, DateUtil.FORMAT_TO_D),
                        DateUtil.formatDate(endDate, DateUtil.FORMAT_TO_D));
            }
        }
        return null;
    }

    /**
     * 计算一段时序数据的均值
     * 保留两位小数
     * @param points
     * @return
     */
    private Double countPointsAvgVal(double[][] points) {
        if (points != null && points.length > 0) {
            double sum = 0.0;
            for (int i = 0; i < points.length; i++) {
                sum += points[i][1];
            }
            double avg = sum/points.length;
            BigDecimal bg = new BigDecimal(avg);
            return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return null;
    }

    @Override
    public byte[] getReportFile(String file) {
        byte[] bytes = redisDao.hget(ReportConstant.REDIS_FILE_KEY.getBytes(), file.getBytes());
        if (bytes != null) {
            redisDao.hdel(ReportConstant.REDIS_FILE_KEY.getBytes(), file.getBytes());
        }
        return bytes;
    }

    @Override
    public void removeAllInvalidFiles() {
        Set<byte[]> keys = redisDao.hkeys(ReportConstant.REDIS_FILE_KEY.getBytes());
        if (keys != null && keys.size() > 0) {
            for (byte[] key : keys) {
                if (key != null) {
                    redisDao.hdel(ReportConstant.REDIS_FILE_KEY.getBytes(), key);
                }
            }
        }
    }
}
