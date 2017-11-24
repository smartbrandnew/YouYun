package com.broada.carrier.monitor.client.impl;

import java.util.TimerTask;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.broada.carrier.monitor.server.api.service.ServerSystemService;
import com.broada.component.utils.error.ErrorUtil;

/**
 * 连接任务
 * 
 * 主要功能：创建连接（获取服务端rmi服务）；
 * @author panhk
 *
 */
public class ConnectTask extends TimerTask {
	private static final Logger logger = LoggerFactory.getLogger(ConnectTask.class);
	private static final int DEFAULT_TRY_COUNT = Integer.parseInt(System.getProperty("connect.trycount", "10"));
	private static final String CLIENT_SYSTEM_PROPERTIES = "clientSystemProperties";

	/**
	 * 检查重复次数
	 */
	private int tryCount = DEFAULT_TRY_COUNT;

	public ConnectTask() {
		super();		
	}

	public static void setServerSystemProperty() {
		if (logger.isDebugEnabled())
			logger.debug("服务端属性加载开始");
		ServerSystemService service = ServerContext.getSystemService();
		String value = service.getProperty(CLIENT_SYSTEM_PROPERTIES);
		if (value == null)
			value = "noSingleTypes";
		if (logger.isDebugEnabled())
			logger.debug("服务端属性加载列表为：" + value);
		String[] properties = value.split(",");
		for (String property : properties) {
			String propValue = service.getProperty(property);
			if (logger.isDebugEnabled())
				logger.debug(String.format("服务端属性：%s=%s", property, propValue));
			if (propValue == null)
				continue;
			System.setProperty(property, propValue);
		}
		if (logger.isDebugEnabled())
			logger.debug("服务端属性加载完成");
	}

	/**
	 * 测试连接的可用性
	 */
	public static void testConnect() throws Exception {
		ServerContext.getSystemService().getTime();
	}

	/**
	 * 方法逻辑：
	 * 1. 获取服务端连接
	 * 2. 判断连接的有效性
	 * 3. 获取服务端版本信息，并进行以下操作：
	 * 		a. 并且判断服务端是否发生更新，如果服务端与客户端不再匹配，那么客户端将退出
	 * 		b. 判断客户端是否需要下载更新版本，如果需要则下载，然后客户端自动退出
	 * 
	 */
	@Override
	public void run() {
		try {
			testConnect();
			tryCount = DEFAULT_TRY_COUNT;
		} catch (Exception e) {			
			ErrorUtil.warn(logger, "连接服务端[" + ServerContext.getIp() + "]第" + (DEFAULT_TRY_COUNT - tryCount) + "次失败", e);
			if (tryCount <= 0) {
				JOptionPane.showMessageDialog(MainWindow.getDefault(), "客户端长时间无法连接服务端,客户端即将退出!", "错误",
						JOptionPane.WARNING_MESSAGE);
				Runtime.getRuntime().halt(1);
			}
			tryCount--;
		}
	}
}
