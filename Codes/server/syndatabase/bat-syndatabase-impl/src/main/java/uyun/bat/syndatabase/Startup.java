package uyun.bat.syndatabase;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.spring.SpringStartup;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.syndatabase.service.ServiceManager;

public class Startup extends SpringStartup {
	
	private static final Logger LOG = LoggerFactory.getLogger(Startup.class);
	
	private static Startup instance;

	public static Startup getInstance() {
		if (instance == null) {
			synchronized (Startup.class) {
				if (instance == null)
					instance = new Startup();
			}
		}
		return instance;
	}

	public Startup() {
		super("SynDatabase", "classpath:uyun/bat/syndatabase/spring.xml");
	}

	public static void main(String[] args) {
		getInstance().startup();
		ServiceManager manager = (ServiceManager)instance.getBean("serviceManager");
		manager.updateResId();
//		Queue<Resource> queue_success = manager.getSuccessQueue();
		Queue<Resource> queue_fail = manager.getFailQueue();
		while(queue_fail != null && queue_fail.size() > 0){
//			Resource res = queue_success.poll();
//			try{
//				manager.synDatabase(res);
//			}catch(Exception e){
//				LOG.info("同步数据库中对应资源ID为" + res.getId() + "的记录异常");
//				queue_success.add(res);
//			}
		}
		manager.setShould_exist(true);
		LOG.info("同步数据成功~");
		System.exit(1);
	}
	
}
