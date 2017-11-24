package com.broada.carrier.monitor.impl.mw.tibco.subject;

import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.tibco.TibcoSnmpPerf;
import com.broada.carrier.monitor.method.snmp.SnmpMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorInstance;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

import java.io.Serializable;
import java.util.*;

public class TibcoSbjMonitor implements Monitor {
	//该监测器涉及到的性能监测项
	//Subject的msg个数
	private static final String ITEM_CON_SUB_MSG = "TIBCOSUBJECT-1";

	//Subject字节数
	private static final String ITEM_CON_SUB_BYTES = "TIBCOSUBJECT-2";

	//该监测器需要采集的mib值
	public static final String[] columns = new String[] { TibcoSnmpPerf.SUB_ENTRY_SUB_MSGS,
			TibcoSnmpPerf.SUB_ENTRY_SUB_BYTES };

	public TibcoSbjMonitor() {
	}

	@Override public MonitorResult monitor(MonitorContext context) {
		MonitorResult mresult = new MonitorResult();
		boolean wonted = true;
		StringBuffer msg = new StringBuffer();

		Collection<MonitorInstance> c = new ArrayList<MonitorInstance>(Arrays.asList(context.getInstances()));
		List<PerfResult> perfs = new ArrayList<PerfResult>();
		SnmpMethod method = new SnmpMethod(context.getMethod());

		MonitorNode mn = context.getNode();

		//校验是否监测subj msg 阈值
		TibcoSnmpPerf perfWalk = null;

		Map v = null;
		try {
			long replyTime = System.currentTimeMillis();
			perfWalk = new TibcoSnmpPerf(mn.getIp(), method.getPort(), method.getCommunity(), method.getVersion().getId());
			v = perfWalk.getSubjEntrysPerf(columns);
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
			mresult.setResponseTime(replyTime);
		} catch (Exception e) {
			mresult.setState(MonitorConstant.MONITORSTATE_FAILING);
			mresult.setResultDesc("采集错误,也许目标节点上的监测代理RvTrace没有运行.");
			return mresult;
		} finally {
			if (perfWalk != null) {
				perfWalk.close();
			}
		}

		for (Iterator<MonitorInstance> iter = c.iterator(); iter.hasNext(); ) {
			MonitorInstance item = (MonitorInstance) iter.next();
			String instKey = item.getCode();
			//String instkeyOid = TextUtil.convStringToSpotAlgorism(instKey == null ? "" : instKey);
			for (int i = 0; i < columns.length; i++) {
				PerfResult perf = new PerfResult("-1", 0);
				if (columns[i].equals(TibcoSnmpPerf.SUB_ENTRY_SUB_MSGS)) {
					perf.setInstanceKey(instKey);
					perf.setItemCode(ITEM_CON_SUB_MSG);
					Object o_dv = v.get(((String) TibcoSnmpPerf.oids.get(TibcoSnmpPerf.SUB_ENTRY_SUB_MSGS)) + "." + instKey);
					if (o_dv == null)
						o_dv = "0";
					perf.setValue(Double.parseDouble((String) o_dv));

				} else if (columns[i].equals(TibcoSnmpPerf.SUB_ENTRY_SUB_BYTES)) {
					perf.setInstanceKey(instKey);
					perf.setItemCode(ITEM_CON_SUB_BYTES);
					Object o_dv = v.get(((String) TibcoSnmpPerf.oids.get(TibcoSnmpPerf.SUB_ENTRY_SUB_BYTES)) + "." + instKey);
					if (o_dv == null)
						o_dv = "0";
					perf.setValue(Double.parseDouble((String) o_dv));

				} else {
					continue;
				}
				perfs.add(perf);
			}
		}

		PerfResult[] arrayPerfs = new PerfResult[perfs.size()];
		for (int i = 0; i < perfs.size(); i++) {
			PerfResult item = (PerfResult) perfs.get(i);
			arrayPerfs[i] = item;
		}
		mresult.setPerfResults(arrayPerfs);
		//    mresult.setPerfResults((PerfResult[] )perfs.toArray());

		if (wonted) {
			mresult.setState(MonitorConstant.MONITORSTATE_NICER);
		} else {
			mresult.setState(MonitorConstant.MONITORSTATE_OVERSTEP);
		}
		if (msg.length() == 0) {
			msg.append("监测一切正常");
			//      msg.append("所有主题的消息个数正常.");
		}
		mresult.setResultDesc(msg.toString());

		return mresult;
	}

	@Override public Serializable collect(CollectContext context) {
		return null;
	}
}
