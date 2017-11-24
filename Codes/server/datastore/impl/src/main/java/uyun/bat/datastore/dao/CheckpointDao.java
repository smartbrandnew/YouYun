package uyun.bat.datastore.dao;

import org.apache.ibatis.annotations.Param;
import uyun.bat.datastore.entity.CheckpointRecord;

import java.util.List;

public interface CheckpointDao {
	CheckpointRecord getSnapshot(String stateId, String objectId);

	void createSnapshot(CheckpointRecord record);

	int updateSnapshot(CheckpointRecord record, long lastTime);

	void createHistory(CheckpointRecord record);

	void deleteSnapshot(@Param("stateId") String stateId, @Param("objectId") String objectId);

	void deleteHistory(@Param("stateId") String stateId, @Param("objectId")  String objectId);

	void deleteSnapshotByStateId(String stateId);

	void deleteHistoryByStateId(String stateId);

	int getSnapshotsCount(String stateId, List<String> tagIds, String value);

	List<CheckpointRecord> getSnapshots(String stateId, List<String> tagIds);

	List<CheckpointRecord> getRecords(String stateId, String objectId, long firstTime, long lastTime);

	List<String> getObjectIdsByStateId(String stateId);

	List<CheckpointRecord> getLastRecords(String stateId, String objectId, long firstTime, long lastTime);

	List<CheckpointRecord> getLastRecordsByObject(String stateId, String objectId);
}
