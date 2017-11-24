package uyun.bat.gateway.dd_agent.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import uyun.bat.gateway.dd_agent.entity.NetDevData.NetDevDataJsonDeserializer;
import uyun.bat.gateway.dd_agent.util.DDJsonDeserializer;

@JsonDeserialize(using = NetDevDataJsonDeserializer.class)
public class NetDevData {
	private long timestamp;
	private String agentVersion;

	private List<DDMetric> metrics;

	private NetEquipment netEquipment;
	private List<TagEntry> netCollectorTags;

	/**
	 * 唯一标识
	 */
	private String identity;

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getAgentVersion() {
		return agentVersion;
	}

	public void setAgentVersion(String agentVersion) {
		this.agentVersion = agentVersion;
	}

	public List<DDMetric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<DDMetric> metrics) {
		this.metrics = metrics;
	}

	public NetEquipment getNetEquipment() {
		return netEquipment;
	}

	public void setNetEquipment(NetEquipment netEquipment) {
		this.netEquipment = netEquipment;
	}

	public List<TagEntry> getNetCollectorTags() {
		return netCollectorTags;
	}

	public void setNetCollectorTags(List<TagEntry> netCollectorTags) {
		this.netCollectorTags = netCollectorTags;
	}

	public static class NetDevDataJsonDeserializer extends JsonDeserializer<NetDevData> {
		@Override
		public NetDevData deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {
			// http://www.uyunsoft.cn/kb/pages/viewpage.action?pageId=15369176

			JsonNode node = jp.getCodec().readTree(jp);

			// 网络设备元数据
			JsonNode temp = node.get("netEquipment");
			NetEquipment netEquipment = null;
			if (!isEmpty(temp)) {
				netEquipment = new NetEquipment();

				JsonNode n = temp.get("ip");
				if (n != null && !n.isNull())
					netEquipment.setIp(n.asText());
				else
					return null;

				n = temp.get("type");
				if (n != null && !n.isNull())
					netEquipment.setType(n.asText());
				n = temp.get("producer");
				if (n != null && !n.isNull())
					netEquipment.setProducer(n.asText());
				n = temp.get("host");
				if (n != null && !n.isNull())
					netEquipment.setHost(n.asText());
				n = temp.get("descr");
				if (n != null && !n.isNull())
					netEquipment.setDescr(n.asText());
			}

			NetDevData data = new NetDevData();
			// 网络设备标识
			temp = node.get("identity");
			if (temp != null && !temp.isNull())
				data.identity = temp.asText();
			else
				return null;

			// 元数据
			data.netEquipment = netEquipment;
			data.timestamp = (long) (node.get("collection_timestamp").asDouble() * 1000);
			data.agentVersion = node.get("agentVersion").asText();

			// 自动发现标签
			temp = node.get("net_collector_tags");
			if (!isEmpty(temp)) {
				List<TagEntry> ats = new ArrayList<TagEntry>();
				Iterator<JsonNode> ite = temp.getElements();
				while (ite.hasNext()) {
					ats.add(DDJsonDeserializer.deserializeTagEntry(ite.next()));
				}
				data.netCollectorTags = ats;
			}

			// 指标数据
			temp = node.get("metrics");
			if (!isEmpty(temp)) {
				Iterator<JsonNode> ite = temp.getElements();
				List<DDMetric> metrics = new ArrayList<DDMetric>();
				DDMetric me = null;
				while (ite.hasNext()) {
					me = DDJsonDeserializer.deserializeDDMetric(ite.next());

					metrics.add(me);
				}
				data.metrics = metrics;
			}

			return data;
		}

		private boolean isEmpty(JsonNode temp) {
			return temp == null || temp.isNull() || temp.size() == 0;
		}
	}
}
