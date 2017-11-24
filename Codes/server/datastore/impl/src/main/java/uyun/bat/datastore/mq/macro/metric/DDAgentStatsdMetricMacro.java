package uyun.bat.datastore.mq.macro.metric;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.exception.DataAccessException;
import uyun.bat.datastore.api.mq.ComplexMetricData;
import uyun.bat.datastore.mq.macro.AbstractMetricMacro;
import uyun.bat.datastore.service.ServiceManager;

public class DDAgentStatsdMetricMacro extends AbstractMetricMacro {
	private static final Logger log = LoggerFactory.getLogger(DDAgentStatsdMetricMacro.class);

	@Override
	public int getCode() {
		return ComplexMetricData.TYPE_DDAGENT_STATSD;
	}

	@Override
	public void exec(ComplexMetricData complexMetricData) {
		Resource temp = complexMetricData.getResource();
		Resource res = queryResource(temp.getTenantId(), temp.getAgentId());

		if (res == null) {
			Date currentDate = new Date();
			res = new Resource(null, currentDate, temp.getHostname(), AbstractMetricMacro.UNKNOWN, temp.getType(),
					"dd-agent:host", temp.getAgentId(), temp.getTenantId(), temp.getApps(), OnlineStatus.ONLINE, AlertStatus.OK,
					currentDate, currentDate, temp.getOs(), new ArrayList<String>(), new ArrayList<String>(),
					new ArrayList<String>());

			boolean flag = false;
			try {
				flag = instertResource(res);
			} catch (DataAccessException e) {
				log.warn(e.getMessage());
				if (log.isDebugEnabled()) {
					log.debug("Stack：", e);
				}
			}
			if (!flag) {
				log.warn("插入资源数据失败!将不保存本次指标。");
				return;
			}
		} else {
			boolean needUpdate = false;
			res.setOs(temp.getOs());
			if (null != temp.getType())
				res.setType(temp.getType());
			// 更新apps
			if (res.getApps() != null && !res.getApps().isEmpty()) {
				// 所有app已包含
				boolean isContain = true;
				for (String app : temp.getApps()) {
					if (!res.getApps().contains(app)) {
						isContain = false;
						res.getApps().add(app);
					}
				}
				if (!isContain) {
					res.setApps(res.getApps());
					needUpdate = true;
				}
			} else {
				if (temp.getApps().size() > 0) {
					res.setApps(temp.getApps());
					needUpdate = true;
				}
			}

			// 此处暂不更新资源的最后上传数据时间，也不做上下线状态变更
			if (needUpdate) {
				boolean flag = ServiceManager.getInstance().getResourceService().updateAsync(res);
				if (!flag) {
					log.warn("更新资源数据失败!将不保存本次指标。");
					return;
				}
			}
		}
		
		// 如果ip有效，则添加ip tag
		boolean isIPAvailable = res.getIpaddr() != null && res.getIpaddr().length() > 0
				&& !AbstractMetricMacro.UNKNOWN.equalsIgnoreCase(res.getIpaddr());

		if (complexMetricData.getPerfMetricList() != null && !complexMetricData.getPerfMetricList().isEmpty()) {

			for (PerfMetric metric : complexMetricData.getPerfMetricList()) {
				// 设置指标资源id,不知道以后是否改为agentid
				metric.addResourceId(res.getId());

				if (isIPAvailable) {
					addMetricTag(metric, "ip", res.getIpaddr());
				}
				
				// 增加用户自定义的tag
				generateUserTags(res.getUserTags(), metric);
			}

			insertPerf(complexMetricData.getPerfMetricList());
		}
	}

}
