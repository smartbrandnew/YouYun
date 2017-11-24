package uyun.bat.datastore.overview;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.datastore.Startup;
import uyun.bat.datastore.api.overview.entity.OTag;
import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.overview.entity.TagResourceData;
import uyun.bat.datastore.overview.entity.OTagResource;
import uyun.bat.datastore.overview.logic.OverviewLogicManager;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ResourceMonitorRecordLogicTest {
	private static final String RESOURCE_ID_1 = "e0a67e986a594a61b3d1e523a0a39c76";
	private static final String RESOURCE_ID_2 = "e0a67e986a594a61b3d1e523a0a39c75";

	private static final String TENANT_ID = "e0a67e986a594a61b3d1e523a0a39d88";

	private static final String TAG_ID_1 = "e0a67e986a594a61b3d1e523a0a39c81";
	private static final String TAG_ID_2 = "e0a67e986a594a61b3d1e523a0a39c82";
	private static final String TAG_ID_3 = "e0a67e986a594a61b3d1e523a0a39c83";

	private static final String MONITOR_ID_1 = "e0a67e986a594a61b3d1e523a0a39c78";
	private static final String MONITOR_ID_2 = "e0a67e986a594a61b3d1e523a0a39c79";
	private static final String MONITOR_ID_3 = "e0a67e986a594a61b3d1e523a0a39c80";

	@Before
	public void setUp() throws Exception {
		Startup.getInstance().startup();
	}

	@Test
	public void test1Create() {
		/**
		 * RESOURCE_ID_1  MONITOR_ID_1 ok
		 * RESOURCE_ID_1  MONITOR_ID_2     warn
		 * RESOURCE_ID_1  MONITOR_ID_3           error
		 */
		long t1 = System.currentTimeMillis();
		List<ResourceMonitorRecord> recordList = new ArrayList<ResourceMonitorRecord>();
		ResourceMonitorRecord record = new ResourceMonitorRecord(TENANT_ID, RESOURCE_ID_1, MONITOR_ID_1, t1);
		record.setWarn(true);
		recordList.add(record);
		record = new ResourceMonitorRecord(TENANT_ID, RESOURCE_ID_1, MONITOR_ID_2, t1);
		record.setOk(true);
		recordList.add(record);
		record = new ResourceMonitorRecord(TENANT_ID, RESOURCE_ID_1, MONITOR_ID_3, t1);
		record.setError(true);
		recordList.add(record);
		
		//测试主键冲突时条件更新
		long t2 = t1 + 1;
		record = new ResourceMonitorRecord(TENANT_ID, RESOURCE_ID_1, MONITOR_ID_1, t2);
		record.setOk(true);
		recordList.add(record);
		record = new ResourceMonitorRecord(TENANT_ID, RESOURCE_ID_1, MONITOR_ID_2, t2);
		record.setWarn(true);
		recordList.add(record);

		int flag = OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().save(recordList);
		assertTrue(flag >= 0);
	}

	@Test
	public void test2GetTagResourceData() {
		/**
		 * TENANT_ID TAG_ID_1  TENANT_ID  k   v
		 * TENANT_ID TAG_ID_2  TENANT_ID  k   ''
		 * TENANT_ID TAG_ID_3  TENANT_ID  ''   v
		 */
		/**
		 * TENANT_ID TAG_ID_1  RESOURCE_ID_1
		 * TENANT_ID TAG_ID_2  RESOURCE_ID_1
		 * TENANT_ID TAG_ID_2  RESOURCE_ID_2
		 */
		/**
		 * 见头上的测试数据
		 * TENANT_ID RESOURCE_ID_1  MONITOR_ID_1 ok
		 * TENANT_ID RESOURCE_ID_1  MONITOR_ID_2     warn
		 * TENANT_ID RESOURCE_ID_1  MONITOR_ID_3           error
		 */
		List<OTag> tagList = new ArrayList<OTag>();
		OTag tag = new OTag(TENANT_ID, "k", "v");
		tag.setId(TAG_ID_1);
		tagList.add(tag);
		tag = new OTag(TENANT_ID, "k", null);
		tag.setId(TAG_ID_2);
		tagList.add(tag);
		tag = new OTag(TENANT_ID, null, "v");
		tag.setId(TAG_ID_3);
		tagList.add(tag);
		tag = new OTag(TENANT_ID, null, null);
		tagList.add(tag);
		int flag = OverviewLogicManager.getInstance().getoTagLogic().createOTag(tagList);
		assertTrue(flag >= 0);
		
		List<OTagResource> oTagResourceList = new ArrayList<OTagResource>();
		OTagResource oTagResource = new OTagResource(TENANT_ID, TAG_ID_1, RESOURCE_ID_1);
		oTagResourceList.add(oTagResource);
		oTagResource = new OTagResource(TENANT_ID, TAG_ID_2, RESOURCE_ID_1);
		oTagResourceList.add(oTagResource);
		oTagResource = new OTagResource(TENANT_ID, TAG_ID_2, RESOURCE_ID_2);
		oTagResourceList.add(oTagResource);
		flag = OverviewLogicManager.getInstance().getoTagResourceLogic().create(oTagResourceList);
		assertTrue(flag >= 0);
		
		List<TagResourceData>  tagResourceDatas = OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().getTagResourceDataList(TENANT_ID, "k");
		assertTrue(tagResourceDatas.size() == 2);
		TagResourceData tagResourceData = OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().getTagResourceData(TENANT_ID, "k", "v");
		assertTrue(tagResourceData != null);
		flag = OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().delete(TENANT_ID, null, null);
		assertTrue(flag >= 0);
	}

}
