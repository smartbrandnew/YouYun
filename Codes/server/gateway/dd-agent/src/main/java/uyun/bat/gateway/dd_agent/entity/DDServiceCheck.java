package uyun.bat.gateway.dd_agent.entity;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import uyun.bat.gateway.dd_agent.entity.DDServiceCheck.DDServiceCheckJsonDeserializer;
import uyun.bat.gateway.dd_agent.util.DDJsonDeserializer;

import java.io.IOException;
import java.util.List;

/**
 * 服务检查
 */
@JsonDeserialize(using = DDServiceCheckJsonDeserializer.class)
public class DDServiceCheck {
	/**
	 * 莫名的id
	 */
	private int id;
	/**
	 * 服务状态
	 */
	private int status;
	/**
	 * 标签
	 */
	private List<TagEntry> tags;
	/**
	 * 时间戳
	 */
	private long timestamp;
	/**
	 * 主机名
	 */
	private String hostName;
	/**
	 * 错误信息
	 */
	private String message;
	/**
	 * 状态指标名
	 */
	private String check;
	/**
	 * agent唯一标识UUid
	 */
	private String uuid;
	
	private String ip;
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public List<TagEntry> getTags() {
		return tags;
	}

	public void setTags(List<TagEntry> tags) {
		this.tags = tags;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCheck() {
		return check;
	}

	public void setCheck(String check) {
		this.check = check;
	}

	public static class DDServiceCheckJsonDeserializer extends JsonDeserializer<DDServiceCheck> {

		@Override
		public DDServiceCheck deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
				JsonProcessingException {
			JsonNode node = jp.getCodec().readTree(jp);

			return DDJsonDeserializer.deserializeDDServiceCheck(node);
		}
	}

	public boolean isNeedSave() {
		for(String temp:service_checks){
			if (temp.equals(check)){
				return true;
			}
		}
		return false;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	//暂时指定保存的状态指标
	private static final String[] service_checks = new String[] { "mysql.can_connect", "apache.can_connect",
			"couchdb.can_connect", "redis.can_connect", "docker.service_up", "etcd.can_connect", "fluentd.is_ok",
			"gearman.can_connect", "haproxy.backend_up", "kyototycoon.can_connect", "lighttpd.can_connect", "marathon.can_connect",
			"mesos.can_connect", "mongodb.can_connect", "nginx.can_connect", "pgbouncer.can_connect", "php_fpm.can_ping",
			"postgres.can_connect", "riak.can_connect", "riakcs.can_connect", "sqlserver.can_connect", "oracle.can_connect",
			"tokumx.can_connect", "vcenter.can_connect", "yarn.can_connect", "activemq.can_connect", "elasticsearch.can_connect",
			"memcache.can_connect","supervisord.can_connect","tomcat.can_connect","supervisord.can_connect",
			"postgres.can_connect","rabbitmq.aliveness","zookeeper.ruok","kafka.can_connect","kafkaclient.can_connect",
			"hdfs.datanode.jmx.can_connect","hdfs.namenode.jmx.can_connect" };


}
