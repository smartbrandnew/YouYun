package uyun.bat.monitor.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.common.config.Config;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.AppMonitorParam;
import uyun.bat.monitor.core.entity.MetricMonitorParam;
import uyun.bat.monitor.core.logic.AppMonitor;
import uyun.bat.monitor.core.logic.MetricMonitor;
import uyun.bat.monitor.impl.common.ServiceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 根据输入的msgTitle输出Alert中的AlertName
 */
public class AlertNameStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(AlertNameStrategy.class);
    private static boolean isZH = Config.getInstance().isChinese();
    private static final Map<MonitorType, Function<Monitor, String>> strategyMap = new HashMap<>();

    private AlertNameStrategy() {
        super();
        initMap();
    }

    private void initMap() {
        /**
         * 指标监测器处理函数 title: ${metric}监测 -> ${metric}-${metricCName}
         */
        Function<Monitor, String> metricMonitorToAlert = monitor -> {
            try {
                MetricMonitor metricMonitor = MonitorQueryUtil.generateMetricMonitor(monitor);
                if (metricMonitor != null) {
                    MetricMonitorParam metricMonitorParam = metricMonitor.getMetricMonitorParam();
                    String metric = metricMonitorParam.getMetric();
                    if (isZH) {
                        String cnName = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metric).getcName();
                        return String.join("-", metric, cnName);
                    } else {
                        return metric;
                    }
                }
            } catch (Exception e) {
                LOG.error("指标监测器转换失败:{}", e.getMessage());
            }
            return "";
        };

        /**
         * 主机监测器处理函数 title:资源离线监测 -> startsWith("资源")
         */
        Function<Monitor, String> hostMonitorToAlert = monitor -> {
            if (isZH) {
                return "host.up/down-主机上下线";
            } else  {
                return "host.up/down";
            }
        };

        /**
         * 应用监测器 title: -> endsWith("可用监测") mysql-mysql可用性监测
         */
        Function<Monitor, String> appMonitorToAlert = monitor -> {
            AppMonitor appMonitor = MonitorQueryUtil.generateAppMonitor(monitor);
            if (appMonitor != null) {
                AppMonitorParam appMonitorParam = appMonitor.getAppMonitorParam();
                String state = appMonitorParam.getState();
                int endIndex = state.indexOf('.');
                endIndex = endIndex == -1 ? state.length() : endIndex;
                String app = state.substring(0, endIndex);
                String desc = "";
                if (isZH) {
                    desc = "可用性监测";
                } else {
                    desc = "Availability Detection";
                }
                return String.join("-", state, app + desc);
            }
            return "";
        };

        /**
         * 事件监测器处理函数 -- 目前不上报
         */
        Function<Monitor, String> eventMonitorToAlert = monitor -> "";

        strategyMap.put(MonitorType.APP, appMonitorToAlert);
        strategyMap.put(MonitorType.EVENT, eventMonitorToAlert);
        strategyMap.put(MonitorType.METRIC, metricMonitorToAlert);
        strategyMap.put(MonitorType.HOST, hostMonitorToAlert);
    }


    public static AlertNameStrategy getInstance() {
        return new AlertNameStrategy();
    }

    public Function<Monitor, String> getFuncByMonitorType(MonitorType monitorType) {
        Function<Monitor, String> function = strategyMap.get(monitorType);
        if (function != null) {
            return function;
        }
        return null;
    }
}
