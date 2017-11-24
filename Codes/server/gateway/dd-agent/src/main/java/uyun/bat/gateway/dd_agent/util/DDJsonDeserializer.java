package uyun.bat.gateway.dd_agent.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;

import uyun.bat.gateway.api.service.util.JsonUtil;
import uyun.bat.gateway.dd_agent.entity.DDEvent;
import uyun.bat.gateway.dd_agent.entity.DDMetric;
import uyun.bat.gateway.dd_agent.entity.DDServiceCheck;
import uyun.bat.gateway.dd_agent.entity.TagEntry;

public abstract class DDJsonDeserializer {
	public static DDEvent deserializeDDEvent(JsonNode node) {
		DDEvent event = new DDEvent();

		JsonNode temp = node.get("timestamp");// python的时间戳到秒，需要*1000
		if (temp != null && !temp.isNull())
			event.setTimestamp((long) (temp.asDouble() * 1000));
		temp = node.get("event_type");
		if (temp != null && !temp.isNull())
			event.setEventType(temp.asText());
		temp = node.get("msg_title");
		if (temp != null && !temp.isNull())
			event.setMsgTitle(temp.asText());
		temp = node.get("msg_text");
		if (temp != null && !temp.isNull())
			event.setMsgText(temp.asText());
		temp = node.get("aggregation_key");
		if (temp != null && !temp.isNull())
			event.setAggregationKey(temp.asText());
		temp = node.get("alert_type");
		if (temp != null && !temp.isNull())
			event.setAlertType(temp.asText());
		temp = node.get("source_type_name");
		if (temp != null && !temp.isNull())
			event.setSourceTypeName(temp.asText());
		temp = node.get("host");
		if (temp != null && !temp.isNull())
			event.setHost(temp.asText());
		temp = node.get("priority");
		if (temp != null && !temp.isNull())
			event.setPriority(temp.asText());

		temp = node.get("tags");
		if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
			Iterator<JsonNode> ite = temp.getElements();
			List<TagEntry> tags = new ArrayList<TagEntry>();
			while (ite.hasNext()) {
				TagEntry tag = deserializeTagEntry(ite.next());
				tags.add(tag);
			}

			event.setTags(tags);
		}

		return event;
	}

	public static TagEntry deserializeTagEntry(JsonNode t) {
		String tag = t.asText();
		int index = tag.indexOf(":");
		TagEntry tagEntry = new TagEntry();
		if (index == -1) {
			tagEntry.setKey(tag);
		} else {
			tagEntry.setKey(tag.substring(0, index));
			if ((index + 1) < (tag.length()))
				tagEntry.setValue(tag.substring(index + 1));
		}
		return tagEntry;
	}

	public static DDMetric deserializeDDMetric(JsonNode node) {
		DDMetric metric = new DDMetric();

		metric.setMetric(node.get(0).asText());
		metric.setTimestamp((long) (node.get(1).asDouble() * 1000));
		metric.setValue(node.get(2).asDouble());

		JsonNode temp = node.get(3);
		if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
			JsonNode t = temp.get("tags");
			if (t != null && !t.isNull() && !(t.size() == 0)) {
				Iterator<JsonNode> ite = t.getElements();

				List<TagEntry> tags = new ArrayList<TagEntry>();
				while (ite.hasNext()) {
					TagEntry tag = deserializeTagEntry(ite.next());
					tags.add(tag);
				}

				metric.setTags(tags);
			}
			t = temp.get("hostname");
			if (t != null && !t.isNull())
				metric.setHostName(t.asText());
			t = temp.get("device_name");
			if (t != null && !t.isNull())
				metric.setDeviceName(t.asText());
			t = temp.get("type");
			if (t != null && !t.isNull())
				metric.setType(t.asText());
		}
		return metric;
	}

	public static DDServiceCheck deserializeDDServiceCheck(JsonNode node) {
		DDServiceCheck serviceCheck = new DDServiceCheck();

		JsonNode temp = node.get("id");
		if (temp != null && !temp.isNull())
			serviceCheck.setId(temp.asInt());
		
		temp = node.get("ip");
		if (temp != null && !temp.isNull())
			serviceCheck.setIp(temp.asText());

		serviceCheck.setStatus(node.get("status").asInt());
		serviceCheck.setTimestamp((long) (node.get("timestamp").asDouble() * 1000));
		serviceCheck.setHostName(node.get("host_name").asText());

		temp = node.get("uuid");
		if (temp != null && !temp.isNull())
			serviceCheck.setUuid(temp.asText());
		
		temp = node.get("message");
		if (temp != null && !temp.isNull())
			serviceCheck.setMessage(temp.asText());

		serviceCheck.setCheck(node.get("check").asText());

		temp = node.get("tags");
		if (temp != null && !temp.isNull() && !(temp.size() == 0)) {
			Iterator<JsonNode> ite = temp.getElements();

			List<TagEntry> tags = new ArrayList<TagEntry>();
			while (ite.hasNext()) {
				TagEntry tag = deserializeTagEntry(ite.next());
				tags.add(tag);
			}

			serviceCheck.setTags(tags);
		}
		return serviceCheck;
	}

	/********************************* 获取agent元数据方法 *********************************/
	@SuppressWarnings({ "unchecked" })
	public static String getIp(String gohai) throws Exception {
		if (gohai != null && gohai.length() > 0) {
			Map<String, Object> meta = JsonUtil.decode(gohai, Map.class);

			Object temp = meta.get("network");
			if (temp instanceof Map) {
				Map<String, String> network = (Map<String, String>) temp;
				// 暂时优先取ipv4
				String ipaddress = network.get("ipaddress");
				if (ipaddress != null && ipaddress.length() > 0)
					return ipaddress;
				// 尝试ipv6
				ipaddress = network.get("ipaddressv6");
				if (ipaddress != null && ipaddress.length() > 0)
					return ipaddress;
			}
		}
		return null;
	}

}
