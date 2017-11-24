package uyun.bat.common.tag;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uyun.bat.common.tag.entity.Tag;
import uyun.bat.common.tag.util.TagUtil;

public class TagTest {

	@Test
	public void test() {
		Tag tag = new Tag();
		tag.setKey("a");
		tag.setValue("b");
		assertTrue("a:b".equals(tag.toString()));
	}

	@Test
	public void test1() {
		Tag tag = TagUtil.string2Tag("a:b");
		assertTrue("a:b".equals(tag.toString()));
		tag = TagUtil.string2Tag("a");
		assertTrue("a:".equals(tag.toString()));
	}

	@Test
	public void test2() {
		String tags = TagUtil.array2String(new String[0]);
		assertTrue("".equals(tags));
		String[] ts = new String[] { "a:b", "c:d" };
		tags = TagUtil.array2String(ts);
		assertTrue("a:b;c:d".equals(tags));

		ts = new String[] { "a", "c" };
		tags = TagUtil.array2String(ts);
		assertTrue("a;c".equals(tags));
	}

	@Test
	public void test3() {
		List<String> ts = new ArrayList<String>();
		ts.add("a:b");
		ts.add("c:d");

		assertTrue("a:b;c:d".equals(TagUtil.list2String(ts)));
	}

	@Test
	public void test4() {

		assertTrue(2 == TagUtil.string2List("a:;b:").size());
	}

}
