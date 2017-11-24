package uyun.bat.monitor.core.util;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.MetricMetaData;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.MonitorType;
import uyun.bat.monitor.core.entity.CheckContext;
import uyun.bat.monitor.core.entity.MonitorParam;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.whale.common.util.text.Unit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class NotifyTemplateConfig {
	private static final Logger logger = LoggerFactory.getLogger(NotifyTemplateConfig.class);
	/******************** 表达式 start ****************************/
	private static final String escaped1 = Pattern.quote("${monitorState}");
	private static final String escaped2 = Pattern.quote(MonitorParam.METRIC_NAME);
	private static final String escaped3 = Pattern.quote("${occurTime}");
	private static final String escaped4 = Pattern.quote("${eventContent}");
	private static final String escaped5 = Pattern.quote("${hostName}");
	private static final String escaped6 = Pattern.quote("${ip}");
	private static final String escaped7 = Pattern.quote("${eventTitle}");
	private static final String escaped8 = Pattern.quote("${value}");
	private static final String escaped9 = Pattern.quote(MonitorParam.DURATION);
	private static final String escaped10 = Pattern.quote(MonitorParam.AGGREGATOR);
	private static final String escaped11 = Pattern.quote(MonitorParam.COMPARISON);
	private static final String escaped12 = Pattern.quote(MonitorParam.THRESHOLD);
	private static final String escaped13 = Pattern.quote(MonitorParam.KEY_WORDS);
	private static final String escaped14 = Pattern.quote("${recoverTime}");
	private static final String escaped15 = Pattern.quote("${threshold_unit}");
	private static final String escaped16 = Pattern.quote(MonitorParam.APP);
	private static final String escapedInstance = Pattern.quote("${instance}");
	/******************** 表达式 end ****************************/

	private static NotifyTemplateConfig instance;

	private MetricTemplateConfig metricTemplateConfig;
	private EventTemplateConfig eventTemplateConfig;
	private SMSTemplateConfig smsTemplateConfig;
	private EmailTemplateConfig emailTemplateConfig;
	private HostTemplateConfig hostTemplateConfig;
	private AppTemplateConfig appTemplateConfig;

	private static boolean isZH = Config.getInstance().isChinese();

	private NotifyTemplateConfig() {
		super();
		init();
	}

	private File getConfigFile() {
		String[] searchPaths = new String[] { "/conf/", "/../conf/", "/../../conf/", "/src/main/resources/conf/" };

		String dir = System.getProperty("work.dir", System.getProperty("user.dir"));
		File file ;
		for (String path : searchPaths) {
			if(Config.getInstance().isChinese()){
				file = new File(dir, path + "monitorNotifyTem.xml");
			}else{
				file = new File(dir, path + "monitorNotifyTemEn.xml");
			}
			
			if (file.exists()) {
				return file;
			}
		}

		return null;
	}

	private void init() {
		try {
			File f = getConfigFile();
			if (f == null)
				throw new RuntimeException("The monitor notification template initializes exception!");
			SAXReader reader = new SAXReader();
			Document doc = reader.read(f);
			Element root = doc.getRootElement();
			List<Element> param = root.elements();
			for (Element e : param) {
				if (e.attributeValue("id").equals("metric")) {
					metricTemplateConfig = MetricTemplateConfig.parseMetricTemplateConfig(e);
				} else if (e.attributeValue("id").equals("event")) {
					eventTemplateConfig = EventTemplateConfig.parseEventTemplateConfig(e);
				} else if (e.attributeValue("id").equals("sms")) {
					smsTemplateConfig = SMSTemplateConfig.parseSMSTemplateConfig(e);
				} else if (e.attributeValue("id").equals("email")) {
					emailTemplateConfig = EmailTemplateConfig.parseEmailTemplateConfig(e);
				}else if (e.attributeValue("id").equals("host")){
					hostTemplateConfig=HostTemplateConfig.parseHostTemplateConfig(e);
				}else if (e.attributeValue("id").equals("app")){
					appTemplateConfig=AppTemplateConfig.parseAppTemplateConfig(e);
				}
			}
		} catch (Throwable e) {
			logger.error("Fail to init notify template！It will bring some notify exceptions!");
		}
	}

	/**
	 * 获取默认单例
	 * 
	 * @return 单例
	 */
	public static NotifyTemplateConfig getInstance() {
		if (instance == null) {
			synchronized (NotifyTemplateConfig.class) {
				if (instance == null)
					instance = new NotifyTemplateConfig();
			}
		}
		return instance;
	}

	public Event generateEvent(CheckContext context) {
		if (MonitorType.METRIC.getCode().equals(context.getEvent().getMonitorType())) {
			/** 指标监测器 */
			return metricTemplateConfig.generateEvent(context);
		} else if (MonitorType.EVENT.getCode().equals(context.getEvent().getMonitorType())){
			/** 事件监测器 */
			return eventTemplateConfig.generateEvent(context);
		}else if (MonitorType.HOST.getCode().equals(context.getEvent().getMonitorType())){
			return hostTemplateConfig.generateEvent(context);
		}else if(MonitorType.APP.getCode().equals(context.getEvent().getMonitorType())){
			return appTemplateConfig.generateEvent(context);
		}else{
			return null;
		}
	}

	public String generateSMS(CheckContext context) {
		return smsTemplateConfig.generateSMS(context);
	}

	public String generateArbiter(CheckContext context){
		return smsTemplateConfig.generateArbiter(context);
	}

	public String generateEmailTitle(CheckContext context) {
		return emailTemplateConfig.generateEmailTitle(context);
	}

	public String generateEmailContent(CheckContext context) {
		return emailTemplateConfig.generateEmailContent(context);
	}

	private static class MetricTemplateConfig {
		private String title;
		private String okContent;
		private String alertContent;

		public static MetricTemplateConfig parseMetricTemplateConfig(Element e) {
			MetricTemplateConfig temp = new MetricTemplateConfig();
			temp.title = e.element("title").getText();
			temp.okContent = e.element("okContent").getText();
			temp.alertContent = e.element("alertContent").getText();
			return temp;
		}

		public Event generateEvent(CheckContext context) {
			Event event = context.getEvent();
			Map<String, String> pm = context.getMonitorParam().getParamMap();
			String temp = title;
			// 指标监测标题替换：${metric}监测
			String metric = pm.get(MonitorParam.METRIC_NAME);
			boolean isWarnAlertLastState = MonitorState.WARNING.equals(context.getLastMonitorState());
			boolean isErrorState = MonitorState.ERROR.equals(context.getMonitorState());
			boolean isInfoState = MonitorState.INFO.equals(context.getMonitorState());
			// 获取指标单位
			String unit = "";
			MetricMetaData metaData = ServiceManager.getInstance().getMetricMetaDataService().queryByName(metric);
			if (metaData != null){
				unit = metaData.getUnit();
				Unit metricUnit = Unit.SETS[0][0].getUnitByCode(unit);
				Unit thresholdUnit = null;
				if (MonitorState.OK.equals(context.getMonitorState()))
					thresholdUnit = Unit.SETS[0][0].getUnitByCode(pm.get(isWarnAlertLastState ? MonitorParam.THRESHOLD_WARN_UNIT
							: MonitorParam.THRESHOLD_UNIT));
				else
					thresholdUnit = Unit.SETS[0][0].getUnitByCode(pm.get(isErrorState ? MonitorParam.THRESHOLD_UNIT
							: MonitorParam.THRESHOLD_WARN_UNIT));
				if (null != thresholdUnit && null != metricUnit) {
					double convertValue = metricUnit.to(thresholdUnit, Double.parseDouble(context.getValue()));
					context.setValue(thresholdUnit.format(convertValue,false));
				}
			}
			temp = temp.replaceAll(escaped2, metric);
			event.setMsgTitle(temp);
			if (MonitorState.OK.equals(context.getMonitorState())) {
				// 指标监测内容正常替换:${hostName}(${ip})的指标${metric}已恢复到${value}${threshold_unit}（最近${duration}）${aggregator}
				temp = okContent.replaceAll(escaped5, context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "").replaceAll(escaped2, metric)
						.replaceAll(escapedInstance, context.getInstance() != null ? context.getInstance() : "")
						.replaceAll(escaped8, context.getValue());
				if (isWarnAlertLastState) {
					// 若上次为警告等级
					temp = temp.replaceAll(escaped9, pm.get(MonitorParam.DURATION_WARN))
							.replaceAll(escaped10, pm.get(MonitorParam.AGGREGATOR))
							.replaceAll(escaped15, pm.get(MonitorParam.THRESHOLD_WARN_UNIT));
				} else {
					// 若上次为空或者告警等级
					temp = temp.replaceAll(escaped9, pm.get(MonitorParam.DURATION))
							.replaceAll(escaped10, pm.get(MonitorParam.AGGREGATOR))
							.replaceAll(escaped15, pm.get(MonitorParam.THRESHOLD_UNIT));
				}

			} else {
				// 指标监测内容异常替换：${hostName}(${ip})的指标${metric}已达到${value}${threshold_unit}（最近${duration}${aggregator}），${comparison}阈值${threshold}${threshold_unit}
				temp = alertContent.replaceAll(escaped5, context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "").replaceAll(escaped2, metric)
						.replaceAll(escapedInstance, context.getInstance() != null ? context.getInstance() : "")
						.replaceAll(escaped8, context.getValue());
				if (isErrorState) {
					temp = temp.replaceAll(escaped9, pm.get(MonitorParam.DURATION))
							.replaceAll(escaped10, pm.get(MonitorParam.AGGREGATOR))
							.replaceAll(escaped11, pm.get(MonitorParam.COMPARISON))
							.replaceAll(escaped12, pm.get(MonitorParam.THRESHOLD))
							.replaceAll(escaped15, pm.get(MonitorParam.THRESHOLD_UNIT));
				} else if(isWarnAlertLastState){
					temp = temp.replaceAll(escaped9, pm.get(MonitorParam.DURATION_WARN))
							.replaceAll(escaped10, pm.get(MonitorParam.AGGREGATOR))
							.replaceAll(escaped11, pm.get(MonitorParam.COMPARISON_WARN))
							.replaceAll(escaped12, pm.get(MonitorParam.THRESHOLD_WARN))
							.replaceAll(escaped15, pm.get(MonitorParam.THRESHOLD_WARN_UNIT));
				} else{
					temp = temp.replaceAll(escaped9, pm.get(MonitorParam.DURATION_INFO))
							.replaceAll(escaped10, pm.get(MonitorParam.AGGREGATOR))
							.replaceAll(escaped11, pm.get(MonitorParam.COMPARISON_INFO))
							.replaceAll(escaped12, pm.get(MonitorParam.THRESHOLD_INFO))
							.replaceAll(escaped15, pm.get(MonitorParam.THRESHOLD_INFO_UNIT));
				}

			}
			event.setMsgContent(temp);
			return event;
		}
	}

	private static class EventTemplateConfig {
		private String title;
		private String singleAlertContent;
		private String batchAlertContent;
		private String timeOkContent;
		private String numOkContent;

		public static EventTemplateConfig parseEventTemplateConfig(Element e) {
			EventTemplateConfig temp = new EventTemplateConfig();
			temp.title = e.element("title").getText();
			temp.singleAlertContent = e.element("singleAlertContent").getText();
			temp.batchAlertContent = e.element("batchAlertContent").getText();
			temp.timeOkContent = e.element("timeOkContent").getText();
			temp.numOkContent = e.element("numOkContent").getText();
			return temp;
		}

		public Event generateEvent(CheckContext context) {
			Event event = context.getEvent();
			Map<String, String> pm = context.getMonitorParam().getParamMap();
			String temp = title;
			// 事件监测标题替换: ${eventTitle} or ${keyWords}监测
			String eventTitle = event.getMsgTitle() != null && event.getMsgTitle().length() > 0 ? event.getMsgTitle() : (pm
					.get(MonitorParam.KEY_WORDS) + "监测");
			temp = temp.replaceAll(escaped7, eventTitle);

			event.setMsgTitle(temp);
			if (MonitorState.ERROR.equals(context.getMonitorState())) {
				if (Double.parseDouble(pm.get(MonitorParam.THRESHOLD)) == 1.0) {
					// 事件监测单条异常内容替换： 收到异常事件：${eventContent}
					temp = singleAlertContent.replaceAll(escaped4, event.getMsgContent());
				} else {
					// 事件监测多条异常内容替换： 已连续${duration}收到${threshold}条“${keyWords}”事件
					temp = batchAlertContent.replaceAll(escaped9, pm.get(MonitorParam.DURATION))
							.replaceAll(escaped12, pm.get(MonitorParam.THRESHOLD))
							.replaceAll(escaped13, pm.get(MonitorParam.KEY_WORDS));
				}
			} else {
				// 如果为时间条件恢复则替换时间为中文
				if (context.getEventRecover()[0].equals("1")) {
					String eventRecover = context.getEventRecover()[1].replace("m", "分钟").replace("h", "小时");
					// 事件监测时间正常内容替换：最近${recoverTime}内已不再收到“${keyWords}”事件
					temp = timeOkContent.replaceAll(escaped14, eventRecover)
							.replaceAll(escaped13, pm.get(MonitorParam.KEY_WORDS));
				} else {
					// 事件监测数量正常内容替换：收到恢复事件：${eventContent}
					temp = numOkContent.replaceAll(escaped4, event.getMsgContent());
				}
			}
			event.setMsgContent(temp);
			return event;
		}
	}

	private static class SMSTemplateConfig {
		private String content;

		public static SMSTemplateConfig parseSMSTemplateConfig(Element e) {
			SMSTemplateConfig temp = new SMSTemplateConfig();
			temp.content = e.element("content").getText();
			return temp;
		}

		public String generateSMS(CheckContext context) {
			// 短信替换： ${monitorState} ${hostName}(${ip}) ${occurTime} ${eventContent}
			String smsTime = DateUtil.formatSimpleTime(context.getEvent().getOccurTime());
			/**
			 * 对短信和站内信做特殊处理去除冗余
			 * 如：10.1.53.100(10.1.53.100)的指标system.cpu.idle已达到49.8%（最近1分钟平均值），大于阈值7.0%
			 * 如有细化分组条件：
			 * 10.1.53.100(10.1.53.100)的指标 device:D\ system.disk.usage已达到49.8%（最近1分钟平均值），大于阈值7.0%
			 * 只展现的指标后面的内容
			 */
			String eventContent = context.getEvent().getMsgContent();
			String[] temp=eventContent.split("的指标");
			if (temp.length>1){
				eventContent = temp[1];
			}
			//阿里大鱼短信内容带有ip会发送失败，所以将10.1.1.1替换成10_1_1_1
			return content.replaceAll(escaped1, isZH ? context.getMonitorState().getCname() : context.getMonitorState().getCode())
					.replaceAll(escaped5, context.getHostName() != null ? context.getHostName() : "")
					.replaceAll(escaped6, context.getIp() != null ? context.getIp().replace(".","_") : "").replaceAll(escaped3, smsTime)
					.replaceAll(escaped4, eventContent);
		}

		public String generateArbiter(CheckContext context){
			// 替换： ${monitorState} ${hostName}(${ip}) ${occurTime} ${eventContent}
			String smsTime = DateUtil.formatSimpleTime(context.getEvent().getOccurTime());
			/**
			 * 对短信和站内信做特殊处理去除冗余
			 * 如：10.1.53.100(10.1.53.100)的指标system.cpu.idle已达到49.8%（最近1分钟平均值），大于阈值7.0%
			 * 只展现的指标后面的内容
			 */
			String eventContent = context.getEvent().getMsgContent();
			String[] temp=eventContent.split("的指标");
			if (temp.length>1){
				eventContent = temp[1];
			}
			return content.replaceAll(escaped1, context.getMonitorState().getCname())
					.replaceAll(escaped5, context.getHostName() != null ? context.getHostName() : "")
					.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "").replaceAll(escaped3, smsTime)
					.replaceAll(escaped4, eventContent);
		}

	}

	private static class EmailTemplateConfig {
		private String okTitle;
		private String alertTitle;
		private String content;

		public static EmailTemplateConfig parseEmailTemplateConfig(Element e) {
			EmailTemplateConfig temp = new EmailTemplateConfig();
			temp.okTitle = e.element("okTitle").getText();
			temp.alertTitle = e.element("alertTitle").getText();
			temp.content = e.element("content").getText();
			return temp;
		}

		public String generateEmailTitle(CheckContext context) {
			if (MonitorState.ERROR.equals(context.getMonitorState())
					|| MonitorState.WARNING.equals(context.getMonitorState())
					|| MonitorState.INFO.equals(context.getMonitorState())) {
				// 邮件标题替换： ${hostName}(${ip}) ${eventTitle}恢复或报警
				return alertTitle.replaceAll(escaped5, context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "")
						.replaceAll(escaped7, context.getEvent().getMsgTitle());
			} else {
				return okTitle.replaceAll(escaped5, context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "")
						.replaceAll(escaped7, context.getEvent().getMsgTitle());
			}
		}

		public String generateEmailContent(CheckContext context) {
			String emailTime = DateUtil.formatDateTime(context.getEvent().getOccurTime());

			// 邮件内容替换:
			// 资源：${hostName}(${ip})<br>时间：${occurTime}<br>事件：${eventTitle}<br>级别：${monitorState}<br>内容：${eventContent}
			return content.replaceAll(escaped5, context.getHostName() != null ? context.getHostName() : "")
					.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "").replaceAll(escaped3, emailTime)
					.replaceAll(escaped7, context.getEvent().getMsgTitle())
					.replaceAll(escaped1, context.getMonitorState().getCname())
					.replaceAll(escaped4, context.getEvent().getMsgContent());
		}
	}

	private static class HostTemplateConfig {
		private String alertTitle;
		private String okTitle;
		private String okContent;
		private String alertContent;

		public static HostTemplateConfig parseHostTemplateConfig(Element e) {
			HostTemplateConfig temp = new HostTemplateConfig();
			temp.alertTitle = e.element("alertTitle").getText();
			temp.okTitle=e.element("okTitle").getText();
			temp.okContent = e.element("okContent").getText();
			temp.alertContent = e.element("alertContent").getText();
			return temp;
		}

		public Event generateEvent(CheckContext context) {
			Event event = context.getEvent();
			Map<String, String> pm = context.getMonitorParam().getParamMap();
			String title;
			String content;
			if (MonitorState.OK.equals(context.getMonitorState())){
				title=okTitle.replaceAll(escaped5,context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "");
				content=okContent;
			}else{
				title=alertTitle.replaceAll(escaped5,context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "");
				content=alertContent.replaceAll(escaped9,pm.get(MonitorParam.DURATION));
			}
			event.setMsgTitle(title);
			event.setMsgContent(content);
			return event;
		}
	}

	private static class AppTemplateConfig {
		private String alertTitle;
		private String okTitle;
		private String okContent;
		private String alertContent;

		public static AppTemplateConfig parseAppTemplateConfig(Element e) {
			AppTemplateConfig temp = new AppTemplateConfig();
			temp.alertTitle = e.element("alertTitle").getText();
			temp.okTitle=e.element("okTitle").getText();
			temp.okContent = e.element("okContent").getText();
			temp.alertContent = e.element("alertContent").getText();
			return temp;
		}

		public Event generateEvent(CheckContext context) {
			Event event = context.getEvent();
			Map<String, String> pm = context.getMonitorParam().getParamMap();
			String title;
			String content;
			if (MonitorState.OK.equals(context.getMonitorState())){
				title=okTitle.replaceAll(escaped5,context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "").replaceAll(escaped16,pm.get(MonitorParam.APP));
				content=okContent.replaceAll(escaped16,pm.get(MonitorParam.APP));
			}else{
				title=alertTitle.replaceAll(escaped5,context.getHostName() != null ? context.getHostName() : "")
						.replaceAll(escaped6, context.getIp() != null ? context.getIp() : "").replaceAll(escaped16,pm.get(MonitorParam.APP));
				content=alertContent.replaceAll(escaped9,pm.get(MonitorParam.DURATION)).replaceAll(escaped16,pm.get(MonitorParam.APP));
			}
			event.setMsgTitle(title);
			event.setMsgContent(content);
			return event;
		}
	}

}
