package com.broada.carrier.monitor.impl.storage;

import java.util.List;

import org.sblim.wbem.cim.CIMInstance;
import org.sblim.wbem.cim.CIMObjectPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.cid.action.protocol.common.HttpProtocolType;
import com.broada.cid.action.protocol.impl.smis.SmisProtocol;
import com.broada.cid.action.protocol.impl.smis.SmisSession;

public class SmisTest {
	
	private static final Logger logger = LoggerFactory.getLogger(SmisTest.class);

	public static void main(String[] args) {
		String ipaddr ="";//smis所在ip
		String userName = "";//smis配置的监测用户
		String password = "";//smis配置的监测用户密码
		String namespace = "root/LsiArray13";//smis监测空间
		String code = "CIM_ComputerSystem";//smis执行
		String proctocol = "http";//协议 http or https
		String proVersion = "SSL";//https才需要
		int port = 5988;//http 5988 https 5989
		SmisProtocol smisProtocol = new SmisProtocol(HttpProtocolType.valueOf(proctocol) == HttpProtocolType.http ? false : true, ipaddr, port, userName, password, namespace, proVersion);
		SmisSession smisSession = new SmisSession(smisProtocol);
		smisSession.connect();
		
		 List<CIMInstance> cs = smisSession.getInstancesByClass(code);
		 for (int i = 0; i < cs.size(); i++) {
			System.out.println("获取到了存储系统跟节点"+cs.get(i));
			System.out.println("开始获取物理磁盘信息！");
			List<CIMInstance> chassisInstances = findChassis(smisSession,cs.get(i).getObjectPath());
			List<CIMInstance> ddInstances = findDiskDriver(smisSession,cs.get(i).getObjectPath());
			for (int j = 0; j < chassisInstances.size(); j++) {
				
			}
			
			for (int j = 0; j < ddInstances.size(); j++) {
				CIMInstance dd = ddInstances.get(j);
				List<CIMInstance> deInstances = findDiskEctent(smisSession,dd.getObjectPath());
				List<CIMInstance> dpInstances = findDiskPackage(smisSession,dd.getObjectPath());
			}
		}
	}
	
	public static List<CIMInstance> findChassis(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> chassisInstances = null;
		try {
			chassisInstances = session.getAssociatedInstances(arrayCOP, "CIM_SystemPackaging", "CIM_PhysicalPackage", "Dependent", "Antecedent");
			if (chassisInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取物理磁盘数为0，请确认支持smi-s协议或者具有物理磁盘！");
			}
			logger.warn("获取到物理磁盘个数："+chassisInstances.size());
			return chassisInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 磁盘
	 * @param session
	 * @param arrayCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskDriver(SmisSession session,CIMObjectPath arrayCOP){
		List<CIMInstance> ddInstances = null;
		try {
			ddInstances = session.getAssociatedInstances(arrayCOP, "CIM_SystemDevice", "CIM_DiskDrive", "GroupComponent", "PartComponent");
			if (ddInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取磁盘模块数为0，请确认支持smi-s协议或者具有磁盘模块信息！");
			}
			return ddInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 磁盘信息
	 * @param session
	 * @param ddCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskEctent(SmisSession session,CIMObjectPath ddCOP){
		List<CIMInstance> deInstances = null;
		try {
			deInstances = session.getAssociatedInstances(ddCOP, "CIM_MediaPresent", "CIM_StorageExtent", "Antecedent", "Dependent");
			if (deInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取磁盘信息数为0，请确认支持smi-s协议或者具有磁盘信息！");
			}
			return deInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
	
	/**
	 * 磁盘模块
	 * @param session
	 * @param ddCOP
	 * @return
	 */
	public static List<CIMInstance> findDiskPackage(SmisSession session,CIMObjectPath ddCOP){
		List<CIMInstance> dpInstances = null;
		try {
			dpInstances = session.getAssociatedInstances(ddCOP, "CIM_Realizes", "CIM_PhysicalPackage", "Dependent", "Antecedent");
			if (dpInstances.size() == 0 ) {
				throw new UnsupportedOperationException("通过smi-s协议获取磁盘模块数为0，请确认支持smi-s协议或者具有磁盘模块！");
			}
			return dpInstances;
		} catch(Exception e) {
			throw new UnsupportedOperationException("通过smi-s协议来监测IBMTSDS存储发生异常。异常信息："+e.getMessage());
		}
	}
}
