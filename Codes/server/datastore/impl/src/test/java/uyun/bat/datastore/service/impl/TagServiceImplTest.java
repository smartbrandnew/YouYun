package uyun.bat.datastore.service.impl;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.service.TagService;
import uyun.whale.common.util.concurrent.ThreadUtil;

import static org.junit.Assert.*;

public class TagServiceImplTest {
	private static final Logger logger = LoggerFactory.getLogger(TagServiceImplTest.class);

	@Test
	public void test() {
		TagService service = Startup.getInstance().getBean(TagService.class);

		// 参数设置
		String[] tags1 = new String[]{"tenantId:abc", "biz1", "host:test"};
		String[] tags2 = new String[]{"tenantId:abc", "biz1", "host:test", "device:c:"};
		String[] tags3 = new String[]{"tenantId:abc", "biz2", "host:test", "app:mysql"};

		// 数据清理
		service.deleteTagsAndObjects(tags1);
		service.deleteTagsAndObjects(tags2);
		service.deleteTagsAndObjects(tags3);
		assertNull(service.getObjectId(tags1));

		// 数据保存与获取
		String objectId1 = service.checkObjectId(tags1);
		assertTrue(objectId1 != null);
		assertEquals(objectId1, service.getObjectId(tags1));
		String objectId2 = service.checkObjectId(tags2);
		String objectId3 = service.checkObjectId(tags3);

		// 数据查询
		String[] objectIds = service.queryObjectIds(new String[]{"biz1"});
		assertTrue(objectId1.equals(objectIds[0]) || objectId1.equals(objectIds[1]));
		assertTrue(objectId2.equals(objectIds[0]) || objectId2.equals(objectIds[1]));
		objectIds = service.queryObjectIds(new String[]{"biz2"});
		assertEquals(objectId3, objectIds[0]);
		assertEquals(1, objectIds.length);
		objectIds = service.queryObjectIds(new String[]{"biz2:test"});
		assertEquals(0, objectIds.length);
	}

	@Test
	public void testConcurrent() throws InterruptedException {
		TagService service = Startup.getInstance().getBean(TagService.class);

		String[] tags1 = new String[]{"tenantId:abc", "biz1", "host:test"};
		service.deleteTagsAndObjects(tags1);
		long now = System.currentTimeMillis();

		int count = 10;
		Thread[] threads = new Thread[count];
		Throwable[] errors = new Throwable[1];
		{
			TagTester tester = new TagTester(service, now + 1000, tags1, errors);
			for (int i = 0; i < threads.length; i++)
				threads[i] = ThreadUtil.createThread(tester);
			for (Thread thread : threads)
				thread.start();
			for (Thread thread : threads)
				thread.join();
			if (errors[0] != null) {
				logger.error("failed", errors[0]);
				fail("cannot concurrent");
			}
		}

		{
			String[] result = new String[1];
			ObjectTester tester = new ObjectTester(service, now + 1000, tags1, errors, result);
			for (int i = 0; i < threads.length; i++)
				threads[i] = ThreadUtil.createThread(tester);
			for (Thread thread : threads)
				thread.start();
			for (Thread thread : threads)
				thread.join();
			if (errors[0] != null) {
				logger.error("failed", errors[0]);
				fail("cannot concurrent");
			}
		}
	}

	private static class TagTester implements Runnable {
		private TagService service;
		private long time;
		private String[] tags;
		private Throwable[] errors;

		public TagTester(TagService service, long time, String[] tags, Throwable[] errors) {
			this.service = service;
			this.time = time;
			this.tags = tags;
			this.errors = errors;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				long now = System.currentTimeMillis();
				if (now > time)
					break;
			}
			try {
				service.checkTagIds(tags);
			} catch (Throwable e) {
				errors[0] = e;
			}
		}
	}

	private static class ObjectTester implements Runnable {
		private TagService service;
		private long time;
		private String[] tags;
		private Throwable[] errors;
		private String[] result;

		public ObjectTester(TagService service, long time, String[] tags, Throwable[] errors, String[] result) {
			this.service = service;
			this.time = time;
			this.tags = tags;
			this.errors = errors;
			this.result = result;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				long now = System.currentTimeMillis();
				if (now > time)
					break;
			}
			try {
				String objectId = service.checkObjectId(tags);
				if (result[0] == null)
					result[0] = objectId;
				else if (!result[0].equals(objectId))
					throw new IllegalArgumentException("Cannot get same objectId");
			} catch (Throwable e) {
				errors[0] = e;
			}
		}
	}
}
