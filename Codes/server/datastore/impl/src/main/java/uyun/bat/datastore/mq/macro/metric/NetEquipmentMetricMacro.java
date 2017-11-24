package uyun.bat.datastore.mq.macro.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.exception.DataAccessException;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.mq.macro.AbstractMetricMacro;
import uyun.bat.datastore.service.ServiceManager;
import uyun.bat.event.api.entity.EventSourceType;

public class NetEquipmentMetricMacro extends AbstractMetricMacro {
	private static final Logger log = LoggerFactory.getLogger(NetEquipmentMetricMacro.class);

	@Override
	public int getCode() {
		return ComplexMetricData.TYPE_DDAGENT_NETEQUIPMENT;
	}

	@Override
	public void exec(ComplexMetricData complexMetricData) {
		// http://www.uyunsoft.cn/kb/pages/viewpage.action?pageId=15369176

		Resource temp = complexMetricData.getResource();
		Resource res = queryResource(temp.getTenantId(), temp.getAgentId());
		boolean flag = false;
		// 从下线转为上线
		boolean rollover = true;
		Date currentDate = new Date();
		if (res == null) {
			// 网络设备相关的数据不足，不保存本次数据。场景：网络设备未保存，先保存其指标
			if (!hasResourceInfo(temp))
				return;

			res = new Resource(null, currentDate, temp.getHostname(), temp.getIpaddr(), ResourceType.NETWORK,
					"dd-agent:netdev", temp.getAgentId(), temp.getTenantId(), temp.getApps(), OnlineStatus.ONLINE,
					AlertStatus.OK, currentDate, currentDate, temp.getOs(), temp.getResTags(), new ArrayList<String>(),
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
			// 弥补多年前网络设备的agentId不是设备唯一标识的bug
			res.setAgentId(temp.getAgentId());

			if (OnlineStatus.ONLINE.equals(res.getOnlineStatus())) {
				rollover = false;
			} else {
				res.setOnlineStatus(OnlineStatus.ONLINE);
			}

			// 更新apps
			if (temp.getApps() != null && !temp.getApps().isEmpty()) {
				if (res.getApps() != null && !res.getApps().isEmpty()) {
					// 所有app已包含
					boolean isContain = true;
					for (String app : temp.getApps()) {
						if (!res.getApps().contains(app)) {
							isContain = false;
							res.getApps().add(app);
						}
					}
					if (!isContain)
						res.setApps(res.getApps());
				} else {
					res.setApps(temp.getApps());
				}
			}

			if (hasResourceInfo(temp)) {
				// 由于界面上没有设置tag的地方，故tags采用覆盖式
				res.setResTags(temp.getResTags());

				res.setHostname(temp.getHostname());

				res.setIpaddr(temp.getIpaddr());
			}
			res.setLastCollectTime(currentDate);
			res.setOs("netdev");

			flag = updateResource(res, rollover);
		}

		// 插入/更细资源失败,无能为力
		if (!flag) {
			log.warn("更新资源数据失败!将不保存本次指标。");
			return;
		}

		if (rollover)
			resourceOnline(res, EventSourceType.DATADOG_AGENT);

		if (complexMetricData.getResourceDetail() != null) {
			complexMetricData.getResourceDetail().setResourceId(res.getId());
			boolean f = ServiceManager.getInstance().getResourceService()
					.saveResourceDetail(complexMetricData.getResourceDetail());
			if (!f)
				log.warn("资源详情数据保存失败!");
		}

		if (complexMetricData.getPerfMetricList() != null && !complexMetricData.getPerfMetricList().isEmpty()) {
			List<Tag> tagTemp = new ArrayList<Tag>();
			tagTemp.add(new Tag("host", res.getHostname()));
			if (res.getIpaddr() != null && res.getIpaddr().length() > 0)
				tagTemp.add(new Tag("ip", res.getIpaddr()));
			if (res.getTags() != null && !res.getTags().isEmpty()) {
				for (ResourceTag rt : res.getTags()) {
					tagTemp.add(new Tag(rt.getKey(), rt.getValue()));
				}
			}

			for (PerfMetric metric : complexMetricData.getPerfMetricList()) {
				// 设置指标资源id,不知道以后是否改为agentid
				metric.addResourceId(res.getId());

				for (Tag tag : tagTemp) {
					addMetricTag(metric, tag.getKey(), tag.getValue());
				}
				// 增加用户自定义的tag
				generateUserTags(res.getUserTags(),metric);
			}

			insertPerf(complexMetricData.getPerfMetricList());
		}
	}

	private boolean hasResourceInfo(Resource res) {
		return res.getHostname() != null && res.getHostname().length() > 0;
	}

}
