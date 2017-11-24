package com.broada.carrier.monitor.probe.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.db.DataSource;
import com.broada.carrier.monitor.probe.impl.dispatch.MonitorDispatcher;
import com.broada.carrier.monitor.probe.impl.dispatch.MonitorResultUploader;
import com.broada.carrier.monitor.probe.impl.yaml.YamlTaskService;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.component.utils.runcheck.DataSourceInfoProvider;
import com.broada.component.utils.runcheck.RuntimeChecker;

public class Probe {
	private static final Logger logger = LoggerFactory.getLogger(Probe.class);
	@Autowired
	private Config config;
	@Autowired
	private DataSource dataSource;
	@Autowired
	private YamlTaskService yamlTaskService;
	private MonitorProbe probe;

	public void startup() {
		String ip = config.getProbeIp();
		if ("127.0.0.1".equals(ip))
			throw new RuntimeException("config.properties文件中属性probe.ipaddr不能配置为127.0.0.1");
		probe = config.getProbe();
		DataSourceInfoProvider.addDataSource(dataSource.getDataSource());
		yamlTaskService.syncAll();
		RuntimeChecker.getDefault().startup();

		MonitorResultUploader.getDefault().startup(probe.getCode());
		MonitorDispatcher.getDefault().startup();
		// 输出测试时probe主机的cpu、内存使用信息
		// SelfMonitor.getInstance().outputInfo();
	}
}
