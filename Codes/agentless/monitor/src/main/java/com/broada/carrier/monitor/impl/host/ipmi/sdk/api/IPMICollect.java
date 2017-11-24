package com.broada.carrier.monitor.impl.host.ipmi.sdk.api;

import java.util.List;
import java.util.Map;

import com.broada.carrier.monitor.impl.host.ipmi.disk.DiskInfo;

/**
 * IPMI信息采集
 * 
 * @author pippo 
 * Create By 2014-5-12 下午3:55:35
 */
public interface IPMICollect {
	
	/**
	 * 校验被监测服务器BMC的IP用户名和密码正确性
	 * @param host
	 * @param username
	 * @param password
	 * @return
	 * @throws IPMIException 
	 */
	ServerType checkAccount() throws IPMIException;
	
	/**
	 * 采集指标信息
	 * @param map
	 * @return
	 * @throws IPMIException 
	 */
	List<QuotaInfo> getQuotaInfos(Map<EntityType,List<SensorType>> map) throws IPMIException;
	
	/**
	 * 采集指定元件类型的指标信息
	 * @param et 
	 * @param type
	 * @return
	 * @throws IPMIException 
	 */
	List<QuotaInfo> getQuotaInfos(EntityType et,List<SensorType> types) throws IPMIException;
	
	/**
	 * 采集底盘信息
	 * @return
	 * @throws IPMIException 
	 */
	ChassisInfo getChassisInfo() throws IPMIException;
	
	/**
	 * 采集基本信息
	 * @return
	 * @throws IPMIException 
	 */
	List<BasicInfo> getBasicInfo() throws IPMIException;
	
	/**
	 * 采集健康信息
	 * @return
	 * @throws IPMIException 
	 */
	List<HealthInfo> getHealthInfo() throws IPMIException;
	
	/**
	 * 采集硬盘信息
	 * @return
	 * @throws IPMIException
	 */
	List<DiskInfo> getDiskInfo() throws IPMIException;
	
}
