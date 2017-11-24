package com.broada.carrier.monitor.common.pool;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestResourceManager {
	@Test
	public void test() {
		TestResourceFactoryImpl factory = new TestResourceFactoryImpl();
		ResourceManager<Integer, String> manager = new ResourceManager<Integer, String>("String对象", 5000, factory);
		
		// 检查正常的申请与释放
		ResourceHandler<Integer, String> rh1 = manager.borrowResource(0);
		assertEquals(1, factory.getCount());
		rh1.returnResource();
		assertEquals(0, factory.getCount());
		rh1.destroyResource();
		assertEquals(0, factory.getCount());
		
		// 检查多个对象
		rh1 = manager.borrowResource(0);		
		ResourceHandler<Integer, String> rh2 = manager.borrowResource(1);
		assertEquals(2, factory.getCount());
		
		// 检查超长时间自动清理
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(0, factory.getCount());
		
		// 检查自动清理后是否可以重复清理
		manager.returnResource(rh1);
		manager.returnResource(rh2);
		assertEquals(0, factory.getCount());
	}
	
	private static class TestResourceFactoryImpl implements ResourceFactory<Integer, String> {
		private int count;
		
		public int getCount() {
			return count;
		}

		@Override
		public synchronized void returnResource(Integer key, String resource) {
			count--;
		}

		@Override
		public synchronized void destroyResource(Integer key, String resource) {
			count--;
		}

		@Override
		public synchronized String borrowResource(Integer key) {
			count++;
			return new String("ssss" + key++);
		}
	}
}