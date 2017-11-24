package uyun.bat.monitor.core.util;


import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.monitor.core.entity.TagEntry;

public class TagUtilTest {

	@Test
	public void testGenerateTags() {
		List<TagEntry> tags = new ArrayList<TagEntry>();
		tags.add(new TagEntry("host", "testHost"));
		tags.add(new TagEntry("host", null));
		tags.add(new TagEntry("host", "testHost"));
		tags.add(new TagEntry("host", "testHost"));
		tags.add(new TagEntry("host", null));
		tags.add(new TagEntry("host", "testHost1"));
		tags.add(new TagEntry("host", "testHost2"));
		TagUtil.generateTags(tags);
	}

}
