package com.broada.carrier.monitor.probe.impl.sync.service;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.probe.impl.config.Config;

import com.broada.carrier.monitor.probe.impl.sync.entity.Agent;
import com.broada.carrier.monitor.probe.impl.sync.entity.AgentSource;
import com.broada.carrier.monitor.probe.impl.util.HTTPClientUtils;
import com.broada.carrier.monitor.probe.impl.util.HostUtil;
import com.broada.carrier.monitor.probe.impl.util.UUIDUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProbeInfoSyncService {
	private static final Logger logger = LoggerFactory.getLogger(ProbeInfoSyncService.class);
	private static final String url = Config.getDefault().getProperty("agentapi.push.agentinfo");
	private static ObjectMapper mapper = new ObjectMapper();

	private static ProbeInfoSyncService instance = new ProbeInfoSyncService();

	public static ProbeInfoSyncService getInstance() {
		return instance;
	}

	public boolean postProbeInfo() {
		Agent agent = getProbeInfo();
		try {
			String json = mapper.writeValueAsString(agent);
			String result = HTTPClientUtils.post(url, json);
			if (result != null)
				return true;
			else {
				logger.warn("上报probe信息失败");
				return false;
			}
		} catch (JsonProcessingException e) {
			logger.warn("json转换异常: ", e);
		} catch (Exception e) {
			logger.warn("上报probe信息出现异常: ", e);
		}
		return false;
	}

	public Agent getProbeInfo() {
		String ipaddr = Config.getDefault().getProbeIp();
		if (ipaddr == null || HostIpUtil.getLocalHost().equals(ipaddr.trim())) {
			ipaddr = HostUtil.getIP();
		}
		String hostname = Config.getDefault().getProbeHostName();
		if (hostname == null || HostIpUtil.getLocalHost().equals(hostname.trim()) || "localhost".equalsIgnoreCase(hostname.trim()))
			hostname = HostUtil.getHostname();
		String id = UUIDUtils.getProbeId();
		String path = Config.getConfDir() + "/conf.d";
		File folder = new File(path);
		List<String> apps = new ArrayList<String>();
		if (folder.isDirectory()) {
			File[] files = folder.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.getName().endsWith(".yaml"))
						return true;
					return false;
				}
			});
			for (File file : files) {
				String name = file.getName();
				apps.add(name);
			}
		}
		List<String> tags = Config.getDefault().getProbeTags();
		Agent agent = new Agent(id, hostname, ipaddr, tags, apps, AgentSource.agentless, new Date());
		return agent;
	}
}
