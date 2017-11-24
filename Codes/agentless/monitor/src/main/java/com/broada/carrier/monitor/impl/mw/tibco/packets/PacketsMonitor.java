package com.broada.carrier.monitor.impl.mw.tibco.packets;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.tibco.TibcoSnmpPerf;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Vector;

public class PacketsMonitor extends BaseMonitor {
	private Log logger = LogFactory.getLog(PacketsMonitor.class);
	//该监测器涉及到的性能监测项
	//多播/广播错包率
	private static final String ITEM_CON_MC_BAD_RATE = "TIBCOPACKETS-1";

	//多播/广播丢包率
	private static final String ITEM_CON_MC_LOST_RATE = "TIBCOPACKETS-2";

	//多播/广播无效包的比率
	private static final String ITEM_CON_MC_NULL_RATE = "TIBCOPACKETS-3";

	//重传包中被拒绝的比率
	public static final String ITEM_CON_RT_REJ_RATE = "TIBCOPACKETS-4";

	//重传包中传输错误比率
	public static final String ITEM_CON_RT_BAD_RATE = "TIBCOPACKETS-5";

	//点对点传输错包率
	public static final String ITEM_CON_PTP_BAD_RATE = "TIBCOPACKETS-6";

	//点对点传输包被拒绝的比率
	public static final String ITEM_CON_PTP_NAK_RATE = "TIBCOPACKETS-7";

	//该监测器需要采集的mib值
	public static final String[] columns = new String[] { TibcoSnmpPerf.TRDP_MC_DATA_PKT, TibcoSnmpPerf.TRDP_MC_NULL_PKT,
			TibcoSnmpPerf.TRDP_MC_BAD_PKT, TibcoSnmpPerf.TRDP_MC_SEQGAP_PKT, TibcoSnmpPerf.TRDP_RT_BAD_PKT,
			TibcoSnmpPerf.TRDP_RT_REJ_PKT, TibcoSnmpPerf.TRDP_RT_REQ_PKT, TibcoSnmpPerf.PTP_BAD_PKT,
			TibcoSnmpPerf.PTP_NAK_PKT, TibcoSnmpPerf.PTP_TOTAL_DATA };

	/**
	 * 默认构造
	 */
	public PacketsMonitor() {
	}

	/**
	 * 进行数据监测
	 *
	 * @param context
	 * @return
	 * @throws Exception
	 */
	@Override public Serializable collect(CollectContext context) {
		MonitorResult mresult = new MonitorResult();
		//监测性能实例项
		PerfResult cmBadRate = new PerfResult(ITEM_CON_MC_BAD_RATE, false);
		PerfResult cmLostRate = new PerfResult(ITEM_CON_MC_LOST_RATE, false);
		PerfResult cmNullRate = new PerfResult(ITEM_CON_MC_NULL_RATE, false);
		PerfResult rtRejRate = new PerfResult(ITEM_CON_RT_REJ_RATE, false);
		PerfResult rtBadRate = new PerfResult(ITEM_CON_RT_BAD_RATE, false);
		PerfResult ptpBadRate = new PerfResult(ITEM_CON_PTP_BAD_RATE, false);
		PerfResult ptpNakRate = new PerfResult(ITEM_CON_PTP_NAK_RATE, false);

		//设置返回性能数据
		PerfResult[] perfs = { cmBadRate, cmLostRate, cmNullRate, rtRejRate, rtBadRate, ptpBadRate, ptpNakRate };
		mresult.setPerfResults(perfs);

		//PerfResult[] prs = new PerfResult();
		MonitorNode mn = context.getNode();
		Vector<Object> curV = new Vector<Object>();
		curV.add(new Long(System.currentTimeMillis()));

		TibcoSnmpPerf perf = null;
		try {
			long replyTime = System.currentTimeMillis();
			SnmpMethod method = new SnmpMethod(context.getMethod());
			int port = method.getPort();
			String community = method.getCommunity();
			perf = new TibcoSnmpPerf(mn.getIp(), port, community, method.getVersion().getId());
			curV.add(perf.getOidItemPerf(columns));
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			mresult.setResponseTime(replyTime);
		} catch (Exception ex) {
			mresult.setState(MonitorConstant.MONITORSTATE_FAILING);
			mresult.setResultDesc("采集错误,也许监测代理RvTrace没有运行.");
			return mresult;
		} finally {
			if (perf != null) {
				perf.close();
			}
		}

		if (curV.size() != 2) {
			mresult.setResultDesc("采集数据时发生异常.");
			//return mr;
		}
		//获取采集的数据，开始比较分析
		//    Map lastMap = (Map) lastV.get(1);
		Map curMap = (Map) curV.get(1);

		//多播/广播错包率
		storeDiscardRate(cmBadRate, (String) curMap.get(TibcoSnmpPerf.TRDP_MC_BAD_PKT),
				(String) curMap.get(TibcoSnmpPerf.TRDP_MC_DATA_PKT));
		//多播/广播丢包率
		storeDiscardRate(cmLostRate, (String) curMap.get(TibcoSnmpPerf.TRDP_MC_SEQGAP_PKT),
				(String) curMap.get(TibcoSnmpPerf.TRDP_MC_DATA_PKT));
		//多播/广播无效包的比率
		storeDiscardRate(cmNullRate, (String) curMap.get(TibcoSnmpPerf.TRDP_MC_NULL_PKT),
				(String) curMap.get(TibcoSnmpPerf.TRDP_MC_DATA_PKT));
		//重传包中被拒绝的比率
		storeDiscardRate(rtRejRate, (String) curMap.get(TibcoSnmpPerf.TRDP_RT_REJ_PKT),
				(String) curMap.get(TibcoSnmpPerf.TRDP_RT_REQ_PKT));
		//重传包中传输错误比率
		storeDiscardRate(rtBadRate, (String) curMap.get(TibcoSnmpPerf.TRDP_RT_BAD_PKT),
				(String) curMap.get(TibcoSnmpPerf.TRDP_RT_REQ_PKT));
		//点对点传输错包率
		storeDiscardRate(ptpBadRate, (String) curMap.get(TibcoSnmpPerf.PTP_BAD_PKT),
				(String) curMap.get(TibcoSnmpPerf.PTP_TOTAL_DATA));
		//点对点传输包被拒绝的比率
		storeDiscardRate(ptpNakRate,
				(String) curMap.get(TibcoSnmpPerf.PTP_NAK_PKT), (String) curMap.get(TibcoSnmpPerf.PTP_TOTAL_DATA));

		//把最近一次的采集值放到缓存中
		//    lastValue.put(String.valueOf(srv.getId()), curV);

		//以下检查各项内容
		StringBuffer msg = new StringBuffer();
		StringBuffer currVal = new StringBuffer();// 当前情况，用于发送Trap
		boolean wonted = true;

    /*TODO 这部分为condition部分内容 因为不存在condtion无法进行判断 删了
    多播/广播错包率
    多播/广播丢包率
    多播/广播无效包的比率
    多播/广播丢包率
    重传包中被拒绝的比率
    重传包中传输错误比率
    点对点传输错包率
    点对点传输包被拒绝的比率
     */

		if (currVal.length() == 0) {
			currVal.append("数据包传输恢复正常.");
		}
		if (wonted) {
			mresult.setState(MonitorConstant.MONITORSTATE_NICER);
			mresult.setResultDesc("监测一切正常");
		} else {
			mresult.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
			mresult.setResultDesc(msg.toString());
		}

		return mresult;
	}

	private void storeDiscardRate(PerfResult perf, String cur, String curTotal) {
		try {
			long c = Long.parseLong(cur);
			long ct = Long.parseLong(curTotal);
			double v = (int) calcuRate(c, ct);
			perf.setValue(v);
		} catch (NumberFormatException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	private double calcuRate(long c, long ct) throws Exception {
		double d = 0.0;
		if (ct != 0) {
			d = ((double) c / ct) * 100;
		}
		d = new BigDecimal(d).setScale(2, 4).doubleValue();
		if (d > 100 || d < 0) {
			throw new Exception("不合理的数值,抛弃！");
		}
		return d;
	}
}
