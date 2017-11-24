package uyun.bat.syndatabase.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.syndatabase.Startup;
import uyun.bat.syndatabase.entity.ResIdTransform;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.impl.ResourceServiceImpl;

public class ResourceServiceImplTest {
	
	private static ResourceServiceImpl resourceService = (ResourceServiceImpl)Startup.getInstance().getBean("resourceService");
	
	@Test
	public void testGetAllTagByKey(){
		resourceService.getAllTagByKey("resourceId");
	}
	
	@Test
	public void testUpdateTag(){
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag2 = new Tag("95622AB67001441D81C27B2B24709E89", "resource", "95622AB67001441D81C27B2B24709E8A");
		tags.add(tag2);
		resourceService.updateTag(tags);
	}
	
	@Test
	public void testGetAllResId() {
		resourceService.getAllResId();
	}

	@Test
	public void updateResId() {
		List<ResIdTransform> trans = new ArrayList<ResIdTransform>();
		ResIdTransform tran = new ResIdTransform();
		tran.setOldResId("95622AB67001441D81C27B2B24709E89");
		tran.setNewResId("95622AB67001441D81C27B2B24709E8A");
		trans.add(tran);
		resourceService.updateResId(trans);
	}
	
}
