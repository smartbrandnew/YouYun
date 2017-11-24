package com.broada.carrier.monitor.server.impl.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.broada.carrier.monitor.server.api.client.ServerServiceFactory;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorRecord;
import com.broada.carrier.monitor.server.api.entity.MonitorResource;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.MonitorTarget;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetStatus;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;

public class ServerLogicUtil {

	public static MonitorTargetStatus getMonitorTargetStatus(ServerServiceFactory serverFactory, String target, MonitorTask[] tasks) {
		MonitorRecord[] records = getTasksRecord(serverFactory, tasks);
		return toTargetStatus(target, Arrays.asList(tasks), Arrays.asList(records));
	}
	
	public static MonitorTargetStatus[] getMonitorTargetsStatus(ServerServiceFactory serverFactory, String[] targets, MonitorTask[] tasks, Class<? extends MonitorTarget> cla) {
		MonitorRecord[] records = getTasksRecord(serverFactory, tasks);
		
		Map<String, List<MonitorTask>> mapTasks = new HashMap<String, List<MonitorTask>>();
		Map<String, List<MonitorRecord>> mapRecords = new HashMap<String, List<MonitorRecord>>();
		
		for(String target : targets){
			List<MonitorTask> list = new ArrayList<MonitorTask>();
			for(MonitorTask task : tasks){
				if((cla.getName().equals(MonitorResource.class.getName()) && task.getResourceId().equals(target)) || 
						(cla.getName().equals(MonitorNode.class.getName()) && task.getNodeId().equals(target)))
					list.add(task);
			}
			mapTasks.put(target, list);
		}
		
		for(String target : targets){
			List<MonitorRecord> list = new ArrayList<MonitorRecord>();
			for(MonitorTask task : mapTasks.get(target)){
				for(MonitorRecord record : records){
					if(record.getTaskId() == task.getId())
						list.add(record);
				}
			}
			mapRecords.put(target, list);
		}
		
		List<MonitorTargetStatus> list = new ArrayList<MonitorTargetStatus>();
		for(String target : mapTasks.keySet()){
			list.add(toTargetStatus(target, mapTasks.get(target), mapRecords.get(target)));
		}
		return list.toArray(new MonitorTargetStatus[0]);
	}
	
	private static MonitorRecord[] getTasksRecord(ServerServiceFactory serverFactory, MonitorTask[] tasks){
		MonitorRecord[] records = null;
		if(tasks == null || tasks.length == 0)
			records = new MonitorRecord[0];
		else {
			String[] ids = new String[tasks.length];
			for(int i = 0; i < tasks.length; i++){
				ids[i] = tasks[i].getId();
			}
			records = serverFactory.getTaskService().getRecords(StringUtils.join(ids, ","));
		}
		return records;
	}
	
	private static MonitorTargetStatus toTargetStatus(String target, List<MonitorTask> tasks, List<MonitorRecord> records){
		Date time = null;
		MonitorState state;
		if (records.size() == 0)
			state = MonitorState.UNMONITOR;
		else {
			long max = 0;
			state = MonitorState.UNMONITOR;
			int failedCount = 0;
			int ovestepCount = 0;			
			int okCount = 0;
			for (MonitorRecord record : records) {
				switch (record.getState()) {
				case FAILED:
					failedCount++;
					break;
				case OVERSTEP:
					ovestepCount++;
					break;
				case SUCCESSED:
					okCount++;
					break;
				default:
					break;
				}
				if (max < record.getTime().getTime())
					max = record.getTime().getTime();
			}			
			time = new Date(max);
			if (failedCount > 0)
				state = MonitorState.FAILED;
			else if (ovestepCount > 0)
				state = MonitorState.OVERSTEP;
			else if (okCount > 0)
				state = MonitorState.SUCCESSED;
		}
		return new MonitorTargetStatus(target, tasks.size(), state, time);
	}

}
