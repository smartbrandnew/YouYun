package uyun.bat.web.impl.testservice;

import java.util.*;

import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.overview.entity.TagResourceData;
import uyun.bat.datastore.api.overview.service.OverviewService;

public class OverviewServiceTest implements OverviewService {

	@Override
	public List<TagResourceData> getOverviewData(String tenantId) {
		List<TagResourceData> datas = new ArrayList<TagResourceData>();
		datas.add(new TagResourceData(null, null, 5, 2, 3));
		datas.add(new TagResourceData("三", null, 1, 1, 0));
		datas.add(new TagResourceData("b", null, 1, 0, 1));
		datas.add(new TagResourceData("二", null, 2, 1, 1));
		datas.add(new TagResourceData("a", null, 3, 2, 1));
		datas.add(new TagResourceData("A", null, 1, 0, 1));

		datas.add(new TagResourceData("三", "一", 1, 1, 0));
		datas.add(new TagResourceData("b", "", 1, 0, 1));
		datas.add(new TagResourceData("二", "三", 2, 1, 1));
		datas.add(new TagResourceData("a", "a", 3, 2, 1));
		datas.add(new TagResourceData("a", "", 2, 0, 1));
		datas.add(new TagResourceData("A", "B", 1, 0, 1));
		datas.add(new TagResourceData("", "", 1, 0, 1));

		return datas;
	}

	@Override
	public List<TagResourceData> getTagResourceDataList(String tenantId, String key) {
		List<TagResourceData> tempList = new ArrayList<TagResourceData>();
		TagResourceData temp = new TagResourceData("三", "", 1, 1, 0);
		tempList.add(temp);
		temp = new TagResourceData("三", "1", 1, 0, 0);
		tempList.add(temp);
		temp = new TagResourceData("三", "二", 1, 0, 1);
		tempList.add(temp);
		return tempList;
	}

	@Override
	public TagResourceData getTagResourceData(String tenantId, String key, String value) {
		return new TagResourceData(key, value, 0, 0, 0);
	}

	@Override
	public Map<String, ResourceMonitorRecord> queryResourceMonitorRecord(String tenantId) {
		Map<String, ResourceMonitorRecord> map = new HashMap<>();
		ResourceMonitorRecord r = new ResourceMonitorRecord("21111111164344d2a748dff88fe7159e",
				"21111111164344d2a748dff88fe7159e", "21111111164344d2a748dff88fe7159e", System.currentTimeMillis());
		r.setError(true);
		map.put("21111111164344d2a748dff88fe7159e", r);
		return map;
	}

	@Override
	public Set<String> queryResIdByErrorRecord(String tenantId) {
		Set<String> temp = new HashSet<>();
		temp.add("21111111164344d2a748dff88fe7159e");
		return temp;
	}

	@Override
	public List<String> getOverviewTagKeyList(String tenantId) {
		List<String> keyList = new ArrayList<String>();
		keyList.add("");
		keyList.add("二");
		keyList.add("A");
		keyList.add("a");
		keyList.add("b");
		keyList.add("三");

		return keyList;
	}

}
