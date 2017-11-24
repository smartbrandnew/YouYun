package com.broada.carrier.monitor.impl.mw.webspheremq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.method.webspheremq.WebSphereMQMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

/**
 * <p>
 * Title: IbmMqMonitor
 * </p>
 * <p>
 * Description: COSS Group
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company: Broada
 * </p>
 *
 * @author plx (panlx@broada.com.cn)
 * @version 2.4
 */
public class IbmMqMonitor extends BaseMonitor {
	private Logger logger = LoggerFactory.getLogger(IbmMqMonitor.class);

	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		String ip = context.getNode().getIp();
		WebSphereMQMethod method = new WebSphereMQMethod(context.getMethod());
		int port = method.getPort();

		IbmMqManager manager = null;
		List<IbmMqQueue> qList = new ArrayList<IbmMqQueue>();
		try {
			manager = IbmMqManager.get(new IbmMqManager.Parameter(ip, port, Integer.valueOf(method.getCcsId())));
			long replyTime = manager.getResponseTime();
			if (replyTime <= 0) {
				replyTime = 1;
			}
			result.setResponseTime((int) replyTime);
			qList = manager.getAllQueues();
		} catch (Exception ibmMqEx) {
			if (logger.isDebugEnabled()) {
				logger.debug("获取队列失败.", ibmMqEx);
			}
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			result.setResultDesc("获取队列失败.");
			return result;
		} finally {
			if (manager != null)
				IbmMqManager.release(manager);
		}

		for (IbmMqQueue curQ : qList) {
			MonitorResultRow row = new MonitorResultRow(curQ.getQName());
			
			int depth = curQ.getCurValue();
			if (depth >= 0) 
				row.setIndicator("WMQ-QUEUE-1", depth);
			
			row.setIndicator("WMQ-QUEUE-2", curQ.getOpenInput());
			row.setIndicator("WMQ-QUEUE-3", curQ.getOpenOutput());
			row.setIndicator("WMQ-QUEUE-4", curQ.getMaxMsgLength());
			row.setIndicator("WMQ-QUEUE-5", IbmMqQueueState.getAllowedDesc(curQ.getPutAllowed()));
			row.setIndicator("WMQ-QUEUE-6", IbmMqQueueState.getAllowedDesc(curQ.getGetAllowed()));
			
			result.addRow(row);
		}

		return result;
	}

}
