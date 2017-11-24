package uyun.bat.syndatabase.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.syndatabase.Startup;
import uyun.bat.syndatabase.entity.Tag;
import uyun.bat.syndatabase.service.impl.TagServiceImpl;

public class TagServiceImplTest {
	
	private static TagServiceImpl tagService = (TagServiceImpl)Startup.getInstance().getBean("tagService");
	
	@Test
	public void testGetAllTagByKey(){
		tagService.getAllTagByKey("resourceId");
	}
	
	@Test
	public void testupdateTag(){
		List<Tag> tags = new ArrayList<Tag>();
		Tag tag1 = new Tag("D54F07E715B84F6A80EAB58982A53A0D", "resource", "newId1");
		Tag tag2 = new Tag("95622AB67001441D81C27B2B24709E89", "resource", "newId2");
		tags.add(tag1);tags.add(tag2);
		tagService.updateTag(tags);
	}
}
