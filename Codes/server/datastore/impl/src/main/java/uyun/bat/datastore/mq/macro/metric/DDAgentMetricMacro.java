package uyun.bat.datastore.mq.macro.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.exception.DataAccessException;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.api.mq.EventInfo;
import uyun.bat.datastore.mq.macro.AbstractMetricMacro;
import uyun.bat.datastore.service.ServiceManager;
import uyun.bat.event.api.entity.Event;
import uyun.bat.event.api.entity.EventSourceType;
import uyun.bat.event.api.entity.EventTag;

public class DDAgentMetricMacro extends AbstractMetricMacro {
	private static final Logger log = LoggerFactory.getLogger(DDAgentMetricMacro.class);

	@Override
	public int getCode() {
		return ComplexMetricData.TYPE_DDAGENT;
	}

	@Override
	public void exec(ComplexMetricData complexMetricData) {
		Resource temp = complexMetricData.getResource();
		// 指标里面的hostname与外面的hostname一致时正常创建资源
		Resource res = generateResource(complexMetricData.getResourceDetail(), temp.getTenantId(), temp.getAgentId(),
				temp.getIpaddr(), temp.getHostname(), temp.getResTags(), temp.getApps(), temp.getOs(), "dd-agent:host", temp.getType());

		if (res == null)
			return;
		// 如果ip有效，则添加ip tag
		boolean isIPAvailable = res.getIpaddr() != null && res.getIpaddr().length() > 0
				&& !AbstractMetricMacro.UNKNOWN.equalsIgnoreCase(res.getIpaddr());

		if (complexMetricData.getPerfMetricList() != null && !complexMetricData.getPerfMetricList().isEmpty()) {
			Map<String, List<PerfMetric>> metricsMap = new HashMap<>();
			for (PerfMetric metric : complexMetricData.getPerfMetricList()) {
				if (metric == null || metric.getTags() == null || metric.getTags().get("host") == null) {
					continue;
				}
				// 设置指标资源id,不知道以后是否改为agentid
				if (metric.getTags().get("host").get(0).equals(res.getHostname())) {
					metric.addResourceId(res.getId());

					if (isIPAvailable) {
						addMetricTag(metric, "ip", res.getIpaddr());
					}

					if (!res.getTags().isEmpty()) {
						for (ResourceTag tag : res.getTags()) {
							addMetricTag(metric, tag.getKey(), tag.getValue());
						}
					}
					metric = generateUserTags(res.getUserTags(), metric);
					generateMetric(metricsMap, res.getId(), metric);

				} else {
					String hostName = metric.getTags().get("host").get(0);
					List<String> apps = generateAPP(metric);
					List<String> ips = metric.getTags().get("ip");
					String ip = ips != null ? ips.get(0) : AbstractMetricMacro.UNKNOWN;
					Resource resTemp = generateResource(null, temp.getTenantId(), hostName, ip, hostName,
							new ArrayList<String>(), apps, "", temp.getHostname(), temp.getType());
					metric.addResourceId(resTemp.getId());

					metric = generateUserTags(res.getUserTags(), metric);
					generateMetric(metricsMap, resTemp.getId(), metric);

				}
			}
			for (Map.Entry<String, List<PerfMetric>> entry : metricsMap.entrySet()) {
				insertPerf(entry.getValue());
			}

		}

		if (complexMetricData.getEventInfoList() != null && !complexMetricData.getEventInfoList().isEmpty()) {
			List<Event> events = new ArrayList<Event>();
			for (EventInfo ei : complexMetricData.getEventInfoList()) {
				Event event = new Event();
				event.setId(ei.getId());
				event.setOccurTime(ei.getOccurTime());
				event.setResId(res.getId());
				event.setMsgTitle(ei.getMsgTitle());
				event.setMsgContent(ei.getMsgContent());
				event.setSourceType(EventSourceType.DATADOG_AGENT.getKey());
				event.setServerity(ei.getServerity());
				List<EventTag> eventTags = new ArrayList<EventTag>();
				for (uyun.bat.common.tag.entity.Tag tagEntry : ei.getEventTags()) {
					EventTag eventTag = new EventTag();
					eventTag.setTagk(tagEntry.getKey());
					if (tagEntry.getValue() != null)
						eventTag.setTagv(tagEntry.getValue());
					else
						eventTag.setTagv("");
					eventTag.setTenantId(res.getTenantId());
					eventTags.add(eventTag);
				}

				event.setEventTags(eventTags);
				event.setTenantId(res.getTenantId());
				event.setIdentity(ei.getIdentity());
				events.add(event);
			}
			ServiceManager.getInstance().getEventService().create(events);
		}
	}

	private List<String> generateAPP(PerfMetric metric) {
		if (metric == null)
			return new ArrayList<String>();

		List<String> apps = new ArrayList<String>();
		int index = -1;
		index = metric.getName().indexOf('.');
		if (index != -1)
			apps.add(metric.getName().substring(0, index));
		return apps;
	}

	/**
	 * metric list里面可能包含不同的resId进行分组
	 */
	private void generateMetric(Map<String, List<PerfMetric>> map, String resId, PerfMetric metric) {
		List<PerfMetric> ps = map.get(resId);
		if (ps == null) {
			ps = new ArrayList<PerfMetric>();
			map.put(resId, ps);
		}
		ps.add(metric);
	}

	private Resource generateResource(ResourceDetail resourceDetail, String tenantId, String agentId, String ipaddr,
			String hostname, List<String> resTags, List<String> apps, String os, String descr, ResourceType type) {
		Resource res = queryResource(tenantId, agentId);
		boolean flag = false;
		// 从下线转为上线
		boolean rollover = true;
		Date currentDate = new Date();
		if (res == null) {
			String ip = ipaddr != null && ipaddr.length() > 0 ? ipaddr : AbstractMetricMacro.UNKNOWN;
			res = new Resource(null, currentDate, hostname, ip, type, descr, agentId, tenantId, apps,
					OnlineStatus.ONLINE, AlertStatus.OK, currentDate, currentDate, os, resTags, new ArrayList<String>(),
					new ArrayList<String>());
			try {
				flag = instertResource(res);
			} catch (DataAccessException e) {
				log.warn(e.getMessage());
				if (log.isDebugEnabled()) {
					log.debug("Stack：", e);
				}
			}
		} else {
			if (OnlineStatus.ONLINE.equals(res.getOnlineStatus())) {
				rollover = false;
			} else {
				res.setOnlineStatus(OnlineStatus.ONLINE);
			}

			// 更新apps
			if (res.getApps() != null && !res.getApps().isEmpty()) {
				// 所有app已包含
				boolean isContain = true;
				for (String app : apps) {
					if (!res.getApps().contains(app)) {
						isContain = false;
						res.getApps().add(app);
					}
				}
				if (!isContain)
					res.setApps(res.getApps());
			} else {
				res.setApps(apps);
			}

			// 由于界面上没有设置tag的地方，故tags采用覆盖式
			// agent的host_tags好像是隔断时间上传的，不是每次都传
			if (resourceDetail != null)
				res.setResTags(resTags);

			res.setHostname(hostname);
			res.setLastCollectTime(currentDate);
			res.setOs(os);
			if (null != type)
				res.setType(type);
			if (ipaddr != null && ipaddr.length() > 0)
				res.setIpaddr(ipaddr);

			flag = updateResource(res, rollover);
		}

		// 插入/更细资源失败,无能为力
		if (!flag) {
			log.warn("更新资源数据失败!将不保存本次指标。");
			return null;
		}

		if (rollover)
			resourceOnline(res, EventSourceType.DATADOG_AGENT);

		if (resourceDetail != null) {
			resourceDetail.setResourceId(res.getId());
			boolean f = ServiceManager.getInstance().getResourceService().saveResourceDetail(resourceDetail);
			if (!f)
				log.warn("资源详情数据保存失败!");
		}

		return res;
	}

}
