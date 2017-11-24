package uyun.bat.event.impl.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.common.config.Config;
import uyun.bat.common.selfmonitor.HTTPClientUtils;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.event.api.entity.AlertData;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.impl.common.ServiceManager;
import uyun.bat.event.impl.util.JsonUtil;
import uyun.bat.event.impl.util.LanguageUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bird.tenant.api.entity.Tenant;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AlertMQListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(AlertMQListener.class);
    private static String ALERT_URL = (String) Config.getInstance().get("alert.openapi");

    @Override
    public void onMessage(Message message) {
        try {
            if (!(message instanceof ObjectMessage)) {
                return;
            }
            Event event = (Event) ((ObjectMessage) message).getObject();
            // push data to Alert
            if (alertEnable()) {
                AlertData alertData = generateAlertData(event);
                if (alertData != null) {
                    Map<String, String> map = new HashMap<>();
                    map.put("body", JsonUtil.encode(alertData));
                    String tenantId = event.getTenantId();
//                    String apiKey = "e10adc3949ba59abbe56e057f2gg88dd";
                    String apiKey = "";
                    if (tenantId != null || (!tenantId.isEmpty())) {
                        try {
                            Tenant t = ServiceManager.getInstance().getTenantService().view(tenantId);
                            if (t != null) {
                                apiKey = t.getApiKeys() != null && t.getApiKeys().size() > 0 ? t.getApiKeys().get(0).getKey() : "";
                            }
                        } catch (Exception e) {
                            logger.warn("Fail to gain tenant apikey:{}", e.getMessage());
                        }
                    }
                    if (apiKey != null || !apiKey.isEmpty()) {
                        String url = ALERT_URL.replace("APIKEY", apiKey);
                        String payload = JsonUtil.encode(Collections.singletonList(map));
                        HTTPClientUtils.post(url, payload);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn("jms message exception：" + e.getMessage());
            if (logger.isDebugEnabled())
                logger.info("Stack：", e);
        }
    }

    private AlertData generateAlertData(Event event) {
        String resId = event.getResId();
        // 只处理带resId字段的资源
        if (resId == null || resId.isEmpty()) {
            return null;
        } else {
            String tenantId = event.getTenantId();
            Resource resource = ServiceManager.getInstance().getResourceService().queryResById(resId, tenantId);
            String entityName = resource.getHostname();
            String entityIp = resource.getIpaddr();
            String description = event.getMsgContent();
            Date occurTime = event.getOccurTime();
            Short serverity = event.getServerity();
            Short sourceType = event.getSourceType();
            String msgTitle = event.getMsgTitle();
            String monitorType = event.getMonitorType();
            String monitorId = event.getMonitorId();
            String alertName = null;
            // 事件是由监测器触发的
            if (monitorId != null && !monitorId.isEmpty()) {
                Monitor monitor = ServiceManager.getInstance().getMonitorService().getMonitorById(tenantId, monitorId);
                alertName = ServiceManager.getInstance().getMonitorService().getAlertNameByMonitorType(monitorType, monitor);
            } else if (sourceType == EventSourceType.OPEN_API.getKey()) {
                // openapi触发的事件
                alertName = msgTitle;
            } else {
                // 暂时认为自身产生的带resID的事件都是资源上下线事件
                if (LanguageUtil.isEnglish()) {
                    alertName = "host.up/down";
                } else if (LanguageUtil.isChinese()) {
                    alertName = "host.up/down-主机上下线";
                }
            }
            if (alertName == null || alertName.isEmpty()) {
                return null;
            } else {
                // properties 字段暂时设为空
                return new AlertData(alertName, serverity, description, occurTime, entityName, entityIp, Collections.emptyList());
            }

        }
    }
    private boolean alertEnable() {
        return Config.getInstance().get("alert.push.mode", false);
    }
}
