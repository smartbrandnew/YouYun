package uyun.bat.datastore.overview.logic;

public class OverviewLogicManager {
	private static OverviewLogicManager instance = new OverviewLogicManager();

	private OTagLogic oTagLogic;
	private OTagResourceLogic oTagResourceLogic;
	private ResourceMonitorRecordLogic resourceMonitorRecordLogic;

	public static OverviewLogicManager getInstance() {
		return instance;
	}

	public OTagLogic getoTagLogic() {
		return oTagLogic;
	}

	public void setoTagLogic(OTagLogic oTagLogic) {
		this.oTagLogic = oTagLogic;
	}

	public OTagResourceLogic getoTagResourceLogic() {
		return oTagResourceLogic;
	}

	public void setoTagResourceLogic(OTagResourceLogic oTagResourceLogic) {
		this.oTagResourceLogic = oTagResourceLogic;
	}

	public ResourceMonitorRecordLogic getResourceMonitorRecordLogic() {
		return resourceMonitorRecordLogic;
	}

	public void setResourceMonitorRecordLogic(ResourceMonitorRecordLogic resourceMonitorRecordLogic) {
		this.resourceMonitorRecordLogic = resourceMonitorRecordLogic;
	}

}
