
package uyun.bat.monitor.core.mq;
import java.util.List;

import org.junit.Test;

public class ConsumerManagerTest {

	@Test
	public void test() {
		Consumer consumer = new Consumer() {
			
			@Override
			public void doConsume(String tenantId, List<MQData> mqData) {
				// TODO Auto-generated method stub
				
			}
		};
		ConsumerManager consumerManager = new ConsumerManager("type", consumer, 5, 3);
		MQData data = new MQData() {
			
			@Override
			public String getTenantId() {
				// TODO Auto-generated method stub
				return null;
			}
		};
		consumerManager.push(data);
	}

}
