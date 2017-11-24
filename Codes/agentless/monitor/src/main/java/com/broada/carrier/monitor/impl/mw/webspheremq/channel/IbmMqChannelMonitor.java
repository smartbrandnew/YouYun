package com.broada.carrier.monitor.impl.mw.webspheremq.channel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.broada.carrier.monitor.impl.common.BaseMonitor;
import com.broada.carrier.monitor.impl.common.entity.MonitorConstant;
import com.broada.carrier.monitor.impl.mw.webspheremq.IbmMqManager;
import com.broada.carrier.monitor.method.webspheremq.WebSphereMQMethod;
import com.broada.carrier.monitor.server.api.entity.MonitorResult;
import com.broada.carrier.monitor.server.api.entity.MonitorResultRow;
import com.broada.carrier.monitor.spi.entity.CollectContext;

public class IbmMqChannelMonitor extends BaseMonitor {
	private static final Log logger = LogFactory.getLog(IbmMqChannelMonitor.class);

	private static final String ITEMIDX_RECEIVE_BYTE = "WMQ-CHANNEL-1";

	private static final String ITEMIDX_SEND_BYTE = "WMQ-CHANNEL-2";

	private static final String ITEMIDX_STATE = "WMQ-CHANNEL-3";

	private static final String ITEMIDX_SEND_SPACE = "WMQ-CHANNEL-4";

	private static final String ITEMIDX_AFFAIR_NUM = "WMQ-CHANNEL-5";

	public static int interval = 1000;//间隔采集默认1000(ms)秒钟
	
	@Override
	public Serializable collect(CollectContext context) {
		MonitorResult result = new MonitorResult();
		String deviceIp = context.getNode().getIp();
		WebSphereMQMethod method = new WebSphereMQMethod(context.getMethod());

		IbmMqManager manager = null;
		List cDisLists = new ArrayList();
		try {
			long time = System.currentTimeMillis();
			manager = IbmMqManager
					.get(new IbmMqManager.Parameter(deviceIp, method.getPort(), Integer.valueOf(method.getCcsId())));
			//保存第一次采集的数据list
			List cFstLists = manager.getAllChannels();
			long replyTime = Math.min(manager.getResponseTime(), 1);
			//间隔interval后,再次采集数据
			Thread.sleep(IbmMqChannelMonitor.interval);
			time = System.currentTimeMillis();
			//保存第二次采集的数据list
			List cSecLists = manager.getAllChannels();
			replyTime += System.currentTimeMillis() - time;
			if (replyTime <= 0)
				replyTime = 1L;
			result.setResponseTime(replyTime);
			//这里只处理两次都有采集都有数据(list都不为空)的情况
			if (!cSecLists.isEmpty() && !cFstLists.isEmpty()) {
				//比较两次相同通道中的数据
				for (int i = 0; i < cSecLists.size(); i++) {
					IbmMqChannel secChan = (IbmMqChannel) cSecLists.get(i);
					/**
					 * 如果第二次采集到通道的状态为不活动(0)的时候，则前后两次不做运算，以第二次为最终的数据，否则的话，前后两次进行相减运算
					 * 因为即使第一次如果通道是不活动的情况下，我们已经初始默认为0了，通道的状态除了上述不活动的状态外，应该都是有数据的
					 */
					if (secChan.getCState().intValue() == 0) {
						cDisLists.add(secChan);
					} else {
						if (cFstLists.contains(secChan)) {
							IbmMqChannel fstChan = (IbmMqChannel) cFstLists.get(cFstLists.indexOf(secChan));
							int sendRate =
									(secChan.getSendByte().intValue() - fstChan.getSendByte().intValue()) / IbmMqChannelMonitor.interval
											/ 1000;
							int rcvRate = (secChan.getReceiveByte().intValue() - fstChan.getReceiveByte().intValue())
									/ IbmMqChannelMonitor.interval / 1000;
							GregorianCalendar gregoriancalendar = new GregorianCalendar();
							Date secDate = secChan.getLastMsgTime();
							Date fstDate = fstChan.getLastMsgTime();
							gregoriancalendar.setTime(secDate);
							gregoriancalendar.setTime(fstDate);
							int timeDif = (int) ((secDate.getTime() - fstDate.getTime()) / 1000l);
							int msgCnt = secChan.getAffairNum().intValue() - fstChan.getAffairNum().intValue();
							if (msgCnt == 0 || timeDif == 0) {
								secChan.setSendSpace(new Integer(0));
							} else {
								secChan.setSendSpace(new Integer(timeDif / msgCnt));
							}
							secChan.setSendByte(new Integer(sendRate));
							secChan.setReceiveByte(new Integer(rcvRate));
							cDisLists.add(secChan);
						} else {
							cDisLists.add(secChan);
						}
					}
				}
			}
		} catch (Exception ex) {
			result.setResultDesc("获取MQ通道列表失败");
			result.setState(MonitorConstant.MONITORSTATE_FAILING);
			if (logger.isDebugEnabled()) {
				logger.debug("获取MQ通道列表失败", ex);
			}
			return result;
		} finally {
			if (manager != null)
				IbmMqManager.release(manager);
		}
		for (int j = 0; j < cDisLists.size(); j++) {
			IbmMqChannel channel = (IbmMqChannel) cDisLists.get(j);
			
			double reRB = Double.parseDouble(channel.getReceiveByte() == null ? "0" : channel.getReceiveByte().toString());
			double reSB = Double.parseDouble(channel.getSendByte() == null ? "0" : channel.getSendByte().toString());
			int reState = channel.getCState() == null ? 1 : channel.getCState().intValue();
			double reSSpace = Double.parseDouble(channel.getSendSpace() == null ? "0" : channel.getSendSpace().toString());
			double reA = Double.parseDouble(channel.getAffairNum().toString());			
			
			MonitorResultRow row = new MonitorResultRow(channel.getCName());
			row.setIndicator(ITEMIDX_RECEIVE_BYTE, reRB);
			row.setIndicator(ITEMIDX_SEND_BYTE, reSB);
			row.setIndicator(ITEMIDX_STATE, IbmMqChannelState.getMQChState(reState));
			row.setIndicator(ITEMIDX_SEND_SPACE, reSSpace);
			row.setIndicator(ITEMIDX_AFFAIR_NUM, reA);
			result.addRow(row);
		}

		return result;
	}
}
