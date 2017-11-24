package com.broada.carrier.monitor.probe.impl.task.event.ipmi;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.Yaml;

import com.broada.carrier.monitor.common.config.BaseConfig;
import com.broada.carrier.monitor.common.util.HostIpUtil;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIException;
import com.broada.carrier.monitor.impl.host.ipmi.sdk.api.IPMIParameter;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.impl.config.Config;
import com.broada.carrier.monitor.probe.impl.openapi.entity.EventVO;
import com.broada.carrier.monitor.probe.impl.util.CommandUtil;
import com.broada.carrier.monitor.probe.impl.util.DateUtil;
import com.broada.carrier.monitor.probe.impl.yaml.YamlBean;
import com.broada.carrier.monitor.probe.impl.yaml.YamlHost;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.utils.StringUtil;

public class IpmiEvent {
	
	@Autowired
	private ProbeServiceFactory probeFactory;
	
	private static Logger LOG = LoggerFactory.getLogger(IpmiEvent.class);
	
	/**
	 * 生成ipmi事件
	 * @param events
	 */
	public void generateIpmiEvent(List<EventVO> events, File file){
		Yaml yaml = new Yaml();
		try {
			YamlBean yamlBean = yaml.loadAs(new FileInputStream(file), YamlBean.class);
			List<YamlHost> hosts = yamlBean.getHosts();
			if(hosts != null && hosts.size() > 0)
				for(YamlHost host:hosts){
					IPMIParameter param = extractIpmiAuthInfo(host, yamlBean.getCollect_methods());
					if(param == null) continue;
					String command = createCommandLine(param, " sel list ");
					events.addAll(ganerateEvent(param, CommandUtil.exec(command), param.getHost()));
				}
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
	/**
	 * 从配置抽取一个ipmi命令参数
	 * @param host
	 * @param methods
	 * @return
	 */
	private IPMIParameter extractIpmiAuthInfo(YamlHost host, List<Map<String, Object>> methods){
		if(StringUtil.isNullOrBlank(host.getIp()) || HostIpUtil.getLocalHost().equals(host.getIp())) return null;
		if(methods == null || methods.size() < 1) return null;
		String dstMethodName = host.getCollect_method();
		IPMIParameter param = new IPMIParameter();
		// 一次仅有一个方法被选中
		for(Map<String, Object> method:methods){
			if(!dstMethodName.equalsIgnoreCase(method.get("name") == null ? "" : method.get("name").toString())) continue;
			if(method.get("type") == null || !method.get("type").toString().equalsIgnoreCase("ProtocolIPMI")) continue;
			if(method.get("username") == null || method.get("password") == null) continue;
			param.setHost(host.getIp());
			param.setUsername(method.get("username").toString());
			param.setPassword(method.get("password").toString());
			if(method.get("level") != null)
				param.setLevel(method.get("level").toString());
		}
		return param;
	}
	
	/**
	 * 创建命令行
	 * @param param
	 * @param command
	 * @return
	 */
	public static String createCommandLine(IPMIParameter param, String command) {
		StringBuilder sb = new StringBuilder();
		BaseConfig config = new BaseConfig();
		String interf = config.getProps().get("ipmi.use.interface", "lanplus");
		sb.append("ipmitool -I ").append(interf);
		sb.append(" -H ").append(param.getHost());
		sb.append(" -U ").append(param.getUsername());
		sb.append(" -P ").append(param.getPassword());
		if (param.getLevel() != null && !param.getLevel().isEmpty())
			sb.append(" -L ").append(param.getLevel());
		if (command != null)
			sb.append(" ").append(command);
		return sb.toString();
	}
	
	/**
	 * 生成事件
	 * @param command
	 * @return
	 */
	private List<EventVO> ganerateEvent(IPMIParameter param, String[] lines, String hostIp){
		List<EventVO> events = new ArrayList<EventVO>();
		if(lines != null && lines.length > 0)
			for(String line:lines){
				String[] array = line.split("\\|");
				Date when = DateUtil.format(array[1].trim() + " " + array[2].trim(), DateUtil.DEFAULT_DATETIME_PATTERN);
				if(System.currentTimeMillis() - Config.getDefault().getProperty("event.intake.period", 5 * 60 * 1000) > when.getTime()) continue; //  过滤过去5分钟以外的事件
				String who = array[3].trim();
				String what = array[4].trim();
				String state = "success";   // 默认值
				boolean recovery = false;   // 是否是发生事件
				if(array.length == 6){
					what += (" " + array[5].trim());
					if(array[5].trim().equals("Deasserted"))
						recovery = true;
					else if(array[5].trim().equals("Asserted"))
						recovery = false;
				}
				if(!what.contains("Deasserted") && !what.contains("Asserted")){
					// 无法识别事件的发生与恢复
					String direction = getEventDetail(param, array[0].trim());
					if(StringUtil.isNullOrBlank(direction))
						continue;     // 无法获知事件的状态(发生还是恢复)
					what += (" " + direction);
					if(direction.equalsIgnoreCase("Deasserted"))
						recovery = true;
					else if(direction.equalsIgnoreCase("Asserted"))
						recovery = false;
				}
				if(recovery) state = "info";   // 恢复事件
				if(!recovery && (what.contains("Critical") || what.contains("NMI")))
					state = "error";
				else if(!recovery && (what.contains("Non-critical") || what.contains("Predictive Failure")))
					state = "warn";
				MonitorNode[] nodes = probeFactory.getNodeService().getNodes();
				if(nodes == null || nodes.length < 1) continue;
				String host_id = null;
				List<String> tags= new ArrayList<String>();
				for(MonitorNode node : nodes){
					if(node.getIp().equals(hostIp)){
						host_id = node.getId();
						tags.add(node.getTags());
					}
				}
				if(StringUtil.isNullOrBlank(host_id)) continue;
				EventVO ev = new EventVO();
				ev.setId(host_id);       // event host_id
				ev.setName("ipmi hardware " + who + " fault ");
				ev.setSource("agentless");  // 服务端未实际使用
				ev.setMessage(what);
				ev.setTimestamp(when.getTime());
				ev.setType("ipmi hardware event");    // 设置的告警条件
				ev.setTags(tags);
				ev.setState(state);   // success, info, warning, error

				events.add(ev);
			}
		return events;
	}
	
	/**
	 * 查询特定事件接口的详情
	 * @param param
	 * @param selEntry
	 * @return 事件的方向(发生还是恢复)
	 */
	private String getEventDetail(IPMIParameter param, String selEntry){
		String direction = "";
		String command = createCommandLine(param, " sel get " + selEntry);
		try {
			String[] lines = CommandUtil.exec(command, 60000, "D:\\uyun\\probe\\monitor\\probe\\ipmitool\\", true);
			if(lines != null && lines.length > 0){
				for(String line:lines){
					if(!line.contains("Event Direction")) continue;
					if(line.contains("Assertion")) direction = "Asserted";
					else if(line.contains("Deassertion")) direction = "Deasserted";
				}
			}
		} catch (IPMIException e) {
			LOG.error(e.getMessage());
		}
		return direction;
	}
}