package uyun.bat.monitor.impl.facade;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.api.entity.EventTag;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.core.entity.ResourceData;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

/**
 * 生成事件监测器变更对应的事件
 */
public abstract class MonitorEventCreator {
	private static final Logger logger = LoggerFactory.getLogger(MonitorEventCreator.class);

	private static boolean isZH = Config.getInstance().isChinese();
	private static final int interval = Config.getInstance().get("resource.offline.interval", 5);
	private static final String EVENT_MONITOR_CONTENT_ZH = "监测器触发条件 : %s";

	private static final String EVENT_MONITOR_CREATE_TITLE_ZH = "创建 %s 监测器: %s";
	private static final String EVENT_MONITOR_CREATE_TITLE_EN = "Create %s monitor: %s";
	private static final String EVENT_MONITOR_CREATE_CONTENT_EN = "Monitor trigger condition: %s";
	
	private static final String EVENT_MONITOR_UPDATE_TITLE_ZH = "编辑 %s 监测器: %s";
	private static final String EVENT_MONITOR_UPDATE_CONTENT_ZH = "现在监测器触发条件 : %s . 修改前监测器触发条件 : %s";
	private static final String EVENT_MONITOR_UPDATE_TITLE_EN = "Edit %s monitor: %s";
	private static final String EVENT_MONITOR_UPDATE_CONTENT_EN = "Current monitor trigger condition: %s . previous: %s";
	

	private static final String EVENT_MONITOR_SILENCE_TITLE_ZH = "%s 监测器: %s";
	private static final String EVENT_MONITOR_SILENCE_ENABLE_CONTENT_ZH = "已静默";
	private static final String EVENT_MONITOR_SILENCE_DISABLE_CONTENT_ZH = "已取消静默";
	private static final String EVENT_MONITOR_SILENCE_ENABLE_CONTENT_EN = "Monitor has been Silenced";
	private static final String EVENT_MONITOR_SILENCE_DISABLE_CONTENT_EN = "Monitor become noisy";
	

	private static final String EVENT_MONITOR_DELETE_TITLE_ZH = "删除 %s 监测器: %s";
	private static final String EVENT_MONITOR_DELETE_TITLE_EN = "Delete %s monitor: %s";
	private static final String EVENT_MONITOR_DELETE_CONTENT_EN = "Monitor trigger condition : %s";
	
	private static final String EVENT_RESOURCE_ONLINE_TITLE_ZH = "资源上线";
	private static final String EVENT_RESOURCE_ONLINE_CONTENT_ZH = "开始接收到该资源相关的数据";
	private static final String EVENT_RESOURCE_ONLINE_TITLE_EN = "Resource Online";
	private static final String EVENT_RESOURCE_ONLINE_CONTENT_EN = "Start receiving data related to this resource";
	
	private static final String EVENT_RESOURCE_OFFLINE_TITLE_ZH = "资源离线";
	private static final String EVENT_RESOURCE_OFFLINE_CONTENT_ZH = "超过 %s 分钟未接收到该资源相关的数据";
	private static final String EVENT_RESOURCE_OFFLINE_TITLE_EN = "Resource Offline";
	private static final String EVENT_RESOURCE_OFFLINE_CONTENT_EN = "There is no data received related to this resource Over %s minutes";
	
	public static void onMonitorCreate(Monitor monitor){
		String title;
		String content;
		if(isZH){
			title = String.format(EVENT_MONITOR_CREATE_TITLE_ZH,monitor.getMonitorType().getName(),monitor.getName());
			content = String.format(EVENT_MONITOR_CONTENT_ZH,MonitorQueryUtil.generateQuery(monitor));
		}else{
			title = String.format(EVENT_MONITOR_CREATE_TITLE_EN,monitor.getMonitorType().getName(),monitor.getName());
			content = String.format(EVENT_MONITOR_CREATE_CONTENT_EN,MonitorQueryUtil.generateQuery(monitor));
		}
		triggeredEvent(title, content, monitor.getTenantId());
	}
	
	public static void onTriggerUpdate(Monitor old, Monitor newData){
		String query = MonitorQueryUtil.generateQuery(newData);
		String title;
		String content;
		if (isZH) {
			title = String.format(EVENT_MONITOR_UPDATE_TITLE_ZH, old.getMonitorType().getName(), old.getName());
			content = String.format(EVENT_MONITOR_UPDATE_CONTENT_ZH, query, MonitorQueryUtil.generateQuery(old));
		} else {
			title = String.format(EVENT_MONITOR_UPDATE_TITLE_EN, old.getMonitorType().getName(), old.getName());
			content = String.format(EVENT_MONITOR_UPDATE_CONTENT_EN, query, MonitorQueryUtil.generateQuery(old));
		}
		triggeredEvent(title, content, old.getTenantId());
	}
	
	public static void onMonitorSilence(Monitor old, Monitor newData){
		String title;
		String content;
		if (isZH) {
			title = String.format(EVENT_MONITOR_SILENCE_TITLE_ZH, old.getMonitorType().getName(), old.getName());
			if (newData.getEnable() != null && !newData.getEnable())
				content = EVENT_MONITOR_SILENCE_ENABLE_CONTENT_ZH;
			else
				content = EVENT_MONITOR_SILENCE_DISABLE_CONTENT_ZH;
		} else {
			title = String.format(EVENT_MONITOR_UPDATE_TITLE_EN, old.getMonitorType().getName(), old.getName());
			if (newData.getEnable() != null && !newData.getEnable())
				content = EVENT_MONITOR_SILENCE_ENABLE_CONTENT_EN;
			else
				content = String.format(EVENT_MONITOR_SILENCE_DISABLE_CONTENT_EN);
		}
		triggeredEvent(title, content, newData.getTenantId());
	}
	
	public static void onMonitorDelete(Monitor old){
		String title;
		String content;
		if (isZH) {
			title = String.format(EVENT_MONITOR_DELETE_TITLE_ZH, old.getMonitorType().getName(), old.getName());
			content = String.format(EVENT_MONITOR_CONTENT_ZH, MonitorQueryUtil.generateQuery(old));
		} else {
			title = String.format(EVENT_MONITOR_DELETE_TITLE_EN, old.getMonitorType().getName(), old.getName());
			content = String.format(EVENT_MONITOR_DELETE_CONTENT_EN, MonitorQueryUtil.generateQuery(old));
		}
		triggeredEvent(title, content, old.getTenantId());
	}

	private static void triggeredEvent(String title, String content, String tenantId) {
		Event event = new Event();
		event.setId(UUIDTypeHandler.createUUID());
		event.setOccurTime(new Date());
		event.setServerity(EventServerityType.INFO.getKey());
		event.setMsgTitle(title);
		event.setMsgContent(content);
		event.setTenantId(tenantId);
		event.setIdentity(UUIDTypeHandler.createUUID());
		event.setSourceType(EventSourceType.MONITOR.getKey());
		// 事件标签未设置
		try {
			ServiceManager.getInstance().getEventService().create(event);
		} catch (Throwable e) {
			if (logger.isWarnEnabled()) {
				logger.warn(e.getMessage());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Stack:", e);
			}
		}
	}
	
	public static void onResourceOnline(ResourceData resource){
		String title;
        String content;
        EventServerityType type;
        Resource res=ServiceManager.getInstance()
				.getResourceService().queryResById(resource.getResourceId(), resource.getTenantId());
		if (null == res) {
			return;
		}
		if (resource.getOnlineStatus().equals(OnlineStatus.ONLINE)) {
			if (isZH) {
				title = EVENT_RESOURCE_ONLINE_TITLE_ZH;
				content = EVENT_RESOURCE_ONLINE_CONTENT_ZH;
			} else {
				title = EVENT_RESOURCE_ONLINE_TITLE_EN;
				content = EVENT_RESOURCE_ONLINE_CONTENT_EN;
			}
			type = EventServerityType.SUCCESS;
			if (res.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
				res.setOnlineStatus(OnlineStatus.ONLINE);
				ServiceManager.getInstance().getResourceService().saveResourceSyncOnly(res);
			}
		} else {
			if (res.getOnlineStatus().equals(OnlineStatus.OFFLINE)) {
				return;
			}
			if (isZH) {
				title = EVENT_RESOURCE_OFFLINE_TITLE_ZH;
				content = String.format(EVENT_RESOURCE_OFFLINE_CONTENT_ZH, interval);
			} else {
				title = EVENT_RESOURCE_OFFLINE_TITLE_EN;
				content = String.format(EVENT_RESOURCE_OFFLINE_CONTENT_EN, interval);
			}
			type = EventServerityType.INFO;
			long now = System.currentTimeMillis();
			if (res.getLastCollectTime().getTime() + interval * 60 * 1000 > now) {
				return;
			}
			res.setOnlineStatus(OnlineStatus.OFFLINE);
			ServiceManager.getInstance().getResourceService().saveResourceSyncOnly(res);
		}
		Event event = new Event();
		event.setId(UUIDTypeHandler.createUUID());
		event.setOccurTime(new Timestamp(System.currentTimeMillis()));
		event.setResId(resource.getResourceId());// 是否该改为资源id，非资源的uuid
		event.setMsgTitle(title);
		event.setMsgContent(content);

		event.setSourceType(EventSourceType.MONITOR.getKey());
		// 好像说上线改成ok的标志
		event.setServerity(type.getKey());

		//资源标签
		event.setEventTags(generateEventTags(res));
		event.setTenantId(resource.getTenantId());
		event.setIdentity(StateUtil.RESOURCE_ONLINE_STATE);

		ServiceManager.getInstance().getEventService().create(event);

		// 保存状态点
		Checkpoint checkpoint = new Checkpoint();
		checkpoint.setState(StateUtil.RESOURCE_ONLINE_STATE);
		checkpoint.setTags(new String[]{"tenantId:" + resource.getTenantId(), "resourceId:" + resource.getResourceId()});
		checkpoint.setTimestamp(event.getOccurTime().getTime());
		checkpoint.setValue(resource.getOnlineStatus().getId() + "");
		ServiceManager.getInstance().getStateService().saveCheckpoint(checkpoint);
	}

	private static List<EventTag> generateEventTags(Resource resource) {
		List<EventTag> tags = new ArrayList<>();
		tags.add(new EventTag(resource.getTenantId(), "host", resource.getHostname()));
		tags.add(new EventTag(resource.getTenantId(), "ip", resource.getIpaddr()));
		List<String> resTags = resource.getResTagsAll();
		if (resTags != null && resTags.size() > 0) {
			for (String str : resTags) {
				int index = str.indexOf(":");
				EventTag et;
				if (index == -1) {
					et = new EventTag(resource.getTenantId(), str, "");
				} else {
					et = new EventTag(resource.getTenantId(), str.substring(0, index), str.substring(index + 1));
				}
				tags.add(et);
			}
		}
		return tags;
	}
}
