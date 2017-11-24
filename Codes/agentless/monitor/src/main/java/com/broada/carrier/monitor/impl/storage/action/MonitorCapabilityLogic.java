package com.broada.carrier.monitor.impl.storage.action;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.api.entity.ProtocolMetadata;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 发现能力相关的逻辑类，负责向业务模块提供能发现什么配置项，所需要的协议等信息。
 */
public class MonitorCapabilityLogic {
	private static final Logger logger = LoggerFactory.getLogger(MonitorCapabilityLogic.class);
	
	private static MonitorCapabilityLogic INSTANCE = new MonitorCapabilityLogic();
	private volatile boolean cached = false;
	
	/**
	 * key: 配置项类型编码
	 * value: 发现动作元数据
	 */
	private Map<String, List<MonitorActionMetadata>> monitorTypeMappingActionMetas;
	
	/**
	 * key: 配置项类型编码
	 * value: 协议元数据列表
	 */
	private Map<String, Set<ProtocolMetadata>> monitorTypeMappingProtocolMetas;
	
	private MonitorCapabilityLogic() {
	}
	
	public static MonitorCapabilityLogic getDefault() {
		return INSTANCE;
	}
		
	/**
	 * 返回所能发现的配置项类型列表。
	 * 
	 * @return 配置项类型列表
	 */
	public List<String> getMonitorTypes() {
		if (!cached) {
			initCache();
		}
		List<String> list = new ArrayList<String>();
		for (String ciTypeCode : monitorTypeMappingActionMetas.keySet()) {
			list.add(ciTypeCode);
		}
		return list;
	}
	
	/**
	 * 返回指定的协议元数据定义对象。
	 * 
	 * @param protocolMetaCode 协议编码
	 * @return 协议元数据定义对象，如果不存在，则返回null
	 */
	public ProtocolMetadata getProtocolMetadata(String protocolCode) {
		if (protocolCode == null) {
			throw new InvalidParameterException("非法的参数，协议编码不能为null");
		}
		if (!cached) {
			initCache();
		}
		for (Set<ProtocolMetadata> metaSet : monitorTypeMappingProtocolMetas.values()) {
			for (ProtocolMetadata meta : metaSet) {
				if (meta.getCode().equals(protocolCode)) {
					return meta;
				}
			}
		}
		return null;
	}
	
	/**
	 * 返回发现某个配置项类型所需要的协议元数据列表。
	 * 
	 * @param type 配置项类型
	 * @return 协议定义元数据列表
	 */
	public Set<ProtocolMetadata> getProtocolMetadatas(String monitorType) {
		if (monitorType == null) {
			throw new InvalidParameterException("非法的参数，监测器类型不能为null");
		}
		if (!cached) {
			initCache();
		}
		return monitorTypeMappingProtocolMetas.get(monitorType);
	}
	
	/**
	 * 对于特定的配置项类型，返回相应的发现动作元数据定义列表。
	 * 
	 * @param ciType 配置项类型
	 * @return 发现动作元数据定义列表
	 */
	@SuppressWarnings("unchecked")
	public List<MonitorActionMetadata> getActionMetadatas(String monitorType) {
		if (!cached) { 
			initCache();
		}
		if (monitorType != null) {
			return monitorTypeMappingActionMetas.get(monitorType.toLowerCase());
		}
		return Collections.emptyList();
	}
	
	/**
	 * 刷新本地缓存数据。
	 */
	public void refreshCache() {
		synchronized (INSTANCE) {
			monitorTypeMappingActionMetas = null;
			monitorTypeMappingProtocolMetas = null;
			cached = false;
		}
	}
	
	/*
	 * 初始化缓存数据。
	 */
	private void initCache() {
		synchronized (INSTANCE) {
			if (!cached) {
				_initCache();
			}
		}
	}
	
	/*
	 * 按业务规则构建缓存数据。
	 */
	private void _initCache() {
		monitorTypeMappingActionMetas = new LinkedHashMap<String, List<MonitorActionMetadata>>();
		monitorTypeMappingProtocolMetas = new LinkedHashMap<String, Set<ProtocolMetadata>>();
		
		MonitorActionMetadata[] actionMetas = MonitorActionClient.getActions();
		ProtocolMetadata[] pMetas = MonitorActionClient.getProtocolMetadatas();
		
		// 先做好映射关系
		Map<String, ProtocolMetadata> protocolMapping = new LinkedHashMap<String, ProtocolMetadata>();
		for (ProtocolMetadata pMeta : pMetas) {
			protocolMapping.put(pMeta.getCode(), pMeta);
		}
		
		
		//Map<String, List<MonitorActionMetadata>> actionMetaMap = new HashMap<String, List<MonitorActionMetadata>>();
		for (MonitorActionMetadata action : actionMetas) {
			String monitorType = action.getOutput().trim().toLowerCase();
			
			// TODO 未声明产出的配置项类型
			if (monitorType == null || monitorType.isEmpty()) {
				continue;
			}
			
			// 处理发现动作
			if (!monitorTypeMappingActionMetas.containsKey(monitorType)) {
				monitorTypeMappingActionMetas.put(monitorType, new ArrayList<MonitorActionMetadata>());
			}
			monitorTypeMappingActionMetas.get(monitorType).add(action);
			
			// 处理发现协议
			Set<ProtocolMetadata> protocolMetas = monitorTypeMappingProtocolMetas.get(monitorType);
			if (protocolMetas == null) {
				protocolMetas = new HashSet<ProtocolMetadata>();
				monitorTypeMappingProtocolMetas.put(monitorType, protocolMetas);
			}
			
			String[] protocols = action.getProtocols();
			if (protocols == null) {
				continue;
			}
			for (String protocol : protocols) {
				ProtocolMetadata p = protocolMapping.get(protocol);
				if (p == null) {
					logger.debug("找不到动作 {} 所需要的协议：{}，", action.getCode(), protocol);
					continue;
				}
				protocolMetas.add(p);
			}
		}
		cached = true;
	}

}
