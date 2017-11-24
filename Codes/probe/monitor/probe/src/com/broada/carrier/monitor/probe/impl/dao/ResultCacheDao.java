package com.broada.carrier.monitor.probe.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.probe.impl.entity.MonitorResultCache;

public class ResultCacheDao {
	@Autowired
	private BaseDao dao;
	
	public int save(MonitorResultCache cache) {
		if (cache.getId() > 0)
			throw new IllegalArgumentException();
		dao.create(cache);
		return cache.getId();
	}
	
	public void delete(int cacheId) {
		dao.delete(MonitorResultCache.class, cacheId);
	}

	public int getCount() {
		Number count = (Number) dao.queryForObject("select count(*) from MonitorResultCache");
		return count == null ? 0 : count.intValue();
	}

	public MonitorResultCache[] get(PageNo pageNo) {
		return dao.queryForPage("from MonitorResultCache order by id", pageNo, new MonitorResultCache[0]).getRows();		
	}
}
