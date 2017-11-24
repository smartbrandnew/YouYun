package com.broada.carrier.monitor.impl.mw.tuxedo.machine;

import java.io.Serializable;
import java.util.Date;

import org.snmp4j.mp.SnmpConstants;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.method.snmp.SnmpVersion;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.snmp.SnmpNotFoundException;
import com.broada.snmp.SnmpWalk;

/**
 * <p>Title: TuxMheMonitor</p>
 * <p>Description: COSS Group</p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: Broada</p>
 *
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */
public class TuxMheMonitor extends BaseMonitor {

	private static final String ITEMIDX_TUXMHEWKCPTTPS = "TUX-MACHINE-1";

	private static final String ITEMIDX_TUXMHEWKINTTPS = "TUX-MACHINE-2";

	private static final String ITEMIDX_TUXMHECLIENTS = "TUX-MACHINE-3";

	private static final String ITEMIDX_TUXMHEWSCLIENTS = "TUX-MACHINE-4";

	private static Date lastD;

	private static long lastWkCptTps;

	private static long lastWkIntTps;

	@Override public Serializable collect(CollectContext context) {
		PerfResult tuxMheWkCptTpsPerc = new PerfResult(ITEMIDX_TUXMHEWKCPTTPS, false);
		PerfResult tuxMheWkIntTpsPerc = new PerfResult(ITEMIDX_TUXMHEWKINTTPS, false);
		PerfResult tuxMheClients = new PerfResult(ITEMIDX_TUXMHECLIENTS, false);
		PerfResult tuxMheWsClients = new PerfResult(ITEMIDX_TUXMHEWSCLIENTS, false);

		MonitorResult result = new MonitorResult();
		result.setResponseTime(MonitorResult.RESPONSETIME_NORESPONSE);

		SnmpMethod option = new SnmpMethod(context.getMethod());
		int port = option.getPort();
		SnmpVersion version = option.getVersion();
		int snmpVer = version.getId();
		String securityLevel = option.getSecurityLevel();
		String securityName = option.getSecurityName();
		String authProtocol = option.getAuthProtocol().getOID().toString();
		String authPassword = option.getAuthPassword();
		String privProtocol = option.getPrivProtocol().getOID().toString();
		String privPassword = option.getPrivPassword();
		String comm = option.getCommunity();
		String ipAddr = context.getNode().getIp();
		int timeout = (int) option.getTimeout();
		SnmpWalk walk = null;
		if (snmpVer == SnmpConstants.version3) {
			walk = new SnmpWalk(ipAddr, port, snmpVer, timeout, securityLevel, securityName, authProtocol, authPassword,
					privProtocol, privPassword);
		} else {
			walk = new SnmpWalk(snmpVer, ipAddr, port, comm);
			walk.setTimeout(timeout);
		}
		SnmpTuxManager mgr = new SnmpTuxManager(walk);
		//监测结果信息和告警的当前值信息
		StringBuffer msgSB = new StringBuffer();
		StringBuffer valSB = new StringBuffer();
		boolean state = true;
		MonitorState stateCode = MonitorConstant.MONITORSTATE_NICER;

		try {
			long respTimeTotal = System.currentTimeMillis();
			long clients = mgr.getCurClients();
			long wsClients = mgr.getWsCurClients();
			respTimeTotal = System.currentTimeMillis() - respTimeTotal;
			if (respTimeTotal <= 0) {
				respTimeTotal = 1L;
			}
			result.setResponseTime(respTimeTotal);
			long wkCptTps, wkIntTps;
			if (lastD == null) {
				long respTime = System.currentTimeMillis();
				long wkCptTps1 = mgr.getWkCompleted();
				long wkIntTps1 = mgr.getWkIntiated();
				respTime = System.currentTimeMillis() - respTime;
				respTimeTotal += respTime;
				Thread.sleep(5000);
				respTime = System.currentTimeMillis();
				long wkCptTps2 = mgr.getWkCompleted();
				long wkIntTps2 = mgr.getWkIntiated();
				respTime = System.currentTimeMillis() - respTime;
				respTimeTotal += respTime;
				if (wkCptTps1 > wkCptTps2) {
					wkCptTps = ((long) Math.pow(2, 31) + wkCptTps1 - wkCptTps2) / 5;
				} else {
					wkCptTps = (wkCptTps2 - wkCptTps1) / 5;
				}
				if (wkIntTps1 > wkIntTps2) {
					wkIntTps = ((long) Math.pow(2, 31) + wkIntTps1 - wkCptTps2) / 5;
				} else {
					wkIntTps = (wkIntTps2 - wkIntTps1) / 5;
				}
			} else {
				long respTime = System.currentTimeMillis();
				wkCptTps = mgr.getWkCompleted();
				wkIntTps = mgr.getWkIntiated();
				respTime = System.currentTimeMillis() - respTime;
				respTimeTotal += respTime;
				long times = (new Date().getTime() - lastD.getTime()) / 1000;
				if (times < 1) {
					Thread.sleep(1000);
					times = 1;
				}
				if (lastWkCptTps > wkCptTps) {
					wkCptTps = ((long) Math.pow(2, 31) + lastWkCptTps - wkCptTps) / times;
				} else {
					wkCptTps = (wkCptTps - lastWkCptTps) / times;
				}
				if (lastWkIntTps > wkIntTps) {
					wkIntTps = ((long) Math.pow(2, 31) + lastWkIntTps - wkIntTps) / times;
				} else {
					wkIntTps = (wkIntTps - lastWkIntTps) / times;
				}
			}
			lastD = new Date();
			lastWkCptTps = wkCptTps;
			lastWkIntTps = wkIntTps;
			long respTime = System.currentTimeMillis();
			respTime = System.currentTimeMillis() - respTime;
			respTimeTotal += respTime;

			tuxMheWkCptTpsPerc.setValue(wkCptTps);

			tuxMheWkIntTpsPerc.setValue(wkIntTps);

			tuxMheClients.setValue(clients);

			tuxMheWsClients.setValue(wsClients);

			respTime = System.currentTimeMillis();
			long numTran = mgr.getNumTran();
			long numTranCmt = mgr.getNumTranCmt();
			long wkCpt = mgr.getWkCompleted();
			long wkInt = mgr.getWkIntiated();
			respTime = System.currentTimeMillis() - respTime;
			respTimeTotal += respTime;
			msgSB.append("机器接收到的事务总数量" + numTran + ".\n");
			msgSB.append("机器处理过的事务总数量" + numTranCmt + ".\n");
			msgSB.append("被成功处理的队列服务数量" + wkCpt + ".\n");
			msgSB.append("运行中的入队列服务数量" + wkInt + ".\n");

			result.setResponseTime(respTimeTotal);
			if (state) {
				stateCode = MonitorConstant.MONITORSTATE_NICER;
			} else {
				stateCode = MonitorConstant.MONITORSTATE_OVERSTEP;
			}
		} catch(SnmpNotFoundException e){
			
		}catch (Exception ex) {
			msgSB.append(ex.getMessage());
			valSB.append(ex.getMessage());
			stateCode = MonitorConstant.MONITORSTATE_FAILING;
		} finally {
			walk.close();
		}
		result.setPerfResults(new PerfResult[] { tuxMheWkCptTpsPerc, tuxMheWkIntTpsPerc, tuxMheClients, tuxMheWsClients });
		result.setState(stateCode);
		if (result.getState() == MonitorConstant.MONITORSTATE_NICER) {
			result.setResultDesc("监测一切正常");
		} else {
			result.setResultDesc(msgSB.toString());
		}

		return result;
	}
}
