package com.broada.carrier.monitor.server.impl.pmdb.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.broada.carrier.monitor.server.api.client.ServerUtil;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorTargetType;
import com.broada.carrier.monitor.server.api.service.ServerTargetTypeService;
import com.broada.carrier.monitor.server.impl.entity.LocalRemoteKey;
import com.broada.carrier.monitor.server.impl.logic.LocalRemoteMapper;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBConverter;
import com.broada.carrier.monitor.server.impl.pmdb.PMDBFacade;
import com.broada.carrier.monitor.server.impl.pmdb.cache.CICache;
import com.broada.carrier.monitor.server.impl.pmdb.cache.CIEntry;
import com.broada.cmdb.api.data.AttributeValue;
import com.broada.cmdb.api.data.Instance;
import com.broada.cmdb.api.data.Relationship;
import com.broada.cmdb.api.model.Attribute;
import com.broada.component.utils.error.ErrorUtil;
import com.broada.component.utils.text.DateUtil;
import com.broada.pmdb.api.data.PerfIndicatorGroup;
import com.broada.pmdb.api.data.StateIndicator;

public class PMDBMapper {
	private static final Logger logger = LoggerFactory.getLogger(PMDBMapper.class);
	@Autowired
	private ServerTargetTypeService targetTypeService;
	@Autowired
	private PMDBFacade pmdbFacade;
	@Autowired
	private LocalRemoteMapper localRemoteMapper;
	private CICache ciCache;
	private MapConfig config = new MapConfig();

	private static PMDBMapper instance;
	
	/**
	 * 用于统计cmdb、pmdb数据上报统计
	 */
	private static long lastLogOut = System.currentTimeMillis();
	private static int interval = 2 * 60 * 1000;
	private static long saveCiTimeUse = 0;
	private static int saveCiTimes = 0;
	private static long savePerfTimeUse = 0;
	private static int savePerfTimes = 0;
	private static long saveStateTimeUse = 0;
	private static int saveStateTimes = 0;

	/**
	 * 获取默认实例
	 * @return
	 */
	public static PMDBMapper getDefault() {
		if (instance == null) {
			synchronized (PMDBMapper.class) {
				if (instance == null)
					instance = new PMDBMapper();
			}
		}
		return instance;
	}	
	
	private CICache getCiCache() {
		if (ciCache == null)
			ciCache = new CICache(pmdbFacade);
		return ciCache;
	}

	public void process(MapInput input) {
		MapMonitor monitor = config.getMonitor(input.getTask().getTypeId());
		if (monitor == null) {
			if (logger.isDebugEnabled())				
				logger.debug(String.format("监测结果忽略，未找到此类型监测任务映射配置：%s", input.getTask()));
			return;
		}
				
		logger.debug("监测结果处理开始：{}", input);

		//将结果放入每个monitor中的标签进行解析、执行
		for (MapTask obj : monitor.getObjects()) {						
			process(obj, input);
		}
	}
	
	private void process(MapTask obj, MapInput input) {
		if (!obj.isMatchState(input.getResult().getState())) {
			logger.debug(String.format("映射规则忽略：当前监测任务的状态不匹配。%s ", obj));
			return;
		}
				
		if (obj instanceof MapObject)
			processObject((MapObject) obj, input);
		else if (obj instanceof MapMonitorScript)
			processScript((MapMonitorScript) obj, input);
		else
			throw new IllegalArgumentException("未知的monitor处理对象类型：" + obj.getClass());
	}
	
	/**
	 * script标签（groovy）
	 * @param script
	 * @param input
	 */
	private void processScript(MapMonitorScript script, MapInput input) {
		//执行脚本，获取outputs
		List<MapOutput> outputs = script.process(input, localRemoteMapper);
		Map<String, List<MapOutput>> outputsByRemoteCode = createGroupByRemoteCode(outputs);
		for (Entry<String, List<MapOutput>> entry : outputsByRemoteCode.entrySet()) {
			String remoteClassCode = entry.getKey();
			Collection<LocalRemoteKey> keys = localRemoteMapper.getKeys(input.getTask().getId(), remoteClassCode);
			LocalRemoteKeyContext context = new LocalRemoteKeyContext(input.getTask().getId(), remoteClassCode, keys);
			for (MapOutput output : entry.getValue()) {				
				SaveResult sr = saveInstance(entry.getKey(), input, output);
				if (sr != null)
					context.save(sr.localKey, sr.remoteKey, remoteClassCode);
			}		
			if (context.isExistsDeleteKeys()) 
				deleteInstance(context.getDeleteKeys());
			localRemoteMapper.saveContext(context);
		}
	}

	/**
	 * 将outputs按实例类型分组
	 * @param outputs
	 * @return <实例类型，实例资源或对象>
	 */
	private Map<String, List<MapOutput>> createGroupByRemoteCode(List<MapOutput> outputs) {
		Map<String, List<MapOutput>> maps = new HashMap<String, List<MapOutput>>();
		for (MapOutput output : outputs) {
			List<MapOutput> list = maps.get(output.getRemoteClassCode());
			if (list == null) {
				list = new ArrayList<MapOutput>();
				maps.put(output.getRemoteClassCode(), list);
			}
			list.add(output);
		}
		return maps;
	}

	/**
	 * object标签
	 * @param mapObject
	 * @param input
	 */
	private void processObject(MapObject mapObject, MapInput input) {
		String localTypeId = null;
		
		switch (mapObject.getLocal()) {//判断数据类型
		case NODE:
			localTypeId = input.getNode().getTypeId();		
		case RESOURCE:
			if (input.getResult().getRows() !=null && input.getResult().getRows().size() > 0)
				input.setInstance(input.getResult().getRows().get(0));
			if (localTypeId == null) {
				if (input.getResource() == null)
					break;
				localTypeId = input.getResource().getTypeId();
			}
			if (!isLocalTypeMatchRemoteType(mapObject, localTypeId)) {
				if (logger.isDebugEnabled())
					logger.debug(String.format("映射规则忽略：当前监测任务的节点或资源类型非目标类型。%s 节点类型：%s 目标类型：%s",
							mapObject, input.getNode().getTypeId(), mapObject.getRemote()));
				break;
			}
			processNodeOrResource(mapObject, input);
			break;
		case INSTANCE:			
			Collection<LocalRemoteKey> keys = localRemoteMapper.getKeys(input.getTask().getId(), mapObject.getRemote());
			LocalRemoteKeyContext context = new LocalRemoteKeyContext(input.getTask().getId(), mapObject.getRemote(), keys);		
			for (MonitorResultRow row : input.getResult().getRows()) {
				input.setInstance(row);
				processInstance(mapObject, input, context);
			}
			if (context.isExistsDeleteKeys()) 
				deleteInstance(context.getDeleteKeys());
			localRemoteMapper.saveContext(context);		
			break;
		default:
			throw new IllegalArgumentException(mapObject.getLocal().toString());
		}		
	}
	
	private boolean isLocalTypeMatchRemoteType(MapObject mapObject, String localTypeId) {
		MonitorTargetType localType = ServerUtil.checkTargetType(targetTypeService, localTypeId);
		if (mapObject.getRemote().equalsIgnoreCase(MonitorTargetType.ROOT_NODE_ID)) {
			if (localType.isNode())
				return true;
			else
				return false;
		} else if (mapObject.getRemote().equalsIgnoreCase(MonitorTargetType.ROOT_RESOURCE_ID)) {
			if (localType.isResource())
				return true;
			else
				return false;
		} 
			
		MonitorTargetType remoteType = ServerUtil.checkTargetType(targetTypeService, mapObject.getRemote());		
		return remoteType.isSubType(localType);
	}

	private void deleteInstance(Collection<LocalRemoteKey> keys) {
		for (LocalRemoteKey key : keys) {
			pmdbFacade.deleteInstanceById(key.getRemoteKey());
			if (logger.isDebugEnabled())
				logger.debug(String.format("删除已废弃资源：%s", key));
		}
	}
	
	private void processNodeOrResource(MapObject mapObject, MapInput input) {
		MapOutput output = processOutput(mapObject, input);
		if (output == null)
			return;
		
		save(mapObject, input, output);
	}
	
	private MapOutput processOutput(MapObject mapObject, MapInput input) {
		MapOutput output = null;
		if (mapObject.getScript() != null)
			output = mapObject.getScript().process(input, localRemoteMapper);
		else if (mapObject.getItems() != null)
			output = new MapItemsProcessor(config, mapObject.getItems()).process(input, localRemoteMapper);
		else
			throw new IllegalArgumentException();
		if (output == null) {
			logger.debug(String.format("映射规则忽略：没有转换出任何有用信息。%s 节点类型：%s 目标类型：%s",
					mapObject, input.getNode().getTypeId(), mapObject.getRemote()));			
		}		
		return output;
	}

	private void processInstance(MapObject mapObject, MapInput input, LocalRemoteKeyContext context) {
		MapOutput output = processOutput(mapObject, input);
		if (output == null)
			return;
		
		SaveResult sr = save(mapObject, input, output);
		if (sr != null)
			context.save(sr.localKey, sr.remoteKey, mapObject.getRemote());
	}
	
	private static class SaveResult {
		private String localKey;
		private String remoteKey;
		
		public SaveResult(String localKey, String remoteKey) {		
			this.localKey = localKey;
			this.remoteKey = remoteKey;
		}
	}

	private SaveResult save(MapObject mapObject, MapInput input, MapOutput output) {
		switch (mapObject.getLocal()) {
		case NODE:
			return saveNode(input, output);
		case RESOURCE:
			return saveResource(input, output);
		case INSTANCE:
			return saveInstance(mapObject.getRemote(), input, output);
		default:
			throw new IllegalArgumentException(mapObject.getLocal().toString());
		}
	}

	private SaveResult saveInstance(String remoteClassCode, MapInput input, MapOutput output) {		
		String localKey = output.getLocalKey() == null ? input.getLocalKey(MapObjectType.INSTANCE) : output.getLocalKey();
		String remoteKey = null;
		SaveResult sr = null;
		if (output.hasInstance()) {
			Instance ci = new Instance();		
			ci.setTemplateCode(remoteClassCode);		
			sr = saveCi(remoteClassCode, localKey, ci, output, input.getNode().getDomainId());
			remoteKey = sr.remoteKey;
		} else {						
			Collection<LocalRemoteKey> keys = localRemoteMapper.getKeysByLocalKey(localKey);
			for (LocalRemoteKey key : keys) {
				if (key.getLocalKey().equals(localKey)) {
					remoteKey = key.getRemoteKey();					
					break;
				}
			}
		}
		
		if (remoteKey != null) {
			saveState(remoteKey, output);		
			savePerf(remoteKey, output);				
		}
		return sr;
	}
	
	private SaveResult saveCi(String remoteClassCode, String localKey, Instance ci, MapOutput output, String domainId) {
		CIEntry cachedEntry = getCiCache().get(remoteClassCode, localKey);
		if (cachedEntry != null) {
			if (equals(cachedEntry.getInstance(), ci))
				return new SaveResult(localKey, cachedEntry.getRemoteKey());
		}
		
		Attribute[] attrs = pmdbFacade.getAttributesByTemplateCode(remoteClassCode);
		for (AttributeValue av : output.getAttributes()) {
			if (attrs == null)
				ci.getValues().add(av);
			else {
				for (Attribute attr : attrs) {
					if (attr.getCode().equalsIgnoreCase(av.getCode())) {
						ci.getValues().add(av);
						break;
					}
				}			
			}
		}
		
		Relationship[] rss = null;
		if (!output.getRelationships().isEmpty()) 
			rss = output.getRelationships().toArray(new Relationship[0]);		

		if (logger.isDebugEnabled())
			logger.debug(String.format("配置项保存[\n localKey: %s \n ci: %s \n attr: %s \n rs: %s]", localKey, ci, ci.getValues().toString(), Arrays.toString(rss)));
		
		long t1 = System.currentTimeMillis();
		String remoteKey = pmdbFacade.saveInstance(localKey, ci, rss, domainId);
		long t2 = System.currentTimeMillis();
		saveCiTimeUse += (t2 - t1);
		saveCiTimes++;
		if (logger.isDebugEnabled())
			logOut(t2);
		getCiCache().add(remoteClassCode, localKey, remoteKey, ci);
		return new SaveResult(localKey, remoteKey);
	}

	private boolean equals(Instance cachedCi, Instance newCi) {
		for (AttributeValue av1 : newCi.getValues().getValues().values()) {
			AttributeValue av2 = cachedCi.getValues().get(av1.getCode());			
			if (!av1.equals(av2))
				return false;
		}
		return true;
	}

	private SaveResult saveResource(MapInput input, MapOutput output) {
		SaveResult sr = null;
		
		if (output.hasInstance()) {
			Instance ci = PMDBConverter.toInstance(input.getResource());
			sr = saveCi(input.getResource().getTypeId(), "ci." + input.getResource().getId(), ci, output, input.getResource().getDomainId());
		}
		
		saveState(input.getResource().getId(), output);
		savePerf(input.getResource().getId(), output);
		return sr;
	}

	private void savePerf(String ciId, MapOutput output) {
		if (output.hasPerfs()) {
			for (PerfIndicatorGroup group : output.getPerfGroups()) {
				group.setInstanceId(ciId);
				try {
					if (logger.isDebugEnabled())
						logger.debug(String.format("性能指标组保存[ciId: %s time: %s group: %s]", group.getInstanceId(),
								DateUtil.format(group.getSampleTime()), group.getGroupCode()));
					
					long t1 = System.currentTimeMillis();
					pmdbFacade.savePerfGroup(group);
					long t2 = System.currentTimeMillis();
					savePerfTimeUse += (t2 - t1);
					savePerfTimes++;
					if (logger.isDebugEnabled())
						logOut(t2);
				} catch (Throwable e) {
					if ( e.getMessage() !=null && e.getMessage().indexOf("当前配置项处于维护中，不充许保存指标") > -1)
						logger.debug("当前配置项处于维护中，不充许保存指标");
					else
						ErrorUtil.warn(logger, String.format("性能指标组保存失败[ciId: %s time: %s group: %s]", group.getInstanceId(),
								DateUtil.format(group.getSampleTime()), group.getGroupCode()), e);
				}
			}
		}
	}

	private void saveState(String ciId, MapOutput output) {
		if (output.hasStates()) {
			for (StateIndicator state : output.getStates()) {
				state.setInstanceId(ciId);
				try {
					if (logger.isDebugEnabled())
						logger.debug(String.format("状态指标保存[ciId: %s time: %s value: %s descr: %s]", state.getInstanceId(),
								DateUtil.format(state.getSampleTime()), state.getValue(), state.getDescr()));
					
					long t1 = System.currentTimeMillis();
					pmdbFacade.saveState(state);
					long t2 = System.currentTimeMillis();
					saveStateTimeUse += (t2 - t1);
					saveStateTimes++;
					if (logger.isDebugEnabled())
						logOut(t2);
				} catch (Throwable e) {
					ErrorUtil.warn(logger, String.format("状态指标保存失败[ciId: %s time: %s value: %s descr: %s]", state.getInstanceId(),
							DateUtil.format(state.getSampleTime()), state.getValue(), state.getDescr()), e);
				}
			}
		}
	}

	private SaveResult saveNode(MapInput input, MapOutput output) {
		SaveResult sr = null;
		
		if (output.hasInstance()) {			
			Instance ci = PMDBConverter.toInstance(input.getNode());
			sr = saveCi(input.getNode().getTypeId(), "ci." + input.getNode().getId(), ci, output, input.getNode().getDomainId());
		}
		
		saveState(input.getNode().getId(), output);
		savePerf(input.getNode().getId(), output);	
		return sr;
	}

	public void deleteInstance(String taskId) {
		Collection<LocalRemoteKey> keys = localRemoteMapper.getKeysByTaskId(taskId);
		if (keys != null) {
			localRemoteMapper.deleteKeysByTaskId(taskId);
			deleteInstance(keys);
		}
	}
	
	private void logOut(long t2){
		if(t2 - lastLogOut >= interval)
			printLog(t2);
	}
	
	private synchronized void printLog(long t2){
		if(t2 - lastLogOut < interval)
			return;
		logger.debug(String.format("%sms内配置项保存总耗时：%sms, 次数：%s", (t2 - lastLogOut), saveCiTimeUse, saveCiTimes));
		saveCiTimeUse = 0;
		saveCiTimes = 0;
		logger.debug(String.format("%sms内性能指标保存总耗时：%sms, 次数：%s", (t2 - lastLogOut), savePerfTimeUse, savePerfTimes));
		savePerfTimeUse = 0;
		savePerfTimes = 0;
		logger.debug(String.format("%sms内状态指标保存总耗时：%sms, 次数：%s", (t2 - lastLogOut), saveStateTimeUse, saveStateTimes));
		saveStateTimeUse = 0;
		saveStateTimes = 0;
		lastLogOut = t2;
	}
}
