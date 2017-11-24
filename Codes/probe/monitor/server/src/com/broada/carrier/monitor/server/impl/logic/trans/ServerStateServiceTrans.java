package com.broada.carrier.monitor.server.impl.logic.trans;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.server.impl.dao.StateDao;
import com.broada.carrier.monitor.server.impl.entity.StateHistory;
import com.broada.carrier.monitor.server.impl.entity.StateLast;
import com.broada.carrier.monitor.server.impl.entity.StateType;

public class ServerStateServiceTrans {
	@Autowired
	private StateDao dao;
	
	public StateLast getStateLast(StateType type, Object objectId) {
		return dao.getLast(type, objectId.toString());
	}
	
	public StateLast[] getStateLasts(StateType type, String taskIds) {
		return dao.getLasts(type, taskIds);
	}
	
	public int getStateLastCountByValue(StateType type, Object value) {
		return dao.getLastCountByValue(type, value.toString());
	}
	
	public void deleteState(StateType type, Object objectId) {
		dao.deleteLast(type, objectId.toString());
		dao.deleteHistory(type, objectId.toString());
	}
	
	/**
	 * 保存状态值
	 * @param type 状态类型
	 * @param objectId 资源ID
	 * @param time 状态采集时间
	 * @param value 当前状态值
	 * @param message 对应消息
	 * @return 返回保存的最后一次状态值
	 */
	public synchronized StateLast saveState(StateType type, String objectId, Date time, String value, String message) {		
		StateLast last = dao.getLast(type, objectId);
		if (last == null) 
			last = new StateLast(type, objectId, time, value, null, message);			
		else {
			if (last.getLastTime().after(time))
				throw new IllegalArgumentException(String.format("保存的状态时间不能早于当前时间[type: %s objId: %s time: %s lastTime: %s]", 
						type, objectId, time, last.getLastTime()));
			
			if (last.getValue().equals(value)) {
				last.setLastTime(time);
				last.setCount(last.getCount() + 1);
				last.setMessage(message);
			} else {
				StateHistory history = new StateHistory(type, objectId, last.getFirstTime(), time, last.getValue(), last.getLastValue(), last.getMessage(), last.getCount());
				dao.save(history);
				
				last = new StateLast(type, objectId, time, value, last.getValue(), message);
			}
		}
		
		dao.save(last);
		return last;
	}

	public StateLast saveState(StateType type, Object objectId, Date time, int value, String message) {
		return saveState(type, objectId.toString(), time, Integer.toString(value), message);
	}
}
