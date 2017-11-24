package com.broada.carrier.monitor.impl.storage;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.sblim.wbem.cim.CIMProperty;
import org.sblim.wbem.cim.CIMValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.impl.smis.SmisSession;

/**
 * SMI 磁盘阵列通用工具类
 * @author ly
 *
 */
public class SMIArrayUtils {
	private static final Logger logger = LoggerFactory.getLogger(SMIArrayUtils.class);
	
	private SMIArrayUtils(){
		throw new InvalidParameterException("此类是工具类，不需要实例化");
	}
	
	/**
	 * 提取固件基本信息
	 * @param session
	 * @param cop
	 * @param prop
	 * @return
	 * @throws Exception
	 */
	public static Properties fetchPhysicalPackage(SmisSession session, CIMObjectPath cop, Properties prop) throws Exception {
		List<CIMInstance> instances = session.getAssociatedInstances(cop, "CIM_SystemPackaging", "CIM_PhysicalPackage", "Dependent", "Antecedent");
		for (CIMInstance cimInstance : instances) {
			try {
				CIMObjectPath ppCOP = cimInstance.getObjectPath();
				prop.put("PP_COP", ppCOP);
				Properties ppProps = getInstanceProperties(cimInstance);
				String serialNo = StringUtils.replace(ppProps.getProperty("SerialNumber"), "\"", "");
				prop.setProperty("SerialNumber", serialNo);
				
				String manufacturer = StringUtils.replace(ppProps.getProperty("Manufacturer"), "\"", "");
				prop.setProperty("Manufacturer", manufacturer);
				
				String model = StringUtils.replace(ppProps.getProperty("Model"), "\"", "");
				prop.setProperty("Model", model);
				
				String desc = StringUtils.replace(ppProps.getProperty("Description"), "\"", "");
				prop.setProperty("Description", desc);
			} catch (Exception e) {
				logger.warn("Exception while fetching PhysicalPackage props");
				e.printStackTrace();
			}
		}
		return prop;
	}
	
	/**
	 * 提取软件版本信息
	 * @param session
	 * @param cop
	 * @param prop
	 * @return
	 * @throws Exception
	 */
	public static Properties fetchSoftwareIdentity(SmisSession session,
			CIMObjectPath cop, Properties prop) throws Exception {
		List<CIMInstance> siInstances = session.getAssociatedInstances(cop, "CIM_InstalledSoftwareIdentity", "CIM_SoftwareIdentity", "System", "InstalledSoftware");
		for (CIMInstance cimInstance : siInstances) {
			String firmwareVersion = null;
			try {
				Properties siProps = getInstanceProperties(cimInstance);
				firmwareVersion = siProps.getProperty("VersionString").replaceAll("\"", "");
				prop.setProperty("VersionString", firmwareVersion);
				break;
			} catch (Exception e) {
				logger.warn("[fetchSoftwareIdentity ] Exception while fetching property - VersionString");
				e.printStackTrace();
			}
		}
		return prop;
	}
	
	/**
	 * 查询磁盘驱动器实例
	 * @param session
	 * @param clusterCop
	 * @return
	 * @throws Exception
	 */
	public static List<CIMInstance> fetchDiskDriverInstances(SmisSession session, CIMObjectPath clusterCop) throws Exception {
		List<CIMInstance> ddInstances = Collections.emptyList();
		try {
			ddInstances = session.getAssociatedInstances(clusterCop, "CIM_SystemDevice", "CIM_DiskDrive", "GroupComponent","PartComponent");
		} catch (Exception e) {
			logger.warn("Exception while trying to retrieve the associated DiskDrive instances for the array COP = " + clusterCop);
			e.printStackTrace();
		}
		return ddInstances;
	}
	
	/**
	 * 查询存储池实例
	 * @param session
	 * @param clusterCop
	 * @return
	 * @throws Exception
	 */
	public static List<CIMInstance> fetchStoragePoolInstances(SmisSession session, CIMObjectPath clusterCop) throws Exception {
		List<CIMInstance> spInstances = Collections.emptyList();
		try {
			spInstances = session.getAssociatedInstances(clusterCop, "CIM_HostedStoragePool", "CIM_StoragePool", "GroupComponent", "PartComponent");
		} catch (Exception e) {
			logger.warn("Exception while trying to retrieve the associated Storage Pool instances for the array COP = " + clusterCop);
			e.printStackTrace();
		}
		return spInstances;
	}
	
	
	public static List<CIMInstance> fetchSVInstances(SmisSession session,
			CIMObjectPath cop) throws Exception {
		List<CIMInstance> volumeInstances = Collections.emptyList();
		try {
			volumeInstances = session.getAssociatedInstances(cop,
					"CIM_AllocatedFromStoragePool", "CIM_StorageVolume","Antecedent", "Dependent");
		} catch (Exception e) {
			logger.warn("Exception while trying to retrieve the associated Storage Volume instances for the array COP = "
					+ cop);
			e.printStackTrace();
		}
		return volumeInstances;
	}

	public static Properties getInstanceProperties(CIMInstance instance) {
		try {
			Properties props = new Properties();
			if (instance != null) {
				@SuppressWarnings("unchecked")
				Vector<CIMProperty> vect = instance.getProperties();
				for (int i = 0; i < vect.size(); i++) {
					CIMProperty instProp = vect.elementAt(i);
					CIMValue value = instProp.getValue();
					if ((value != null) && (!value.isNull())) {
						props.setProperty(instProp.getName(), value.toString());
					} else {
						props.setProperty(instProp.getName(), "NotAvailable");
					}
				}
				return props;
			}
		} catch (Exception ex) {
			logger.warn("Exception occured while fetching props from the given instance "
					+ ex.getMessage());
		}
		return null;
	}
	
	/**
	 * 枚举实例
	 * @param session
	 * @param path
	 * @return
	 */
	public static List<CIMInstance> findCIMInstances(SmisSession session,String path) {
		List<CIMInstance> CIMInstances =  session.getInstancesByClass(path);
		if(CIMInstances.size()>0)
			logger.warn("获取到"+path+"实例个数："+CIMInstances.size()+"!");
		return CIMInstances;
	}
	
	/**
	 * 校验path对应的模型是否存在，不存在返回0，存在返回1
	 * @param session
	 * @param path
	 * @return
	 */
	public static int checkCIMInstances(SmisSession session,String path) {
		try{
		List<CIMInstance> CIMInstances =  session.getInstancesByClass(path);
		if(CIMInstances.size()>0)
			return 1;
		return 0;
		}
		catch(Exception e){
			return 0;
		}
	}
	
}
