package uyun.bat.common.constants;

public class MonitorConstants {
	public static final String datastore_insert_1m_tps = "bat.tps.datastore.insert.1m";
	public static final String datastore_insert_5m_tps = "bat.tps.datastore.insert.5m";
	public static final String datastore_insert_15m_tps = "bat.tps.datastore.insert.15m";
	public static final String datastore_query_1m_tps = "bat.tps.datastore.query.1m";
	public static final String datastore_query_5m_tps = "bat.tps.datastore.query.5m";
	public static final String datastore_query_15m_tps = "bat.tps.datastore.query.15m";
	public static final String datastore_insert_5m_failed_count = "bat.tps.datastore.insert.failed.5m";
	public static final String datastore_query_5m_failed_count = "bat.tps.datastore.query.failed.5m";

	public static final String gateway_metric_insert_1m_tps = "bat.tps.gateway.metrics.1m";
	public static final String gateway_metric_insert_5m_tps = "bat.tps.gateway.metrics.5m";
	public static final String gateway_metric_insert_15m_tps = "bat.tps.gateway.metrics.15m";
	public static final String gateway_event_insert_1m_tps = "bat.tps.gateway.events.1m";
	public static final String gateway_event_insert_5m_tps = "bat.tps.gateway.events.5m";
	public static final String gateway_event_insert_15m_tps = "bat.tps.gateway.events.15m";
	
	public static final String event_insert_1m_tps = "bat.tps.event.insert.1m";
	public static final String event_insert_5m_tps = "bat.tps.event.insert.5m";
	public static final String event_insert_15m_tps = "bat.tps.event.insert.15m";
	public static final String event_query_page_1m_tps = "bat.tps.event.query.page.1m";
	public static final String event_query_page_5m_tps = "bat.tps.event.query.page.5m";
	public static final String event_query_page_15m_tps = "bat.tps.event.query.page.15m";
	
	public static final String event_query_graph_1m_tps = "bat.tps.event.query.graph.1m";
	public static final String event_query_graph_5m_tps = "bat.tps.event.query.graph.5m";
	public static final String event_query_graph_15m_tps = "bat.tps.event.query.graph.15m";
	
	public static final String event_insert_5m_failed_count = "bat.tps.event.insert.failed.5m";
	public static final String event_query_5m_failed_count = "bat.tps.event.query.failed.5m";

	public static final String monitor_deal_1m_tps = "bat.tps.monitor.deal.1m";
	public static final String monitor_deal_5m_tps = "bat.tps.monitor.deal.5m";
	public static final String monitor_deal_15m_tps = "bat.tps.monitor.deal.15m";
	
	//租户第一次指标采集时间
	public static final String metric_start_collect_time="bat.tenant.metric.start.collect.time";
	//租户最后一次采集指标时间
	public static final String metric_end_collect_time="bat.tenant.metric.end.collect.time";
	//租户指标采集持续时间:最后一次采集-首次采集时间
	public static final String metric_collect_duration_time="bat.tenant.metric.collect.duration";
	

	//租户第一次事件上报时间
	public static final String event_start_collect_time="bat.tenant.event.start.collect.time";
	//租户最后一次事件上报时间
	public static final String event_end_collect_time="bat.tenant.event.end.collect.time";
	//租户事件上报持续时间:最后一次上报-首次上报时间
	public static final String event_collect_duration_time="bat.tenant.event.collect.duration";

}
