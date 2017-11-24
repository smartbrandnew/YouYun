package com.broada.carrier.monitor.impl.host.cli.windowsio;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.method.cli.error.CLIConnectException;
import com.broada.carrier.monitor.method.cli.error.CLIException;
import com.broada.carrier.monitor.method.cli.error.CLILoginFailException;
import com.broada.carrier.monitor.method.cli.error.CLIResultParseException;
import com.broada.carrier.monitor.server.api.entity.MonitorMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorNode;

public class CLIIOExecutor {
	public static List procIO(String taskId, MonitorNode node, MonitorMethod method) throws CLIConnectException,
			CLILoginFailException, CLIResultParseException, CLIException {
		CLIResult result = null;
		result = new CLIExecutor(taskId).execute(node, method,
				CLIConstant.COMMAND_IO);

		List ioInfos = result.getListTableResult();
		List ioConds = new ArrayList();
		for (int index = 0; index < ioInfos.size(); index++) {
			Properties properties = (Properties) ioInfos.get(index);
			String name = properties.get("name").toString().trim();
			String currentDiskQueueLength = (String) properties.get("currentDiskQueueLength");
			String diskReadBytesPerSec = (String) properties.get("diskReadBytesPerSec");
			String diskReadsPerSec = (String) properties.get("diskReadsPerSec");
			String diskWriteBytesPerSec = (String) properties.get("diskWriteBytesPerSec");
			String diskWritesPerSec = (String) properties.get("diskWritesPerSec");
			String percentDiskReadTime = (String) properties.get("percentDiskReadTime");
			String percentDiskTime = (String) properties.get("percentDiskTime");
			String percentDiskWriteTime = (String) properties.get("percentDiskWriteTime");
			String percentIdleTime = (String) properties.get("percentIdleTime");

			CLIIOMonitorCondition cond = new CLIIOMonitorCondition();
			cond.setField(name);
			cond.setCurrentDiskQueueLength(Integer.parseInt(currentDiskQueueLength));
			cond.setDiskReadBytesPerSec(Float.parseFloat(diskReadBytesPerSec));
			cond.setDiskReadsPerSec(Float.parseFloat(diskReadsPerSec));
			cond.setDiskWriteBytesPerSec(Float.parseFloat(diskWriteBytesPerSec));
			cond.setDiskWritesPerSec(Float.parseFloat(diskWritesPerSec));
			cond.setPercentDiskReadTime(Float.parseFloat(percentDiskReadTime));
			cond.setPercentDiskTime(Float.parseFloat(percentDiskTime));
			cond.setPercentDiskWriteTime(Float.parseFloat(percentDiskWriteTime));
			cond.setPercentIdleTime(Float.parseFloat(percentIdleTime));
			ioConds.add(cond);
		}
		return ioConds;
	}
}
