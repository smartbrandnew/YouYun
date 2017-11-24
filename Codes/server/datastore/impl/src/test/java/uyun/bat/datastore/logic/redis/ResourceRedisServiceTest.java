package uyun.bat.datastore.logic.redis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;

public class ResourceRedisServiceTest {
	private static ResourceRedisService service = Startup.getInstance().getBean(ResourceRedisService.class);
	/**
	 * id必须32位
	 */
	private static final String TENANT_ID = "12345678910111213141516171819202";
	private static final String RESOURCE_ID = "12345678910111213141516171819203";

	@Test
	public void testInsertResourceLong() {

		List<String> tags = new ArrayList<String>();
		tags.add("host:zhaoyn,myPC");
		List<String> apps = new ArrayList<String>();
		apps.add("oracle");

		// 插入或者更新资源
		Resource resource = new Resource();
		resource.setHostname("localhost");
		resource.setIpaddr("127.0.0.1");
		resource.setResTags(tags);
		resource.setAgentId("");
		resource.setTenantId(TENANT_ID);
		resource.setApps(apps);
		resource.setOs("linux");
		resource.setId(RESOURCE_ID);
		service.insert(resource, 10);
	}

	@Test
	public void testUpdate() {

		List<String> tags = new ArrayList<String>();
		tags.add("host:zhaoyn,myPC");
		List<String> apps = new ArrayList<String>();
		apps.add("oracle");
		Resource resource = new Resource(RESOURCE_ID, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER,
				"测试数据.....", UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(),
				new Date(), "winiodws", tags, new ArrayList<String>(), new ArrayList<String>());
		service.update(resource);
	}

	@Test
	public void testDelete() {
		service.delete(RESOURCE_ID);
	}

	@Test
	public void testGetResById() {
		service.queryResById(RESOURCE_ID);
	}
}
