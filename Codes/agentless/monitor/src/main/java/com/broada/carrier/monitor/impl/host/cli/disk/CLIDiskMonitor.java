package com.broada.carrier.monitor.impl.host.cli.disk;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.method.cli.CLIConstant;
import com.broada.carrier.monitor.method.cli.CLIExecutor;
import com.broada.carrier.monitor.method.cli.entity.CLIResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;
import com.broada.carrier.monitor.spi.entity.MonitorContext;

public class CLIDiskMonitor extends BaseMonitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(CLIDiskMonitor.class);

	@Override
	public MonitorResult monitor(MonitorContext context) {
		return collect(context.getTask().getId(), new CollectContext(context));
	}

	@Override
	public Serializable collect(CollectContext context) {
		return collect("-1", context);
	}

	private MonitorResult collect(String taskId, CollectContext context) {
		MonitorResult result = new MonitorResult();
		CLIResult cliResult = null;
		try {
			long replyTime = System.currentTimeMillis();
			cliResult = new CLIExecutor(taskId).execute(context.getNode(), context.getMethod(),
					CLIConstant.COMMAND_DISK);
			replyTime = System.currentTimeMillis() - replyTime;
			if (replyTime <= 0)
				replyTime = 1L;
		} catch (Throwable e) {
			return CLIExecutor.processError(e);
		}

		List<Properties> diskInfos = cliResult.getListTableResult();
		for (int index = 0; index < diskInfos.size(); index++) {
			Properties properties = (Properties) diskInfos.get(index);
			MonitorResultRow row = new MonitorResultRow((String) properties.get("diskname"), (String) properties.get("diskname"));
			row.addTag("device:" + row.getInstCode());
			float capacity = Float.parseFloat((String) properties.get("capacity"));
			if(capacity < 0f)
				LOG.error("磁盘名为:" + row.getInstCode() + "的利用率数据异常，当前采集的值为:" + capacity);
			row.setIndicator("CLI-DISKSPACE-3", capacity < 0f? 0f : capacity);
			row.setIndicator("CLI-DISKSPACE-2", Float.parseFloat((String) properties.get("available")));
			row.setIndicator("CLI-DISKSPACE-1", Float.parseFloat((String) properties.get("blocks")));
			result.addRow(row);
		}
		return result;
	}
}
