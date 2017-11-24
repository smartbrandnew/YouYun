package uyun.bat.dashboard.api.service;

import java.util.List;

import uyun.bat.dashboard.api.entity.TenantResTemplate;

/**
 * 
 * @author zhoucp 
 * Create at 2017年2月9日 上午10:55:32
 */
public interface TenantResTemplateService {
	/**
	 * 获取租户个性化模板
	 * 
	 * @param appName
	 * @param tenantId
	 * @param resourceId
	 * @return
	 */
	TenantResTemplate getTemplate(String appName,String tenantId,String resourceId);
	
	/**
	 * 创建模板
	 * 
	 * @param template
	 */
	void createTemplate(TenantResTemplate template);
	
	/**UUIDTypeHandler
	 * 更新模板
	 * 
	 * @param template
	 */
	void update(TenantResTemplate template);
	
	/**
	 * 删除模板
	 * 
	 * @param template
	 */
	void delete(TenantResTemplate template);
	
	/**
	 * 获取全局模板
	 * 
	 * @param appName
	 * @param tenantId
	 * @return
	 */
	TenantResTemplate getGlobalTemplate(String appName,String tenantId);
	
}
