package uyun.bat.monitor.impl.logic;

/**
 * 逻辑管理层 对facade传来的参数进行逻辑处理及持久化
 */
public abstract class LogicManager {
	private static LogicManager instance = new LogicManager() {
	};

	public static LogicManager getInstance() {
		return instance;
	}

	private MonitorLogic monitorLogic;

	private NotifyRecordLogic notifyRecordLogic;

	private AutoRecoverRecordLogic autoRecoverRecordLogic;

	public MonitorLogic getMonitorLogic() {
		return monitorLogic;
	}

	public void setMonitorLogic(MonitorLogic monitorLogic) {
		this.monitorLogic = monitorLogic;
	}

	public NotifyRecordLogic getNotifyRecordLogic() {
		return notifyRecordLogic;
	}

	public void setNotifyRecordLogic(NotifyRecordLogic notifyRecordLogic) {
		this.notifyRecordLogic = notifyRecordLogic;
	}

	public AutoRecoverRecordLogic getAutoRecoverRecordLogic() {
		return autoRecoverRecordLogic;
	}

	public void setAutoRecoverRecordLogic(AutoRecoverRecordLogic autoRecoverRecordLogic) {
		this.autoRecoverRecordLogic = autoRecoverRecordLogic;
	}
}
