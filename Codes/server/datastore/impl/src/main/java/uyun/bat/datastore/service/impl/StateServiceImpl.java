package uyun.bat.datastore.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.dao.DuplicateKeyException;

import uyun.bat.datastore.api.entity.Checkperiod;
import uyun.bat.datastore.api.entity.Checkpoint;
import uyun.bat.datastore.api.entity.State;
import uyun.bat.datastore.api.overview.entity.ResourceMonitorRecord;
import uyun.bat.datastore.api.service.StateService;
import uyun.bat.datastore.api.service.TagService;
import uyun.bat.datastore.dao.CheckpointDao;
import uyun.bat.datastore.dao.StateDao;
import uyun.bat.datastore.dao.TagObjectDao;
import uyun.bat.datastore.entity.CheckpointRecord;
import uyun.bat.datastore.entity.Tag;
import uyun.bat.datastore.logic.impl.StateLogicImpl;
import uyun.bat.datastore.overview.logic.OverviewLogicManager;
import uyun.bat.monitor.api.common.util.StateUtil;
import uyun.bat.monitor.api.entity.MonitorState;
import uyun.whale.common.mybatis.type.UUIDTypeHandler;
import uyun.whale.common.util.error.ErrorUtil;

// TODO 可以修改为使用各种缓存
public class StateServiceImpl implements StateService {
	private static final String RESOURCE_PREFIX = StateUtil.RESOURCE_ID + ":";
	private static final String TENANT_PREFIX = StateUtil.TENANT_ID + ":";
	private static final String MONITOR_PREFIX = StateUtil.MONITOR_ID + ":";
	
	public static final String TAG_KEY_TENANT_ID = "tenantId";
	public static final String TAG_VALUE_BAT_TENANT_ID = "fb32560b3e0911e69bcb005056a34519";
	private static final Logger logger = LoggerFactory.getLogger(StateServiceImpl.class);
	@Autowired
	private StateDao stateDao;
	@Autowired
	private CheckpointDao checkpointDao;
	@Autowired
	private TagService tagService;
	@Autowired
	private TagObjectDao tagObjectDao;
	@Autowired
	private StateLogicImpl stateLogic;
	
	@Override
	public String saveState(String tenantId, State state) {
		if (state.getId() == null) {
			State exists = stateDao.get(tenantId, state.getName());
			if (exists != null)
				state.setId(exists.getId());
		}
		if (state.getId() != null)
			stateDao.update(state);
		else {
			state.setId(UUIDTypeHandler.createUUID());
			stateDao.create(tenantId, state);
		}
		return state.getId();
	}

	@Override
	public void deleteState(String tenantId, String name) {
		stateDao.delete(tenantId, name);
	}

	@Override
	public State[] getStates(String tenantId) {
		return stateDao.getByTenantId(tenantId);
	}

	@Override
	public void saveCheckpoint(Checkpoint cp) {
		String stateId = checkStateId(getTenantId(cp.getTags()), cp.getState());
		String objectId = tagService.checkObjectId(cp.getTags());
		for (int i = 0; i < 10; i++) {
			CheckpointRecord record = checkpointDao.getSnapshot(stateId, objectId);
			try {
				stateLogic.createCheckpoint(stateId, objectId, cp, record);
				break;
			} catch (DuplicateKeyException e) {
				ErrorUtil.warn(logger, String.format("Thread %s try again time: %d", Thread.currentThread().getId(), i), e);
			} catch (DeadlockLoserDataAccessException e) {
				ErrorUtil.warn(logger, String.format("Thread %s try again time: %d", Thread.currentThread().getId(), i), e);
			}
		}
		
		saveOverviewData(cp);
	}

	private void saveOverviewData(Checkpoint cp) {
		if (cp.getTags() == null)
			return;
		String tenantId = null;
		String monitorId = null;
		String resourceId = null;

		for (String tag : cp.getTags()) {
			if (tag.startsWith(TENANT_PREFIX)) {
				tenantId = tag.substring(9);
			} else if (tag.startsWith(MONITOR_PREFIX)) {
				monitorId = tag.substring(10);
			} else if (tag.startsWith(RESOURCE_PREFIX)) {
				resourceId = tag.substring(11);
			}
		}

		if (tenantId != null && monitorId != null && resourceId != null) {
			try {
				MonitorState monitorState = MonitorState.checkByCode(cp.getValue());
				List<ResourceMonitorRecord> records = new ArrayList<ResourceMonitorRecord>();
				ResourceMonitorRecord record = new ResourceMonitorRecord(tenantId, resourceId, monitorId, cp.getTimestamp());
				if (monitorState == MonitorState.OK)
					record.setOk(true);
				else if (monitorState == MonitorState.WARNING)
					record.setWarn(true);
				else if (monitorState == MonitorState.ERROR)
					record.setError(true);
				else if (monitorState == MonitorState.INFO)
					record.setInfo(true);

				records.add(record);
				OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().save(records);
			} catch (Exception e) {
				// 无缘总览数据
				return;
			}
		}
	}

	private String getTenantId(String[] tags) {
		if (tags != null) {
			for (String tag : tags) {
				if (tag.equals(TAG_KEY_TENANT_ID))
					return Tag.VALUE_EMPTY;
				else if (tag.startsWith(TAG_KEY_TENANT_ID))
					return tag.substring(TAG_KEY_TENANT_ID.length() + 1);
			}
		}
		return TAG_VALUE_BAT_TENANT_ID;
	}

	private String checkStateId(String tenantId, String stateName) {
		State exists = stateDao.get(tenantId, stateName);
		String stateId;
		if (exists == null) {
			State state = new State(stateName);
			try {
				state.setId(UUIDTypeHandler.createUUID());
				stateDao.create(tenantId, state);
				stateId = state.getId();
			} catch (DuplicateKeyException e) {
				stateId = stateDao.get(tenantId, stateName).getId();
			}
		} else
			stateId = exists.getId();
		return stateId;
	}

	@Override
	public Checkpoint getCheckpoint(String state, String[] tags) {
		String stateId = checkStateId(getTenantId(tags), state);
		if (stateId != null) {
			String objectId = tagService.getObjectId(tags);
			if (objectId != null) {
				CheckpointRecord record = checkpointDao.getSnapshot(stateId, objectId);
				if (record != null)
					return new Checkpoint(state, record.getLastTime(), record.getValue(), tags);
			}
		}
		return null;
	}

	@Override
	public void deleteCheckpoints(String state, String[] tags) {
		String stateId=null;
		if (null!=state){
			stateId = checkStateId(getTenantId(tags), state);
		}
		String[] objectIds = tagService.queryObjectIds(tags);
		if (objectIds == null || objectIds.length < 1)
			return;
		for (String objectId : objectIds) {
			checkpointDao.deleteSnapshot(stateId, objectId);
			checkpointDao.deleteHistory(stateId, objectId);
			tagObjectDao.deleteMapByObjectId(objectId);
			tagObjectDao.deleteObject(objectId);
		}
		
		deleteOverviewData(state, tags);
	}
	
	private void deleteOverviewData(String state, String[] tags) {
		if (tags == null)
			return;
		
		String tenantId = null;
		String monitorId = null;

		for (String tag : tags) {
			if (tag.startsWith(TENANT_PREFIX)) {
				tenantId = tag.substring(9);
			} else if (tag.startsWith(MONITOR_PREFIX)) {
				monitorId = tag.substring(10);
			}
		}

		if (tenantId != null && monitorId != null){			
			OverviewLogicManager.getInstance().getResourceMonitorRecordLogic().delete(tenantId, null, monitorId);
		}
	}

	@Override
	public void deleteCheckpoints(String state, String tenantId) {
		String stateId = checkStateId(tenantId, state);
		checkpointDao.deleteSnapshotByStateId(stateId);
		checkpointDao.deleteHistoryByStateId(stateId);
		//咋咩删除objectId？
	}

	@Override
	public int getCheckpointsCount(String state, String[] tags, String value) {
		// TODO 注意这里可能会产生大量的objectId返回
		String stateId = checkStateId(getTenantId(tags), state);
		if (stateId != null) {
			List<String> tagIds = tagService.getTagIds(tags);
			if (tagIds != null)
				return checkpointDao.getSnapshotsCount(stateId, tagIds, value);
		}
		return 0;
	}

	@Override
	public Checkpoint[] getCheckpoints(String state, String[] tags) {
		String stateId = checkStateId(getTenantId(tags), state);
		if (stateId != null) {
			List<String> tagIds = tagService.getTagIds(tags);
			if (tagIds != null) {
				List<CheckpointRecord> items = checkpointDao.getSnapshots(stateId, tagIds);
				if (items != null)
					return convert(items, state);
			}
		}
		return new Checkpoint[0];
	}

	@Override
	public Checkperiod[] getCheckperiods(String state, String[] tags, long firstTime, long lastTime) {
		Checkpoint[] cps = getCheckpoints(state, tags);
		if (cps.length > 1)
			throw new IllegalArgumentException("tags: " + Arrays.toString(tags) + " had multi object");
		else if (cps.length == 1) {
			String objectId = tagService.getObjectId(cps[0].getTags());
			return getCheckperiods(getTenantId(tags), state, objectId, firstTime, lastTime);
		} else
			return new Checkperiod[0];
	}

	@Override
	public Checkperiod[] getCheckperiods(String tenantId, String state, String objectId, long firstTime, long lastTime) {
		String stateId = checkStateId(tenantId, state);
		if (stateId != null) {
			List<CheckpointRecord> items = checkpointDao.getRecords(stateId, objectId, firstTime, lastTime);
			if (items != null) {
				String[] tags = tagService.getObjectTags(objectId);
				return convert(items, state, tags);
			}
		}
		return new Checkperiod[0];
	}

	private Checkperiod[] convert(List<CheckpointRecord> items, String state, String[] tags) {
		Checkperiod[] result = new Checkperiod[items.size()];
		int i = 0;
		for (CheckpointRecord record : items) {
			result[i] = convertPeriod(record, state, tags);
			i++;
		}
		return result;
	}

	private Checkperiod convertPeriod(CheckpointRecord record, String state, String[] tags) {
		return new Checkperiod(state, tags, record.getFirstTime(), record.getLastTime(), record.getValue(),
				record.getPriorValue(), record.getCount(), record.getDescr());
	}

	private Checkpoint[] convert(List<CheckpointRecord> records, String state) {
		Checkpoint[] result = new Checkpoint[records.size()];
		int i = 0;
		for (CheckpointRecord record : records) {
			result[i] = convert(record, state);
			i++;
		}
		return result;
	}

	private Checkpoint convert(CheckpointRecord record, String state) {
		String[] tags = tagService.getObjectTags(record.getObjectId());
		return new Checkpoint(state, record.getLastTime(), record.getValue(), tags);
	}

	public Checkperiod[] getLastCheckperiods(String state,String[] tags,long firstTime,long lastTime){
		String stateId = checkStateId(getTenantId(tags), state);
		if (null==stateId){
			return new Checkperiod[0];
		}
		String[] objectIds = tagService.queryObjectIds(tags);
		if (0==objectIds.length){
			return new Checkperiod[0];
		}
		List<Checkperiod> list=new ArrayList<>();
		for(String objectId:objectIds){
			List<CheckpointRecord> items = checkpointDao.getLastRecords(stateId, objectId, firstTime, lastTime);
			if (items != null&&items.size()>0) {
				String[] resultTags = tagService.getObjectTags(objectId);
				Checkperiod[] checkperiods= convert(items, state, resultTags);
				list.add(checkperiods[0]);
			}
		}
		return list.toArray(new Checkperiod[0]);
	}

	public List<String> getTagsByState(String tenantId,String stateName){
		List<String> tags=new ArrayList<>();
		State state = stateDao.get(tenantId, stateName);
		if (null==state){
			return tags;
		}
		List<String> objectIds=checkpointDao.getObjectIdsByStateId(state.getId());
		if (null==objectIds||objectIds.isEmpty()){
			return tags;
		}
		Set<String> set=new HashSet<>();
		for(String objectId:objectIds){
			String[] tagArr=tagService.getObjectTags(objectId);
			if (null!=tagArr&&tagArr.length>0){
				set.addAll(Arrays.asList(tagArr));
			}
		}
		tags.addAll(set);
		buildTags(tags);
		return tags;
	}

	private static final String TAG_KEY_RESOURCE_ID = "resourceId";
	private void buildTags(List<String> tags) {
		if (tags.isEmpty()){
			return;
		}
		Iterator<String> iter=tags.iterator();
		while (iter.hasNext()){
			String tag=iter.next();
			if (tag.startsWith(TAG_KEY_TENANT_ID) || tag.startsWith(TAG_KEY_RESOURCE_ID)){
				iter.remove();
			}
		}
	}

	@Override
	public Checkperiod getLastCheckperiod(String tenantId, String state, String objectId) {
		String stateId = checkStateId(tenantId, state);
		if (stateId != null) {
			List<CheckpointRecord> items = checkpointDao.getLastRecordsByObject(stateId, objectId);
			if (items != null && items.size() > 0) {
				String[] tags = tagService.getObjectTags(objectId);
				return convertPeriod(items.get(0), state, tags);
			}
		}
		return null;
	}
}
