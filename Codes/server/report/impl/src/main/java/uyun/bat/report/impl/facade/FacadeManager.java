package uyun.bat.report.impl.facade;

public abstract class FacadeManager {
	private static FacadeManager instance = new FacadeManager() {
	};

	public static FacadeManager getInstance() {
		return instance;
	}

	private ReportFacade reportFacade;

	private ReportGroupFacade reportGroupFacade;

	public ReportFacade getReportFacade() {
		return reportFacade;
	}

	public void setReportFacade(ReportFacade reportFacade) {
		this.reportFacade = reportFacade;
	}

	public ReportGroupFacade getReportGroupFacade() {
		return reportGroupFacade;
	}

	public void setReportGroupFacade(ReportGroupFacade reportGroupFacade) {
		this.reportGroupFacade = reportGroupFacade;
	}
}
