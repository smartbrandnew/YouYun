package uyun.bat.web.impl.service.rest.report;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;

import uyun.bat.common.config.Config;
import uyun.bat.common.utils.StringUtils;
import uyun.bat.datastore.api.exception.Illegalargumentexception;
import uyun.bat.report.api.entity.Report;
import uyun.bat.report.api.entity.ReportConstant;
import uyun.bat.report.api.entity.ReportGroup;
import uyun.bat.report.api.entity.web.TReportDataAll;
import uyun.bat.report.api.entity.web.TReportResourceMetrics;
import uyun.bat.report.api.service.ReportService;
import uyun.bat.web.api.report.entity.SimpleReport;
import uyun.bat.web.api.report.entity.SimpleReportGroup;
import uyun.bat.web.api.report.service.ReportWebService;
import uyun.bat.web.impl.common.entity.TenantConstants;
import uyun.bat.web.impl.common.service.ServiceManager;
 
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

/**
 * Created by lilm on 17-2-28.
 */
@Service(protocol = "rest")
@Path("v2/report")
public class ReportRESTService implements ReportWebService {

    private static ReportService reportService;
    private static boolean isZH = Config.getInstance().isChinese();
    static {
        reportService = ServiceManager.getInstance().getReportService();
    }

    @POST
    @Path("create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Report createReport(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, Report report) {
         
        report.setTenantId(tenantId);
        report.setModified(new Date());
        report.setReportId(UUIDTypeHandler.createUUID());
        report.setStatus((short) 1);
        Report r = reportService.createReport(report);
        if (r != null) {
            reportService.createReportDataAll(report);
        }
        return r;
    }

    @GET
    @Path("delete")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteReport(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                             @QueryParam("reportId") String reportId) {
        reportService.deleteReport(tenantId, reportId);
    }

    @GET
    @Path("query")
    @Produces(MediaType.APPLICATION_JSON)
    public Report queryReport(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                              @QueryParam("reportId") String reportId, @QueryParam("reportDataId") String reportDataId,
                              @QueryParam("sortField") String sortField, @QueryParam("sortOrder") String sortOrder,
                              @QueryParam("pageSize") Integer pageSize, @QueryParam("current") Integer current) {
        Report r = reportService.queryReport(reportId, reportDataId, tenantId, sortField, sortOrder);
        return reportDataByPage(r, pageSize, current);
    }

    //手动分页 由于数据库分页不适合
    private Report reportDataByPage(Report r, Integer pageSize, Integer current) {
        if (r == null || pageSize == null || current == null) {
            return r;
        }
        if (pageSize < 1 || current < 1) {
            return new Report();
        }
        TReportDataAll dataAll = r.getData();
        if (dataAll == null) {
            return r;
        }
        dataAll.setPageSize(pageSize);
        List<TReportResourceMetrics> resources = dataAll.getResources();
        if (resources != null && resources.size() > 0) {
            dataAll.setTotalCount(resources.size());
            int totalPage = (resources.size()  +  pageSize  - 1) / pageSize;
            if (current > totalPage) {
                current = totalPage;
            }
            dataAll.setCurrentPage(current);
            if (resources.size() > pageSize) {
                int begin = (current - 1) * pageSize;
                int end = begin + pageSize;
                if (end >= resources.size()) {
                    end = resources.size();
                }
                dataAll.setResources(resources.subList(begin, end));
            }
        } else {
            dataAll.setTotalCount(0);
        }
        return r;
    }

    @GET
    @Path("switch")
    @Produces(MediaType.APPLICATION_JSON)
    public void reportSwitch(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                             @QueryParam("reportId") String reportId) {
        reportService.reportSwitch(tenantId, reportId);
    }

    @GET
    @Path("query/list")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SimpleReport> queryReportsByGroupId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                                                    @QueryParam("groupId") String groupId) {
         
        String regex = "^[a-z0-9A-Z\u4e00-\u9fa5]+$";
        if (StringUtils.isNotBlank(groupId) && !groupId.matches(regex))
            throw new Illegalargumentexception("groupId is not correct！");
        List<Report> list = reportService.getReportsByGroupId(tenantId, groupId);
        List<SimpleReport> spList = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (Report r : list) {
                if (r == null) {
                    continue;
                }
                SimpleReport sp = new SimpleReport(r.getReportId(), r.getReportType(),
                        r.getReportName(), r.getDiagramType(), r.getStatus(), r.getGroupId());
                spList.add(sp);
            }
        }
        return spList;
    }

    @POST
    @Path("group/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ReportGroup createReportGroup(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                                         ReportGroup group) {
         
        group.setTenantId(tenantId);
        group.setModified(new Date());
        if (StringUtils.isBlank(group.getParentId())) {
            //防止插入空字符
            group.setParentId(null);
        }
        group.setGroupId(UUIDTypeHandler.createUUID());
        group.setStatus((short)1);
        return reportService.createReportGroup(group);
    }

    @POST
    @Path("group/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ReportGroup updateReportGroup(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                                         ReportGroup group) {
         
        group.setModified(new Date());
        return reportService.updateReportGroup(group);
    }

    @GET
    @Path("group/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SimpleReportGroup> getAllGroupsByTenantId(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId) {
         
        boolean flag = generateDefaultGroups(tenantId);
        if (flag) {
            List<ReportGroup> groups = reportService.getAllReportGroups(tenantId);
            List<SimpleReportGroup> list = new ArrayList<>();
            if (groups != null && groups.size() > 0) {
                for (ReportGroup group : groups) {
                    SimpleReportGroup sg = new SimpleReportGroup(group.getGroupId(), group.getGroupName());
                    list.add(sg);
                }
            }
            return list;
        }
        return null;
    }

    @POST
    @Path("group/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    public void deleteReportGroup(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId, ReportGroup group) {
         
        group.setTenantId(tenantId);
        reportService.deleteReportGroup(group);
    }

    /**
     * 初次操作生成默认分组
     * 查询历史记录若为空则生成
     * @param tenantId
     * @return
     */
    private boolean generateDefaultGroups(String tenantId) {
        List<ReportGroup> groups =
                reportService.getHistoryReportGroups(tenantId);
        if (groups == null || groups.size() == 0) {
            String[] defaultNames;
            if (isZH)
                defaultNames=new String[]{"操作系统报表", "中间件报表", "数据库报表"};
            else
            	defaultNames=new String[]{"System Report", "Middleware Report", "Database Report"};
        	List<ReportGroup> defaultGroups = new ArrayList<>();
            for (String defaultName : defaultNames) {
                ReportGroup group = new ReportGroup();
                group.setGroupName(defaultName);
                group.setTenantId(tenantId);
                group.setGroupId(UUIDTypeHandler.createUUID());
                group.setModified(new Date());
                group.setStatus((short)1);
                defaultGroups.add(group);
            }
            //初次操作插入默认分组
            int flag = reportService.batchInsertDefaultGroups(defaultGroups);
            if (flag <= 0) {
                return false;
            }
        }
        return true;
    }

    @POST
    @Path("export")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map exportReport(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                            Map<String, Object> params){
        String uuid = reportService.exportReport(tenantId, params);
        Map map = new HashMap<>();
        map.put("file", uuid);
        return map;
    }

    @GET
    @Path("download")
    @Produces(MediaType.APPLICATION_JSON)
    public void downloadFile(@HeaderParam(TenantConstants.COOKIE_TENANT_ID) String tenantId,
                             @QueryParam("file") String file, @QueryParam("name") String name) {
        HttpServletResponse rs = (HttpServletResponse) RpcContext.getContext().getResponse();
        try {
            byte[] bytes = reportService.getReportFile(file);
            if (bytes == null) {
                return;
            }
            if (StringUtils.isBlank(name)) {
                name = "report";
            }
            Report r = null;
            if (file.contains(":")) {
                String reportId = file.split(":")[1];
                r = reportService.queryReportById(null, reportId);
            }
            if (r != null) {
                String reportType = r.getReportType();
                switch (reportType) {
                    case ReportConstant.REPORT_DAILY:
                        if (isZH)
                            name = "日报_" + name;
                        else
                            name = "daily_" +name;
                        break;
                    case ReportConstant.REPORT_WEEKLY:
                        if (isZH)
                            name = "周报_" + name;
                        else
                            name = "weekly_" +name;
                        break;
                    case ReportConstant.REPORT_MONTHLY:
                        if (isZH)
                            name = "月报_" + name;
                        else
                            name = "monthly_" +name;
                        break;
                    default:
                        break;
                }
            }
            rs.reset();
            String suffix = generateExcelTitleSuffix();
            rs.setContentType("multipart/form-data;charset=UTF-8"); //自动识别
            rs.setHeader("Content-Disposition","attachment;filename=" + URLEncoder.encode(name, "UTF-8") + suffix);
            //文件流输出到rs里
            rs.getOutputStream().write(bytes);
            rs.getOutputStream().flush();
            rs.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成excel文件名后缀
     * _03-06-10-10.xls
     * @return
     */
    private String generateExcelTitleSuffix() {
        StringBuilder sb = new StringBuilder();
        sb.append("_");
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd_HH-mm");
        String dateStr = sdf.format(new Date());
        sb.append(dateStr).append(".xls");
        return sb.toString();
    }

}
