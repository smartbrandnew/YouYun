package com.broada.carrier.monitor.impl.host.cli.windowsio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

/**
 * windows磁盘Ｉ／Ｏ监测监测器实现
 * 
 * @author Huangjb (huangjb@broada.com.cn)
 * Create By 2008-6-30 下午05:11:37
 */
public class CLIIOMonitor extends BaseMonitor {

	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context));
	}

	/**
	 * 传入参数srvId
	 */
	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context);
	}

	private MonitorResult collect(String taskId, CollectContext context) {
    List conditions = null;
    try {
      conditions = CLIIOExecutor.procIO(taskId, context.getNode(), context.getMethod());
    } catch (Throwable fe) {
    	return CLIExecutor.processError(fe);
    }
    List<PerfResult> perfs = new ArrayList<PerfResult>();

    for (int index = 0; index < conditions.size(); index++) {
      CLIIOMonitorCondition cond = (CLIIOMonitorCondition) conditions.get(index);
      PerfResult perf = new PerfResult("CLI-WINDOWSDEVICEIO-1", cond.getCurrentDiskQueueLength());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-2", cond.getDiskReadsPerSec());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-3", cond.getDiskWritesPerSec());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-4", cond.getDiskReadBytesPerSec());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-5", cond.getDiskWriteBytesPerSec());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-6", cond.getPercentDiskReadTime());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-7", cond.getPercentDiskWriteTime());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-8", cond.getPercentDiskTime());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

      perf = new PerfResult("CLI-WINDOWSDEVICEIO-9", cond.getPercentIdleTime());
      perf.setInstKey(cond.getField());
      perfs.add(perf);

    }

    MonitorResult result = new MonitorResult();
    result.setPerfResults((PerfResult[]) perfs.toArray(new PerfResult[perfs.size()]));
    return result;
  }
}
