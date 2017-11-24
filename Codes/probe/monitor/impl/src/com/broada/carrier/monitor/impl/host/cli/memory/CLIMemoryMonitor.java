package com.broada.carrier.monitor.impl.host.cli.memory;

import java.io.Serializable;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.PerfResult;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLIMemoryMonitor extends BaseMonitor {

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
    CLIResult cliResult = null;
    float real = 0.0f;
    float realuseutil = 0.0f;
    float virtualutil = 0.0f;
    float virtualused = 0.0f;
    try {
      cliResult = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(),
          CLIConstant.COMMAND_MEMORY);
      try {
        real = Float.parseFloat(cliResult.getPropResult().getProperty("real"));
        realuseutil = Float.parseFloat(cliResult.getPropResult().getProperty("realuseutil"));
        virtualused = Float.parseFloat(cliResult.getPropResult().getProperty("virtualused"));
        virtualutil = Float.parseFloat(cliResult.getPropResult().getProperty("virtualutil"));
      } catch (Throwable t) {
        throw new CLIResultParseException(t);
      }
    } catch (Throwable fe) {
      return CLIExecutor.processError(fe);
    }

    PerfResult[] perfs = new PerfResult[] { new PerfResult("CLI-HOSTMEMORY-3", real), new PerfResult("CLI-HOSTMEMORY-1", realuseutil),
        new PerfResult("CLI-HOSTMEMORY-4", virtualused), new PerfResult("CLI-HOSTMEMORY-2", virtualutil) };
    MonitorResult result = new MonitorResult(perfs);    
    return result;
  }
}
