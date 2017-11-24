package com.broada.carrier.monitor.impl.host.snmp.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.snmp.SnmpWalk;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpValue;
import com.broada.utils.TextUtil;

/**
 * 通过Snmp协议进行应用进程获取和管理的类
 *
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Maico Pang
 * @version 1.0
 */

public class SnmpProcessManager {
	private static final Log logger = LogFactory.getLog(SnmpProcessManager.class);

	//进程相关的MIB OID定义
	public static final String OID_PROCINDEX = ".1.3.6.1.2.1.25.4.2.1.1";

	public static final String OID_PROCNAME = ".1.3.6.1.2.1.25.4.2.1.2";

	public static final String OID_PROCPATH = ".1.3.6.1.2.1.25.4.2.1.4";

	public static final String OID_PROCPARAM = ".1.3.6.1.2.1.25.4.2.1.5";

	public static final String OID_PROCTYPE = ".1.3.6.1.2.1.25.4.2.1.6";

	public static final String OID_PROCSTATUS = ".1.3.6.1.2.1.25.4.2.1.7";

	public static final String OID_PROCPERFCPU = ".1.3.6.1.2.1.25.5.1.1.1";

	public static final String OID_PROCPERFMEM = ".1.3.6.1.2.1.25.5.1.1.2";

	public static final String OID_MEMORYSIZE = ".1.3.6.1.2.1.25.2.2";

	//存储相关的MIB OID定义
	public static final String OID_HRSTORAGEINDEX = ".1.3.6.1.2.1.25.2.3.1.1";
	public static final String OID_HRSTORAGEDESCR = ".1.3.6.1.2.1.25.2.3.1.3";
	public static final String OID_HRSTORAGEALLOCATIONUNITS = ".1.3.6.1.2.1.25.2.3.1.4";
	public static final String OID_HRSTORAGESIZE = ".1.3.6.1.2.1.25.2.3.1.5";
	public static final String OID_HRSTORAGEUSED = ".1.3.6.1.2.1.25.2.3.1.6";

	//进程的运行状态
	public static final int RUNSTATUS_RUNNING = 1;

	public static final int RUNSTATUS_RUNNABLE = 2;

	public static final int RUNSTATUS_NOTRUNNABLE = 3;

	public static final int RUNSTATUS_INVALID = 4;

	//状态中文标识
	public static final String[] RUNSTATUS = { "未定义", "运行中", "等待资源", "等待事件", "无效进程" };

	protected SnmpWalk walk;

	private Map nameMap;

	/**
	 * 设置snmpwalk对象
	 * @param walk
	 */
	public void setWalk(SnmpWalk walk) {
		this.walk = walk;
	}

	public SnmpProcessManager(SnmpWalk walk) {
		this.walk = walk;
	}

	/**
	 * 转换运行状态标识为中文
	 * @param runStatus
	 * @return
	 */
	public static String convRunStatus(int runStatus) {
		if (runStatus > RUNSTATUS.length - 1 || runStatus < 0) {
			return RUNSTATUS[0];
		} else {
			return RUNSTATUS[runStatus];
		}
	}

	/**
	 * 把目标节点的所有进程名称信息放到Map里
	 * @throws SnmpException
	 * @throws SnmpException 
	 */
	private void putProcessNames() throws SnmpException {
		SnmpResult[] results = walk.snmpWalk(OID_PROCNAME);
		if (results.length == 0
				|| (results.length == 1 && (results[0].getValue() == null || results[0].getValue().toString()
						.equals("noSuchObject")))) {
			throw new SnmpException("无法获取进程列表,可能代理不支持进程管理!", 0, SnmpException.notFound);
		}
		// 保存到Map里
		TreeMap tempNameMap = new TreeMap();
		for (int index = 0; index < results.length; index++) {
			SnmpResult result = results[index];
			SnmpValue var = result.getValue();
			String name = var.toString();
			// add by maico pang 2007-08-20 过滤掉一些不需要的进程
			if (name == null || name.trim().length() == 0 || name.indexOf("System Idle Process") >= 0) {
				continue;
			}
			String oid = result.getOid().toString();
			if (result.getValue().isNull() || oid.length() <= OID_PROCNAME.length())
				continue;
			String instance = oid.toString().substring(OID_PROCNAME.length() + 1);
			tempNameMap.put(Integer.valueOf(instance), name);
		}
		nameMap = tempNameMap;
	}

	/**
	 * 根据名称获取进程ID号
	 * 如果查找不到，返回-1
	 *
	 * @param name
	 * @param matchCase
	 * @param matchType 匹配类型请看com.broada.utils.TextUtil类的相关定义
	 * @return
	 * @throws SnmpException
	 */
	public int getPIDByName(String name, boolean matchCase, int matchType) throws SnmpException {
		if (nameMap == null) {
			putProcessNames();
		}
		for (Iterator iter = nameMap.entrySet().iterator(); iter.hasNext();) {
			Entry entry = (Entry) iter.next();
			if (TextUtil.matches(entry.getValue().toString(), name, matchCase, matchType)) {
				return ((Integer) entry.getKey()).intValue();
			}
		}
		return -1;
	}

	/**
	 * 获得所有进程列表
	 *
	 * 暂时只是返回名称列表
	 *
	 * @return
	 */
	public List getProcessList() throws SnmpException {
		if (nameMap == null) {
			putProcessNames();
		}
		return Collections.unmodifiableList(new ArrayList(nameMap.values()));
	}

	/**
	 * 获得封装好的所有的应用进程（包括进程运行状态，进程内存使用率，进程CPU使用时间，进程监测时间）
	 * 因为进程CPU使用率需要在2分钟内采集到的数据计算，所以在外部设置
	 * @return
	 */
	public List getApliedProcess(Collection processNames) throws SnmpException {
		List apliedProcess = new ArrayList();
		// 不使用缓存
		putProcessNames();
		Set set = nameMap.keySet();
		Iterator it = set.iterator();

		Map processContainer = new HashMap();
		int totalMemoryKB = (int) (getMemory().getSize() / 1024);
		if (totalMemoryKB == 0)
			throw new SnmpException("总物理内存大小为0");
		while (it.hasNext()) {
			ApplicationProcess process = new ApplicationProcess();
			Integer pid = (Integer) it.next();
			String processName = nameMap.get(pid).toString();
			String processInst = processName;
			if (processNames != null && !processNames.contains(processName))
				continue;
			int nowPid = pid.intValue();
			int runStatus = getRunStatus(nowPid);
			int procMemKB = getProcMem(nowPid);
			double procMem = (double) procMemKB / 1024;//进程占用的内存使用量(单位M)
			double procMemPer = (double) procMemKB / (double) totalMemoryKB * 100;//进程占用的内存百分比（单位%)
			Double procMemPercent = Double.valueOf(String.valueOf(new BigDecimal(procMemPer).setScale(2,
					BigDecimal.ROUND_HALF_UP)));
			Double procMemPVavel = new Double(90); //进程占用的内存百分比阈值（单位%)

			int procCPUVal = getProcCPU(nowPid);//进程占用的CPU时间
			long time = System.currentTimeMillis();//当前监测的时间

			Double procCPUPVavel = new Double(90); //进程占用的CPU百分比阈值（单位%)

			process.setIsWacthed(Boolean.FALSE);//默认是不选中的
			process.setCode(processInst);
			process.setName(processName);
			process.setRunStatus(runStatus);
			process.setProcMem(procMem);
			process.setProcCPUPVavel(procCPUPVavel);
			process.setProcMemPercent(procMemPercent);
			process.setProcMemPVavel(procMemPVavel);
			process.setLastVal(procCPUVal);
			process.setLastMonitorTime(time);

			//进程CPU使用率默认为0,数据在外部计算设置
			process.setProcCPUPercent(new Double(0));

			ApplicationProcess applProcess = (ApplicationProcess) processContainer.get(processName);
			if (applProcess == null) {
				processContainer.put(processName, process);
			} else {
				if (applProcess.getProcMem() < procMem) {
					applProcess.setProcMem(procMem);
				}
				if (applProcess.getProcMemPercent().doubleValue() < procMemPercent.doubleValue()) {
					applProcess.setProcMemPercent(procMemPercent);
				}
				if (applProcess.getLastVal() < procCPUVal) {
					applProcess.setLastVal(procCPUVal);

				}
			}
		}
		apliedProcess = new ArrayList(processContainer.values());
		return apliedProcess;
	}

	/**
	 * 获得封装好的所有的应用进程（包括进程运行状态，进程内存使用率，进程CPU使用时间，进程监测时间）
	 * 因为进程CPU使用率需要在2分钟内采集到的数据计算，所以在外部设置
	 * @return
	 */
	public List getApliedProcess() throws SnmpException {
		return getApliedProcess(null);
	}

	/**
	 * 根据PID获得进程的真实名称
	 *
	 * @param PID
	 * @return
	 * @throws SnmpException
	 */
	public String getRealName(int PID) throws SnmpException {
		if (nameMap == null) {
			putProcessNames();
		}
		return nameMap.get(new Integer(PID)).toString();
	}

	/**
	 * 根据OID和进程号获取最原始的值
	 * @param OID
	 * @param PID
	 * @param errMsg
	 * @return
	 * @throws SnmpException
	 */
	private Object getValue(String OID, int PID, String errMsg) throws SnmpException {
		SnmpResult var = null;
		try {
			var = walk.snmpGet(OID + "." + PID);
		} catch (SnmpException ex) {
			throw new SnmpException("获取进程ID为" + PID + "的" + errMsg + "错误，可能该进程已经不存在！", ex, ex.getErrorIndex(),
					ex.getErrorStatus());
		}
		if (var == null) {
			throw new SnmpException("无法获取进程ID为" + PID + "的" + errMsg + "！", 0, SnmpException.noSuchName);
		} else {
			return var.getValue().toString();
		}
	}

	/**
	 * 根据OID和进程号，获取整型值
	 * @param OID
	 * @param PID
	 * @param errMsg
	 * @return
	 * @throws SnmpException
	 */
	private int getIntValue(String OID, int PID, String errMsg) throws SnmpException {
		Object obj = getValue(OID, PID, errMsg);
		return Integer.valueOf(obj.toString()).intValue();
	}

	/**
	 * 得到进程的Path
	 *
	 * @param PID
	 * @return
	 * @throws SnmpException
	 */
	public String getPath(int PID) throws SnmpException {
		return getValue(OID_PROCPATH, PID, "程序路径").toString();
	}

	/**
	 * 获得进程的运行状态
	 * @param PID
	 * @return 返回值请查看RUNSTATUS的相关定义
	 * @throws SnmpException
	 */
	public int getRunStatus(int PID) throws SnmpException {
		return getIntValue(OID_PROCSTATUS, PID, "运行状态");
	}

	/**
	 * 获得进程占用的内存（单位 Kbyte)
	 * @param PID
	 * @return
	 * @throws SnmpException
	 */
	public int getProcMem(int PID) throws SnmpException {
		return getIntValue(OID_PROCPERFMEM, PID, "占用内存");
	}

	/**
	 * 获取当前目标设备的指定oid值。
	 * 且如果目标设备不支持或返回为null，则报告异常，且在日志中说明
	 * @param oid
	 * @return
	 * @throws SnmpException
	 */
	public SnmpValue getSnmpValue(String oid) throws SnmpException {
		SnmpResult result = walk.snmpGet(oid);
		if (result == null || result.getValue().isNull()) {
			if (logger.isWarnEnabled()) {
				logger.warn("目标设备不支持监测所需要的MIB定义[" + oid + "]，请联系厂商处理");
			}
			throw new SnmpException("目标设备不支持监测所需要的MIB定义，请联系厂商处理", 0, SnmpException.noSuchName);
		}

		return result.getValue();
	}

	/**
	 * 根据hrStorageTable优先获取物理内存的信息，如果不存在物理内存，则考虑虚拟内存
	 * @return
	 * @throws SnmpException 如果取不到，则弹出异常 
	 */
	public Memory getMemory() throws SnmpException {
		boolean hasPhysicalMemory = false;
		Memory memory = null;
		SnmpResult[] results = walk.snmpWalk(OID_HRSTORAGEINDEX);
		for (int index = 0; results != null && index < results.length; index++) {
			SnmpResult result = results[index];
			String oid = result.getValue().toString();

			//根据描述获取物理内存实例，该判断还有待丰富			
			String descr = getSnmpValue(OID_HRSTORAGEDESCR + "." + oid).toString();
			if (descr.equals("Real Memory") || descr.equals("System RAM") || descr.equals("Physical Memory")
					|| descr.equals("Virtual Memory")) {
				SnmpValue storageAllocationUnits = getSnmpValue(OID_HRSTORAGEALLOCATIONUNITS + "." + oid);
				SnmpValue storageUsed = getSnmpValue(OID_HRSTORAGEUSED + "." + oid);
				SnmpValue storageSize = getSnmpValue(OID_HRSTORAGESIZE + "." + oid);

				memory = new Memory(descr, storageSize.toLong() * storageAllocationUnits.toLong(), storageUsed.toLong()
						* storageAllocationUnits.toLong());

				if (!descr.equals("Virtual Memory")) {// 如果只是取到了虚拟内存，则可以继续尝试
					hasPhysicalMemory = true;
					break;
				}
			}
		}

		if (memory == null || !hasPhysicalMemory)
			throw new SnmpException("目标设备不支持监测所需要的MIB定义，请联系开发商处理", 0, SnmpException.notFound);

		return memory;
	}

	/**
	 * 这个方法很容易引入岐义，关闭
	 * 
	 * 注意该方法是通过总内存减去空闲内存获取到的
	 * 所以传入的OID是获取空闲内存的OID
	 * @param oid
	 * @return
	 * @throws SnmpException
	 * 
	protected int getUsedMemory(String oid) throws SnmpException {
	  SnmpResult var = walk.snmpGet(oid);

	  int freeMem = 0;
	  if (var != null && var.getValue() != null) {
	    freeMem = (int) var.getValue().toLong();
	    return getMemorySize() - freeMem;
	  } else {
	    throw new SnmpException("取不到空闲内存大小。");
	  }
	}
	*/

	/**
	 * 由于一个进程存在一些动态链接库是整个操作系统共享的。因此将所有进程的使用内存相加会导致已使用内存大于物理内存，甚至超过虚拟内存的情况。
	 * 
	private int getUsedMemByProcess() throws SnmpException {
	  SnmpResult[] results = walk.snmpWalk(OID_PROCINDEX);
	  if (results.length == 0) {
	    throw new SnmpException("无法获取进程列表,可能代理不支持进程管理!");
	  }
	  //累加所有进程使用的内存
	  int allMem = 0;
	  SnmpResult procName = null;
	  SnmpResult procVar = null;
	  Map procMemMap = new HashMap();
	  for (int index = 0; index < results.length; index++) {
	    SnmpResult result = results[index];
	    String oid = result.getValue().toString();
	    if (logger.isDebugEnabled()) {
	      logger.debug("内存OID：" + oid);
	    }
	    procName = walk.snmpGet(OID_PROCNAME + "." + oid);
	    procVar = walk.snmpGet(OID_PROCPERFMEM + "." + oid);
	    if (logger.isDebugEnabled()) {
	      logger.debug("进程名：" + procName.getValue() + "   内存：" + procVar.getValue());
	    }
	    if (procName != null && procVar != null) {
	      procMemMap.put("" + procName + procVar.getOid(), procVar.getValue());
	    }
	  }
	  Collection varList = procMemMap.values();
	  for (Iterator iter = varList.iterator(); iter.hasNext();) {
	    SnmpValue item = (SnmpValue) iter.next();
	    if (logger.isDebugEnabled()) {
	      logger.debug("进程内存大小：" + item);
	    }
	    allMem += item.toLong();
	  }
	  return allMem;
	}
	*/

	/**
	 * 获得进程占用的内存的百分比（单位%)
	 * @param PID
	 * @return
	 * @throws SnmpException
	 */
	public double getProcMemPercent(int PID) throws SnmpException {
		int mem = getProcMem(PID);
		int allMem = (int) (getMemory().getSize() / 1024);
		if (allMem == 0) {
			throw new SnmpException("总物理内存大小为0");
		}
		return (double) mem / allMem * 100;
	}

	/**
	 * 获得进程的CPU使用时间
	 * @param PID
	 * @return
	 * @throws SnmpException
	 */
	public int getProcCPU(int PID) throws SnmpException {
		return getIntValue(OID_PROCPERFCPU, PID, "CPU使用时间");
	}

	/**
	 * 获取所有进程的CPU使用时间片
	 * @return
	 * @throws SnmpException
	 */
	public int getAllProcCPU() throws SnmpException {
		SnmpResult[] results = walk.snmpWalk(OID_PROCPERFCPU);
		if (logger.isDebugEnabled()) {
			logger.debug("进程CPU使用率：");
		}
		int total = 0;
		for (int index = 0; index < results.length; index++) {
			SnmpResult result = results[index];
			if (logger.isDebugEnabled()) {
				logger.debug(" --" + result.getOid() + ":" + result.getValue());
			}
			if (!result.getOid().toString().equals(OID_PROCPERFCPU + ".1")) {
				total += result.getValue().toLong();
			}
		}
		return total;
	}

	/**
	 * 获取目标主机的CPU使用率
	 * @return
	 * @throws SnmpException
	 */
	public float getCpuPercentage() throws SnmpException {
		//return 0;
		throw new UnsupportedOperationException("SnmpProcessManager不支持该方法,由子类实现。");
	}

	/**
	 * 主机类型
	 * @return
	 */
	public String getType() {
		return "unknown";
	}
}