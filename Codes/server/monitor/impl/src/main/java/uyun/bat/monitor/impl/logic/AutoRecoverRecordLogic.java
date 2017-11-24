package uyun.bat.monitor.impl.logic;

import uyun.bat.monitor.api.entity.AutoRecoverRecord;
import uyun.bat.monitor.impl.dao.AutoRecoverRecordDao;

import javax.annotation.Resource;
import java.util.List;

public class AutoRecoverRecordLogic {
	@Resource
	private AutoRecoverRecordDao autoRecoverRecordDao;

	public AutoRecoverRecordDao getAutoRecoverRecordDao() {
		return autoRecoverRecordDao;
	}

	public void setAutoRecoverRecordDao(AutoRecoverRecordDao autoRecoverRecordDao) {
		this.autoRecoverRecordDao = autoRecoverRecordDao;
	}

	public AutoRecoverRecord createAutoRecoverRecord(AutoRecoverRecord autoRecoverRecord) {
		autoRecoverRecordDao.createAutoRecoverRecord(autoRecoverRecord);
		return autoRecoverRecord;
	}

	public boolean deleteByResId(String tenantId, String resId) {
		return autoRecoverRecordDao.deleteByResId(tenantId, resId) > 0;
	}

	public List<AutoRecoverRecord> getAutoRecoverRecordList() {
		return autoRecoverRecordDao.getAutoRecoverRecordList();
	}

	public AutoRecoverRecord getByResId(String tenantId, String resId, String monitorId) {
		return autoRecoverRecordDao.getByResId(tenantId, resId, monitorId);
	}

	public boolean deleteByMonitorId(String tenantId, String monitorId) {
		return autoRecoverRecordDao.deleteByMonitorId(tenantId, monitorId) > 0;
	}
}
