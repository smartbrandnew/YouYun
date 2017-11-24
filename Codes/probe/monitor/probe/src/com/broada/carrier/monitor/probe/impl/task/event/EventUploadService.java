package com.broada.carrier.monitor.probe.impl.task.event;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.EventVO;
import com.broada.carrier.monitor.probe.impl.openapi.service.EventService;
import com.broada.carrier.monitor.probe.impl.task.event.cli.CliEvent;
import com.broada.carrier.monitor.probe.impl.task.event.ipmi.IpmiEvent;

public class EventUploadService {

	private static Logger LOG = LoggerFactory.getLogger(EventUploadService.class);
	
	@Autowired
	private IpmiEvent ipmiEvent;
	@Autowired
	private CliEvent cliEvent;
	
	@Autowired
	EventService eventService;
	
	public void uploadEvent(){
		List<EventVO> events = new ArrayList<EventVO>();
		File yamlDir = new File(Config.getYamlDir());
		File ipmi_cfg = new File(yamlDir, "ipmi.yaml");
		if(ipmi_cfg.exists() && ipmi_cfg.isFile())
			ipmiEvent.generateIpmiEvent(events, ipmi_cfg);
		File cli_cfg = new File(yamlDir, "cli.yaml");
		if(cli_cfg.exists() && cli_cfg.isFile())
			cliEvent.generateCliEvent(events, cli_cfg);
		if(events.isEmpty()) return;
		eventService.postHosts(events);
		LOG.info("上报故障事件数量:" + events.size());
	}
	
}
