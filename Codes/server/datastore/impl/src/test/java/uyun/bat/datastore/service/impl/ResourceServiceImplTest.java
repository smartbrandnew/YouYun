package uyun.bat.datastore.service.impl;

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
import uyun.bat.datastore.api.entity.ResourceOpenApiQuery;
import uyun.bat.datastore.api.entity.ResourceOrderBy;
import uyun.bat.datastore.api.entity.ResourceOrderBy.Order;
import uyun.bat.datastore.api.entity.ResourceOrderBy.SortBy;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.service.ResourceService;

public class ResourceServiceImplTest {
    private static ResourceService resourceService = (ResourceService) Startup.getInstance().getBean("resourceService");
    /**
     * id必须32位
     */
    private static final String TENANT_ID = "12345678910111213141516171819202";
    
    private static final String RESOURCE_ID="12345678910111213141516171819202";
    @Test
    public void queryResourceTags() {
        resourceService.queryResTags(TENANT_ID);

    }

    @Test
    public void queryAllRes() {
        resourceService.queryAllRes(TENANT_ID, false);
    }

    @Test
    public void queryResTagNames() {
        resourceService.queryResTagNames(TENANT_ID);
    }

    @Test
    public void insert() {
		List<String> tags = new ArrayList<String>();
		tags.add("host:myPC");
        List<String> apps = new ArrayList<String>();
        apps.add("oracle");
        Resource resource = new Resource(RESOURCE_ID, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER,
				"测试数据......", UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(),
				new Date(), "winiodws", tags, new ArrayList<String>(), new ArrayList<String>());
        resourceService.insertAsync(resource);
        boolean ig = resourceService.insertAsync(resource);
        resourceService.delete(TENANT_ID, RESOURCE_ID);
        Assert.assertTrue(ig);
    }

    @Test
    public void update() {
		List<String> tags = new ArrayList<String>();
		tags.add("host:test,broada");
		tags.add("user:tom1");
		tags.add("company:uyun");
        List<String> apps = new ArrayList<String>();
        apps.add("oracle");
        apps.add("mysql");
        apps.add("windows");
        Resource resource = new Resource(UUID.randomUUID().toString(), new Date(), "hostname", "10.1.10.22",
				ResourceType.SERVER, "测试数据.......", UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE,
				AlertStatus.OK, new Date(), new Date(), "linux", tags, new ArrayList<String>(), new ArrayList<String>());
        resourceService.updateAsync(resource);
    }

    @Test
    public void testDelete() {
        resourceService.delete(TENANT_ID, RESOURCE_ID);
    }

    @Test
    public void testInsertBatch() {
        List<Resource> list = new ArrayList<Resource>();
        List<String> ids = new ArrayList<String>();
        for (int i = 0; i < 1; i++) {
			List<String> tags = new ArrayList<String>();
			tags.add("host:myPC");
            List<String> apps = new ArrayList<String>();
            apps.add("oracle");
            apps.add("mysql");
            String id =RESOURCE_ID;
            Resource resource = new Resource(id, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER, "测试数据.........",
					UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.OFFLINE, AlertStatus.OK, new Date(), new Date(),
					"winiodws", tags, new ArrayList<String>(), new ArrayList<String>());
            list.add(resource);
            ids.add(id);
        }
        resourceService.insertAsync(list, TENANT_ID);
        for (String id : ids) {
            resourceService.delete(TENANT_ID, id);
        }

    }

    /**
     * 根据关键字(ipaddr、hostname、tagk、tag)，使用tag查询格式(host:mypc)
     * 格式示例（"zhaoyn,10.1.10.7,host:mypc,host:mysql")，关键字用";"号分开
     */
    @Test
    public void queryResByKey() {
        String key = "hostname;10.1.10.7;host:myPC";
        resourceService.queryByKey(TENANT_ID, key, 0, 10, OnlineStatus.ONLINE);
    }

    @Test
    public void queryByFilterAndSortBy() {
        resourceService.queryByKeyAndSortBy(TENANT_ID, "", new ResourceOrderBy(Order.ASCENDING, SortBy.ONLINESTATUS), 1,
                10, null);
    }

    @Test
    public void queryByFilterAndGroupByTag() {
        String filter = "";
        String tenantId = TENANT_ID;
        String groupBy = "host";
        int pageNo = 1;
        int size = 10;
        resourceService.queryByFilterAndGroupByTag(tenantId, filter, groupBy, pageNo, size, OnlineStatus.ONLINE);
    }

    @Test
    public void queryAllResPage() {
        resourceService.queryAllRes(TENANT_ID, 1, 10, OnlineStatus.OFFLINE);
    }

    @Test
    public void queryByFilter() {
        String key = "";
        resourceService.queryByKey(TENANT_ID, key, 1, 10, OnlineStatus.OFFLINE);
    }

    @Test
    public void querySimpleResource() {
		resourceService.query(OnlineStatus.ONLINE, System.currentTimeMillis());
    }

    @Test
    public void queryResDetail() {
        resourceService.queryByResId(TENANT_ID);
    }

    @Test
    public void saveResDetail() {
        resourceService.saveResourceDetail(new ResourceDetail(TENANT_ID, TENANT_ID, "monitor专用", "agent 2.7.8"));
    }

    @Test
    public void deleteResDetail() {
        resourceService.deleteResourceDetail(TENANT_ID);
    }

    @Test
    public void queryResourceListByCondition() {
        ResourceOpenApiQuery query = new ResourceOpenApiQuery(TENANT_ID, "10.1.10.7", null, "计算机", null, null, new Date(
                System.currentTimeMillis() - 1000 * 60 * 60), 1, 10);
        resourceService.queryResListByCondition(query);
    }

    @Test
    public void queryResCount() {
        resourceService.getResCount();
    }

    @Test
    public void queryResCountByDate() {
        resourceService.getResCountByDate(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000 * 20l), new Date());
    }


    @Test
    public void queryTagBytags() {
        resourceService.getResTagsByTag(TENANT_ID, "");
    }

    public void getResStatusCount() {
        resourceService.getResStatusCount("E0A67E986A594A61B3D1E523A0A39C77");
    }
}
