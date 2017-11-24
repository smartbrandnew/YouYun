package uyun.bat.gateway.dd_agent.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import uyun.bat.gateway.dd_agent.entity.DDAgentData.AgentDataJsonDeserializer;
import uyun.bat.gateway.dd_agent.util.DDJsonDeserializer;

/**
 * agent汇报的数据
 */
@JsonDeserialize(using = AgentDataJsonDeserializer.class)
public class DDAgentData {
	private String uuid;
	private String internalHostname;
	private String os;
	private long timestamp;
	private List<DDMetric> metrics;
	private List<DDServiceCheck> serviceChecks;
	private List<DDServiceEvents> events;
	private String python;
	private String agentVersion;
	private String machine_type;
	/**
	 * host meta data
	 */
	private String gohai;
	/**
	 * agent tag
	 */
	private Map<String, List<TagEntry>> hostTags;
	
	private String ip;

	// 以下属性朕不处理
	// private Object externalHostTags;
	// private Object hostTags;

	// linux的一些数据，暂不存储
	// private Object ioStats;
	// private Object processes;
	// private Object resources;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getInternalHostname() {
		return internalHostname;
	}

	public void setInternalHostname(String internalHostname) {
		this.internalHostname = internalHostname;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<DDMetric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<DDMetric> metrics) {
		this.metrics = metrics;
	}

	public List<DDServiceCheck> getServiceChecks() {
		return serviceChecks;
	}

	public void setServiceChecks(List<DDServiceCheck> serviceChecks) {
		this.serviceChecks = serviceChecks;
	}

	public List<DDServiceEvents> getEvents() {
		return events;
	}

	public void setEvents(List<DDServiceEvents> events) {
		this.events = events;
	}

	public String getPython() {
		return python;
	}

	public void setPython(String python) {
		this.python = python;
	}

	public String getAgentVersion() {
		return agentVersion;
	}

	public void setAgentVersion(String agentVersion) {
		this.agentVersion = agentVersion;
	}

	public String getGohai() {
		return gohai;
	}

	public void setGohai(String gohai) {
		this.gohai = gohai;
	}

	public Map<String, List<TagEntry>> getHostTags() {
		return hostTags;
	}

	public void setHostTags(Map<String, List<TagEntry>> hostTags) {
		this.hostTags = hostTags;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMachine_type() {
		return machine_type;
	}

	public void setMachine_type(String machine_type) {
		this.machine_type = machine_type;
	}

	public static class AgentDataJsonDeserializer extends JsonDeserializer<DDAgentData> {
		@Override
		public DDAgentData deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {
			JsonNode node = jp.getCodec().readTree(jp);

			JsonNode temp = node.get("os");
			if (temp != null && !temp.isNull()) {
				String os = node.get("os").asText();

				if ("windows".equalsIgnoreCase(os))
					return deserializeWindowsData(node, jp, ctxt);
				else if ("linux".equalsIgnoreCase(os))
					return deserializeLinuxData(node, jp, ctxt);
				else
					return null;
			} else {
				// statsd
				return generateCommonData(node, jp, ctxt);
			}
		}

		/**
		 * 公共部分数据赋值
		 */
		private DDAgentData generateCommonData(JsonNode node, JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			DDAgentData data = new DDAgentData();
			data.uuid = node.get("uuid").asText();
			data.internalHostname = node.get("internalHostname").asText();

			JsonNode temp = node.get("ip");
			if (temp != null && !temp.isNull())
				data.ip = temp.asText();
			temp = node.get("os");
			if (temp != null && !temp.isNull())
				data.os = temp.asText().toLowerCase();
			temp = node.get("machine_type");
			if (temp != null && !temp.isNull())
				data.machine_type = temp.asText();
			temp = node.get("collection_timestamp");
			if (temp != null && !temp.isNull())
				data.timestamp = (long) (temp.asDouble() * 1000);
			temp = node.get("agentVersion");
			if (temp != null && !temp.isNull())
				data.agentVersion = temp.asText();
			temp = node.get("python");
			if (temp != null && !temp.isNull())
				data.python = temp.asText();

			// 事件
			temp = node.get("events");
			if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
				Iterator<String> ite = temp.getFieldNames();
				List<DDServiceEvents> events = new ArrayList<DDServiceEvents>();
				String serviceCheck = null;
				DDServiceEvents serviceEvents = null;
				JsonNode serviceNode = null;
				while (ite.hasNext()) {
					serviceCheck = ite.next();
					serviceNode = temp.get(serviceCheck);
					if (serviceNode != null && !serviceNode.isNull() && !(serviceNode.size() == 0)) {
						serviceEvents = new DDServiceEvents();
						serviceEvents.setCheck(serviceCheck);
						Iterator<JsonNode> eveIte = serviceNode.getElements();
						List<DDEvent> ddevents = new ArrayList<DDEvent>();
						while (eveIte.hasNext()) {
							DDEvent e = DDJsonDeserializer.deserializeDDEvent(eveIte.next());
							ddevents.add(e);
						}
						serviceEvents.setEvents(ddevents);
						events.add(serviceEvents);
					}
				}

				data.events = events;
			}
			// 指标
			temp = node.get("metrics");
			if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
				Iterator<JsonNode> ite = temp.getElements();
				List<DDMetric> metrics = new ArrayList<DDMetric>();
				DDMetric me = null;
				while (ite.hasNext()) {
					me = DDJsonDeserializer.deserializeDDMetric(ite.next());
					if (me.getHostName() == null)
						me.setHostName(data.getInternalHostname());
					metrics.add(me);
				}
				data.metrics = metrics;
			}
			// service_check
			temp = node.get("service_checks");
			if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
				Iterator<JsonNode> ite = temp.getElements();
				List<DDServiceCheck> serviceChecks = new ArrayList<DDServiceCheck>();
				while (ite.hasNext()) {
					serviceChecks.add(DDJsonDeserializer.deserializeDDServiceCheck(ite.next()));
				}
				data.serviceChecks = serviceChecks;
			}

			temp = node.get("gohai");
			if (temp != null && !temp.isNull()) {
				data.gohai = temp.asText();
			}

			temp = node.get("host-tags");
			if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
				data.hostTags = new HashMap<String, List<TagEntry>>();
				Iterator<String> ite = temp.getFieldNames();
				String type = null;
				JsonNode tagNode = null;
				while (ite.hasNext()) {
					type = ite.next();
					tagNode = temp.get(type);
					if (tagNode == null || tagNode.isNull() || tagNode.size() == 0) {
						continue;
					}
					Iterator<JsonNode> tagIte = tagNode.getElements();
					List<TagEntry> tags = new ArrayList<TagEntry>();
					while (tagIte.hasNext()) {
						tags.add(DDJsonDeserializer.deserializeTagEntry(tagIte.next()));
					}
					data.hostTags.put(type, tags);
				}
			}
			return data;
		}

		/**
		 * 解析windows版本数据
		 */
		private DDAgentData deserializeWindowsData(JsonNode node, JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			DDAgentData data = generateCommonData(node, jp, ctxt);

			return data;
		}

		/**
		 * 一些agent上传上来的指标名需要统一下 cpuGuest暂时无人认领
		 */
		private static final String[] linux_agent_metric = new String[] { "cpuIdle", "cpuWait", "cpuStolen", "cpuSystem",
				"cpuUser", "cpuGuest", "system.load.1", "system.load.5", "system.load.15", "system.load.norm.1",
				"system.load.norm.5", "system.load.norm.15", "system.uptime", "memSwapCached", "memSwapFree", "memSwapPctFree",
				"memSwapTotal", "memSwapUsed", "memBuffers", "memCached", "memSlab", "memShared", "memPhysTotal",
				"memPhysUsed", "memPhysFree", "memPhysPctUsage", "memPhysUsable", "memPageTables" ,"cpuUsage"};
		/**
		 * 服务端对于上传上来的linux指标名标准映射
		 */
		private static final String[] linux_server_metric = new String[] { "system.cpu.idle", "system.cpu.iowait",
				"system.cpu.stolen", "system.cpu.system", "system.cpu.user", "system.cpu.guest", "system.load.1",
				"system.load.5", "system.load.15", "system.load.norm.1", "system.load.norm.5", "system.load.norm.15",
				"system.uptime", "system.swap.cached", "system.swap.free", "system.swap.pct_free", "system.swap.total",
				"system.swap.used", "system.mem.buffered", "system.mem.cached", "system.mem.slab", "system.mem.shared",
				"system.mem.total", "system.mem.used", "system.mem.free", "system.mem.pct_usage", "system.mem.usable",
				"system.mem.page_tables" ,"system.cpu.pct_usage"};

		/**
		 * linux上传的io指标
		 */
		private static final String[] linux_agent_io_metric = new String[] { "avgqu-sz", "avgrq-sz", "await", "r_await",
				"r/s", "rkB/s", "rrqm/s", "svctm", "%util", "w_await", "w/s", "wkB/s", "wrqm/s" };
		/**
		 * 服务端对于上传上来的linux-io指标名标准映射
		 */
		private static final String[] linux_server_io_metric = new String[] { "system.io.avg_q_sz", "system.io.avg_rq_sz",
				"system.io.await", "system.io.r_await", "system.io.r_s", "system.io.rkb_s", "system.io.rrqm_s",
				"system.io.svctm", "system.io.util", "system.io.w_await", "system.io.w_s", "system.io.wkb_s",
				"system.io.wrqm_s" };

		/**
		 * 解析linux版本数据
		 */
		private DDAgentData deserializeLinuxData(JsonNode node, JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			DDAgentData data = generateCommonData(node, jp, ctxt);
			List<DDMetric> metrics = data.metrics;

			if (metrics == null)
				metrics = new ArrayList<DDMetric>();
			data.metrics = metrics;

			JsonNode temp = null;
			DDMetric ddmetric = null;

			for (int length = linux_agent_metric.length, i = 0; i < length; i++) {
				temp = node.get(linux_agent_metric[i]);
				if (temp == null || temp.isNull())
					continue;
				ddmetric = new DDMetric();
				ddmetric.setMetric(linux_server_metric[i]);
				ddmetric.setValue(temp.asDouble());
				ddmetric.setTimestamp(data.timestamp);
				ddmetric.setHostName(data.internalHostname);
				metrics.add(ddmetric);
			}
			
			temp = node.get("ioStats");
			if (temp != null && !temp.isNull() && temp.size() > 0) {
				Iterator<String> ite = temp.getFieldNames();
				String device = null;
				JsonNode metricNode = null;
				JsonNode n = null;
				while (ite.hasNext()) {
					device = ite.next();
					metricNode = temp.get(device);
					if (metricNode == null || metricNode.isNull() || metricNode.size() == 0) {
						continue;
					}
					for (int length = linux_agent_io_metric.length, i = 0; i < length; i++) {
						n = metricNode.get(linux_agent_io_metric[i]);
						if (n == null || n.isNull())
							continue;
						ddmetric = new DDMetric();
						ddmetric.setMetric(linux_server_io_metric[i]);
						ddmetric.setValue(n.asDouble());
						ddmetric.setTimestamp(data.timestamp);
						ddmetric.setHostName(data.internalHostname);
						ddmetric.setDeviceName(device);
						metrics.add(ddmetric);
					}
				}
			}

			return data;
		}
	}
}
