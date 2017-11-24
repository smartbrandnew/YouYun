package com.broada.carrier.monitor.impl.host.snmp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.storage.netapp.cpu.CPU;
import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;

public class NetAppSnmpProcessManager  {
	private static final Log logger = LogFactory.getLog(NetAppSnmpProcessManager.class);

	public static final String BATTERYSTATUS = ".1.3.6.1.4.1.789.1.2.5.1";
	public static final String CPUBUSYTIMEPERCENT = ".1.3.6.1.4.1.789.1.2.1.3";
	public static final String CPUCPINTERRUPTPERCENT = ".1.3.6.1.4.1.789.1.2.1.11";
	public static final String CPUIDLETIMEPERCENT = ".1.3.6.1.4.1.789.1.2.1.5";
	public static final String CPUUPTIME = ".1.3.6.1.4.1.789.1.2.1.1";
	public static final String FSOVERALLSTATUS= ".1.3.6.1.4.1.789.1.5.7.1";
	public static final String OID_FSUSED = ".1.3.6.1.4.1.789.1.5.7.3";
	public static final String RAIDSTATUS = ".1.3.6.1.4.1.789.1.6.1.1.3";
	public static final String OID_FAILEDPOWER_COUNT = ".1.3.6.1.4.1.789.1.2.4.4";

	private Map nameMap;
	protected SnmpWalk walk;
	
	public NetAppSnmpProcessManager(SnmpWalk walk) {
		this.walk = walk;
		// TODO Auto-generated constructor stub
	}
	
	public SnmpResult[] getBatteryStatus(SnmpWalk walk) throws SnmpException{
		SnmpResult[] walkResult = null;
		walkResult = walk.snmpWalk(BATTERYSTATUS);
		return walkResult;
	}
	
	public SnmpResult[] getFsStatus(SnmpWalk walk) throws SnmpException{
		SnmpResult[] walkResult = null;
		walkResult = walk.snmpWalk(FSOVERALLSTATUS);
		return walkResult;
	}
	
	public SnmpResult[] getFsUsed(SnmpWalk walk) throws SnmpException{
		SnmpResult[] walkResult = null;
		walkResult = walk.snmpWalk(OID_FSUSED);
		return walkResult;
	}
	
	public SnmpResult[] getFailedPower(SnmpWalk walk) throws SnmpException{
		SnmpResult[] walkResult = null;
		walkResult = walk.snmpWalk(OID_FAILEDPOWER_COUNT);
		return walkResult;
	}
	
	public SnmpResult[] getRaidStatus(SnmpWalk walk) throws SnmpException{
		SnmpResult[] walkResult = null;
		walkResult = walk.snmpWalk(RAIDSTATUS);
		return walkResult;
	}
	
	/**
	 * 计算CPU的个数可能有多个CPU的情况
	 * 
	 * @throws Exception
	 */
	public SnmpResult[] getCPUInterrup(SnmpWalk walk) throws SnmpException {
		SnmpResult[] results = walk.snmpWalk(CPUCPINTERRUPTPERCENT);
		return results;
	}
	
	/**
	 * 计算CPU的个数可能有多个CPU的情况
	 * 
	 * @throws Exception
	 */
	public SnmpResult[] getCPUIDletime(SnmpWalk walk) throws SnmpException {
		SnmpResult[] results = walk.snmpWalk(CPUIDLETIMEPERCENT);
		return results;
	}
	
	public List<CPU> getApliedCpu() throws Exception {
		List<CPU> apliedCpus = new ArrayList<CPU>();
		if (this.nameMap == null) {
			putCpusNames();
		}
		Map cpuContainer = new HashMap();
		Set set = this.nameMap.keySet();
		Iterator it = set.iterator();
		while (it.hasNext()) {
			CPU process = new CPU();
			Integer cid = (Integer) it.next();
			String processName = this.nameMap.get(cid).toString();
			String processInst = processName;

			Double useRateValue = getUseRata(cid);
			Double interruptRateValue = getInterrupRata(cid);
			Double vacancyRateValue = getIdletimeRata(cid);
			
			String time =  getCPUuptime(cid) + "";
			
			Double useRateVavel = new Double(90.0D);
			Double vacancyRateVavel = new Double(90.0D);
			Double interruptRateVavel = new Double(3.0D);
			
			process.setIsWacthed(Boolean.FALSE);
			process.setInstance(processInst);
			process.setLabel(processName);

			process.setUseRateValue(useRateValue);
			process.setUseRateVavel(useRateVavel);
			
			process.setVacancyRateValue(vacancyRateValue);
			process.setVacancyRateVavel(vacancyRateVavel);

			process.setInterruptRateValue(interruptRateValue);
			process.setInterruptRateVavel(interruptRateVavel);

			process.setCpuuptime(time);
			CPU applProcess = (CPU) cpuContainer
					.get(processName);
			if (applProcess == null) {
				cpuContainer.put(processName, process);
			}
		}
		apliedCpus = new ArrayList(cpuContainer.values());
		return apliedCpus;
	}
	

	
	/**
	 * 计算CPU的个数可能有多个CPU的情况
	 * 
	 * @throws Exception
	 */
	private void putCpusNames() throws Exception {
		SnmpResult[] results = this.walk.snmpWalk(CPUCPINTERRUPTPERCENT);
		if (results.length == 0) {
			throw new SnmpException("cpu列表,可能代理不支持cpu管理!");
		}
		this.nameMap = new TreeMap();
		for (int index = 0; index < results.length; ++index) {
			SnmpResult result = results[index];
			String name = result.getValue().toString();
			if ((name == null) || (name.trim().length() == 0))
				continue;
			SnmpOID oid = result.getOid();
			String instance = oid.toString().substring(
					CPUCPINTERRUPTPERCENT.length() + 1);
			this.nameMap.put(Integer.valueOf(instance), "CPU-"+Integer.valueOf(instance));
		}
	}
	
	/**
	 * CPU使用率
	 */
	private Double getUseRata(int cid) {
		try {
			return new Double( getValue(CPUBUSYTIMEPERCENT, cid,
					"CPU使用率")+"");
		} catch (Exception e) {
			logger.error("CPU使用率数据转化错误", e);
			e.printStackTrace();
		}
		return new Double(-1);
	}
	
	/**
	 * CPU中断率
	 * 
	 * @param cid
	 * @return
	 */
	private Double getInterrupRata(int cid) {
		try {
			return new Double( getValue(CPUCPINTERRUPTPERCENT, cid,
					"CP中断率")+"");
		} catch (Exception e) {
			logger.error("CPU使用率数据转化错误", e);
			e.printStackTrace();
		}
		return new Double(-1);
	}

	/**
	 * CPU闲置率
	 */
	private Double getIdletimeRata(int cid) {
		try {
			return new Double(getValue(CPUIDLETIMEPERCENT, cid,
					"CPU闲置率")+"");
		} catch (Exception e) {
			logger.error("CPU使用率数据转化错误", e);
			e.printStackTrace();
		}
		return new Double(-1);
	}

	/**
	 * CPU启动时间
	 */
	private Object getCPUuptime(int cid) {
		try {
			return getValue(CPUUPTIME, cid, "CPU启动时间");
		} catch (Exception e) {
			logger.error("CPU使用率数据转化错误", e);
			e.printStackTrace();
		}
		return new Double(-1);
	}
	
	private Object getValue(String OID, int PID, String errMsg)throws Exception {
		SnmpResult var = null;
		try {
			var = this.walk.snmpGet(OID + "." + PID);
		} catch (Exception ex) {
			throw new SnmpException("获取CPUID为" + PID + "的" + errMsg
					+ "错误，可能该CPU已经不存在！", ex);
		}
		if (var == null) {
			throw new SnmpException("无法获取CPUID为" + PID + "的" + errMsg + "！");
		}
		return var.getValue().toString();
	}


}
