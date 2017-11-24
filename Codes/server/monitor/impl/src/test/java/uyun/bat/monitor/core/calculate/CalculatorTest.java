package uyun.bat.monitor.core.calculate;


import org.junit.Test;

import uyun.bat.monitor.core.logic.ConstantDef;
import uyun.bat.monitor.impl.Startup;
import uyun.bat.monitor.impl.common.RedisConnectionPool;

public class CalculatorTest {
	
	@Test
	public void testCalculatorManager(){
		CalculatorManager cManager = CalculatorManager.getInstance();
		cManager.setStateMetricConsumers(1);
		cManager.setResourceConsumers(1);
		cManager.setMetricConsumers(1);
		cManager.setEventConsumers(1);
		System.out.println(cManager.toString());
		
		RedisConnectionPool redisConnectionPool = Startup.getInstance().getBean(RedisConnectionPool.class);
		redisConnectionPool.getResource();
		cManager.pushToMetricQueue("1","1");
		cManager.pushToEventQueue(ConstantDef.TENANT_ID, ConstantDef.TEST_ID,ConstantDef.TEST_TITLE,ConstantDef.TEST_CONTENT,true,ConstantDef.TEST_RESID);
		cManager.pusthToResourceQueue("1","1","1",(short) 1,1,"1","1");
		cManager.pusthToStateMetricQueue("1","1");
	}
	
	@Test
	public void testCustomQueue(){
		CustomQueue customQueue = new CustomQueue("qName");
		customQueue.push("123");
		customQueue.pop();	
		System.out.println(customQueue.toString());
	}

}
