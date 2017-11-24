package uyun.bat.gateway.dd_agent.util;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.easymock.EasyMock;
import org.junit.Test;

import ch.qos.logback.classic.Logger;
import uyun.bat.gateway.dd_agent.entity.TagEntry;

public class DDJsonDeserializerTest {

	@Test
	public void testDeserializeDDEvent() {
		JsonNode node = EasyMock.createNiceMock(JsonNode.class);
		EasyMock.expect(node.get("timestamp")).andReturn(node);
		EasyMock.expect(node.get("event_type")).andReturn(node);
		EasyMock.expect(node.get("msg_title")).andReturn(node);
		EasyMock.expect(node.get("msg_text")).andReturn(node);
		EasyMock.expect(node.get("aggregation_key")).andReturn(node);
		EasyMock.expect(node.get("alert_type")).andReturn(node);
		EasyMock.expect(node.get("source_type_name")).andReturn(node);
		EasyMock.expect(node.get("host")).andReturn(node);
		EasyMock.expect(node.get("priority")).andReturn(node);
		EasyMock.expect(node.get("tags")).andReturn(node);
		EasyMock.expect(node.asText()).andReturn("test");
		EasyMock.expect(node.asDouble()).andReturn(1234.11);
		EasyMock.expect(node.isNull()).andReturn(false);
		EasyMock.replay(node);
		DDJsonDeserializer.deserializeDDEvent(node);
	}

	@Test
	public void testDeserializeTagEntry() {
		JsonNode node = EasyMock.createNiceMock(JsonNode.class);
		EasyMock.expect(node.asText()).andReturn("monitor:test");
		EasyMock.replay(node);
		TagEntry tag= DDJsonDeserializer.deserializeTagEntry(node);
		assertTrue(tag.getKey().equals("monitor"));
		assertTrue(tag.getValue().equals("test"));
	}

	@Test
	public void testDeserializeDDMetric() {
		JsonNode node = EasyMock.createNiceMock(JsonNode.class);
		EasyMock.expect(node.get(0)).andReturn(node);
		EasyMock.expect(node.get(1)).andReturn(node);
		EasyMock.expect(node.get(2)).andReturn(node);
		EasyMock.expect(node.get(3)).andReturn(node);
		EasyMock.expect(node.asText()).andReturn("test");
		EasyMock.expect(node.asDouble()).andReturn(1234.11);
		EasyMock.expect(node.isNull()).andReturn(false);
		EasyMock.expect(node.size()).andReturn(1);
		EasyMock.expect(node.get("type")).andReturn(node);
		EasyMock.expect(node.get("device_name")).andReturn(node);
		EasyMock.expect(node.get("hostname")).andReturn(node);
		EasyMock.expect(node.get("tags")).andReturn(node);
		EasyMock.replay(node);
		DDJsonDeserializer.deserializeDDMetric(node);
	}

	@Test
	public void testDeserializeDDServiceCheck() {
		List<JsonNode> list = new ArrayList<JsonNode>();
		JsonNode node = EasyMock.createNiceMock(JsonNode.class);
		EasyMock.expect(node.get("id")).andReturn(node);
		EasyMock.expect(node.get("ip")).andReturn(node);
		EasyMock.expect(node.get("status")).andReturn(node);
		EasyMock.expect(node.get("timestamp")).andReturn(node);
		EasyMock.expect(node.get("host_name")).andReturn(node);
		EasyMock.expect(node.get("uuid")).andReturn(node);
		EasyMock.expect(node.get("message")).andReturn(node);
		EasyMock.expect(node.get("check")).andReturn(node);
		EasyMock.expect(node.get("tags")).andReturn(node);
		EasyMock.expect(node.isNull()).andReturn(false);
		EasyMock.expect(node.size()).andReturn(1);
		EasyMock.expect(node.asText()).andReturn("monitor:test").atLeastOnce();
		EasyMock.expect(node.asInt()).andReturn(1);
		list.add(node);
		EasyMock.expect(node.getElements()).andReturn(list.iterator());
		EasyMock.replay(node);
		DDJsonDeserializer.deserializeDDServiceCheck(node);
	}

	@Test
	public void testGetIp() {
		String gohai = "{\"network\": {\"macaddress\": \"00:50:56:85:1d:cf\", \"ipaddress\": \"10.20.67.178\", \"ipaddressv6\": \"fe80::d791:d553:7c0a:8516\"}, \"filesystem\": [{\"mounted_on\": \"/\", \"kb_size\": \"104806400\", \"name\": \"/dev/mapper/cl-root\"}, {\"mounted_on\": \"/dev\", \"kb_size\": \"8118308\", \"name\": \"devtmpfs\"}, {\"mounted_on\": \"/dev/shm\", \"kb_size\": \"8133892\", \"name\": \"tmpfs\"}, {\"mounted_on\": \"/run\", \"kb_size\": \"8133892\", \"name\": \"tmpfs\"}, {\"mounted_on\": \"/sys/fs/cgroup\", \"kb_size\": \"8133892\", \"name\": \"tmpfs\"}, {\"mounted_on\": \"/opt\", \"kb_size\": \"400356356\", \"name\": \"/dev/mapper/cl-opt\"}, {\"mounted_on\": \"/boot\", \"kb_size\": \"2086912\", \"name\": \"/dev/sda1\"}, {\"mounted_on\": \"/run/user/0\", \"kb_size\": \"1626780\", \"name\": \"tmpfs\"}], \"platform\": {\"pythonV\": \"2.7.11\", \"kernel_release\": \"3.10.0-514.el7.x86_64\", \"kernel_version\": \"#1 SMP Tue Nov 22 16:42:41 UTC 2016\", \"hostname\": \"uyunapp\", \"machine\": \"x86_64\", \"GOOARCH\": \"amd64\", \"kernel_name\": \"Linux\", \"hardware_platform\": \"x86_64\", \"GOOS\": \"linux\", \"goV\": \"1.3.3\", \"os\": \"GNU/Linux\", \"processor\": \"x86_64\"}, \"gohai\": {\"build_date\": \"Thu Apr 21 18:04:04 UTC 2016\", \"git_hash\": \"0234f68\", \"git_branch\": \"5.7.x-agent\", \"go_version\": \"go version go1.3.3 linux/amd64\"}, \"memory\": {\"swap_total\": \"16777212kB\", \"total\": \"16267784kB\"}, \"cpu\": {\"cpu_logical_processors\": \"4\", \"family\": \"6\", \"vendor_id\": \"GenuineIntel\", \"cpu_cores\": \"4\", \"mhz\": \"1993.777\", \"stepping\": \"1\", \"cache_size\": \"24576 KB\", \"model\": \"37\", \"model_name\": \"Intel(R) Xeon(R) CPU E7- 8850  @ 2.00GHz\"}}";
		try {
			DDJsonDeserializer.getIp(gohai);
		} catch (Exception e) {
		}
	}

}
