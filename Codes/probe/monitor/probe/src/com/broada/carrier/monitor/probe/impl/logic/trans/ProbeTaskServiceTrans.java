package com.broada.carrier.monitor.probe.impl.logic.trans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.common.entity.PageNo;
import com.broada.carrier.monitor.probe.impl.dao.InstanceDao;
import com.broada.carrier.monitor.probe.impl.dao.RecordDao;
import com.broada.carrier.monitor.probe.impl.dao.ResultCacheDao;
import com.broada.carrier.monitor.probe.impl.dao.TaskDao;
import com.broada.carrier.monitor.probe.impl.dao.TempDataDao;
import com.broada.carrier.monitor.probe.impl.entity.MonitorResultCache;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorRecord;
import com.broada.carrier.monitor.probe.impl.entity.ProbeSideMonitorTask;
import com.broada.carrier.monitor.probe.impl.logic.ProbeTaskServiceEx;
import com.broada.carrier.monitor.server.api.entity.CollectParams;
import com.broada.carrier.monitor.server.api.entity.CollectResult;
import com.broada.carrier.monitor.server.api.entity.CollectTaskSign;
import com.broada.carrier.monitor.server.api.entity.ExecuteParams;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.TestParams;
import com.broada.carrier.monitor.spi.entity.MonitorTempData;

public class ProbeTaskServiceTrans implements ProbeTaskServiceEx {
	private static final Logger logger = LoggerFactory
			.getLogger(ProbeTaskServiceTrans.class);
	@Autowired
	private TaskDao taskDao;
	@Autowired
	private InstanceDao instanceDao;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private ResultCacheDao cacheDao;
	@Autowired
	private TempDataDao tempDataDao;

	@Override
	public MonitorTask[] getTasks() {
		return taskDao.getAll();
	}

	@Override
	public void saveTask(MonitorTask task, MonitorInstance[] instances,
			MonitorRecord record) {
		logger.debug("监测任务保存：{}", task);
		ProbeSideMonitorTask pt = new ProbeSideMonitorTask(task);
		taskDao.save(pt);
		instanceDao.deleteByTaskId(pt.getId());
		if (instances != null) {
			for (MonitorInstance instance : instances) {
				instance.setTaskId(pt.getId());
				instanceDao.save(instance);
			}
		}
		saveRecord(record);
	}

	@Override
	public void deleteTask(String taskId) {
		logger.debug("监测任务删除：{}", taskId);
		taskDao.delete(taskId);
		instanceDao.deleteByTaskId(taskId);
	}

	@Override
	public void deleteAll() {
		taskDao.deleteAll();
		instanceDao.deleteAll();
	}

	@Override
	public MonitorResult executeTask(String taskId, ExecuteParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Serializable collectTask(CollectParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorRecord getRecord(String taskId) {
		MonitorRecord record = recordDao.get(taskId);
		if (record == null) {
			MonitorTask task = getTask(taskId);
			if (task == null)
				throw new IllegalArgumentException("不存在的监测任务：" + taskId);
			else {
				record = new MonitorRecord(taskId);
				saveRecord(record);
			}
		}
		return record;
	}

	@Override
	public void saveRecord(MonitorRecord record) {
		recordDao.save(new ProbeSideMonitorRecord(record));
	}

	@Override
	public MonitorTask getTask(String taskId) {
		return taskDao.get(taskId);
	}

	@Override
	public MonitorTask[] getTasksByPolicyCode(String policyCode) {
		return taskDao.getByPolicyCode(policyCode);
	}

	@Override
	public int getResultCachesCount() {
		return cacheDao.getCount();
	}

	@Override
	public MonitorResultCache[] getResultCaches(PageNo pageNo) {
		return cacheDao.get(pageNo);
	}

	@Override
	public void deleteResultCaches(MonitorResultCache[] caches) {
		for (MonitorResultCache cache : caches)
			cacheDao.delete(cache.getId());
	}

	@Override
	public void saveResultCache(MonitorResultCache cache) {
		cacheDao.save(cache);
	}

	@Override
	public MonitorInstance[] getInstancesByTaskId(String taskId) {
		return instanceDao.getByTaskId(taskId);
	}

	@Override
	public void dispatchTask(String taskId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MonitorResult testTask(TestParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveTempData(MonitorTempData tempData) {
		tempDataDao.save(tempData);
	}

	@Override
	public MonitorTempData getTempData(String taskId) {
		return tempDataDao.get(taskId);
	}

	@Override
	public void cancelCollect(String nodeId, String taskId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CollectResult getCollectResult(String nodeId, String taskId) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CollectTaskSign commitTask(CollectParams arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getAllTaskIds() {
		List<String> list = new ArrayList<String>();
		String[] arrs = taskDao.getAllTaskIds();
		if (arrs != null)
			list.addAll(Arrays.asList(arrs));
		return list;
	}

	@Override
	public void delete(String id) {
		taskDao.delete(id);

	}
}
