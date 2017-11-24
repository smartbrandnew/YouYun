package uyun.bat.syndatabase.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.syndatabase.Startup;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.impl.OverviewTagServiceImpl;

public class OverviewTagServiceImplTest {
	
private static OverviewTagServiceImpl overviewTagService = (OverviewTagServiceImpl)Startup.getInstance().getBean("overviewTagService");
	
	@Test
	public void testGetAllTagByKey(){
		overviewTagService.getAllTagByKey("resourceId");
	}
	
	@Test
	public void testupdateTag(){
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag1 = new Tag("D54F07E715B84F6A80EAB58982A53A0D", "resourceId", "newId2");
		Tag tag2 = new Tag("95622AB67001441D81C27B2B24709E89", "resourceId", "newId1");
		tags.add(tag1);tags.add(tag2);
		overviewTagService.updateTag(tags);
	}
	
}
