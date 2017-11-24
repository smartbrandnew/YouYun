package uyun.bat.gateway.agent.servicetest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.common.tag.util.TagUtil;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.datastore.api.entity.PageResource;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.datastore.api.entity.ResourceDetail;
import uyun.bat.datastore.api.entity.ResourceTag;
import uyun.bat.datastore.api.entity.ResourceType;
import uyun.bat.datastore.api.service.ResourceService;

public class ResourceServiceTest {
	private static Resource queryResById(){
		Resource r =new Resource();
		r.setId("1399323fc67b055fd8f187fa65962a7c");
		r.setAgentId("1399323fc67b055fd8f187fa65962a7c");
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag("key", "value");
		tags.add(tag);
		r.setResTags(TagUtil.listTag2String(tags));
		r.setId("12");
		r.setAgentId("agentId");
		r.setHostname("hostname");
		r.setIpaddr("ipaddr");
		r.setType(ResourceType.NETWORK);
		Date modified = new Date();
		r.setModified(modified);
		List<String> apps = new ArrayList<>();
		r.setApps(apps);
		r.setOnlineStatus(OnlineStatus.ONLINE);
		return r;
	}

	private static boolean updateAsync(){
		return true;
	}

	private static Resource queryResByAgentId(){
		Resource r = new Resource();
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag = new Tag("kay", "value");
		tags.add(tag);
		r.setResTags(TagUtil.listTag2String(tags));
		r.setId("12");
		r.setAgentId("agentId");
		r.setHostname("hostname");
		r.setIpaddr("ipaddr");
		r.setType(ResourceType.NETWORK);
		Date modified = new Date();
		r.setModified(modified);
		List<String> apps = new ArrayList<>();
		r.setApps(apps);
		r.setOnlineStatus(OnlineStatus.ONLINE);
		return r;
	}
	private static PageResource queryResListByCondition(){
		List<Resource> listr = new ArrayList<>();
		listr.add(queryResByAgentId());
		PageResource pageResource = new PageResource(10, listr);
		return pageResource;
	}
	
	private static ResourceDetail queryByResId(){
		ResourceDetail resourceDetail = new ResourceDetail();
		resourceDetail.setAgentDesc("agentDesc");
		resourceDetail.setDetail("detail");
		return resourceDetail;
	}
	public static ResourceService create() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(ResourceService.class);
		Callback callback = new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if ("queryResById".equals(method.getName())) {
					return queryResById();
				}
				if ("updateAsync".equals(method.getName())) {
					return updateAsync();
				}
				if("queryResByAgentId".equals(method.getName())){
					return queryResByAgentId();
				}
				if("queryResListByCondition".equals(method.getName())){
					return queryResListByCondition();
				}
				if("queryByResId".equals(method.getName())){
					return queryByResId();
				}
				return null;
			}
		};

		enhancer.setCallbacks(new Callback[] { callback });
		return (ResourceService) enhancer.create();
	}

}
