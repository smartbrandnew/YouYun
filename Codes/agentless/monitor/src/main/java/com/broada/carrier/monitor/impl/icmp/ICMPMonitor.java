package com.broada.carrier.monitor.impl.icmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorState;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.Monitor;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * ICMP 监听器实现类
 * ICMP监测产生的性能数据如果timeout，没有回应，则返回的值为-1，这样处理可能会有歧义，建议改进
 * <p>Title: </p>
 * <p>Description: NMS Group</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Broada</p>
 * @author Eric Liu
 * @version 1.0
 */
public class ICMPMonitor implements Monitor {
	static final String ITEMIDX_TTLAVG = "ICMP-1";

	public MonitorResult monitor(MonitorContext context) {
		MonitorResult result = new MonitorResult();
		String param = context.getTask().getParameter();
		String ip = context.getNode().getIp();
		ICMPParameter p = new ICMPParameter(param);
		PerfResult perf = new PerfResult(ITEMIDX_TTLAVG);

		List<String> extAddrs = p.getExtAddrs();
		List<String> addrs = new ArrayList<String>(extAddrs);
		addrs.add(0, ip);
		StringBuffer sb = new StringBuffer();
		int ttl = new NativeICMPMonitor().monitor(p, addrs, sb);

		if (ttl < 0) {
			result.setMessage(sb.toString());
			result.setState(MonitorState.FAILED);
			return result;
		}

		//处理性能
		perf.setValue(ttl);
		result.setPerfResults(new PerfResult[] { perf });  
		return result;
	}

	/**
	 * 监测处理器
	 * 
	 * @author Maico Pang (panghf@broada.com.cn)
	 */
	public interface MonitorProcesser {
		/**
		 * 真正的监测实现方法
		 * @param p 监测参数
		 * @param addrs 监测地址列表
		 * @param desc 结果描述
		 * @return 超时返回-1,否则返回响应时间(毫秒)
		 */
		public int monitor(ICMPParameter p, List<String> addrs, StringBuffer desc);
	}

	/**
	 * callParams参数要带有parameter XML字符串值
	 */
	public Serializable collect(CollectContext context) {
		String parametersString = context.getParameter();
		String ip = context.getNode().getIp();
		ICMPParameter p = new ICMPParameter(parametersString);
		List<String> extAddrs = p.getExtAddrs();
		List<String> addrs = new ArrayList<String>(extAddrs);
		addrs.add(0, ip);
		StringBuffer sb = new StringBuffer();
		int ttl = new NativeICMPMonitor().monitor(p, addrs, sb);
		PerfResult performance = new PerfResult("responseTime",ttl);
		performance.setValue(ttl);
		return performance;
	}
}
