package com.broada.carrier.monitor.probe.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

public class TempDataDao {
	@Autowired
	private BaseDao dao;

	public void save(MonitorTempData tempData) {
		dao.save(tempData);
	}
	
	public MonitorTempData get(String taskId) {
		return dao.get(MonitorTempData.class, taskId);
	}
}
