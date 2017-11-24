package com.broada.carrier.monitor.impl.mw.tuxedo.machine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.calculator.ExpressionCalculator.InvalidExpressionException;
import com.broada.snmp.*;
import com.broada.snmputil.SnmpException;

/**
 * 通过Snmp协议进行Tuxedo性能参数获取和管理的类
 * 
 * <p>Title: SnmpTuxManager</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */
public class SnmpTuxManager {

	//Tuxedo MIB OID
	/*------------  MACHINE  ------------*/
	public static final Log logger = LogFactory.getLog(SnmpTuxManager.class);
	public static final String OID_TUX_MHE_NUMTRAN = ".1.3.6.1.4.1.140.300.5.2.18";
	public static final String OID_TUX_MHE_NUMTRANCMT = ".1.3.6.1.4.1.140.300.5.2.20";
	public static final String OID_TUX_MHE_WKCOMPLETED = ".1.3.6.1.4.1.140.300.5.2.30";
	public static final String OID_TUX_MHE_WKINITIATED = ".1.3.6.1.4.1.140.300.5.2.31";
	public static final String OID_TUX_MHE_WKCOMPLETEDPERSEC = "";
	public static final String OID_TUX_MHE_WKINITIATEDPERSEC = "";
	public static final String OID_TUX_MHE_CURCLIENTS = ".1.3.6.1.4.1.140.300.5.2.2";
	public static final String OID_TUX_MHE_WSCURCLIENTS = ".1.3.6.1.4.1.140.300.5.2.6";
	public static final String OID_TUX_MHE_PMID = ".1.3.6.1.4.1.140.300.5.1.1.1";
	public static final String OID_TUX_MHE_STATE = ".1.3.6.1.4.1.140.300.5.1.1.6";

	private final SnmpWalk walk;

	public SnmpTuxManager(SnmpWalk walk) {
		this.walk = walk;
	}

	/**
	 * Get number of transactions initiated (tpbegin(3)) from this machine.
	 * @return
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 * @throws SnmpException
	 */
	public long getNumTran() throws SnmpException {
		try {
			return SnmpWalkUtil.getFirstLongValue(walk, OID_TUX_MHE_NUMTRAN);
		} catch (SnmpNotFoundException e) {
			logger.debug("无法找到OID对应的值： ", e);
			return 0;
		}
	}

	/**
	 * Get number of  transactions  committed  (tpcommit(3))  from this machine.
	 * @return
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 */
	public long getNumTranCmt() throws SnmpException {
		try {
			return SnmpWalkUtil.getFirstLongValue(walk, OID_TUX_MHE_NUMTRANCMT);
		} catch (SnmpNotFoundException e) {
			logger.debug("无法找到OID对应的值： ", e);
			return 0;
		}
	}

	/**
	 * Get total service load dequeued and processed  successfully by servers running on this machine.
	 * @return
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 */
	public long getWkCompleted() throws SnmpException {
		try {
			return SnmpWalkUtil.getFirstLongValue(walk, OID_TUX_MHE_WKCOMPLETED);
		} catch (SnmpNotFoundException e) {
			logger.debug("无法找到OID对应的值： ", e);
			return 0;
		}
	}

	/**
	 * Get total service load enqueued by clients/servers  running on  this  machine.
	 * @return
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 */
	public long getWkIntiated() throws SnmpException {
		try {
			return SnmpWalkUtil.getFirstLongValue(walk, OID_TUX_MHE_WKINITIATED);
		} catch (SnmpNotFoundException e) {
			logger.debug("无法找到OID对应的值： ", e);
			return 0;
		}
	}

	/**
	 * Get number  of  clients,  both  native   and   workstation, currently logged in to this machine.
	 * @return
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 */

	public long getCurClients() throws SnmpException {
		try {
			return SnmpWalkUtil.getFirstLongValue(walk, OID_TUX_MHE_CURCLIENTS);
		} catch (SnmpNotFoundException e) {
			logger.debug("无法找到OID对应的值： ", e);
			return 0;
		}
	}

	/**
	 * Get number of workstation clients currently  logged  in  to  this machine.
	 * @return
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 */
	public long getWsCurClients() throws SnmpException {
		try {
			return SnmpWalkUtil.getFirstLongValue(walk, OID_TUX_MHE_WSCURCLIENTS);
		} catch (SnmpNotFoundException e) {
			logger.debug("无法找到OID对应的值： ", e);
			return 0;
		}
	}

	/**
	 * Get physical machine identifier. 
	 * @return
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 */
	public InstanceItem[] getPmid() throws SnmpException, SnmpNotFoundException {
		return SnmpWalkUtil.getInstanceItems(walk, OID_TUX_MHE_PMID);
	}

	/**
	 * 关闭资源
	 */
	public void close() {
		if (walk != null) {
			walk.close();
		}
	}

	/**
	 * Get machine state.
	 * @param ii
	 * @return 1 is active, other is inactive.
	 * @throws SnmpException
	 * @throws SnmpNotFoundException
	 * @throws InvalidExpressionException
	 * @throws Exception
	 */
	public int getState(InstanceItem ii) throws SnmpException, SnmpNotFoundException, InvalidExpressionException,
			Exception {
		return (int) SnmpWalkUtil.getExpressionValue(walk, OID_TUX_MHE_STATE, ii.getInstance());
	}
}
