package monitor.cli.method;

import java.util.HashMap;
import java.util.Map;

import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class CLITest {

	public static void main(String[] args) {
		
		CLIExecutor executor = new CLIExecutor("-1");
		MonitorNode monitorNode = new MonitorNode();
		monitorNode.setIp("10.1.11.228");
		MonitorMethod option = new MonitorMethod();
		Map<String, Object> props = new HashMap<String, Object>();
		props.put("sessionName", "ssh");
		props.put("remotePort", 22);
		props.put("loginTimeout", 10000);
		props.put("prompt", "#");
		props.put("loginName", "root");
//		props.put("username", "root");
		props.put("password", "broada123");
		props.put("sysname", "AIX");
		props.put("waitTimeout", 15000);
		option.setProperties(props);
		CLIResult result = executor.execute(monitorNode, option, "netstat");
		System.out.println(result);
	}

}
