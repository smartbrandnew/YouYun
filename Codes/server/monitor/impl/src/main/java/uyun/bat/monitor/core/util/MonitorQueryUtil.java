package uyun.bat.monitor.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.OnlineStatus;
import uyun.bat.event.api.entity.EventServerityType;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.api.entity.Options;
import uyun.bat.monitor.core.entity.*;
import uyun.bat.monitor.core.logic.*;
import uyun.bat.monitor.impl.logic.LogicManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class MonitorQueryUtil {
	private static final Logger logger = LoggerFactory.getLogger(MonitorQueryUtil.class);

	private static Pattern pattern = Pattern.compile(" ");
	private static Pattern pattern1 = Pattern.compile("[{}]");
	private static Pattern pattern6 = Pattern.compile("[\\(''\\)]");
	private static Pattern pattern7 = Pattern.compile("\"");
	private static Pattern pattern5 = Pattern.compile("_");

	/**
	 * 指标tag集合部分有个坑，逗号被占，只能用分号
	 */
	private static Pattern pattern8 = Pattern.compile(";");

	/**
	 * 根据监测器查询条件解析出相关条件参数
	 */
	public static MetricMonitor generateMetricMonitor(Monitor monitor) throws Exception {
		MetricMonitorParam param = new MetricMonitorParam();

		// avg:system.cpu{!host:jianglf;role:1}
		// avg:system cpu{!host:jianglf;role:1} by {resourceId}
		String[] tempAry = pattern1.split(monitor.getQuery(), 0);
		// 除by以外的有效字符串
		if (tempAry.length == 4) {
			// avg:system.cpu 1{!host:jianglf;role:1} by {resourceId}
			// 有 by分组
			String[] groups = pattern8.split(tempAry[3], 0);
			List<String> groupList = new ArrayList<String>();
			for (String group : groups) {
				groupList.add(group);
			}
			param.setGroups(groupList);
		}

		if (!"*".equals(tempAry[1])) {
			List<TagEntry> tags = generateTags(tempAry[1]);
			param.setTags(tags);
		}

		int index = tempAry[0].indexOf(":");

		param.setAggregator(Aggregator.checkByCode(tempAry[0].substring(0, index)));
		param.setMetric(tempAry[0].substring(index + 1));

		param.setThresholds(monitor.getOptions());

		// 阈值都没有设置
		if (param.getThresholds() == null || param.getThresholds().isEmpty())
			return null;
		return new MetricMonitor(monitor, param);
	}

	private static List<TagEntry> generateTags(String tags) {
		List<TagEntry> tes = new ArrayList<TagEntry>();
		for (String s : pattern8.split(tags, 0)) {
			int index = s.indexOf(":");
			TagEntry tagEnry = new TagEntry();
			if (index == -1) {
				tagEnry.setKey(s);
			} else {
				tagEnry.setKey(s.substring(0, index));
				if ((index + 1) < (s.length()))
					tagEnry.setValue(s.substring(index + 1));
			}
			tes.add(tagEnry);
		}
		return tes;
	}

	/**
	 * 根据监测器查询条件解析出相关条件参数 events('status:alert,warning
	 * tags:{host:jianglf-centos6.7-64;role:1}
	 * "asd"').by('resourceId').rollup('count').last('5m') >= 10
	 */
	public static EventMonitor generateEventMonitor(Monitor monitor,String[] ss) throws Exception {
		EventMonitorParam param = new EventMonitorParam();
		String query = monitor.getQuery();
		if (null == query || query.isEmpty()) {
			return null;
		}
		String[] tempArr = pattern6.split(monitor.getQuery(), 0);

		String eventsArr[] = pattern.split(tempArr[2], 0);
		String tags[] = pattern1.split(eventsArr[1], 0);
		param.setTags(generateTags(tags[1]));
		eventsArr = pattern7.split(eventsArr[2], 0);
		param.setKeyWords(eventsArr[1]);
		param.setAggregator(tempArr[10]);
		param.setPeriod(tempArr[14]);
		String[] comArr = pattern.split(tempArr[16], 0);
		param.setComparison(Comparison.checkByCode(comArr[1]));
		param.setThreshold(EventMonitorParam.checkThreshold(comArr[2]));
		EventData eventData=new EventData();
		if (null!=ss&&ss.length>0){
			eventData.setTitle(ss[2]);
			eventData.setContent(ss[3]);
			eventData.setRecover(Boolean.parseBoolean(ss[4]));
			eventData.setResId(ss[5]);
		}
		return new EventMonitor(monitor, param,eventData);
	}

	public static List<MetricMonitor> getMetricMonitor(String tenantId) {
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic().getMonitors(tenantId, MonitorType.METRIC);
		List<MetricMonitor> metricList = new ArrayList<MetricMonitor>();
		for (Monitor monitor : monitors) {
			try {
				MetricMonitor mm = generateMetricMonitor(monitor);
				if (mm != null)
					metricList.add(mm);
			} catch (Exception e) {
				if (logger.isWarnEnabled())
					logger.warn("Parse monitor query condition exception, monitor do not work！query=" + monitor.getQuery());
			}
		}
		return metricList;
	}

	public static Checker getCheckerById(String[] ss) {
		String tenantId=ss[0];
		String monitorId=ss[1];
		Monitor monitor = LogicManager.getInstance().getMonitorLogic().getMonitorById(tenantId, monitorId);
		if (monitor == null || !monitor.getEnable()) {
			return null;
		}
		try {
			if (MonitorType.METRIC.equals(monitor.getMonitorType())) {
				return generateMetricMonitor(monitor);
			} else if (MonitorType.EVENT.equals(monitor.getMonitorType())) {
				return generateEventMonitor(monitor,ss);
			}else if (MonitorType.APP.equals(monitor.getMonitorType())){
				return generateAppMonitor(monitor);
			}else if (MonitorType.HOST.equals(monitor.getMonitorType())){
				return generateHostMonitor(monitor,ss);
			}else{
				return null;
			}

		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn("Parse monitor query condition exception！query=" + monitor.getQuery());
			return null;
		}
	}

	public static void main(String[] args) throws Throwable {
		Monitor m = new Monitor();
		m.setQuery("avg:system.cpu.system{host:jianglf-centos6.7-64;role:1} by {resourceId}");
		m.setOptions(new Options());
		//保留老的格式"last_5m < 8"
		m.getOptions().addThreshold(Options.ALERT, "last_5m < 8 MB");
		m.getOptions().addThreshold(Options.WARNING, null);
		generateMetricMonitor(m);
		m.setQuery("events('status:alert,warning tags:{host:jianglf-centos6.7-64;role:1} \"端口下线\"').by('resourceId').rollup('count').last('5m') >= 10");
		m.setOptions(new Options());
		m.getOptions().setEventRecover(new String[] { "1", "10m" });
		EventMonitor monitor = generateEventMonitor(m,null);
		System.out.println(monitor);

	}

	public static List<AppMonitor> getAppMonitor(String tenantId) {
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic().getMonitors(tenantId,MonitorType.APP);
		if (null == monitors || monitors.isEmpty()) {
			return new ArrayList<>();
		}
		List<AppMonitor> appMonitors = new ArrayList<>();
		for (Monitor monitor : monitors) {
			try {
				appMonitors.add(generateAppMonitor(monitor));
			} catch (Exception e) {
				if (logger.isWarnEnabled())
					logger.warn("Parse monitor query condition exception, monitor do not work！query=" + monitor.getQuery());
			}
		}
		return appMonitors;
	}

	/**
	 * ('host:jianglf-centos6.7-64;role:1').state('mysql.can_connect').by('resourceId').last('5m')
	 * @param monitor
	 * @return
	 */
	public static AppMonitor generateAppMonitor(Monitor monitor) {
		AppMonitorParam param=new AppMonitorParam();
		String query=monitor.getQuery();
		if (null==query||query.isEmpty()){
			return new AppMonitor(monitor,param);
		}
		String[] arr=pattern6.split(query,0);
		if (!"*".equals(arr[2])) {
			List<TagEntry> tags = generateTags(arr[2]);
			param.setTags(tags);
		}
		param.setState(arr[6]);
		param.setPeriod(arr[14]);
		param.setOptions(monitor.getOptions());
		return new AppMonitor(monitor,param);
	}

	/**
	 * ('host:jianglf-centos6.7-64;role:1').by('resourceId').last('5m')
	 * @param monitor
	 * @return
	 */
	public static HostMonitor generateHostMonitor(Monitor monitor, String[] ss){
		HostMonitorParam param=new HostMonitorParam();
		String query=monitor.getQuery();
		if (null==query||query.isEmpty()){
			return new HostMonitor(monitor,param);
		}
		String[] arr=pattern6.split(query,0);
		if (!"*".equals(arr[2])) {
			List<TagEntry> tags = generateTags(arr[2]);
			param.setTags(tags);
		}
		param.setPeriod(arr[10]);
		param.setOptions(monitor.getOptions());
		ResourceData resourceData=new ResourceData();
		if (null!=ss&&ss.length>0){
			resourceData.setResourceId(ss[2]);
			resourceData.setEventSourceType(Short.parseShort(ss[3]));
			resourceData.setOnlineStatus(OnlineStatus.checkById(Integer.parseInt(ss[4])));
			resourceData.setHostname(ss[5]);
			resourceData.setIpaddr(ss[6]);
		}
		return new HostMonitor(monitor,param,resourceData);
	}

	public static List<HostMonitor> getHostMonitor(String tenantId) {
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic().getMonitors(tenantId,MonitorType.HOST);
		if (null == monitors || monitors.isEmpty()) {
			return new ArrayList<>();
		}
		List<HostMonitor> hostMonitors = new ArrayList<HostMonitor>();
		for (Monitor monitor : monitors) {
			try {
				hostMonitors.add(generateHostMonitor(monitor,null));
			} catch (Exception e) {
				if (logger.isWarnEnabled())
					logger.warn("Parse monitor query condition exception, monitor do not work！query=" + monitor.getQuery());
			}
		}
		return hostMonitors;
	}

	public static List<EventMonitor> getEventMonitor(String tenantId) {
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic().getMonitors(tenantId, MonitorType.EVENT);
		List<EventMonitor> eventMonitors = new ArrayList<EventMonitor>();
		for (Monitor monitor : monitors) {
			try {
				eventMonitors.add(generateEventMonitor(monitor,null));
			} catch (Exception e) {
				if (logger.isWarnEnabled())
					logger.warn("Parse monitor query condition exception, monitor do not work！query=" + monitor.getQuery());
			}
		}
		return eventMonitors;
	}

	public static List<EventMonitor> getTriggeredEventMonitor() {
		List<Monitor> monitors = LogicManager.getInstance().getMonitorLogic()
				.getCheckEventMonitors(MonitorType.EVENT, MonitorState.ERROR, "eventRecover\":[\"1\",");
		if (null == monitors || monitors.isEmpty()) {
			return new ArrayList<EventMonitor>();
		}
		List<EventMonitor> eventMonitors = new ArrayList<EventMonitor>();
		for (Monitor monitor : monitors) {
			try {
				eventMonitors.add(generateEventMonitor(monitor,null));
			} catch (Exception e) {
				if (logger.isWarnEnabled())
					logger.warn("Parse monitor query condition exception, monitor do not work！query=" + monitor.getQuery());
			}
		}
		return eventMonitors;
	}

	public static EventServerityType getEventServerityType(MonitorState monitorStatus) {
		if (MonitorState.ERROR.equals(monitorStatus)) {
			return EventServerityType.ERROR;
		} else if (MonitorState.WARNING.equals(monitorStatus)) {
			return EventServerityType.WARNING;
		} else if (MonitorState.INFO.equals(monitorStatus)) {
			return EventServerityType.INFO;
		} else if (MonitorState.OK.equals(monitorStatus)) {
			return EventServerityType.SUCCESS;
		} else {
			return null;
		}
	}

	public static MonitorState getMonitorStatus(short key) {
		if (EventServerityType.ERROR.getKey() == key) {
			return MonitorState.ERROR;
		} else if (EventServerityType.WARNING.getKey() == key) {
			return MonitorState.WARNING;
		} else if (EventServerityType.INFO.getKey() == key) {
			return MonitorState.INFO;
		} else if (EventServerityType.SUCCESS.getKey() == key) {
			return MonitorState.OK;
		} else {
			return null;
		}
	}

	public static String getRecoversByTime(String[] eventRecover) {
		if (null == eventRecover || eventRecover.length != 2 || !eventRecover[0].equals("1")) {
			return null;
		}
		return eventRecover[1];
	}

	public static String getRecoversByKeywords(String[] eventRecover) {
		if (null == eventRecover || eventRecover.length != 2 || !eventRecover[0].equals("2")) {
			return null;
		}
		return eventRecover[1];
	}

	private static final String METRIC_MONITOR_QUERY_TEMPL="%s的%s指标的%s在最近%s时错误，且在最近%s时警告，且在最近%s时提醒";
	private static final String METRIC_MONITOR_QUERY_TEMPL_OLD="%s的%s指标的%s在最近%s时错误，且在最近%s时警告";
	private static final String EVENT_MONITOR_QUERY_TEMPL="在%s内收到%s的%s事件，且至少%s条时告警";
	private static final String APP_MONITOR_QUERY_TEMPL="在%s内未收到主机%s的%s应用相关数据时%s";
	private static final String HOST_MONITOR_QUERY_TEMPL="在 %s 内未收到主机%s的相关数据时%s";
	private static final String ALERT="告警";
	private static final String ALL_HOST="所有设备";
	private static final String METRIC_MONITOR_QUERY_TEMPL_EN="The %s value of the %s indicator for %s is error at the last %s and warns when the %s and remind when the %s";
	private static final String EVENT_MONITOR_QUERY_TEMPL_EN="In %s received %s's %s events, and at least %s when the alarm";
	private static final String APP_MONITOR_QUERY_TEMPL_EN="In %s did not receive the host %s's %s application-related data when %s";
	private static final String HOST_MONITOR_QUERY_TEMPL_EN="In %s did not receive the host %s when the relevant data %s";
	private static final String ALERT_EN="alarm";
	private static final String ALL_HOST_EN="All devices";

	final static boolean isZH = Config.getInstance().isChinese();

	private static String generateHostMonitorQuery(Monitor monitor){

		String[] arr=pattern6.split(monitor.getQuery(),0);
		String host = !"*".equals(arr[2]) ? arr[2] : (isZH ? ALL_HOST : ALL_HOST_EN);
		String period=arr[10];

		String status=Options.WARNING;
		Map<String, String> thresholds=monitor.getOptions().getThresholds();
		for(Map.Entry<String,String> entry:thresholds.entrySet()){
			if (Options.ALERT.equals(entry.getKey())){
				status= Options.ALERT;
				break;
			}
			entry.getKey();
		}
		String msg = status.equals(Options.WARNING) ? MonitorState.checkByCode(status).getCname() : (isZH ? ALERT : ALERT_EN);
		if (isZH)
			return String.format(HOST_MONITOR_QUERY_TEMPL, period, host, msg);
		else
			return String.format(HOST_MONITOR_QUERY_TEMPL_EN, period, host, msg);
	}

	private static String generateAppMonitorQuery(Monitor monitor){
		String[] arr=pattern6.split(monitor.getQuery(),0);
		String host= !"*".equals(arr[2])?arr[2]:(isZH ? ALL_HOST : ALL_HOST_EN);
		String app=arr[6].split("\\.")[0];
		String period=arr[14];

		String status=Options.WARNING;
		Map<String, String> thresholds=monitor.getOptions().getThresholds();
		for(Map.Entry<String,String> entry:thresholds.entrySet()){
			if (Options.ALERT.equals(entry.getKey())){
				status= Options.ALERT;
				break;
			}
			entry.getKey();
		}
		String msg=status.equals(Options.WARNING)?MonitorState.checkByCode(status).getCname():(isZH ? ALERT : ALERT_EN);
		if (isZH)
			return String.format(APP_MONITOR_QUERY_TEMPL, period, host, app, msg);
		else
			return String.format(APP_MONITOR_QUERY_TEMPL_EN, period, host, app, msg);
	}

	private static String generateEventMonitorQuery(Monitor monitor){

		String[] tempArr = pattern6.split(monitor.getQuery(), 0);
		String eventsArr[] = pattern.split(tempArr[2], 0);
		String tags[] = pattern1.split(eventsArr[1], 0);
		String host=tags[1];
		eventsArr = pattern7.split(eventsArr[2], 0);
		String keywords=eventsArr[1];
		String period=tempArr[14];
		String[] comArr = pattern.split(tempArr[16], 0);
		String count=comArr[2];
		if (isZH)
			return String.format(EVENT_MONITOR_QUERY_TEMPL, period, host, keywords, count);
		else
			return String.format(EVENT_MONITOR_QUERY_TEMPL_EN, period, host, keywords, count);
	}

	private static String generateMetricMonitorQuery(Monitor monitor){
		String[] tempAry = pattern1.split(monitor.getQuery(), 0);
		String host="*".equals(tempAry[1])?(isZH ? ALL_HOST : ALL_HOST_EN):tempAry[1];
		int index = tempAry[0].indexOf(":");
		String metric=tempAry[0].substring(index + 1);
		Aggregator agg = Aggregator.checkByCode(tempAry[0].substring(0, index));
		String aggregator = isZH ? agg.getName() : agg.getCode();
		// last_5m > 10.0
		String alertCondition=monitor.getOptions().getThresholds().get(Options.ALERT);
		String[] alertConditions=pattern.split(alertCondition,0);
		String[] alertArray = pattern5.split(alertConditions[0], 0);
		String unit="";
		if (alertConditions.length==4){
			unit=alertConditions[3];
		}
		Comparison alertComparison = Comparison.checkByCode(alertConditions[1]);
		String alert = alertArray[1] + (isZH ? alertComparison.getCname() : alertComparison.getName()) + alertConditions[2] + unit;

		String warnCondition=monitor.getOptions().getThresholds().get(Options.WARNING);
		String[] warnConditions=pattern.split(warnCondition,0);
		String[] warnArray = pattern5.split(warnConditions[0], 0);
		unit="";
		if (warnConditions.length==4) {
			unit=warnConditions[3];
		}
		Comparison warnComparison = Comparison.checkByCode(warnConditions[1]);
		String warning = warnArray[1] + (isZH ? warnComparison.getCname() : warnComparison.getName()) + warnConditions[2] + unit;

		String infoCondition=monitor.getOptions().getThresholds().get(Options.INFO);
		//旧的监测器没有info
		if(null != infoCondition) {
			String[] infoConditions = pattern.split(infoCondition, 0);
			String[] infoArray = pattern5.split(infoConditions[0], 0);
			unit = "";
			if (infoConditions.length == 4) {
				unit = infoConditions[3];
			}
			Comparison infoComparison = Comparison.checkByCode(infoConditions[1]);
			String info = infoArray[1] + (isZH ? infoComparison.getCname() : infoComparison.getName()) + infoConditions[2] + unit;
			if (isZH)
				return String.format(METRIC_MONITOR_QUERY_TEMPL, host, metric, aggregator, alert, warning, info);
			else
				return String.format(METRIC_MONITOR_QUERY_TEMPL_EN, aggregator, metric, host, alert, warning, info);
		}
		if (isZH)
			return String.format(METRIC_MONITOR_QUERY_TEMPL_OLD, host, metric, aggregator, alert, warning);
		else
			return String.format(METRIC_MONITOR_QUERY_TEMPL_OLD, aggregator, metric, host, alert, warning);
	}
	public static String generateQuery(Monitor monitor) {
		try {
			if (MonitorType.METRIC.equals(monitor.getMonitorType())){
				return generateMetricMonitorQuery(monitor);
			}else if (MonitorType.HOST.equals(monitor.getMonitorType())){
				return generateHostMonitorQuery(monitor);
			}else if (MonitorType.APP.equals(monitor.getMonitorType())){
				return generateAppMonitorQuery(monitor);
			}else if (MonitorType.EVENT.equals(monitor.getMonitorType())){
				return generateEventMonitorQuery(monitor);
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled())
				logger.warn("While monitor modify trigger event, parse monitor exception:query=" + monitor.getQuery());
		}
		return null;
	}

}
