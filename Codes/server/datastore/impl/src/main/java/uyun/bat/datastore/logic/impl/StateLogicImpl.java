package uyun.bat.datastore.logic.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.dao.CheckpointDao;
import uyun.bat.datastore.entity.CheckpointRecord;

public class StateLogicImpl {
	private static final Logger logger = LoggerFactory.getLogger(StateLogicImpl.class);
	@Autowired
	private CheckpointDao checkpointDao;

	public void createCheckpoint(String stateId, String objectId, Checkpoint cp, CheckpointRecord record) {
		if (record == null) {
			record = new CheckpointRecord(stateId, objectId, cp.getTimestamp(), cp.getValue(), null, cp.getDescr());
			checkpointDao.createSnapshot(record);
			return;
		}

		if (record.getLastTime() > cp.getTimestamp()) {
			logger.warn(String.format("checkpoint time[%s] cannot before last time[%s]", cp, record.getLastTime()));
		} else if (record.getValue().equals(cp.getValue())) {
			long lastTime = record.getLastTime();
			record.setLastTime(cp.getTimestamp());
			record.setCount(record.getCount() + 1);
			if (checkpointDao.updateSnapshot(record, lastTime) == 0)
				throw new DuplicateKeyException("snapshot update by another transaction");
		} else {
			long lastTime = record.getLastTime();
			record.setLastTime(cp.getTimestamp());
			checkpointDao.createHistory(record);
			CheckpointRecord old = new CheckpointRecord(stateId, objectId, cp.getTimestamp(), cp.getValue(), record.getValue(),
					cp.getDescr());
			if (checkpointDao.updateSnapshot(old, lastTime) == 0)
				throw new DuplicateKeyException("snapshot update by another transaction");
		}
	}
}
