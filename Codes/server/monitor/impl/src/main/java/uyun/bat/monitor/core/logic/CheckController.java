package uyun.bat.monitor.core.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;

import uyun.bat.common.config.Config;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.Resource;
import uyun.bat.event.api.entity.Event;
import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.AutoRecoverRecord;
import uyun.bat.monitor.api.entity.Monitor;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.api.entity.NotifyRecord;
import uyun.bat.monitor.core.entity.CheckContext;
import uyun.bat.monitor.core.entity.Symbol;
import uyun.bat.monitor.core.util.ArbiterSenderUtil;
import uyun.bat.monitor.core.util.MonitorQueryUtil;
import uyun.bat.monitor.core.util.NotifyTemplateConfig;
import uyun.bat.monitor.impl.common.ServiceManager;
import uyun.bat.monitor.impl.logic.AutoRecoverTask;
import uyun.bat.monitor.impl.logic.LogicManager;
import uyun.bird.notify.api.EmailParams;
import uyun.bird.notify.api.Params;
import uyun.bird.notify.api.SmsParams;
import uyun.bird.notify.api.imsg.IMsgParams;
import uyun.bird.notify.api.imsg.IMsgReceiverType;
import uyun.bird.tenant.api.entity.User;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

import com.alibaba.dubbo.common.URL;

/**
 * 监测器触发管理
 */
public class CheckController {
	private static final Logger logger = LoggerFactory.getLogger(CheckController.class);
	// 监测器吞吐量计数器
	private static AtomicLong atomic = new AtomicLong();

	private static CheckController instance = new CheckController() {
	};

	public static CheckController getInstance() {
		return instance;
	}

	public long getMonitorTps() {
		return atomic.longValue();
	}

	/**
	 * 执行监测器
	 * 
	 * @param checker
	 * @return
	 */
	public void check(Checker checker) {
		Monitor monitor = checker.getMonitor();
		if (monitor == null)
			return;
		// 检查监测器状态是否翻转
		MonitorState state = checker.checkIfMonitorStatusRollover();
		if (state != null && !state.equals(monitor.getMonitorState())) {
			// 监测器状态翻转，更新监测器状态
			monitor.setMonitorState(state);
			LogicManager.getInstance().getMonitorLogic().updateMonitorState(monitor);
		}
		checker.doAfterCheck();
		atomic.incrementAndGet();
		return;
	}

	/**
	 * 监测器触发某事件
	 * 
	 * @param monitor
	 * @param context
	 * @param symbol 事件标识字符串
	 * @return
	 */
	public boolean trigger(Monitor monitor, CheckContext context, Symbol symbol) {
		// 暂缺分布式事务
		if (context.getEvent() == null || monitor == null || symbol == null)
			return false;

		String[] tags = symbol.generateTags();
		String state = symbol.generateState();
		String resourceId = symbol.getResourceId();

		Checkpoint[] checkpoints = ServiceManager.getInstance().getStateService().getCheckpoints(state, tags);

		if (checkpoints != null && checkpoints.length > 0) {
			// 由于tags可能会变，故还是取时间戳最后的作为最后的检查点
			Checkpoint lastCheckpoint = checkpoints[0];
			if (checkpoints.length > 1) {
				for (int i = 1; i < checkpoints.length; i++) {
					Checkpoint cp = checkpoints[i];
					if (cp.getTimestamp() > lastCheckpoint.getTimestamp()) {
						lastCheckpoint = cp;
					}
				}
			}
			try {
				context.setLastMonitorState(MonitorState.checkByCode(lastCheckpoint.getValue()));
			} catch (Exception e) {
				// 此处不做处理
			}

			if (symbol.getMonitorState().getCode().equals(lastCheckpoint.getValue())) {
				// 若该类事件已触发过，且该状态不变，则不再触发
				saveCheckpoint(resourceId, monitor, context.getEvent(), state, tags);
				return false;
			}
		} else {
			MonitorState ms = MonitorQueryUtil.getMonitorStatus(context.getEvent().getServerity());
			if (MonitorState.OK.equals(ms)) {
				// 若该类事件未触发，且当前为正常状态，则不需要触发
				saveCheckpoint(resourceId, monitor, context.getEvent(), state, tags);
				return false;
			}
		}
		// 是否自愈
		if (Config.getInstance().get("auto.push.mode", false))
			autoRecover(context, monitor);
		// 设置事件的标题和内容
		Event complexEvent = NotifyTemplateConfig.getInstance().generateEvent(context);
		/**
		 * 对事件台展现的内容做特殊处理去除冗余
		 * 如：10.1.53.100(10.1.53.100)的指标system.cpu.idle已达到49.8%（最近1分钟平均值），大于阈值7.0%
		 * 事件台只需展现的指标后面的内容
		 */
		String[] temp = complexEvent.getMsgContent().split("的指标");
		String preStr;
		String sufStr = "";
		if (temp.length > 1) {
			preStr = temp[0] + "的指标";
			sufStr = temp[1];
			complexEvent.setMsgContent(sufStr);
		} else {
			preStr = complexEvent.getMsgContent();
		}

		// 查询该监测器的该事件
		complexEvent.setIdentity(symbol.toString());
		complexEvent.setId(UUIDTypeHandler.createUUID());

		// 保存事件
		complexEvent = ServiceManager.getInstance().getEventService().create(complexEvent);
		if (complexEvent == null)
			return false;
		/** 再把冗余加回来 */
		complexEvent.setMsgContent(preStr + sufStr);
		context.setEvent(complexEvent);
		saveCheckpoint(resourceId, monitor, context.getEvent(), state, tags);
		return true;
	}

	// 状态触发过 需要一直发送状态
	private void saveCheckpoint(String resourceId, Monitor monitor, Event event, String state, String[] tags) {
		String tenantId = monitor.getTenantId();
		Set<String> resTags = new HashSet<String>();
		if (null != resourceId && resourceId.length() > 0) {
			Resource resource = ServiceManager.getInstance().getResourceService().queryResById(resourceId, tenantId);
			if (null != resource) {
				resTags.add(StateUtil.HOST + ":" + resource.getHostname());
			}
		}
		if (tags != null && tags.length > 0) {
			for (String s : tags) {
				resTags.add(s);
			}
		}
		Checkpoint checkpoint = new Checkpoint();
		checkpoint.setState(state);
		checkpoint.setTags(resTags.toArray(new String[resTags.size()]));
		checkpoint.setTimestamp(event.getOccurTime().getTime());
		checkpoint.setValue(MonitorQueryUtil.getMonitorStatus(event.getServerity()).getCode());
		checkpoint.setDescr(monitor.getName());
		ServiceManager.getInstance().getStateService().saveCheckpoint(checkpoint);
	}

	/**
	 * 发送通知
	 *
	 * @param context
	 * @param users 用户id列表
	 * @return
	 */
	public boolean notify(CheckContext context, List<String> users) {
		if (users == null || users.size() == 0)
			return true;

		try {
			List<User> us = ServiceManager.getInstance().getUserService().listByUserIds(users.toArray(new String[0]));
			// 如果没有相关用户，则直接算发送成功了
			if (us == null || us.size() == 0)
				return true;

			List<String> ids = new ArrayList<String>();
			List<String> emails = new ArrayList<String>();
			List<String> phones = new ArrayList<String>();
			List<String> userNames = new ArrayList<String>();
			for (User u : us) {
				ids.add(u.getUserId());
				emails.add(u.getEmail());
				userNames.add(u.getRealname());
				phones.add(u.getMobile());
			}

			// 发邮件
			String emailTitle = NotifyTemplateConfig.getInstance().generateEmailTitle(context);
			String emailContent = NotifyTemplateConfig.getInstance().generateEmailContent(context);

			EmailParams params = Params.email(emailTitle, emailContent).to(emails.toArray(new String[0]));
			try {
				ServiceManager.getInstance().getNotifyService().sendEmail(params);
			} catch (Exception e) {
				logger.warn("send mail error", e);
			}

			// 发短信
			String smsContent = NotifyTemplateConfig.getInstance().generateSMS(context);
			SmsParams smsParams = Params.sms(smsContent).to(phones.toArray(new String[0]));
			try {
				ServiceManager.getInstance().getNotifyService().sendSms(smsParams, "SMS_13011654");
			} catch (Exception e) {
				logger.warn("send sms error", e);
			}

			// 发站内信
			String source = Config.getInstance().get("product.bat.productNum", "1001");
			for (String userId : ids) {
				IMsgParams iMsgParams = IMsgParams.imsg(IMsgParams.SENDER_SYSTEM, smsContent, userId)
						.receiverType(IMsgReceiverType.USER).sendTime(context.getEvent().getOccurTime()).source(source);
				try {
					ServiceManager.getInstance().getiMessageService().send(iMsgParams);
				} catch (Exception e) {
					logger.warn("send notify error", e);
				}
			}

			// 发送trap到Arbiter
			if (Config.getInstance().get("snmp.send",false) && context.getMonitorState() != null) {
				if (context.getMonitorState().equals(MonitorState.ERROR)
						|| context.getMonitorState().equals(MonitorState.WARNING)|| context.getMonitorState().equals(MonitorState.OK)) {
					String content=NotifyTemplateConfig.getInstance().generateArbiter(context);
					String monitorType = context.getMonitorType().getCode();
					if(monitorType!=null){
						switch(monitorType){
						case "metric":
							PDU pdu =ArbiterSenderUtil.generatePerformenceMetricAlertMsg(context, content);
							ArbiterSenderUtil.sendPDU(pdu);break;
						case "event":ArbiterSenderUtil.sendPDU(ArbiterSenderUtil.generateAbnormalEventAlertMsg(context, content));break;
						case "host":ArbiterSenderUtil.sendPDU(ArbiterSenderUtil.generateHostOrAppUnableAlertMsg(context, content));break;
						case "app":ArbiterSenderUtil.sendPDU(ArbiterSenderUtil.generateHostOrAppUnableAlertMsg(context, content));break;
						//default: sender.sendPDU(ArbiterSenderUtil.generatePerformenceMetricAlertMsg(context, smsContent));
						}
					}
					//sender.sendPDU(sender.generatePerformenceMetricAlertMsg(context, smsContent));
				}
			}


			// 保存通知记录
			StringBuilder sb = new StringBuilder();
			for (String userName : userNames) {
				sb.append(userName);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);

			NotifyRecord notifyRecord = new NotifyRecord();
			notifyRecord.setId(UUIDTypeHandler.createUUID());
			notifyRecord.setMonitorId(context.getEvent().getMonitorId());
			notifyRecord.setContent(context.getEvent().getMsgContent());
			notifyRecord.setName(sb.toString());
			notifyRecord.setTime(context.getEvent().getOccurTime());
			notifyRecord.setTenantId(context.getEvent().getTenantId());
			notifyRecord = LogicManager.getInstance().getNotifyRecordLogic().createNotifyRecord(notifyRecord);
			return true;
		} catch (Throwable e) {
			// 暂缺发送通知失败的处理逻辑，只能默默画圈圈
			if (logger.isWarnEnabled())
				logger.warn("notify failed:" + e.getMessage());
			if (logger.isDebugEnabled())
				logger.warn("Stack:", e);

			return false;
		}
	}

	private void autoRecover(CheckContext context, Monitor monitor) {
		// 监测器的主机状态是非正常的
		boolean isWrong = !MonitorState.OK.equals(context.getMonitorState());

		AutoRecoverRecord arr = LogicManager.getInstance().getAutoRecoverRecordLogic()
				.getByResId(monitor.getTenantId(), context.getResId(), monitor.getId());
		if (arr == null && isWrong && null != monitor.getAutoRecoveryParams()
				&& null != monitor.getAutoRecoveryParams().getId()) {
			// automation的编排任务ID
			String executeId = monitor.getAutoRecoveryParams().getId();
			String params = URL.decode(monitor.getAutoRecoveryParams().getParams()).replace("${hostId}", context.getIp());
			// 该监测器执行编排任务时间
			long interval = PeriodUtil.generatePeriod(monitor.getAutoRecoveryParams().getTime()).getInterval();
			logger.debug("interval:" + interval + "Tenant id: " + monitor.getTenantId());
			if (interval == 0) {
				new AutoRecoverTask().generateData(monitor.getTenantId(), monitor.getName(), "", params, executeId, interval);
			} else {
				AutoRecoverRecord autoRecoverRecord = new AutoRecoverRecord(UUIDTypeHandler.createUUID(), monitor.getId(),
						monitor.getName(), context.getResId(), context.getHostName(), new Date(), monitor.getTenantId(), executeId,
						params, interval);
				LogicManager.getInstance().getAutoRecoverRecordLogic().createAutoRecoverRecord(autoRecoverRecord);
			}
		}
		if (MonitorState.OK.equals(context.getMonitorState()))
			LogicManager.getInstance().getAutoRecoverRecordLogic().deleteByResId(monitor.getTenantId(), context.getResId());
	}

}
