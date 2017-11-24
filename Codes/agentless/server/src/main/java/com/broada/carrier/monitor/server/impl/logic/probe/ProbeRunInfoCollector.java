package com.broada.carrier.monitor.server.impl.logic.probe;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.config.SimpleProperties;
import com.broada.carrier.monitor.probe.api.client.ProbeServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;
import com.broada.carrier.monitor.server.api.entity.MonitorProbeStatus;
import com.broada.carrier.monitor.server.api.entity.OnlineState;
import com.broada.carrier.monitor.server.impl.config.Config;
import com.broada.carrier.monitor.server.impl.logic.ServerProbeServiceImpl;

/**
 * 负责收集Probe的实时运行信息。
 * 
 * @author chenliang1
 */
public class ProbeRunInfoCollector extends TimerTask {

	private static final Log logger = LogFactory.getLog(ProbeRunInfoCollector.class);

	@Autowired
	private ServerProbeServiceImpl probeService;

	private static int DEFAULT_TRY_TIME = 2;
	private static int DEFAULT_TRY_INTERVAL = 5000;

	/**
	 * 调度周期(单位：毫秒)
	 */
	private static long interval = 5 * 60;
	private Timer timer;

	static {
		// 从配置文件中读取变量
		loadSettings();
	}

	private static ProbeRunInfoCollector instance;

	private ProbeRunInfoCollector() {
	}

	/**
	 * 获取默认实例
	 * 
	 * @return
	 */
	public static ProbeRunInfoCollector getDefault() {
		if (instance == null) {
			synchronized (ProbeRunInfoCollector.class) {
				if (instance == null)
					instance = new ProbeRunInfoCollector();
			}
		}
		return instance;
	}

	/**
	 * 从配置文件中读取变量
	 */
	private static void loadSettings() {
		SimpleProperties props = Config.getDefault().getProps();
		interval = props.get("numen.server.probemgr.audit.interval", interval);
		DEFAULT_TRY_TIME = props.get("numen.server.probemgr.audit.tryTime", 2);
		DEFAULT_TRY_INTERVAL = props.get("numen.server.probemgr.audit.tryInterval", 5000);
	}

	/**
	 * 检查指定Probe的在线（是否可以成功连接）状态，当发生状态变化（由不可连接变成可连接，或可连接变成不可连接）时，
	 * 调用ProbeStatusChangeNotifer发出通知。
	 * 
	 * @param probe
	 */
	public MonitorProbeStatus checkStatus(MonitorProbe probe) {
		OnlineState onlineState = OnlineState.OFFLINE;
		MonitorProbeStatus oldState = probeService.getProbeStatus(probe.getId());
		for (int i = 0; i < DEFAULT_TRY_TIME; i++) {
			try {
				ProbeServiceFactory probeFactory = probeService.checkProbeFactory(probe.getId());
				probeFactory.getSystemService().getTime();
				onlineState = OnlineState.ONLINE;
				break;
			} catch (Throwable e) {
				logger.warn(String.format("检查Probe[%s ip:%s port:%d]在线状态失败，当前尝试第%d次。错误：%s", probe.getCode(), probe.getHost(),
						probe.getPort(), i + 1, e));
				logger.debug("堆栈：", e);

				try {
					Thread.sleep(DEFAULT_TRY_INTERVAL);
				} catch (InterruptedException e1) {
					break;
				}
			}
		}

		if (logger.isDebugEnabled())
			logger.debug(String.format("探针状态监测状态为%s，探针[%s]", onlineState, getProbeDescr(probe)));

		MonitorProbeStatus state = new MonitorProbeStatus(probe.getId(), onlineState, new Date());
		probeService.saveProbeState(state);

		if (oldState.getOnlineState() != OnlineState.UNKNOWN && oldState.getOnlineState() != state.getOnlineState()) {
			if (onlineState == OnlineState.ONLINE) {
				logger.info(String.format("探针状态恢复为在线，探针[%s]", getProbeDescr(probe)));
				ProbeStatusAlertProcessor.probeOnline(probe);
			} else {
				logger.warn(String.format("探针状态异常为下线，探针[%s]", getProbeDescr(probe)));
				ProbeStatusAlertProcessor.probeOffline(probe);
			}
			return state;
		}

		return state;
	}

	private String getProbeDescr(MonitorProbe probe) {
		return String.format("code: %s name: %s addr:%s:%d", probe.getCode(), probe.getName(), probe.getHost(),
				probe.getPort());
	}

	/**
	 * 获取Probe列表，调用doCollect方法以记录该Probe的运行信息。
	 */
	public void run() {
		if (logger.isDebugEnabled())
			logger.debug("探针状态监测开始");
		MonitorProbe[] probes = null;
		try {
			probes = probeService.getProbes();
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error("获取Probe列表时发生错误", e);
			}
			return;
		}
		if (probes == null || probes.length == 0)
			return;
		for (MonitorProbe probe : probes) {
			if (logger.isDebugEnabled())
				logger.debug(String.format("探针状态监测开始，探针[%s]", getProbeDescr(probe)));
			try {
				checkStatus(probe);
			} catch (Exception e) {
				logger.warn(String.format("探针状态监测失败，探针[%s]。错误：%s", getProbeDescr(probe), e));
				logger.debug("堆栈：", e);
			}
		}
	}

	/**
	 * 启动探针轮询
	 */
	public void startup() {
		if (timer == null) {
			if (interval <= 0) {
				logger.info("探针信息采集被禁用，周期配置值：" + interval);
			} else {
				if (logger.isDebugEnabled())
					logger.debug(String.format("探针状态监测轮询开始，每隔[%ds]轮询一次", interval));
				Timer timer = new Timer(getClass().getSimpleName());
				timer.schedule(this, 60 * 1000, interval * 1000);
			}
		}
	}
}