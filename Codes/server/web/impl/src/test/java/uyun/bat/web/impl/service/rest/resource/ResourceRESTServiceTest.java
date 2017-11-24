package uyun.bat.web.impl.service.rest.resource;

import org.junit.Test;
import uyun.bat.dashboard.api.entity.Dashboard;
import uyun.bat.dashboard.api.entity.TenantResTemplate;
import uyun.bat.web.api.dashboard.entity.MineDashboard;
import uyun.bat.web.api.resource.entity.*;
import uyun.bat.web.impl.testservice.StartService;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class ResourceRESTServiceTest extends StartService{

	ResourceRESTService resourceREST = new ResourceRESTService();
	private static final String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";
	private static final String USER_ID = "94baaadca64344d2a748dff88fe7159e";	

	@Test
	public void testGetResourceTags() {
		Tag tags = resourceREST.getResourceTags(TENANT_ID);
		assertTrue(tags!=null);
	}

	@Test
	public void testSearchResource() {
		String searchValue = null;
		String groupBy = "test";
		int pageSize=0;
		int current=0;
		String sortField = null;
		String sortOrder = null;
		String[] checkValue = new String[1];
		checkValue[0]="在线";
		ResourceList1 list = resourceREST.searchResource(TENANT_ID, searchValue, groupBy, pageSize, current, sortField, sortOrder, checkValue);
		assertTrue(list.getTotalCount()==1);
	}

	@Test
	public void testGetResourceById() {
		String tenantId = null;
		String resourceName = null;
		String appName = "app";
		String resourceId = null;
		int limit = 10;
		String userId = "123";
		MineDashboard md = resourceREST.getResourceById(TENANT_ID,userId, resourceName, appName, resourceId, limit);
	}

	@Test
	public void testGetCircleList() {
		String filterBy = "system.cpu.idle";
		String fillBy = "system.cpu.idle";
		String from = null;
		String to = null;
		CircleList cl=resourceREST.getCircleList(TENANT_ID, filterBy, fillBy, from, to);
		assertTrue(cl.getHosts().get(0).getId().equals("123"));
	}

	@Test
	public void testGetHostIndication() {
		IndicationList is=resourceREST.getHostIndication(TENANT_ID, "system.cpu.used");
		assertTrue(!is.getHosts().isEmpty());
	}

	@Test
	public void testIsResourceExist() {
		//resourceREST.isResourceExist(TENANT_ID);
		assertTrue(resourceREST.isResourceExist(TENANT_ID));
	}

	@Test
	public void testGetResourceDetailById() {
		assertTrue(resourceREST.getResourceDetailById(TENANT_ID, USER_ID)!=null);
		
	}

	@Test
	public void testGetResourceTagList() {
		assertTrue(resourceREST.getResourceTagList(TENANT_ID, USER_ID).get(0).equals("ls"));
		
	}

	@Test
	public void testGetResCountByOnlineStatus() {
		assertTrue(resourceREST.getResCountByOnlineStatus(TENANT_ID).getCounts().get(0).getCount()==1);
	}
	@Test
	public void testGetEventByResId(){
		resourceREST.getEventByResId(TENANT_ID, USER_ID, 10,10);
	}
	
	@Test
	public void testUpdateUserTags(){
		UserTag tags = new UserTag();
		String tenantId = null;
		tags.setResourceId("123");
		tags.setUserTags("userTags");
		resourceREST.updateUserTags(tenantId, tags);
	}
	
	@Test
	public void testQueryAllResTags(){
		resourceREST.queryAllResTags(TENANT_ID);
	}
	
	@Test
	public void testGetResourceByResId(){
		resourceREST.getResourceByResId(TENANT_ID, USER_ID);
	}

	
	@Test
	public void test1GlobalApply() {
		TenantResTemplate template=new TenantResTemplate();
		template.setAppName("system");
		template.setDashId(null);
		template.setResourceId("123");
		template.setTenantId("123");
		String userId= "12345";
		/*MineDashboard md = new MineDashboard();
		List<Dashwindow> dashwindows = new ArrayList<Dashwindow>();
		Dashwindow d = new Dashwindow();
		List<Request> requests=new ArrayList<Request>();
		Request e=new Request();
		e.setQ("[\"avg:system.io.w_s{host:jtv-itsmtz04-t}\",\"avg:system.io.r_s{host:jtv-itsmtz04-t}\"]");
		requests.add(e);
		d.setRequests(requests);
		md.setDashwindows(dashwindows);
		ResourceRESTService.cacheMap.put(EncryptUtil.string2MD5("["+"123"+"],["+"12345"+"],["+"system"+"]"), md);*/
		//resourceREST.globalApply(userId , template);
	}
	
	
	@Test
	public void test2GlobalApply() {
		TenantResTemplate template=new TenantResTemplate();
		template.setAppName("system");
		template.setDashId("123");
		template.setResourceId(null);
		template.setTenantId("456");
		String userId= "12345";
		resourceREST.globalApply(userId , template);
	}

	
	@Test
	public void test3GlobalApply() {
		TenantResTemplate template=new TenantResTemplate();
		template.setAppName("Oracle");
		template.setDashId("123");
		template.setResourceId("123");
		template.setTenantId("123");
		String userId= "12345";
		resourceREST.globalApply(userId , template);
	}
	
	/*@Test
	public void testUpdateTemplate() {
		TenantResTemplate template=new TenantResTemplate();
		template.setAppName("Oracle");
		template.setDashId("123");
		template.setResourceId(null);	//空值表示无自定义模板
		//template.setResourceId("123");	//非空值表示有自定义模板
		template.setTenantId("123");
		Dashwindow dashwindow=new Dashwindow();
		dashwindow.setDashId("123");
		dashwindow.setId("456");
		//dashwindow.setName("system.cpu.idle");
		dashwindow.setName("name");
		Dashwindow dash = resourceREST.updateTemplate(template, dashwindow);
		System.out.println("UpdateTemplate结果："+dash.toString());
	}

	@Test
	public void testCreateTemplate() {
		TenantResTemplate template=new TenantResTemplate();
		template.setAppName("Oracle");
		template.setDashId("123");
		template.setResourceId(null);
		//template.setResourceId("123");
		template.setTenantId("123");
		Dashwindow dashwindow=new Dashwindow();
		dashwindow.setDashId("123");
		dashwindow.setId("456");
		dashwindow.setName("system.cpu.idle");
		Dashwindow dash = resourceREST.createTemplate(template, dashwindow);
		System.out.println("CreateTemplate的结果："+dash.toString());
		
	}
	
	@Test
	public void testDeleteDashwindowTemplate() {
		TenantResTemplate template=new TenantResTemplate();
		template.setAppName("Oracle");
		template.setDashId("123");
		template.setResourceId(null);
		//template.setResourceId("123");
		template.setTenantId("123");
		Dashwindow dashwindow=new Dashwindow();
		dashwindow.setDashId("123");
		dashwindow.setId("456");
		dashwindow.setName("system.cpu.idle");
		resourceREST.deleteDashwindowTemplate(template, dashwindow);
	}

	@Test
	public void testsortTemplate() {
		TenantResTemplate template=new TenantResTemplate();
		template.setAppName("Oracle");
		template.setDashId("123");
		template.setResourceId(null);
		//template.setResourceId("123");
		template.setTenantId("123");
		Dashboard dashboard = new Dashboard();
		dashboard.setId("123");
		dashboard = resourceREST.sortTemplate(template, dashboard);
		System.out.println("sortTemplate的结果："+dashboard.toString());
	}
	*/
	
	@Test
	public void testBuildReation(){
		Dashboard dashboard = new Dashboard();
		Dashboard newd=new Dashboard();
		List<String > list = new ArrayList<String>();
		list.add("1");
		list.add("2");
		list.add("3");
		List<String > list1 = new ArrayList<String>();
		list1.add("4");
		list1.add("5");
		list1.add("6");
		dashboard.setDashwindowIdList(list);
		newd.setDashwindowIdList(list1);
		resourceREST.buildRelation(dashboard, newd);
	}
}
