package com.broada.carrier.monitor.server.impl.dao;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.db.BaseDao;
import com.broada.carrier.monitor.common.db.PrepareQuery;
import com.broada.carrier.monitor.server.impl.entity.StateHistory;
import com.broada.carrier.monitor.server.impl.entity.StateLast;
import com.broada.carrier.monitor.server.impl.entity.StateType;

public class StateDao {
	@Autowired 
	private BaseDao dao;
	
	public void save(StateLast last) {
		dao.save(last);
	}
	
	public void save(StateHistory history) {
		dao.save(history);
	}
	
	public StateLast getLast(StateType type, String objectId) {
		return dao.get(StateLast.class, new StateLast.Key(type, objectId));
	}
	
	public StateLast[] getLasts(StateType type, String taskIds) {
		PrepareQuery query = new PrepareQuery("select n from StateLast n");
		return dao.queryForArray(query ,new StateLast[0]);
	}

	public int getLastCountByValue(StateType type, String value) {
		PrepareQuery query = new PrepareQuery("select count(1) from mon_task t1 left join mon_state_last t2 on t1.id=t2.object_id join RES_OBJECT_REAL t3 on t1.node_id=t3.id " +
				"where t2.type="+type.ordinal()
				+" and t2.value="+value);
		query.setNativeSql(true);
		return ((Number) dao.queryForObject(query)).intValue();
	}

	public int deleteLast(StateType type, String objectId) {
		return delete(StateLast.class, type, objectId);		
	}

	private int delete(Class<?> cls, StateType type, String objectId) {
		PrepareQuery query = new PrepareQuery("delete " + cls.getSimpleName());
		query.append("where key.type = ", type);
		query.append("and key.objectId = ", objectId);
		return dao.execute(query);
	}

	public int deleteHistory(StateType type, String objectId) {
		return delete(StateHistory.class, type, objectId);
	}
}
