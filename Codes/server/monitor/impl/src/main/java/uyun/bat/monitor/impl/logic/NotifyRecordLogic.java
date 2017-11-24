package uyun.bat.monitor.impl.logic;

import java.util.Date;

import javax.annotation.Resource;

import uyun.bat.monitor.api.common.util.PeriodUtil;
import uyun.bat.monitor.api.common.util.PeriodUtil.Period;
import uyun.bat.monitor.api.entity.NotifyRecord;
import uyun.bat.monitor.api.entity.PageNotifyRecord;
import uyun.bat.monitor.impl.dao.NotifyRecordDao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

public class NotifyRecordLogic {
	@Resource
	private NotifyRecordDao notifyRecordDao;

	public NotifyRecordDao getNotifyRecordDao() {
		return notifyRecordDao;
	}

	public void setNotifyRecordDao(NotifyRecordDao notifyRecordDao) {
		this.notifyRecordDao = notifyRecordDao;
	}

	public NotifyRecord createNotifyRecord(NotifyRecord notifyRecord) {
		notifyRecordDao.createNotifyRecord(notifyRecord);
		return notifyRecord;
	}

	public boolean deleteByMonitorId(String tenantId, String monitorId) {
		return notifyRecordDao.deleteByMonitorId(tenantId, monitorId) > 0;
	}

	public PageNotifyRecord getNotifyRecordList(String tenantId, String monitorId, int currentPage, int pageSize,
			String timeRange) {
		PageHelper.startPage(currentPage, pageSize);
		Period period = PeriodUtil.generatePeriod(timeRange);
		Date startTime = new Date(period.getStart());
		Date endTime = new Date(period.getEnd());
		Page<NotifyRecord> mr = (Page<NotifyRecord>) notifyRecordDao.getNotifyRecordList(tenantId, monitorId, startTime,
				endTime);
		PageNotifyRecord pageNotifyRecord = new PageNotifyRecord();
		pageNotifyRecord.setCount((int) mr.getTotal());
		pageNotifyRecord.setNotifyRecords(mr.getResult());
		return pageNotifyRecord;
	}
}
