package uyun.bat.monitor.core.mq;

import java.util.List;

public interface Consumer {
	void doConsume(String tenantId, List<MQData> mqData);
}
