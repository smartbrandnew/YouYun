package com.broada.carrier.monitor.client.impl.impexp;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpFile;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpNode;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpResource;
import com.broada.carrier.monitor.client.impl.impexp.entity.ImpExpTask;
import com.broada.carrier.monitor.client.impl.impexp.entity.Log;
import com.broada.carrier.monitor.client.impl.impexp.entity.LogLevel;
import com.broada.carrier.monitor.client.impl.impexp.entity.MapLibrary;
import com.broada.carrier.monitor.client.impl.impexp.util.Logger;
import com.broada.carrier.monitor.client.impl.impexp.util.TableUtil;
import com.broada.carrier.monitor.common.util.TextUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorPolicy;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.common.db.DBUtil;
import com.broada.common.db.Table;
import com.broada.utils.ReversibleEncrypt;

public class ExporterNumen implements Exporter {
	private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
	private ImpExpFile file = new ImpExpFile();
	private Connection conn;
	private String url;
	private String username;
	private String password;
	private Pattern encryptedPattern = Pattern.compile("^\\[(.+?)\\]==$");
	private ReversibleEncrypt encrypt = ReversibleEncrypt.create("monitorMethodOptions");
	private Map<Integer, ImpExpNode> nodeMap = new HashMap<Integer, ImpExpNode>();
	private Map<Integer, ImpExpResource> resourceMap = new HashMap<Integer, ImpExpResource>();
	private Map<Integer, String> methodMap = new HashMap<Integer, String>();
	private Map<Integer, String> policyMap = new HashMap<Integer, String>();
	
	public ExporterNumen(String ip, int port, String sid, String username, String password) {
		this.url = String.format("jdbc:oracle:thin:@%s:%d:%s", ip, port, sid);
		this.username = username;
		this.password = password;
	}

	public ExporterNumen(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public ImpExpFile exp() {
		conn = DBUtil.createConnection(DRIVER, url, username, password);
		try {
			expMethods();
			expPolicies();
			expProbes();
			expNodes();			
			expTasks();
		} finally {
			DBUtil.close(conn);
		}		
		return file;
	}

	private void expTasks() {
		Table table = DBUtil.queryForTable(conn, "select * from srvmonitor_service order by node_id, res_id");
		for (int i = 0; i < table.getRowCount(); i++) {
			int id = TableUtil.checkInteger(table, i, "SRV_ID");
			int nodeId = TableUtil.checkInteger(table, i, "NODE_ID");
			int resourceId = TableUtil.checkInteger(table, i, "RES_ID");
			String typeId = TableUtil.checkString(table, i, "TYPE_ID");
			String name = TableUtil.checkString(table, i, "SRV_NAME");		
			String parameter = TableUtil.get(table, i, "PARAMETER", "");
			boolean enabled = TableUtil.checkInteger(table, i, "ENABLED") == 1;
			
			typeId = checkTaskType(typeId);
			if (typeId.equalsIgnoreCase(Const.VALUE_IGNORE))
				continue;
			
			//序号	监测节点IP	监测资源名称	类型	名称	监测方法编码	监测策略编码
			MonitorNode node = nodeMap.get(nodeId);
			if (node == null)
				continue;
			
			ImpExpResource resource = resourceMap.get(resourceId);
			
			String methodCode = null;
			String text = TextUtil.between(parameter, "monitorMethodParamId=\"", "\"");
			if (!TextUtil.isEmpty(text)) {
				int methodId = Integer.parseInt(text);
				methodCode = methodMap.get(methodId);
			}
			
			String policyCode = policyMap.get(id);
			
			ImpExpTask task = new ImpExpTask();
			
			task.setNodeIp(node.getIp());
			task.setResourceName(resource == null ? null : resource.getName());
			task.setTypeId(typeId);
			task.setName(name);
			task.setMethodCode(methodCode);
			task.setPolicyCode(policyCode);
			task.setEnabled(enabled);
					
			file.add(task);	
			
			expInstances(id, task, parameter);
		}
		
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测任务 %d 个。", file.getTasks().size())));
	}

	private void expInstances(int numenTaskId, ImpExpTask task, String parameter) {
		Table table = DBUtil.queryForTable(conn, "select * from srvmonitor_instance where srv_id = ?", numenTaskId);
		for (int i = 0; i < table.getRowCount(); i++) {			
			String key = TableUtil.checkString(table, i, "INST_KEY");
			String name = TableUtil.get(table, i, "INST_NAME", key);	
			MonitorInstance instance = new MonitorInstance(key, name);
			if (task.getTypeId().equalsIgnoreCase("CPU")
					|| task.getTypeId().equalsIgnoreCase("RAM")) {
				if (parameter != null) {
					String expr = TextUtil.between(parameter, "desc=\"", "\"");
					if (expr != null && expr.contains("."))
						instance.setExtra(expr);
				}
			}			
			task.add(instance);
		}
	}

	private String checkTaskType(String typeId) {
		String result = MapLibrary.getDefault().get("numen.monitor.type." + typeId);
		if (result == null)
			result = typeId;
		return result;
	}

	private void expNodes() {
		Table table = DBUtil.queryForTable(conn, "select * from srvmonitor_node");
		for (int i = 0; i < table.getRowCount(); i++) {
			int id = TableUtil.checkInteger(table, i, "NODE_ID");
			String name = TableUtil.checkString(table, i, "NODENAME");
			String ip = TableUtil.checkString(table, i, "NODEADDR");
			String typeId = TableUtil.checkString(table, i, "NODE_RES_TYPE");
			String probeCode = TableUtil.checkString(table, i, "PROBE_CODE");		
			boolean virtual = TableUtil.checkInteger(table, i, "VIRTUAL") == 1;
			
			if (virtual)
				typeId = "VM";
			else {
				typeId = checkNodeType(typeId);
				if (typeId.equals(Const.VALUE_IGNORE))
					continue;
			}
			
			ImpExpNode node = new ImpExpNode();			
			node.setName(name);
			node.setIp(ip);
			node.setTypeId(typeId);
			node.setProbeCode(probeCode);
			node = file.add(node);			
			expResources(id, node);
			nodeMap.put(id, node);
		}
		
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测节点 %d 个。", file.getNodes().size())));
	}
	
	private void expResources(int numenNodeId, ImpExpNode node) {
		if (node.getTypeId().equals("NetDev") || node.getTypeId().equals("SecDev"))
			return;
		
		Table table = DBUtil.queryForTable(conn, "select * from sm_resource where node_id = ?", numenNodeId);
		for (int i = 0; i < table.getRowCount(); i++) {
			int id = TableUtil.checkInteger(table, i, "RES_ID");
			String name = TableUtil.checkString(table, i, "NAME");
			String typeId = TableUtil.checkString(table, i, "RES_TYPE_ID");
			
			typeId = checkResourceType(typeId);
			if (typeId.equals(Const.VALUE_IGNORE))
				continue;
			
			ImpExpResource resource = new ImpExpResource();			
			resource.setName(name);
			resource.setTypeId(typeId);
			node.addResource(resource);
			resourceMap.put(id, resource);
		}
	}

	private String checkNodeType(String typeId) {
		return MapLibrary.getDefault().check("numen.node.type." + typeId);
	}	

	private String checkResourceType(String typeId) {
		return MapLibrary.getDefault().check("numen.resource.type." + typeId);
	}

	private void expProbes() {
		Table table = DBUtil.queryForTable(conn, "select * from sm_probe");
		for (int i = 0; i < table.getRowCount(); i++) {
			String code = TableUtil.checkString(table, i, "CODE");
			String name = TableUtil.checkString(table, i, "NAME");
			String descr = TableUtil.get(table, i, "DESCR", "");
			String srvaddr = TableUtil.checkString(table, i, "SRVADDR");
			int srvport = TableUtil.checkInteger(table, i, "SRVPORT");
			
			MonitorProbe probe = new MonitorProbe();			
			probe.setCode(code);			
			probe.setName(name);
			probe.setDescr(descr);
			probe.setHost(srvaddr);
			probe.setPort(srvport);
					
			file.add(probe);	
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测探针 %d 个。", file.getProbes().size())));
	}

	private void expPolicies() {
		Table table = DBUtil.queryForTable(conn, "select srv_id, interval, erratuminterval from srvmonitor_service");
		for (int i = 0; i < table.getRowCount(); i++) {
			int id = TableUtil.checkInteger(table, i, "SRV_ID");
			int interval = TableUtil.checkInteger(table, i, "INTERVAL");
			int erratuminterval = TableUtil.checkInteger(table, i, "ERRATUMINTERVAL");
			
			String code = "tp-" + id;
			String name = "tp-" + interval;
			
			MonitorPolicy policy = new MonitorPolicy();			
			policy.setCode(code);			
			policy.setName(name);
			policy.setInterval(interval);
			policy.setErrorInterval(erratuminterval);
						
			MonitorPolicy exists = file.find(policy);
			if (exists == null) {					
				file.add(policy);
				exists = policy;
			}
			policyMap.put(id, exists.getCode());			
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测策略 %d 个。", file.getPolicies().size())));
	}
	
	private void expMethods() {
		Table table = DBUtil.queryForTable(conn, "select * from sm_monitormethod_param order by monitormethodid");
		for (int i = 0; i < table.getRowCount(); i++) {
			int id = TableUtil.checkInteger(table, i, "MONITORMETHODPARAMID");
			String typeId = TableUtil.checkString(table, i, "MONITORMETHODID");
			String name = TableUtil.checkString(table, i, "MONITORMETHODPARAMNAME");
			String descr = TableUtil.get(table, i, "MONITORMETHODPARAMDESC", "");
			
			String carrierTypeId = MapLibrary.getDefault().get("numen.method." + typeId);
			if (carrierTypeId == null || carrierTypeId.equalsIgnoreCase(Const.VALUE_IGNORE))
				continue;
			
			MonitorMethod method = new MonitorMethod();
			method.setCode(typeId + "-" + id);
			method.setTypeId(carrierTypeId);
			method.setName(name);
			method.setDescr(descr);
			
			expMethodProperties(typeId, method, id);
			
			MonitorMethod exists = file.find(method);
			if (exists == null) { 							
				file.add(method);
				exists = method;
			}
			methodMap.put(id, exists.getCode());
		}
		Logger.log(new Log(LogLevel.INFO, String.format("共导出监测方法 %d 个。", file.getMethods().size())));
	}
	
	private void expMethodProperties(String bccTypeId, MonitorMethod method, int methodId) {
		Table table = DBUtil.queryForTable(conn, "select * from sm_monitormethod_value where monitormethodparamid = ?", methodId);
		for (int i = 0; i < table.getRowCount(); i++) {
			String code = TableUtil.checkString(table, i, "PROPERTYNAME");
			String value = TableUtil.get(table, i, "PROPERTYVALUE", "");			
			value = decrypt(value);
			
			String mapCode = MapLibrary.getDefault().get("numen.method." + bccTypeId + "." + code);
			if (mapCode != null) {
				if (mapCode.equalsIgnoreCase(Const.VALUE_IGNORE))
					continue;
				String[] items = mapCode.split(";");
				if (items.length > 0) {
					code = items[0];
					for (int j = 1; j < items.length; j++) {
						String[] fields = items[j].split("=");
						if (value.equalsIgnoreCase(fields[0])) {
							value = fields[1];
							break;
						}
					}
				} else
					code = mapCode;			
			}
			
			method.getProperties().set(code, value);
		}
	}

	private String decrypt(String value) {
		Matcher match = encryptedPattern.matcher(value);
		if (match.matches() && match.groupCount() > 0) 
			value = encrypt.decrypt(match.group(1));
		return value;
	}
}
