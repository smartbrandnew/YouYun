package com.broada.carrier.monitor.impl.device.ifperf;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.MonitorErrorUtil;
import com.broada.carrier.monitor.impl.device.ifstatus.IfstatusMonitor;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.server.api.entity.MonitorTask;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;
import com.broada.common.util.Unit;
import com.broada.snmputil.Snmp;
import com.broada.snmputil.SnmpException;
import com.broada.snmputil.SnmpOID;
import com.broada.snmputil.SnmpResult;
import com.broada.snmputil.SnmpRow;
import com.broada.snmputil.SnmpTable;
import com.broada.snmputil.SnmpTarget;

/**
 * 网络流量监测类，通过固定间隔时间取网络取得的 端口的输出量，和端口的输入量，根据间隔时间内取得差值求得端口速度 id: > 0: 服务监测 <
 * -100000000 && > -200000000：netaction调用 < -200000000 && > -300000000: 实时监测调用 <
 * -300000000: 性能快照调用 User: nile black Date: 2005-3-7 Time: 15:44:17
 */
public class IntfPerfMonitor implements Monitor {
	private static final Log logger = LogFactory.getLog(IntfPerfMonitor.class);

	// 这个变量用于保存上一次的值
	public static final Map<String, SnmpTableWrapper> lastValue = new HashMap<String, SnmpTableWrapper>();

	public static final SnmpOID OID_IF_INOCTETS = new SnmpOID(".1.3.6.1.2.1.2.2.1.10");

	public static final SnmpOID OID_IF_INUCASTPKTS = new SnmpOID(".1.3.6.1.2.1.2.2.1.11");

	public static final SnmpOID OID_IF_INNUCASTPKTS = new SnmpOID(".1.3.6.1.2.1.2.2.1.12");

	public static final SnmpOID OID_IF_INDISCARDS = new SnmpOID(".1.3.6.1.2.1.2.2.1.13");

	public static final SnmpOID OID_IF_INERRORS = new SnmpOID(".1.3.6.1.2.1.2.2.1.14");

	public static final SnmpOID OID_IF_OUTOCTETS = new SnmpOID(".1.3.6.1.2.1.2.2.1.16");

	public static final SnmpOID OID_IF_OUTUCASTPKTS = new SnmpOID(".1.3.6.1.2.1.2.2.1.17");

	public static final SnmpOID OID_IF_OUTNUCASTPKTS = new SnmpOID(".1.3.6.1.2.1.2.2.1.18");
	public static final SnmpOID OID_IF_OUTDISCARDS = new SnmpOID(".1.3.6.1.2.1.2.2.1.19");

	public static final SnmpOID OID_IF_OUTERRORS = new SnmpOID(".1.3.6.1.2.1.2.2.1.20");

	public static final SnmpOID OID_IF_SPEED = new SnmpOID(".1.3.6.1.2.1.2.2.1.5");

	public static final SnmpOID[] COLUMNS = new SnmpOID[] { OID_IF_INOCTETS, OID_IF_INUCASTPKTS, OID_IF_INNUCASTPKTS,
			OID_IF_INDISCARDS, OID_IF_INERRORS, OID_IF_OUTOCTETS, OID_IF_OUTUCASTPKTS, OID_IF_OUTNUCASTPKTS,
			OID_IF_OUTDISCARDS, OID_IF_OUTERRORS, OID_IF_SPEED };

	public static final String IDX_IF_INOCTETS = "INTFPREF-1";

	public static final String IDX_IF_INUCASTPKTS = "INTFPREF-5"; // 单播

	public static final String IDX_IF_INNUCASTPKTS = "INTFPREF-7"; // 广播

	public static final String IDX_IF_INDISCARDS = "INTFPREF-3";

	public static final String IDX_IF_INERRORS = "INTFPREF-9";

	public static final String IDX_IF_OUTOCTETS = "INTFPREF-2";

	public static final String IDX_IF_OUTUCASTPKTS = "INTFPREF-6";

	public static final String IDX_IF_OUTNUCASTPKTS = "INTFPREF-8";

	public static final String IDX_IF_OUTDISCARDS = "INTFPREF-4";

	public static final String IDX_IF_OUTERRORS = "INTFPREF-10";

	private class SnmpTableWrapper {
		private SnmpTable table;

		private long time;

		/**
		 * @return Returns the table.
		 */
		public SnmpTable getTable() {
			return table;
		}

		/**
		 * @param table
		 *            The table to set.
		 */
		public void setTable(SnmpTable table) {
			this.table = table;
		}

		/**
		 * @return Returns the time.
		 */
		public long getTime() {
			return time;
		}

		/**
		 * @param time
		 *            The time to set.
		 */
		public void setTime(long time) {
			this.time = time;
		}
	}

	@Override
	public MonitorResult monitor(MonitorContext context) {
		MonitorResult mr = new MonitorResult();
		MonitorResult result = IfstatusMonitor.walk(new CollectContext(context));
		List<MonitorInstance> instanceList = new ArrayList<MonitorInstance>();
		if (result.getRows() != null) {
			for (MonitorResultRow row : result.getRows()) {
				instanceList.add(row.retInstance());
			}
		}
		MonitorInstance[] instanceArray = instanceList.toArray(new MonitorInstance[] {});
		context.setInstances(instanceArray);
		// 初始化实例OID
		int index = 0;
		SnmpOID[] instances = new SnmpOID[context.getInstances().length];
		for (int i = 0; i < context.getInstances().length; i++) {
			MonitorInstance mi = context.getInstances()[i];
			instances[index++] = new SnmpOID("." + mi.getCode());
		}

		SnmpMethod method = new SnmpMethod(context.getMethod());
		SnmpTarget snmpParam = method.getTarget(context.getNode().getIp());
		snmpParam.setDiscardErrorRow(false);

		MonitorTask srv = context.getTask();
		boolean firstMonitor = !lastValue.containsKey(srv.getId());
		SnmpTableWrapper lastTable = new SnmpTableWrapper();
		if (firstMonitor) {
			/*
			 * if (srv.getId() == null || srv.getId().trim().length() <= 0)
			 * logger.debug("新的服务监测ID：" + srv.getId()); else if (srv.getId() >
			 * -200000000 && srv.getId() <= -100000000)
			 * logger.debug("新的NetAction ID：" + srv.getId()); else if
			 * (srv.getId() > -300000000 && srv.getId() <= -200000000)
			 * logger.debug("新的实时监测ID：" + srv.getId()); else if (srv.getId() >
			 * -400000000 && srv.getId() <= -300000000) logger.debug("新的性能快照ID："
			 * + srv.getId()); else logger.debug("错误的监测ID：" + srv.getId() +
			 * "可能引起数据混乱");
			 */
			lastTable.setTime(System.currentTimeMillis());
			try {
				lastTable.setTable(Snmp.walkTable(snmpParam, COLUMNS, instances));
			} catch (SnmpException e) {
				return MonitorErrorUtil.process(e);
			}

			try {
				Thread.sleep(5000);
			} catch (Exception e) {
				logger.error("首次监测两次采集期间等待失败", e);
			}
		} else {
			logger.debug("继续监测，ID：" + srv.getId());
			lastTable = (SnmpTableWrapper) lastValue.get(srv.getId());
		}

		SnmpTableWrapper nowTable = new SnmpTableWrapper();
		nowTable.setTime(System.currentTimeMillis());
		try {
			nowTable.setTable(Snmp.walkTable(snmpParam, COLUMNS, instances));
		} catch (SnmpException e) {
			return MonitorErrorUtil.process(e);
		}
		// 避免由于上次采集或是计算错误引起的v2永远无法保存入lastValue。
		// 但是正确的重构方法应该是，每采集一次都需要对当前采集的数据有效性进行验证，验证通过才考虑进行流量计算。
		lastValue.put(srv.getId(), nowTable);

		long lastTime, nowTime;
		lastTime = lastTable.getTime();
		nowTime = nowTable.getTime();
		if (context.getInstances().length == 0) {
			return MonitorErrorUtil.process(new SnmpException("无法获取到设备的端口数"));
		}
		for (int i = 0; i < context.getInstances().length; i++) {
			MonitorInstance mi = context.getInstances()[i];
			SnmpOID instanceOID = new SnmpOID("." + mi.getCode());
			String port = mi.getCode();

			SnmpRow lastRow = lastTable.getTable().getRow(instanceOID);
			SnmpRow nowRow = nowTable.getTable().getRow(instanceOID);

			if (lastRow == null || nowRow == null)
				continue;

			mr.addPerfResult(createSpeedPerf(port, IDX_IF_INOCTETS, nowRow.getCell(OID_IF_INOCTETS),
					lastRow.getCell(OID_IF_INOCTETS), nowTable.getTime(), lastTable.getTime()));
			mr.addPerfResult(createSpeedPerf(port, IDX_IF_OUTOCTETS, nowRow.getCell(OID_IF_OUTOCTETS),
					lastRow.getCell(OID_IF_OUTOCTETS), nowTable.getTime(), lastTable.getTime()));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_INDISCARDS, nowRow.getCell(OID_IF_INDISCARDS),
					lastRow.getCell(OID_IF_INDISCARDS), nowTime, lastTime));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_INERRORS, nowRow.getCell(OID_IF_INERRORS),
					lastRow.getCell(OID_IF_INERRORS), nowTime, lastTime));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_INUCASTPKTS, nowRow.getCell(OID_IF_INUCASTPKTS),
					lastRow.getCell(OID_IF_INUCASTPKTS), nowTime, lastTime));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_INNUCASTPKTS, nowRow.getCell(OID_IF_INNUCASTPKTS),
					lastRow.getCell(OID_IF_INNUCASTPKTS), nowTime, lastTime));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_OUTDISCARDS, nowRow.getCell(OID_IF_OUTDISCARDS),
					lastRow.getCell(OID_IF_OUTDISCARDS), nowTime, lastTime));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_OUTERRORS, nowRow.getCell(OID_IF_OUTERRORS),
					lastRow.getCell(OID_IF_OUTERRORS), nowTime, lastTime));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_OUTUCASTPKTS, nowRow.getCell(OID_IF_OUTUCASTPKTS),
					lastRow.getCell(OID_IF_OUTUCASTPKTS), nowTime, lastTime));
			mr.addPerfResult(createNumberPerf(mi.getCode(), IDX_IF_OUTNUCASTPKTS, nowRow.getCell(OID_IF_OUTNUCASTPKTS),
					lastRow.getCell(OID_IF_OUTNUCASTPKTS), nowTime, lastTime));
			mr.addPerfResult(new PerfResult(mi.getCode(), IfstatusMonitor.IFSTATUS_ITEM_SPEED, Unit.bps.to(Unit.Mbps,
					nowRow.getCell(OID_IF_SPEED).getValue().toLong())));
		}

		return mr;
	}

	private PerfResult createNumberPerf(String instanceKey, String code, SnmpResult nowCell, SnmpResult lastCell,
			long nowTime, long lastTime) {
		if (nowCell == null || lastCell == null)
			return new PerfResult(instanceKey, code, 0);
		else if (nowCell.getValue() == null || lastCell.getValue() == null)
			return new PerfResult(instanceKey, code, 0);
		else if (nowCell.getValue().isNull() || lastCell.getValue().isNull())
			return new PerfResult(instanceKey, code, 0);

		long nowValue = 0;
		long lastValue = 0;
		try {
			nowValue = nowCell.getValue().toLong();
			lastValue = lastCell.getValue().toLong();
		} catch (NumberFormatException e) {
			logger.error("SNMP获取到的值非数字型");
			return null;
		}

		return new PerfResult(instanceKey, code, calcuNumber(nowValue, lastValue, nowTime, lastTime));
	}

	private double calcuNumber(long nowResult, long lastResult, long nowTime, long lastTime) {
		long timeDiff = nowTime - lastTime;
		if (timeDiff == 0)
			return 0;

		if (nowResult < lastResult)
			nowResult += (0x1FFFFFFFFL >>> 1);

		double ret = (nowResult - lastResult) / (timeDiff / 1000.0); // kbps
		return new BigDecimal(ret).setScale(2, 4).doubleValue();
	}

	private PerfResult createSpeedPerf(String instanceKey, String code, SnmpResult nowCell, SnmpResult lastCell,
			long nowTime, long lastTime) {
		if (nowCell == null || lastCell == null)
			return null;
		else if (nowCell.getValue() == null || lastCell.getValue() == null)
			return null;
		else if (nowCell.getValue().isNull() || lastCell.getValue().isNull())
			return null;

		long nowValue = 0;
		long lastValue = 0;
		try {
			nowValue = nowCell.getValue().toLong();
			lastValue = lastCell.getValue().toLong();
		} catch (NumberFormatException e) {
			logger.error("SNMP获取到的值非数字型");
			return null;
		}

		return new PerfResult(instanceKey, code, calcuSpeed(nowValue, lastValue, nowTime, lastTime));
	}

	private double calcuSpeed(long nowResult, long lastResult, long nowTime, long lastTime) {
		long timeDiff = nowTime - lastTime;
		if (timeDiff == 0)
			return 0;

		if (nowResult < lastResult)
			nowResult += (0x1FFFFFFFFL >>> 1);

		double ret = (((nowResult - lastResult) * 8.0) / (timeDiff / 1000.0)) / 1000.0; // kbps
		return new BigDecimal(ret).setScale(2, 4).doubleValue();
	}

	/**
	 * 需要snmpWalk的构造参数
	 */
	@Override
	public Serializable collect(CollectContext context) {
		return IfstatusMonitor.walk(context);
	}
}