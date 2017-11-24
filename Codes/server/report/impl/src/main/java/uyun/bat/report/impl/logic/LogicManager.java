package uyun.bat.report.impl.logic;

public abstract class LogicManager {
	private static LogicManager instance = new LogicManager() {
	};

	public static LogicManager getInstance() {
		return instance;
	}

	private ReportLogic reportLogic;

	private ReportGroupLogic reportGroupLogic;

	public ReportLogic getReportLogic() {
		return reportLogic;
	}

	public void setReportLogic(ReportLogic reportLogic) {
		this.reportLogic = reportLogic;
	}

	public ReportGroupLogic getReportGroupLogic() {
		return reportGroupLogic;
	}

	public void setReportGroupLogic(ReportGroupLogic reportGroupLogic) {
		this.reportGroupLogic = reportGroupLogic;
	}
}
