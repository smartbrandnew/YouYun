package uyun.bat.datastore.api.entity;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

public class ResourceTest {

	Resource resource = new Resource();
	@Test
	public void test() {
		Date modified = new Date();
		resource.setModified(modified);
		Date lastCollectTime = new Date();
		resource.setLastCollectTime(lastCollectTime);
		Date createTime = new Date();
	    resource.setCreateTime(createTime);
	    resource.setIpaddr("127.0.0.1");
	    resource.setOs("os");
	    resource.setHostname("hostname");
	    resource.setDescribtion("describtion");
	    resource.setId(UUID.randomUUID().toString());
	    resource.setTenantId(UUID.randomUUID().toString());
		
		ResourceType type = ResourceType.NETWORK;
		List<String> apps = new ArrayList<>();
		OnlineStatus onlineStatus = OnlineStatus.ONLINE;
		AlertStatus alertStatus = AlertStatus.OK;

		Map<String, String> attritutes = new HashMap<>();
		attritutes.put("createTime", "123");
		attritutes.put("lastCollectTime", "123");
		attritutes.put("modified", "123");
		attritutes.put("onlineStatus", "123");
		
		Resource r = new Resource("123",modified,"hostname","127.0.0.1",type,
				"describtion","agentId","tenantId",apps,onlineStatus,
				alertStatus,lastCollectTime,createTime,"os",new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		r.toString();
	}
	
	@Test
	public void entityTest(){
		resource.getAgentId();
		resource.getAlertStatus();
		resource.getAppNames();
		resource.getApps();
		resource.getCreateTime();
		resource.getDescribtion();
		resource.getHostname();
		resource.getId();
		resource.getIpaddr();
		resource.getLastCollectTime();
		resource.getModified();
		resource.getOnlineStatus();
		resource.getOs();
		resource.getResourceTypeName();
		resource.getResTags();
		resource.getTags();
		resource.getTenantId();
		resource.getUserTags();
		resource.setResourceTypeName("test");
	}

}
