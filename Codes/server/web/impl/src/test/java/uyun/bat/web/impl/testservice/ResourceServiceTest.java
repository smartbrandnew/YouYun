package uyun.bat.web.impl.testservice;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.datastore.api.entity.*;
import uyun.bat.datastore.api.service.ResourceService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public  class ResourceServiceTest implements ResourceService{

	public boolean insertAsync(Resource resource) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean updateAsync(Resource resource) {
		// TODO Auto-generated method stub
		return false;
	}


	
	public long insert(List<Resource> resources) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Resource> queryAllRes(String tenantId, boolean isContainNetwork) {
		// TODO Auto-generated method stub
		Resource resource = new Resource();
		resource.setResourceTypeName(tenantId);
		List<String> apps = new ArrayList<String>();
		apps.add("123");
		resource.setApps(apps);
		String key = "123";
		resource.setIpaddr(key);
		String hostname = "11111";
		resource.setHostname(hostname);
		String id = "123";
		resource.setId(id);
		resource.setOnlineStatus(OnlineStatus.ONLINE);
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag(key, key);
		tags.add(tag);
		resource.setResTags(TagUtil.listTag2String(tags));
		List<Resource> resources = new ArrayList<Resource>();
		resources.add(resource);
		return resources;
	}

	@Override
	public List<Tag> queryResTags(String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public PageResourceGroup queryByFilterAndGroupByTag(String tenantId, String filter, String groupBy, int pageNo,
			int size, OnlineStatus onlineStatus) {
		Resource resource =new Resource();
		resource.setResourceTypeName(groupBy);
		resource.setOnlineStatus(OnlineStatus.ONLINE);
		resource.setId("123");
		AlertStatus alertStatus = AlertStatus.OK;
		resource.setAlertStatus(alertStatus);
		resource.setHostname("hostname");
		resource.setOs("os");
		List<String>apps = new ArrayList<>();
		apps.add("test");
		resource.setApps(apps);
		ResourceGroup group=new ResourceGroup("计算机设备");
		group.addResource(resource);
		List<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
		resourceGroups.add(group);
		PageResourceGroup prg=new PageResourceGroup(size, resourceGroups);
		return prg;
	}

	@Override
	public List<String> queryResTagNames(String tenantId) {
		List<String> list=new ArrayList<String>();
		list.add("123");
		return list;
	}

	
	public Resource queryResById(String id) {
		Resource r=new Resource();
		r.setHostname(id);
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag("producer", "machine");
		tags.add(tag);
		r.setResTags(TagUtil.listTag2String(tags));
		List<String> apps = new ArrayList<>();
		apps.add("host");
		r.setApps(apps);
		r.setOs("windos");
		r.setHostname("hostname");
		r.setResourceTypeName("metric");
		r.setIpaddr("120.0.0.1");
		OnlineStatus onlineStatus=OnlineStatus.ONLINE;
		r.setOnlineStatus(onlineStatus);
		r.setResourceTypeName("网络设备");
		r.setId("123");
		r.setAlertStatus(AlertStatus.OK);
		return r;
	}

	
	public Resource queryResByAgentId(String agentId, String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public PageResource queryByKey(String tenantId, String key, int pageNo, int size, OnlineStatus onlineStatus) {
		// TODO Auto-generated method stub
		PageResource qbk = new PageResource(pageNo, null);
		List<Resource> resources = new ArrayList<Resource>();
		Resource r=new Resource();
		List<String> apps=new ArrayList<String>();
		apps.add("123");
		r.setApps(apps);
		r.setIpaddr(key);
		String hostname="11111";
		r.setHostname(hostname);
		String id="123";
		r.setId(id);
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag(key, key);
		tags.add(tag);
		r.setResTags(TagUtil.listTag2String(tags));
		resources.add(r);
		qbk.setResources(resources);
	
		return qbk;
	}

	
	public PageResource queryByKeyAndSortBy(String tenantId, String filter, ResourceOrderBy orderBy, int pageNo,
			int size, OnlineStatus onlineStatus) {
		// TODO Auto-generated method stub
		Resource resource =new Resource();
		resource.setResourceTypeName(tenantId);
		List<Resource> resources=new ArrayList<Resource>();
		resources.add(resource);
		PageResource pr =new PageResource(size, resources);
		return pr;
	}

	
	public PageResource queryAllRes(String tenantId, int pageNo, int pageSize, OnlineStatus onlineStatus) {
		// TODO Auto-generated method stub
		Resource resource =new Resource();
		resource.setResourceTypeName(tenantId);
		AlertStatus alertStatus=AlertStatus.OK;
		resource.setAlertStatus(alertStatus);
		resource.setOnlineStatus(onlineStatus);
		List<Resource> resources=new ArrayList<Resource>();
		resources.add(resource);
		PageResource pr =new PageResource(pageNo, resources);
		return pr;
	}


	@Override
	public List<SimpleResource> query(OnlineStatus onlineStatus, long lastCollectTime) {
		return null;
	}

	public boolean saveResourceDetail(ResourceDetail resourceDetail) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public ResourceDetail queryByResId(String resId) {
		// TODO Auto-generated method stub
		ResourceDetail rd= new ResourceDetail();
		String agentDesc="robot";
		rd.setAgentDesc(agentDesc);
		String detail="none";
		rd.setDetail(detail);
		rd.setResourceId(resId);
		return rd;
	}

	
	public boolean deleteResourceDetail(String resId) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public PageResource queryResListByCondition(ResourceOpenApiQuery query) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<ResourceCount> getResCountByDate(Date startTime, Date endTime) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<ResourceCount> getResCount() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<ResourceCount> getResCountByOnlineStatus(OnlineStatus status) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getResTagsByTag(String tenantId, String tags) {
		// TODO Auto-generated method stub
		List<String> ls = new ArrayList<String>();
		ls.add("ls");
		return ls;
	}

	@Override
	public List<ResourceStatusCount> getResStatusCount(String tenantId) {
		// TODO Auto-generated method stub
		ResourceStatusCount rsc=new ResourceStatusCount();
		rsc.setCount(1);
		List<ResourceStatusCount> list=new ArrayList<ResourceStatusCount>();
		list.add(rsc);
		return list;
	}

	@Override
	public List<SimpleResource> queryByTenantIdAndTags(String tenantId, List<Tag> tags) {
		return null;
	}

	@Override
	public List<String> queryAllResTags(String tenantId) {
		return null;
	}
	
	public long insertAsync(List<Resource> resources, String tenantId) {
		return 0;
	}


	@Override
	public boolean saveResourceSync(Resource resource) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean saveResourceSyncOnly(Resource resource) {
		return false;
	}

	@Override
	public Resource queryResById(String id, String tenantId) {
		Resource r=new Resource();
		r.setHostname(id);
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag("producer", "machine");
		tags.add(tag);
		r.setResTags(TagUtil.listTag2String(tags));;
		List<String> apps = new ArrayList<>();
		apps.add("host");
		r.setApps(apps);
		r.setOs("windos");
		r.setHostname("hostname");
		r.setResourceTypeName("metric");
		r.setIpaddr("120.0.0.1");
		OnlineStatus onlineStatus=OnlineStatus.ONLINE;
		r.setOnlineStatus(onlineStatus);
		r.setResourceTypeName("网络设备");
		r.setId("123");
		r.setAlertStatus(AlertStatus.OK);
		return r;
	}


	@Override
	public boolean delete( String tenantId, String resourceId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Map<String, String>> queryResTagsByMetrics(String tenantId, List<String> metrics) {
		return null;
	}

	@Override
	public List<Resource> queryTenantResByTags(String tenantId, List<String> resTags, String sortField, String sortOrder) {
		return null;
	}

	@Override
	public List<Resource> queryResourcesByIpaddrs(String tenantId, List<String> ipaddrs) {
		return null;
	}

	@Override
	public void updateUserTagsBatch(List<Resource> list) {

	}

	@Override
	public String resIdTransform(String tenantId, String resId) {
		return null;
	}
}
