package uyun.bat.datastore.entity;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import uyun.bat.datastore.api.entity.RelativeTime;
import uyun.bat.datastore.api.entity.TimeUnit;

public class TagQueryTest {

	@Test
	public void testSetRelativeTime() {
		TagQuery query = new TagQuery(new RelativeTime(24, TimeUnit.HOURS), "cpu", UUID.randomUUID().toString());
		query.setRelativeTime(24, TimeUnit.HOURS);
		Assert.assertEquals(query.getStart_relative().getValue(), 24);
	}

	@Test
	public void testGetStart_relative() {
		TagQuery query = new TagQuery(new RelativeTime(24, TimeUnit.HOURS), "cpu", UUID.randomUUID().toString());
		query.setRelativeTime(24, TimeUnit.HOURS);
		Assert.assertEquals(query.getStart_relative().getValue(), 24);
	}

	@Test
	public void testToJsonString() {
		TagQuery query = new TagQuery(new RelativeTime(24, TimeUnit.HOURS), "cpu", UUID.randomUUID().toString());
		Assert.assertNotNull(query.toJsonString());
	}

}
