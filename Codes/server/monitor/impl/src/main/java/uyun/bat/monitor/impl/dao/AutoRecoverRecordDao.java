package uyun.bat.monitor.impl.dao;

import uyun.bat.monitor.api.entity.AutoRecoverRecord;

import java.util.List;

public interface AutoRecoverRecordDao {

	int createAutoRecoverRecord(AutoRecoverRecord autoRecoverRecord);

	int deleteByResId(String tenantId, String resId);

	AutoRecoverRecord getByResId(String tenantId, String resId, String monitorId);

	int deleteByMonitorId(String tenantId, String monitorId);

	List<AutoRecoverRecord> getAutoRecoverRecordList();

}
