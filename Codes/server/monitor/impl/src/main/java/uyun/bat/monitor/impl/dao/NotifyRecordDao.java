package uyun.bat.monitor.impl.dao;

import java.util.Date;
import java.util.List;

import uyun.bat.monitor.api.entity.NotifyRecord;

public interface NotifyRecordDao {

	int createNotifyRecord(NotifyRecord notifyRecord);

	int deleteByMonitorId(String tenantId, String monitorId);

	List<NotifyRecord> getNotifyRecordList(String tenantId, String monitorId, Date startTime, Date endTime);
}
