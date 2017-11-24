package com.broada.carrier.monitor.probe.impl.task.resource.detail;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.ResourceDetail;
import com.broada.carrier.monitor.probe.impl.openapi.service.ResourceService;
import com.broada.carrier.monitor.probe.impl.task.resource.detail.impl.CLIProvider;
import com.broada.utils.StringUtil;


public class ResourceDetailUploadService {
	
	private static Logger LOG = LoggerFactory.getLogger(ResourceDetailUploadService.class);
	
	@Autowired
	private ResourceService service;
	@Autowired
	private ProbeServiceFactory probeFactory;
	
	private static final String CONFIG_FILE_SUFFIX = ".yaml";
	
	public void uploadResourceDetail(){
		try{
			String providers = Config.getDefault().getProperty("resource.detail.provider", "cli");
			if(StringUtil.isNullOrBlank(providers)) 
				return ;
			Set<String> apps = new HashSet<String>(Arrays.asList(providers.split(",")));
			File yamlDir = new File(Config.getYamlDir());
			File[] files = yamlDir.listFiles();
			if(files != null){
				for(File file:files){
					if(!file.getName().endsWith(CONFIG_FILE_SUFFIX))
						continue;
					if(!apps.contains(file.getName().replace(CONFIG_FILE_SUFFIX, ""))) 
						continue;
					Provider provider = getProvider(file.getName().replace(CONFIG_FILE_SUFFIX, ""));
					if(provider == null)
						continue;
					List<ResourceDetail> details = provider.getResourceDetail();
					if(details == null || details.isEmpty())
						return ;
					uploadResourceDetail(details);
				}
			}
		} catch (Exception e) {
			LOG.error("上报资源详情失败:{}",e);
		}
		
	}
	
	/**
	 * 根据应用名获取响应的provider
	 * @param app
	 * @return
	 */
	public Provider getProvider(String app){
		if(app.equalsIgnoreCase("cli"))
			return new CLIProvider(probeFactory);
		else
			return null;
	}
	
	/**
	 * 上报资源详情
	 * @param details
	 */
	private void uploadResourceDetail(List<ResourceDetail> detail){
		service.postResourceDetail(detail);
	}
	
}
