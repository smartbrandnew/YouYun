package com.broada.carrier.monitor.probe.impl.task.resource.detail.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.broada.carrier.monitor.common.net.IPUtil;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.probe.impl.openapi.entity.CPU;
import com.broada.carrier.monitor.probe.impl.openapi.entity.DeviceDetail;
import com.broada.carrier.monitor.probe.impl.openapi.entity.FileSystem;
import com.broada.carrier.monitor.probe.impl.openapi.entity.Memory;
import com.broada.carrier.monitor.probe.impl.openapi.entity.Network;
import com.broada.carrier.monitor.probe.impl.openapi.entity.Platform;
import com.broada.carrier.monitor.probe.impl.openapi.entity.ResourceDetail;
import com.broada.carrier.monitor.probe.impl.task.resource.detail.Provider;
import com.broada.carrier.monitor.probe.impl.yaml.ResourceType;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.cid.action.api.entity.Protocol;
import com.broada.cid.action.protocol.impl.ccli.CcliProtocol;
import com.broada.cid.action.protocol.impl.ccli.CcliSession;
import com.broada.numen.agent.script.util.TextUtil;
import com.broada.utils.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CLIProvider implements Provider{
	
	private ProbeServiceFactory probeFactory;
	
	public CLIProvider() {
		// TODO Auto-generated constructor stub
	}
	
	public CLIProvider(ProbeServiceFactory probeFactory) {
		this.probeFactory = probeFactory;
	}
	
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public List<ResourceDetail> getResourceDetail() throws Exception {
		MonitorMethod[] methods = probeFactory.getMethodService().getMethods();
		if(methods == null || methods.length < 1)
			return null;
		MethodFacade methods_list = new MethodFacade();
		for(MonitorMethod method:methods){
			if(!method.getTypeId().equalsIgnoreCase("ProtocolCcli") 
					|| (!method.getProperties().get("sysname").toString().startsWith("AIX")))
				continue;
			methods_list.add(method);
		}
		Map<MonitorMethod, MonitorNode> map = new HashMap<MonitorMethod, MonitorNode>();
		MonitorTask[] tasks = probeFactory.getTaskService().getTasks();
		if(tasks == null || tasks.length < 1){
			methods_list = null;
			return null;
		}
		for(MonitorTask task:tasks){
			MonitorMethod method = methods_list.getMethodByCode(task.getMethodCode());
			if(method != null){
				MonitorNode node = probeFactory.getNodeService().getNode(task.getNodeId());
				if(node != null)
					map.put(method, node);
			}
		}
		List<ResourceDetail> details = new ArrayList<ResourceDetail>();
		if(!map.isEmpty()){
			for(MonitorMethod method:map.keySet()){
				ResourceDetail resourcedetail = new ResourceDetail();
				String hostId = map.get(method).getId();
				String hostName = map.get(method).getName();
				resourcedetail.setHost_id(hostId);
				resourcedetail.setHost_name(hostName);
				if(details.contains(resourcedetail)){
					continue;
				}
				DeviceDetail detail = new DeviceDetail();
				method.getProperties().put("username", method.getProperties().get("loginName"));
				CcliProtocol protocol = new CcliProtocol(new Protocol("cli", method.getProperties()));
				protocol.setField("ip", map.get(method).getIp());
				CcliSession session = new CcliSession(protocol);
				session.connect();
				MonitorNode node = map.get(method);
				String type = null;
				if(StringUtil.isNullOrBlank(node.getTypeId()) ||
						node.getTypeId().equals(ResourceType.SERVER.getCode()))
					type = ResourceType.SERVER.getCode();
				else
					type = node.getTypeId();
				resolvePlatform(detail, session, type);
				resolveFileSystem(detail, session);
				resolveCPU(detail, session);
				resolveMemory(detail, session);
				resolveNetwork(detail, session);
				session.disconnect();
				resourcedetail.setDetails(mapper.writeValueAsString(detail));
				details.add(resourcedetail);
			}
		}
		return details;
	}
	
	static class MethodFacade{
		Set<MonitorMethod>  methods = new HashSet<MonitorMethod>();
		
		public void add(MonitorMethod method){
			methods.add(method);
		}
		
		MonitorMethod getMethodByCode(String code){
			for(MonitorMethod method:methods){
				if(method.getCode().equalsIgnoreCase(code))
					return method;
			}
			return null;
		}
	}
	
	/**
	 * 解析platform字段
	 * @param detail
	 * @param session
	 * @return
	 */
	private void resolvePlatform(DeviceDetail detail, CcliSession session, String type){
		String stat = session.execute("echo `uname -v` `oslevel` `uname -M` `bootinfo -K` `uname -s` `prtconf|grep 'Host Name'|awk '{print $3}'`");
		String[] lines = TextUtil.splitLine(stat);
		if(lines != null && lines.length > 0){
			String[] array = lines[1].split(" ");
			Platform platform = new Platform(array[5], array[0], type, array[3], array[4], array[1], array[2]);
			detail.setPlatform(platform);
		}
	}
	
	/**
	 * 解析fileSystem字段
	 * @param detail
	 * @param session
	 */
	private void resolveFileSystem(DeviceDetail detail, CcliSession session){
		String stat = session.execute("df");
		String[] lines = TextUtil.splitLine(stat);
		for(int i=1; i<lines.length; i++){
			String[] array = lines[i].split(" ");
			List<String> list = new ArrayList<String>();
			for(String arr:array){
				if(!StringUtil.isNullOrBlank(arr))
					list.add(arr);
			}
			String kb_size = list.get(1);
			if(kb_size.contains("-")) continue;
			detail.addFileSystem(new FileSystem(list.get(6), kb_size, list.get(0)));
		}
	}
	
	/**
	 * 解析cpu字段
	 * @param detail
	 * @param session
	 */
	private void resolveCPU(DeviceDetail detail, CcliSession session){
		String count = session.execute("echo `smtctl | grep 'Bind processor' | wc -l`");
		String info = session.execute("prtconf");
		String[] lines = TextUtil.splitLine(info);
		String cpu_cores = "";
		String process_type = "";
		String clock_speed = "";
		for(int i=1; i<lines.length; i++){
			if(lines[i].contains("Number Of Processors")){
				String array[] = lines[i].split(":");
				cpu_cores = array[1].trim();
			} else if(lines[i].contains("Processor Clock Speed")){
				String array[] = lines[i].trim().split(":");
				clock_speed = array[1].replace(" MHz", "").trim();
			} else if(lines[i].contains("Processor Type")){
				String array[] = lines[i].split(":");
				process_type = array[1];
			} 
		}
		detail.setCpu(new CPU(Integer.valueOf(count), cpu_cores, clock_speed, process_type));
	}
	
	/**
	 * 解析memory字段
	 * @param detail
	 * @param session
	 */
	private void resolveMemory(DeviceDetail detail, CcliSession session){
		String stat = session.execute("prtconf");
		String total = "";
		String swap = "";
		String[] lines = TextUtil.splitLine(stat);
		for(int i=1; i<lines.length; i++){
			if(lines[i].contains("Total Paging Space")){
				String array[] = lines[i].split(":");
				swap = array[1].replace("MB", "").trim();
				swap = String.valueOf(Long.valueOf(swap).longValue() * 1024).concat("kB");
			} else if(lines[i].contains("Good Memory Size")){
				String array[] = lines[i].split(":");
				total = array[1].replace("MB", "").trim();
				total = String.valueOf(Long.valueOf(total).longValue() * 1024).concat("kB");
			}
			detail.setMemory(new Memory(swap, total));
		}
	}
	
	/**
	 * 解析network字段
	 * @param detail
	 * @param session
	 */
	private void resolveNetwork(DeviceDetail detail, CcliSession session){
		Set<String> nics = new HashSet<String>();
		String stat = session.execute("echo `lsdev -Cc if | grep 'Available' | grep 'en'| awk '{print $1}'`");
		String[] lines = TextUtil.splitLine(stat);
		for(int i=1; i<lines.length; i++){
			nics.add(lines[i]);
		}
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		// 网卡和地址绑定
		for(String nic:nics){
			List<String> ips = new ArrayList<String>();
			stat = session.execute("lsattr -El " + nic);
			lines = TextUtil.splitLine(stat);
			String ipv4 = "";
			String ipv6 = "";
			for(int i=1; i<lines.length; i++){
				if(lines[i].contains("netaddr") && !lines[i].contains("netaddr6")){
					String array[] = TextUtil.splitWord(lines[i]);
					ipv4 = array[1].trim();
				} else if(lines[i].contains("netaddr6")){
					String array[] = TextUtil.splitWord(lines[i]);
					ipv6 = array[1].trim();
				}
			}
			ips.add(IPUtil.isIPv4Address(ipv4)==true? ipv4:"");
			ips.add(IPUtil.isIPv6Address(ipv6)==true? ipv6:"");
			map.put(nic, ips);
		}
		// 网卡和mac绑定
		List<Network> networks = new ArrayList<Network>();
		for(String nic:nics){
			stat = session.execute("echo `lscfg -vp -l " + nic.replace("en", "ent") + "`");
			lines = TextUtil.splitLine(stat);
			for(int i=0; i<lines.length; i++){
				String flag = "Network Address.............";
				int index = lines[i].indexOf(flag);
				Network network = new Network(nic, lines[i].substring(index + flag.length(), index + flag.length() + 12), 
						map.get(nic).get(0), map.get(nic).get(1)) ;
				networks.add(network);
			}
		}
		detail.setNetwork(networks);
	}
	
}
