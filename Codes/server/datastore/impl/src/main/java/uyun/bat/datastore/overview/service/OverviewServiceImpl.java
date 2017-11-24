package uyun.bat.datastore.overview.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.overview.entity.TagResourceData;
import uyun.bat.datastore.api.overview.service.OverviewService;
import uyun.bat.datastore.overview.logic.OverviewLogicManager;

public class OverviewServiceImpl implements OverviewService {

	@Override
	public List<String> getOverviewTagKeyList(String tenantId) {
		return OverviewLogicManager.getInstance().getoTagLogic().getTagKeyListByTenantId(tenantId);
	}

	@Override
	public List<TagResourceData> getOverviewData(String tenantId) {
		return OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().getOverviewData(tenantId);
	}

	@Override
	public List<TagResourceData> getTagResourceDataList(String tenantId, String key) {
		return OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().getTagResourceDataList(tenantId, key);
	}

	@Override
	public TagResourceData getTagResourceData(String tenantId, String key, String value) {
		return OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().getTagResourceData(tenantId, key, value);
	}

	@Override
	public Map<String, ResourceMonitorRecord> queryResourceMonitorRecord(String tenantId) {
		return OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().queryResourceMonitorRecord(tenantId);
	}

	@Override
	public Set<String> queryResIdByErrorRecord(String tenantId) {
		return OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().queryResIdByErrorRecord(tenantId);
	}
}
