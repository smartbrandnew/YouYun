package com.broada.carrier.monitor.server.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.server.api.entity.MonitorProbe;

public class ProbeDao {
	@Autowired
	private BaseDao dao;

	public MonitorProbe[] getAll() {		
		return dao.queryForArray(new PrepareQuery("from MonitorProbe"), new MonitorProbe[0]);			
	}	
	
	public int save(MonitorProbe probe) {
		if (probe.getId() == 0)
			dao.create(probe);
		else
			dao.save(probe);
		return probe.getId();
	}
	
	public void delete(int id) {
		dao.delete(MonitorProbe.class, id);
	}

	public MonitorProbe get(int id) {
		return dao.get(MonitorProbe.class, id);
	}
}
