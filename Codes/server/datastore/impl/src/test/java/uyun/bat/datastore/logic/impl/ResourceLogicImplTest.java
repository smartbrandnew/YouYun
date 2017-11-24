package uyun.bat.datastore.logic.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceOrderBy;
import uyun.bat.datastore.api.entity.ResourceOrderBy.Order;
import uyun.bat.datastore.api.entity.ResourceOrderBy.SortBy;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.logic.ResourceLogic;

public class ResourceLogicImplTest {
    private static ResourceLogic resourceLogic = (ResourceLogic) Startup.getInstance().getBean("resourceLogic");
    private static final String TENANT_ID = "12345678910111213141516171819202";
  	private static final String RESOURCE_ID = "12345678910111213141516171819204";

	@Test
	public void queryResourceTags() {
		resourceLogic.queryResourceTags(TENANT_ID);
	}

	@Test
	public void queryAllRes() {
		resourceLogic.queryAllRes(TENANT_ID, true);
	}

	@Test
	public void queryResTagNames() {
		resourceLogic.queryResTagNames(TENANT_ID);
	}

	@Test
	public void save() {
		List<String> tags = new ArrayList<String>();
		tags.add("host:zhaoyn,myPC");
		List<String> apps = new ArrayList<String>();
		apps.add("oracle");
		String uuid = UUID.randomUUID().toString();
		Resource resource = new Resource(RESOURCE_ID, new Date(), "BROADA-ZHAOYN", "127.0.0.1", ResourceType.SERVER,
				"测试数据.....", UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(),
				new Date(), "winiodws", tags, new ArrayList<String>(), new ArrayList<String>());
		boolean ig = resourceLogic.save(resource);
		// resourceLogic.delete(uuid);
		Assert.assertTrue(ig);
	}

	@Test
	public void delete() {
		resourceLogic.delete(TENANT_ID, UUID.randomUUID().toString());
	}

	@Test
	public void testInsertBatch() {
		List<Resource> list = new ArrayList<Resource>();
		List<String> ids = new ArrayList<String>();
		for (int i = 0; i < 1; i++) {
			List<String> tags = new ArrayList<String>();
			tags.add("host:bPC");
			List<String> apps = new ArrayList<String>();
			apps.add("oracle");
			String uuid = UUID.randomUUID().toString();
			Resource resource = new Resource(uuid, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER, "测试数据.........",
					UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.OFFLINE, AlertStatus.OK, new Date(), new Date(),
					"winiodws", tags, new ArrayList<String>(), new ArrayList<String>());
			list.add(resource);
			ids.add(uuid);
		}
		resourceLogic.insert(TENANT_ID, list);
		for (String id : ids) {
			resourceLogic.delete(TENANT_ID, id);
		}
	}

	/**
	 * 根据关键字(ipaddr、hostname、tagk、tag)，使用tag查询格式(host:mypc)
	 * 格式示例（"zhaoyn,10.1.10.7,host:mypc,host:mysql")，关键字用","号分开
	 */
	@Test
	public void queryResByKey() {
		String key = "hostname,10.1.10.22";
		resourceLogic.queryByKey(TENANT_ID, key, 0, 10, null);
	}

	@Test
	public void queryByFilterAndSortBy() {
		resourceLogic.queryByKeyAndSortBy(TENANT_ID, "", new ResourceOrderBy(Order.ASCENDING,
				SortBy.ONLINESTATUS), 1, 10, null);
	}

	@Test
	public void queryByFilterAndGroupByTag() {
		String filter = "  ";
		String tenantId = TENANT_ID;
		String groupBy = "host";
		int pageNo = 1;
		int size = 10;
		resourceLogic.queryByFilterAndGroupByTag(tenantId, filter, groupBy, pageNo, size, null);

	}

	@Test
	public void queryAllResPage() {
		resourceLogic.queryAllRes(TENANT_ID, 1, 10, null);
	}

	@Test
	public void queryByFilter() {
		String key = "";
		resourceLogic.queryByKey(TENANT_ID, key, 1, 10, null);
	}

	@Test
	public void querySimpleResource() {
		resourceLogic.query(OnlineStatus.ONLINE, System.currentTimeMillis());
	}

	@Test
	public void queryResDetail() {
		resourceLogic.queryByResId(TENANT_ID);
	}

	@Test
	public void saveResDetail() {
		resourceLogic
				.saveResourceDetail(new ResourceDetail(TENANT_ID, TENANT_ID, "2016年7月份引入", "agent 2.7.8"));
	}

	@Test
	public void deleteResDetail() {
		resourceLogic.deleteResourceDetail(TENANT_ID);
	}

	@Test
	public void testUpdateBatch() {

		List<String> tags = new ArrayList<String>();
		tags.add("host:zhaoyn,myPC");
		List<String> apps = new ArrayList<String>();
		apps.add("oracle");
		Resource resource = new Resource(TENANT_ID, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER, "测试数据.....",
				UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(), new Date(),
				"winiodws", tags, new ArrayList<String>(), new ArrayList<String>());
		List<Resource> list = new ArrayList<Resource>();
		list.add(resource);
		resourceLogic.updateResBatch(TENANT_ID,list);
	}

	@Test
	public void testGetAuthorizationIds() {
		resourceLogic.getAuthorizationResIds(TENANT_ID, 3);
	}

	@Test
	public void testDeleteAuthorizationRes() {
		resourceLogic.deleteAuthorizationRes(TENANT_ID,new ArrayList<String>(Arrays.asList(new String[] {
				"0015bba49a0966dcd60e9e05b37a968f", "00177ced3fdc1ebd829271b8dc519185" })));
	}

	@Test
	public void testGetMetricSpanTime() {
		resourceLogic.getMetricSpanTime();
	}
}
