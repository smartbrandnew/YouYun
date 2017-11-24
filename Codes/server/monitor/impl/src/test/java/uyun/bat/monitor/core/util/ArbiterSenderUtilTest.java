package uyun.bat.monitor.core.util;

import java.io.IOException;

import uyun.bat.monitor.api.entity.MonitorState;
import uyun.bat.monitor.core.entity.CheckContext;

public class ArbiterSenderUtilTest {


	public void testGenerateTrapMsg() {
		//TODO
	}


	public void testSendPDU() throws IOException {
		//TODO
		CheckContext context=new CheckContext();
		context.setHostName("zhoucp");
		context.setIp("10.1.240.147");
		context.setResId("1234567");
		context.setValue("0.3");
		MonitorState monitorState=MonitorState.ERROR;
		context.setMonitorState(monitorState);
		String content = "我是一条测试数据";
		ArbiterSenderUtil.sendPDU(ArbiterSenderUtil.generatePerformenceMetricAlertMsg(context, content));
		//sender.sendPDU(sender.generateHostOrAppUnableAlertMsg(context, content));
		//sender.sendPDU(sender.generateAbnormalEventAlertMsg(context, content));
	}


	public void testCreateTarget4Trap() {
		//TODO
	}

}
