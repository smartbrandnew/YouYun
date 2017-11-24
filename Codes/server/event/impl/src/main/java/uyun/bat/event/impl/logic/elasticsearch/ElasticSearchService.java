package uyun.bat.event.impl.logic.elasticsearch;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticSearchService {

	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchService.class);

	//事件表
	public static final String eventType = "event";
	//故障表
	public static final String faultType = "fault";

	private Client client;

	private String indexName;
	private String clusterName;
	private String transSniff;
	private String ipList;
	private int bulkActions = 1000;
	private int flushIntervalSec = 1;
	private int concurrentRequests = 10;

	private void init() {
		Settings settings = Settings.settingsBuilder()
				.put("cluster.name", clusterName)
				.build();
		try {
			String[] ipaddrs = ipList.split(",");
			TransportClient transportClient = TransportClient.builder().settings(settings).build();
			for (String ipaddr : ipaddrs) {
				String[] arrays = ipaddr.split(":");
				transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(arrays[0]), Integer
						.parseInt(arrays[1])));
			}
			client = transportClient;
		} catch (UnknownHostException e) {
			logger.warn("ElasticSearch init exception..." + e.getMessage());
		}
		createIndexAndMappings();
	}

	private void createIndexAndMappings() {
		IndicesExistsResponse exists = client.admin().indices().exists(new IndicesExistsRequest(indexName)).actionGet();
		if (!exists.isExists()) {
			client.admin().indices().prepareCreate(indexName).execute().actionGet();
			putMappings();
		}
		setTtlEnabled();
	}

	private void putMappings() {
		XContentBuilder relateBuilder = null;
		try {
			relateBuilder = jsonBuilder()
					.startObject()
					.startObject(eventType)
					.startObject("_parent")
					.field("type", faultType)
					.endObject()
					.endObject()
					.endObject();
		} catch (IOException e) {
			logger.warn("ElasticSearch create Mappings exception..." + e.getMessage());
		}

		client.admin().indices().preparePutMapping(indexName)
				.setType(eventType).setSource(relateBuilder)
				.execute().actionGet();

		putFaultMappings();
		putEventMappings();
	}

	private void putEventMappings() {
		XContentBuilder builder = null;
		try {
			builder = jsonBuilder()
					.startObject()
					.startObject(eventType)
					.startObject("properties")
					.startObject("id")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("tenant_id")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("fault_id")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("res_id")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("msg_title")
					.field("type", "string")
					.endObject()
					.startObject("msg_content")
					.field("type", "string")
					.endObject()
					.startObject("source_type")
					.field("type", "integer")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("serverity")
					.field("type", "integer")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("monitor_id")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("identity")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("occur_time")
					.field("type", "date")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("tags")
					.field("type", "string")
					.endObject()
					.startObject("origin_tags")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("host")
					.field("type", "string")
					.endObject()
					.startObject("ip")
					.field("type", "string")
					.endObject()
					.startObject("sort_id")
					.field("type", "integer")
					.field("index", "not_analyzed")
					.endObject()
					.endObject()
					.endObject()
					.endObject();
		} catch (IOException e) {
			logger.warn("ElasticSearch create Mappings exception...");
		}
		client.admin().indices().preparePutMapping(indexName)
				.setType(eventType).setSource(builder)
				.execute().actionGet();
	}

	private void putFaultMappings() {
		XContentBuilder faultBuilder = null;
		try {
			faultBuilder = jsonBuilder()
					.startObject()
					.startObject(faultType)
					.startObject("properties")
					.startObject("fault_id")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("tenant_id")
					.field("type", "string")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("relate_count")
					.field("type", "long")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("first_time")
					.field("type", "date")
					.field("index", "not_analyzed")
					.endObject()
					.startObject("recover")
					.field("type", "boolean")
					.field("index", "not_analyzed")
					.endObject()
					.endObject()
					.endObject()
					.endObject();
		} catch (IOException e) {
			logger.warn("ElasticSearch create Mappings exception...");
		}
		client.admin().indices().preparePutMapping(indexName)
				.setType(faultType).setSource(faultBuilder)
				.execute().actionGet();
	}

	private void setTtlEnabled() {
		XContentBuilder builder = null;
		try {
			builder = jsonBuilder()
					.startObject()
					.startObject("_ttl")
					.field("enabled", "true")
					.endObject()
					.endObject();
		} catch (IOException e) {
			logger.warn("ElasticSearch enable ttl setting exception");
		}
		client.admin().indices().preparePutMapping(indexName)
				.setType(faultType).setSource(builder)
				.execute().actionGet();
		client.admin().indices().preparePutMapping(indexName)
				.setType(eventType).setSource(builder)
				.execute().actionGet();
	}

	private BulkProcessor bulkProcessor;

	public BulkProcessor getBulkProcess() {
		if (null == bulkProcessor) {
			synchronized (ElasticSearchService.class) {
				bulkProcessor = BulkProcessor.builder(client,
						new BulkProcessor.Listener() {
							@Override
							public void beforeBulk(long executionId,
												   BulkRequest request) {
							}

							@Override
							public void afterBulk(long executionId,
												  BulkRequest request,
												  BulkResponse response) {
							}

							@Override
							public void afterBulk(long executionId,
												  BulkRequest request,
												  Throwable failure) {
								logger.warn("ElasticSearch insert exception:" + failure.getMessage() + "," + failure.getCause());
							}
						})
						.setBulkActions(bulkActions)
						.setFlushInterval(TimeValue.timeValueSeconds(flushIntervalSec))
						.setConcurrentRequests(concurrentRequests)
						.build();
			}
		}
		return bulkProcessor;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String isTransSniff() {
		return transSniff;
	}

	public void setTransSniff(String transSniff) {
		this.transSniff = transSniff;
	}

	public String getIpList() {
		return ipList;
	}

	public void setIpList(String ipList) {
		this.ipList = ipList;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	private void destroy() {
		if (null!=bulkProcessor){
			bulkProcessor.close();
		}
		if (null!=client){
			client.close();
		}
	}
}
