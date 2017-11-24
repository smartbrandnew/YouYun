package uyun.bat.gateway.agent.service.openapi;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import uyun.bat.common.config.Config;
import uyun.bat.common.constants.RestConstants;
import uyun.bat.datastore.api.entity.AggregatorType;
import uyun.bat.datastore.api.entity.DataPoint;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.QueryBuilder;
import uyun.bat.datastore.api.entity.QueryMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.entity.TimeUnit;
import uyun.bat.datastore.api.util.DateUtil;
import uyun.bat.gateway.agent.entity.showentity.HostAppCount;
import uyun.bat.gateway.agent.entity.showentity.HostHealth;
import uyun.bat.gateway.agent.entity.showentity.HostStoreUsage;
import uyun.bat.gateway.agent.entity.showentity.ShowList;
import uyun.bat.gateway.agent.entity.showentity.TopQuery;
import uyun.bat.gateway.agent.service.api.ShowService;
import uyun.bat.gateway.agent.util.IntegrationUtil;
import uyun.bat.gateway.api.service.ServiceManager;
import uyun.whale.common.util.number.RandomUtil;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;

@Service(protocol = "rest-agent", delay = 3000)
@Path("v2")
public class ShowRESTService implements ShowService {
    private DecimalFormat dcmFmt = new DecimalFormat("0.00");

    @GET
    @Path("hosts/health")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HostHealth getHostsHealth(@Context HttpServletRequest request) {
        String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);
        HostHealth hostHealth = new HostHealth();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        //获取全部的资源
        List<Resource> resourceList = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, true);
        List<String> servers = new ArrayList<>();
        List<String> networks = new ArrayList<>();
        //区别资源的设备类型
        for (Resource r : resourceList) {
            if (ResourceType.SERVER.equals(r.getType()))
                servers.add(r.getId());
            else networks.add(r.getId());
        }
        int serversAlert = 0;
        int networkAlert = 0;
        //获取错误的资源与上面的列表进行匹配
        Set<String> errorRecord = ServiceManager.getInstance().getOverviewService().queryResIdByErrorRecord(tenantId);
        for (String r : errorRecord) {
            if (servers.contains(r))
                serversAlert++;
            else networkAlert++;
        }
        String serverHealthRate = servers.size() == 0 ? "100.00%" : dcmFmt.format((servers.size() - serversAlert) / 1.0 / servers.size() * 100) + "%";
        String networkHealthRate = networks.size() == 0 ? "100.00%" : dcmFmt.format((networks.size() - networkAlert) / 1.0 / networks.size() * 100) + "%";
        map.put("serverHealthRate", serverHealthRate);
        map.put("networkHealthRate", networkHealthRate);
        list.add(map);
        hostHealth.setData(list);
        return hostHealth;
    }

    @GET
    @Path("hosts/apps_count/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShowList getHostsAppsCount(@Context HttpServletRequest request) {
        String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);
        ShowList hostAppCountList = new ShowList();
        //获取全部的资源
        List<Resource> resourceList = ServiceManager.getInstance().getResourceService().queryAllRes(tenantId, true);
        Set<String> store = new HashSet<>();
        Set<String> service = new HashSet<>();
        Set<String> database = new HashSet<>();
        Set<String> platform = new HashSet<>();
        Set<String> router = new HashSet<>();
        Set<String> switchs = new HashSet<>();
        //区别资源的设备类型
        for (Resource r : resourceList) {
            for (String app : r.getApps()) {
                String appType = IntegrationUtil.getAllMap().get(app);
                if (ResourceType.SERVER.equals(r.getType()) && IntegrationUtil.SERVICE.equals(appType))
                    service.add(r.getId());
                if (ResourceType.SERVER.equals(r.getType()) && IntegrationUtil.STORE.equals(appType))
                    store.add(r.getId());
                if (ResourceType.SERVER.equals(r.getType()) && IntegrationUtil.DATABASE.equals(appType))
                    database.add(r.getId());
                if (ResourceType.SERVER.equals(r.getType()) && IntegrationUtil.PLATFORM.equals(appType))
                    platform.add(r.getId());
            }
            if (ResourceType.NETWORK.equals(r.getType()) && r.getResTags().contains("equipment:Router"))
                router.add(r.getId());
            if (ResourceType.NETWORK.equals(r.getType()) && r.getResTags().contains("equipment:Switch"))
                switchs.add(r.getId());

        }
        int storeAlert = 0;
        int serviceAlert = 0;
        int databaseAlert = 0;
        int platformAlert = 0;
        int routerAlert = 0;
        int switchAlert = 0;
        //获取错误的资源与上面的列表进行匹配
        Set<String> errorRecord = ServiceManager.getInstance().getOverviewService().queryResIdByErrorRecord(tenantId);
        for (String r : errorRecord) {
            if (store.contains(r))
                storeAlert++;
            if (service.contains(r))
                serviceAlert++;
            if (database.contains(r))
                databaseAlert++;
            if (platform.contains(r))
                platformAlert++;
            if (router.contains(r))
                routerAlert++;
            if (switchs.contains(r))
                switchAlert++;
        }
        List<Object> list = new ArrayList<>();
        //如果是中文
        boolean isZH = Config.getInstance().isChinese();
        list.add(new HostAppCount(isZH?IntegrationUtil.STORE:IntegrationUtil.STORE_EN, storeAlert, store.size()));
        list.add(new HostAppCount(isZH?IntegrationUtil.SERVICE:IntegrationUtil.SERVICE_EN, serviceAlert, service.size()));
        list.add(new HostAppCount(isZH?IntegrationUtil.DATABASE:IntegrationUtil.DATABASE_EN, databaseAlert, database.size()));
        list.add(new HostAppCount(isZH?IntegrationUtil.PLATFORM:IntegrationUtil.PLATFORM_EN, platformAlert, platform.size()));
        list.add(new HostAppCount(isZH?IntegrationUtil.ROUTER:IntegrationUtil.ROUTER_EN, routerAlert, router.size()));
        list.add(new HostAppCount(isZH?IntegrationUtil.SWITCH:IntegrationUtil.SWITCH_EN, switchAlert, switchs.size()));
        hostAppCountList.setData(list);
        return hostAppCountList;
    }

    @GET
    @Path("hosts/store_usage/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HostHealth queryStoreUsage(@Context HttpServletRequest request, @QueryParam("ip") String ip, @QueryParam("app") String app) {
        String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);
        HostHealth hostHealth = new HostHealth();
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Double totalSpace = 0.0;
        Double usedSpace = 0.0;
        Double availableSpace = 0.0;
        Double usedRate = 0.0;
        //默认查ibm_ds
        usedSpace = getLastPerf(tenantId, "ibmds.storagepool.usagespace_allocated", ip);
        availableSpace = getLastPerf(tenantId, "ibmds.storagepool.usagespace_unallocated", ip);
        usedRate = getLastPerf(tenantId, "ibmds.storagepool.usagespace_usage", ip);
        //如果app查ibm_svc
        if (!StringUtils.isEmpty(app) && "ibm_svc".equals(app.toLowerCase())) {
            usedSpace = getLastPerf(tenantId, "ibmsvc.storepool.capacity_used", ip);
            availableSpace = getLastPerf(tenantId, "ibmsvc.storepool.capacity_unused", ip);
            usedRate = getLastPerf(tenantId, "ibmsvc.storepool.capacity_usage", ip);
        }
        //如果上面两个都是null则模拟数据
        if (null == usedSpace || null == availableSpace) {
            usedSpace = Double.valueOf(dcmFmt.format(RandomUtil.rand(18, 21) / 1.01));//18~21T随机
            availableSpace = Double.valueOf(dcmFmt.format(RandomUtil.rand(3, 5) / 1.01));//3-5T随机
            usedRate = Double.valueOf(dcmFmt.format(usedSpace / (usedSpace + availableSpace)));
        }
        totalSpace = Double.valueOf(dcmFmt.format(usedSpace + availableSpace));

        map.put("totalSpace", totalSpace);
        map.put("usedSpace", usedSpace);
        map.put("availableSpace", availableSpace);
        map.put("usedRate", usedRate);
        map.put("unit", "T");
        list.add(map);
        hostHealth.setData(list);
        return hostHealth;
    }

    private Double getLastPerf(String tenantId, String metricName, String ip) {
        QueryBuilder builder = QueryBuilder.getInstance();
        QueryMetric metric = builder.addMetric(metricName).addAggregatorType(AggregatorType.last).addTenantId(tenantId);
        if (!StringUtils.isEmpty(ip))
            metric.addTag("ip", ip);
        //最近一个小时最后一个点
        builder.setStart(1, TimeUnit.HOURS);
        PerfMetric perfMetric = ServiceManager.getInstance().getMetricService().queryCurrentPerfMetric(builder);
        if (perfMetric != null) {
            MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metricName);
            List<DataPoint> dataPoints = perfMetric.getDataPoints();
            if (dataPoints != null && dataPoints.size() > 0) {
                DataPoint dataPoint = dataPoints.get(dataPoints.size() - 1);
                return (Double) dataPoint.getValue();
            }
        }
        return null;
    }

    @GET
    @Path("hosts/top/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShowList queryTopN(@Context HttpServletRequest request, @QueryParam("metric") String metric, @QueryParam("group") String group) {
        String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);
        List<Object> list = new ArrayList<>();
        QueryBuilder builder = QueryBuilder.getInstance();
        if (StringUtils.isEmpty(metric))
            metric = "system.net.ping_response_time";
        if (StringUtils.isEmpty(group))
            group = "ip";
        builder.addMetric(metric).addAggregatorType(AggregatorType.last).addTenantId(tenantId).addGrouper(group);
        //最近一个小时最后一个点
        builder.setStart(1, TimeUnit.HOURS);
        List<PerfMetric> perfMetric = ServiceManager.getInstance().getMetricService().queryTopN(builder, 10);
        //如果查询为空
        if(perfMetric.size() == 0){
            TopQuery top = new TopQuery("",0d);
            list.add(top);
        }
        for (PerfMetric p : perfMetric) {
            TopQuery top = new TopQuery(p.getTags().get(group).get(0), (Double) p.getDataPoints().get(0).getValue());
            list.add(top);
        }

        ShowList showTopN = new ShowList(list);
        return showTopN;
    }

    //模拟数据
    private List<Double> TEMP_VALUE = new ArrayList<Double>() {{
        add(59.41);
        add(65.35);
        add(69.31);
        add(75.25);
        add(67.44);
        add(72.38);
        add(73.55);
    }};
    //获取第一次访问接口的0点时间
    private static long FIRSTTIME =  System.currentTimeMillis()/(1000*3600*24)*(1000*3600*24)-TimeZone.getDefault().getRawOffset();

    @GET
    @Path("hosts/usage/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ShowList queryHostStoreUsage(@Context HttpServletRequest request, @QueryParam("ip") String ip, @QueryParam("metric") String metric) {
        String tenantId = (String) request.getAttribute(RestConstants.TENANT_ID);
        ShowList hostStoreUsage = new ShowList();
        List<Object> list = new ArrayList<>();
        Double usedRate = 0.0;

        //默认查ibmds,如果有输入metric且是ibmsvc查ibmsvc的存储使用率
        if (StringUtils.isEmpty(metric))
            metric = "ibmsvc.storepool.capacity_usage";
        if ("ibmds.storagepool.usagespace_usage".equals(metric.toLowerCase()))
            metric = "ibmds.storagepool.usagespace_usage";

        QueryBuilder builder = QueryBuilder.getInstance();
        QueryMetric metricName = builder.addMetric(metric).addAggregatorType(AggregatorType.last).addTenantId(tenantId);
        if (!StringUtils.isEmpty(ip))
            metricName.addTag("ip", ip);
        //最近7天 每天一个点interval=86400
        builder.setStart(7, TimeUnit.DAYS);
        List<PerfMetric> perfMetrics = ServiceManager.getInstance().getMetricService().querySeries(builder, 86400);
        for (PerfMetric p : perfMetrics) {
            if (p != null) {
                List<DataPoint> dataPoints = p.getDataPoints();
                if (dataPoints != null && dataPoints.size() > 0) {
                    for (DataPoint d : dataPoints) {
                        String time = DateUtil.formatDate(new Date(d.getTimestamp()), "MM-dd");
                        list.add(new HostStoreUsage(time, (Double) d.getValue(), "%"));
                    }
                }
            }
        }
        //如果perfMetrics为0则模拟数据
        if (perfMetrics.size() == 0) {
            //1day
            long day = 24 * 60 * 60 * 1000L;
            long current = System.currentTimeMillis();
            long interval = (current - FIRSTTIME) / day;
            //如果访问间隔时间超过1d则移除第一个，末尾随机增加一个，替换初始时间
            if (interval >= 1) {
                TEMP_VALUE.remove(0);
                TEMP_VALUE.add(Double.valueOf(dcmFmt.format(RandomUtil.rand(70, 90) / 1.01)));
                FIRSTTIME = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
            }
            for (int i = 0; i < TEMP_VALUE.size(); i++) {
                list.add(new HostStoreUsage(DateUtil.formatDate(new Date(System.currentTimeMillis() - (TEMP_VALUE.size() - i - 1) * day), "MM-dd"), TEMP_VALUE.get(i), "%"));
            }

        }
        hostStoreUsage.setData(list);
        return hostStoreUsage;
    }
}
