package uyun.bat.datastore.mq.macro.metric;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PerfMetric;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.exception.DataAccessException;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.mq.macro.AbstractMetricMacro;
import uyun.bat.event.api.entity.EventSourceType;

public class OpenAPIMetricMacro extends AbstractMetricMacro {
	private static final Logger log = LoggerFactory.getLogger(OpenAPIMetricMacro.class);

	@Override
	public int getCode() {
		return ComplexMetricData.TYPE_OPENAPI;
	}

	@Override
	public void exec(ComplexMetricData complexMetricData) {
		Resource temp = complexMetricData.getResource();

		if (!"unknown".equals(temp.getAgentId())) {
			Resource res = queryResource(temp.getTenantId(), temp.getAgentId());
			boolean flag = false;
			// 从下线转为上线
			boolean rollover = true;
			Date currentDate = new Date();
			String ipaddr = temp.getIpaddr() == null ? AbstractMetricMacro.UNKNOWN : temp.getIpaddr();
			if (res == null) {
				// 先默认为主机设备
				res = new Resource(null, currentDate, temp.getHostname(), ipaddr, temp.getType() == null? ResourceType.SERVER:temp.getType(), "openapi",
						temp.getAgentId(), temp.getTenantId(), temp.getApps(), OnlineStatus.ONLINE, AlertStatus.OK, currentDate,
						currentDate, temp.getOs(), new ArrayList<String>(), new ArrayList<String>(), temp.getAgentlessTags());
				try {
					flag = instertResource(res);
				} catch (DataAccessException e) {
					log.warn(e.getMessage());
					if (log.isDebugEnabled()) {
						log.debug("Stack：", e);
					}
				}
			} else {
				// openAPI插入的数据并没有设定在线状态，按道理这里应该保持是在线状态
				if (OnlineStatus.ONLINE.equals(res.getOnlineStatus())) 
					rollover = false;
				else 
					res.setOnlineStatus(OnlineStatus.ONLINE);
				if (!"unknown".equalsIgnoreCase(temp.getHostname().trim()))
					res.setHostname(temp.getHostname());
				if (!"unknown".equalsIgnoreCase(ipaddr))
					res.setIpaddr(ipaddr);
				res.setLastCollectTime(currentDate);
				if (temp.getOs() != null && temp.getOs().length() > 0)
					res.setOs(temp.getOs());
				if (null != temp.getType())
					res.setType(temp.getType());
				if (temp.getAgentlessTags() != null && !temp.getAgentlessTags().isEmpty())
					res.setAgentlessTags(temp.getAgentlessTags());
				List<String> apps = res.getApps();
				for (String app : temp.getApps()) {
					if (!apps.contains(app))
						apps.add(app);
				}
				res.setApps(apps);
				flag = updateResource(res, rollover);
			}
			// 插入失败,无能为力
			if (!flag)
				return;
			if (rollover)
				resourceOnline(res, EventSourceType.OPEN_API);
			
			for (PerfMetric metric : complexMetricData.getPerfMetricList()) {
				// 设置指标资源id,不知道以后是否改为agentid
				metric.addResourceId(res.getId());

				// 默认添加host标签
				addMetricTag(metric, "host", res.getHostname());

				// 如果ip有效，则添加ip tag
				boolean isIPAvailable = res.getIpaddr() != null && res.getIpaddr().length() > 0
						&& !AbstractMetricMacro.UNKNOWN.equalsIgnoreCase(res.getIpaddr());
				if (isIPAvailable) {
					addMetricTag(metric, "ip", res.getIpaddr());
				}

				if (!res.getTags().isEmpty()) {
					for (ResourceTag tag : res.getTags()) {
						addMetricTag(metric, tag.getKey(), tag.getValue());
					}
				}

				// 增加用户自定义的tag
				generateUserTags(res.getUserTags(), metric);

				// 增加agentlessTags
				generateUserTags(res.getAgentlessTags(), metric);

			}
		}
		insertPerf(complexMetricData.getPerfMetricList());
	}
}
