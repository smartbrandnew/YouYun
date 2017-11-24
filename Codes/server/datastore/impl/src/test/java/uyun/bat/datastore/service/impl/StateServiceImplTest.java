package uyun.bat.datastore.service.impl;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.State;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.datastore.api.service.TagService;
import uyun.whale.common.util.concurrent.ThreadUtil;
import uyun.whale.common.util.number.RandomUtil;

import java.util.Arrays;

import static org.junit.Assert.*;

public class StateServiceImplTest {
	private static final Logger logger = LoggerFactory.getLogger(StateServiceImplTest.class);
	private StateService service = Startup.getInstance().getBean(StateService.class);

	// 数据定义
	private String state = "port.work";
	private String tenantId = "tenantId:" + StateServiceImpl.TAG_VALUE_BAT_TENANT_ID;
	private String[] tags = new String[]{tenantId, "host:router001", "port:eth0/0"};
	private String[] tags1 = new String[]{tenantId, "host:router001", "port:eth0/1"};
	private String[] tags2 = new String[]{tenantId, "host:router001", "port:eth0/2"};

	@Test
	public void testState() {
		cleanCheckpoints();

		// 清理数据
		String tenantIdSystem = StateServiceImpl.TAG_VALUE_BAT_TENANT_ID;
		State[] states = service.getStates(tenantIdSystem);
		for (State state : states)
			service.deleteState(tenantIdSystem, state.getName());

		states = service.getStates(tenantIdSystem);
		assertEquals(0, states.length);

		State statePortWork = new State("port.work");
		String stateId = service.saveState(tenantIdSystem, statePortWork);
		states = service.getStates(tenantIdSystem);
		assertEquals(1, states.length);
		assertEquals(statePortWork, states[0]);
		assertEquals(stateId, states[0].getId());
	}

	private void cleanCheckpoints() {
		State[] states = service.getStates(StateServiceImpl.TAG_VALUE_BAT_TENANT_ID);
		for (State state : states)
			service.deleteCheckpoints(state.getName(), StateServiceImpl.TAG_VALUE_BAT_TENANT_ID);
	}

	@Test
	public void testCheckpoint() {
		// 数据清理
		cleanCheckpoints();

		// 保存与获取测试
		long now = System.currentTimeMillis();
		Checkpoint cp = new Checkpoint(state, now, "up", "port up", tags);
		service.saveCheckpoint(cp);

		Checkpoint cp2 = service.getCheckpoint(state, tags);
		assertEquals(cp.getTimestamp(), cp2.getTimestamp());
		assertEquals(cp.getValue(), cp2.getValue());

		// 变更保存测试
		service.saveCheckpoint(new Checkpoint(state, now + 1000, "down", "port down", tags));
		cp2 = service.getCheckpoint(state, tags);
		assertEquals("down", cp2.getValue());

		// 统计测试
		service.saveCheckpoint(new Checkpoint(state, now + 1000, "up", "port up", tags1));
		service.saveCheckpoint(new Checkpoint(state, now + 1000, "up", "port up", tags2));
		assertEquals(2, service.getCheckpointsCount(state, new String[]{tenantId, "host:router001"}, "up"));
		assertEquals(1, service.getCheckpointsCount(state, new String[]{tenantId, "host:router001"}, "down"));

		// 批量查询测试
		Checkpoint[] cps = service.getCheckpoints(state, new String[]{tenantId});
		assertEquals(3, cps.length);
		assertContains(tags, cps);
		assertContains(tags1, cps);
		assertContains(tags2, cps);

		cps = service.getCheckpoints(state, new String[]{"tenantId:fb32560b3e0911e69bcb0050aaa34519"});
		assertTrue(cps == null || cps.length == 0);

		// 再次转换测试
		service.saveCheckpoint(new Checkpoint(state, now + 2000, "down", "port down again", tags));
		service.saveCheckpoint(new Checkpoint(state, now + 3000, "up", "port up", tags));

		// 检查状态变更历史
		Checkperiod[] periods = service.getCheckperiods(state, tags, now, now + 2001);
		assertEquals(2, periods.length);

		assertEquals("up", periods[0].getValue());
		assertEquals(now, periods[0].getFirstTime());
		assertEquals(now + 1000, periods[0].getLastTime());
		assertEquals(1, periods[0].getCount());
		assertEquals(null, periods[0].getPriorValue());
		assertEquals("port up", periods[0].getDescr());

		assertEquals("down", periods[1].getValue());
		assertEquals(now + 1000, periods[1].getFirstTime());
		assertEquals(now + 3000, periods[1].getLastTime());
		assertEquals(2, periods[1].getCount());
		assertEquals("up", periods[1].getPriorValue());
		assertEquals("port down", periods[1].getDescr());

		// 清除测试
		service.deleteCheckpoints(state, tags);
		cp2 = service.getCheckpoint(state, tags);
		assertEquals(null, cp2);
	}

	@Test
	public void testLastCheckpoint(){
		long now=System.currentTimeMillis();
		service.saveCheckpoint(new Checkpoint(state, now, "down", "port down again", tags));
		service.saveCheckpoint(new Checkpoint(state, now + 2000, "down", "port down again", tags));
		service.saveCheckpoint(new Checkpoint(state, now + 3000, "up", "port up", tags));

		TagService tagService = Startup.getInstance().getBean(TagService.class);
		String objectId=tagService.getObjectId(tags);
		Checkperiod lastCheckperiod=service.getLastCheckperiod(StateServiceImpl.TAG_VALUE_BAT_TENANT_ID,state,objectId);

		assertEquals("up",lastCheckperiod.getValue());
		assertEquals(now+3000,lastCheckperiod.getFirstTime());
		assertEquals("port up",lastCheckperiod.getDescr());

		// 清除测试
		service.deleteCheckpoints(state, tags);
		Checkpoint cp = service.getCheckpoint(state, tags);
		assertEquals(null, cp);
	}

	private void assertContains(String[] tags, Checkpoint[] cps) {
		for (Checkpoint cp : cps) {
			if (equals(tags, cp.getTags()))
				return;
		}
		fail("cps not contains tags: " + Arrays.toString(tags));
	}

	private boolean equals(String[] tags1, String[] tags2) {
		for (String tag1 : tags1) {
			boolean exists = false;
			for (String tag2 : tags2) {
				if (tag1.equals(tag2)) {
					exists = true;
					break;
				}
			}
			if (!exists)
				return false;
		}
		return true;
	}

	@Test
	public void testConcurrent() throws InterruptedException {
		// 数据清理
		cleanCheckpoints();

		long now = System.currentTimeMillis();

		int count = 10;
		Thread[] threads = new Thread[count];
		Throwable[] errors = new Throwable[1];
		{
			StateTester tester = new StateTester(service, now + 1000, tags1, errors);
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

	@Test
	public void testRandomConcurrent() throws InterruptedException {
		// 数据清理
		cleanCheckpoints();

		int count = 10;
		Thread[] threads = new Thread[count];
		Throwable[] errors = new Throwable[1];
		{
			RandomStateTester tester = new RandomStateTester(service, tags1, errors);
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

	/**
	 * 并发测试多个随机状态的更新
	 */
	private static class RandomStateTester implements Runnable {
		private StateService service;
		private String[] tags;
		private Throwable[] errors;

		public RandomStateTester(StateService service, String[] tags, Throwable[] errors) {
			this.service = service;
			this.tags = tags;
			this.errors = errors;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					Thread.sleep(RandomUtil.rand(10));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					service.saveCheckpoint(new Checkpoint("concurrent.state", System.currentTimeMillis(), Integer.toString(RandomUtil.rand(3)), tags));
				} catch (Throwable e) {
					errors[0] = e;
					break;
				}
			}
		}
	}

	/**
	 * 并发测试多个相同状态的更新
	 */
	private static class StateTester implements Runnable {
		private StateService service;
		private long time;
		private String[] tags;
		private Throwable[] errors;

		public StateTester(StateService service, long time, String[] tags, Throwable[] errors) {
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
				service.saveCheckpoint(new Checkpoint("concurrent.state", time, "ok", tags));
			} catch (Throwable e) {
				errors[0] = e;
			}
		}
	}
}
