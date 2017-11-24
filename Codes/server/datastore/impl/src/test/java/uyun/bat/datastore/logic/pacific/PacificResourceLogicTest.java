package uyun.bat.datastore.logic.pacific;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.AlertStatus;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceClassCode;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.dao.ResourceDao;
import uyun.bat.datastore.logic.ResourceLogic;
import uyun.bat.datastore.logic.impl.ResourceLogicImpl;
import uyun.bat.datastore.service.PacificManager;
import uyun.pacific.model.api.service.ModelService;
import uyun.pacific.model.api.service.ResAttributeService;
import uyun.pacific.model.api.service.ResClassService;
import uyun.pacific.model.api.service.ResInterfaceService;
import uyun.pacific.model.api.service.ResRelationMetaService;
import uyun.pacific.model.api.service.ResRelationTypeService;
import uyun.pacific.model.api.service.ResUniqueKeyService;
import uyun.pacific.resource.api.service.AuditService;
import uyun.pacific.resource.api.service.ResObjectService;
import uyun.pacific.resource.api.service.ResRelationService;
import uyun.pacific.resource.api.service.ResZoneService;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PacificResourceLogicTest {
	/**
	 * id必须32位
	 */
	private static final String TENANT_ID = "51b3d9e060334acd9e6f5dff8ae49822";
	private static final String RESOURCE_ID = "12345678910111213141516171819203";
	private static PacificResourceLogic pacificResourceLogic = Startup.getInstance()
			.getBean(PacificResourceLogic.class);
	private static PacificManager pacificManager = Startup.getInstance().getBean(PacificManager.class);
	private static ResourceLogic resourceLogic = Startup.getInstance().getBean(ResourceLogic.class);
	private static final String SERVER_CLASSCODE = "Server";
	private static final String SWITCHER_CLASSCODE = "Switch";
	private static final String ROUTER_CLASSCODE = "Router";
	private static String id;
	private static List<String> resIdList = new ArrayList<>();
	static {
		IMocksControl control = EasyMock.createNiceControl();
		
		ResourceDao rd = control.createMock(ResourceDao.class);
		ResObjectService resObjectService = control.createMock(ResObjectService.class);
		ModelService modelService = control.createMock(ModelService.class);
		AuditService auditService = control.createMock(AuditService.class);
		ResInterfaceService resInterfaceService = control.createMock(ResInterfaceService.class);
		ResAttributeService resAttributeService = control.createMock(ResAttributeService.class);
		ResClassService resClassService=control.createMock(ResClassService.class);
		ResRelationMetaService resRelationMetaService = control.createMock(ResRelationMetaService.class);
		ResRelationService  resRelationService = control.createMock(ResRelationService.class);
		ResRelationTypeService resRelationTypeService = control.createMock(ResRelationTypeService.class);
		ResUniqueKeyService resUniqueKeyService = control.createMock(ResUniqueKeyService.class);
		ResZoneService resZoneService = control.createMock(ResZoneService.class);
		EasyMock.expect(modelService.checkAndUpgrade(TENANT_ID)).andReturn(true).times(1);
		
		pacificManager.setPacificResObjectService(resObjectService);
		pacificManager.setPacificModelService(modelService);
		pacificManager.setPacificAuditService(auditService);
		pacificManager.setPacificInterfaceService(resInterfaceService);
		pacificManager.setPacificResAttributeService(resAttributeService);
		pacificManager.setPacificResClassService(resClassService);
		pacificManager.setPacificResRelationMetaService(resRelationMetaService);
		pacificManager.setPacificResRelationService(resRelationService);
		pacificManager.setPacificResRelationTypeService(resRelationTypeService);
		pacificManager.setPacificResUniqueKeyService(resUniqueKeyService);
		pacificManager.setPacificResZoneService(resZoneService);
		
		control.replay();
		pacificManager.getPacificModelService().checkAndUpgrade(TENANT_ID);
	}

	@Test
	public void test7Delete() {
		pacificResourceLogic.delete(TENANT_ID, id);
	}

	//@Test
	public void test1SaveResource() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		ResourceDao rd = EasyMock.createNiceMock(ResourceDao.class);
		EasyMock.expect(rd.deleteResAppById("")).andReturn(1);
		EasyMock.expect(rd.deleteResTagById("")).andReturn(1);
		rd.saveRes(new Resource());
		EasyMock.expectLastCall().andReturn(1);
		EasyMock.replay(rd);
		List<String> tags = new ArrayList<String>();
		tags.add("equipment:Router");
		tags.add("数据中心:数据A");
		tags.add("业务中心:业务B");
		tags.add("ip:10.1.200.101");
		List<String> apps = new ArrayList<String>();
		apps.add("oracle");
		String uuid = UUID.randomUUID().toString();
		Resource resource = new Resource(uuid, new Date(), "hostname", "100.1.10.157", ResourceType.NETWORK, "测试数据.....",
				UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(), new Date(),
				"windows", tags, new ArrayList<String>(), new ArrayList<String>());
		id = pacificResourceLogic.save(resource);
		resource.setId(id);
		resourceLogic.save(resource);
		// pacificResourceLogic.delete(ResourceClassCode.SERVER.getClassCode(),
		// TENANT_ID, id);

	}

	@Test
	public void test3Insert() {
		List<Resource> list = new ArrayList<Resource>();
		for (int i = 0; i < 2; i++) {
			List<String> tags = new ArrayList<String>();
			tags.add("host:bPC");
			List<String> apps = new ArrayList<String>();
			apps.add("oracle");
			String uuid = UUID.randomUUID().toString();
			Resource resource = new Resource(uuid, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER, "测试数据.........",
					UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.OFFLINE, AlertStatus.OK, new Date(), new Date(),
					"winiodws", tags, new ArrayList<String>(), new ArrayList<String>());
			list.add(resource);
		}
		pacificResourceLogic.insert(TENANT_ID, list);

	}

	@Test
	public void test4QueryResourceTags() {
		List<Tag> tags = pacificResourceLogic.queryResourceTags(TENANT_ID);
		System.out.println("tags: " + tags);
	}

	@Test
	public void test5QueryResTagNames() {
		List<String> list = pacificResourceLogic.queryResTagNames(TENANT_ID);
		System.out.println("list: " + list);
	}

	@Test
	public void test6QueryResById() {
		List<String> tags = new ArrayList<String>();
		tags.add("host:zhaoyn,myPC");
		tags.add("数据中心:数据A");
		tags.add("业务中心:业务B");
		tags.add("ip:10.1.200.101");
		List<String> apps = new ArrayList<String>();
		apps.add("oracle");
		String uuid = UUID.randomUUID().toString();
		Resource resource = new Resource(uuid, new Date(), "hostname", "100.1.10.117", ResourceType.SERVER, "测试数据.....",
				UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(), new Date(),
				"windows", tags, new ArrayList<String>(), new ArrayList<String>());
		id = pacificResourceLogic.save(resource);
		Resource res = pacificResourceLogic.queryResById(SERVER_CLASSCODE, TENANT_ID, id);
		System.out.println("resource: " + res);
		pacificResourceLogic.delete(TENANT_ID, id);
		res = pacificResourceLogic.queryResById(SERVER_CLASSCODE, TENANT_ID, id);
		System.out.println("删除后resource: " + res);
	}

	@Test
	public void testQueryResByIpaddr() {
		pacificResourceLogic.queryResByIpaddr(TENANT_ID, "10.1.10.187", ResourceClassCode.SERVER);

	}
	/*
	 * @Test public void testQueryResByAgentId() {
	 * pacificResourceLogic.queryResByAgentId(RESOURCE_ID, TENANT_ID); }
	 * 
	 * @Test public void testQueryAllResStringIntIntOnlineStatus() {
	 * pacificResourceLogic.queryAllRes(TENANT_ID, 1, 10, OnlineStatus.ONLINE);
	 * }
	 * 
	 * @Test public void testQueryResListByCondition() { ResourceOpenApiQuery
	 * query = new ResourceOpenApiQuery(TENANT_ID, "10.1.10.7", null, "计算机",
	 * null, null, new Date( System.currentTimeMillis() - 1000 * 60 * 60), 1,
	 * 10); pacificResourceLogic.queryResListByCondition(query); }
	 */

	@Test
	public void test8GetResFiltersByTag() {
		List<String> list = pacificResourceLogic.getResTagsByTag(TENANT_ID, "host:zhaoyn;ip:10.1.200.101");
		System.out.println("list: " + list);
	}

	/*
	 * @Test public void testGetAuthorizationResIds() {
	 * pacificResourceLogic.getAuthorizationResIds(TENANT_ID, 32); }
	 * 
	 * @Test public void testQueryByTenantIdAndTags() {
	 * pacificResourceLogic.queryByTenantIdAndTags(TENANT_ID, Arrays.asList(new
	 * uyun.bat.datastore.api.entity.Tag("host", "myPC"))); }
	 */

	@Test
	public void test9UpdateResBatch() {
		List<String> tags = new ArrayList<String>();
		tags.add("host:zhaoyn,myPC");
		List<String> apps = new ArrayList<String>();
		apps.add("oracle");
		Resource resource = new Resource(TENANT_ID, new Date(), "hostname", "10.1.10.7", ResourceType.SERVER, "测试数据.....",
				UUID.randomUUID().toString(), TENANT_ID, apps, OnlineStatus.ONLINE, AlertStatus.OK, new Date(), new Date(),
				"windows", tags, new ArrayList<String>(), new ArrayList<String>());
		List<Resource> list = new ArrayList<Resource>();
		list.add(resource);
		List<String> oidList = pacificResourceLogic.updateResBatch(TENANT_ID, list);
		resIdList.addAll(oidList);
	}

	@Test
	public void test10QueryAllRes() {
		pacificResourceLogic.queryAllRes(TENANT_ID, true);
	}

	@Test
	public void test11DeleteAuthorizationResStringListOfString() {
		pacificResourceLogic.deleteAuthorizationRes(TENANT_ID, resIdList);
	}

	/*
	 * @Test public void testGetResCountByDate() {
	 * pacificResourceLogic.getResCountByDate(new
	 * Date(System.currentTimeMillis() - 1000 * 3600 * 24), new Date()); }
	 * 
	 * @Test public void testGetResCount() { pacificResourceLogic.getResCount();
	 * }
	 * 
	 * @Test public void testGetResStatusCount() {
	 * pacificResourceLogic.getResStatusCount(TENANT_ID); }
	 * 
	 * @Test public void testGetResIdInId() {
	 * pacificResourceLogic.getResIdInId(Arrays.asList(UUID.randomUUID().
	 * toString())); }
	 * 
	 * @Test public void testGetAllTenantId() {
	 * pacificResourceLogic.getAllTenantId(); }
	 * 
	 * @Test public void testGetResCountByTenantId() {
	 * pacificResourceLogic.getResCountByTenantId(TENANT_ID); }
	 * 
	 * @Test public void testGetMetricSpanTime() {
	 * pacificResourceLogic.getMetricSpanTime(); }
	 * 
	 * @Test public void testGetAllResId() {
	 * pacificResourceLogic.getAllResId(TENANT_ID); }
	 * 
	 * @Test public void testQuery() {
	 * pacificResourceLogic.query(Arrays.asList(new Tag("host", "myPC")),
	 * OnlineStatus.ONLINE, System.currentTimeMillis()); }
	 * 
	 * @Test public void testBatchInsert() { List<SimpleResource> list = new
	 * ArrayList<SimpleResource>(); List<String> ids = new ArrayList<String>();
	 * for (int i = 0; i < 2; i++) { String id = UUID.randomUUID().toString();
	 * SimpleResource sr = new SimpleResource(); sr.setTenantId(TENANT_ID);
	 * sr.setResourceId(id); sr.setCreateTime(new Date());
	 * sr.setLastCollectTime(new Date()); sr.setIpaddr("127.0.0." + i);
	 * list.add(sr); ids.add(id); } pacificResourceLogic.batchInsert(list);
	 * pacificResourceLogic.batchDelete(TENANT_ID, ids); }
	 * 
	 * @Test public void testBatchUpdate() { List<SimpleResource> list = new
	 * ArrayList<SimpleResource>(); List<String> ids = new ArrayList<String>();
	 * for (int i = 0; i < 2; i++) { String id = UUID.randomUUID().toString();
	 * SimpleResource sr = new SimpleResource(); sr.setTenantId(TENANT_ID);
	 * sr.setResourceId(id); sr.setCreateTime(new Date());
	 * sr.setLastCollectTime(new Date()); sr.setIpaddr("127.0.0." + i);
	 * list.add(sr); ids.add(id); } pacificResourceLogic.batchUpdate(list);
	 * pacificResourceLogic.batchDelete(TENANT_ID, ids); }
	 * 
	 * @Test public void testSaveSimpleResource() { SimpleResource sr = new
	 * SimpleResource(); sr.setTenantId(TENANT_ID);
	 * sr.setResourceId(RESOURCE_ID); sr.setCreateTime(new Date());
	 * sr.setLastCollectTime(new Date()); sr.setIpaddr("127.0.0.1");
	 * pacificResourceLogic.save(sr); }
	 * 
	 * @Test public void testDeleteSimpleResource() {
	 * pacificResourceLogic.deleteSimpleResource(TENANT_ID, RESOURCE_ID); }
	 * 
	 * @Test public void testBatchDelete() {
	 * pacificResourceLogic.batchDelete(TENANT_ID, Arrays.asList(RESOURCE_ID));
	 * }
	 * 
	 * @Test public void testQueryByFilterAndGroupByTag() {
	 * 
	 * }
	 * 
	 * @Test public void testQueryByKey() {
	 * 
	 * }
	 * 
	 * @Test public void testQueryByKeyAndSortBy() {
	 * 
	 * }
	 */
}
