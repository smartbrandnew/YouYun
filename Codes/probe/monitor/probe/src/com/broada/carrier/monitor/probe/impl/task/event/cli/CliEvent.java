package com.broada.carrier.monitor.probe.impl.task.event.cli;

import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.EventVO;
import com.broada.carrier.monitor.probe.impl.yaml.YamlBean;
import com.broada.carrier.monitor.probe.impl.yaml.YamlHost;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;
import com.broada.numen.agent.script.util.TextUtil;
import com.broada.utils.StringUtil;

public class CliEvent {

	@Autowired
	private ProbeServiceFactory probeFactory;

	private static Logger LOG = LoggerFactory.getLogger(CliEvent.class);

	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private Map<String, Map<String, Object>> time_cache = new HashMap<String, Map<String, Object>>();

	/**
	 * 生成cli事件
	 * @param events
	 */
	public void generateCliEvent(List<EventVO> events, File file){
		Yaml yaml = new Yaml();
		Map<String, String> nodes_map = new HashMap<String, String>();
		Map<String, CcliProtocol> methods_map = new HashMap<String, CcliProtocol>();
		try {
			YamlBean yamlBean = yaml.loadAs(new FileInputStream(file), YamlBean.class);
			List<YamlHost> hosts = yamlBean.getHosts();
			if(hosts != null && hosts.size() > 0)
				for(YamlHost host:hosts)
					nodes_map.put(host.getIp(), host.getCollect_method());
			List<Map<String, Object>> method_list = yamlBean.getCollect_methods();
			if(method_list != null && method_list.size() > 0){
				MonitorMethod[] mms = probeFactory.getMethodService().getMethods();
				if(mms != null && mms.length > 0){
					
				}
				for(Map<String, Object> map:method_list){
					Map<String, Object> props = new HashMap<String, Object>();
					props.put("sessionName", map.get("sessionName"));
					props.put("remotePort", map.get("remotePort"));
					props.put("loginTimeout", map.get("loginTimeout"));
					props.put("prompt", map.get("prompt"));
					props.put("username", map.get("loginName"));
					props.put("password", map.get("password"));
					props.put("sysname", map.get("sysname"));
					CcliProtocol protocol = new CcliProtocol(new Protocol("cli", props));
					if(!nodes_map.isEmpty()){
						for(String ip:nodes_map.keySet()){
							if(map.get("name").toString().equals(nodes_map.get(ip)))
								protocol.setField("ip", ip);
						}
					}
				}
			}
			if(!methods_map.isEmpty()){
				for(String name:methods_map.keySet()){
					generateCliEvent(name, methods_map.get(name), events);
				}
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	/**
	 * 生成Cli重启事件
	 * @param name
	 * @param ccliProtocol
	 * @param events
	 */
	private void generateCliEvent(String name, CcliProtocol protocol, List<EventVO> events) {
		CcliSession session = new CcliSession(protocol);
		session.connect();
		long current_time = System.currentTimeMillis();
		String stat = executeCommand(session);
		session.disconnect();
		if(stat == null) return;
		try {
			long value = format.parse(stat).getTime();
			if(time_cache.get(protocol.getIp()) == null){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("last_collect_time", current_time);
				map.put("last_collect_value", value);
				return ;
			} else{
				Map<String, Object> map = time_cache.get(protocol.getIp());
				long last_collect_time = (Long) map.get("last_collect_time");
				long last_collect_value = (Long) map.get("last_collect_value");
				judgeByCondition(protocol.getIp(), last_collect_time, last_collect_value, current_time, value, events);
				// 刷新本次的采集时间和采集值
				map.put("last_collect_time", current_time);
				map.put("last_collect_value", value);
			}
		} catch (ParseException e) {
			LOG.error("转换时间:{}异常", stat);
		}
	}

	/**
	 * 判断是否产生重启告警事件
	 * @param last_collect_time
	 * @param last_collect_value
	 * @param current_time
	 * @param value
	 * @return
	 */
	private void judgeByCondition(String ip, long last_collect_time, long last_collect_value, 
			long current_time, long value, List<EventVO> events) {
		EventVO event = new EventVO();
		long A3 = Config.getDefault().getProperty("cli.event.overflow", 4294967295l);
		long B3 = Config.getDefault().getProperty("cli.event.tolerance", 60l);
		long uptimeInstance = value < last_collect_value? value - last_collect_value + A3:last_collect_value - value;
		long error = (current_time - last_collect_time) * 100 - uptimeInstance;
		boolean result = error > B3 * 100 || error < (-100 * B3);
		if(!result)
			return ;
		else{
			EventVO ev = new EventVO();
			String host_id = "";
			MonitorNode[] nodes = probeFactory.getNodeService().getNodes();
			if(nodes != null && nodes.length > 0){
				for(MonitorNode node:nodes){
					if(node.getIp().equals(ip))
						host_id = node.getId();
				}
			}
			if(StringUtil.isNullOrBlank(host_id))
				return;
			ev.setId(host_id);       // event host_id
			ev.setName("device:" + ip + " reboot");
			ev.setSource("agentless");
			ev.setMessage("device:" + ip + " reboot");
			ev.setTimestamp(System.currentTimeMillis());
			ev.setType("device reboot event");
			ev.setState("error");   // success, info, warning, error
			events.add(event);
		}
	}

	/**
	 * 执行重启事件查询的命令[待拓展]
	 * @param session
	 * @return
	 */
	// TODO  待拓展
	private String executeCommand(CcliSession session){
		String stat = null;
		String os = session.getParams().getFieldString("sysname");
		if(os.contains("indow")){
			try{
				stat = session.execute("systeminfo | findstr \"系统启用时间:\"");
				String[] lines = TextUtil.splitLine(stat);
				if(lines.length > 0){
					for(String line:lines){
						int position = line.indexOf(":") + 1;
						String[] array = line.substring(position).trim().split(",");
						return array[array.length-1].concat(" ").concat(array[array.length-2]).replace("/", "-");
					}
				}
			}catch (Exception e) {
				LOG.error("获取操作系统启动时间发生异常:{}", e);
			}
		} else if(os.contains("inux")){
			try{
				stat = session.execute("who -b");
				String[] lines = TextUtil.splitLine(stat);
				if(lines.length > 0){
					for(String line:lines){
						String[] array = line.split(" ");
						return array[array.length-1].concat(" ").concat(array[array.length-2]);
					}
				}
			}catch (Exception e) {
				LOG.error("获取操作系统启动时间发生异常:{}", e);
			}
		}
		return null;
	}
	
}
