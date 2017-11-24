package uyun.bat.monitor.impl.logic;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import uyun.bat.monitor.api.entity.NotifyRecord;
import uyun.bat.monitor.api.entity.PageNotifyRecord;
import uyun.bat.monitor.impl.Startup;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;

/**
 * 监测器触发所调用的特殊方法测试
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotifyRecordLogicTest {
	private static final String TENANT_ID = "94baaadca64344d2a748dff88fe7159e";

	private static String monitorId = UUIDTypeHandler.createUUID();

	public NotifyRecordLogicTest() {
		Startup.getInstance().startup();
	}

	@Test
	public void test1CreateNotifyRecord() {
		NotifyRecord notifyRecord = new NotifyRecord();
		notifyRecord.setId(UUIDTypeHandler.createUUID());
		notifyRecord.setMonitorId(monitorId);
		notifyRecord.setContent("张三家被盗");
		notifyRecord.setName("张三");
		notifyRecord.setTime(new Date());
		notifyRecord.setTenantId(TENANT_ID);
		notifyRecord = LogicManager.getInstance().getNotifyRecordLogic().createNotifyRecord(notifyRecord);
		String id = notifyRecord.getId();
		assertTrue(id.length() > 0);
	}

	@Test
	public void test2GetNotifyRecordList() {
		PageNotifyRecord pnr = LogicManager.getInstance().getNotifyRecordLogic()
				.getNotifyRecordList(TENANT_ID, monitorId, 1, 20,"4h");
		assertTrue(pnr.getNotifyRecords() != null && pnr.getNotifyRecords().size() >= 0);
	}

	@Test
	public void test3DeleteByMonitorId() {
		boolean isDeleted = LogicManager.getInstance().getNotifyRecordLogic().deleteByMonitorId(TENANT_ID, monitorId);
		assertTrue(isDeleted);
	}

}
